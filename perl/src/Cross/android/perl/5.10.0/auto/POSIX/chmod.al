# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 577 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/chmod.al)"
sub chmod {
    usage "chmod(mode, filename)" if @_ != 2;
    CORE::chmod($_[0], $_[1]);
}

# end of POSIX::chmod
1;
