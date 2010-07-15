# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 636 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/chdir.al)"
sub chdir {
    usage "chdir(directory)" if @_ != 1;
    CORE::chdir($_[0]);
}

# end of POSIX::chdir
1;
