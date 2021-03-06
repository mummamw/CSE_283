import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;   // for IOException and Input/OutputStream


public class CompressionServer {

	 private static final int BUFSIZE = 1024;   // was only half of what was needed for zip?

	  public static void main(String[] args) throws IOException {

	    int servPort = Integer.parseInt(args[0]);

	    // Create a server socket to accept client connection requests
	    ServerSocket servSock = new ServerSocket(servPort);

	    int recvMsgSize;   // Size of received message
	    byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer

	    for (;;) { // Run forever, accepting and servicing connections
	    	
	    	Socket clntSock = servSock.accept();
	    	
	    	InputStream in = clntSock.getInputStream();
	    	
	    	OutputStream out = clntSock.getOutputStream();
	    	
	    	 BufferedInputStream origin = null;
			 
			 // Create a file output stream
	         //FileOutputStream dest = new FileOutputStream(fileOutput);
			   
	         ZipOutputStream ZipOut = new ZipOutputStream(new BufferedOutputStream(out));
			   
	         //out.setMethod(ZipOutputStream.DEFLATED);
	         byte data[] = new byte[2 * BUFSIZE];
			 
	         // get a list of files from current directory
	         // FileInputStream fi = new FileInputStream(fileInput);
			  
	         try{
	         
	          origin = new BufferedInputStream(in, 2 * BUFSIZE);
				
	          ZipEntry entry = new ZipEntry("proj2.bin");
	          
	          ZipOut.putNextEntry(entry);
	          
	          int count;
	          
	          
	          while((count = origin.read(data, 0, 2 * BUFSIZE)) != -1) {
	             ZipOut.write(data, 0, count);
	             ZipOut.flush();
	          }
	          
	         out.close();
	         } catch(FileNotFoundException e) {
	        	 System.out.println("No file");
	         } catch (IOException e) {
	             System.out.println("Wrong IO Error");
	         }
	           
	      ZipOut.close();  // Close the socket.  We are done with this client!
	    }
	    /* NOT REACHED */
	  }
 }




