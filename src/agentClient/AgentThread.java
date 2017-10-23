package agentClient;

import java.io.IOException;

import javax.swing.JOptionPane;

import agentClient.AgentClient;

public class AgentThread extends Thread {

	@Override
	public void run() {
		try{
			//While the agent has not decided to quit, and has not received word of being able to quit
			while(AgentClient.wantsToQuit == false || AgentClient.canQuit == false){
				if(AgentClient.fromServer.ready()){
					String[] response = AgentClient.fromServer.readLine().split("~", 1);
					//If the response has 2 parts, then it is a message from the server, 
					// the receiving of a new customer, or the end of an existing customer's session
					if(response.length == 2){
						//If it's a message, display it
						if(response[0].equals(AgentClient.customer1)){
							AgentClient.customer1Dialog.setText(
									AgentClient.customer1Dialog.getText()
									+ "\n" + AgentClient.customer1 + ": " + response[1]);
						}
						else if(response[0].equals(AgentClient.customer2)){
							AgentClient.customer2Dialog.setText(
									AgentClient.customer2Dialog.getText()
									+ "\n" + AgentClient.customer2 + ": " + response[1]);
						}
						//If it's a new customer, add it to an unused area
						else if(response[0].equals("NewCustomer")){
							if(AgentClient.customer1 == null){
								AgentClient.customer1 = response[1];
								AgentClient.customer1Label.setText("Connected Customer: " + response[1]);
								AgentClient.customer1Input.setDisable(false);
								JOptionPane.showMessageDialog(null, 
										"A new customer has joined in. Name: " + response[1], 
										"New Customer", 
										JOptionPane.INFORMATION_MESSAGE
										);
							}
							else if(AgentClient.customer2 == null){
								AgentClient.customer2 = response[1];
								AgentClient.customer2Label.setText("Connected Customer: " + response[1]);
								AgentClient.customer2Input.setDisable(false);
								JOptionPane.showMessageDialog(null, 
										"A new customer has joined in. Name: " + response[1], 
										"New Customer", 
										JOptionPane.INFORMATION_MESSAGE
										);
							}
						}
						else if(response[0].equals("Remove")){
							if(AgentClient.customer1.equals(response[1])){
								AgentClient.customer1 = null;
								AgentClient.customer1Dialog.setText("");
								AgentClient.customer1Label.setText("Connected Customer: ");
								AgentClient.customer1Input.setDisable(true);
								JOptionPane.showMessageDialog(null, 
										"A customer has disconnected. Name: " + response[1], 
										"Customer Disconnected", 
										JOptionPane.INFORMATION_MESSAGE
										);
							}
							else if(AgentClient.customer2.equals(response[1])){
								AgentClient.customer2 = null;
								AgentClient.customer2Dialog.setText("");
								AgentClient.customer2Label.setText("Connected Customer: ");
								AgentClient.customer2Input.setDisable(true);
								JOptionPane.showMessageDialog(null, 
										"A customer has disconnected. Name: " + response[1], 
										"Customer Disconnected", 
										JOptionPane.INFORMATION_MESSAGE
										);
							}
						}
					}
					//Otherwise, the user requested to quit
					//Whether or not the user can quit is based on the server's response
					else{
						if(response[0].equals("Can quit")){
							AgentClient.canQuit = true;
							JOptionPane.showMessageDialog(null, 
									"You can now quit this session.", 
									"Session Quit Successful", 
									JOptionPane.INFORMATION_MESSAGE
									);
						}
						if(response[0].equals("Cannot quit")){
							JOptionPane.showMessageDialog(null, 
									"You cannot quit this session yet.", 
									"Session Quit Failed", 
									JOptionPane.INFORMATION_MESSAGE
									);
						}
					}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, 
					"IO Exception encountered. Details: \n" + e, 
					"IO Exception encountered", 
					JOptionPane.ERROR_MESSAGE
					);
		}
	}
}
