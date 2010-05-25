# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 721 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/isatty.al)"
sub isatty {
    usage "isatty(filehandle)" if @_ != 1;
    -t $_[0];
}

# end of POSIX::isatty
1;
