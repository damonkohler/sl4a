# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 701 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/getpgrp.al)"
sub getpgrp {
    usage "getpgrp()" if @_ != 0;
    CORE::getpgrp;
}

# end of POSIX::getpgrp
1;
