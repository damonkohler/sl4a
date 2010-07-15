# NOTE: Derived from ../../lib/../xlib/arm/DynaLoader.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package DynaLoader;

#line 341 "../../lib/../xlib/arm/DynaLoader.pm (autosplit into ../../lib/../xlib/arm/auto/DynaLoader/dl_find_symbol_anywhere.al)"
sub dl_find_symbol_anywhere
{
    my $sym = shift;
    my $libref;
    foreach $libref (@dl_librefs) {
	my $symref = dl_find_symbol($libref,$sym);
	return $symref if $symref;
    }
    return undef;
}

1;
# end of DynaLoader::dl_find_symbol_anywhere
