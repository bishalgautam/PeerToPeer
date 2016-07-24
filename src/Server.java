import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server implements Runnable{
    public static List<File> fileList = new ArrayList<File>();
	private int sPort ; //The server will be listening on this port number
	protected ServerSocket listener;
	
	protected boolean stopped = false;
	protected static int[][] neighbours = new int[5][2]; 
	protected static int chunkCounter;
	protected static String name;  //name of the file
	//protected static final int SO_TIMEOUT = 23; 
	protected Thread runningThread ;
	
	private Server(int port)	{
		this.sPort = port;
		this.runningThread= Thread.currentThread();
	}
	
	private void openServerSocket() {
		try {
			this.listener = new ServerSocket(this.sPort);
			this.listener.setSoTimeout(20000);
		} catch (IOException e){
			throw new RuntimeException("server cannnot be initiated", e);
		}
	
	}	
	
	public synchronized void stop() {
        this.stopped = true;

	}  
	
	
	public void run(){
			// TODO Auto-generated method stub
		openServerSocket();
		
		try {
			createNeighbour();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
			     int clientNum = 1;
			     
	    	try {
	        		while(!stopped) {
	        			System.out.println("stopped vale:" + stopped);
	            		try {
							new Handler(listener.accept(),clientNum).start();
							 System.out.println("Client "  + clientNum + " is connected!");
							 clientNum++;
						} catch (SocketTimeoutException  e ) {
							// TODO Auto-generated catch block
							System.out.println("Timed out after 20sec");
						} catch (IOException e1){
							e1.printStackTrace();
						}

	        			}
	    	} finally {
	    		try {
		        	if(this.listener != null && !this.listener.isClosed()){
		                this.listener.close();
		                }
		            } catch (IOException e ) {
		            	e.printStackTrace(System.err);
		               // throw new RuntimeException("Error closing server", e);
		            }
	    		System.out.println("Server is closed");	
					// listener.close();
	    	}
		}	
	
	public static void main(String[] args) throws Exception {
		
		Server server = new Server(8000);
//		server.openServerSocket();
        
        
		System.out.println("The server is running."); 
		/*
		 * 
		 */
//		server.createNeighbour();
		

		System.out.println("Please enter the file from Desktop/test");
	    // split the files into chunks of 100KB
		Scanner readfile = new Scanner(System.in);
		String fileName = readfile.nextLine();
	 
		 File file = new File ("/Users/bishalgautam/Desktop/test/"+ fileName);
		 splitFile(file );
		 readfile.close();
		 
		  Thread t = new Thread(server, "serverthread");
				  t.start();
				  
		 
		  // wait for the first chunks to be distributed to the clients
		 try {
//	            System.out.println("Sleeping...");
			 
	            Thread.sleep(9*10000); //90 sec to type and initiate 5 clients
//	            System.out.println("Done sleeping, no interrupt.");
	        } catch (InterruptedException e) {
//	            System.out.println("I was interrupted!");
	            e.printStackTrace();
	        }
		  
		 //close the scanner  
		 if (readfile != null ){
			  readfile.close();
		 }
		
		System.out.println("server is stopping");
	     server.stop();
  
	 	
    	}
	
	public void createNeighbour() throws IOException{
		
		 File configFile = new File("config.txt");
	   //  File configFile = new File("src/config.txt"); // when using eclipse
		 BufferedReader in = new BufferedReader(new FileReader(configFile));

		 try{
		    String s;
		    while((s = in.readLine()) != null){
		    	   
		    		String[] nextLine = s.split(" ");
		            int clientPort = Integer.parseInt(nextLine[0]);
		            int clientPortUn = Integer.parseInt(nextLine[1]);
		            int clientPortDn = Integer.parseInt(nextLine[2]);
		            neighbours[clientPort - 8001][0] = clientPortUn;
		            neighbours[clientPort - 8001][1] = clientPortDn;
		    }

		}catch(Exception e){
		    e.printStackTrace();
		}finally{
			if(in != null){
			  in.close();
			}
		}
		
	}
	
	/*
	 * split the file into the chunk of 100KB;
	 */
	public static void splitFile(File f) throws IOException {
        chunkCounter = 1; 

        int sizeOfFiles = 100*1024;// 100KB
        byte[] buffer = new byte[sizeOfFiles];

        try (BufferedInputStream bis = new BufferedInputStream(
                new FileInputStream(f))) {//try-with-resources to ensure closing stream
               name = f.getName();

            int splitsize = 0;
            while ((splitsize = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                File newFile = new File(f.getParent(), name + "."
                        + String.format("%03d", chunkCounter++));
                fileList.add(newFile);
                
                System.out.println("Files Created"+(chunkCounter-1)+":"+newFile.length());
                
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, splitsize);//tmp is chunk size
                }
            }
            chunkCounter--;
            System.out.println("no of total chunks: "+chunkCounter);
        }
        
        
        
    }


	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    private static class Handler extends Thread {
		private Socket connection;
        private ObjectInputStream in;	//stream read from the socket
        private ObjectOutputStream out;  //stream write to the socket
		private int no;		            //The index number of the client
		
		public Handler(Socket connection, int no) {
                this.connection = connection;
	    		this.no = no;
        	}

        public void run() {
        	
       	synchronized(this){
       		Thread.currentThread();
        		
        	}
 		
       	try{
 			
			//initialize Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			try{
     			    sendMessage(""+neighbours[no-1][0]);// sending the uploadneighbour 
     			    
     			    sendMessage(""+neighbours[no-1][1]);//sending the downloadneighbour
				    
					sendMessage(""+no);
					sendMessage(""+fileList.size());
					sendMessage(name); // sending the input file name
					
					for(int i = no-1; i < fileList.size(); i=i+5){
						sendFile(fileList.get(i));
						//sendMessage(""+i);
				}
			}
			catch(Exception e){
				throw new RuntimeException("fileList is missing", e);
				}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			
			try{
				if(in != null){
					System.out.println("closing the in input stream");
				in.close();
				}
				if(out != null){
					System.out.println("closing the out output stream");
				out.close();
				}
				if(connection != null){
					System.out.println("closing the TCP sockets opened for clients");
				connection.close();
				}
				
			}
			catch(IOException ioException){
				System.out.println("Exception closing connection " + ioException.getMessage());
				System.out.println("Disconnect with Client " + no);
			}
		}
	}

	//send a message to the output stream
	public void  sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("Send message from server: " + msg + " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	public void sendFile(File f)
	{
		try{
			out.writeObject(f);
			out.flush();
			System.out.println("Send file  from server :" +fileList.indexOf(f)+ " to Client " + no);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
  }
}

	
