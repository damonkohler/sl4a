package JSON::PP;

# JSON-2.0

use 5.005;
use strict;
use base qw(Exporter);
use overload;

use Carp ();
use B ();
#use Devel::Peek;

$JSON::PP::VERSION = '2.24000';

@JSON::PP::EXPORT = qw(encode_json decode_json from_json to_json);

# instead of hash-access, i tried index-access for speed.
# but this method is not faster than what i expected. so it will be changed.

use constant P_ASCII                => 0;
use constant P_LATIN1               => 1;
use constant P_UTF8                 => 2;
use constant P_INDENT               => 3;
use constant P_CANONICAL            => 4;
use constant P_SPACE_BEFORE         => 5;
use constant P_SPACE_AFTER          => 6;
use constant P_ALLOW_NONREF         => 7;
use constant P_SHRINK               => 8;
use constant P_ALLOW_BLESSED        => 9;
use constant P_CONVERT_BLESSED      => 10;
use constant P_RELAXED              => 11;

use constant P_LOOSE                => 12;
use constant P_ALLOW_BIGNUM         => 13;
use constant P_ALLOW_BAREKEY        => 14;
use constant P_ALLOW_SINGLEQUOTE    => 15;
use constant P_ESCAPE_SLASH         => 16;
use constant P_AS_NONBLESSED        => 17;

use constant P_ALLOW_UNKNOWN        => 18;

BEGIN {
    my @xs_compati_bit_properties = qw(
            latin1 ascii utf8 indent canonical space_before space_after allow_nonref shrink
            allow_blessed convert_blessed relaxed allow_unknown
    );
    my @pp_bit_properties = qw(
            allow_singlequote allow_bignum loose
            allow_barekey escape_slash as_nonblessed
    );

    # Perl version check, Unicode handling is enable?
    # Helper module sets @JSON::PP::_properties.

    my $helper = $] >= 5.008 ? 'JSON::PP58'
               : $] >= 5.006 ? 'JSON::PP56'
               :               'JSON::PP5005'
               ;

    eval qq| require $helper |;
    if ($@) { Carp::croak $@; }

    for my $name (@xs_compati_bit_properties, @pp_bit_properties) {
        my $flag_name = 'P_' . uc($name);

        eval qq/
            sub $name {
                my \$enable = defined \$_[1] ? \$_[1] : 1;

                if (\$enable) {
                    \$_[0]->{PROPS}->[$flag_name] = 1;
                }
                else {
                    \$_[0]->{PROPS}->[$flag_name] = 0;
                }

                \$_[0];
            }

            sub get_$name {
                \$_[0]->{PROPS}->[$flag_name] ? 1 : '';
            }
        /;
    }

}



# Functions

my %encode_allow_method
     = map {($_ => 1)} qw/utf8 pretty allow_nonref latin1 self_encode escape_slash
                          allow_blessed convert_blessed indent indent_length allow_bignum
                          as_nonblessed
                        /;
my %decode_allow_method
     = map {($_ => 1)} qw/utf8 allow_nonref loose allow_singlequote allow_bignum
                          allow_barekey max_size relaxed/;


my $JSON; # cache

sub encode_json ($) { # encode
    ($JSON ||= __PACKAGE__->new->utf8)->encode(@_);
}


sub decode_json { # decode
    ($JSON ||= __PACKAGE__->new->utf8)->decode(@_);
}

# Obsoleted

sub to_json($) {
   Carp::croak ("JSON::PP::to_json has been renamed to encode_json.");
}


sub from_json($) {
   Carp::croak ("JSON::PP::from_json has been renamed to decode_json.");
}


# Methods

sub new {
    my $class = shift;
    my $self  = {
        max_depth   => 512,
        max_size    => 0,
        indent      => 0,
        FLAGS       => 0,
        fallback      => sub { encode_error('Invalid value. JSON can only reference.') },
        indent_length => 3,
    };

    bless $self, $class;
}


sub encode {
    return $_[0]->PP_encode_json($_[1]);
}


sub decode {
    return $_[0]->PP_decode_json($_[1], 0x00000000);
}


sub decode_prefix {
    return $_[0]->PP_decode_json($_[1], 0x00000001);
}


# accessor


# pretty printing

sub pretty {
    my ($self, $v) = @_;
    my $enable = defined $v ? $v : 1;

    if ($enable) { # indent_length(3) for JSON::XS compatibility
        $self->indent(1)->indent_length(3)->space_before(1)->space_after(1);
    }
    else {
        $self->indent(0)->space_before(0)->space_after(0);
    }

    $self;
}

# etc

sub max_depth {
    my $max  = defined $_[1] ? $_[1] : 0x80000000;
    $_[0]->{max_depth} = $max;
    $_[0];
}


sub get_max_depth { $_[0]->{max_depth}; }


sub max_size {
    my $max  = defined $_[1] ? $_[1] : 0;
    $_[0]->{max_size} = $max;
    $_[0];
}


sub get_max_size { $_[0]->{max_size}; }


sub filter_json_object {
    $_[0]->{cb_object} = defined $_[1] ? $_[1] : 0;
    $_[0]->{F_HOOK} = ($_[0]->{cb_object} or $_[0]->{cb_sk_object}) ? 1 : 0;
    $_[0];
}

sub filter_json_single_key_object {
    if (@_ > 1) {
        $_[0]->{cb_sk_object}->{$_[1]} = $_[2];
    }
    $_[0]->{F_HOOK} = ($_[0]->{cb_object} or $_[0]->{cb_sk_object}) ? 1 : 0;
    $_[0];
}

sub indent_length {
    if (!defined $_[1] or $_[1] > 15 or $_[1] < 0) {
        Carp::carp "The acceptable range of indent_length() is 0 to 15.";
    }
    else {
        $_[0]->{indent_length} = $_[1];
    }
    $_[0];
}

sub get_indent_length {
    $_[0]->{indent_length};
}

sub sort_by {
    $_[0]->{sort_by} = defined $_[1] ? $_[1] : 1;
    $_[0];
}

sub allow_bigint {
    Carp::carp("allow_bigint() is obsoleted. use allow_bignum() insted.");
}

###############################

###
### Perl => JSON
###


{ # Convert

    my $max_depth;
    my $indent;
    my $ascii;
    my $latin1;
    my $utf8;
    my $space_before;
    my $space_after;
    my $canonical;
    my $allow_blessed;
    my $convert_blessed;

    my $indent_length;
    my $escape_slash;
    my $bignum;
    my $as_nonblessed;

    my $depth;
    my $indent_count;
    my $keysort;


    sub PP_encode_json {
        my $self = shift;
        my $obj  = shift;

        $indent_count = 0;
        $depth        = 0;

        my $idx = $self->{PROPS};

        ($ascii, $latin1, $utf8, $indent, $canonical, $space_before, $space_after, $allow_blessed,
            $convert_blessed, $escape_slash, $bignum, $as_nonblessed)
         = @{$idx}[P_ASCII .. P_SPACE_AFTER, P_ALLOW_BLESSED, P_CONVERT_BLESSED,
                    P_ESCAPE_SLASH, P_ALLOW_BIGNUM, P_AS_NONBLESSED];

        ($max_depth, $indent_length) = @{$self}{qw/max_depth indent_length/};

        $keysort = $canonical ? sub { $a cmp $b } : undef;

        if ($self->{sort_by}) {
            $keysort = ref($self->{sort_by}) eq 'CODE' ? $self->{sort_by}
                     : $self->{sort_by} =~ /\D+/       ? $self->{sort_by}
                     : sub { $a cmp $b };
        }

        encode_error("hash- or arrayref expected (not a simple scalar, use allow_nonref to allow this)")
             if(!ref $obj and !$idx->[ P_ALLOW_NONREF ]);

        my $str  = $self->object_to_json($obj);

        unless ($ascii or $latin1 or $utf8) {
            utf8::upgrade($str);
        }

        if ($idx->[ P_SHRINK ]) {
            utf8::downgrade($str, 1);
        }

        return $str;
    }


    sub object_to_json {
        my ($self, $obj) = @_;
        my $type = ref($obj);

        if($type eq 'HASH'){
            return $self->hash_to_json($obj);
        }
        elsif($type eq 'ARRAY'){
            return $self->array_to_json($obj);
        }
        elsif ($type) { # blessed object?
            if (blessed($obj)) {

                return $self->value_to_json($obj) if ( $obj->isa('JSON::PP::Boolean') );

                if ( $convert_blessed and $obj->can('TO_JSON') ) {
                    my $result = $obj->TO_JSON();
                    if ( defined $result and $obj eq $result ) {
                        encode_error( sprintf(
                            "%s::TO_JSON method returned same object as was passed instead of a new one",
                            ref $obj
                        ) );
                    }
                    return $self->object_to_json( $result );
                }

                return "$obj" if ( $bignum and _is_bignum($obj) );
                return $self->blessed_to_json($obj) if ($allow_blessed and $as_nonblessed); # will be removed.

                encode_error( sprintf("encountered object '%s', but neither allow_blessed "
                    . "nor convert_blessed settings are enabled", $obj)
                ) unless ($allow_blessed);

                return 'null';
            }
            else {
                return $self->value_to_json($obj);
            }
        }
        else{
            return $self->value_to_json($obj);
        }
    }


    sub hash_to_json {
        my ($self, $obj) = @_;
        my ($k,$v);
        my %res;

        encode_error("json text or perl structure exceeds maximum nesting level (max_depth set too low?)")
                                         if (++$depth > $max_depth);

        my ($pre, $post) = $indent ? $self->_up_indent() : ('', '');
        my $del = ($space_before ? ' ' : '') . ':' . ($space_after ? ' ' : '');

        if ( my $tie_class = tied %$obj ) {
            if ( $tie_class->can('TIEHASH') ) {
                $tie_class =~ s/=.+$//;
                tie %res, $tie_class;
            }
        }

        # In the old Perl verions, tied hashes in bool context didn't work.
        # So, we can't use such a way (%res ? a : b)
        my $has;

        for my $k (keys %$obj) {
            my $v = $obj->{$k};
            $res{$k} = $self->object_to_json($v) || $self->value_to_json($v);
            $has = 1 unless ( $has );
        }

        --$depth;
        $self->_down_indent() if ($indent);

        return '{' . ( $has ? $pre : '' )                                                   # indent
                   . ( $has ? join(",$pre", map { utf8::decode($_) if ($] < 5.008);         # key for Perl 5.6
                                                string_to_json($self, $_) . $del . $res{$_} # key : value
                                            } _sort( $self, \%res )
                             ) . $post                                                      # indent
                           : ''
                     )
             . '}';
    }


    sub array_to_json {
        my ($self, $obj) = @_;
        my @res;

        encode_error("json text or perl structure exceeds maximum nesting level (max_depth set too low?)")
                                         if (++$depth > $max_depth);

        my ($pre, $post) = $indent ? $self->_up_indent() : ('', '');

        if (my $tie_class = tied @$obj) {
            if ( $tie_class->can('TIEARRAY') ) {
                $tie_class =~ s/=.+$//;
                tie @res, $tie_class;
            }
        }

        for my $v (@$obj){
            push @res, $self->object_to_json($v) || $self->value_to_json($v);
        }

        --$depth;
        $self->_down_indent() if ($indent);

        return '[' . ( @res ? $pre : '' ) . ( @res ? join( ",$pre", @res ) . $post : '' ) . ']';
    }


    sub value_to_json {
        my ($self, $value) = @_;

        return 'null' if(!defined $value);

        my $b_obj = B::svref_2object(\$value);  # for round trip problem
        my $flags = $b_obj->FLAGS;

        return $value # as is 
            if ( (    $flags & B::SVf_IOK or $flags & B::SVp_IOK
                   or $flags & B::SVf_NOK or $flags & B::SVp_NOK
                 ) and !($flags & B::SVf_POK )
            ); # SvTYPE is IV or NV?

        my $type = ref($value);

        if(!$type){
            return string_to_json($self, $value);
        }
        elsif( blessed($value) and  $value->isa('JSON::PP::Boolean') ){
            return $$value == 1 ? 'true' : 'false';
        }
        elsif ($type) {
            if ((overload::StrVal($value) =~ /=(\w+)/)[0]) {
                return $self->value_to_json("$value");
            }

            if ($type eq 'SCALAR' and defined $$value) {
                return   $$value eq '1' ? 'true'
                       : $$value eq '0' ? 'false'
                       : $self->{PROPS}->[ P_ALLOW_UNKNOWN ] ? 'null'
                       : encode_error("cannot encode reference to scalar");
            }

             if ( $self->{PROPS}->[ P_ALLOW_UNKNOWN ] ) {
                 return 'null';
             }
             else {
                 if ( $type eq 'SCALAR' or $type eq 'REF' ) {
                    encode_error("cannot encode reference to scalar");
                 }
                 else {
                    encode_error("encountered $value, but JSON can only represent references to arrays or hashes");
                 }
             }

        }
        else {
            return $self->{fallback}->($value)
                 if ($self->{fallback} and ref($self->{fallback}) eq 'CODE');
            return 'null';
        }

    }


    my %esc = (
        "\n" => '\n',
        "\r" => '\r',
        "\t" => '\t',
        "\f" => '\f',
        "\b" => '\b',
        "\"" => '\"',
        "\\" => '\\\\',
        "\'" => '\\\'',
    );


    sub string_to_json {
        my ($self, $arg) = @_;

        $arg =~ s/([\x22\x5c\n\r\t\f\b])/$esc{$1}/g;
        $arg =~ s/\//\\\//g if ($escape_slash);
        $arg =~ s/([\x00-\x08\x0b\x0e-\x1f])/'\\u00' . unpack('H2', $1)/eg;

        if ($ascii) {
            $arg = JSON_PP_encode_ascii($arg);
        }

        if ($latin1) {
            $arg = JSON_PP_encode_latin1($arg);
        }

        if ($utf8) {
            utf8::encode($arg);
        }

        return '"' . $arg . '"';
    }


    sub blessed_to_json {
        my $b_obj = B::svref_2object($_[1]);
        if ($b_obj->isa('B::HV')) {
            return $_[0]->hash_to_json($_[1]);
        }
        elsif ($b_obj->isa('B::AV')) {
            return $_[0]->array_to_json($_[1]);
        }
        else {
            return 'null';
        }
    }


    sub encode_error {
        my $error  = shift;
        Carp::croak "$error";
    }


    sub _sort {
        my ($self, $res) = @_;
        defined $keysort ? (sort $keysort (keys %$res)) : keys %$res;
    }


    sub _up_indent {
        my $self  = shift;
        my $space = ' ' x $indent_length;

        my ($pre,$post) = ('','');

        $post = "\n" . $space x $indent_count;

        $indent_count++;

        $pre = "\n" . $space x $indent_count;

        return ($pre,$post);
    }


    sub _down_indent { $indent_count--; }


    sub PP_encode_box {
        {
            depth        => $depth,
            indent_count => $indent_count,
        };
    }

} # Convert


sub _encode_ascii {
    join('',
        map {
            $_ <= 127 ?
                chr($_) :
            $_ <= 65535 ?
                sprintf('\u%04x', $_) : sprintf('\u%x\u%x', _encode_surrogates($_));
        } unpack('U*', $_[0])
    );
}


sub _encode_latin1 {
    join('',
        map {
            $_ <= 255 ?
                chr($_) :
            $_ <= 65535 ?
                sprintf('\u%04x', $_) : sprintf('\u%x\u%x', _encode_surrogates($_));
        } unpack('U*', $_[0])
    );
}


sub _encode_surrogates { # from perlunicode
    my $uni = $_[0] - 0x10000;
    return ($uni / 0x400 + 0xD800, $uni % 0x400 + 0xDC00);
}


sub _is_bignum {
    $_[0]->isa('Math::BigInt') or $_[0]->isa('Math::BigFloat');
}



#
# JSON => Perl
#

my $max_intsize;

BEGIN {
    my $checkint = 1111;
    for my $d (5..30) {
        $checkint .= 1;
        my $int   = eval qq| $checkint |;
        if ($int =~ /[eE]/) {
            $max_intsize = $d - 1;
            last;
        }
    }
}

{ # PARSE 

    my %escapes = ( #  by Jeremy Muhlich <jmuhlich [at] bitflood.org>
        b    => "\x8",
        t    => "\x9",
        n    => "\xA",
        f    => "\xC",
        r    => "\xD",
        '\\' => '\\',
        '"'  => '"',
        '/'  => '/',
    );

    my $text; # json data
    my $at;   # offset
    my $ch;   # 1chracter
    my $len;  # text length (changed according to UTF8 or NON UTF8)
    # INTERNAL
    my $depth;          # nest counter
    my $encoding;       # json text encoding
    my $is_valid_utf8;  # temp variable
    my $utf8_len;       # utf8 byte length
    # FLAGS
    my $utf8;           # must be utf8
    my $max_depth;      # max nest nubmer of objects and arrays
    my $max_size;
    my $relaxed;
    my $cb_object;
    my $cb_sk_object;

    my $F_HOOK;

    my $allow_bigint;   # using Math::BigInt
    my $singlequote;    # loosely quoting
    my $loose;          # 
    my $allow_barekey;  # bareKey

    # $opt flag
    # 0x00000001 .... decode_prefix

    sub PP_decode_json {
        my ($self, $opt); # $opt is an effective flag during this decode_json.

        ($self, $text, $opt) = @_;

        ($at, $ch, $depth) = (0, '', 0);

        if (!defined $text or ref $text) {
            decode_error("malformed text data.");
        }

        my $idx = $self->{PROPS};

        ($utf8, $relaxed, $loose, $allow_bigint, $allow_barekey, $singlequote)
            = @{$idx}[P_UTF8, P_RELAXED, P_LOOSE .. P_ALLOW_SINGLEQUOTE];

        if ( $utf8 ) {
            utf8::downgrade( $text, 1 ) or Carp::croak("Wide character in subroutine entry");
        }
        else {
            utf8::upgrade( $text );
        }

        $len = length $text;

        ($max_depth, $max_size, $cb_object, $cb_sk_object, $F_HOOK)
             = @{$self}{qw/max_depth  max_size cb_object cb_sk_object F_HOOK/};

        if ($max_size > 1) {
            use bytes;
            my $bytes = length $text;
            decode_error(
                sprintf("attempted decode of JSON text of %s bytes size, but max_size is set to %s"
                    , $bytes, $max_size), 1
            ) if ($bytes > $max_size);
        }

        # Currently no effect
        # should use regexp
        my @octets = unpack('C4', $text);
        $encoding =   ( $octets[0] and  $octets[1]) ? 'UTF-8'
                    : (!$octets[0] and  $octets[1]) ? 'UTF-16BE'
                    : (!$octets[0] and !$octets[1]) ? 'UTF-32BE'
                    : ( $octets[2]                ) ? 'UTF-16LE'
                    : (!$octets[2]                ) ? 'UTF-32LE'
                    : 'unknown';

        my $result = value();

        if (!$idx->[ P_ALLOW_NONREF ] and !ref $result) {
                decode_error(
                'JSON text must be an object or array (but found number, string, true, false or null,'
                       . ' use allow_nonref to allow this)', 1);
        }

        if ($len >= $at) {
            my $consumed = $at - 1;
            white();
            if ($ch) {
                decode_error("garbage after JSON object") unless ($opt & 0x00000001);
                return ($result, $consumed);
            }
        }

        $result;
    }


    sub next_chr {
        return $ch = undef if($at >= $len);
        $ch = substr($text, $at++, 1);
    }


    sub value {
        white();
        return          if(!defined $ch);
        return object() if($ch eq '{');
        return array()  if($ch eq '[');
        return string() if($ch eq '"' or ($singlequote and $ch eq "'"));
        return number() if($ch =~ /[0-9]/ or $ch eq '-');
        return word();
    }

    sub string {
        my ($i, $s, $t, $u);
        my $utf16;
        my $is_utf8;

        ($is_valid_utf8, $utf8_len) = ('', 0);

        $s = ''; # basically UTF8 flag on

        if($ch eq '"' or ($singlequote and $ch eq "'")){
            my $boundChar = $ch if ($singlequote);

            OUTER: while( defined(next_chr()) ){

                if((!$singlequote and $ch eq '"') or ($singlequote and $ch eq $boundChar)){
                    next_chr();

                    if ($utf16) {
                        decode_error("missing low surrogate character in surrogate pair");
                    }

                    utf8::decode($s) if($is_utf8);

                    return $s;
                }
                elsif($ch eq '\\'){
                    next_chr();
                    if(exists $escapes{$ch}){
                        $s .= $escapes{$ch};
                    }
                    elsif($ch eq 'u'){ # UNICODE handling
                        my $u = '';

                        for(1..4){
                            $ch = next_chr();
                            last OUTER if($ch !~ /[0-9a-fA-F]/);
                            $u .= $ch;
                        }

                        # U+D800 - U+DBFF
                        if ($u =~ /^[dD][89abAB][0-9a-fA-F]{2}/) { # UTF-16 high surrogate?
                            $utf16 = $u;
                        }
                        # U+DC00 - U+DFFF
                        elsif ($u =~ /^[dD][c-fC-F][0-9a-fA-F]{2}/) { # UTF-16 low surrogate?
                            unless (defined $utf16) {
                                decode_error("missing high surrogate character in surrogate pair");
                            }
                            $is_utf8 = 1;
                            $s .= JSON_PP_decode_surrogates($utf16, $u) || next;
                            $utf16 = undef;
                        }
                        else {
                            if (defined $utf16) {
                                decode_error("surrogate pair expected");
                            }

                            if ( ( my $hex = hex( $u ) ) > 127 ) {
                                $is_utf8 = 1;
                                $s .= JSON_PP_decode_unicode($u) || next;
                            }
                            else {
                                $s .= chr $hex;
                            }
                        }

                    }
                    else{
                        unless ($loose) {
                            decode_error('illegal backslash escape sequence in string');
                        }
                        $s .= $ch;
                    }
                }
                else{

                    if ( ord $ch  > 127 ) {
                        if ( $utf8 ) {
                            unless( $ch = is_valid_utf8($ch) ) {
                                $at -= 1;
                                decode_error("malformed UTF-8 character in JSON string");
                            }
                            else {
                                $at += $utf8_len - 1;
                            }
                        }
                        else {
                            utf8::encode( $ch );
                        }

                        $is_utf8 = 1;
                    }

                    if (!$loose) {
                        if ($ch =~ /[\x00-\x1f\x22\x5c]/)  { # '/' ok
                            $at--;
                            decode_error('invalid character encountered while parsing JSON string');
                        }
                    }

                    $s .= $ch;
                }
            }
        }

        decode_error("unexpected end of string while parsing JSON string");
    }


    sub white {
        while( defined $ch  ){
            if($ch le ' '){
                next_chr();
            }
            elsif($ch eq '/'){
                next_chr();
                if(defined $ch and $ch eq '/'){
                    1 while(defined(next_chr()) and $ch ne "\n" and $ch ne "\r");
                }
                elsif(defined $ch and $ch eq '*'){
                    next_chr();
                    while(1){
                        if(defined $ch){
                            if($ch eq '*'){
                                if(defined(next_chr()) and $ch eq '/'){
                                    next_chr();
                                    last;
                                }
                            }
                            else{
                                next_chr();
                            }
                        }
                        else{
                            decode_error("Unterminated comment");
                        }
                    }
                    next;
                }
                else{
                    $at--;
                    decode_error("malformed JSON string, neither array, object, number, string or atom");
                }
            }
            else{
                if ($relaxed and $ch eq '#') { # correctly?
                    pos($text) = $at;
                    $text =~ /\G([^\n]*(?:\r\n|\r|\n))/g;
                    $at = pos($text);
                    next_chr;
                    next;
                }

                last;
            }
        }
    }


    sub array {
        my $a  = [];

        decode_error('json text or perl structure exceeds maximum nesting level (max_depth set too low?)')
                                                    if (++$depth > $max_depth);

        next_chr();
        white();

        if(defined $ch and $ch eq ']'){
            --$depth;
            next_chr();
            return $a;
        }
        else {
            while(defined($ch)){
                push @$a, value();

                white();

                if (!defined $ch) {
                    last;
                }

                if($ch eq ']'){
                    --$depth;
                    next_chr();
                    return $a;
                }

                if($ch ne ','){
                    last;
                }

                next_chr();
                white();

                if ($relaxed and $ch eq ']') {
                    --$depth;
                    next_chr();
                    return $a;
                }

            }
        }

        decode_error(", or ] expected while parsing array");
    }


    sub object {
        my $o = {};
        my $k;

        decode_error('json text or perl structure exceeds maximum nesting level (max_depth set too low?)')
                                                if (++$depth > $max_depth);
        next_chr();
        white();

        if(defined $ch and $ch eq '}'){
            --$depth;
            next_chr();
            if ($F_HOOK) {
                return _json_object_hook($o);
            }
            return $o;
        }
        else {
            while (defined $ch) {
                $k = ($allow_barekey and $ch ne '"' and $ch ne "'") ? bareKey() : string();
                white();

                if(!defined $ch or $ch ne ':'){
                    $at--;
                    decode_error("':' expected");
                }

                next_chr();
                $o->{$k} = value();
                white();

                last if (!defined $ch);

                if($ch eq '}'){
                    --$depth;
                    next_chr();
                    if ($F_HOOK) {
                        return _json_object_hook($o);
                    }
                    return $o;
                }

                if($ch ne ','){
                    last;
                }

                next_chr();
                white();

                if ($relaxed and $ch eq '}') {
                    --$depth;
                    next_chr();
                    if ($F_HOOK) {
                        return _json_object_hook($o);
                    }
                    return $o;
                }

            }

        }

        $at--;
        decode_error(", or } expected while parsing object/hash");
    }


    sub bareKey { # doesn't strictly follow Standard ECMA-262 3rd Edition
        my $key;
        while($ch =~ /[^\x00-\x23\x25-\x2F\x3A-\x40\x5B-\x5E\x60\x7B-\x7F]/){
            $key .= $ch;
            next_chr();
        }
        return $key;
    }


    sub word {
        my $word =  substr($text,$at-1,4);

        if($word eq 'true'){
            $at += 3;
            next_chr;
            return $JSON::PP::true;
        }
        elsif($word eq 'null'){
            $at += 3;
            next_chr;
            return undef;
        }
        elsif($word eq 'fals'){
            $at += 3;
            if(substr($text,$at,1) eq 'e'){
                $at++;
                next_chr;
                return $JSON::PP::false;
            }
        }

        $at--; # for decode_error report

        decode_error("'null' expected")  if ($word =~ /^n/);
        decode_error("'true' expected")  if ($word =~ /^t/);
        decode_error("'false' expected") if ($word =~ /^f/);
        decode_error("malformed JSON string, neither array, object, number, string or atom");
    }


    sub number {
        my $n    = '';
        my $v;

        # According to RFC4627, hex or oct digts are invalid.
        if($ch eq '0'){
            my $peek = substr($text,$at,1);
            my $hex  = $peek =~ /[xX]/; # 0 or 1

            if($hex){
                decode_error("malformed number (leading zero must not be followed by another digit)");
                ($n) = ( substr($text, $at+1) =~ /^([0-9a-fA-F]+)/);
            }
            else{ # oct
                ($n) = ( substr($text, $at) =~ /^([0-7]+)/);
                if (defined $n and length $n > 1) {
                    decode_error("malformed number (leading zero must not be followed by another digit)");
                }
            }

            if(defined $n and length($n)){
                if (!$hex and length($n) == 1) {
                   decode_error("malformed number (leading zero must not be followed by another digit)");
                }
                $at += length($n) + $hex;
                next_chr;
                return $hex ? hex($n) : oct($n);
            }
        }

        if($ch eq '-'){
            $n = '-';
            next_chr;
            if (!defined $ch or $ch !~ /\d/) {
                decode_error("malformed number (no digits after initial minus)");
            }
        }

        while(defined $ch and $ch =~ /\d/){
            $n .= $ch;
            next_chr;
        }

        if(defined $ch and $ch eq '.'){
            $n .= '.';

            next_chr;
            if (!defined $ch or $ch !~ /\d/) {
                decode_error("malformed number (no digits after decimal point)");
            }
            else {
                $n .= $ch;
            }

            while(defined(next_chr) and $ch =~ /\d/){
                $n .= $ch;
            }
        }

        if(defined $ch and ($ch eq 'e' or $ch eq 'E')){
            $n .= $ch;
            next_chr;

            if(defined($ch) and ($ch eq '+' or $ch eq '-')){
                $n .= $ch;
                next_chr;
                if (!defined $ch or $ch =~ /\D/) {
                    decode_error("malformed number (no digits after exp sign)");
                }
                $n .= $ch;
            }
            elsif(defined($ch) and $ch =~ /\d/){
                $n .= $ch;
            }
            else {
                decode_error("malformed number (no digits after exp sign)");
            }

            while(defined(next_chr) and $ch =~ /\d/){
                $n .= $ch;
            }

        }

        $v .= $n;

        if ($v !~ /[.eE]/ and length $v > $max_intsize) {
            if ($allow_bigint) { # from Adam Sussman
                require Math::BigInt;
                return Math::BigInt->new($v);
            }
            else {
                return "$v";
            }
        }
        elsif ($allow_bigint) {
            require Math::BigFloat;
            return Math::BigFloat->new($v);
        }

        return 0+$v;
    }


    sub is_valid_utf8 {

        $utf8_len = $_[0] =~ /[\x00-\x7F]/  ? 1
                  : $_[0] =~ /[\xC2-\xDF]/  ? 2
                  : $_[0] =~ /[\xE0-\xEF]/  ? 3
                  : $_[0] =~ /[\xF0-\xF4]/  ? 4
                  : 0
                  ;

        return unless $utf8_len;

        my $is_valid_utf8 = substr($text, $at - 1, $utf8_len);

        return ( $is_valid_utf8 =~ /^(?:
             [\x00-\x7F]
            |[\xC2-\xDF][\x80-\xBF]
            |[\xE0][\xA0-\xBF][\x80-\xBF]
            |[\xE1-\xEC][\x80-\xBF][\x80-\xBF]
            |[\xED][\x80-\x9F][\x80-\xBF]
            |[\xEE-\xEF][\x80-\xBF][\x80-\xBF]
            |[\xF0][\x90-\xBF][\x80-\xBF][\x80-\xBF]
            |[\xF1-\xF3][\x80-\xBF][\x80-\xBF][\x80-\xBF]
            |[\xF4][\x80-\x8F][\x80-\xBF][\x80-\xBF]
        )$/x )  ? $is_valid_utf8 : '';
    }


    sub decode_error {
        my $error  = shift;
        my $no_rep = shift;
        my $str    = defined $text ? substr($text, $at) : '';
        my $mess   = '';
        my $type   = $] >= 5.008           ? 'U*'
                   : $] <  5.006           ? 'C*'
                   : utf8::is_utf8( $str ) ? 'U*' # 5.6
                   : 'C*'
                   ;

        for my $c ( unpack( $type, $str ) ) { # emulate pv_uni_display() ?
            $mess .=  $c == 0x07 ? '\a'
                    : $c == 0x09 ? '\t'
                    : $c == 0x0a ? '\n'
                    : $c == 0x0d ? '\r'
                    : $c == 0x0c ? '\f'
                    : $c <  0x20 ? sprintf('\x{%x}', $c)
                    : $c <  0x80 ? chr($c)
                    : sprintf('\x{%x}', $c)
                    ;
            if ( length $mess >= 20 ) {
                $mess .= '...';
                last;
            }
        }

        unless ( length $mess ) {
            $mess = '(end of string)';
        }

        Carp::croak (
            $no_rep ? "$error" : "$error, at character offset $at (before \"$mess\")"
        );

#        Carp::croak (
#            $no_rep ? "$error" : "$error, at character offset $at [\"$mess\"]"
#        );
    }


    sub _json_object_hook {
        my $o    = $_[0];
        my @ks = keys %{$o};

        if ( $cb_sk_object and @ks == 1 and exists $cb_sk_object->{ $ks[0] } and ref $cb_sk_object->{ $ks[0] } ) {
            my @val = $cb_sk_object->{ $ks[0] }->( $o->{$ks[0]} );
            if (@val == 1) {
                return $val[0];
            }
        }

        my @val = $cb_object->($o) if ($cb_object);
        if (@val == 0 or @val > 1) {
            return $o;
        }
        else {
            return $val[0];
        }
    }


    sub PP_decode_box {
        {
            text    => $text,
            at      => $at,
            ch      => $ch,
            len     => $len,
            depth   => $depth,
            encoding      => $encoding,
            is_valid_utf8 => $is_valid_utf8,
        };
    }

} # PARSE


sub _decode_surrogates { # from perlunicode
    my $uni = 0x10000 + (hex($_[0]) - 0xD800) * 0x400 + (hex($_[1]) - 0xDC00);
    my $un  = pack('U*', $uni);
    utf8::encode( $un );
    return $un;
}


sub _decode_unicode {
    my $un = pack('U', hex shift);
    utf8::encode( $un );
    return $un;
}





###############################
# Utilities
#

BEGIN {
    eval 'require Scalar::Util';
    unless($@){
        *JSON::PP::blessed = \&Scalar::Util::blessed;
    }
    else{ # This code is from Sclar::Util.
        # warn $@;
        eval 'sub UNIVERSAL::a_sub_not_likely_to_be_here { ref($_[0]) }';
        *JSON::PP::blessed = sub {
            local($@, $SIG{__DIE__}, $SIG{__WARN__});
            ref($_[0]) ? eval { $_[0]->a_sub_not_likely_to_be_here } : undef;
        };
    }
}


# shamely copied and modified from JSON::XS code.

$JSON::PP::true  = do { bless \(my $dummy = 1), "JSON::PP::Boolean" };
$JSON::PP::false = do { bless \(my $dummy = 0), "JSON::PP::Boolean" };

sub is_bool { defined $_[0] and UNIVERSAL::isa($_[0], "JSON::PP::Boolean"); }

sub true  { $JSON::PP::true  }
sub false { $JSON::PP::false }
sub null  { undef; }

###############################

package JSON::PP::Boolean;


use overload (
   "0+"     => sub { ${$_[0]} },
   "++"     => sub { $_[0] = ${$_[0]} + 1 },
   "--"     => sub { $_[0] = ${$_[0]} - 1 },
   fallback => 1,
);


###############################

package JSON::PP::IncrParser;

use strict;

use constant INCR_M_WS   => 0; # initial whitespace skipping
use constant INCR_M_STR  => 1; # inside string
use constant INCR_M_BS   => 2; # inside backslash
use constant INCR_M_JSON => 3; # outside anything, count nesting

$JSON::PP::IncrParser::VERSION = '1.01';

my $unpack_format = $] < 5.006 ? 'C*' : 'U*';

sub new {
    my ( $class ) = @_;

    bless {
        incr_nest    => 0,
        incr_text    => undef,
        incr_parsing => 0,
        incr_p       => 0,
    }, $class;
}


sub incr_parse {
    my ( $self, $coder, $text ) = @_;

    $self->{incr_text} = '' unless ( defined $self->{incr_text} );

    if ( defined $text ) {
        if ( utf8::is_utf8( $text ) and !utf8::is_utf8( $self->{incr_text} ) ) {
            utf8::upgrade( $self->{incr_text} ) ;
            utf8::decode( $self->{incr_text} ) ;
        }
        $self->{incr_text} .= $text;
    }


    my $max_size = $coder->get_max_size;

    if ( defined wantarray ) {

        $self->{incr_mode} = INCR_M_WS;

        if ( wantarray ) {
            my @ret;

            $self->{incr_parsing} = 1;

            do {
                push @ret, $self->_incr_parse( $coder, $self->{incr_text} );

                unless ( !$self->{incr_nest} and $self->{incr_mode} == INCR_M_JSON ) {
                    $self->{incr_mode} = INCR_M_WS;
                }

            } until ( !$self->{incr_text} );

            $self->{incr_parsing} = 0;

            return @ret;
        }
        else { # in scalar context
            $self->{incr_parsing} = 1;
            my $obj = $self->_incr_parse( $coder, $self->{incr_text} );
            $self->{incr_parsing} = 0 if defined $obj; # pointed by Martin J. Evans
            return $obj ? $obj : undef; # $obj is an empty string, parsing was completed.
        }

    }

}


sub _incr_parse {
    my ( $self, $coder, $text, $skip ) = @_;
    my $p = $self->{incr_p};
    my $restore = $p;

    my @obj;
    my $len = length $text;

    if ( $self->{incr_mode} == INCR_M_WS ) {
        while ( $len > $p ) {
            my $s = substr( $text, $p, 1 );
            $p++ and next if ( 0x20 >= unpack($unpack_format, $s) );
            $self->{incr_mode} = INCR_M_JSON;
            last;
       }
    }

    while ( $len > $p ) {
        my $s = substr( $text, $p++, 1 );

        if ( $s eq '"' ) {
            if ( $self->{incr_mode} != INCR_M_STR  ) {
                $self->{incr_mode} = INCR_M_STR;
            }
            else {
                $self->{incr_mode} = INCR_M_JSON;
                unless ( $self->{incr_nest} ) {
                    last;
                }
            }
        }

        if ( $self->{incr_mode} == INCR_M_JSON ) {

            if ( $s eq '[' or $s eq '{' ) {
                if ( ++$self->{incr_nest} > $coder->get_max_depth ) {
                    Carp::croak('json text or perl structure exceeds maximum nesting level (max_depth set too low?)');
                }
            }
            elsif ( $s eq ']' or $s eq '}' ) {
                last if ( --$self->{incr_nest} <= 0 );
            }
        }

    }

    $self->{incr_p} = $p;

    return if ( $self->{incr_mode} == INCR_M_JSON and $self->{incr_nest} > 0 );

    return '' unless ( length substr( $self->{incr_text}, 0, $p ) );

    local $Carp::CarpLevel = 2;

    $self->{incr_p} = $restore;
    $self->{incr_c} = $p;

    my ( $obj, $tail ) = $coder->decode_prefix( substr( $self->{incr_text}, 0, $p ) );

    $self->{incr_text} = substr( $self->{incr_text}, $p );
    $self->{incr_p} = 0;

    return $obj or '';
}


sub incr_text {
    if ( $_[0]->{incr_parsing} ) {
        Carp::croak("incr_text can not be called when the incremental parser already started parsing");
    }
    $_[0]->{incr_text};
}


sub incr_skip {
    my $self  = shift;
    $self->{incr_text} = substr( $self->{incr_text}, $self->{incr_c} );
    $self->{incr_p} = 0;
}


sub incr_reset {
    my $self = shift;
    $self->{incr_text}    = undef;
    $self->{incr_p}       = 0;
    $self->{incr_mode}    = 0;
    $self->{incr_nest}    = 0;
    $self->{incr_parsing} = 0;
}

###############################


1;
__END__
=pod

=head1 NAME

JSON::PP - JSON::XS compatible pure-Perl module.

=head1 SYNOPSIS

 use JSON::PP;

 # exported functions, they croak on error
 # and expect/generate UTF-8

 $utf8_encoded_json_text = encode_json $perl_hash_or_arrayref;
 $perl_hash_or_arrayref  = decode_json $utf8_encoded_json_text;

 # OO-interface

 $coder = JSON::PP->new->ascii->pretty->allow_nonref;
 $pretty_printed_unencoded = $coder->encode ($perl_scalar);
 $perl_scalar = $coder->decode ($unicode_json_text);

 # Note that JSON version 2.0 and above will automatically use
 # JSON::XS or JSON::PP, so you should be able to just:
 
 use JSON;

=head1 DESCRIPTION

This module is L<JSON::XS> compatible pure Perl module.
(Perl 5.8 or later is recommended)

JSON::XS is the fastest and most proper JSON module on CPAN.
It is written by Marc Lehmann in C, so must be compiled and
installed in the used environment.

JSON::PP is a pure-Perl module and has compatibility to JSON::XS.


=head2 FEATURES

=over

=item * correct unicode handling

This module knows how to handle Unicode (depending on Perl version).

See to L<JSON::XS/A FEW NOTES ON UNICODE AND PERL> and L<UNICODE HANDLING ON PERLS>.


=item * round-trip integrity

When you serialise a perl data structure using only datatypes supported by JSON,
the deserialised data structure is identical on the Perl level.
(e.g. the string "2.0" doesn't suddenly become "2" just because it looks like a number).

=item * strict checking of JSON correctness

There is no guessing, no generating of illegal JSON texts by default,
and only JSON is accepted as input by default (the latter is a security feature).
But when some options are set, loose chcking features are available.

=back

=head1 FUNCTIONS

Basically, check to L<JSON> or L<JSON::XS>.

=head2 encode_json

    $json_text = encode_json $perl_scalar

=head2 decode_json

    $perl_scalar = decode_json $json_text

=head2 JSON::PP::true

Returns JSON true value which is blessed object.
It C<isa> JSON::PP::Boolean object.

=head2 JSON::PP::false

Returns JSON false value which is blessed object.
It C<isa> JSON::PP::Boolean object.

=head2 JSON::PP::null

Returns C<undef>.

=head1 METHODS

Basically, check to L<JSON> or L<JSON::XS>.

=head2 new

    $json = new JSON::PP

Rturns a new JSON::PP object that can be used to de/encode JSON
strings.

=head2 ascii

    $json = $json->ascii([$enable])
    
    $enabled = $json->get_ascii

If $enable is true (or missing), then the encode method will not generate characters outside
the code range 0..127. Any Unicode characters outside that range will be escaped using either
a single \uXXXX or a double \uHHHH\uLLLLL escape sequence, as per RFC4627.
(See to L<JSON::XS/OBJECT-ORIENTED INTERFACE>).

In Perl 5.005, there is no character having high value (more than 255).
See to L<UNICODE HANDLING ON PERLS>.

If $enable is false, then the encode method will not escape Unicode characters unless
required by the JSON syntax or other flags. This results in a faster and more compact format.

  JSON::PP->new->ascii(1)->encode([chr 0x10401])
  => ["\ud801\udc01"]

=head2 latin1

    $json = $json->latin1([$enable])
    
    $enabled = $json->get_latin1

If $enable is true (or missing), then the encode method will encode the resulting JSON
text as latin1 (or iso-8859-1), escaping any characters outside the code range 0..255.

If $enable is false, then the encode method will not escape Unicode characters
unless required by the JSON syntax or other flags.

  JSON::XS->new->latin1->encode (["\x{89}\x{abc}"]
  => ["\x{89}\\u0abc"]    # (perl syntax, U+abc escaped, U+89 not)

See to L<UNICODE HANDLING ON PERLS>.

=head2 utf8

    $json = $json->utf8([$enable])
    
    $enabled = $json->get_utf8

If $enable is true (or missing), then the encode method will encode the JSON result
into UTF-8, as required by many protocols, while the decode method expects to be handled
an UTF-8-encoded string. Please note that UTF-8-encoded strings do not contain any
characters outside the range 0..255, they are thus useful for bytewise/binary I/O.

(In Perl 5.005, any character outside the range 0..255 does not exist.
See to L<UNICODE HANDLING ON PERLS>.)

In future versions, enabling this option might enable autodetection of the UTF-16 and UTF-32
encoding families, as described in RFC4627.

If $enable is false, then the encode method will return the JSON string as a (non-encoded)
Unicode string, while decode expects thus a Unicode string. Any decoding or encoding
(e.g. to UTF-8 or UTF-16) needs to be done yourself, e.g. using the Encode module.

Example, output UTF-16BE-encoded JSON:

  use Encode;
  $jsontext = encode "UTF-16BE", JSON::XS->new->encode ($object);

Example, decode UTF-32LE-encoded JSON:

  use Encode;
  $object = JSON::XS->new->decode (decode "UTF-32LE", $jsontext);


=head2 pretty

    $json = $json->pretty([$enable])

This enables (or disables) all of the C<indent>, C<space_before> and
C<space_after> flags in one call to generate the most readable
(or most compact) form possible.

=head2 indent

    $json = $json->indent([$enable])
    
    $enabled = $json->get_indent

The default indent space lenght is three.
You can use C<indent_length> to change the length.

=head2 space_before

    $json = $json->space_before([$enable])
    
    $enabled = $json->get_space_before

=head2 space_after

    $json = $json->space_after([$enable])
    
    $enabled = $json->get_space_after

=head2 relaxed

    $json = $json->relaxed([$enable])
    
    $enabled = $json->get_relaxed

=head2 canonical

    $json = $json->canonical([$enable])
    
    $enabled = $json->get_canonical

If you want your own sorting routine, you can give a code referece
or a subroutine name to C<sort_by>. See to C<JSON::PP OWN METHODS>.

=head2 allow_nonref

    $json = $json->allow_nonref([$enable])
    
    $enabled = $json->get_allow_nonref

=head2 allow_unknown

    $json = $json->allow_unknown ([$enable])
    
    $enabled = $json->get_allow_unknown

=head2 allow_blessed

    $json = $json->allow_blessed([$enable])
    
    $enabled = $json->get_allow_blessed

=head2 convert_blessed

    $json = $json->convert_blessed([$enable])
    
    $enabled = $json->get_convert_blessed

=head2 filter_json_object

    $json = $json->filter_json_object([$coderef])

=head2 filter_json_single_key_object

    $json = $json->filter_json_single_key_object($key [=> $coderef])

=head2 shrink

    $json = $json->shrink([$enable])
    
    $enabled = $json->get_shrink

In JSON::XS, this flag resizes strings generated by either
C<encode> or C<decode> to their minimum size possible.
It will also try to downgrade any strings to octet-form if possible.

In JSON::PP, it is noop about resizing strings but tries
C<utf8::downgrade> to the returned string by C<encode>.
See to L<utf8>.

See to L<JSON::XS/OBJECT-ORIENTED INTERFACE>

=head2 max_depth

    $json = $json->max_depth([$maximum_nesting_depth])
    
    $max_depth = $json->get_max_depth

Sets the maximum nesting level (default C<512>) accepted while encoding
or decoding. If a higher nesting level is detected in JSON text or a Perl
data structure, then the encoder and decoder will stop and croak at that
point.

Nesting level is defined by number of hash- or arrayrefs that the encoder
needs to traverse to reach a given point or the number of C<{> or C<[>
characters without their matching closing parenthesis crossed to reach a
given character in a string.

If no argument is given, the highest possible setting will be used, which
is rarely useful.

See L<JSON::XS/SSECURITY CONSIDERATIONS> for more info on why this is useful.

When a large value (100 or more) was set and it de/encodes a deep nested object/text,
it may raise a warning 'Deep recursion on subroutin' at the perl runtime phase.

=head2 max_size

    $json = $json->max_size([$maximum_string_size])
    
    $max_size = $json->get_max_size

Set the maximum length a JSON text may have (in bytes) where decoding is
being attempted. The default is C<0>, meaning no limit. When C<decode>
is called on a string that is longer then this many bytes, it will not
attempt to decode the string but throw an exception. This setting has no
effect on C<encode> (yet).

If no argument is given, the limit check will be deactivated (same as when
C<0> is specified).

See L<JSON::XS/SSECURITY CONSIDERATIONS> for more info on why this is useful.

=head2 encode

    $json_text = $json->encode($perl_scalar)

=head2 decode

    $perl_scalar = $json->decode($json_text)

=head2 decode_prefix

    ($perl_scalar, $characters) = $json->decode_prefix($json_text)



=head1 INCREMENTAL PARSING

In JSON::XS 2.2, incremental parsing feature of JSON
texts was experimentally implemented.
Please check to L<JSON::XS/INCREMENTAL PARSING>.

=over 4

=item [void, scalar or list context] = $json->incr_parse ([$string])

This is the central parsing function. It can both append new text and
extract objects from the stream accumulated so far (both of these
functions are optional).

If C<$string> is given, then this string is appended to the already
existing JSON fragment stored in the C<$json> object.

After that, if the function is called in void context, it will simply
return without doing anything further. This can be used to add more text
in as many chunks as you want.

If the method is called in scalar context, then it will try to extract
exactly I<one> JSON object. If that is successful, it will return this
object, otherwise it will return C<undef>. If there is a parse error,
this method will croak just as C<decode> would do (one can then use
C<incr_skip> to skip the errornous part). This is the most common way of
using the method.

And finally, in list context, it will try to extract as many objects
from the stream as it can find and return them, or the empty list
otherwise. For this to work, there must be no separators between the JSON
objects or arrays, instead they must be concatenated back-to-back. If
an error occurs, an exception will be raised as in the scalar context
case. Note that in this case, any previously-parsed JSON texts will be
lost.

=item $lvalue_string = $json->incr_text

This method returns the currently stored JSON fragment as an lvalue, that
is, you can manipulate it. This I<only> works when a preceding call to
C<incr_parse> in I<scalar context> successfully returned an object. Under
all other circumstances you must not call this function (I mean it.
although in simple tests it might actually work, it I<will> fail under
real world conditions). As a special exception, you can also call this
method before having parsed anything.

This function is useful in two cases: a) finding the trailing text after a
JSON object or b) parsing multiple JSON objects separated by non-JSON text
(such as commas).

In Perl 5.005, C<lvalue> attribute is not available.
You must write codes like the below:

    $string = $json->incr_text;
    $string =~ s/\s*,\s*//;
    $json->incr_text( $string );

=item $json->incr_skip

This will reset the state of the incremental parser and will remove the
parsed text from the input buffer. This is useful after C<incr_parse>
died, in which case the input buffer and incremental parser state is left
unchanged, to skip the text parsed so far and to reset the parse state.

=back



=head1 JSON::PP OWN METHODS

=head2 allow_singlequote

    $json = $json->allow_singlequote([$enable])

If C<$enable> is true (or missing), then C<decode> will accept
JSON strings quoted by single quotations that are invalid JSON
format.

    $json->allow_singlequote->decode({"foo":'bar'});
    $json->allow_singlequote->decode({'foo':"bar"});
    $json->allow_singlequote->decode({'foo':'bar'});

As same as the C<relaxed> option, this option may be used to parse
application-specific files written by humans.


=head2 allow_barekey

    $json = $json->allow_barekey([$enable])

If C<$enable> is true (or missing), then C<decode> will accept
bare keys of JSON object that are invalid JSON format.

As same as the C<relaxed> option, this option may be used to parse
application-specific files written by humans.

    $json->allow_barekey->decode('{foo:"bar"}');

=head2 allow_bignum

    $json = $json->allow_bignum([$enable])

If C<$enable> is true (or missing), then C<decode> will convert
the big integer Perl cannot handle as integer into a L<Math::BigInt>
object and convert a floating number (any) into a L<Math::BigFloat>.

On the contary, C<encode> converts C<Math::BigInt> objects and C<Math::BigFloat>
objects into JSON numbers with C<allow_blessed> enable.

   $json->allow_nonref->allow_blessed->allow_bignum;
   $bigfloat = $json->decode('2.000000000000000000000000001');
   print $json->encode($bigfloat);
   # => 2.000000000000000000000000001

See to L<JSON::XS/MAPPING> aboout the normal conversion of JSON number.

=head2 loose

    $json = $json->loose([$enable])

The unescaped [\x00-\x1f\x22\x2f\x5c] strings are invalid in JSON strings
and the module doesn't allow to C<decode> to these (except for \x2f).
If C<$enable> is true (or missing), then C<decode>  will accept these
unescaped strings.

    $json->loose->decode(qq|["abc
                                   def"]|);

See L<JSON::XS/SSECURITY CONSIDERATIONS>.

=head2 escape_slash

    $json = $json->escape_slash([$enable])

According to JSON Grammar, I<slash> (U+002F) is escaped. But default
JSON::PP (as same as JSON::XS) encodes strings without escaping slash.

If C<$enable> is true (or missing), then C<encode> will escape slashes.

=head2 (OBSOLETED)as_nonblessed

    $json = $json->as_nonblessed

(OBSOLETED) If C<$enable> is true (or missing), then C<encode> will convert
a blessed hash reference or a blessed array reference (contains
other blessed references) into JSON members and arrays.

This feature is effective only when C<allow_blessed> is enable.

=head2 indent_length

    $json = $json->indent_length($length)

JSON::XS indent space length is 3 and cannot be changed.
JSON::PP set the indent space length with the given $length.
The default is 3. The acceptable range is 0 to 15.

=head2 sort_by

    $json = $json->sort_by($function_name)
    $json = $json->sort_by($subroutine_ref)

If $function_name or $subroutine_ref are set, its sort routine are used
in encoding JSON objects.

   $js = $pc->sort_by(sub { $JSON::PP::a cmp $JSON::PP::b })->encode($obj);
   # is($js, q|{"a":1,"b":2,"c":3,"d":4,"e":5,"f":6,"g":7,"h":8,"i":9}|);

   $js = $pc->sort_by('own_sort')->encode($obj);
   # is($js, q|{"a":1,"b":2,"c":3,"d":4,"e":5,"f":6,"g":7,"h":8,"i":9}|);

   sub JSON::PP::own_sort { $JSON::PP::a cmp $JSON::PP::b }

As the sorting routine runs in the JSON::PP scope, the given
subroutine name and the special variables C<$a>, C<$b> will begin
'JSON::PP::'.

If $integer is set, then the effect is same as C<canonical> on.

=head1 INTERNAL

For developers.

=over

=item PP_encode_box

Returns

        {
            depth        => $depth,
            indent_count => $indent_count,
        }


=item PP_decode_box

Returns

        {
            text    => $text,
            at      => $at,
            ch      => $ch,
            len     => $len,
            depth   => $depth,
            encoding      => $encoding,
            is_valid_utf8 => $is_valid_utf8,
        };

=back

=head1 MAPPING

See to L<JSON::XS/MAPPING>.


=head1 UNICODE HANDLING ON PERLS

If you do not know about Unicode on Perl well,
please check L<JSON::XS/A FEW NOTES ON UNICODE AND PERL>.

=head2 Perl 5.8 and later

Perl can handle Unicode and the JSON::PP de/encode methods also work properly.

    $json->allow_nonref->encode(chr hex 3042);
    $json->allow_nonref->encode(chr hex 12345);

Reuturns C<"\u3042"> and C<"\ud808\udf45"> respectively.

    $json->allow_nonref->decode('"\u3042"');
    $json->allow_nonref->decode('"\ud808\udf45"');

Returns UTF-8 encoded strings with UTF8 flag, regarded as C<U+3042> and C<U+12345>.

Note that the versions from Perl 5.8.0 to 5.8.2, Perl built-in C<join> was broken,
so JSON::PP wraps the C<join> with a subroutine. Thus JSON::PP works slow in the versions.


=head2 Perl 5.6

Perl can handle Unicode and the JSON::PP de/encode methods also work.

=head2 Perl 5.005

Perl 5.005 is a byte sementics world -- all strings are sequences of bytes.
That means the unicode handling is not available.

In encoding,

    $json->allow_nonref->encode(chr hex 3042);  # hex 3042 is 12354.
    $json->allow_nonref->encode(chr hex 12345); # hex 12345 is 74565.

Returns C<B> and C<E>, as C<chr> takes a value more than 255, it treats
as C<$value % 256>, so the above codes are equivalent to :

    $json->allow_nonref->encode(chr 66);
    $json->allow_nonref->encode(chr 69);

In decoding,

    $json->decode('"\u00e3\u0081\u0082"');

The returned is a byte sequence C<0xE3 0x81 0x82> for UTF-8 encoded
japanese character (C<HIRAGANA LETTER A>).
And if it is represented in Unicode code point, C<U+3042>.

Next, 

    $json->decode('"\u3042"');

We ordinary expect the returned value is a Unicode character C<U+3042>.
But here is 5.005 world. This is C<0xE3 0x81 0x82>.

    $json->decode('"\ud808\udf45"');

This is not a character C<U+12345> but bytes - C<0xf0 0x92 0x8d 0x85>.


=head1 TODO

=over

=item speed

=item memory saving

=back


=head1 SEE ALSO

Most of the document are copied and modified from JSON::XS doc.

L<JSON::XS>

RFC4627 (L<http://www.ietf.org/rfc/rfc4627.txt>)

=head1 AUTHOR

Makamaka Hannyaharamitu, E<lt>makamaka[at]cpan.orgE<gt>


=head1 COPYRIGHT AND LICENSE

Copyright 2007-2009 by Makamaka Hannyaharamitu

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself. 

=cut
