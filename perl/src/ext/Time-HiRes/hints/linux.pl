use Config;

# Some linux environments do not have librt.
unless ($Config{cc} eq 'agcc') {
  # Needs to explicitly link against librt to pull in clock_nanosleep.
  $self->{LIBS} = ['-lrt'];
}

