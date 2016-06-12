import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	                
 
	public void Client() {}

	void run() throws NumberFormatException, ClassNotFoundException
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			InputStream input = null;
			OutputStream output = null;
			
			int no = Integer.parseInt((String)in.readObject());
			int listsize = Integer.parseInt((String)in.readObject());
			 
			 for(int i = no-1 ; i < listsize ; i = i+5)	{
				 File f1 = (File)in.readObject();
				 System.out.println("Received Chunk"+i);
				 
				 File f = new File("/Users/bishalgautam/Desktop/test/"+no+"/chunk"+String.format("%03d",i));
					try {
						
						 input = new FileInputStream(f1);
						 System.out.println("inside try block");
						 output = new FileOutputStream(f);
						byte[] buf = new byte[102400];
						int bytesRead=0;
						while ((bytesRead = input.read(buf)) > 0) {
							System.out.println("inside the while");
							output.write(buf, 0, bytesRead);}
					} finally {
						if(input != null)
						input.close();
						if(output != null)
						output.close();
					}
//					F.add(f);
//					list.add(I);
			 }
			
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
            		System.err.println("Class not found");
        	} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		finally{
			//Close connections
			try{
				if(in != null)
				in.close();
				if(out != null)
				out.close();
				if(requestSocket != null)
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	//main method
	public static void main(String args[]) throws NumberFormatException, ClassNotFoundException
	{
		Client client = new Client();
		client.run();
	}

}