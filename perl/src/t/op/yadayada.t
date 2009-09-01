#!./perl

BEGIN {
    chdir 't' if -d 't';
    @INC = '../lib';
    require './test.pl';
}

use strict;

plan 1;

my $err = "Unimplemented at $0 line " . ( __LINE__ + 2 ) . ".\n";

eval { ... };

is $@, $err;
