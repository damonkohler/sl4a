# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 191 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/sin.al)"
sub sin {
    usage "sin(x)" if @_ != 1;
    CORE::sin($_[0]);
}

# end of POSIX::sin
1;
