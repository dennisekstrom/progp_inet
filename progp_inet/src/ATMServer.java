import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author Viebrapadata
 */
public class ATMServer {

	private ArrayList<String> codeList = generateCodeList();

	class AccountInfo {
		int loginCode, balance;

		AccountInfo(int loginCode, int balance) {
			this.loginCode = loginCode;
			this.balance = balance;
		}
	}

	private static ArrayList<String> generateCodeList() {
		ArrayList<String> s = new ArrayList<String>();
		s.addAll(Arrays.asList(new String[] { "01", "03", "05", "07", "09" }));
		for (int i = 11; i < 100; i += 2) {
			s.add("" + i);
		}
		return s;
	}

	private int connectionPort = 8988;
	private volatile String welcomeMessage = "Welcome to Bank!";
	private volatile HashMap<Long, AccountInfo> accounts = new HashMap<Long, AccountInfo>();

	// thread prompting for welcome message to be displayed on client side
	private Thread serverAdminThread = new Thread() {

		Scanner scanner = new Scanner(System.in);

		@Override
		public void run() {
			int choice, code, initBalance;
			long id;
			while (true) {
				// print administration menu
				System.out.println("Server administraion menu:");
				System.out.println("(1) Add account");
				System.out.println("(2) Remove account");
				System.out.println("(3) Set welcome message");

				// get administrator choice
				choice = Integer.parseInt(scanner.nextLine());

				switch (choice) {
				case 1:
					// prompt for account info
					System.out.print("Card number: \n> ");
					id = Long.parseLong(scanner.nextLine());

					// print message and return if card number unavailable
					if (accounts.containsKey(id)) {
						System.out.println("Card number already in use.");
						break;
					}

					// else, keep prompting for account info
					System.out.print("Security code: \n> ");
					code = Integer.parseInt(scanner.nextLine());
					System.out.print("Initial balance: \n> ");
					initBalance = Integer.parseInt(scanner.nextLine());

					// add account
					accounts.put(id, new AccountInfo(code, initBalance));
					System.out.println("Account successfully added.");
					break;
				case 2:
					System.out.print("Card number: \n> ");
					id = Long.parseLong(scanner.nextLine());
					AccountInfo removed = accounts.remove(id);
					if (removed == null)
						System.out.println("No such account: " + id);
					else
						System.out.println("Account successfully removed.");
					break;
				case 3:
					System.out.print("Type welcome message: \n> ");
					welcomeMessage = scanner.nextLine();
					break;
				}
			}
		}
	};

	private ATMServer() throws IOException {
		ServerSocket serverSocket = null;

		boolean listening = true;

		try {
			serverSocket = new ServerSocket(connectionPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + connectionPort);
			System.exit(1);
		}

		System.out.println("Bank started listening on port: " + connectionPort);

		// start thread to prompt for welcome message to be displayed
		serverAdminThread.start();

		while (listening)
			new ATMServerThread(serverSocket.accept(), this).start();

		serverSocket.close();
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public HashMap<Long, AccountInfo> getAccounts() {
		return accounts;
	}
	
	public ArrayList<String> getCodeList() {
		return codeList;
	}

	public Integer getBalance(long cardNumber) {
		AccountInfo ai = accounts.get(cardNumber);
		return (ai == null) ? null : ai.balance;
	}

	public void deposit(long cardNumber, int amount) {
		AccountInfo ai = accounts.get(cardNumber);
		if (ai != null)
			ai.balance += amount;
	}

	public static void main(String[] args) throws IOException {
		new ATMServer();
	}
}