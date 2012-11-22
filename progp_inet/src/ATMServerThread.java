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

	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;
	private ATMServer server;
	private Language language;
	private long cardNumber;

	private enum Language {
		//@formatter:off
		SWE("(1) Saldo\n(2) Uttag\n(3) Insättning\n(4) Avsluta\r", 
				"Ange belopp: ",
				"Nuvarande saldo är %d dollar",
				"Kortnummer: ",
				"Inloggningskod: ",
				"Inloggad med kortnr: %d", 
				"Fel kortnummer eller inloggningskod.",
				"Dina tre försök har förbrukats."),
		ENG("(1) Balance\n(2) Whitdraw\n(3) Deposit\n(4) Exit\r",
				"Enter amount: ", 
				"Current balance is %d dollars",
				"Card number: ",
				"Login code: ",
				"Logged in with card number: %d",
				"Wrong user name or login code.",
				"Your three attempts to log in failed.");
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

		Language(String menu, String enterAmount, String currentBalance,
				String cardNumber, String loginCode, String successfulLogin,
				String unsuccessfulLogin, String threeTriesExpired) {
			this.menu = menu;
			this.enterAmount = enterAmount;
			this.currentBalance = currentBalance;
			this.cardNumber = cardNumber;
			this.loginCode = loginCode;
			this.successfulLogin = successfulLogin;
			this.unsuccessfulLogin = unsuccessfulLogin;
			this.threeTriesExpired = threeTriesExpired;
		}
	}

	public ATMServerThread(Socket socket, ATMServer server) {
		super("ATMServerThread");
		this.socket = socket;
		this.server = server;
	}

	// public synchronized void setWelcomeMessage(String welcomeMessage) {
	// this.welcomeMessage = welcomeMessage + "\n";
	// }

	private String readLine() throws IOException {
		String str = in.readLine();
		// System.out.println("" + socket + " : " + str);
		return str;
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
		out.println(language.cardNumber);
		long cardNumber = readLongFromClient();

		// request security code
		out.println(language.loginCode);
		int loginCode = readIntFromClient();

		ATMServer.AccountInfo ai = server.getAccounts().get(cardNumber);
		if (ai != null && ai.loginCode == loginCode) {
			this.cardNumber = cardNumber;
			out.printf(language.successfulLogin + " " + language.currentBalance
					+ "\n", cardNumber, ai.balance);
			return true;
		} else {
			out.println(language.unsuccessfulLogin);
			return false;
		}
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
					out.println(preMsgInfo + Language.setLanguage);
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
					out.println(LOGIN_OK);

					preMsgInfo = "";
					do {
						// request menu choice
						out.println(preMsgInfo + server.getWelcomeMessage()
								+ "\n" + language.menu);
						choice = readIntFromClient();

						int deposit = 1;
						switch (choice) {
						case 2:
							deposit = -1;
						case 3:
							out.println(language.enterAmount);
							value = readIntFromClient();
							server.deposit(cardNumber, deposit * value);
						case 1:
							out.printf(language.currentBalance + "\n",
									server.getBalance(cardNumber));
							preMsgInfo = "";
							break;
						default:
							preMsgInfo = "Invalid choice.\n";

						}
					} while (choice != 4);

					out.println("Good Bye");

					out.close();
					in.close();
					socket.close();
				} else {
					out.println(language.threeTriesExpired);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
