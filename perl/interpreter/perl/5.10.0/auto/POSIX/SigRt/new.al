# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX::SigRt;

#line 1025 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/SigRt/new.al)"
sub new {
    my ($rtsig, $handler, $flags) = @_;
    my $sigset = POSIX::SigSet->new($rtsig);
    my $sigact = POSIX::SigAction->new($handler,
				       $sigset,
				       $flags);
    POSIX::sigaction($rtsig, $sigact);
}

# end of POSIX::SigRt::new
1;
