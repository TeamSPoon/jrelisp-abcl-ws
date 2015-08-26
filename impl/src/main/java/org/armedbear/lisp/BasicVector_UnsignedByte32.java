/*
 * BasicVector_UnsignedByte32.java
 *
 * Copyright (C) 2002-2006 Peter Graves
 * $Id$
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
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.armedbear.lisp;

import static org.armedbear.lisp.Lisp.*;
import java.util.Arrays;

// A basic vector is a specialized vector that is not displaced to another
// array, has no fill pointer, and is not expressly adjustable.
public final class BasicVector_UnsignedByte32 extends AbstractVector
{
  private int capacity;

  private long[] elements;

  public BasicVector_UnsignedByte32(int capacity)
  {
    elements = new long[capacity];
    this.capacity = capacity;
  }

  public BasicVector_UnsignedByte32(LispObject[] array)

  {
    capacity = array.length;
    elements = new long[capacity];
    for (int i = array.length; i-- > 0;)
      elements[i] = array[i].longValue();
  }

  @Override
  public LispObject typeOf()
  {
    return list(Symbol.SIMPLE_ARRAY, UNSIGNED_BYTE_32,
                 new Cons(Fixnum.getInstance(capacity)));
  }

  @Override
  public LispObject classOf()
  {
    return BuiltInClass.VECTOR;
  }

  @Override
  public LispObject typep(LispObject type)
  {
    if (type == Symbol.SIMPLE_ARRAY)
      return T;
    if (type == BuiltInClass.SIMPLE_ARRAY)
      return T;
    return super.typep(type);
  }

  @Override
  public LispObject getElementType()
  {
    return UNSIGNED_BYTE_32;
  }

  @Override
  public boolean isSimpleVector()
  {
    return false;
  }

  @Override
  public boolean hasFillPointer()
  {
    return false;
  }

  @Override
  public boolean isAdjustable()
  {
    return false;
  }

  @Override
  public int capacity()
  {
    return capacity;
  }

  @Override
  public int length()
  {
    return capacity;
  }

  @Override
  public LispObject elt(int index)
  {
    try
      {
        return number(elements[index]);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
        return NIL; // Not reached.
      }
  }

  @Override
  public int aref(int index)
  {
    try
      {
        return (int) elements[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, elements.length);
        return -1; // Not reached.
      }
  }

  @Override
  public long aref_long(int index)
  {
    try
      {
        return elements[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, elements.length);
        return -1; // Not reached.
      }
  }

  @Override
  public LispObject AREF(int index)
  {
    try
      {
        return number(elements[index]);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, elements.length);
        return NIL; // Not reached.
      }
  }

  @Override
  public void aset(int index, LispObject newValue)
  {
    try
      {
        elements[index] = newValue.longValue();
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
      }
  }

  @Override
  public LispObject subseq(int start, int end)
  {
    BasicVector_UnsignedByte32 v = new BasicVector_UnsignedByte32(end - start);
    int i = start, j = 0;
    try
      {
        while (i < end)
          v.elements[j++] = elements[i++];
        return v;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        // FIXME
        return error(new TypeError("Array index out of bounds: " + i + "."));
      }
  }

  @Override
  public void fill(LispObject obj)
  {
    for (int i = capacity; i-- > 0;)
      elements[i] = obj.longValue();
  }
  
  
  @Override
  public void shrink(int n)
  {
    if (n < capacity)
      {
        long[] newArray = new long[n];
        System.arraycopy(elements, 0, newArray, 0, n);
        elements = newArray;
        capacity = n;
        return;
      }
    if (n == capacity)
      return;
    error(new LispError());
  }

  @Override
  public LispObject reverse()
  {
    BasicVector_UnsignedByte32 result = new BasicVector_UnsignedByte32(capacity);
    int i, j;
    for (i = 0, j = capacity - 1; i < capacity; i++, j--)
      result.elements[i] = elements[j];
    return result;
  }

  @Override
  public LispObject nreverse()
  {
    int i = 0;
    int j = capacity - 1;
    while (i < j)
      {
        long temp = elements[i];
        elements[i] = elements[j];
        elements[j] = temp;
        ++i;
        --j;
      }
    return this;
  }

  @Override
  public AbstractVector adjustArray(int newCapacity,
                                     LispObject initialElement,
                                     LispObject initialContents)

  {
    if (initialContents != null)
      {
        LispObject[] newElements = new LispObject[newCapacity];
        if (initialContents.listp())
          {
            LispObject list = initialContents;
            for (int i = 0; i < newCapacity; i++)
              {
                newElements[i] = list.car();
                list = list.cdr();
              }
          }
        else if (initialContents.vectorp())
          {
            for (int i = 0; i < newCapacity; i++)
              newElements[i] = initialContents.elt(i);
          }
        else
          type_error(initialContents, Symbol.SEQUENCE);
        return new BasicVector_UnsignedByte32(newElements);
      }
    if (capacity != newCapacity)
      {
        LispObject[] newElements = new LispObject[newCapacity];
        System.arraycopy(elements, 0, newElements, 0,
                         Math.min(capacity, newCapacity));
        if (initialElement != null)
            for (int i = capacity; i < newCapacity; i++)
                newElements[i] = initialElement;
        return new BasicVector_UnsignedByte32(newElements);
      }
    // No change.
    return this;
  }

  @Override
  public AbstractVector adjustArray(int newCapacity,
                                     AbstractArray displacedTo,
                                     int displacement)
  {
    return new ComplexVector(newCapacity, displacedTo, displacement);
  }
  
  public LispObject printObject() {
	    if (Symbol.PRINT_READABLY.symbolValue() != NIL) {
	    	StringBuilder sb = new StringBuilder(String.format("#.(MAKE-ARRAY %d :ELEMENT-TYPE '(UNSIGNED-BYTE 32) :INITIAL-CONTENTS '(\n",elements.length));
	    	
	    	for(int i=0;i<elements.length;i++) {
	    		sb.append("    ");
	    		sb.append(elements[i]);
	    		if (i > 0 && (i % 3) == 0) {
	    			sb.append("\n");
	    		}
	    	}
	    	sb.append("))");
	    	return new SimpleString(sb.toString());
	    }
	    return super.printObject();
  }
  
  public long[] toArray() {
	  return Arrays.copyOf(elements, elements.length);
  }
  
  public void internValues(long[] values) {
	 elements = Arrays.copyOf(values, values.length);;
  	 capacity = values.length;
  }
}
