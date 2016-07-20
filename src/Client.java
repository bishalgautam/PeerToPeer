import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;



public class Client implements Runnable {
	private Socket requestSocket; // socket connect to the server
	private ObjectOutputStream out; // stream write to the socket
	private ObjectInputStream in; // stream read from the socket
	
	private int serverPort;
	private List<File> file = new ArrayList<File>();
	private List<Integer> list = new ArrayList<Integer>();
	private Map< Integer, File> map = new HashMap<Integer,File>();
	protected int downloadNeighbour;
	protected int uploadNeighbour; 
	protected int listsize;
	protected String name;
	protected boolean isStopped = false; 
	protected Thread t;
	protected boolean downloadcomplete = false;
	protected boolean uploadcomplete = false;
	
	
	public Client(int serverPort) {
		this.serverPort = serverPort;

	}

//	public void openPeerServerPort() {
//		try {
//			this.serverSocket = new ServerSocket(serverPort);
////			this.serverSocket.setSoTimeout(4000);
//			System.out.println("Server started at " + serverPort);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			throw new RuntimeException("Server not opened at :" + serverPort, e);
//		}
//
//	}

	public void connectDownloadNeighbour() throws InterruptedException {
		while (true) {
			try {
				System.out.println("Inside ConnectDownNeighbour");
				System.out.println("Attempting connection to download Neighbor");
				requestSocket = new Socket("localhost", downloadNeighbour);
				System.out.println("Connected to downloadNeighbor" + downloadNeighbour);
				break;
			} catch (Exception e) {
				Thread.sleep(1000);
			}
		}
	}
	

	public void run() {
		

		this.getChunkServer();

//		this.openPeerServerPort();

		
		
  
//		try {
//			new UploadHandler(serverSocket.accept(), serverPort).start();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	while( !this.downloadcomplete || !this.uploadcomplete ){
		
//	}          
	  try {
		  if(!this.uploadcomplete){
		      Thread.sleep(10000-serverPort);
//		  	while(true) {
    			// accept the connection at user provided port no.
//		  			try {
		  			System.out.println("Intializing the uploadhander");
		  				
		  			t =	new Thread (new UploadHandler(serverPort));
		  			t.start();
		  				
//		  			} catch (SocketTimeoutException  e ) {
//						// TODO Auto-generated catch block
//						System.out.println("Timed out after 20sec");
//		  			} catch (IOException e) {
//					// TODO Auto-generated catch block
//		  				e.printStackTrace();
//				}
    		}
//	    	   if(this.downloadcomplete){
//	    		   t.join();
//	    		   continue;
//    	   }
		  	
	      try {
	    	  
	    	  if(!this.downloadcomplete){
	    		  
//	    	  }
       			System.out.println("second try block client's thread");
       			this.connectDownloadNeighbour();
       			out = new ObjectOutputStream(requestSocket.getOutputStream());
       			out.flush();
       			in = new ObjectInputStream(requestSocket.getInputStream());
       			
 //      			while(!isStopped){
       			// get the size of the list of chunks  download neighbor has 
      // 			if (in.available() > 0){
       				// prevent the EOF 
       			int size =  Integer.parseInt((String) in.readObject());
       			
       			List<Integer> dnList = new ArrayList<Integer>();
       			//creating the list as of the download neighbor 
       			
       			for( int i =0 ; i < size ; i++){
       				dnList.add(Integer.parseInt((String) in.readObject()));
       			}
       			
       			List<Integer> reqList = new ArrayList<Integer>();
       			
       			for( int j : dnList ){
       				if(map.get(j) == null){
       					 reqList.add(j);
       				}
       			}
       			
       			sendMessage(""+reqList.size());
       			sendList(reqList);
       			
//       			if(reqList.size() != 0){
//       				sendList(reqList);	
//       			}
       			
       			
       			
       			// Receive the chunks from download neighbor according to the reqList 
       			
       			for(int i=0; i<reqList.size();i++){
					
       				File f1 = (File)in.readObject();

					File f = new File("/Users/bishalgautam/Desktop/test/"+ (serverPort-8000) + "/" + name + "." + String.format("%03d", reqList.get(i)));
					System.out.println(reqList.get(i)+"th chunk received");
					InputStream input = null;
					OutputStream output = null;
					try {
						input = new FileInputStream(f1);
						output = new FileOutputStream(f); 
						byte[] buf = new byte[102400];
						int bytesRead;
						while ((bytesRead = input.read(buf)) > 0) {
							output.write(buf, 0, bytesRead);
						}
					} finally {
						if(input != null){
							input.close();
						}
						if(output != null){
							output.close();
						}
						
					}
					file.add(f);
//					System.out.println(file);
					list.add(reqList.get(i));
					map.put(reqList.get(i), f);
				//	System.out.println("list of the files received "+ file);
					System.out.println("list of the chunks received "+ list);
				}
//       			break;
       			sendMessage(""+list.size());
       			
       			if(list.size() == listsize){
       					this.stop();
       			}
//       			 break;
//       			System.out.println((String) in.readObject());
//       			System.out.println((String) in.readObject());
//       			}
       	}
       		}catch (EOFException e) {
				   System.out.println("EOF is reached ");
       		}catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (ClassNotFoundException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}


   		
	  } catch (InterruptedException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	finally {
		try {
			
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if ( in != null ){
				in.close();
			}
			if ( out != null ){
				out.close();
			}
			if ( requestSocket != null ){
				requestSocket.close(); 
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	  
}	  // end while loop 
	  
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
			 name = (String) in.readObject();

			for (int i = no - 1; i < listsize; i = i + 5) {
				File f1 = (File) in.readObject();
				System.out.println("Received Chunk" + (i+1));

				File f = new File(
						"/Users/bishalgautam/Desktop/test/" + no + "/" + name + "." + String.format("%03d", i + 1));
				try {

					input = new FileInputStream(f1);
					//System.out.println("inside try block");
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
				map.put(i+1,f);
				System.out.println("map :"+ map.get(i+1));
				file.add(f);
				list.add(i+1);
				//System.out.println("list of the files received "+ file);
				System.out.println("list of the chunks received "+ list);
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
					System.out.println("finally getchunk from server ended ");
				}
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

	}
	// stopping the download once all the chunks are downloaded
	
	public  synchronized void stop(){
		this.isStopped = true;
		this.downloadcomplete = true;
		
		System.out.println("Download complete Starting to Merge the Files .......");
		try{
			this.MergeChunks();
		}catch (IOException e){
			e.printStackTrace();		
		}
		
		
	}
	

	//To send ArrayList to the outputStream 
		public void sendList(List<Integer> list){
				for(int j = 0; j < list.size(); j++){
					sendMessage(""+list.get(j));
				}
			}

	// send a message to the output stream
	public void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			 System.out.println("Send message from client @ "+ serverPort + ":" +  msg +  "to server @ "+ downloadNeighbour );
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}
	// send a file to the output stream
	public void sendFile(File f) {
		try {
			out.writeObject(f);
			out.flush();
			// System.out.println("Send file " +fileList.indexOf(f)+ " to
			 System.out.println("Send file from client @ "+ serverPort + ":" +  f +  "to server @ "+ downloadNeighbour );
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		

	}
	
	// merge the chunks to create a new file
			public void MergeChunks() throws IOException{
				
				File f2 = new File("/Users/bishalgautam/Desktop/test/"+ (serverPort-8000) + "/" + name );
				try{
					FileOutputStream out = new FileOutputStream(f2,true);
					FileInputStream in = null;
					
					for(int i=0; i< listsize; i++){	
						int index = list.indexOf(i+1);
						in = new FileInputStream(file.get(index));
						byte[] B = new byte[(int)file.get(index).length()];
						int b = in.read(B, 0,(int)file.get(index).length());
						out.write(B);
		                out.flush();
						in.close();
					}
					out.close();
				}catch(Exception e){
					
				}
			}
			
			
	// main method
	public static void main(String args[]) throws NumberFormatException, ClassNotFoundException {
		System.out.println("Please specify the port number for this client to listen :");
		Scanner in = new Scanner(System.in);
		int port = in.nextInt();
		in.close();

		Client client = new Client(port);
		Thread t = new Thread(client, "client0000");
		t.start();
		
				
				
		
		try {
//            System.out.println("Sleeping...");
            Thread.sleep(6*10000); //60 sec to  transfer data 
         //   t.join();
//            System.out.println("Done sleeping, no interrupt.");
        } catch (InterruptedException e) {
//            System.out.println("I was interrupted!");
            e.printStackTrace();
        }

	}

	/**
	 * Upload handler class that enables other peer to listen to this client and
	 * get the file list.
	 * 
	 */
	private class UploadHandler implements Runnable {
		private ServerSocket serverSocket;  
		private Socket connection;
		private ObjectInputStream in; // stream read from the socket
		private ObjectOutputStream out; // stream write to the socket
		private int peerID; // port no of client acting as server

		public UploadHandler(int no) {
//			this.connection = connection;
			this.peerID = no;
		}
		
		public void openPeerServerPort() {
			try {
				this.serverSocket = new ServerSocket(peerID);
				this.serverSocket.setSoTimeout(4000);
				System.out.println("Server started at " + peerID);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException("Server not opened at :" + peerID, e);
			}

		}
		
		
		public void run() {

			synchronized (this) {
				Thread.currentThread();
			}
			
			this.openPeerServerPort();
			
			try {
				this.connection = serverSocket.accept();
				
			} catch (SocketTimeoutException  e ) {
				// TODO Auto-generated catch block
				System.out.println("Timed out after 20sec");
			}catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				System.out.println("Handler called inside"+ this.peerID);
				
				// initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				
				
			try {
//				while(true){
					
					
					sendMessage(""+list.size());
					sendList(list); 
					
					int reqlsize = Integer.parseInt((String) in.readObject());
					List<Integer> reqList = new ArrayList<Integer>(); 
					
					for( int i= 0; i < reqlsize ; i ++){
						reqList.add(Integer.parseInt((String) in.readObject()));
					}
				
//					if(reqList.size() != 0){
//						
					System.out.println("size : "+reqList.size());
					
					for(int j : reqList){
							sendFile(map.get(j));
						}
//					}
//					
					int clistsize = Integer.parseInt((String) in.readObject());
					
					if(reqList.isEmpty() && clistsize == listsize){
						uploadcomplete = true;
					System.out.println("uploadcomplete:"+ uploadcomplete);	
					
					}	
					reqList.clear();			
//				   }
				} catch (EOFException e) {
				   System.out.println("EOF is reached  at uploadhandler");
				}	catch (Exception e) {
					throw new RuntimeException("fileList is missing", e);
				}
			} catch (IOException ioException) {
				System.out.println("Disconnect with Client " + uploadNeighbour);
			} finally {
				// Close connections
				try {
					
//					try {
////			            System.out.println("Sleeping...");
//			            Thread.sleep(6*1000); //6 sec to  transfer data 
////			            System.out.println("Done sleeping, no interrupt.");
//			        } catch (InterruptedException e) {
////			            System.out.println("I was interrupted!");
//			            e.printStackTrace();
//			        }
							
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
					if (connection != null) {
						connection.close();
					}
					if(serverSocket != null){
						serverSocket.close();
					}
						
				} catch (IOException ioException) {
					System.out.println("Disconnect with Client " + peerID);
				}
			}
			
		}
		
		//To send ArrayList to the outputStream 
			public void sendList(List<Integer> list){
					for(int j = 0; j < list.size(); j++){
						sendMessage(""+list.get(j));
					}
				}

		// send a message to the output stream
		public void sendMessage(String msg) {
			try {
				out.writeObject(msg);
				out.flush();
				 System.out.println("Send message from server @ "+ peerID + " : " + msg + "to Client " + uploadNeighbour);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
		// send a file to the output stream
		public void sendFile(File f) {
			try {
				out.writeObject(f);
				out.flush();
				 System.out.println("Send file from server @ "+ peerID + " : " + f + " to Client " + uploadNeighbour );
				// Client " + peerID);
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

}
