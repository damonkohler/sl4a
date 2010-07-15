# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 376 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/rewind.al)"
sub rewind {
    usage "rewind(filehandle)" if @_ != 1;
    CORE::seek($_[0],0,0);
}

# end of POSIX::rewind
1;
