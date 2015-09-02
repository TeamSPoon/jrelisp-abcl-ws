/*
 * Bignum.java
 *
 * Copyright (C) 2003-2007 Peter Graves
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

import java.math.BigInteger;

public final class Bignum extends LispInteger
{
  public final BigInteger value;

  private static BigInteger MOST_NEGATIVE_FIXNUM =
          BigInteger.valueOf(Integer.MIN_VALUE);
  private static BigInteger MOST_POSITIVE_FIXNUM =
          BigInteger.valueOf(Integer.MAX_VALUE);

  public static LispInteger getInstance(long l) {
      if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE)
          return Fixnum.getInstance(l);
      else
          return new Bignum(l);
  }

  public static LispInteger getInstance(BigInteger n) {
      if (MOST_NEGATIVE_FIXNUM.compareTo(n) < 0 ||
              MOST_POSITIVE_FIXNUM.compareTo(n) > 0)
          return new Bignum(n);
      else
          return Fixnum.getInstance(n.intValue());
  }

  public static LispInteger getInstance(String s, int radix) {
      BigInteger value = new BigInteger(s, radix);

      return Bignum.getInstance(value);
  }

  private Bignum(long l)
  {
    value = BigInteger.valueOf(l);
  }

  private Bignum(BigInteger n)
  {
    value = n;
  }

   
  public Object javaInstance()
  {
    return value;
  }

   
  public Object javaInstance(Class c) {
    String cn = c.getName();
    if (cn.equals("java.lang.Byte") || cn.equals("byte"))
      return Byte.valueOf((byte)value.intValue());
    if (cn.equals("java.lang.Short") || cn.equals("short"))
      return Short.valueOf((short)value.intValue());
    if (cn.equals("java.lang.Integer") || cn.equals("int"))
      return Integer.valueOf(value.intValue());
    if (cn.equals("java.lang.Long") || cn.equals("long"))
      return Long.valueOf((long)value.longValue());
    return javaInstance();
  }


   
  public LispObject typeOf()
  {
    if (value.signum() > 0)
      return list(Symbol.INTEGER,
                   new Bignum((long)Integer.MAX_VALUE + 1));
    return Symbol.BIGNUM;
  }

   
  public LispObject classOf()
  {
    return BuiltInClass.BIGNUM;
  }

   
  public LispObject typep(LispObject type)
  {
    if (type != null && type.isSymbol())
      {
        if (type == Symbol.BIGNUM)
          return T;
        if (type == Symbol.INTEGER)
          return T;
        if (type == Symbol.RATIONAL)
          return T;
        if (type == Symbol.REAL)
          return T;
        if (type == Symbol.NUMBER)
          return T;
        if (type == Symbol.SIGNED_BYTE)
          return T;
        if (type == Symbol.UNSIGNED_BYTE)
          return value.signum() >= 0 ? T : NIL;
      }
    else if (type != null && type.isLispClass())
      {
        if (type == BuiltInClass.BIGNUM)
          return T;
        if (type == BuiltInClass.INTEGER)
          return T;
        if (type == BuiltInClass.RATIONAL)
          return T;
        if (type == BuiltInClass.REAL)
          return T;
        if (type == BuiltInClass.NUMBER)
          return T;
      }
    else if (type != null && type.isCons())
      {
        if (type.equal(UNSIGNED_BYTE_8))
          return NIL;
        if (type.equal(UNSIGNED_BYTE_32))
          {
            if (minusp())
              return NIL;
            return isLessThan(UNSIGNED_BYTE_32_MAX_VALUE) ? T : NIL;
          }
      }
    return super.typep(type);
  }

   
  public boolean numberp()
  {
    return true;
  }

   
  public boolean integerp()
  {
    return true;
  }

   
  public boolean rationalp()
  {
    return true;
  }

   
  public boolean realp()
  {
    return true;
  }

   
  public boolean eql(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj != null && obj.isBignum())
      {
        if (value.equals(((Bignum)obj).value))
          return true;
      }
    return false;
  }

   
  public boolean equal(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj != null && obj.isBignum())
      {
        if (value.equals(((Bignum)obj).value))
          return true;
      }
    return false;
  }

   
  public boolean equalp(LispObject obj)
  {
    if (obj != null && obj.numberp())
      return isEqualTo(obj);
    return false;
  }

   
  public LispObject ABS()
  {
    if (value.signum() >= 0)
      return this;
    return new Bignum(value.negate());
  }

   
  public LispObject NUMERATOR()
  {
    return this;
  }

   
  public LispObject DENOMINATOR()
  {
    return Fixnum.ONE;
  }

   
  public boolean evenp()
  {
    return !value.testBit(0);
  }

   
  public boolean oddp()
  {
    return value.testBit(0);
  }

   
  public boolean plusp()
  {
    return value.signum() > 0;
  }

   
  public boolean minusp()
  {
    return value.signum() < 0;
  }

   
  public boolean zerop()
  {
    return false;
  }

   
  public int intValue()
  {
    return value.intValue();
  }

   
  public long longValue()
  {
    return value.longValue();
  }

   
  public float floatValue()
  {
    float f = value.floatValue();
    if (Float.isInfinite(f))
      error(new TypeError("The value " + princToString() +
                           " is too large to be converted to a single float."));
    return f;
  }

   
  public double doubleValue()
  {
    double d = value.doubleValue();
    if (Double.isInfinite(d))
      error(new TypeError("The value " + princToString() +
                           " is too large to be converted to a double float."));
    return d;
  }

  public static BigInteger getValue(LispObject obj)
  {
          
    if (obj != null && obj.isBignum())
      {
        return ((Bignum)obj).value;
      }
        type_error(obj, Symbol.BIGNUM);
        // Not reached.
        return null;
  }

   
  public final LispObject incr()
  {
    return number(value.add(BigInteger.ONE));
  }

   
  public final LispObject decr()
  {
    return number(value.subtract(BigInteger.ONE));
  }

   
  public LispObject add(int n)
  {
    return number(value.add(BigInteger.valueOf(n)));
  }

   
  public LispObject add(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return number(value.add(Fixnum.getBigInteger(obj)));
    if (obj != null && obj.isBignum())
      return number(value.add(((Bignum)obj).value));
    if (obj != null && obj.isRatio())
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(value.multiply(denominator).add(numerator),
                      denominator);
      }
    if (obj != null && obj.isSingleFloat())
      return new SingleFloat(floatValue() + ((SingleFloat)obj).value);
    if (obj != null && obj.isDoubleFloat())
      return new DoubleFloat(doubleValue() + ((DoubleFloat)obj).value);
    if (obj != null && obj.isComplex())
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(add(c.getRealPart()), c.getImaginaryPart());
      }
    return type_error(obj, Symbol.NUMBER);
  }

   
  public LispObject subtract(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return number(value.subtract(Fixnum.getBigInteger(obj)));
    if (obj != null && obj.isBignum())
      return number(value.subtract(((Bignum)obj).value));
    if (obj != null && obj.isRatio())
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(value.multiply(denominator).subtract(numerator),
                      denominator);
      }
    if (obj != null && obj.isSingleFloat())
      return new SingleFloat(floatValue() - ((SingleFloat)obj).value);
    if (obj != null && obj.isDoubleFloat())
      return new DoubleFloat(doubleValue() - ((DoubleFloat)obj).value);
    if (obj != null && obj.isComplex())
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(subtract(c.getRealPart()),
                                   Fixnum.ZERO.subtract(c.getImaginaryPart()));
      }
    return type_error(obj, Symbol.NUMBER);
  }

   
  public LispObject multiplyBy(int n)
  {
    if (n == 0)
      return Fixnum.ZERO;
    if (n == 1)
      return this;
    return new Bignum(value.multiply(BigInteger.valueOf(n)));
  }

   
  public LispObject multiplyBy(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      {
        int n = ((Fixnum)obj).value;
        if (n == 0)
          return Fixnum.ZERO;
        if (n == 1)
          return this;
        return new Bignum(value.multiply(BigInteger.valueOf(n)));
      }
    if (obj != null && obj.isBignum())
      return new Bignum(value.multiply(((Bignum)obj).value));
    if (obj != null && obj.isRatio())
      {
        BigInteger n = ((Ratio)obj).numerator();
        return number(n.multiply(value), ((Ratio)obj).denominator());
      }
    if (obj != null && obj.isSingleFloat())
      return new SingleFloat(floatValue() * ((SingleFloat)obj).value);
    if (obj != null && obj.isDoubleFloat())
      return new DoubleFloat(doubleValue() * ((DoubleFloat)obj).value);
    if (obj != null && obj.isComplex())
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(multiplyBy(c.getRealPart()),
                                   multiplyBy(c.getImaginaryPart()));
      }
    return type_error(obj, Symbol.NUMBER);
  }

   
  public LispObject divideBy(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return number(value, Fixnum.getBigInteger(obj));
    if (obj != null && obj.isBignum())
      return number(value, ((Bignum)obj).value);
    if (obj != null && obj.isRatio())
      {
        BigInteger d = ((Ratio)obj).denominator();
        return number(d.multiply(value), ((Ratio)obj).numerator());
      }
    if (obj != null && obj.isSingleFloat())
      return new SingleFloat(floatValue() / ((SingleFloat)obj).value);
    if (obj != null && obj.isDoubleFloat())
      return new DoubleFloat(doubleValue() / ((DoubleFloat)obj).value);
    if (obj != null && obj.isComplex())
      {
        Complex c = (Complex) obj;
        LispObject realPart = c.getRealPart();
        LispObject imagPart = c.getImaginaryPart();
        LispObject denominator =
          realPart.multiplyBy(realPart).add(imagPart.multiplyBy(imagPart));
        return Complex.getInstance(multiplyBy(realPart).divideBy(denominator),
                                   Fixnum.ZERO.subtract(multiplyBy(imagPart).divideBy(denominator)));
      }
    return type_error(obj, Symbol.NUMBER);
  }

   
  public boolean isEqualTo(LispObject obj)
  {
    if (obj != null && obj.isBignum())
      return value.equals(((Bignum)obj).value);
    if (obj != null && obj.isSingleFloat())
      return isEqualTo(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isEqualTo(((DoubleFloat)obj).rational());
    if (obj.numberp())
      return false;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

   
  public boolean isNotEqualTo(LispObject obj)
  {
    if (obj != null && obj.isBignum())
      return !value.equals(((Bignum)obj).value);
    if (obj != null && obj.isSingleFloat())
      return isNotEqualTo(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isNotEqualTo(((DoubleFloat)obj).rational());
    if (obj.numberp())
      return true;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

   
  public boolean isLessThan(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return value.compareTo(Fixnum.getBigInteger(obj)) < 0;
    if (obj != null && obj.isBignum())
      return value.compareTo(((Bignum)obj).value) < 0;
    if (obj != null && obj.isRatio())
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) < 0;
      }
    if (obj != null && obj.isSingleFloat())
      return isLessThan(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isLessThan(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

   
  public boolean isGreaterThan(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return value.compareTo(Fixnum.getBigInteger(obj)) > 0;
    if (obj != null && obj.isBignum())
      return value.compareTo(((Bignum)obj).value) > 0;
    if (obj != null && obj.isRatio())
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) > 0;
      }
    if (obj != null && obj.isSingleFloat())
      return isGreaterThan(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isGreaterThan(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

   
  public boolean isLessThanOrEqualTo(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return value.compareTo(Fixnum.getBigInteger(obj)) <= 0;
    if (obj != null && obj.isBignum())
      return value.compareTo(((Bignum)obj).value) <= 0;
    if (obj != null && obj.isRatio())
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) <= 0;
      }
    if (obj != null && obj.isSingleFloat())
      return isLessThanOrEqualTo(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isLessThanOrEqualTo(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

   
  public boolean isGreaterThanOrEqualTo(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      return value.compareTo(Fixnum.getBigInteger(obj)) >= 0;
    if (obj != null && obj.isBignum())
      return value.compareTo(((Bignum)obj).value) >= 0;
    if (obj != null && obj.isRatio())
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) >= 0;
      }
    if (obj != null && obj.isSingleFloat())
      return isGreaterThanOrEqualTo(((SingleFloat)obj).rational());
    if (obj != null && obj.isDoubleFloat())
      return isGreaterThanOrEqualTo(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

   
  public LispObject truncate(LispObject obj)
  {
    final LispThread thread = LispThread.currentThread();
    LispObject value1, value2;
    try
      {
        if (obj != null && obj.isFixnum())
          {
            BigInteger divisor = ((Fixnum)obj).getBigInteger();
            BigInteger[] results = value.divideAndRemainder(divisor);
            BigInteger quotient = results[0];
            BigInteger remainder = results[1];
            value1 = number(quotient);
            value2 = (remainder.signum() == 0) ? Fixnum.ZERO : number(remainder);
          }
        else if (obj != null && obj.isBignum())
          {
            BigInteger divisor = ((Bignum)obj).value;
            BigInteger[] results = value.divideAndRemainder(divisor);
            BigInteger quotient = results[0];
            BigInteger remainder = results[1];
            value1 = number(quotient);
            value2 = (remainder.signum() == 0) ? Fixnum.ZERO : number(remainder);
          }
        else if (obj != null && obj.isRatio())
          {
            Ratio divisor = (Ratio) obj;
            LispObject quotient =
              multiplyBy(divisor.DENOMINATOR()).truncate(divisor.NUMERATOR());
            LispObject remainder =
              subtract(quotient.multiplyBy(divisor));
            value1 = quotient;
            value2 = remainder;
          }
        else if (obj != null && obj.isSingleFloat())
          {
            // "When rationals and floats are combined by a numerical
            // function, the rational is first converted to a float of the
            // same format." 12.1.4.1
            return new SingleFloat(floatValue()).truncate(obj);
          }
        else if (obj != null && obj.isDoubleFloat())
          {
            // "When rationals and floats are combined by a numerical
            // function, the rational is first converted to a float of the
            // same format." 12.1.4.1
            return new DoubleFloat(doubleValue()).truncate(obj);
          }
        else
          return type_error(obj, Symbol.REAL);
      }
    catch (ArithmeticException e)
      {
        if (obj.zerop())
          return error(new DivisionByZero());
        else
          return error(new ArithmeticError(e.getMessage()));
      }
    return thread.setValues(value1, value2);
  }

   
  public LispObject ash(LispObject obj)
  {
    BigInteger n = value;
    if (obj !=  null && obj.isFixnum())
      {
        int count = ((Fixnum)obj).value;
        if (count == 0)
          return this;
        // BigInteger.shiftLeft() succumbs to a stack overflow if count
        // is Integer.MIN_VALUE, so...
        if (count == Integer.MIN_VALUE)
          return n.signum() >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
        return number(n.shiftLeft(count));
      }
    if (obj != null && obj.isBignum())
      {
        BigInteger count = ((Bignum)obj).value;
        if (count.signum() > 0)
          return error(new LispError("Can't represent result of left shift."));
        if (count.signum() < 0)
          return n.signum() >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
        Debug.bug(); // Shouldn't happen.
      }
    return type_error(obj, Symbol.INTEGER);
  }

   
  public LispObject LOGNOT()
  {
    return number(value.not());
  }

   
  public LispObject LOGAND(int n)
  {
    if (n >= 0)
      return Fixnum.getInstance(value.intValue() & n);
    else
      return number(value.and(BigInteger.valueOf(n)));
  }

   
  public LispObject LOGAND(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      {
        int n = ((Fixnum)obj).value;
        if (n >= 0)
          return Fixnum.getInstance(value.intValue() & n);
        else
          return number(value.and(BigInteger.valueOf(n)));
      }
    else if (obj != null && obj.isBignum())
      {
        final BigInteger n = ((Bignum)obj).value;
        return number(value.and(n));
      }
    else
      return type_error(obj, Symbol.INTEGER);
  }

   
  public LispObject LOGIOR(int n)
  {
    return number(value.or(BigInteger.valueOf(n)));
  }

   
  public LispObject LOGIOR(LispObject obj)
  {
    if (obj != null && obj.isFixnum())
      {
        final BigInteger n = ((Fixnum)obj).getBigInteger();
        return number(value.or(n));
      }
    else if (obj != null && obj.isBignum())
      {
        final BigInteger n = ((Bignum)obj).value;
        return number(value.or(n));
      }
    else
      return type_error(obj, Symbol.INTEGER);
  }

   
  public LispObject LOGXOR(int n)
  {
    return number(value.xor(BigInteger.valueOf(n)));
  }

   
  public LispObject LOGXOR(LispObject obj)
  {
    final BigInteger n;
    if (obj != null && obj.isFixnum())
      n = ((Fixnum)obj).getBigInteger();
    else if (obj != null && obj.isBignum())
      n = ((Bignum)obj).value;
    else
      return type_error(obj, Symbol.INTEGER);
    return number(value.xor(n));
  }

   
  public LispObject LDB(int size, int position)
  {
    BigInteger n = value.shiftRight(position);
    BigInteger mask = BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE);
    return number(n.and(mask));
  }

   
  public int hashCode()
  {
    return value.hashCode();
  }

   
  public LispObject printObject()
  {
    final LispThread thread = LispThread.currentThread();
    final int base = Fixnum.getValue(Symbol.PRINT_BASE.symbolValue(thread));
    String s = value.toString(base).toUpperCase();
    if (Symbol.PRINT_RADIX.symbolValue(thread) != NIL)
      {
        StringBuffer sb = new StringBuffer();
        switch (base)
          {
          case 2:
            sb.append("#b");
            sb.append(s);
            break;
          case 8:
            sb.append("#o");
            sb.append(s);
            break;
          case 10:
            sb.append(s);
            sb.append('.');
            break;
          case 16:
            sb.append("#x");
            sb.append(s);
            break;
          default:
            sb.append('#');
            sb.append(String.valueOf(base));
            sb.append('r');
            sb.append(s);
            break;
          }
        s = sb.toString();
      }
    return new SimpleString(s);
  }
  
   
  public final boolean isBignum() {
	  return true;
  }
}
