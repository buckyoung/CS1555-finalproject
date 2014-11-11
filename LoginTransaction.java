public class LoginTransaction extends Transaction {

	public LoginTransaction(String username, String password, int loginType) {
		this.username = username;
		this.password = password;
		this.loginType = loginType;
	}

	public void execute() {
		
		// do some DB work
	
		results = ( success ) ? "Logged in successfully!" : "Login attempt failed.";
	}
	
	private String username;
	private String password;
	private int loginType;
}