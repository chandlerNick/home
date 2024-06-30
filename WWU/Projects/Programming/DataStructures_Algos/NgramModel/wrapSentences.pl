#! /usr/bin/env perl

use strict;
use warnings;

my $usage  = "usage:\n";
   $usage .= "./wrapSentences.pl inputFile\n";

exit main();

sub main { 
	if( @ARGV != 1 ) {
		print STDERR "\nError: Wrong number of arguments.\n$usage\n";
		exit(1);
	}

	my $IN_FN = $ARGV[0];

	open(my $IN_FILE,$IN_FN)
		or die("Error: Unable to open file $IN_FN\n");

	while( !eof($IN_FILE) ) {
		my $line = <$IN_FILE>;
		chomp($line);
		$line =~ s///g;
		if( $line =~ m/.*\.\s*$/ ) {
			$line = "$line\n";
		}
		$line =~ s/(?!(Mr|Dr|Mrs|Miss|Ms|Prof))\.  ?([A-Z])/\n$2/g;
		chomp($line);
		print " $line";
	}

	return 0;
}
