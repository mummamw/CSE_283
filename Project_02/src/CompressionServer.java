import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.io.*;   // for IOException and Input/OutputStream

public class CompressionServer {

  private static final int BUFSIZE = 1024;   // Size of receive buffer

  public static void main(String[] args) throws IOException {

    int servPort = Integer.parseInt(args[0]);

    // Create a server socket to accept client connection requests
    ServerSocket servSock = new ServerSocket(servPort);

    int recvMsgSize;   // Size of received message
    byte[] byteBuffer = new byte[BUFSIZE];  // Receive buffer

    for (;;) { // Run forever, accepting and servicing connections
      Socket clntSock = servSock.accept();     // Get client connection

      System.out.println("Handling client at " +
        clntSock.getInetAddress().getHostAddress() + " on port " +
             clntSock.getPort());

      InputStream in = clntSock.getInputStream();
      OutputStream out = clntSock.getOutputStream();

      // Receive until client closes connection, indicated by -1 return
      while ((recvMsgSize = in.read(byteBuffer)) != -1) {
        System.out.println("Received " + new String(byteBuffer));
        out.write(byteBuffer, 0, recvMsgSize);
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
      }

      clntSock.close();  // Close the socket.  We are done with this client!
    }
    /* NOT REACHED */
  }
 }




