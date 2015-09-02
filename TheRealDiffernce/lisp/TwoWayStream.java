/*
 * TwoWayStream.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: TwoWayStream.java,v 1.27 2007/02/23 21:17:35 piso Exp $
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

public class TwoWayStream extends Stream
{
    public final Stream in;
    public final Stream out;

    public TwoWayStream(Stream in, Stream out)
    {
        super(Symbol.TWO_WAY_STREAM);
        this.in = in;
        this.out = out;
        isInputStream = true;
        isOutputStream = true;
    }

    public TwoWayStream(Stream in, Stream out, boolean interactive)
    {
        this(in, out);
        setInteractive(interactive);
    }

    public LispObject getElementType()
    {
        LispObject itype = in.getElementType();
        LispObject otype = out.getElementType();
        if (itype.equal(otype))
            return itype;
        return list3(Symbol.AND, itype, otype);
    }

    public Stream getInputStream()
    {
        return in;
    }

    public Stream getOutputStream()
    {
        return out;
    }

    public boolean isCharacterInputStream()
    {
        return in.isCharacterInputStream();
    }

    public boolean isBinaryInputStream()
    {
        return in.isBinaryInputStream();
    }

    public boolean isCharacterOutputStream()
    {
        return out.isCharacterOutputStream();
    }

    public boolean isBinaryOutputStream()
    {
        return out.isBinaryOutputStream();
    }

    public LispObject typeOf()
    {
        return Symbol.TWO_WAY_STREAM;
    }

    public LispObject classOf()
    {
        return BuiltInClass.TWO_WAY_STREAM;
    }

    public LispObject typep(LispObject type)
    {
        if (type == Symbol.TWO_WAY_STREAM)
            return T;
        if (type == BuiltInClass.TWO_WAY_STREAM)
            return T;
        return super.typep(type);
    }

    // Returns -1 at end of file.

    protected int _readChar() throws java.io.IOException
    {
        return in._readChar();
    }


    protected void _unreadChar(int n) throws java.io.IOException
    {
        in._unreadChar(n);
    }


    protected boolean _charReady() throws java.io.IOException
    {
        return in._charReady();
    }

    public void _writeChar(char c)
    {
        out._writeChar(c);
    }

    public void _writeChars(char[] chars, int start, int end)

    {
        out._writeChars(chars, start, end);
    }

    public void _writeString(String s)
    {
        out._writeString(s);
    }

    public void _writeLine(String s)
    {
        out._writeLine(s);
    }

    // Reads an 8-bit byte.
    public int _readByte()
    {
        return in._readByte();
    }

    // Writes an 8-bit byte.
    public void _writeByte(int n)
    {
        out._writeByte(n);
    }

    public void _finishOutput()
    {
        out._finishOutput();
    }

    public void _clearInput()
    {
        in._clearInput();
    }

    public LispObject listen()
    {
        return in.listen();
    }

    public LispObject freshLine()
    {
        return out.freshLine();
    }

    public LispObject close(LispObject abort)
    {
        // "The effect of CLOSE on a constructed stream is to close the
        // argument stream only. There is no effect on the constituents of
        // composite streams."
        setOpen(false);
        return T;
    }


    public LispObject printObject()
    {
        return new SimpleString(unreadableString("TWO-WAY-STREAM"));
    }

    // ### make-two-way-stream input-stream output-stream => two-way-stream
    private static final Primitive MAKE_TWO_WAY_STREAM =
        new Primitive(Symbol.MAKE_TWO_WAY_STREAM, "input-stream output-stream")
    {
        public LispObject execute(LispObject first, LispObject second)

        {
            final Stream in = checkStream(first);
            final Stream out = checkStream(second);
            if (!in.isInputStream())
                return type_error(in, list2(Symbol.SATISFIES,
                                                 Symbol.INPUT_STREAM_P));
            if (!out.isOutputStream())
                return type_error(out, list2(Symbol.SATISFIES,
                                                  Symbol.OUTPUT_STREAM_P));
            return new TwoWayStream(in, out);
        }
    };

    // ### two-way-stream-input-stream two-way-stream => input-stream
    private static final Primitive TWO_WAY_STREAM_INPUT_STREAM =
        new Primitive(Symbol.TWO_WAY_STREAM_INPUT_STREAM, "two-way-stream")
    {
        public LispObject execute(LispObject arg)
        {
           if (arg instanceof TwoWayStream)
                return ((TwoWayStream)arg).in;
                return type_error(arg, Symbol.TWO_WAY_STREAM);
            }
    };

    // ### two-way-stream-output-stream two-way-stream => output-stream
    private static final Primitive TWO_WAY_STREAM_OUTPUT_STREAM =
        new Primitive(Symbol.TWO_WAY_STREAM_OUTPUT_STREAM, "two-way-stream")
    {
        public LispObject execute(LispObject arg)
        {
           if (arg instanceof TwoWayStream)
                return ((TwoWayStream)arg).out;
                return type_error(arg, Symbol.TWO_WAY_STREAM);
        }
    };
}
