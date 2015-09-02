package org.armedbear.lisp;

import static org.armedbear.lisp.Lisp.*;

public class ArrayIndexTypeError 
	extends TypeError {

	
	public ArrayIndexTypeError(String message)
	{
		super(StandardClass.TYPE_ERROR);
		setFormatControl(message);
		setInstanceSlotValue(Symbol.DATUM, NIL);
		setInstanceSlotValue(Symbol.EXPECTED_TYPE, NIL);
	}
	
}
