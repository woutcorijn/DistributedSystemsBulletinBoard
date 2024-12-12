import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            // Create the bulletin board server with 10 cells
            BulletinBoardImpl board = new BulletinBoardImpl(10);

            // Bind the bulletin board to RMI registry
            Registry registry = LocateRegistry.createRegistry(6500);
            registry.rebind("BulletinBoard", board);
            System.out.println("Bulletin Board Server is ready on port 1099");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
