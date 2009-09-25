#!./perl

BEGIN {
   if( $ENV{PERL_CORE} ) {
        chdir 't' if -d 't';
        @INC = qw(../lib);
    }
}

use FileCache;

END{
  unlink("Foo_Bar");
}
print "1..1\n";

{# Test 5: that close is overridden properly within the caller
     cacheout local $_ = "Foo_Bar";
     print $_ "Hello World\n";
     close($_);
     print 'not ' if fileno($_);
     print "ok 1\n";
}
