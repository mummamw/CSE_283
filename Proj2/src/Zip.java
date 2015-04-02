import java.io.*;
import java.util.zip.*;

public class Zip {
   static final int BUFFER = 2048;
   public static void main (String argv[]) {
      try {
         String fileInput = argv[0];
         String fileOutput = argv[1];
		 
         BufferedInputStream origin = null;
		 
		 // Create a file output stream
         FileOutputStream dest = new 
           FileOutputStream(fileOutput);
		   
         ZipOutputStream out = new ZipOutputStream(new 
           BufferedOutputStream(dest));
		   
         //out.setMethod(ZipOutputStream.DEFLATED);
         byte data[] = new byte[BUFFER];
		 
         // get a list of files from current directory
          FileInputStream fi = new 
            FileInputStream(fileInput);
			
          origin = new 
            BufferedInputStream(fi, BUFFER);
			
          ZipEntry entry = new ZipEntry("proj2.bin");
          out.putNextEntry(entry);
          int count;
          while((count = origin.read(data, 0, 
            BUFFER)) != -1) {
             out.write(data, 0, count);
          }
         origin.close();
         out.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
   }
} 
