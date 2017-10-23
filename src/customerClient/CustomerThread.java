package customerClient;

import java.io.IOException;

import javax.swing.JOptionPane;

public class CustomerThread extends Thread {

	@Override
	public void run() {
		try{
			//While we haven't found an agent to talk to yet
			while(CustomerClient.agent == null){
				CustomerClient.toServer.println("Waiting for agent");
				String response = CustomerClient.fromServer.readLine();
				//If the response is to wait for an agent, inform the user to do so
				if(response.equals("Wait for agent")){
					JOptionPane.showMessageDialog(null, 
							"Waiting for agents to be available", 
							"Waiting for agents", 
							JOptionPane.INFORMATION_MESSAGE
							);
					CustomerClient.agentName.setText("Waiting for agents to be available.");
					Thread.sleep(1000);
					CustomerClient.agentName.setText("Waiting for agents to be available..");
					Thread.sleep(1000);
					CustomerClient.agentName.setText("Waiting for agents to be available...");
					Thread.sleep(1000);
					CustomerClient.agentName.setText("Waiting for agents to be available....");
					Thread.sleep(1000);
					CustomerClient.agentName.setText("Waiting for agents to be available.....");
					Thread.sleep(1000);
				}
				//Otherwise, the response is the agent's name, and it should be stored
				else{
					CustomerClient.agent = response;
					JOptionPane.showMessageDialog(null, 
							"Agent found. Name: " + CustomerClient.agent, 
							"Agent found", 
							JOptionPane.INFORMATION_MESSAGE
							);
					CustomerClient.agentName.setText("Connected To: " + CustomerClient.agent);
				}
			}
			//While the customer has not decided to quit, and has not received word of being able to quit
			while(CustomerClient.wantsToQuit == false){
				if(CustomerClient.fromServer.ready()){
					String[] response = CustomerClient.fromServer.readLine().split("~", 1);
					//If the response has 2 parts, then it is a message from the server
					if(response.length == 2){
						if(response[0].equals(CustomerClient.agent)){
							CustomerClient.clientTextArea.setText(
									CustomerClient.clientTextArea.getText()
									+ "\n" + CustomerClient.username + ": " + response[1]);
						}
					}
					//Otherwise, the user requested to quit, and the server should quit
					else{
						
					}
				}
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, 
					"IO Exception encountered. Details: \n" + e, 
					"IO Exception encountered", 
					JOptionPane.ERROR_MESSAGE
					);
		} catch (InterruptedException e) {
		}
	}

}
