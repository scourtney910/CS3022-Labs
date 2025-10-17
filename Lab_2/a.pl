#!/usr/bin/perl
sub G {
    local $B;
    while (1) {
        print "+int: ";
        $B = <STDIN>; chomp $B; print "\n";
        if ($B =~ /^\d+$/){last;}
    }return $B;}
sub F {
    my ($B) = @_;
    for (1..$B) {print(' ' x ($B - $_),'#' x $_,'  ','#' x $_,"\n")}}
F(G());