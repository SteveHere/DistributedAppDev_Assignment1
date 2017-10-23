package customerClient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CustomerClient extends Application {

	Socket socket;
	BufferedReader fromServer;
	PrintWriter toServer;
	
	@Override
	public void start(Stage primaryStage){
		String serverAddress = JOptionPane.showInputDialog(
				"Enter IP Address of a machine that is\n" +
				"running the date service on port 9090:");

		try{
			try{      
				socket = new Socket(serverAddress, 9090);

				// Receiving or reading data from the socket
				fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				toServer = new PrintWriter(socket.getOutputStream(), true);
				
				String initialResponse = fromServer.readLine();
				
				//Main logic only starts when the server sends back "Login Authentication Required."
				if(initialResponse.equals("Client request required")){
					boolean correctUsernameFormat = false; //assumed incorrect by default
					String input = null;

					do{
						input = loginPrompt();
						//input is only correct if the username has no tilde, or if nothing was entered
						correctUsernameFormat = (input == null) || !input.contains("~");
						if(correctUsernameFormat == false){
							JOptionPane.showMessageDialog(
									null, 
									"Incorrect username format detected. Please try again.", 
									"Incorrect Username Format", 
									JOptionPane.ERROR_MESSAGE
									);
						}
					} while(correctUsernameFormat == false);
					
					
					if(input == null){
						JOptionPane.showMessageDialog(null, 
								"Login aborted. Exiting program.", 
								"Login aborted", 
								JOptionPane.INFORMATION_MESSAGE
								);
					}
					else{
						toServer.println("Customer~" + input); 

						String response = fromServer.readLine();
						if(response.equals("Connection established")){
							primaryStage.setTitle("CVT Agent Client");
							primaryStage.setScene(getCustomerApplicationScene());
							primaryStage.show();
						}
						else if(response.equals("Username already taken")){
							JOptionPane.showMessageDialog(null, 
									"Username has already been taken. Exiting program.", 
									"Username Already Taken", 
									JOptionPane.INFORMATION_MESSAGE
									);
						}
					}
				}

				fromServer.close();
				toServer.close();
			} finally{
				socket.close();
			}
		} catch(Exception e){
			System.out.println("Exception occurred " + e);
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
	
	private static String loginPrompt(){
		JTextField username = new JTextField();

		JPanel myPanel = new JPanel(new BorderLayout(5, 5));
		
		JPanel labels = new JPanel(new GridLayout(0,1,2,2));
		labels.add(new JLabel("Username: ", SwingConstants.LEFT));
		myPanel.add(labels, BorderLayout.WEST);

		JPanel controls = new JPanel(new GridLayout(0,1,2,2));
		controls.add(username);
		myPanel.add(controls, BorderLayout.CENTER);
		
		int result = JOptionPane.showOptionDialog(null, myPanel, 
				"CVT Customer: Enter Username", JOptionPane.OK_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE, null, new String[]{"Connect", "Cancel"}, null);
		if (result == JOptionPane.OK_OPTION) {
			return username.getText();
		}
		return null;
	}
	
	private Scene getCustomerApplicationScene() {
		Pane result = new Pane();
		return new Scene(result, 720, 600);
	}

}
