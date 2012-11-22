import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * @author Snilledata
 */
public class ATMClient {
	private static int connectionPort = 8988;
	private static Scanner scanner = new Scanner(System.in);
	private static Socket ATMSocket = null;
	private static PrintWriter out = null;
	private static BufferedReader in = null;
	private static String adress = "";

	// private static void printServerMsgWithNewlines(BufferedReader in)
	// throws IOException {
	// int c;
	// while ((c = in.read()) != '\r') {
	// System.out.print((char) c);
	// }
	// in.read();
	// }

	private static void send(Number num) {
		send("" + num);
	}

	private static void send(String msg) {
		LinkedList<String> packages = new LinkedList<String>(Arrays.asList(msg
				.split(".{5}")));
		if (packages.getLast().length() == ATMServerThread.BYTES_PER_PACKAGE)
			packages.add("\0");
		else
			packages.addLast(packages.removeLast() + "\0");

		for (String p : packages)
			out.print(p);
	}

	private static String receive() throws IOException {
		String s = "";
		char c;
		System.out.println("hej");
		while ((c = (char) in.read()) != '\0') {
			System.out.println("hej");
			s = s + c;
		}
		return s;
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
					// printServerMsgWithNewlines(in);
					System.out.println(receive());
					languageOption = getIntegerInput();
					send(languageOption);
				} while (languageOption < 1 || 2 < languageOption);

				// take action depending on login action
				// user is given three tries to log in
				boolean loginOK = false;
				String serverMsg;
				for (int i = 0; !loginOK && i < 3; i++) {
					serverMsg = receive();

					loginOK = serverMsg.equals(ATMServerThread.LOGIN_OK);
					if (loginOK)
						break;

					// server requests user name
					System.out.println(serverMsg);
					send(getLongInput());

					// server requests login code
					System.out.println(receive());
					send(getLongInput());

					// server sends message about login
					System.out.println(receive());
				}

				if (loginOK) {
					// take action depending on menuOption
					int menuOption;
					do {
						// server requests menu choice
						System.out.println(receive()); // welcome msg
						System.out.println(receive()); // menu
						menuOption = getIntegerInput();
						send(menuOption); // return choice to server

						switch (menuOption) {
						case 2:
							//$FALL-THROUGH$
						case 3:
							// servers requests amount entry
							System.out.print(receive());
							send(getIntegerInput()); // return amount to
														// server
						case 1:
							// servers sends current balance message
							System.out.println(receive());
							break;
						}
					} while (menuOption != 4);
				} else {
					// server sends something about 3 tries used
					System.out.println(receive());
				}
			}
		} catch (Exception e) {
			out.close();
			in.close();
			ATMSocket.close();
		}
	}
}
