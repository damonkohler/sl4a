# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 329 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/getc.al)"
sub getc {
    usage "getc(handle)" if @_ != 1;
    CORE::getc($_[0]);
}

# end of POSIX::getc
1;
