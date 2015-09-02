/*
 * RandomState.java
 *
 * Copyright (C) 2003-2005 Peter Graves
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.Random;

public final class RandomStateObject 
	extends StructureObject
{

	public static int min_values = 100;
	public static int max_values = 500;
	
	public static final LispObject STATE = RandomStateClass.STATE;
	
	protected Object lock = new Object();
    private Random random;
    
    public RandomStateObject()
    {
    	super(Symbol.RANDOM_STATE,initSlots(T));
    	random = new Random(parseSeed());
    }

    public RandomStateObject(LispObject rs)
    {
    	super(Symbol.RANDOM_STATE,initSlots(rs));
    	random = new Random(parseSeed());
    }

    private static LispObject[] initSlots(LispObject rs) {
    	
    	
    	StructureClass structureClass = BuiltInClass.RANDOM_STATE;
    	
    	LispObject slotDefinitions = structureClass.getSlotDefinitions();
    	
    	//SimpleVector effectiveSlots = (SimpleVector)structureClass.getSlotDefinitions();
    	//int effectiveSlotsLen = effectiveSlots.length();
    	
    	LispObject[] effectiveSlots = ((Cons)slotDefinitions).copyToArray();
    	int effectiveSlotsLen = effectiveSlots.length;
    	
    	LispObject[] effectiveSlotsArray = new LispObject[effectiveSlotsLen];
    	for(int i=0; i<effectiveSlotsLen;i++) {
    		//effectiveSlotsArray[i] = effectiveSlots.elt(i);
    		effectiveSlotsArray[i] = effectiveSlots[i];
    	}
    	LispObject[] slots = new LispObject[effectiveSlotsArray.length];
    	
    	int myidx = -1;
    	
    	for (int i = 0; i < effectiveSlotsArray.length; i++) {
    	    SimpleVector slotDefinition = (SimpleVector) effectiveSlotsArray[i];
    		//LispObject[] slotDefinition = ((Cons)effectiveSlotsArray[i]).copyToArray();
    		
    		
    	    LispObject candidateSlotName = slotDefinition.AREF(1);
    		//LispObject candidateSlotName = slotDefinition[1];
    		
    		//LispObject candidateSlotName = ((SlotDefinition)effectiveSlotsArray[i]).getInstanceSlotValue(Symbol.NAME);
    	    if(STATE == candidateSlotName) {
    	    	myidx = i;
    	    	i = effectiveSlotsArray.length;
    	    }
    	}
    	
    	if (myidx == -1) {
    		return null; // You are floating in the void.
    	}
    	
    	if (rs == NIL) {
    		rs = (RandomStateObject)Symbol._RANDOM_STATE_.symbolValue();
    	}
    	
    	if (rs != null && rs.isRandomStateObject()) {
    		RandomStateObject rso= (RandomStateObject)rs;
    		int idx = rso.getSlotIndex(STATE);
    		synchronized(rso.lock) {
    			BasicVector_UnsignedByte32 fseed = (BasicVector_UnsignedByte32)rso.getSlotValue(idx);
    			int flen = fseed.length();
    			BasicVector_UnsignedByte32 seed = new BasicVector_UnsignedByte32(flen);
    			for(int i=0;i<flen;i++) {
    				seed.aset(i, fseed.AREF(i)); 
    			}
    			slots[myidx] = seed;
    		}
    	} else if (rs == T) {
    		Random tmp_rand = new Random();
    		int len = Math.abs(min_values) == Math.abs(max_values) ? 
    				  min_values : 
    				  (int)(Math.abs(min_values) 
    				       + Math.abs((Math.abs(tmp_rand.nextLong())) 
    				    		      % Math.abs(max_values - min_values)));
    		BasicVector_UnsignedByte32 seed = new BasicVector_UnsignedByte32(len);
    		int sv;
    		for(int i=0;i<len;i++) {
    			sv = ((i % 2) > 0) ? Integer.MIN_VALUE : 0;
    			sv = Math.abs(tmp_rand.nextInt() ^ sv);
    			seed.aset(i, Fixnum.getInstance(sv));
    		}
    		slots[myidx] = seed;
    	} else {
    		RandomStateObject rso= (RandomStateObject)Symbol._RANDOM_STATE_.symbolValue();
    		int idx = rso.getSlotIndex(STATE);
    		synchronized(rso.lock) {
    			BasicVector_UnsignedByte32 fseed = (BasicVector_UnsignedByte32)rso.getSlotValue(idx);
    			int flen = fseed.length();
    			BasicVector_UnsignedByte32 seed = new BasicVector_UnsignedByte32(flen);
    			for(int i=0;i<flen;i++) {
    				seed.aset(i, fseed.AREF(i)); 
    			}
    			slots[myidx] = seed;
    		}
    	}
    	
    	return slots;
    }
    
    private long parseSeed() {
    	int idx = getSlotIndex(STATE);
		BasicVector_UnsignedByte32 _seed = (BasicVector_UnsignedByte32)getSlotValue(idx);
		long[] seed = _seed.toArray();
    	long s = 0;
    	int r;
    	long sv;
    	synchronized(lock) {
    		int sl = seed.length;
    		for(int i=0;i<sl;i++) {
    			sv = seed[i];
    			r = (int)(i % 64);
    			s  = s ^ ((sv >> r) | (sv << (64 - r)));  
    		}
    	}
    	return s;
    }
    
    private void nextSeed() {
    	synchronized(lock) {
    		int idx = getSlotIndex(STATE);
    		BasicVector_UnsignedByte32 _seed = (BasicVector_UnsignedByte32)getSlotValue(idx);
    		long[] seed = _seed.toArray();
    		int len = seed.length;
    		for(int i=0;i<len;i++) {
    			seed[i] = Math.abs((int)(random.nextLong() % Integer.MAX_VALUE));
    		}
    		_seed.internValues(seed);
    		setSlotValue(idx,_seed);
    	}
    }
    
    
    @Override
    public LispObject typeOf()
    {
        return Symbol.RANDOM_STATE;
    }

    @Override
    public LispObject classOf()
    {
        return BuiltInClass.RANDOM_STATE;
    }

    @Override
    public LispObject typep(LispObject type)
    {
        if (type == Symbol.RANDOM_STATE)
            return T;
        if (type == BuiltInClass.RANDOM_STATE)
            return T;
        return super.typep(type);
    }

    @Override
    public LispObject printObject()
    {
    	if (Symbol.PRINT_READABLY.symbolValue() != NIL) {
    	
    		int c = slotCount();
    		StringBuilder sb = new StringBuilder("#S(RANDOM-STATE ");
    		
    		for (int i=0; i< c;i++) {
    			
    			sb.append("\n  ");
    			sb.append(getSlotName(i).printObject().toString());
    			sb.append(" ");
    			sb.append(getSlotValue(i).printObject().toString());
    		}
    	
    		sb.append(")");
    		return new SimpleString(sb.toString());
    	}
    	
        return new SimpleString(unreadableString("RANDOM-STATE"));
    }

    public LispObject random(LispObject arg)
    {
    	random.setSeed(parseSeed());
    	
        if (arg != null && arg.isFixnum()) {
            int limit = ((Fixnum)arg).value;
            if (limit > 0) {
                int n = random.nextInt((int)limit);
                nextSeed();
                return Fixnum.getInstance(n);
            }
        } else if (arg != null && arg.isBignum()) {
            BigInteger limit = ((Bignum)arg).value;
            if (limit.signum() > 0) {
                int bitLength = limit.bitLength();
                BigInteger rand = new BigInteger(bitLength + 1, random);
                nextSeed();
                BigInteger remainder = rand.remainder(limit);
                
                return number(remainder);
            }
        } else if (arg != null && arg.isSingleFloat()) {
            float limit = ((SingleFloat)arg).value;
            if (limit > 0) {
                float rand = random.nextFloat();
                nextSeed();
                return new SingleFloat(rand * limit);
            }
        } else if (arg != null && arg.isDoubleFloat()) {
            double limit = ((DoubleFloat)arg).value;
            if (limit > 0) {
                double rand = random.nextDouble();
                nextSeed();
                return new DoubleFloat(rand * limit);
            }
        }
        return type_error(arg, list(Symbol.OR,
                                          list(Symbol.INTEGER, Fixnum.ONE),
                                          list(Symbol.FLOAT, list(Fixnum.ZERO))));
    }

    // ### random limit &optional random-state => random-number
    private static final Primitive RANDOM =
        new Primitive(Symbol.RANDOM, "limit &optional random-state")
    {
        @Override
        public LispObject execute(LispObject arg)
        {
            RandomStateObject randomState =
                (RandomStateObject) Symbol._RANDOM_STATE_.symbolValue();
            return randomState.random(arg);
        }
        @Override
        public LispObject execute(LispObject first, LispObject second)

        {
            if (second != null && second.isRandomStateObject()) {
                RandomStateObject randomState = (RandomStateObject) second;
                return randomState.random(first);
            }
            return type_error(first, Symbol.RANDOM_STATE);
        }
    };

    // ### make-random-state &optional state
    private static final Primitive MAKE_RANDOM_STATE =
        new Primitive(Symbol.MAKE_RANDOM_STATE, "&optional state")
    {
        @Override
        public LispObject execute()
        {
            return new RandomStateObject((RandomStateObject)Symbol._RANDOM_STATE_.symbolValue());
        }
        @Override
        public LispObject execute(LispObject arg)

        {
            if (arg == NIL)
                return new RandomStateObject((RandomStateObject)Symbol._RANDOM_STATE_.symbolValue());
            if (arg == T)
                return new RandomStateObject();
            if (arg != null && arg.isRandomStateObject())
                return new RandomStateObject((RandomStateObject)arg);
            return type_error(arg, Symbol.RANDOM_STATE);
        }
    };

    // ### random-state-p
    private static final Primitive RANDOM_STATE_P =
        new Primitive(Symbol.RANDOM_STATE_P, "object")
    {
        @Override
        public LispObject execute(LispObject arg)
        {
            return arg != null && arg.isRandomStateObject() ? T : NIL;
        }
    };
    
    @Override
    public final boolean isRandomStateObject() {
    	return true;
    }
}
