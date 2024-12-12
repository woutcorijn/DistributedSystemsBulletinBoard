import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
    void sendMessage(String receiver, String message) throws RemoteException;
    String receiveMessage(String sender) throws RemoteException;

    String getName() throws RemoteException;
    int getBoardSize() throws RemoteException;
    void addConnection(Connection connection) throws RemoteException;
}
