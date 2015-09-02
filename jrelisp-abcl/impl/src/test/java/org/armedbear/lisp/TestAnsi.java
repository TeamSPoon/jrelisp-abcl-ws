package org.armedbear.lisp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.armedbear.lisp.Cons;
import org.armedbear.lisp.Nil;
import org.armedbear.lisp.Function;
import org.armedbear.lisp.Interpreter;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.Load;
import org.armedbear.lisp.Package;
import org.armedbear.lisp.Packages;
import org.armedbear.lisp.ProcessingTerminated;
import org.armedbear.lisp.Symbol;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestAnsi {

    public static boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
    
	
	@Test
	public void allAnsiTests() {
		try {
			deleteRecursive(new File("../ansi-test/trunk/ansi-tests/scratch"));
		} catch (Exception ex) {
			// good luck!
		}
		
		Interpreter impl;
		
		try {
			impl = Interpreter.createDefaultInstance(new String[]{"--load","src/test/resources/org/armedbear/lisp/ansi-tests.lisp"});
		} catch (ProcessingTerminated e) {
		}

		Package rtPackage = Packages.findPackageGlobally("REGRESSION-TEST");
		assertNotNull("REGRESSION-TEST package not found", rtPackage);
		Symbol sym = rtPackage.findInternalSymbol("*FAILED-TESTS*");
		if (sym != null) {
			assertEquals(Lisp.NIL, sym.symbolValue());
		}

	}
}
