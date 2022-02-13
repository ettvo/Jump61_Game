# This makefile is defined to give you the following targets:
#
#    default: Same as check
#    check: For each file, F-1.in, use F-1.in as input to tester.py.
#    clean: Remove various unnecessary files that may have accumulated.
#
# You can use this file without understanding most of it, of course, but
# I strongly recommend that you try to figure it out, and where you cannot,
# that you ask questions.

SHELL = /bin/bash

# Name of class containing main procedure 
MAIN = jump61.Main

# If your python3 implementation has a different name (like just 'python'),
# simply run
#      make PYTHON=python check
# No need to change this file.
PYTHON = python3

# Flags to Java interpreter: check assertions
JFLAGS = -ea

# A non-standard classpath that works Linux, Mac, and Windows.
# We include everything in CLASSPATH, plus the proj2 directory itself
# (from which one should be able to find the amazons classes).
CPATH = "..:$(CLASSPATH):;..;$(CLASSPATH)"

TESTER_FLAGS =

TESTS := *-1.in

.PHONY: default check clean

# First, and therefore default, target.
default:
	"$(MAKE)" -C .. 
	"$(MAKE)" check

# The "CLASSPATH=..." clause in front of the command below temporarily 
# changes the location in which Java searches for classes to be the directory
# that contains your project.
check:
	@echo "Testing application $(MAIN)..."
	@CLASSPATH=$(CPATH) "$(PYTHON)" tester.py $(TESTER_FLAGS) $(TESTS)

# 'make clean' will clean up stuff you can reconstruct.
clean:
	$(RM) -r *~ *.out *.err __pycache__
