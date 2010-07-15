# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 534 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/strerror.al)"
sub strerror {
    usage "strerror(errno)" if @_ != 1;
    local $! = $_[0];
    $! . "";
}

# end of POSIX::strerror
1;
