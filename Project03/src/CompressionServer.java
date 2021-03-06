import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.*;   // for IOException and Input/OutputStream


public class CompressionServer {

	 private static final int ECHOMAX = 65535;   

	 public static void main(String[] args) throws IOException {

	    int servPort = Integer.parseInt(args[0]);
	    DatagramSocket socket = new DatagramSocket(servPort);   // Create a server socket to accept client connection requests
	    DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
	    
	    
	    int recvMsgSize;   // Size of received message
	    byte[] byteBuffer = new byte[ECHOMAX];  // Receive buffer

	    for (;;) { // Run forever, accepting and servicing connections
	    	socket.receive(packet);
	    	byte[] data = packet.getData();
	    	String filename = new String(data, 0, packet.getLength());
	    	System.out.println("Recieved: " + filename);

	    	FileOutputStream fout = new FileOutputStream(filename.trim());
	    	//Zipping 
	    	FileOutputStream foutZip = new FileOutputStream(filename.trim() + ".zip");
	    	ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(foutZip));
            			   
			ZipEntry zip = new ZipEntry(filename);    // Gotten from first packet sent
	    	zout.putNextEntry(zip);

	    	int count = 1;
	    	//getting rid of try method because exceeptions kept catching?
	    	for(;;) {
	    	  socket.receive(packet);
              //Getting content
              data = packet.getData();
              if(new String(data, 0, packet.getLength()).equals("KILL_ME")) {
              	System.out.println("End of file was found from magic string");
              	break;
              }    //End of if loop not for loop.
              fout.write(data, 0, packet.getLength());
              fout.flush();
              zout.write(data, 0, packet.getLength());
              count++;
	          }
	         zout.closeEntry(); 
	         zout.close();
	         fout.close();
	     }
	           
	     
	    }
	    /* NOT REACHED */
 }




