import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Viebrapadata
 */
public class ATMServerThread extends Thread {
	private Socket socket = null;
	private BufferedReader in;
	private PrintWriter out;

	private enum Language {
		SWE("Välkommen till Bank!\n(1)Saldo\n(2)Uttag\n(3)Insättning\n(4)Avsluta\r",
				"Ange belopp: ", "Nuvarande saldo är %d dollar"), 
		ENG("Welcome to Bank!\n(1)Balance\n(2)Whitdraw\n(3)Deposit\n(4)Exit\r",
				"Enter amount: ", "Current balance is %d dollars");

		static String setLanguage = "Set language! Ange språk! \n(1)English \n(2)Svenska\r"; 
		String menu;
		String enterAmount;
		String currentBalance;

		Language(String menu, String enterAmount, String currentBalance) {
			this.menu = menu;
			this.enterAmount = enterAmount;
			this.currentBalance = currentBalance;
		}
	}

	private Language language;

	public ATMServerThread(Socket socket) {
		super("ATMServerThread");
		this.socket = socket;
	}

	private String readLine() throws IOException {
		String str = in.readLine();
		// System.out.println("" + socket + " : " + str);
		return str;
	}

	private boolean validateUser() {
		return true;
	}

	/**
	 * @return integer input from client.
	 */
	private int readIntFromClient() throws IOException {
		return Integer.parseInt(readLine());
	}

	public void run() {

		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String inputLine, outputLine;

			int balance = 1000;
			int value;
			out.println(Language.setLanguage);
			int choice = readIntFromClient();
			switch (choice) {
			case 1:
				language = Language.ENG;
				break;
			case 2:
				language = Language.SWE;
				break;
			default:

				break;
			}
			validateUser();
			out.println(language.menu);
			choice = readIntFromClient();
			while (choice != 4) {
				int deposit = 1;
				switch (choice) {
				case 2:
					deposit = -1;
				case 3:
					out.println(language.enterAmount);
					value = readIntFromClient();
					balance += deposit * value;
				case 1:
					out.printf(language.currentBalance + "\n", balance);
					out.println(language.menu);
					choice = readIntFromClient();
					break;
				case 4:
					break;
				default:
					break;
				}
			}
			out.println("Good Bye");
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
