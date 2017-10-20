package agentClientPackage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class AgentAccess {
	public static void main(String[] args) throws IOException{
		
		String serverAddress = JOptionPane.showInputDialog(
				"Enter IP address of a machine that is\n"+
				"running the date service on port 8080:");
		
		Socket socket = null;
		 
		 try {
			 try {
				 socket = new Socket(serverAddress, 8080);
				 
				 // Receiving or reading data from the socket
				 System.out.println("Agent receiving from the server ");
				 BufferedReader fromServer = 
						 new BufferedReader(new InputStreamReader(socket.getInputStream()));
				 String request = fromServer.readLine();
				 System.out.println(request);
				 
				 // Sending data or writing to the socket
				 Scanner userInput = new Scanner(System.in);
				 String userName = userInput.nextLine();
				 PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true);
				 toServer.println(userName);
				 System.out.println("Agent sending to the server ");
				 
				 // Receiving or reading data from the socket
				 request = fromServer.readLine();
				 System.out.println(request);
				 String pw = userInput.nextLine();
				 toServer.println(pw);
				 
				 String welcome = fromServer.readLine();
				 JOptionPane.showMessageDialog(null,  welcome);
				 System.exit(0);
				 
				 socket.close();
			 }finally {
				 socket.close();
			 }//end inner try-finally block
		 
		 }catch(Exception e) {
			 System.out.println("Exception occured " +e);
		 }// end outer try-finally block
		 
	}// end main
}