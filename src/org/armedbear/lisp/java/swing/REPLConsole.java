/*
 * ConsoleDocument.java
 *
 * Copyright (C) 2008-2009 Alessio Stalla
 *
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

package org.armedbear.lisp.java.swing;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.lang.RuntimeException;
import java.io.Reader;
import java.io.Writer;

import javax.print.attribute.AttributeSetUtilities;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;

import org.armedbear.lisp.Debug;
import org.armedbear.lisp.Function;
import org.armedbear.lisp.Interpreter;
import org.armedbear.lisp.Lisp;
import org.armedbear.lisp.LispObject;
import org.armedbear.lisp.LispThread;
import org.armedbear.lisp.SimpleString;
import org.armedbear.lisp.SpecialBindingsMark;
import org.armedbear.lisp.Stream;
import org.armedbear.lisp.Symbol;
import org.armedbear.lisp.TwoWayStream;

import static org.armedbear.lisp.Lisp.*;

public class REPLConsole extends DefaultStyledDocument {

    private static JTextArea txt;

  private StringBuffer inputBuffer = new StringBuffer();
	
  private Reader reader = new Reader() {

      @Override
	public void close() throws RuntimeException {
	}

      @Override
      public synchronized int read(char[] cbuf, int off, int len) throws RuntimeException {
        try {
          int length = Math.min(inputBuffer.length(), len);
          while(length <= 0) {
            wait();
            length = Math.min(inputBuffer.length(), len);
          }
          inputBuffer.getChars(0, length, cbuf, off);
          inputBuffer.delete(0, length);
          return length;
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
	
  private Writer writer = new Writer() {
	    
      @Override
	public void close() throws RuntimeException {
	}

      @Override
	public void flush() throws RuntimeException {
	}

      @Override
      public void write(final char[] cbuf, final int off, final int len) throws RuntimeException {
        try {
          final int insertOffs;
          synchronized(reader) {
            if(inputBuffer.toString().matches("^\\s*$")) {
              int length = inputBuffer.length();
              inputBuffer.delete(0, length);
            }
            insertOffs = getLength() - inputBuffer.length();
            reader.notifyAll();
          }
          Runnable r = new Runnable() {
              public void run() {
                synchronized(reader) {
                  try {
				superInsertString(insertOffs, new String(cbuf, off, len), null);
                  } catch(Exception e) {
                    assert(false); //BadLocationException should not happen here
                  }
                }
              }
            };
          SwingUtilities.invokeAndWait(r);
        }  catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
	
  private boolean disposed = false;
  private final Thread replThread;
	
    public REPLConsole(LispObject replFunction, boolean start) {
    final LispObject replWrapper = makeReplWrapper(new Stream(Symbol.SYSTEM_STREAM, new BufferedReader(reader)),
		new Stream(Symbol.SYSTEM_STREAM, new BufferedWriter(writer)), replFunction);
    replThread = new Thread("REPL-thread-" + System.identityHashCode(this)) {
        public void run() {
          while(true) {
            replWrapper.execute();
            yield();
          }
        }
      };
	if (start)
    replThread.start();
  }

  @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    synchronized(reader) {
      int bufferStart = getLength() - inputBuffer.length();
      if(offs < bufferStart) {
        throw new BadLocationException("Can only insert after " + bufferStart, offs);
      }
      superInsertString(offs, str, a);
      inputBuffer.insert(offs - bufferStart, str);
      if(processInputP(inputBuffer, str)) {
        reader.notifyAll();
      }
    }
  }
	
    private void insertString(String str) throws BadLocationException {
	if (str == null || str.length() == 0)
	    return;
	synchronized (reader) {
	    int bufferStart = getLength() - inputBuffer.length();
	    int offs = bufferStart;
	    SimpleAttributeSet attributes = new SimpleAttributeSet();
	    superInsertString(offs, str, attributes);
	    inputBuffer.insert(offs - bufferStart, str);
	    if (processInputP(inputBuffer, str)) {
		reader.notifyAll();
	    }
	}

    }

    protected void superInsertString(int offs, String str, AttributeSet a) throws BadLocationException {
    super.insertString(offs, str, a);
  }
	
  /**
   * Guaranteed to run with exclusive access to the buffer.
     *
     * @param sb
   * @param sb NB sb MUST NOT be destructively modified!!
   * @return
   */
  protected boolean processInputP(StringBuffer sb, String str) {
    if(str.indexOf("\n") == -1) {
      return false;
    }
    int parenCount = 0;
    int len = sb.length();
    for(int i = 0; i < len; i++) {
      char c = sb.charAt(i);
      if(c == '(') {
        parenCount++;
      } else if(c == ')') {
        parenCount--;
        if(parenCount == 0) {
          return true;
        }
      }
    }
    return parenCount <= 0;
  }
	
  @Override
  public void remove(int offs, int len) throws BadLocationException {
    synchronized(reader) {
      int bufferStart = getLength() - inputBuffer.length();
      if(offs < bufferStart) {
        throw new BadLocationException("Can only remove after " + bufferStart, offs);
      }
      super.remove(offs, len);
      inputBuffer.delete(offs - bufferStart, offs - bufferStart + len);
    }
  }
	
  public Reader getReader() {
    return reader;
  }
	
  public Writer getWriter() {
    return writer;
  }
	
  public void setupTextComponent(final JTextComponent txt) {
    addDocumentListener(new DocumentListener() {

        //			@Override
        public void changedUpdate(DocumentEvent e) {
        }

        // @Override
        public void insertUpdate(DocumentEvent e) {
          int len = getLength();
          if(len - e.getLength() == e.getOffset()) { //The insert was at the end of the document
            txt.setCaretPosition(getLength());
          }
        }

        // @Override
        public void removeUpdate(DocumentEvent e) {
        }
      });
    txt.setCaretPosition(getLength());
  }
	
  public void dispose() {
    disposed = true;
    for(DocumentListener listener : getDocumentListeners()) {
      removeDocumentListener(listener);
    }
    try {
      reader.close();
      writer.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    replThread.interrupt(); //really?
  }
	
  private final LispObject debuggerHook = new Function() {
	    
      @Override
      public LispObject execute(LispObject condition, LispObject debuggerHook) {
        if(disposed) {
          return PACKAGE_SYS.findSymbol("%DEBUGGER-HOOK-FUNCTION").execute(condition, debuggerHook);
        } else {
          return NIL;
        }
      }
	    
    };
	
  public LispObject makeReplWrapper(final Stream in, final Stream out, final LispObject fn) {
    return new Function() {	
      @Override
      public LispObject execute() {
        SpecialBindingsMark lastSpecialBinding = LispThread.currentThread().markSpecialBindings();
        try {
          TwoWayStream ioStream = new TwoWayStream(in, out);
          LispThread.currentThread().bindSpecial(Symbol.DEBUGGER_HOOK, debuggerHook);
          LispThread.currentThread().bindSpecial(Symbol.STANDARD_INPUT, in);
          LispThread.currentThread().bindSpecial(Symbol.STANDARD_OUTPUT, out);
          LispThread.currentThread().bindSpecial(Symbol.ERROR_OUTPUT, out);
          LispThread.currentThread().bindSpecial(Symbol.TERMINAL_IO, ioStream);
          LispThread.currentThread().bindSpecial(Symbol.DEBUG_IO, ioStream);
          LispThread.currentThread().bindSpecial(Symbol.QUERY_IO, ioStream);
          return fn.execute();
        } finally {
          LispThread.currentThread().resetSpecialBindings(lastSpecialBinding);
        }
      }
	    
    };
  }
	
  public void disposeOnClose(final Window parent) {
    parent.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          dispose();
          parent.removeWindowListener(this);
        }
      });
  }
	
  public static void main(String[] args) {
	if (args.length == 0)
	    args = new String[] { "--load", "src/test/resources/org/armedbear/lisp/ansi-tests.lisp" };
    LispObject repl = null;
    try {		
	    Interpreter.createInstance();
	    final Interpreter interpreter = Interpreter.getInstance();
	    Symbol.COMPILE_FILE.setSymbolFunction(Symbol.LOAD.getSymbolFunctionOrDie());
	    Symbol.COMPILE_FILE.setBuiltInFunction(true);
	    Lisp.is_running = true;
	    repl = interpreter.eval("#'top-level::top-level-loop");

	    //disableClassloading = true;
	    final REPLConsole d = new REPLConsole(repl, false);

	    // final JTextComponent
	    txt = new JTextArea(d);

    d.setupTextComponent(txt);
    JFrame f = new JFrame();
    f.add(new JScrollPane(txt));
	    centerWindow(f);
	    f.setPreferredSize(new Dimension(500, 250));
	    f.setMaximumSize(new Dimension(10000, 200));
    d.disposeOnClose(f);
    f.setDefaultCloseOperation(f.EXIT_ON_CLOSE);
    f.pack();
    f.setVisible(true);
	    f.setResizable(true);
	    f.setAlwaysOnTop(true);
	    //    Thread.sleep(10000);
	    d.replThread.start();
	    is_testing = true;
	    if (is_testing) {
		Symbol s = checkSymbol(interpreter.eval("'sys:*compile-file-type*"));
		LispObject prev = s.getSymbolValue();
		Debug.assertTrue(prev.getStringValue().equals("abcl"));
		s.setSymbolValue(new SimpleString("lsp"));
		d.insertString("(load \"doit.lsp\")\n");
	    }
	} catch (Throwable e) {
	    e.printStackTrace();
	    System.exit(1); // Ok. We haven't done anything useful yet.
	}

    }

    public static void centerWindow(JFrame frame) {

	Insets insets = frame.getInsets();
	frame.setSize(new Dimension(insets.left + insets.right + 500, insets.top + insets.bottom + 250));
	frame.setVisible(true);
	frame.setResizable(false);

	Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
	int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
	frame.setLocation(x, y);
  }
	
}
