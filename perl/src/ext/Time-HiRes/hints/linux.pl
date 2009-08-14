use Config;
unless ($Config{ccflags} =~ /-DANDROID/) {
    $self->{LIBS} = ['-lrt'];
}
