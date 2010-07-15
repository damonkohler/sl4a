# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX::SigRt;

#line 1003 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/SigRt/_getsig.al)"
sub _getsig {
    &_croak;
    my $rtsig = $_[0];
    # Allow (SIGRT)?MIN( + n)?, a common idiom when doing these things in C.
    $rtsig = $_SIGRTMIN + ($1 || 0)
	if $rtsig =~ /^(?:(?:SIG)?RT)?MIN(\s*\+\s*(\d+))?$/;
    return $rtsig;
}

# end of POSIX::SigRt::_getsig
1;
