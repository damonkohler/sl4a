# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 186 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/pow.al)"
sub pow {
    usage "pow(x,exponent)" if @_ != 2;
    $_[0] ** $_[1];
}

# end of POSIX::pow
1;
