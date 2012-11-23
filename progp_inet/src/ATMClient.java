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
	private Scanner scanner = new Scanner(System.in);
	private Socket ATMSocket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private String adress = "";

	// private static void printServerMsgWithNewlines(BufferedReader in)
	// throws IOException {
	// int c;
	// while ((c = in.read()) != '\r') {
	// System.out.print((char) c);
	// }
	// in.read();
	// }

	private ATMClient(String adress) throws IOException {
		this.adress = adress;
		runClient();
	}

	private void send(Number num) {
		send("" + num);
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
		System.out.println("fšre");
		System.out.println(in.read());
		System.out.println("efter");
		while ((c = (char) in.read()) != '\0') {
			System.out.println("hej");
			s = s + c;
			System.out.println("hej");
		}
		System.out.println("hej2");
		return s;
	}

	private int getIntegerInput() {
		System.out.print("\n> ");
		return scanner.nextInt();
	}

	private long getLongInput() {
		System.out.print("\n> ");
		return scanner.nextLong();
	}

	public void runClient() throws IOException {

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

	public static void main(String[] args) throws IOException {
		try {
			new ATMClient(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println("Missing argument ip-adress");
			System.exit(1);
		}
	}
}
