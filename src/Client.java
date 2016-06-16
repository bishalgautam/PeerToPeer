import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;



public class Client implements Runnable {
	private Socket requestSocket; // socket connect to the server
	private ObjectOutputStream out; // stream write to the socket
	private ObjectInputStream in; // stream read from the socket
	protected ServerSocket serverSocket;
	private int serverPort;
	private List<File> file = new ArrayList<File>();
	private List<Integer> list = new ArrayList<Integer>();
	private Map<File, Integer> map = new HashMap<File, Integer>();
	protected int downloadNeighbour;
	protected int uploadNeighbour;
	protected int listsize;

	public Client(int serverPort) {
		this.serverPort = serverPort;

	}

	public void openPeerServerPort() {
		try {
			this.serverSocket = new ServerSocket(serverPort);
			System.out.println("Server started at " + serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Server not opened at :" + serverPort, e);
		}

	}

	public void connectDownloadNeighbour() throws InterruptedException {
		while (true) {
			try {
				System.out.println("Inside ConnectDownNeighbour");
				System.out.println("Attempting connection to download Neighbor");
				requestSocket = new Socket("localhost", downloadNeighbour);
				System.out.println("Connected to downloadNeighbor" + downloadNeighbour);
				break;
			} catch (Exception e) {
				Thread.sleep(2000);
			}
		}
	}

	public void run() {
		
		try {
		// get the server's chunk
		this.getChunkServer();

		this.openPeerServerPort();

		
  
//		try {
//			new UploadHandler(serverSocket.accept(), serverPort).start();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
//    		while(true) {
    			// accept the connection at user provided port no.
    			try {
					new UploadHandler(serverSocket.accept(),serverPort).start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        
        		/*
        		 * **** here is the tricky part when the UploadHandler is spawned
        		 * parallel to it the following code will also run
        		 * 
        		 */

        		try {
        			System.out.println("hey");
        			this.connectDownloadNeighbour();
        			out = new ObjectOutputStream(requestSocket.getOutputStream());
        			out.flush();
        			in = new ObjectInputStream(requestSocket.getInputStream());

        			System.out.println((String) in.readObject());

        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		} catch (ClassNotFoundException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        	}

//		}    		
   
	 finally {
		try {
			serverSocket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}		
		


	public void getChunkServer() {

		try {
			// create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			// initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			InputStream input = null;
			OutputStream output = null;

			uploadNeighbour = Integer.parseInt((String) in.readObject());
			downloadNeighbour = Integer.parseInt((String) in.readObject());

			int no = Integer.parseInt((String) in.readObject());
			listsize = Integer.parseInt((String) in.readObject());
			String name = (String) in.readObject();

			for (int i = no - 1; i < listsize; i = i + 5) {
				File f1 = (File) in.readObject();
				System.out.println("Received Chunk" + i);

				File f = new File(
						"/Users/bishalgautam/Desktop/test/" + no + "/" + name + "." + String.format("%03d", i + 1));
				try {

					input = new FileInputStream(f1);
					System.out.println("inside try block");
					output = new FileOutputStream(f);
					byte[] buf = new byte[102400];
					int bytesRead = 0;
					while ((bytesRead = input.read(buf)) > 0) {
						// System.out.println("inside the while");
						output.write(buf, 0, bytesRead);
					}
				} finally {
					if (input != null) {
						input.close();
					}
					if (output != null) {
						output.close();
					}
				}
				map.put(f, i);
				file.add(f);
				list.add(i);
			}

		} catch (ConnectException e) {
			System.err.println("Connection refused. You need to initiate a server first.");
		} catch (ClassNotFoundException e) {
			System.err.println("Class not found");
		} catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// Close connections
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if (requestSocket != null) {
					requestSocket.close();
					System.out.println("finally ended ");
				}
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}

	// send a message to the output stream
	void sendMessage(String msg) {
		try {
			// stream write the message
			out.writeObject(msg);
			out.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	// main method
	public static void main(String args[]) throws NumberFormatException, ClassNotFoundException {
		System.out.println("Please specify the port number for this client to listen :");
		Scanner in = new Scanner(System.in);
		int port = in.nextInt();
		in.close();

		Client client = new Client(port);
		new Thread(client).start();

	}

	/**
	 * Upload handler class that enables other peer to listen to this client and
	 * get the file list.
	 * 
	 */
	private class UploadHandler extends Thread {
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int peerID; // port no of client acting as server

		public UploadHandler(Socket connection, int no) {
			this.connection = connection;
			this.peerID = no;
		}

		public void run() {

			synchronized (this) {
				Thread.currentThread();
			}

			try {
				System.out.println("Handler called");
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try {
					sendMessage("Hi");

				} catch (Exception e) {
					throw new RuntimeException("fileList is missing", e);
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + peerID);
			} finally {
				// Close connections
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
					if (connection != null) {
						connection.close();
					}
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + peerID);
				}
			}
		}

		// send a message to the output stream
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				 System.out.println("Send message from server: " + msg + "to Client " + peerID);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		public void sendFile(File f) {
			try {
				out.writeObject(f);
				out.flush();
				// System.out.println("Send file " +fileList.indexOf(f)+ " to
				// Client " + peerID);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

}
