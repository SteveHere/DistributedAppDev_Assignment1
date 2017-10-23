package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

import common.Pair;

public class Server {
	
	static Map<String, String> agentsLoginInfo;
	static Map<String, RequestHandler> customerThreads;
	static Map<String, RequestHandler> agentThreads;
	static Map<String, Pair<String, String>> agentToCustomer;
	static Queue<String> waitingCustomers; 
	
	static PrintWriter logFile;
	
	public static void main(String[] args) {
		ServerSocket listener;  
		Socket connection; 
		
		// First off, open log.txt
		try{
			logFile = new PrintWriter(new FileWriter("log.txt", true));
		} catch (IOException e){
			System.err.println("Cannot open log.txt. Please fix the problem and try again.");
			System.exit(1);
		}
		
		// Next, preload a list of usernames and passwords for agents
		parseAgentsLoginInfo();
		
		// Accept and process connections forever, or until some error occurs.
		try {
			listener = new ServerSocket(9090);
			System.out.println("Server is Up, Running and Waiting....");
			while (true) {
				// Accept connection request and create the thread handler needed to resolve this.
				connection = listener.accept(); 
				// Set up the writers and readers
				
				RequestHandler handler = new RequestHandler(connection); 
				handler.start();
			}
		}
		catch (IOException e) {
			System.out.println("Sorry, the server has encountered IO problems. Shutting down.");
			logFile.println((System.currentTimeMillis() / 1000L) + ": Server shutting down due to IO problems.");
		}
		finally {
			logFile.close();
		}
	}

	// This function parses the login info from agents.txt.
	// The format for each line must go as follows: <username>~<password>
	private static void parseAgentsLoginInfo() {
		try(BufferedReader fileReader = new BufferedReader(new FileReader("agents.txt"))){
			String line = null;
			while((line = fileReader.readLine()) != null){
				if(!line.isEmpty()){
					String[] loginInfo = line.split("~", 2);
					if(loginInfo.length == 2) {
						agentsLoginInfo.put(loginInfo[0], loginInfo[1]);
					}
					else {
						throw new IllegalArgumentException();
					}
				}
			}
		} catch (IllegalArgumentException iae) {
			System.err.println("Encountered parsing error with agents.txt.\n"
					+ "Proper format: <Username>~<Password>\n"
					+ "Please fix this formatting issue and try again.");
			logFile.println((System.currentTimeMillis() / 1000L) 
					+ ": Encountered parsing error with agents.txt. Exiting program.");
			logFile.close();
			System.exit(2);
		} catch (IOException e) {
			System.err.println("Encountered an IO error with agents.txt.\n"
					+ "Please fix the problem and try again.");
			logFile.println((System.currentTimeMillis() / 1000L) 
					+": Encountered IO error with agents.txt. Exiting program.");
			logFile.close();
			System.exit(3);
		} 
	}
	
}
