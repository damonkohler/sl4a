# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 568 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/strstr.al)"
sub strstr {
    usage "strstr(big, little)" if @_ != 2;
    CORE::index($_[0], $_[1]);
}

# end of POSIX::strstr
1;
