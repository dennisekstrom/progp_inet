import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author Snilledata
 */
public class ATMClient {

	private static final String LOGIN_OK = "LOGIN_OK";
	private static final String TRANSACTION_CODE_OK = "TRANSACTION_CODE_OK";
	private static final String BALANCE_ADJUSTMENT_OK = "BALANCE_ADJUSTMENT_OK";
	private static final int CHARS_PER_PACKAGE = 5;

	private static int connectionPort = 8988;
	private Scanner scanner = new Scanner(System.in);
	private Socket ATMSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;

	private ATMClient(String adress) throws IOException {
		try {
			ATMSocket = new Socket(adress, connectionPort);
			out = new PrintWriter(ATMSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					ATMSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + adress);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't open connection to " + adress);
			System.exit(1);
		}

		System.out.println("Contacting bank ... ");
		runClient();

		out.close();
		in.close();
		ATMSocket.close();
	}

	private void send(Number num) {
		send("" + num);
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

	private int getIntegerInput() {
		System.out.print("> ");
		return scanner.nextInt();
	}

	private String getStringInput() {
		System.out.print("> ");
		scanner.nextLine();
		return scanner.nextLine();
	}

	private long getLongInput() {
		System.out.print("> ");
		return scanner.nextLong();
	}

	private void chooseLanguage() throws IOException {
		int languageOption;
		do {
			// server requests language choice
			// printServerMsgWithNewlines(in);
			System.out.println(receive());
			languageOption = getIntegerInput();
			send(languageOption);
		} while (languageOption < 1 || 2 < languageOption);
	}

	private boolean validateUser() throws IOException {

		boolean loginOK = false;
		String serverMsg;

		// user is given three tries to log in
		for (int i = 0; !loginOK && i < 3; i++) {

			serverMsg = receive();
			loginOK = serverMsg.equals(LOGIN_OK);
			if (loginOK)
				break;

			// server requests card number
			System.out.println(serverMsg);
			send(getLongInput());

			// server requests login code
			System.out.println(receive());
			send(getLongInput());

			// server sends message about login
			System.out.println(receive());
		}

		return loginOK;
	}

	private boolean validateBalanceAction() throws IOException {
		// server requests deposit/withdrawal amount
		System.out.println(receive());
		send(getIntegerInput());

		String serverMsg = receive();
		if (serverMsg.equals(BALANCE_ADJUSTMENT_OK)) {
			return true;
		} else {
			// server sends something about why transaction was denied
			System.out.println(serverMsg);
			return false;
		}
	}

	private boolean validateTransaction() throws IOException {
		// server requests transaction code
		System.out.println(receive());
		send(getStringInput());

		String serverMsg = receive();
		if (serverMsg.equals(TRANSACTION_CODE_OK)) {
			return true;
		} else {
			System.out.println(serverMsg);
			return false;
		}
	}

	private boolean validateBalanceAdjustment() throws IOException {
		String serverMsg = receive();
		if (serverMsg.equals(BALANCE_ADJUSTMENT_OK)) {
			return true;
		} else {
			System.out.println(serverMsg);
			return false;
		}
	}

	private void mainMenu() throws IOException {
		// take action depending on menuOption
		int menuOption;
		do {
			// server requests menu choice
			System.out.println(receive()); // welcome msg + menu
			menuOption = getIntegerInput();
			send(menuOption); // return choice to server

			switch (menuOption) {
			case 2:
				// FALL-THROUGH
			case 3:
				// server requests deposit/withdrawal amount and, if true,
				// followed by transaction security code
				if (!validateBalanceAction() || !validateTransaction()
						|| !validateBalanceAdjustment())
					break;
			case 1:
				// servers sends current balance message
				System.out.println(receive());
				break;
			}
		} while (menuOption != 4);
	}

	private void runClient() throws IOException {
		while (true) {
			chooseLanguage();

			if (validateUser()) {
				mainMenu();

				// server sends good bye message
				System.out.println(receive());
			} else {
				// server sends about 3 tries expired
				System.out.println(receive());
			}
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			new ATMClient(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Missing argument ip-adress");
			System.exit(1);
		}
	}
}
