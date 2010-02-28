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

#
# Author: Jarkko Hietaniemi <jhi@google.com>
#

package Android;

use strict;

use vars qw($VERSION $AUTOLOAD);

$VERSION = 0.001;

use IO::Socket;
use JSON;
use Getopt::Long;
use Data::Dumper;

my %Opt;

# You can specify the server port number to contact either via the
# $ENV{AP_PORT} (the default behaviour) or via --port=n (when debugging
# Android.pm and/or running in server mode, the default being client mode).
# Running a test server:
# perl -w Android.pm --port=4321 --trace --server
# Running a test client:
# perl -w Android.pm --port=4321 --trace --request Bar 1 2 foo 3.4

# This BEGIN parses options, if any.
BEGIN {
    %Opt = (port => $ENV{AP_PORT} ? $ENV{AP_PORT} : 4321);
    GetOptions('port=i' => \$Opt{port},
               'server' => \$Opt{server},
               'request' => \$Opt{request},
               'trace' => \$Opt{trace}) or
                 die "$0: Usage: $0 [--port=n] [--server] [--request method ...]\n";
    (defined $Opt{port} && $Opt{port} =~ /^\d+$/)
      or die "$0: AP_PORT '$Opt{port}' undefined or illegal\n";
}

# server() is run if --server is given to Android.pm.
sub server {
  # getprotobyname() is still a fatally unimplemented stub in Android 2.0.1.
  my $proto = eval { getprotobyname('tcp') } || 6;
  my $server = IO::Socket::INET->new(Proto     => 'tcp',
                                     LocalPort => $Opt{port},
                                     Listen    => SOMAXCONN,
                                     Reuse     => 1);
  die "$0: Cannot start server: $!\n" unless defined $server;
  if ($Opt{trace}) {
      show_trace(qq[$0: server: accepting in port $Opt{port}]);
  }
  while (defined(my $client = $server->accept())) {
      show_trace(qq[$0: server: client $client]) if $Opt{trace};
      $client->autoflush(1);
      my $json = readline($client);
      chomp($json);
      show_trace(qq[server: rcvd: "$json"]) if $Opt{trace};
      print $client $json, "\n";  # We just echo back what they said to us.
      show_trace(qq[server: sent: "$json"]) if $Opt{trace};
      close($client);
  }
}

sub new {
    my $class = shift;
    if (@_) {
        print STDERR "$0: client: new() expected no arguments, got @_\n";
    }
    my $fh = IO::Socket::INET->new(Proto    => 'tcp',
                                   PeerAddr => 'localhost',
                                   PeerPort => $Opt{port})
        or die "$0: Cannot connect to server port $Opt{port} on localhost\n";
    $fh->autoflush(1);
    if ($Opt{trace}) {
        show_trace(qq[Android: server in port $Opt{port}]);
    }
    bless {
        conn => $fh,
        id   => 0,
    }, $class;
}

# One can use this to set the proxy object to display what's being
# sent down and up the wire (as JSON), or query the state of tracing.
# If tracing is on, the client library will also dump the Perl result
# that was decoded from JSON.
sub trace {
    if (@_ == 2) {
        $_[0]->{trace} = $_[1];
    } else {
        return $_[0]->{trace};
    }
}

# Utility function for showing traces.
sub show_trace {
  print STDERR "<<@_>>\n";
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
    my $self = shift;
    $self->_close();
    print STDERR "$0: client: connection closed\n";
}

# Given a method and parameters, call the server with JSON,
# and return the parsed the response JSON.  If the server side
# looks to be dead, close the connection and return undef.
sub do_rpc {
    my $self = shift;
    if ($self->trace) {
        show_trace(qq[do_rpc: $self: @_]);
    }
    my $method = pop;
    my $request = to_json({ id => $self->{id},
                            method => $method,
                            params => [ @_ ] });
    if (defined $self->{conn}) {
        print { $self->{conn} } $request, "\n";
        if ($self->trace) {
            show_trace(qq[client: sent: "$request"]);
        }
        $self->{id}++;
        my $response = readline($self->{conn});
        chomp $response;
        if ($self->trace) {
            show_trace(qq[client: rcvd: "$response"]);
        }
        if (defined $response && length $response) {
            my $result = from_json($response);
            my $success = 0;
            my $error;
            if (defined $result) {
                if (ref $result eq 'HASH') {
                    if (defined $result->{error}) {
                        $error = to_json($result->{error});
                    } else {
                        $success = 1;
                    }
                } else {
                    $error = "illegal JSON reply: $result";
                }
            }
            unless ($success || defined $error) {
                $error = "unknown JSON error";
            }
            if (defined $error) {
                printf STDERR "$0: client: error: %s\n", $error;
            }
            if ($Opt{trace}) {
                print STDERR Data::Dumper->Dump([$result], [qw(result)]);
            }
            return $result;
        }
    }
    $self->close;
    return;
}

# Return stubs that call do_rpc() with the method name smuggled in.
sub rpc_maker {
    if ($Opt{trace}) {
        show_trace(qq[rpc_maker: @_]);
    }
    my $method = shift;
    sub {
        push @_, $method;
        goto &do_rpc;  # Knock the stub out of the call stack.
    }
}

# AUTOLOAD installs RPC proxies for all unknown methods.
sub AUTOLOAD {
    if ($Opt{trace}) {
        show_trace(qq[AUTOLOAD=$AUTOLOAD, @_]);
    }
    my ($method) = ($AUTOLOAD =~ /::(\w+)$/);
    return if $method eq 'DESTROY';
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

# This BEGIN block either invokes server() or sends a client request,
# or does nothing (the case of using Android.pm as a client library).
sub BEGIN {
    if (defined $Opt{server}) {
        &server;
    } elsif (defined $Opt{request}) {
        my $android = Android->new();
        $android->trace(1) if $Opt{trace};
        my $method = shift @ARGV;
        $android->$method(@ARGV);
        exit(0);
    }
}

1;
