# Copyright (C) 2009 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

# Author: Jarkko Hietaniemi <jhi@iki.fi>

package Android;

use strict;

use vars qw($VERSION $PORT $AUTOLOAD);

$VERSION = 0.001;

use IO::Socket;
use JSON;

BEGIN {
    $PORT = $ENV{AP_PORT} or die "$0: AP_PORT undefined\n";
}

sub new {
    my $class = shift;
    if (@_) {
	print STDERR "$0: new() expected no arguments, got @_\n";
    }
    my $fh = IO::Socket::INET->new(Proto    => 'tcp',
				   PeerAddr => 'localhost',
				   PeerPort => $PORT)
	or die "$0: Cannot connect to port $PORT on localhost";
    $fh->autoflush(1);
    bless {
	conn => $fh,
	id   => 0,
    }, $class; 
}

# One can use this to set the proxy object to display what's being
# sent down the wire (as JSON), or query the state of tracing.
sub trace {
    if (@_ == 2) {
	$_[0]->{trace} = $_[1];
    } else {
	return $_[0]->{trace};
    }
}

# The connection is implicitly closed when the proxy object goes out
# of scope, but one can use the close() method to explicitly terminate
# the connection.  This is also used internally by the do_rpc() in
# case the server end looks to have gone away.  The _close() closes
# the connection quietly, close() closes the connection noisily.
sub _close {
    if (defined $_[0]->{conn}) {
	close($_[0]->{conn});
	undef $_[0]->{conn};
    }
}
sub close {
    $_[0]->_close();
    print STDERR "$0: connection closed\n";
}

# Given a method and parameters, call the server with JSON,
# and return the parsed the response JSON.  If the server side
# looks to be dead, close the connection and return undef.
sub do_rpc {
    my $self = shift;
    my $method = pop;
    my $request = to_json({ id => $self->{id},
			    method => $method,
			    params => [ @_ ] }) . "\n";
    if ($self->trace) { print STDERR ">> $request" }
    if (defined $self->{conn}) {
	print { $self->{conn} } $request;
	$self->{id}++;
	my $response = readline($self->{conn});
	if ($self->trace) { print STDERR "<< $response" }
	if (defined $response && length $response) {
	    my $result = from_json($response);
	    if (defined $result && exists $result->{error}) {
		print STDERR "$0: error: ", to_json($result->{error}), "\n";
	    }
	    return $result;
	}
    }
    $self->close;
    return;
} 

# Return stubs that call do_rpc() with the method name smuggled in.
sub rpc_maker {
    my $method = shift;
    sub {
	push @_, $method;
	goto &do_rpc;  # Knock the stub out of the call stack.
    }
}

sub help {
    my ($self, $method) = @_;
    my $help = defined $method ? $self->help($method) : $self->_help();
    if (exists $help->{error}) {
	print STDERR "Failed to retrieve help text.\n";
    } else {
	for my $m (@{ $help->{result} }) {
	    print "$m\n";
	}
    }
}

sub AUTOLOAD {
    my ($method) = ($AUTOLOAD =~ /::(\w+)$/);
    return if $method eq 'DESTROY';
    # print STDERR "$0: installing proxy method '$method'\n";
    my $rpc = rpc_maker($method);
    {
	# Install the RPC proxy method, we will not came here
	# any more for the same method name.
	no strict 'refs';
	*$method = $rpc;
    }
    goto &$rpc;  # Call the RPC now.
}

sub DESTROY {
    $_[0]->_close();
}

1;
