import java.io.*;
import java.net.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;

import java.security.cert.X509Certificate;

final class HttpRequest implements Runnable {
	final static int BUF_SIZE = 1024000;
	final static String CRLF = "\r\n";

	byte[] buffer;
	Socket socket;

	// Constructor
	public HttpRequest(Socket socket) throws Exception {
		this.socket = socket;
		buffer = new byte[BUF_SIZE];
}

	// Implement the run() method of the Runnable interface.
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			//System.out.println(e);
			e.printStackTrace();
		}
	}

	private int getContent() throws Exception {
		int total = 0, rcv = 0;
		
		while (rcv != -1) {
			rcv = socket.getInputStream().read(buffer, total,
				BUF_SIZE - total - 1);
			String msg = new String(buffer, total, rcv);
			System.out.println(msg);
			total += rcv;
			
			if (msg.startsWith("GET") || msg.indexOf("Upload"+CRLF) != -1) {
				System.out.println("EXITING");
				break;
			}
		}
		// returns the total bytes in the buffer
		return total;
	}

	//Compresion send no longer here, in BackupCode

	private void processRequest() throws Exception {

		int total = getContent();

		// Get a reference to the socket's input and output streams.
		// InputStream is = socket.getInputStream();
		InputStream is = new ByteArrayInputStream(buffer, 0, total);
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());

		// Set up input stream filters.
		BufferedReader br = new BufferedReader(new InputStreamReader(is,"US-ASCII"));
		
		int byteCount = 0;

		// Get the request line of the HTTP request message.
		String requestLine = br.readLine();
		byteCount += requestLine.length() + 2;       //Difference from main code
		// Extract the filename from the request line.
		StringTokenizer tokens = new StringTokenizer(requestLine);
		String method = tokens.nextToken(); // skip over the method, which
											// should be "GET"
		String fileName = tokens.nextToken();

//Post Request area============================================================

		if (method.equals("POST")) {

            String line = "", delim = "";
            int dataLength = 0;

			while((line = br.readLine()) != null){
				byteCount += line.length() + 2;
				if (line.indexOf("Content-Length:") != -1) {
					String contentLenStr = line.substring("Content-Length: "
							.length());
					dataLength = Integer.parseInt(contentLenStr);
				}
				// finds delim
				else if (line.indexOf("----WebKitForm") != -1) {
					int indexOfDelim = line.indexOf("----WebKitForm");
					delim = "--" + line.substring(indexOfDelim);
				}
				// stops going through headers
				else if (line.equals("")) {
					line = br.readLine();
					byteCount += line.length() + 2;
					System.out.println("Got to start of file, current line:\n"
							+ line);
					break;
				}
			}

			is =  new ByteArrayInputStream(buffer, byteCount, dataLength);
			br = new BufferedReader(new InputStreamReader(is, "US-ASCII"));

			StringBuilder postString = new StringBuilder();
			int bodyStart = 0;
			int numLines = 0;
			boolean actualBody = false;
			boolean endBody = false;

			while ((line = br.readLine()) != null) {
				if(line.indexOf("Content-Type") != -1) {
					actualBody = true;
					bodyStart += line.length() + 4;
					line = br.readLine();           
				} else if(line.indexOf(delim) != -1) {
					endBody = true;
				}
				if(actualBody){
					postString.append(line);
					if(!endBody) {
						numLines++;
					}
				} else {
					bodyStart += line.length() + 1;
				}
			}

			String[] postData = postString.toString().split(delim);
			String postFileContents = postData[0];
			System.out.println(postFileContents);
			System.out.println(numLines + " " + delim + " " + dataLength);
			int postFileContentLength = postFileContents.length();
			String postFileName = postData[1].substring(postData[1].indexOf("destination\"") + "destination\"".length());
			
			byte[] postDataBytes = Arrays.copyOfRange(buffer,
					byteCount + bodyStart + 1,
					byteCount + bodyStart  + 1 + postFileContentLength + numLines);
			
			sendPostToCompression(postFileName, postDataBytes);
		}
		


//===========================Get Request area==================================
		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;

		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}

		// Construct the response message.
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;
		} else if (method.equals("POST")) {
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-Type: text/html" + CRLF;
			entityBody = "<HTML>"
					+ "<HEAD><TITLE>File Uploaded Successful</TITLE></HEAD>"
					+ "<BODY>File Upload Successful</BODY></HTML>";
		} else {
			statusLine = "HTTP/1.0 404 Not Found" + CRLF;
			contentTypeLine = "Content-Type: text/html" + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>"
					+ "<BODY>Not Found</BODY></HTML>";
		}
		// Send the status line.
		os.writeBytes(statusLine);

		// Send the content type line.
		os.writeBytes(contentTypeLine);

		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);

		// Send the entity body.
		if (fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}

		// Close streams and socket.
		os.close();
		br.close();
		socket.close();
	}

	private void sendPostToCompression(String fileName, byte[] data)
			throws Exception {
		int port = 5000;
		InetAddress address = InetAddress.getLocalHost();
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			SSLSocketFactory f = (SSLSocketFactory) sc.getSocketFactory();
			SSLSocket c = (SSLSocket) f.createSocket(address, port);
			c.startHandshake();
			OutputStream ow = c.getOutputStream();
			String header = fileName + " " + data.length + "\n";
			ow.write(header.getBytes(), 0, header.length());
			ow.write(data, 0, data.length);
			ow.flush();
			ow.close();
			
			c.close();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}	

	private static void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception {
		// Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;

		// Copy requested file into the socket's output stream.
		while ((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}

	private static String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if (fileName.endsWith(".png")) {
			return "image/png";
		}
		if (fileName.endsWith(".pdf")) {
			return "application/pdf";
		}
		if (fileName.endsWith(".zip")) {
			return "application/zip";
		}
		if (fileName.endsWith(".jpeg")  || fileName.endsWith(".jpg")) {
			return "image/jpeg";
		}
		return "application/octet-stream";
	}
}

public final class WebServer {
	public static void main(String argv[]) throws Exception {
		//6789 for project
		int port = Integer.parseInt(argv[0]);

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

			while (true) {
				// Listen for a TCP connection request.
				SSLSocket c = (SSLSocket) s.accept();

				// Construct an object to process the HTTP request message.
				HttpRequest request = new HttpRequest(c);

				// Create a new thread to process the request.
				Thread thread = new Thread(request);

				// Start the thread.
				thread.start();
			}

			//BufferedWriter w = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
			//BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()));
		} catch (Exception e) {
			System.out.println(e);
		}
			// Process HTTP service requests in an infinite loop.
	}
}