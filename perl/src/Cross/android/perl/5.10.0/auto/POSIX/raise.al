# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 232 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/raise.al)"
sub raise {
    usage "raise(sig)" if @_ != 1;
    CORE::kill $_[0], $$;	# Is this good enough?
}

# end of POSIX::raise
1;
