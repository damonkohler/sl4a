#!/usr/bin/env python
#
#  Copyright (C) 2017 shimoda
#  Copyright (C) 2016 Google, Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at:
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import collections
import itertools
import os
import re
import subprocess

# Parsing states:
# STATE_INITIAL: looking for rpc or function defintion
# STATE_RPC_DECORATOR: in the middle of a multi-line rpc definition
# STATE_FUNCTION_DECORATOR: in the middle of a multi-line function definition
# STATE_COMPLETE: done parsing a function
STATE_INITIAL = 1
STATE_RPC_DECORATOR = 2
STATE_FUNCTION_DEFINITION = 3
STATE_COMPLETE = 4

# RE to match key=value tuples with matching quoting on value.
KEY_VAL_RE = re.compile(r'''
        (?P<key>\w+)\s*=\s* # Key consists of only alphanumerics
        (?P<quote>["']?)    # Optional quote character.
        (?P<value>.*?)      # Value is a non greedy match
        (?P=quote)          # Closing quote equals the first.
        ($|,)               # Entry ends with comma or end of string
    ''', re.VERBOSE)

# RE to match a function definition and extract out the function name.
FUNC_RE = re.compile(r'.+\s+(\w+)\s*\(.*')


class Function(object):
    """Represents a RPC-exported function."""

    def __init__(self, rpc_def, func_def):
        """Constructs a function object given its RPC and
            function signature."""
        self._function = ''
        self._signature = ''
        self._description = ''
        self._returns = ''

        self._ParseRpcDefinition(rpc_def)
        self._ParseFunctionDefinition(func_def)

    def _ParseRpcDefinition(self, s):
        """Parse RPC definition."""
        # collapse string concatenation
        s = s.replace('" + "', '')
        s = s.strip('()')
        for m in KEY_VAL_RE.finditer(s):
            if m.group('key') == 'description':
                self._description = m.group('value')
            if m.group('key') == 'returns':
                self._returns = m.group('value')

    def _ParseFunctionDefinition(self, s):
        """Parse function definition."""
        # Remove some keywords we don't care about.
        s = s.replace('public ', '')
        s = s.replace('synchronized ', '')
        # Remove any throw specifications.
        s = re.sub('\s+throws.*', '', s)
        s = s.strip('{')
        # Remove all the RPC parameter annotations.
        s = s.replace('@RpcOptional ', '')
        s = s.replace('@RpcOptional() ', '')
        s = re.sub('@RpcParameter\s*\(.+?\)\s+', '', s)
        s = re.sub('@RpcDefault\s*\(.+?\)\s+', '', s)
        m = FUNC_RE.match(s)
        if m:
            self._function = m.group(1)
        self._signature = s.strip()

    @property
    def function(self):
        return self._function

    @property
    def signature(self):
        return self._signature

    @property
    def description(self):
        return self._description

    @property
    def returns(self):
        return self._returns


class DocGenerator(object):
    """Documentation genereator."""

    def __init__(self, basepath):
        """Construct based on all the *Facade.java files
            in the given basepath."""
        self._functions = collections.defaultdict(list)

        for path, dirs, files in os.walk(basepath):
            for f in files:
                if f.endswith('Facade.java'):
                    self._Parse(os.path.join(path, f))

    def _Parse(self, filename):
        """Parser state machine for a single file."""
        state = STATE_INITIAL
        self._current_rpc = ''
        self._current_function = ''

        with open(filename, 'r') as f:
            for line in f.readlines():
                line = line.strip()
                if state == STATE_INITIAL:
                    state = self._ParseLineInitial(line)
                elif state == STATE_RPC_DECORATOR:
                    state = self._ParseLineRpcDecorator(line)
                elif state == STATE_FUNCTION_DEFINITION:
                    state = self._ParseLineFunctionDefinition(line)

                if state == STATE_COMPLETE:
                    self._EmitFunction(filename)
                    state = STATE_INITIAL

    def _ParseLineInitial(self, line):
        """Parse a line while in STATE_INITIAL."""
        if line.startswith('@Rpc('):
            self._current_rpc = line[4:]
            if not line.endswith(')'):
                # Multi-line RPC definition
                return STATE_RPC_DECORATOR
        elif line.startswith('public'):
            self._current_function = line
            if not line.endswith('{'):
                # Multi-line function definition
                return STATE_FUNCTION_DEFINITION
            else:
                return STATE_COMPLETE
        return STATE_INITIAL

    def _ParseLineRpcDecorator(self, line):
        """Parse a line while in STATE_RPC_DECORATOR."""
        self._current_rpc += ' ' + line
        if line.endswith(')'):
            # Done with RPC definition
            return STATE_INITIAL
        else:
            # Multi-line RPC definition
            return STATE_RPC_DECORATOR

    def _ParseLineFunctionDefinition(self, line):
        """Parse a line while in STATE_FUNCTION_DEFINITION."""
        self._current_function += ' ' + line
        if line.endswith('{'):
            # Done with function definition
            return STATE_COMPLETE
        else:
            # Multi-line function definition
            return STATE_FUNCTION_DEFINITION

    def _EmitFunction(self, filename):
        """Store a function definition from the current parse state."""
        if self._current_rpc and self._current_function:
            module = os.path.basename(filename)[0:-5]
            f = Function(self._current_rpc, self._current_function)
            if f.function:
                self._functions[module].append(f)

        self._current_rpc = None
        self._current_function = None

    def WriteOutput(self, filename):
        git_rev = None
        try:
            git_rev = subprocess.check_output('git rev-parse HEAD',
                                              shell=True).strip()
        except subprocess.CalledProcessError as e:
            # Getting the commit ID is optional;we continue if we cannot get it
            try:
                git_rev = subprocess.check_output('hg parents | head -n1',
                                                  shell=True).strip()
            except subprocess.CalledProcessError as e:
                pass

        with open(filename, 'w') as f:
            if git_rev:
                f.write('Generated at commit `%s`\n\n' % git_rev)
            # Write table of contents
            for module in sorted(self._functions.keys()):
                f.write('**%s**\n\n' % module)
                for func in self._functions[module]:
                    f.write('  * [%s](#%s)\n' %
                            (func.function, func.function.lower()))
                f.write('\n')

            f.write('# Method descriptions\n\n')
            for func in itertools.chain.from_iterable(
                    self._functions.itervalues()):
                f.write('## %s\n\n' % func.function)
                f.write('```\n')
                f.write('%s\n\n' % func.signature)
                f.write('%s\n' % func.description)
                if func.returns:
                    if func.returns.lower().startswith('return'):
                        f.write('\n%s\n' % func.returns)
                    else:
                        f.write('\nReturns %s\n' % func.returns)
                f.write('```\n\n')


# Main
if __name__ == "__main__":
    basepath = os.path.abspath(os.path.join(os.path.dirname(
        os.path.realpath(__file__)), '..', 'android'))
    g = DocGenerator(basepath)
    g.WriteOutput(os.path.join(basepath, '../docs/ApiReference.md'))
# vi: ft=python:et:ts=4:nowrap
