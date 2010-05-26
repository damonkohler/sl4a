# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 103 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/tolower.al)"
sub tolower {
    usage "tolower(string)" if @_ != 1;
    lc($_[0]);
}

# end of POSIX::tolower
1;
