# jrelisp-abcl
JRE Lisp - ABCL Implementation

## Status

This project is not under active development and is only provided for achival purposes.  This code is not suitable for any particular purpose and the Authors do not take any responsiblity for it's use.

## Purpose

The purpose of this branch is to better integrate common lisp into existing JVM based enterprise applications. 

# Armed Bear Common Lisp

GENERAL INFORMATION
-------------------

Armed Bear Common Lisp is a conforming implementation of ANSI Common
Lisp that runs in a Java virtual machine.  It compilebs Lisp code
directly to Java byte code.


LICENSE
=======

Armed Bear Common Lisp is distributed under the GNU General Public
License with a classpath exception (see "Classpath Exception" below).

A copy of GNU General Public License (GPL) is included in this
distribution, in the file COPYING.

Linking this software statically or dynamically with other modules is
making a combined work based on this software. Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

** Classpath Exception 

As a special exception, the copyright holders of this software give
you permission to link this software with independent modules to
produce an executable, regardless of the license terms of these
independent modules, and to copy and distribute the resulting
executable under terms of your choice, provided that you also meet,
for each linked independent module, the terms and conditions of the
license of that module. An independent module is a module which is not
derived from or based on this software. If you modify this software,
you may extend this exception to your version of the software, but you
are not obligated to do so. If you do not wish to do so, delete this
exception statement from your version.

BUILDING FROM SOURCE RELEASE
============================

Using Maven
-----------

Download a release from github then execute

mvn clean package

The executable jar file will be placed in the subfolder named target/

Using Ant (Depricated)
----------------------

Download a release from github then execute

Then simply execute

    unix$ ant

or

    dos> ant.bat

from the directory containing this README file will create an
executable wrapper ('abcl' under UNIX, 'abcl.bat' under Windows).  Use
this wrapper to start ABCL.

Building from Lisp
------------------

Building from a Lisp is the most venerable and untested way of
building ABCL.  It produces a "non-standard" version of the
distribution that doesn't share build instructions with the previous
two methods, but it still may be of interest to those who absolutely
don't want to know anything about Java.

First, copy the file 'customizations.lisp.in' to 'customization.lisp',
in the directory containing this README file, editing to suit your
situation, paying attention to the comments in the file.  The critical
step is to have Lisp special variable '*JDK*' point to the root of the
Java Development Kit.  Underneath the directory referenced by the
value of '*JDK*' there should be an executable Java compiler in
'bin/javac' ('bin/javac.exe' under Windows).

Then, one may either use the 'build-from-lisp.sh' shell script or load
the necessary files into your Lisp image by hand.

** Using the 'build-from-lisp.sh' script

Under UNIX-like systems, you may simply invoke the
'build-from-lisp.sh' script as './build-from-lisp.sh
<lisp-of-choice>', e.g.

    unix$ ./build-from-lisp.sh sbcl

After a successful build, you may use 'abcl' ('abcl.bat' on Windows)
to start ABCL.  Note that this wrappers contain absolute paths, so
you'll need to edit them if you move things around after the build.

If you're developing on ABCL, you may want to use

    unix$ ./build-from-lisp.sh <implementation> --clean=nil

to not do a full rebuild.

In case of failure in the javac stage, you might try this:

    unix$ ./build-from-lisp.sh <implementation> --full=t --clean=t --batch=nil

This invokes javac separately for each .java file, which avoids running
into limitations on command line length (but is a lot slower).

** Building from another Lisp by hand

There is also an ASDF definition in 'abcl.asd' for the BUILD-ABCL
which can be used to load the necessary Lisp definitions, after which

    CL-USER> (build-abcl:build-abcl :clean t :full t)

will build ABCL.  If ASDF isn't present, simply LOAD the
'customizations.lisp' and 'build-abcl.lisp' files to achieve the same
effect as loading the ASDF definition.


BUGS
====

JRE Lisp ABCL is a conforming ANSI Common Lisp implementation.  Any other
behavior should be reported as a bug.

ABCL has a manual stating its conformance to the ANSI standard,
providing a compliant and practical Common Lisp implementation.
Because of this, 

### Tests 

JRE Lisp now fails only 3 out of 21708 total tests in the ANSI CL
test suite (derived from the tests originally written for GCL).

Maxima's test suite runs without failures.

ABCL comes with a test suite, see the output of `ant help.test` for more
information.

# Authors
    Ralph Ritoch

# Original Authors 

On behalf of all ABCL development team and contributors,

    Erik Huelsmann
    Mark Evenson
    Rudolf Schlatte
    Alessio Stalla
    Ville Voutilainen

August 2015
