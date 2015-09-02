package org.armedbear.lisp.io;

import java.io.File;
import java.net.URL;

public interface IPathname {

	URL toURL();
	File toFile();
	
}
