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

	private void printMenu() {
		out.println("Welcome to Bank! \n(1)Balance, \n(2)Withdrawal, \n(3)Deposit, \n(4)Exit\r");
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

			int value, balance = 1000;
			validateUser();
			printMenu();
			int choice = readIntFromClient();
			while (choice != 4) {
				int deposit = 1;
				switch (choice) {
				case 2:
					deposit = -1;
				case 3:
					out.println("Enter amount: ");
					value = readIntFromClient();
					balance += deposit * value;
				case 1:
					out.println("Current balance is " + balance + " dollars");
					printMenu();
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
