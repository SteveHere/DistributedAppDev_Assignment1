package server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;

import common.Pair;
import sun.rmi.log.ReliableLog.LogFile;

public class RequestHandler extends Thread {
	
	Socket clientSocket;
	//These members are generated
	private PrintWriter toClient;
	private BufferedReader fromClient;
	private String username;
	
	//This is only used by customers
	private PrintWriter transcript;
	
	public RequestHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	@Override
	public void run() {
		String clientSocketAddress = clientSocket.getInetAddress().toString();
		
		try{
	    	//Set up readers and writers
			toClient = new PrintWriter(clientSocket.getOutputStream(), true);
			fromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			Server.logFile.println((System.currentTimeMillis() / 1000L) 
					+ " Client from " + clientSocketAddress + " connected");
			Server.logFile.flush();
			
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
					username = clientResponse[1];
					startCustomerConnection();
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
					Server.agentToCustomer.put(clientResponse[1], new Pair<String, String>(null, null));
					username = clientResponse[1];
					startAgentConnection();
				}
			}
			//If its not neither, print out a format error message to the client
			else{
				toClient.println("Incorrect format from client response");
			}
			toClient.close();
			fromClient.close();
			clientSocket.close();
		} catch (IOException ioe) {
			Server.logFile.println((System.currentTimeMillis() / 1000L) 
					+ ": IO Exception occured while connecting with "
					+ clientSocketAddress + "\n" 
					+ ioe.getStackTrace().toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Server.logFile.println((System.currentTimeMillis() / 1000L) 
				+ " Client from " + clientSocketAddress + " disconnected");
		Server.logFile.flush();
	}
	
	//This method handles connections with agents
	private void startAgentConnection() throws IOException, InterruptedException {
		boolean clientWantsToExit = false;
		while(clientWantsToExit == false){			
			if(fromClient.ready()){
				//Next, receive a response from the client
				String response = fromClient.readLine();
				//If the client wants to quit, only allow it to if it has nobody to attend to
				if(response.equals("Quit")){
					if(Server.agentToCustomer.get(username).isEmpty()){
						clientWantsToExit = true;
						Server.agentThreads.remove(username);
						Server.agentToCustomer.remove(username);
						toClient.println("Can quit");
					}
					else{
						toClient.println("Cannot quit");
					}
				}
				//Otherwise, it's a message to a customer
				//In this case, send it to the appropriate customer
				//Check if 
				else{
					String[] clientResponse = response.split("~", 2);
					if(clientResponse.length == 2){
						//If the response is for both customers
						if(clientResponse[0].equals("Both")){
							Pair<String, String> customers = Server.agentToCustomer.get(username);
							//Check if the customers exist and whether they belong to the agent
							if(Server.customerThreads.containsKey(customers.getLeft())
									&& Server.agentToCustomer.get(username).contains(customers.getLeft())){
								Server.customerThreads.get(customers.getLeft()).record((System.currentTimeMillis() / 1000L) 
										+ " " + username + ": " + clientResponse[1]);
								Server.customerThreads.get(customers.getLeft()).transferMessage(username, clientResponse[1]);
							}
							if(Server.customerThreads.containsKey(customers.getRight())
									&& Server.agentToCustomer.get(username).contains(customers.getRight())){
								Server.customerThreads.get(customers.getRight()).record((System.currentTimeMillis() / 1000L) 
										+ " " + username + ": " + clientResponse[1]);
								Server.customerThreads.get(customers.getRight()).transferMessage(username, clientResponse[1]);
							}
						}
						//Otherwise, send the message to the customer that is specified in the agent's response
						else if(Server.customerThreads.containsKey(clientResponse[0])
								&& Server.agentToCustomer.get(username).contains(clientResponse[0])
								){
							Server.customerThreads.get(clientResponse[0]).record((System.currentTimeMillis() / 1000L) 
									+ " " + username + ": " + clientResponse[1]);
							Server.customerThreads.get(clientResponse[0]).transferMessage(username, clientResponse[1]);
						}
					}
				}
			}
			
			//Next, check if the agent is full and whether or not they want to exit
			//If not for both, take from queue and give it to agent
			if(clientWantsToExit == false && !Server.agentToCustomer.get(username).isFull()){
				try{
					String customerToAdd = Server.waitingCustomers.remove();
					toClient.println("NewCustomer~" + customerToAdd);
					Pair<String, String> t = Server.agentToCustomer.get(username);
					Pair<String, String> newPair = null;
					if(t.isEmpty()){
						newPair = new Pair<String, String>(customerToAdd, null);
					}
					else if(t.getLeft() != null){
						newPair = new Pair<String, String>(t.getLeft(), customerToAdd);
					}
					else if(t.getRight() != null){
						newPair = new Pair<String, String>(customerToAdd, t.getRight());
					}
					Server.agentToCustomer.put(username, newPair);
				} catch (NoSuchElementException nsee){
				}
			}
			Thread.sleep(1000);
		}
	}

	//This method handles connections with customers
	private void startCustomerConnection() throws IOException, InterruptedException {
		//The customer first pushes their own name to the Queue
		Server.waitingCustomers.add(username);
		
		boolean clientWantsToExit = false;
		String agent = null;
		while(clientWantsToExit == false){
			if(fromClient.ready()){
				//First, receive a response from the client
				String response = fromClient.readLine();
				//If the customer requests agents
				System.out.println(response);
				//Otherwise, the customer is sending a message to the agent
				String[] clientResponse = response.split("~", 2);
				if(clientResponse.length == 2){
					if(Server.agentThreads.containsKey(clientResponse[0])){
						record((System.currentTimeMillis() / 1000L) + " " + username + ": " + clientResponse[1]);
						Server.agentThreads.get(agent).transferMessage(username, clientResponse[1]);
					}
				}
				else if(response.equals("Waiting for agent")){
					//If the customer's name is still in queue, send to client to wait for agent
					if(Server.waitingCustomers.contains(username)){
						toClient.println("Wait for agent");
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							Server.logFile.println((System.currentTimeMillis() / 1000L) 
							+ ": Thread interruption occured in "
							+ clientSocket.getInetAddress().toString());
						}
					}
					//If not in queue, search through agentToCustomer to find customer, and return agent
					else{
						boolean customerFound = false;
						for(String agentKey : Server.agentToCustomer.keySet()){
							Pair<String, String> temp = Server.agentToCustomer.get(agentKey);
							if(temp.getLeft().equals(username) || temp.getRight().equals(username)){
								toClient.println(agentKey);
								agent = agentKey;
								transcript = new PrintWriter(
										new FileWriter(
												new File(".").getCanonicalPath() 
												+ File.separator + "transcripts" 
														+ File.separator + agent + "~" + username + ".txt", true)
										);
								customerFound = true;
								break;
							}
						}
						//If the customer cannot be found, add back to queue
						if(customerFound == false){
							Server.waitingCustomers.add(username);
							toClient.println("Wait for agent");
						}
					}
				}
				//If the customer requests to quit, quit.
				else if(response.equals("Quit")){
					clientWantsToExit = true;
					Server.customerThreads.remove(username);
					//Remove all traces of the user from the hashmaps
					if(agent != null && Server.agentThreads.get(agent) != null){
						Server.agentThreads.get(agent).toClient.println("Remove~" + username);
						Pair<String, String> temp = Server.agentToCustomer.get(agent);
						if(temp.getLeft().equals(username)){
							Server.agentToCustomer.put(agent, new Pair<String, String>(temp.getRight(), null));
						}
						else if(temp.getRight().equals(username)){
							Server.agentToCustomer.put(agent, new Pair<String, String>(temp.getLeft(), null));
						}
					}
					if(transcript != null){
						transcript.flush();
						transcript.close();
					}
					toClient.println("Can quit");
				}
			}
		}
	}
	
	//This method transfers messages from external calls to the connected client
	public void transferMessage(String source, String message){
		toClient.println(source + "~" + message);
	}
	
	//This method records the messages sent into the customer's transcript 
	private void record(String string) {
		if(transcript != null){
			transcript.println(string);		
			transcript.flush();
			System.out.println("REcorded");
		}
	}

	private boolean isCustomerUsernameTaken(String username) {
		return Server.customerThreads.containsKey(username);
	}

	private boolean isAgentLoggedIn(String username) {
		return Server.agentThreads.containsKey(username);
	}
	
	private boolean logInAgent(String username, String password) {
		return Server.agentsLoginInfo.get(username) != null
				&& Server.agentsLoginInfo.get(username).equals(password);
	}
}
