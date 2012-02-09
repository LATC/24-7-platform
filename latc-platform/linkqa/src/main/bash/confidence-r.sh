#!/bin/bash
R --slave --vanilla --quiet --no-save  <<EEE

a <- $2
n <- $1
error <- qnorm(0.975)*a/sqrt(n)
left <- a-error
right <- a+error
left
right
EEE

