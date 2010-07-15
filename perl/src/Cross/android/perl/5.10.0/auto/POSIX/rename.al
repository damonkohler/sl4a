# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 371 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/rename.al)"
sub rename {
    usage "rename(oldfilename, newfilename)" if @_ != 2;
    CORE::rename($_[0], $_[1]);
}

# end of POSIX::rename
1;
