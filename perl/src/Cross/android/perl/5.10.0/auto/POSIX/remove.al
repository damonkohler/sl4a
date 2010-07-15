# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 366 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/remove.al)"
sub remove {
    usage "remove(filename)" if @_ != 1;
    (-d $_[0]) ? CORE::rmdir($_[0]) : CORE::unlink($_[0]);
}

# end of POSIX::remove
1;
