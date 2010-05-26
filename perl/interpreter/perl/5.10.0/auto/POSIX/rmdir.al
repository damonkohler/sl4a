# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 731 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/rmdir.al)"
sub rmdir {
    usage "rmdir(directoryname)" if @_ != 1;
    CORE::rmdir($_[0]);
}

# end of POSIX::rmdir
1;
