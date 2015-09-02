/*
 * Function.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: Function.java,v 1.59 2007/02/23 21:17:33 piso Exp $
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.armedbear.lisp;

import static org.armedbear.lisp.Lisp.*;

public abstract class Function extends Operator
{
    private LispObject propertyList = NIL;
    private int callCount;
    private int hotCount;
    /**
     * The value of *load-truename* which was current when this function
     * was loaded, used for fetching the class bytes in case of disassembly.
     */
    private final LispObject loadedFrom;

    protected Function() {
	LispObject loadTruename = Symbol.LOAD_TRUENAME.symbolValueNoThrow();
	loadedFrom = loadTruename != null ? loadTruename : NIL;
    }

    public Function(String name)
    {
        this(name, (String)null);
    }

    public Function(String name, String arglist)
    {
	this();
        if(arglist != null)
            setLambdaList(new SimpleString(arglist));
        if (name != null) {
            Symbol symbol = Symbol.addFunction(name.toUpperCase(), this);
            if (cold)
                symbol.setBuiltInFunction(true);
            setLambdaName(symbol);
        }
    }

    public Function(Symbol symbol)
    {
	this(symbol, null, null);
    }
    public Function(Symbol symbol, String arglist)
    {
	this(symbol, arglist, null);
    }

    public Function(Symbol symbol, String arglist, String docstring)
    {
	this();
        symbol.setSymbolFunction(this);
        if (cold)
            symbol.setBuiltInFunction(true);
        setLambdaName(symbol);
        if(arglist != null)
        setLambdaList(new SimpleString(arglist));
        if (docstring != null) {
            try {
                symbol.setDocumentation(Symbol.FUNCTION,
                                        new SimpleString(docstring));
            }
            catch (ConditionThrowable t) {
                Debug.assertTrue(false);
            }
        }
    }


    public Function(String name, Package pkg)
    {
        this(name, pkg, false);
    }

    public Function(String name, Package pkg, boolean exported)
    {
        this(name, pkg, exported, null, null);
    }

    public Function(String name, Package pkg, boolean exported,
                    String arglist)
    {
        this(name, pkg, exported, arglist, null);
    }

    public Function(String name, Package pkg, boolean exported,
                    String arglist, String docstring)
    {
	this();
        if (arglist instanceof String)
            setLambdaList(new SimpleString(arglist));
        if (name != null) {
            try {
                Symbol symbol;
                if (exported)
                    symbol = pkg.internAndExport(name.toUpperCase());
                else
                    symbol = pkg.intern(name.toUpperCase());
                symbol.setSymbolFunction(this);
                if (cold)
                    symbol.setBuiltInFunction(true);
                setLambdaName(symbol);
                if (docstring != null)
                    symbol.setDocumentation(Symbol.FUNCTION,
                                            new SimpleString(docstring));
            }
            catch (ConditionThrowable t) {
                Debug.assertTrue(false);
            }
        }
    }

    public Function(LispObject name)
    {
	this();
        setLambdaName(name);
    }

    public Function(LispObject name, LispObject lambdaList)
    {
	this();
        setLambdaName(name);
        setLambdaList(lambdaList);
    }

    public LispObject typeOf()
    {
        return Symbol.FUNCTION;
    }

    public LispObject classOf()
    {
        return BuiltInClass.FUNCTION;
    }

    public LispObject typep(LispObject typeSpecifier)  
    {
        if (typeSpecifier == Symbol.FUNCTION)
            return T;
        if (typeSpecifier == Symbol.COMPILED_FUNCTION)
            return T;
        if (typeSpecifier == BuiltInClass.FUNCTION)
            return T;
        return super.typep(typeSpecifier);
    }

    public final LispObject getPropertyList()
    {
        if (propertyList == null)
            propertyList = NIL;
        return propertyList;
    }

    public final void setPropertyList(LispObject obj)
    {
        if (obj == null)
            throw new NullPointerException();
        propertyList = obj;
    }

    public final void setClassBytes(byte[] bytes)  
    {
        propertyList = putf(propertyList, Symbol.CLASS_BYTES,
                            new JavaObject(bytes));
    }

    public final LispObject getClassBytes() {
	LispObject o = getf(propertyList, Symbol.CLASS_BYTES, NIL);
	if(o != NIL) {
	    return o;
	} else {
	    ClassLoader c = getClass().getClassLoader();
	    if(c instanceof FaslClassLoader) {
		final LispThread thread = LispThread.currentThread(); 
		SpecialBindingsMark mark = thread.markSpecialBindings(); 
		try { 
		    thread.bindSpecial(Symbol.LOAD_TRUENAME, loadedFrom); 
		    return new JavaObject(((FaslClassLoader) c).getFunctionClassBytes(this));
		} catch(Throwable t) {
		    //This is because unfortunately getFunctionClassBytes uses
		    //Debug.assertTrue(false) to signal errors
		    if(t instanceof ControlTransfer) {
			throw (ControlTransfer) t;
		    } else {
			return NIL;
		    }
		} finally { 
		    thread.resetSpecialBindings(mark); 
		}		
	    } else {
		return NIL;
	    }
	}
    }

    public static final Primitive FUNCTION_CLASS_BYTES = new pf_function_class_bytes();
    public static final class pf_function_class_bytes extends Primitive {
	public pf_function_class_bytes() {
	    super("function-class-bytes", PACKAGE_SYS, false, "function");
        }
         
        public LispObject execute(LispObject arg) {
            if (arg instanceof Function) {
                return ((Function) arg).getClassBytes();
	    }
            return type_error(arg, Symbol.FUNCTION);
        }
    }

     
    public LispObject execute()  
    {
        return error(new WrongNumberOfArgumentsException(this, 0));
    }

    public LispObject execute(LispObject arg)  
    {
        return error(new WrongNumberOfArgumentsException(this, 1));
    }

    public LispObject execute(LispObject first, LispObject second)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 2));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 3));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 4));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 5));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 6));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 7));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh, LispObject eighth)
         
    {
        return error(new WrongNumberOfArgumentsException(this, 8));
    }

    public LispObject execute(LispObject[] args)  
    {
        return error(new WrongNumberOfArgumentsException(this));
    }

     
    public LispObject printObject()
    {
        LispObject name = getLambdaName();
        if (name != null && name != NIL) {
            return new SimpleString(unreadableString(name.princToString()));
        }
        // No name.
        LispObject lambdaList = getLambdaList();
        if (lambdaList != null) {
            StringBuilder sb = new StringBuilder("FUNCTION ");
            sb.append("(LAMBDA ");
            if (lambdaList == NIL) {
                sb.append("()");
            } else {
                final LispThread thread = LispThread.currentThread();
                final SpecialBindingsMark mark = thread.markSpecialBindings();
                thread.bindSpecial(Symbol.PRINT_LENGTH, Fixnum.THREE);
                try {
                    sb.append(lambdaList.printObject().toString());
                }
                finally {
                    thread.resetSpecialBindings(mark);
                }
            }
            sb.append(")");
            return new SimpleString(unreadableString(sb.toString()));
        }
        return new SimpleString(unreadableString("FUNCTION"));
    }

    // Used by the JVM compiler.
    public final void argCountError()  
    {
        error(new WrongNumberOfArgumentsException(this));
    }

    // Profiling.
    public final int getCallCount()
    {
        return callCount;
    }

    public void setCallCount(int n)
    {
        callCount = n;
    }

    public final void incrementCallCount()
    {
        ++callCount;
    }

     
    public final int getHotCount()
    {
        return hotCount;
    }

     
    public void setHotCount(int n)
    {
        hotCount = n;
    }

     
    public final void incrementHotCount()
    {
        ++hotCount;
    }
    
     
    public final boolean isFunction() {
  	  return true;
    }
}
