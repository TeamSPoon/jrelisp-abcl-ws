package org.armedbear.lisp;

import static org.armedbear.lisp.Lisp.*;

public class RandomStateClass 
	extends StructureClass {

	public static final Symbol DEFSTRUCT_SLOT_DESCRIPTION = PACKAGE_SYS.intern("DEFSTRUCT_SLOT_DESCRIPTION");
	public static final Symbol STATE = internKeyword("STATE");
	public static final Symbol RANDOM_STATE_STATE = internKeyword("RANDOM-STATE-STATE");
	
	public RandomStateClass() {

		super(Symbol.RANDOM_STATE);
		
		
		int n_slots = 1;
		
		// Allocate Slots
		Cons directSlots;
		//LispObject[] instanceSlotNames = new LispObject[1];
		
		// Define STATE Slot
		
		//instanceSlotNames[0] = STATE;
		
		SimpleVector stateSlotDefinition = new SimpleVector(7);
		
		LispObject structure_class = LispClass.findClass(Symbol.STRUCTURE_CLASS);
		
		//setDirectSuperclass(structure_class);
		stateSlotDefinition.aset(0, DEFSTRUCT_SLOT_DESCRIPTION); // Class?
		stateSlotDefinition.aset(1, STATE); // Name?
		stateSlotDefinition.aset(2,Fixnum.getInstance(0)); // Index?
		stateSlotDefinition.aset(3,RANDOM_STATE_STATE); // Func?
		stateSlotDefinition.aset(4,NIL); // Default? (should be sequence?)
		stateSlotDefinition.aset(5,Symbol.SEQUENCE); // Type?
		stateSlotDefinition.aset(6,NIL); // Readonly??
		

		directSlots = new Cons(stateSlotDefinition,NIL);
		Cons slots = directSlots;
        
        setCPL(this, BuiltInClass.STRUCTURE_OBJECT, BuiltInClass.CLASS_T);
        setDirectSlotDefinitions(directSlots);
        setSlotDefinitions(slots);
        setFinalized(true);
    	addClass(Symbol.STRUCTURE_CLASS, this);
        
	}
	
	/*
    @Override
    public LispObject typeOf()
    {
        return Symbol.RANDOM_STATE;
    }
    */

	/*
    @Override
    public LispObject classOf()
    {
        return LispClass.findClass(Symbol.RANDOM_STATE);
    }
	*/
	
    @Override
    public LispObject typep(LispObject type)
    {
    	if (type == Symbol.RANDOM_STATE) {
    		return T;
    	}
    	if (type == BuiltInClass.RANDOM_STATE) {
    		return T;
    	}
        if (type == LispClass.findClass(Symbol.RANDOM_STATE))
            return T;
        
        return super.typep(type);
    }

	public LispObject readState(LispObject data_in) {
		
		RandomStateObject mrso = new RandomStateObject();
		
		if (data_in == NIL) {
			return mrso;
		}
		
		if (!data_in.isCons()) {
			System.out.println("READ HAS A "+data_in.getClass().toString());
			return data_in;
		}
		
		LispObject data = data_in; // Anyone else, need not apply!
		
		LispObject nState = NIL;
		LispObject key;
		LispObject value;
		
		while(data != NIL) {
			key = ((Cons)data).car();
			data = ((Cons)data).cdr();
			if (data != NIL) {
				value = ((Cons)data).car();
				if (key == STATE && value != NIL) {
					nState = value;
				} else {
					System.out.println("KEY A "+key.getClass().toString());
				}
				data = ((Cons)data).cdr(); 
			}
		}
		
		
		int idx = mrso.getSlotIndex(STATE);
		
		synchronized(mrso.lock) {
			mrso.setSlotValue(idx, nState);
		}
		
		return mrso;
	}
	

}
