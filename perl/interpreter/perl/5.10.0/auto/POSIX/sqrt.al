# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 196 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/sqrt.al)"
sub sqrt {
    usage "sqrt(x)" if @_ != 1;
    CORE::sqrt($_[0]);
}

# end of POSIX::sqrt
1;
