#!/usr/bin/perl
# Finds the files that have the same name, case insensitively,
# in the current directory and its subdirectories

BEGIN {
    chdir 't' if -d 't';
    @INC = '../lib';
}



use warnings;
use strict;
use File::Find;

my %files;
my $test_count = 0;

find(sub {
	   my $name = $File::Find::name;
	   # Assumes that the path separator is exactly one character.
	   $name =~ s/^\.\..//;
	   push @{$files{lc $name}}, $name;
	 }, '.');

my $failed;

foreach (values %files) {
    if (@$_ > 1) {
		print "not ok ".++$test_count. " - ". join(", ", @$_), "\n";
    } else {
		print "ok ".++$test_count. " - ". join(", ", @$_), "\n";
	}
}

print "1..".$test_count."\n";
