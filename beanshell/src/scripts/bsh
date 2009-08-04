#!/bin/sh
#
# Run the BeanShell interpreter on a file with command line arguments.  If the
# user has bsh.Interpreter in their classpath then the interpreter is executed 
# with the user's classpath.  Otherwise, the default bsh path specified in 
# DEFAULTBSH is added to end of the user's path.
#
# If you wish to avoid the classpath test (which uses javap) then you can set
# the environment variable NOBSHCHECK to a value and the user classpath will
# always be used.
#

# The (hard coded) location of a default bsh jar.
# e.g. Win
#DEFAULTBSH=`cygpath --windows $HOME/pkg/bsh-1.3a1.jar`
# e.g. Unix
DEFAULTBSH=/home/pat/bin/bsh-1.3a1.jar

name=`basename $0`
if [ $name = "bshd" ]; then
	debug="-Ddebug=true"
fi

# Determine if we're running under Unix or Cygwin/windows
if uname | grep -i 'cygwin' 2>&1 > /dev/null
then
	# (Using cygpath has issues with drive letters, etc.)
	_PATHSEP=';'
	_CYGWIN=true;
else
	_PATHSEP=':'
fi

# Avoid adding the default bsh jar to the classpath unecessarily.
# For most purposes this wouldn't matter... but for testing bsh we'd like to
# avoid extra bsh junk.
#
if [ "$NOBSHCHECK" ] || javap bsh.Interpreter 2>&1 | 
	grep 'public class bsh.Interpreter extends' > /dev/null
then
	# Have bsh
	java $debug bsh.Interpreter $* 
else
	if [ ! -f $DEFAULTBSH ]; then
		echo "BeanShell not found at path: $DEFAULTBSH"
		exit;
	fi

	# Don't have bsh
	# Cygwin doesn't like an extra leading path separator, avoid it
	if [ "$CLASSPATH" ]; then
		path="${CLASSPATH}${_PATHSEP}${DEFAULTBSH}"
	else
		path=${DEFAULTBSH}
	fi

	java $debug -classpath $path bsh.Interpreter $* 
fi

