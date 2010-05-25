# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 146 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/fcntl.al)"
sub fcntl {
    usage "fcntl(filehandle, cmd, arg)" if @_ != 3;
    CORE::fcntl($_[0], $_[1], $_[2]);
}

# end of POSIX::fcntl
1;
