package org.armedbear.lisp;

import com.vnetpublishing.lisp.clapi.ILispImpl;
import com.vnetpublishing.lisp.clapi.ILispDispatcher;

public class CLDispatcher implements ILispDispatcher {
	
	public ILispImpl dispatch(String[] args) {
		
		final Interpreter interpreter = Interpreter.createDefaultInstance(args);
		
        Runnable r = new Runnable() {        	
            public void run() {
               try {
                    if (interpreter != null)
                            interpreter.run();
                } catch (ProcessingTerminated e) {
                    System.exit(e.getStatus());
                }
            }
        };
        new Thread(null, r, "interpreter", 4194304L).start();
        
        return new CLImplementation(interpreter);
	}

}
