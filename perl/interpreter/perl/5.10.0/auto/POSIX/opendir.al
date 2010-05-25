# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 118 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/opendir.al)"
sub opendir {
    usage "opendir(directory)" if @_ != 1;
    my $dirhandle;
    CORE::opendir($dirhandle, $_[0])
	? $dirhandle
	: undef;
}

# end of POSIX::opendir
1;
