/* 
 * Pathname.java
 *
 * Copyright (C) 2003-2007 Peter Graves
 * $Id$
 */

package org.armedbear.lisp.util;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.UUID;

public class SafePushbackReader
	extends PushbackReader
{
	public static final Reader NULLREADER = new NullReader();

	public SafePushbackReader(Reader in)
	{
		super(in);
	}

	public SafePushbackReader(Reader in, int size)
	{
		super(in,size);
	}

	public void close() throws IOException
	{
		if (in != NULLREADER) {
			try {
				super.close();
			} finally {
				in = NULLREADER;
				lock = UUID.randomUUID();
			}
		}
	}

	public void finalize()
		throws Throwable
	{
/*
		//System.out.println("Finalizing SafePushbackReader");
		if ((in instanceof PushbackReader) && !(in instanceof SafePushbackReader)) {
			throw new Exception("Use SafePushbackReader instead of PushbackReader");
		}
*/
		if (in != NULLREADER) {
			close();
		}
		super.finalize();
	}

	private static class NullReader
		extends Reader
	{
		@Override
		public int read(char[] cbuf, int off, int len)
			throws IOException
		{
			throw new IOException("Stream closed");
		}
		@Override
		public void close()
			throws IOException
		{
			// NOOP
		}
	}
}