#!/bin/bash

# March 2014, Scott D. Anderson
# I place this in the public domain; do as you wish with it.
# The code is not guaranteed to be bulletproof; use at your own risk.

if [ $# -eq 0 ]; then
    echo "Usage: $0 [webapp] filename"
    echo "Example: $0 Greetings.java"
    echo "Example: $0 wwellesl Greetings.java"
    echo "Compiles the file with javac and, if it compiles, reloads"
    echo "the given web app, which defaults to your username"
    exit
fi

# use meaningful names for the command-line arguments
if [ $# -eq 1 ]; then
    webapp=`whoami`
    file=$1
else
    webapp=$1
    file=$2
fi

if [ ! -e $file ]; then
    # try adding .java
    file=$file.java
fi

if [ ! -e $file ]; then
    echo "I couldn't find the file: $file"
    exit
fi

base=`basename $file .java`
echo "file is $file; base is $base"

source /home/cs304/bin/classpath.sh

javac $base.java
if [ $? -ne 0 ]; then
    # $? will be zero if the compilation is successful
    echo "Compilation failed."
else 
    # chmod a+r $base.class
    # in case there are related files, make all .class files readable
    chmod a+r *.class
    ~cs304/pub/perl/add-servlet.pl $base
    response=`GET -C scott:tabbby "http://cs.wellesley.edu:8080/manager/html/reload?path=/$webapp"`
    echo "$response" | grep -q "OK - Reloaded" 
    if [ $? -ne 0 ]; then
        echo "failed to update servlet via servlet manager"
        echo "$response" | grep -A 2 'Message:' 
    else
        echo "updated servlet using manager; you should be all set now"
    fi
fi
