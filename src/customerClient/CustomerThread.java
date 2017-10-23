package customerClient;

import java.io.IOException;

import javax.swing.JOptionPane;

import javafx.application.Platform;

public class CustomerThread extends Thread {

	@Override
	public void run() {
		try{
			Thread.sleep(1000);
			//While we haven't found an agent to talk to yet
			JOptionPane.showMessageDialog(null, 
					"Waiting for agents to be available", 
					"Waiting for agents", 
					JOptionPane.INFORMATION_MESSAGE
					);
			while(CustomerClient.agent == null){
				CustomerClient.toServer.println("Waiting for agent");
				String response = CustomerClient.fromServer.readLine();
				//If the response is to wait for an agent, inform the user to do so
				if(response.equals("Wait for agent")){
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Waiting for agents to be available.");
					});
					Thread.sleep(1000);
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Waiting for agents to be available..");
					});
					Thread.sleep(1000);
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Waiting for agents to be available...");
					});
					Thread.sleep(1000);
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Waiting for agents to be available....");
					});
					Thread.sleep(1000);
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Waiting for agents to be available.....");
					});
					Thread.sleep(1000);
				}
				//Otherwise, the response is the agent's name, and it should be stored
				else if(!response.equals("Can quit")){
					CustomerClient.agent = new String(response);
					JOptionPane.showMessageDialog(null, 
							"Agent found. Name: " + CustomerClient.agent, 
							"Agent Found", 
							JOptionPane.INFORMATION_MESSAGE
							);
					Platform.runLater(()->{
						CustomerClient.agentName.setText("Connected To: " + CustomerClient.agent);
						CustomerClient.clientText.setDisable(false);
						CustomerClient.send.setDisable(false);
					});
				}
				//Lastly, check if the customer wanted to quit early, and as such allow the customer to do so
				else{
					break;
				}
			}
			//While the customer has not decided to quit, and has not received word of being able to quit
			while(CustomerClient.wantsToQuit == false){
				if(CustomerClient.fromServer.ready()){
					String[] response = CustomerClient.fromServer.readLine().split("~", 2);
					//If the response has 2 parts, then it is a message from the server
					if(response.length == 2){
						if(response[0].equals(CustomerClient.agent)){
							Platform.runLater(()->{
								CustomerClient.clientTextArea.setText(
									CustomerClient.clientTextArea.getText()
									+ CustomerClient.agent + ": " + response[1] + "\n");
							});
						}
					}
					//Otherwise, the user requested to quit, and the server should quit
					else{
						break;
					}
				}
			}
			System.exit(0);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, 
					"IO Exception encountered. Details: \n" + e, 
					"IO Exception encountered", 
					JOptionPane.ERROR_MESSAGE
					);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
