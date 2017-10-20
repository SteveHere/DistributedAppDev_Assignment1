package serverPackage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RequestHandler extends Thread {
	
	Socket clientSocket;
	
	public RequestHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		String clientSocketAddress = clientSocket.getInetAddress().toString();
		
		try{
	    	//Set up readers and writers
			PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			//Send first request from server to client requesting details and logininfo
			toClient.println("Client request required");
			String[] clientResponse = fromClient.readLine().split("~");
			
			//The response must be in either 2 or 3 parts; 2 for customer, 3 for agent
			//Customer format: Customer~<username>
			if(clientResponse.length == 2 && clientResponse[0].equals("Customer")){
				//For the customer, first off, check if the username already exists
				//If it does, disconnect from the connection and print that the username's taken
				if(isCustomerUsernameTaken(clientResponse[1])){
					toClient.println("Username already taken");
				}
				//If not, add to the map of customer connections
				else{
					toClient.println("Connection established");
					Server.customerThreads.put(clientResponse[1], this);
					startCustomerConnection(toClient, fromClient);
				}
			}
			//Agent format: Agent~<username>~<password>
			else if(clientResponse.length == 3 && clientResponse[0].equals("Agent")){
				//For the agent, first off, check if the connection already exists
				//If it does, print that the agent's already logged in
				if(isAgentLoggedIn(clientResponse[1])){
					toClient.println("Agent has already logged in");
				}
				//If not, then try to log the agent in, if failed, send failed login attempt
				else if(logInAgent(clientResponse[1], clientResponse[2]) == false){
					toClient.println("Incorrect login details");
				}
				//If succeeded in logging in, add the user to the map of agent connections
				else{
					toClient.println("Login successful");
					Server.agentThreads.put(clientResponse[1], this);
					startAgentConnection(toClient, fromClient);
				}
			}
			//If its not neither, print out a format error message to the client
			else{
				toClient.println("Incorrect format from client response");
			}
			toClient.close();
			fromClient.close();
		}catch(IOException ioe){
			Server.logFile.println((System.currentTimeMillis() / 1000L) 
					+ ": IO Exception occured while connecting with "
					+ clientSocketAddress);
		}
	}
	
	private void startAgentConnection(PrintWriter toClient, BufferedReader fromClient) throws IOException {
		while(clientSocket.getInetAddress().isReachable(15)){
			
		}
	}

	private void startCustomerConnection(PrintWriter toClient, BufferedReader fromClient) throws IOException {
		while(clientSocket.getInetAddress().isReachable(15)){
			
		}
	}

	private boolean isCustomerUsernameTaken(String username) {
		return Server.customerThreads.containsKey(username);
	}

	private boolean isAgentLoggedIn(String username) {
		return Server.agentThreads.containsKey(username);
	}
	
	private boolean logInAgent(String username, String password) {
		return Server.agentsLoginInfo.get(username).equals(password);
	}
}
