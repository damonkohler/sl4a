#!./perl -w

BEGIN {
    chdir 't' if -d 't';
    @INC = '../lib';
    require './test.pl';
}

plan tests => 1;

my $rx = qr//;

is(ref $rx, "Regexp", "qr// blessed into `Regexp' by default");
