import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server implements Runnable{
    public static List<File> fileList = new ArrayList<File>();
	private int sPort ; //The server will be listening on this port number
	protected ServerSocket listener;
	private boolean stopped = false;
	protected static int[][] neighbours = new int[5][2]; 
	protected static int chunkCounter;
	protected static String name;  //name of the file
	
	private Server(int port)	{
		this.sPort = port;
	}
	
	private void openServerSocket() {
		try {
			this.listener = new ServerSocket(this.sPort);
		} catch (IOException e){
			throw new RuntimeException("server cannnot be initiated", e);
		}
	
	}	
	
	public synchronized void stop(){
        this.stopped = true;
        try {
            this.listener.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
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
	            		try {
							new Handler(listener.accept(),clientNum).start();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				System.out.println("Client "  + clientNum + " is connected!");
				clientNum++;
	        			}
	    	} finally {
	        		try {
						listener.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
		 
		 new Thread(server).start();	 
		 
		  // wait for the first chunks to be distributed to the clients
		 try {
//	            System.out.println("Sleeping...");
	            Thread.sleep(30*10000);
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
       // ServerSocket listener = new ServerSocket(8000);
		 
    	}
	
	public void createNeighbour() throws IOException{
		
		 File configFile = new File("config.txt");
		 BufferedReader in = new BufferedReader(new FileReader(configFile));

		 try{
		    String s;
		    while((s = in.readLine()) != null){
		    	   
		    		String[] nextLine = s.split(" ");
		            int clientPort = Integer.parseInt(nextLine[0]);
		            int clientPortUn = Integer.parseInt(nextLine[1]);
		            int clientPortDn = Integer.parseInt(nextLine[2]);
		            neighbours[clientPort - 9001][0] = clientPortUn;
		            neighbours[clientPort - 9001][1] = clientPortDn;
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
				in.close();
				}
				if(out != null){
				out.close();
				}
				if(connection != null){
				connection.close();
				}
			}
			catch(IOException ioException){
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

	
