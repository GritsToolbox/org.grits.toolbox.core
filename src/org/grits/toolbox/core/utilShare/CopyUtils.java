package org.grits.toolbox.core.utilShare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * General copy file 
 * @author kitaemyoung
 *
 */
public class CopyUtils {
	public static void copyFilesFromTo(String from, String to) throws IOException {
		File input = new File(from);
		File output = new File(to);
		InputStream in = new FileInputStream(input);
		OutputStream out = new FileOutputStream(output);
		byte[] buf = new byte[1000000];
        int len;
        while ((len = in.read(buf)) > 0){
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
	}
	
	public static void copyFilesFromTo(String from, String to, IProgressMonitor monitor ) throws IOException {
		File input = new File(from);
		File output = new File(to);
		InputStream in = new FileInputStream(input);
		OutputStream out = new FileOutputStream(output);
		byte[] buf = new byte[1000000];
        int len;
        while ( ((len = in.read(buf)) > 0) && (monitor == null || ! monitor.isCanceled())){
          out.write(buf, 0, len);
        }
        in.close();
        out.close();
	}
}
