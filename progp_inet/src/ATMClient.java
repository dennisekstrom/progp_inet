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
	private static int connectionPort = 8988;

	private static Scanner scanner = new Scanner(System.in);

	private static void printServerMsgWithNewlines(BufferedReader in)
			throws IOException {
		int c;
		while ((c = in.read()) != '\r') {
			System.out.print((char) c);
		}
		in.read();
	}

	private void send(String s) {
		
	}
	
	private String receive() {
		return "";
	}
	
	private static int getIntegerInput() {
		System.out.print("\n> ");
		return scanner.nextInt();
	}

	private static long getLongInput() {
		System.out.print("\n> ");
		return scanner.nextLong();
	}

	public static void main(String[] args) throws IOException {

		Socket ATMSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		String adress = "";

		try {
			adress = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Missing argument ip-adress");
			System.exit(1);
		}
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
		try {
			while (true) {
				int languageOption;
				do {
					// server requests language choice
					printServerMsgWithNewlines(in);
					languageOption = getIntegerInput();
					out.println(languageOption);
				} while (languageOption < 1 || 2 < languageOption);

				// take action depending on login action
				// user is given three tries to log in
				boolean loginOK = false;
				String serverMsg;
				for (int i = 0; !loginOK && i < 3; i++) {
					serverMsg = in.readLine();

					loginOK = serverMsg.equals(ATMServerThread.LOGIN_OK);
					if (loginOK)
						break;

					// server requests user name
					System.out.println(serverMsg);
					out.println(getLongInput());

					// server requests login code
					System.out.println(in.readLine());
					out.println(getLongInput());

					// server sends message about login
					System.out.println(in.readLine());
				}

				if (loginOK) {
					// take action depending on menuOption
					int menuOption;
					do {
						// server requests menu choice
						System.out.println(in.readLine());
						printServerMsgWithNewlines(in);
						menuOption = getIntegerInput();
						out.println(menuOption); // return choice to server

						switch (menuOption) {
						case 2:
							//$FALL-THROUGH$
						case 3:
							// servers requests amount entry
							System.out.print(in.readLine());
							out.println(getIntegerInput()); // return amount to
															// server
						case 1:
							// servers sends current balance message
							System.out.println(in.readLine());
							break;
						}
					} while (menuOption != 4);
				} else {
					// server sends something about 3 tries used
					System.out.println(in.readLine());
				}
			}
		} catch (Exception e) {
			out.close();
			in.close();
			ATMSocket.close();
		}
	}
}
