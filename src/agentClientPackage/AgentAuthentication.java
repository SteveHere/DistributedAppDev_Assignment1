package agentClientPackage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AgentAuthentication {
	private String USERNAME = "bob123";
	private String PASSWORD = "12345";
	
	public AgentAuthentication() {
	}
	
	public boolean check(String username, String pw) {
		
		
		if(username.equals(USERNAME)){
				
			if(pw.equals(PASSWORD)) {
				return true;
				
			}else {
				return false;
			}
			
		}else {
			return false;
		}
		
	}//end while

}
