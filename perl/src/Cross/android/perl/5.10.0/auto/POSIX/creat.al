# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 141 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/creat.al)"
sub creat {
    usage "creat(filename, mode)" if @_ != 2;
    &open($_[0], &O_WRONLY | &O_CREAT | &O_TRUNC, $_[1]);
}

# end of POSIX::creat
1;
