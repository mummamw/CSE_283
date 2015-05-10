import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class CompressionServer2 {
	
	private static final int ECHOMAX = 65535 ;  // Maximum size of echo datagram
	public static void main(String[] args) throws IOException {

		String ksName = "keystore.jks";
		char ksPass[] = "password".toCharArray();
		char ctPass[] = "password".toCharArray();
		try { //Don't Try just do. 
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName), ksPass);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ctPass);
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(kmf.getKeyManagers(), null, null);


		//Server Socket Created5
			SSLServerSocketFactory ssf = sc.getServerSocketFactory();
			int servPort = Integer.parseInt(args[0]);
			SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(servPort);
		//Socket created from the ServerSocket
			SSLSocket c = (SSLSocket) s.accept();
		//Creating Buffered reader and writer as input streams
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
		BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));

		//moved above into clreating SSLSocket section
		//int servPort = Integer.parseInt(args[0]);
		//Socket is already created as s currently
		//DatagramSocket socket = new DatagramSocket(servPort);
		
		//No longer recieving packets, It is just a stream now.
		//DatagramPacket packet = new DatagramPacket(new byte[ECHOMAX], ECHOMAX);
		//test

		for (;;) {  // Run forever, receiving and echoing datagrams
			//socket.receive(packet);     // Receive packet from client       
			//byte[] data = packet.getData(); //gives you a byte array of sent info
			//String fileName = new String(data, 0, packet.getLength());
			String fileName = new String(r.readLine());
			System.out.println("Received file: " + fileName);



			FileOutputStream fout = new FileOutputStream(fileName.trim());
			FileOutputStream foutForZip = new FileOutputStream(fileName.trim() + ".zip");
			ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(foutForZip)); 
						
			//creates file output stream using the inputed filename
		
			ZipEntry zip = new ZipEntry(fileName); 
			zout.putNextEntry(zip);
			//int count = 1;
			
			String m = "";
			//while((m=r.readLine())!= null){
			while(true) {
				//socket.receive(packet);
				//receive content bytes from socket
				//w.write(m,0,m.length());
				//data = packet.getData();
				
				
				//if (m.equals("TERMINATE")) {
				if (m.indexOf("TERMINATE") > 0){
					System.out.println("Found end of file.");
					break;
				}
				byte[] m2 = m.getBytes();
				fout.write(m2, 0,m2.length); 
				fout.flush(); 
				//zout.writeBytes(m2, 0, m2.length);
				//write bytes to the file 
				//System.out.println("Wrote packet #" + count);
				//count++;
			}
			zout.closeEntry();
			zout.close();
			fout.close(); 
			w.close();
			r.close();
			c.close();
			s.close();
			System.out.println("Finished method");
		}
		/* NOT REACHED */
	} catch(Exception e){
		System.err.println(e.toString());
	}
	}
}


