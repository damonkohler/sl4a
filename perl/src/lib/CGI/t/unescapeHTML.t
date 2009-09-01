use lib 't/lib';
use Test::More 'no_plan';
use CGI 'unescapeHTML';

is( unescapeHTML( '&amp;'), '&', 'unescapeHTML: &'); 
is( unescapeHTML( '&quot;'), '"', 'unescapeHTML: "'); 
TODO: {
    local $TODO = 'waiting on patch. Reference: https://rt.cpan.org/Ticket/Display.html?id=39122';
    is( unescapeHTML( 'Bob & Tom went to the store; Where did you go?'), 
         'Bob & Tom went to the store; Where did you go?', 'unescapeHTML: a case where &...; should not be escaped.');
}
