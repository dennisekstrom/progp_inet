import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Viebrapadata
 */
public class ATMServerThread extends Thread {

	private static final String LOGIN_OK = "LOGIN_OK";
	private static final String TRANSACTION_CODE_OK = "TRANSACTION_CODE_OK";
	private static final String BALANCE_ADJUSTMENT_OK = "BALANCE_ADJUSTMENT_OK";
	private static final int CHARS_PER_PACKAGE = 5;

	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;
	private ATMServer server;
	private Language language;
	private long cardNumber;

	private enum BalanceAction {
		WITHDRAWAL, DEPOSIT;
	}

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
				"Ange transaktionskod från kodlista:",
				"Uttag nekat. Saldo otillräckligt.",
				"Ogiltig transaktionskod.",
				"Ogiltig inmatning.",
				"Hej då!"),
		ENG("(1) Balance\n(2) Whitdrawal\n(3) Deposit\n(4) Exit",
				"Enter amount: ", 
				"Current balance is %d dollars",
				"Card number: ",
				"Login code: ",
				"Logged in with card number: %d",
				"Wrong user name or login code.",
				"Your three attempts to log in failed.",
				"Enter transaction code from code list:",
				"Withdrawal denied. Insufficient funds.",
				"Illegal transaction code.",
				"Illegal input.",
				"Good bye!");
		//@formatter:on

		static String setLanguage = "Set language! Ange språk!\n(1)English\n(2)Svenska";
		String menu;
		String enterAmount;
		String currentBalance;
		String cardNumber;
		String loginCode;
		String successfulLogin;
		String unsuccessfulLogin;
		String threeTriesExpired;
		String codeList;
		String insufficientFunds;
		String invalidTransactionCode;
		String illegalInput;
		String goodBye;

		Language(String menu, String enterAmount, String currentBalance,
				String cardNumber, String loginCode, String successfulLogin,
				String unsuccessfulLogin, String threeTriesExpired,
				String codeList, String insufficientFunds,
				String invalidTransactionCode, String illegalInput,
				String goodBye) {
			this.menu = menu;
			this.enterAmount = enterAmount;
			this.currentBalance = currentBalance;
			this.cardNumber = cardNumber;
			this.loginCode = loginCode;
			this.successfulLogin = successfulLogin;
			this.unsuccessfulLogin = unsuccessfulLogin;
			this.threeTriesExpired = threeTriesExpired;
			this.codeList = codeList;
			this.insufficientFunds = insufficientFunds;
			this.invalidTransactionCode = invalidTransactionCode;
			this.illegalInput = illegalInput;
			this.goodBye = goodBye;
		}
	}

	public ATMServerThread(Socket socket, ATMServer server) throws IOException {
		super("ATMServerThread");
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.socket = socket;
		this.server = server;
	}

	private void send(String msg) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < msg.length(); i++) {
			sb.append(msg.charAt(i));
			if (sb.length() == CHARS_PER_PACKAGE) {
				out.print(sb);
				out.flush();
				sb = new StringBuilder();
			}
		}
		out.print(sb + "\0");
		out.flush();
	}

	private String receive() throws IOException {
		String s = "";
		char c;
		while ((c = (char) in.read()) != '\0')
			s += c;
		return s;
	}

	/**
	 * @return integer of type int input from client.
	 */
	private int readIntFromClient() throws IOException {
		return Integer.parseInt(receive());
	}

	/**
	 * @return integer of type long input from client.
	 */
	private long readLongFromClient() throws IOException {
		return Long.parseLong(receive());
	}

	private void chooseLanguage() throws IOException {

		int choice;
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
	}

	private boolean validateUser() throws IOException {

		boolean loginOK = false;

		// give user three tries to log in
		for (int i = 0; !loginOK && i < 3; i++) {

			// request card number
			send(language.cardNumber);
			long cardNumber = readLongFromClient();

			// request login code
			send(language.loginCode);
			int loginCode = readIntFromClient();

			ATMServer.AccountInfo ai = server.getAccounts().get(cardNumber);

			if (ai != null && ai.loginCode == loginCode) { // login OK
				this.cardNumber = cardNumber;
				send(String.format(language.successfulLogin, cardNumber,
						ai.balance));
				send(LOGIN_OK); // inform client about login OK
				loginOK = true;
			} else { // login failed
				send(language.unsuccessfulLogin);
				loginOK = false;
			}
		}

		return loginOK;
	}

	/**
	 * @return amount to change balance with
	 */
	private Integer validateBalanceAction(BalanceAction action)
			throws IOException {

		send(language.enterAmount);
		int amount = readIntFromClient();

		if (amount < 0) {
			send(language.illegalInput);
			return null;
		}

		send(BALANCE_ADJUSTMENT_OK);
		return (action == BalanceAction.DEPOSIT) ? amount : -amount;
	}
	
	private boolean validateTransaction() throws IOException {
		send(language.codeList);
		String transactionCode = receive();
		if (server.getCodeList().contains(transactionCode)) {
			send(TRANSACTION_CODE_OK);
			return true;
		} else {
			send(language.invalidTransactionCode);
			return false;
		}
	}

	private boolean validateBalanceAdjustment(BalanceAction action, int amount) {
		if (action == BalanceAction.DEPOSIT
				|| server.getBalance(cardNumber) + amount >= 0) {
			send(BALANCE_ADJUSTMENT_OK);
			return true;
		} else {
			send(language.insufficientFunds);
			return false;
		}
	}

	private void mainMenu() throws IOException {
		Integer balanceAdjustment;
		int choice;
		String preMsgInfo = "";
		do {
			// request menu choice
			send(preMsgInfo + server.getWelcomeMessage() + "\n" + language.menu);
			choice = readIntFromClient();

			BalanceAction action = BalanceAction.DEPOSIT;
			switch (choice) {
			case 2:
				action = BalanceAction.WITHDRAWAL;
			case 3:
				balanceAdjustment = validateBalanceAction(action);
				if (balanceAdjustment != null && validateTransaction()
						&& validateBalanceAdjustment(action, balanceAdjustment)) {
					server.deposit(cardNumber, balanceAdjustment);
				} else {
					preMsgInfo = "";
					break;
				}
			case 1:
				send(String.format(language.currentBalance,
						server.getBalance(cardNumber)));
				preMsgInfo = "";
				break;
			default:
				preMsgInfo = "Invalid choice.\n";

			}
		} while (choice != 4);
	}

	@Override
	public void run() {
		try {
			while (true) {
				chooseLanguage();

				if (validateUser()) {
					mainMenu();
					send(language.goodBye);
				} else {
					send(language.threeTriesExpired);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
