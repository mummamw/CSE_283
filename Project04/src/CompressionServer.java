import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.io.*;

public class CompressionServer {
	
	private static final int ECHOMAX = 65535 ;  // Maximum size of echo datagram
	public static void main(String[] args) throws IOException {

		int servPort = Integer.parseInt(args[0]);
		DatagramSocket socket = new DatagramSocket(servPort);
	
		DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
		
		for (;;) {  // Run forever, receiving and echoing datagrams
			socket.receive(packet);     // Receive packet from client       
			byte[] data = packet.getData(); //gives you a byte array of sent info
			String fileName = new String(data, 0, packet.getLength());
			System.out.println("Received file: " + fileName);
			FileOutputStream fout = new FileOutputStream(fileName.trim());
			FileOutputStream foutForZip = new FileOutputStream(fileName.trim() + ".zip");
			ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(foutForZip)); 
						
			//creates file output stream using the inputted filename
			
			ZipEntry zip = new ZipEntry(fileName); //again how to get filename?
			zout.putNextEntry(zip);
			int count = 1;
			while(true){
				socket.receive(packet);
				//receive content bytes from socket
				data = packet.getData();
				if (new String(data, 0, packet.getLength()).equals("TERMINATE")) {
					System.out.println("Found end of file.");
					break;
				}
				fout.write(data, 0, packet.getLength()); 
				fout.flush(); 
				zout.write(data, 0, packet.getLength());
				//write bytes to the file 
				System.out.println("Wrote packet #" + count);
				count++;
			}
			zout.closeEntry();
			zout.close();
			fout.close(); 
			System.out.println("Finished method");
		}
		/* NOT REACHED */
	}
}
