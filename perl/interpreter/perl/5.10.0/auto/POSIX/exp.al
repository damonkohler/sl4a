# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 171 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/exp.al)"
sub exp {
    usage "exp(x)" if @_ != 1;
    CORE::exp($_[0]);
}

# end of POSIX::exp
1;
