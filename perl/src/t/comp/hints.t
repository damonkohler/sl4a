#!./perl

# Tests the scoping of $^H and %^H

BEGIN {
    chdir 't' if -d 't';
    @INC = qw(. ../lib);
}


BEGIN { print "1..32\n"; }
BEGIN {
    print "not " if exists $^H{foo};
    print "ok 1 - \$^H{foo} doesn't exist initially\n";
    if (${^OPEN}) {
	print "not " unless $^H & 0x00020000;
	print "ok 2 - \$^H contains HINT_LOCALIZE_HH initially with ${^OPEN}\n";
    } else {
	print "not " if $^H & 0x00020000;
	print "ok 2 - \$^H doesn't contain HINT_LOCALIZE_HH initially\n";
    }
}
{
    # simulate a pragma -- don't forget HINT_LOCALIZE_HH
    BEGIN { $^H |= 0x04020000; $^H{foo} = "a"; }
    BEGIN {
	print "not " if $^H{foo} ne "a";
	print "ok 3 - \$^H{foo} is now 'a'\n";
	print "not " unless $^H & 0x00020000;
	print "ok 4 - \$^H contains HINT_LOCALIZE_HH while compiling\n";
    }
    {
	BEGIN { $^H |= 0x00020000; $^H{foo} = "b"; }
	BEGIN {
	    print "not " if $^H{foo} ne "b";
	    print "ok 5 - \$^H{foo} is now 'b'\n";
	}
    }
    BEGIN {
	print "not " if $^H{foo} ne "a";
	print "ok 6 - \$^H{foo} restored to 'a'\n";
    }
    # The pragma settings disappear after compilation
    # (test at CHECK-time and at run-time)
    CHECK {
	print "not " if exists $^H{foo};
	print "ok 9 - \$^H{foo} doesn't exist when compilation complete\n";
	if (${^OPEN}) {
	    print "not " unless $^H & 0x00020000;
	    print "ok 10 - \$^H contains HINT_LOCALIZE_HH when compilation complete with ${^OPEN}\n";
	} else {
	    print "not " if $^H & 0x00020000;
	    print "ok 10 - \$^H doesn't contain HINT_LOCALIZE_HH when compilation complete\n";
	}
    }
    print "not " if exists $^H{foo};
    print "ok 11 - \$^H{foo} doesn't exist at runtime\n";
    if (${^OPEN}) {
	print "not " unless $^H & 0x00020000;
	print "ok 12 - \$^H contains HINT_LOCALIZE_HH at run-time with ${^OPEN}\n";
    } else {
	print "not " if $^H & 0x00020000;
	print "ok 12 - \$^H doesn't contain HINT_LOCALIZE_HH at run-time\n";
    }
    # op_entereval should keep the pragmas it was compiled with
    eval q*
	print "not " if $^H{foo} ne "a";
	print "ok 13 - \$^H{foo} is 'a' at eval-\"\" time\n";
	print "not " unless $^H & 0x00020000;
	print "ok 14 - \$^H contains HINT_LOCALIZE_HH at eval\"\"-time\n";
    *;
}
BEGIN {
    print "not " if exists $^H{foo};
    print "ok 7 - \$^H{foo} doesn't exist while finishing compilation\n";
    if (${^OPEN}) {
	print "not " unless $^H & 0x00020000;
	print "ok 8 - \$^H contains HINT_LOCALIZE_HH while finishing compilation with ${^OPEN}\n";
    } else {
	print "not " if $^H & 0x00020000;
	print "ok 8 - \$^H doesn't contain HINT_LOCALIZE_HH while finishing compilation\n";
    }
}

require 'test.pl';

# bug #27040: hints hash was being double-freed
my $result = runperl(
    prog => '$^H |= 0x20000; eval q{BEGIN { $^H |= 0x20000 }}',
    stderr => 1
);
print "not " if length $result;
print "ok 15 - double-freeing hints hash\n";
print "# got: $result\n" if length $result;

{
    BEGIN{$^H{x}=1};
    for my $tno (16..17) {
        eval q(
            print $^H{x}==1 && !$^H{y} ? "ok $tno\n" : "not ok $tno\n";
            $^H{y} = 1;
        );
        if ($@) {
            (my $str = $@)=~s/^/# /gm;
            print "not ok $tno\n$str\n";
        }
    }
}

{
    $[ = 11;
    print +($[ == 11 ? "" : "not "), "ok 18 - setting \$[ affects \$[\n";
    our $t11; BEGIN { $t11 = $^H{'$['} }
    print +($t11 == 11 ? "" : "not "), "ok 19 - setting \$[ affects \$^H{'\$['}\n";

    BEGIN { $^H{'$['} = 22 }
    print +($[ == 22 ? "" : "not "), "ok 20 - setting \$^H{'\$['} affects \$[\n";
    our $t22; BEGIN { $t22 = $^H{'$['} }
    print +($t22 == 22 ? "" : "not "), "ok 21 - setting \$^H{'\$['} affects \$^H{'\$['}\n";

    BEGIN { %^H = () }
    print +($[ == 0 ? "" : "not "), "ok 22 - clearing \%^H affects \$[\n";
    our $t0; BEGIN { $t0 = $^H{'$['} }
    print +($t0 == 0 ? "" : "not "), "ok 23 - clearing \%^H affects \$^H{'\$['}\n";
}

{
    $[ = 13;
    BEGIN { $^H |= 0x04000000; $^H{foo} = "z"; }

    our($ri0, $rf0); BEGIN { $ri0 = $^H; $rf0 = $^H{foo}; }
    print +($[ == 13 ? "" : "not "), "ok 24 - \$[ correct before require\n";
    print +($ri0 & 0x04000000 ? "" : "not "), "ok 25 - \$^H correct before require\n";
    print +($rf0 eq "z" ? "" : "not "), "ok 26 - \$^H{foo} correct before require\n";

    our($ra1, $ri1, $rf1, $rfe1);
    BEGIN { require "comp/hints.aux"; }
    print +($ra1 == 0 ? "" : "not "), "ok 27 - \$[ cleared for require\n";
    print +(!($ri1 & 0x04000000) ? "" : "not "), "ok 28 - \$^H cleared for require\n";
    print +(!defined($rf1) && !$rfe1 ? "" : "not "), "ok 29 - \$^H{foo} cleared for require\n";

    our($ri2, $rf2); BEGIN { $ri2 = $^H; $rf2 = $^H{foo}; }
    print +($[ == 13 ? "" : "not "), "ok 30 - \$[ correct after require\n";
    print +($ri2 & 0x04000000 ? "" : "not "), "ok 31 - \$^H correct after require\n";
    print +($rf2 eq "z" ? "" : "not "), "ok 32 - \$^H{foo} correct after require\n";
}
