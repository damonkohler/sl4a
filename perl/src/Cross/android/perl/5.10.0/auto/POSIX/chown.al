# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 641 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/chown.al)"
sub chown {
    usage "chown(uid, gid, filename)" if @_ != 3;
    CORE::chown($_[0], $_[1], $_[2]);
}

# end of POSIX::chown
1;
