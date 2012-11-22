import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Viebrapadata
 */
public class ATMServerThread extends Thread {

	public static final String LOGIN_OK = "LOGIN_OK";
	public static final int BYTES_PER_PACKAGE = 5;

	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;
	private ATMServer server;
	private Language language;
	private long cardNumber;

	private enum Language {
		//@formatter:off
		SWE("(1) Saldo\n(2) Uttag\n(3) Insättning\n(4) Avsluta", 
				"Ange belopp: ",
				"Nuvarande saldo är %d dollar",
				"Kortnummer: ",
				"Inloggningskod: ",
				"Inloggad med kortnr: %d", 
				"Fel kortnummer eller inloggningskod.",
				"Dina tre försök har förbrukats.",
				"Ange transaktionskod frÂn kodlista."),
		ENG("(1) Balance\n(2) Whitdraw\n(3) Deposit\n(4) Exit",
				"Enter amount: ", 
				"Current balance is %d dollars",
				"Card number: ",
				"Login code: ",
				"Logged in with card number: %d",
				"Wrong user name or login code.",
				"Your three attempts to log in failed.",
				"Enter transaction code from code list.");
		//@formatter:on

		static String setLanguage = "Set language! Ange språk! \n(1)English \n(2)Svenska\r";
		String menu;
		String enterAmount;
		String currentBalance;
		String cardNumber;
		String loginCode;
		String successfulLogin;
		String unsuccessfulLogin;
		String threeTriesExpired;
		String codeList;

		Language(String menu, String enterAmount, String currentBalance,
				String cardNumber, String loginCode, String successfulLogin,
				String unsuccessfulLogin, String threeTriesExpired, String codeList) {
			this.menu = menu;
			this.enterAmount = enterAmount;
			this.currentBalance = currentBalance;
			this.cardNumber = cardNumber;
			this.loginCode = loginCode;
			this.successfulLogin = successfulLogin;
			this.unsuccessfulLogin = unsuccessfulLogin;
			this.threeTriesExpired = threeTriesExpired;
			this.codeList = codeList;
		}
	}

	public ATMServerThread(Socket socket, ATMServer server) {
		super("ATMServerThread");
		this.socket = socket;
		this.server = server;
	}

	private void send(String msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < msg.length(); i++) {
			sb.append(msg.charAt(i));
			if (sb.length() == 5) {
				out.print(sb);
				sb = new StringBuilder();
			}
		}
		out.print(sb + "\0");
	}

	private String receive() throws IOException {
		String s = "";
		char c;
		while ((c = (char) in.read()) != '\0') {
			s = s + c;
		}
		return s;
	}

	private String readLine() throws IOException {
		return receive();
	}

	/**
	 * @return integer of type int input from client.
	 */
	private int readIntFromClient() throws IOException {
		return Integer.parseInt(readLine());
	}

	/**
	 * @return integer of type long input from client.
	 */
	private long readLongFromClient() throws IOException {
		return Long.parseLong(readLine());
	}

	private boolean validateUser() throws IOException {
		// request card number
		send(language.cardNumber);
		long cardNumber = readLongFromClient();

		// request security code
		send(language.loginCode);
		int loginCode = readIntFromClient();

		ATMServer.AccountInfo ai = server.getAccounts().get(cardNumber);
		if (ai != null && ai.loginCode == loginCode) {
			this.cardNumber = cardNumber;
			send(String.format(language.successfulLogin + " "
					+ language.currentBalance + "\n", cardNumber, ai.balance));
			return true;
		} else {
			send(language.unsuccessfulLogin);
			return false;
		}
	}
	
	private boolean validateTransaction() throws IOException {
		send(language.codeList);
		String transactionCode = readLine();
		if(server.codeList.contains(transactionCode)) {
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		try {
			while (true) {
				out = new PrintWriter(socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));

				int value, choice;
				String preMsgInfo = "";
				do {
					// request language choice
					send(preMsgInfo + Language.setLanguage);
					choice = readIntFromClient();

					switch (choice) {
					case 1:
						language = Language.ENG;
						break;
					case 2:
						language = Language.SWE;
						break;
					default:
						preMsgInfo = "Invalid choice.\n";
						break;
					}
				} while (choice < 1 || 2 < choice);

				// give user three tries to log in
				boolean loginOK = false;
				for (int i = 0; !loginOK && i < 3; i++) {
					loginOK = validateUser();
				}

				if (loginOK) {
					// inform client that login was OK
					send(LOGIN_OK);

					preMsgInfo = "";
					do {
						// request menu choice
						send(preMsgInfo + server.getWelcomeMessage() + "\n"
								+ language.menu);
						choice = readIntFromClient();

						int deposit = 1;
						switch (choice) {
						case 2:
							deposit = -1;
						case 3:
							validateTransaction();
							send(language.enterAmount);
							value = readIntFromClient();
							server.deposit(cardNumber, deposit * value);
						case 1:
							send(String.format(language.currentBalance + "\n",
									server.getBalance(cardNumber)));
							preMsgInfo = "";
							break;
						default:
							preMsgInfo = "Invalid choice.\n";

						}
					} while (choice != 4);

					send("Good Bye");

					out.close();
					in.close();
					socket.close();
				} else {
					send(language.threeTriesExpired);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
