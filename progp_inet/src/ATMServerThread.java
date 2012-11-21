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
	PrintWriter out;
	
	private enum Language {
		SWE, ENG
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

	private void printMenu(Language language) {
		switch (language) {
		case ENG:
			out.println("Welcome to Bank! \n(1)Balance, \n(2)Withdrawal, \n(3)Deposit, \n(4)Exit\r");			
			break;
		case SWE:
			out.println("Välkommen till Bank! \n(1)Saldo, \n(2)Uttag, \n(3)Insättning, \n(4)Avsluta\r");
			break;
		default:
			break;
		}
	}

	public void run() {

		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			String inputLine, outputLine;

			int balance = 1000;
			int value;
			printSetLanguage();
			inputLine = readLine();
			int choice = Integer.parseInt(inputLine);
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
			printMenu(language);
			inputLine = readLine();
			choice = Integer.parseInt(inputLine);
			while (choice != 4) {
				int deposit = 1;
				switch (choice) {
				case 2:
					deposit = -1;
				case 3:
					printEnterAmount(language);
					inputLine = readLine();
					value = Integer.parseInt(inputLine);
					balance += deposit * value;
				case 1:
					printCurrentBalance(balance, language);
					printMenu(language);
					inputLine = readLine();
					choice = Integer.parseInt(inputLine);
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

	private void printSetLanguage() {
		out.println("Set language! Ange språk! \n(1)English \n(2)Svenska");
	}

	private void printEnterAmount(Language language) {
		switch (language) {
		case ENG:
			out.println("Enter amount: ");			
			break;
		case SWE:
			out.println("Ange belopp: ");
			break;
		}
	}

	private void printCurrentBalance(int balance, Language language) {
		switch (language) {
		case ENG:
			out.println("Current balance is " + balance + " dollars");			
			break;
		case SWE:
			out.println("Nuvarande saldo är " + balance + " dollar");
			break;
		}
	}
}
