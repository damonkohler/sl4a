# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 201 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/getpwnam.al)"
sub getpwnam {
    usage "getpwnam(name)" if @_ != 1;
    CORE::getpwnam($_[0]);
}

# end of POSIX::getpwnam
1;
