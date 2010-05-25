# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 161 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/atan2.al)"
sub atan2 {
    usage "atan2(x,y)" if @_ != 2;
    CORE::atan2($_[0], $_[1]);
}

# end of POSIX::atan2
1;
