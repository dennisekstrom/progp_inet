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

	private static int getMenuOption(BufferedReader in) throws IOException {
		int c;
		while ((c = in.read()) != '\r') {
			System.out.print((char) c);
		}
		Scanner scanner = new Scanner(System.in);
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
		int menuOption = getMenuOption(in);
		Scanner scanner = new Scanner(System.in);
		int userInput;
		out.println(menuOption);
		while (menuOption != 4) {
			switch (menuOption) {
			case 1:
				System.out.println(in.readLine());
				System.out.println(in.readLine());
				System.out.print("> ");
				menuOption = scanner.nextInt();
				out.println(menuOption);
				
				break;
			case 2:
				//$FALL-THROUGH$
			case 3:
				System.out.println(in.readLine());
				userInput = scanner.nextInt();
				out.println(userInput);
				String str;
				do {
					str = in.readLine();
					System.out.println(str);
				} while (!str.startsWith("(1)"));
				System.out.print("> ");
				menuOption = scanner.nextInt();
				out.println(menuOption);
				break;
			default:
				menuOption = scanner.nextInt();
				break;
			}
		}

		out.close();
		in.close();
		ATMSocket.close();
	}
}
