/*
 * AbstractVector.java
 *
 * Copyright (C) 2003-2006 Peter Graves
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

public abstract class AbstractVector extends AbstractArray
{
  public LispObject typep(LispObject type)  
  {
    if (type == Symbol.VECTOR)
      return T;
    if (type == BuiltInClass.VECTOR)
      return T;
    if (type == Symbol.SEQUENCE)
      return T;
    if (type == BuiltInClass.SEQUENCE)
      return T;
    return super.typep(type);
  }


  public final boolean vectorp()
  {
    return true;
  }

  public boolean equalp(LispObject obj)  
  {
    if (obj != null && obj.isAbstractVector())
      {
        if (length() != obj.length())
          return false;
        AbstractVector v = (AbstractVector) obj;
        for (int i = length(); i-- > 0;)
          if (!AREF(i).equalp(v.AREF(i)))
            return false;
        return true;
      }
    return false;
  }

  public final int getRank()
  {
    return 1;
  }

  public final LispObject getDimensions()
  {
    return new Cons(Fixnum.getInstance(capacity()));
  }

  public final int getDimension(int n)  
  {
    if (n != 0)
      {
        error(new TypeError("bad dimension for vector"));
        // Not reached.
        return 0;
      }
    return capacity();
  }

  public final int getTotalSize()
  {
    return capacity();
  }

  public abstract int capacity();

  public abstract LispObject subseq(int start, int end)  ;

  public LispObject deleteEq(LispObject item)  
  {
    final int limit = length();
    int i = 0;
    int j = 0;
    while (i < limit)
      {
        LispObject obj = AREF(i++);
        if (obj != item)
          aset(j++, obj);
      }
    final int newLength = j;
    if (newLength < capacity())
      shrink(newLength);
    return this;
  }

  public LispObject deleteEql(LispObject item)  
  {
    final int limit = length();
    int i = 0;
    int j = 0;
    while (i < limit)
      {
        LispObject obj = AREF(i++);
        if (!obj.eql(item))
          aset(j++, obj);
      }
    final int newLength = j;
    if (newLength < capacity())
      shrink(newLength);
    return this;
  }

  public abstract void shrink(int n)  ;

  public int checkIndex(int index)  
  {
    if (index < 0 || index >= capacity())
      badIndex(index, capacity());
    return index;
  }

  protected void badIndex(int index, int limit)  
  {
    StringBuilder sb = new StringBuilder("Invalid array index ");
    sb.append(index);
    sb.append(" for ");
    sb.append(princToString());
    if (limit > 0)
      {
        sb.append(" (should be >= 0 and < ");
        sb.append(limit);
        sb.append(").");
      }
    error(new TypeError(sb.toString(),
                         Fixnum.getInstance(index),
                         list(Symbol.INTEGER,
                               Fixnum.ZERO,
                               Fixnum.getInstance(limit - 1))));

  }

  public void setFillPointer(int n)  
  {
    noFillPointer();
  }

  public void setFillPointer(LispObject obj)  
  {
    noFillPointer();
  }

  public boolean isSimpleVector()
  {
    return false;
  }

  public abstract LispObject reverse()  ;

  public LispObject nreverse()  
  {
    int i = 0;
    int j = length() - 1;
    while (i < j)
      {
        LispObject temp = AREF(i);
        aset(i, AREF(j));
        aset(j, temp);
        ++i;
        --j;
      }
    return this;
  }

   
  public LispObject printObject()
  {
    final LispThread thread = LispThread.currentThread();
    if (Symbol.PRINT_READABLY.symbolValue(thread) != NIL)
      {
        StringBuilder sb = new StringBuilder("#(");
        final int limit = length();
        for (int i = 0; i < limit; i++)
          {
            if (i > 0)
              sb.append(' ');
            sb.append(AREF(i).printObject());
          }
        sb.append(')');
        return new SimpleString(sb.toString());
      }
    else if (Symbol.PRINT_ARRAY.symbolValue(thread) != NIL)
      {
        int maxLevel = Integer.MAX_VALUE;
        final LispObject printLevel =
          Symbol.PRINT_LEVEL.symbolValue(thread);
        if (printLevel != null && printLevel.isFixnum())
          maxLevel = ((Fixnum)printLevel).value;
        LispObject currentPrintLevel =
          _CURRENT_PRINT_LEVEL_.symbolValue(thread);
        int currentLevel = Fixnum.getValue(currentPrintLevel);
        if (currentLevel < maxLevel)
          {
            StringBuffer sb = new StringBuffer("#(");
            int maxLength = Integer.MAX_VALUE;
            final LispObject printLength =
              Symbol.PRINT_LENGTH.symbolValue(thread);
            if (printLength != null && printLength.isFixnum())
              maxLength = ((Fixnum)printLength).value;
            final int length = length();
            final int limit = Math.min(length, maxLength);
            final SpecialBindingsMark mark = thread.markSpecialBindings();
            thread.bindSpecial(_CURRENT_PRINT_LEVEL_, currentPrintLevel.incr());
            try
              {
                for (int i = 0; i < limit; i++)
                  {
                    if (i > 0)
                      sb.append(' ');
                    sb.append(AREF(i).printObject());
                  }
              }
            finally
              {
                thread.resetSpecialBindings(mark);
              }
            if (limit < length)
              sb.append(limit > 0 ? " ..." : "...");
            sb.append(')');
            return new SimpleString(sb.toString());
          }
        else
          return new SimpleString("#");
      }
    else
      {
        StringBuffer sb = new StringBuffer();
        sb.append(isSimpleVector() ? "SIMPLE-VECTOR " : "VECTOR ");
        sb.append(capacity());
        return new SimpleString(unreadableString(sb.toString()));
      }
  }

  // For EQUALP hash tables.
  public int psxhash()
  {
    try
      {
        final int length = length();
        final int limit = length < 4 ? length : 4;
        long result = 48920713; // Chosen at random.
        for (int i = 0; i < limit; i++)
          result = mix(result, AREF(i).psxhash());
        return (int) (result & 0x7fffffff);
      }
    catch (Throwable t)
      {
        // Shouldn't happen.
        Debug.trace(t);
        return 0;
      }
  }

  public abstract AbstractArray adjustArray(int size,
                                              LispObject initialElement,
                                              LispObject initialContents)
     ;
  public abstract AbstractArray adjustArray(int size,
                                              AbstractArray displacedTo,
                                              int displacement)
     ;


  public AbstractArray adjustArray(int[] dims,
                                              LispObject initialElement,
                                              LispObject initialContents)
    {
      return adjustArray(dims[0], initialElement, initialContents);
  }

  public AbstractArray adjustArray(int[] dims,
                                              AbstractArray displacedTo,
                                              int displacement)
    {
      return adjustArray(dims[0], displacedTo, displacement);
  }
  
  public final boolean isAbstractVector() {
	  return true;
  }
}
