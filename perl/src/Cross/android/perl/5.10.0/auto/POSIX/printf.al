# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 349 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/printf.al)"
sub printf {
    usage "printf(pattern, args...)" if @_ < 1;
    CORE::printf STDOUT @_;
}

# end of POSIX::printf
1;
