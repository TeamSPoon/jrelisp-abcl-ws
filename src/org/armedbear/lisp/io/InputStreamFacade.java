package org.armedbear.lisp.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class InputStreamFacade extends InputStream {

	InputStream is = null;
	URL url;
	protected final UUID uuid = UUID.randomUUID();
	
	Throwable ctrace = null;
	
	public static boolean debug = false;
	
	public InputStreamFacade(IPathname path, boolean do_cache) {
		
		if (debug) {
			try {
				throw new Exception("Trace");
			} catch (Throwable t) {
				ctrace = t;
			}
		}
		
		url = path.toURL();
		
		try {
			if ("file".equalsIgnoreCase(url.toURI().getScheme())) {
				is = new FileInputStream(path.toFile());
			} else {
				is = url.openStream();
			}
			
			//logDebug("OPENED");
			
			if (do_cache) {
				try {
					preload();
				} catch (IOException e) {
					try {
						close();
					} catch (IOException e1) {
						// Ok, this is bad.
					}
				}
			}
			
		} catch (URISyntaxException e) {
			// Must be a bug somewhere
			e.printStackTrace();
			is = null;
		} catch (FileNotFoundException e) {
			is = null;
		} catch (IOException e) {
			is = null;
		}
		
		

	}
	
	

	
	public InputStreamFacade(URL url, boolean do_cache) {
		
		try {
			throw new Exception("Trace");
		} catch (Throwable t) {
			ctrace = t;
		}
		
		this.url = url;
		
		try {
			if ("file".equals(url.toURI().getScheme().toLowerCase())) {
				is = new FileInputStream(new File(url.toURI()));
			} else {
				is = url.openStream();
			}
			
			if (do_cache) {
				try {
					preload();
				} catch (IOException e) {
					try {
						close();
					} catch (IOException e1) {
						// Ok, this is bad.
					}
				}
			}
			
			//logDebug("OPENED");
		} catch (URISyntaxException e) {
			// Must be a bug somewhere
			e.printStackTrace();
			is = null;
		} catch (FileNotFoundException e) {
			is = null;
		} catch (IOException e) {
			is = null;
		}
		
		

		
	}
	
	
	public InputStreamFacade(IPathname path) 
	{
		this(path,false);
	}
	
	
	public InputStreamFacade(URL url) 
	{
		this(url,false);
	}
	
	private void preload() throws IOException {
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();
		is.close();
		is = new ByteArrayInputStream(buffer.toByteArray());
	}
	
	
	
	private void logDebug(String msg) 
	{
		System.err.println(String.format("InputStreamFacade %s#%s: %s",url.toString(),uuid.toString(),msg));
	}
	
	@Override
	public int read() throws IOException {
		if (is == null) {
			throw new IOException("InputStream unavailable");
		}
		
		return is.read();
	}
	
	public void close() throws IOException {
		if (is != null) {
			//logDebug("CLOSED");
			is.close();
			is = null;
		}
	}
	
	public void finalize() throws Throwable {
		if (is != null) {
			close();
			if (debug) {
				logDebug("AutoClosed");
				if (ctrace != null) {
					ctrace.printStackTrace();
				}
			}
		}
		super.finalize();
	}

	public InputStream getInputStream() {
		return is;
	}
}
