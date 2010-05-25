# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 344 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/perror.al)"
sub perror {
    print STDERR "@_: " if @_;
    print STDERR $!,"\n";
}

# end of POSIX::perror
1;
