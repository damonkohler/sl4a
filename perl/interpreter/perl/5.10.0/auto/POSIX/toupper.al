# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 108 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/toupper.al)"
sub toupper {
    usage "toupper(string)" if @_ != 1;
    uc($_[0]);
}

# end of POSIX::toupper
1;
