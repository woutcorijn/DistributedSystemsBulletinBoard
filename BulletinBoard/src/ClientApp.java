import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) {
        try {
            // Get the bulletin board from RMI registry
            Registry registry = LocateRegistry.getRegistry("localhost", 6500);
            BulletinBoard board = (BulletinBoard) registry.lookup("BulletinBoard");

            LoginGUI loginGUI = new LoginGUI();

            while (!loginGUI.isReady()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            String clientName = loginGUI.getClientName();
            String otherClientName = loginGUI.getOtherClientName();

            ClientImpl client = new ClientImpl(clientName, board);

            if (!Bump.loadConnection(client, otherClientName)) {
                Bump.setupBump(client, otherClientName);
            }

            Bump.loadConnection(client, otherClientName);

            System.out.println("Connection established between " + clientName + " and " + otherClientName);

            // Create the GUI for chat
            ChatClientGUI gui = new ChatClientGUI(client, otherClientName);

            // Thread to handle receiving messages
            Thread receiveThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String receivedMessage = null;
                    try {
                        receivedMessage = client.receiveMessage(otherClientName);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                    if (receivedMessage != null) {
                        gui.displayReceivedMessage(receivedMessage);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            // Start the receiving thread
            receiveThread.start();

            // Main thread just starts the GUI
            // No need for additional user input handling here, as the GUI manages it

            // Wait for the user to quit
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String userInput = scanner.nextLine();

                // Exit condition
                if ("exit".equalsIgnoreCase(userInput)) {
                    System.out.println("Exiting chat...");
                    receiveThread.interrupt();
                    break;
                }
            }

            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
