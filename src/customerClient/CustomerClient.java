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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class CustomerClient extends Application {

	static Socket socket;
	static BufferedReader fromServer;
	static PrintWriter toServer;
	static String username;
	static String agent = null;
	static boolean wantsToQuit = false;
	
	static Label agentName;
	static TextArea clientTextArea;
	
	@Override
	public void start(Stage primaryStage){
		String serverAddress = JOptionPane.showInputDialog(
				"Enter IP Address of a machine that is\n" +
				"running the date service on port 9090:");

		try{
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
				socket = new Socket(serverAddress, 9090);

				// Receiving or reading data from the socket
				fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				toServer = new PrintWriter(socket.getOutputStream(), true);
				
				String initialResponse = fromServer.readLine();
				
				//Main logic only starts when the server sends back "Login Authentication Required."
				if(initialResponse.equals("Client request required")){						
					toServer.println("Customer~" + input); 

					String response = fromServer.readLine();
					if(response.equals("Connection established")){
						username = input;
						primaryStage.setTitle("CVT Customer Client - User: " + username);
						primaryStage.setScene(getCustomerApplicationScene());
						primaryStage.setResizable(false);
						primaryStage.show();
						
						CustomerThread thread = new CustomerThread();
						thread.start();
						thread.join();
					}
					else if(response.equals("Username already taken")){
						JOptionPane.showMessageDialog(null, 
								"Username has already been taken. Exiting program.", 
								"Username Already Taken", 
								JOptionPane.INFORMATION_MESSAGE
								);
					}
				}
				fromServer.close();
				toServer.close();
				socket.close();
			}
		} catch(Exception e){
			JOptionPane.showMessageDialog(
					null, 
					"Exception encountered. Details: \n" + e, 
					"Exception encountered", 
					JOptionPane.ERROR_MESSAGE
					);
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
		GridPane result = new GridPane();
		result.setPadding(new Insets(5));
		result.setVgap(5);
		result.setHgap(5);
		
		//Creation of components
		agentName = new Label("Connected To: ");
		
		Button quit = new Button("Quit");
		
		clientTextArea = new TextArea();
		
		TextField clientText = new TextField ();
		
		Button send = new Button("Send");
		
		//Styling of components
		clientTextArea.setMinHeight(390);
		clientTextArea.setMinWidth(440);
		clientText.setMinWidth(420);
		quit.setMinWidth(55);
		send.setMinWidth(55);
		
		//Evetn handling
		send.setOnMouseClicked(e -> {
			if(!clientText.getText().equals("")){
				clientTextArea.setText(clientTextArea.getText() 
						+ ": " + clientText.getText() + "\n");
				clientTextArea.positionCaret(clientTextArea.getLength());
				toServer.println(agent + "~" + clientText.getText());
				clientText.setText("");
			}
		});
		
		quit.setOnMouseClicked(e -> {
			toServer.println("Quit");
			wantsToQuit = true;
		});
		
		//Positioning of components
		result.add(agentName, 0, 0);
		result.add(quit, 1, 0);
		result.add(clientTextArea, 0, 1, 2, 1);
		result.add(clientText, 0, 2);
		result.add(send, 1, 2);
		
		return new Scene(result, 480, 450);
	}

}
