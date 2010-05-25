# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 339 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/gets.al)"
sub gets {
    usage "gets()" if @_ != 0;
    scalar <STDIN>;
}

# end of POSIX::gets
1;
