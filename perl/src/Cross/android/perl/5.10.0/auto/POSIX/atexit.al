# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 419 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/atexit.al)"
sub atexit {
    unimpl "atexit() is C-specific: use END {} instead";
}

# end of POSIX::atexit
1;
