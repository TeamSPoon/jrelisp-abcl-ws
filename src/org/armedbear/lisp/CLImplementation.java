package org.armedbear.lisp;

import com.vnetpublishing.lisp.clapi.ILispImpl;

public class CLImplementation 
	implements ILispImpl {

	Interpreter interpreter;
	
	protected CLImplementation(Interpreter interpreter) {
		this.interpreter = interpreter; 
	}
}
