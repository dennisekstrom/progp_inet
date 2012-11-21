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
	private static int connectionPort = 8989;

	private static Scanner scanner = new Scanner(System.in);
	
	private static void printServerMsgWithNewlines(BufferedReader in) throws IOException {
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
		int languageOption = 1;
		languageOption = getIntegerInput();
		out.println(languageOption);
		
		// take action depending on menuOption
		int menuOption; 
		int userInput;
		do {
			// server requests menu choice
			printServerMsgWithNewlines(in);
			menuOption = getIntegerInput();
			out.println(menuOption);
			
			switch (menuOption) {
			case 1:
				System.out.println(in.readLine());	// gets currentBalance msg from server
				break;
			case 2:
				//$FALL-THROUGH$
			case 3:
				System.out.println(in.readLine());	// gets enterAmount msg from server
				userInput = getIntegerInput();
				out.println(userInput);
				break;
			}
		} while (menuOption != 4);

		out.close();
		in.close();
		ATMSocket.close();
	}
}
