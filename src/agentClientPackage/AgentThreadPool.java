package agentClientPackage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

public class AgentThreadPool {
	private static final int THREAD_POOL_SIZE = 5;
	private static final int QUEUE_CAPACITY = 10;
	
	private static ArrayBlockingQueue<Socket> connectionQueue;
	
	public static void main(String[] args) {
		ServerSocket listener;
		Socket connection;
		
		try {
			listener = new ServerSocket(8080);
			connectionQueue = new 
					ArrayBlockingQueue<Socket>(QUEUE_CAPACITY);
			for(int i =0; i <THREAD_POOL_SIZE; i++) {
				new ConnectionHandler(); //Create the thread; it starts itself.
			}
			
			System.out.println("Listening on port 8080");
			
			while(true) {
				//Accept next connection request and put it in the queue.
				connection = listener.accept();
				try {
					connectionQueue.put(connection);
					//Blocks if queue is full
				}
				catch(InterruptedException e) {
					
				}
			}
		}catch (Exception e) {
			System.out.println("Error: "+e);
			return;
		}
	}// end main()
	
	private static class ConnectionHandler extends Thread{
		ConnectionHandler(){
			start();
		}
		
		public void run() {
			while(true) {
				Socket agent;
				
				try {
					agent = connectionQueue.take();
				}
				catch(InterruptedException e) {
					continue; //(If, interrupted, just go back to start of while loop.)
				}
				
				String agentAddress = agent.getInetAddress().toString();
				try {
					AgentAuthentication login = new AgentAuthentication();
					
					//Sending or writing data from the socket
					System.out.println("Server sending to the agent");
					PrintWriter toAgent = new PrintWriter(agent.getOutputStream(), true);
					
					toAgent.println("Enter Username: ");
					
					BufferedReader input= 
							new BufferedReader(new InputStreamReader(agent.getInputStream()));
					String username = input.readLine();
					System.out.println("Server got " +username+ " from the agent");
					
					toAgent.println("Enter Password: ");
					
					String pw = input.readLine();
					System.out.println("Server got " +pw + " agent");
					
					login.check(username, pw);
					System.out.println("Connection from" +agentAddress);
					System.out.println("Handled by thread" +this);
					PrintWriter outgoing; //Stream for sending data
					outgoing = new PrintWriter(agent.getOutputStream());
					outgoing.println("Welcome agent!");
					outgoing.flush(); //Make sure the data is actually sent!
					agent.close();
				}
				catch (Exception e) {
					System.out.println("Error on connection with:"
							+ agentAddress+ ":" +e);
				}
			}
		}
	}//end inner class
}// end class