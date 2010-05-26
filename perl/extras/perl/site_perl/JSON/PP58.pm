package JSON::PP58;

use 5.008;
use strict;

my @properties;

$JSON::PP58::VERSION = '1.02';


BEGIN {

    unless ( defined &utf8::is_utf8 ) {
       require Encode;
       *utf8::is_utf8 = *Encode::is_utf8;
    }

    *JSON::PP::JSON_PP_encode_ascii      = \&JSON::PP::_encode_ascii;
    *JSON::PP::JSON_PP_encode_latin1     = \&JSON::PP::_encode_latin1;
    *JSON::PP::JSON_PP_decode_surrogates = \&JSON::PP::_decode_surrogates;
    *JSON::PP::JSON_PP_decode_unicode    = \&JSON::PP::_decode_unicode;

    if ($] >= 5.008 and $] < 5.008003) { # join() in 5.8.0 - 5.8.2 is broken.
        package JSON::PP;
        require subs;
        subs->import('join');
        eval q|
            sub join {
                return '' if (@_ < 2);
                my $j   = shift;
                my $str = shift;
                for (@_) { $str .= $j . $_; }
                return $str;
            }
        |;
    }

}


sub JSON::PP::incr_parse {
    local $Carp::CarpLevel = 1;
    ( $_[0]->{_incr_parser} ||= JSON::PP::IncrParser->new )->incr_parse( @_ );
}


sub JSON::PP::incr_text : lvalue {
    $_[0]->{_incr_parser} ||= JSON::PP::IncrParser->new;

    if ( $_[0]->{_incr_parser}->{incr_parsing} ) {
        Carp::croak("incr_text can not be called when the incremental parser already started parsing");
    }
    $_[0]->{_incr_parser}->{incr_text};
}


sub JSON::PP::incr_skip {
    ( $_[0]->{_incr_parser} ||= JSON::PP::IncrParser->new )->incr_skip;
}


sub JSON::PP::incr_reset {
    ( $_[0]->{_incr_parser} ||= JSON::PP::IncrParser->new )->incr_reset;
}


1;
__END__

=pod

=head1 NAME

JSON::PP58 - Helper module in using JSON::PP in Perl 5.8 and lator

=head1 DESCRIPTION

JSON::PP calls internally.

=head1 AUTHOR

Makamaka Hannyaharamitu, E<lt>makamaka[at]cpan.orgE<gt>


=head1 COPYRIGHT AND LICENSE

Copyright 2008 by Makamaka Hannyaharamitu

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself. 

=cut

