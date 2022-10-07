#!/bin/sh
find . -type d | tr "/" "." | cut -b 3- | awk ' { print $1 ":" } '

