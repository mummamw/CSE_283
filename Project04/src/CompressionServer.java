import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
//SSL section of imports
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


//realized finally that we really are just using our old stuff....
// CompressionRequest
final class CompressionRequest implements Runnable {
	
	//Declaration section
	final static int BUF_SIZE = 10240000;
	final static String TERM_STRING = "--------TERMINATE";
	SSLSocket socket;
	byte[] buffer;
	final static String CRLF = "\r\n";

	private int getContent(SSLSocket socket) throws Exception {
		int total = 0, rcv = 0;
		while (rcv != -1) {
			rcv = socket.getInputStream().read(buffer, total,
					BUF_SIZE - total - 1);
			if (rcv != -1) {
				total += rcv;
			}
		}
		// returns the total bytes in the buffer ..
		return total;
	}

	//SSL
	public CompressionRequest(SSLSocket socket) {
		this.socket = socket;
		buffer = new byte[BUF_SIZE];
	}
	
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void processRequest() throws Exception {
		try {
			int size = getContent(socket);
			ByteArrayInputStream is = new ByteArrayInputStream(buffer, 0, size);
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"US-ASCII"));
			String infoLine = br.readLine();
			System.out.println(infoLine);
			String[] infoLineArr = infoLine.split(" ");
			String fileName = infoLineArr[0];
			int fileSize = Integer.parseInt(infoLineArr[1]);
			System.out.println("Received file: " + fileName + "\nFile Size: " + fileSize);

			FileOutputStream fout = new FileOutputStream(fileName.trim());
			FileOutputStream foutForZip = new FileOutputStream(fileName.trim() + ".zip");
			ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(foutForZip));
			ZipEntry zip = new ZipEntry(fileName); 
			zout.putNextEntry(zip);

			// receive content bytes from socket
			fout.write(buffer, infoLine.length() + 1, fileSize);
			fout.flush();
			zout.write(buffer, infoLine.length() + 1, fileSize);
			// write bytes to the file

			zout.closeEntry();
			zout.close();
			fout.close();
			System.out.println("Finished method");
		} catch (Exception e) {
			throw e;
		}
	}
}


// CompressionServer class
public class CompressionServer {
	public static void main(String[] args) throws IOException {
			int port = Integer.parseInt(args[0]);
			String ksName = "keystore.jks";
			char ksPass[] = "password".toCharArray();
			char ctPass[] = "password".toCharArray();
			try {
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream(ksName), ksPass);
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, ctPass);
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(kmf.getKeyManagers(), null, null);
				SSLServerSocketFactory ssf = sc.getServerSocketFactory();
				SSLServerSocket s = (SSLServerSocket) ssf.createServerSocket(port);
				System.out.println("Started Compression SSL Server on port " + port);

			for(;;){  //Run Forever
				SSLSocket c = (SSLSocket) s.accept(); //getting packets
				System.out.println("Connection made");
				CompressionRequest request = new CompressionRequest(c);
				Thread t = new Thread(request);
				t.start();
			} //End of for loop
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
	



