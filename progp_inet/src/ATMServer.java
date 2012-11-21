import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Viebrapadata
 */
public class ATMServer {

	private static int connectionPort = 8988;
	private static String welcomeMessage = "Welcome to Bank!";

	public static void main(String[] args) throws IOException {

		ServerSocket serverSocket = null;

		boolean listening = true;

		try {
			serverSocket = new ServerSocket(connectionPort);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + connectionPort);
			System.exit(1);
		}

		System.out.println("Bank started listening on port: " + connectionPort);

		ExecutorService es = Executors.newFixedThreadPool(2);
		Future<String> welcomeFuture = es.submit(getCallable());
		Future<?> threadFuture = es.submit(new ATMServerThread(serverSocket
				.accept()));

		while (listening) {
			if (welcomeFuture.isDone())
				welcomeFuture = es.submit(getCallable());

			if (threadFuture.isDone())
				es.submit(new ATMServerThread(serverSocket.accept()));
		}

		serverSocket.close();
	}

	private static Callable<String> getCallable() {
		return new Callable<String>() {

			Scanner scanner = new Scanner(System.in);

			@Override
			public String call() {
				System.out.println(Thread.currentThread());
				return scanner.nextLine();
			}
		};
	}
}