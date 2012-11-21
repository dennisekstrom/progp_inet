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
	}

	private static int getIntegerInput() {
		System.out.print("\n> ");
		return scanner.nextInt();
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

		// server requests language choice
		printServerMsgWithNewlines(in);
		out.println(getIntegerInput());
		// take action depending on menuOption
		int menuOption;
		do {
			// gets menu from server
			printServerMsgWithNewlines(in);
			menuOption = getIntegerInput();
			out.println(menuOption); // give back option entered by user

			switch (menuOption) {
			case 2:
				//$FALL-THROUGH$
			case 3:
				// gets enterAmount msg from server
				System.out.println(in.readLine());
				// give back amount entered by user
				out.println(getIntegerInput());
			case 1:
				// gets currentBalance msg from server
				System.out.println(in.readLine());
				break;
			}
		} while (menuOption != 4);

		out.close();
		in.close();
		ATMSocket.close();
	}
}
