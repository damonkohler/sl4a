# Author: Sawyer X
# Email:  xsawyerx@cpan.org or xsawyerx@gmail.com

use strict;
use warnings;

use Android;
use Try::Tiny;

local $|       = 1;
our   $VERSION = '0.02';
my    $droid   = Android->new();

sub event_loop {
  for my $i ( 1 .. 10 ) {
    my $e = $droid->receiveEvent();
    exists $e->{'result'} and return 1;
    sleep 2;
  }

  return;
}

# tests should return TRUE for pass, FALSE for fail
my @tests = (
  [ clipboard => sub {
    my $previous = $droid->getClipboard()->{'result'};
    my $msg      = 'Hello, Perl!';
    $droid->setClipboard($msg);
    my $echo     = $droid->getClipboard()->{'result'};
    $droid->setClipboard($previous);

    return $echo eq $msg;
  } ],

  # we don't have gdata, we can try Net::Google or others instead
  # but since they aren't bundled, this test would be pointless
  # TODO (sawyer): this test :)
  [ gdata => sub {} ],

  [ gps => sub {
    $droid->startLocating();
    try     { return event_loop()    }
    finally { $droid->stopLocating() };
  } ],

  [ sensors => sub {
    $droid->startSensing();
    try     { return event_loop()   }
    finally { $droid->stopSensing() };
  } ],

  [ speak => sub {
    my $result = $droid->speak('Hello, Perl!');
    return not defined $result->{'error'};
  } ],

  [ phone_state => sub {
    $droid->startTrackingPhoneState();
    try     { return event_loop()              }
    finally { $droid->stopTrackingPhoneState() };
  } ],

  [ ringer_silent => sub {
    my $result1 = $droid->toggleRingerSilentMode();
    my $result2 = $droid->toggleRingerSilentMode();
    return ( not defined $result1->{'error'} ) and
           ( not defined $result2->{'error'} );
  } ],

  [ ringer_volume => sub {
    my $get_result = $droid->getRingerVolume();
    $get_result->{'error'} and return;

    $droid->setRingerVolume(0);
    my $set_result = $droid->setRingerVolume( $get_result->{'result'} );
    $set_result->{'error'} and return;

    return 1;
  } ],

  [ get_last_known_location => sub {
    my $result = $droid->getLastKnownLocation();
    return not defined $result->{'error'};
  } ],

  [ geocode => sub {
    my $result = $droid->geocode( 0.0, 0.0, 1 );
    return not defined $result->{'error'};
  } ],

  [ wifi => sub {
    my $result1 = $droid->toggleWifiState();
    my $result2 = $droid->toggleWifiState();
    return ( not defined $result1->{'error'} ) and
           ( not defined $result2->{'error'} );
  } ],

  [ make_toast => sub {
    my $result = $droid->makeToast('Hello, Perl!');
    return not defined $result->{'error'};
  } ],

  [ vibrate => sub {
    my $result = $droid->vibrate();
    return not defined $result->{'error'};
  } ],

  [ notify => sub {
    my $result = $droid->notify('Hello, Perl!');
    return not defined $result->{'error'};
  } ],

  [ get_running_packages => sub {
    my $result = $droid->getRunningPackages();
    return not defined $result->{'error'};
  } ],

  [ alert_dialog => sub {
    my $title   = 'User Interface';
    my $message = 'Welcome to the ASE integration test.';
    $droid->dialogCreateAlert( $title, $message );
    $droid->dialogSetPositiveButtonText('Continue');
    $droid->dialogShow();
    my $response = $droid->dialogGetResponse()->{'result'};
    return $response->{'which'} eq 'positive';
  } ],

  [ alert_dialog_with_buttons => sub {
    my $title   = 'Alert';
    my $message = 'This alert box has 3 buttons and ' .
                  'will wait for you to press one';
    $droid->dialogCreateAlert( $title, $message );
    $droid->dialogSetPositiveButtonText('Yes');
    $droid->dialogSetNegativeButtonText('No');
    $droid->dialogSetNeutralButtonText('Cancel');
    $droid->dialogShow();
    my $response = $droid->dialogGetResponse->{'result'};
    my $which    = $response->{'which'};
    return grep /^$which$/, 'positive', 'negative', 'neutral';
  } ],

  [ spinner_progress => sub {
    my $title   = 'Spinner';
    my $message = 'This is simple spinner progress.';
    $droid->dialogCreateSpinnerProgress( $title, $message );
    $droid->dialogShow();
    sleep 2;
    $droid->dialogDismiss();
    return 1;
  } ],

  [ horizontal_progress => sub {
    my $title   = 'Horizontal';
    my $message = 'This is simple horizontal progress.';
    $droid->dialogCreateHorizontalProgress( $title, $message, 50 );
    $droid->dialogShow();
    for my $x ( 0 .. 50 ) {
      # kinky way of sleeping 0.1 instead of using Time::HiRes
      select undef, undef, undef, 0.1;
      $droid->dialogSetCurrentProgress($x);
    }
    $droid->dialogDismiss();
    return 1;
  } ],

  [ alert_dialog_with_list => sub {
    my $title = 'Alert';
    $droid->dialogCreateAlert($title);
    $droid->dialogSetItems( [ qw/foo bar baz/ ] );
    $droid->dialogShow();
    my $response = $droid->dialogGetResponse()->{'result'};
    return 1;
  } ],

  [ alert_dialog_with_single_choice_list => sub {
    my $title = 'Alert';
    $droid->dialogCreateAlert($title);
    $droid->dialogSetSingleChoiceItems( [ qw/foo bar baz/ ] );
    $droid->dialogSetPositiveButtonText('Yay!');
    $droid->dialogShow();
    my $response = $droid->dialogGetResponse()->{'result'};
    return 1;
  } ],

  [ alert_dialog_with_multi_choice_list => sub {
    my $title = 'Alert';
    $droid->dialogCreateAlert($title);
    $droid->dialogSetMultiChoiceItems( [ qw/foo bar baz/ ], [] );
    $droid->dialogSetPositiveButtonText('Yay!');
    $droid->dialogShow();
    my $response = $droid->dialogGetResponse()->{'result'};
    return 1;
  } ],
);

foreach my $test (@tests) {
  my ( $name, $callback ) = @{$test};
  print "Running $name... ";
  print $callback->() ? "PASS\n" : "FAIL\n";
}

