# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX::SigAction;

#line 985 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/SigAction/mask.al)"
sub mask    { $_[0]->{MASK}    = $_[1] if @_ > 1; $_[0]->{MASK} };
# end of POSIX::SigAction::mask
1;
