import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BulletinBoard extends Remote {
    void addMessage(int idx, String value, byte[] tag, String proofOfWork) throws RemoteException;
    String getMessage(int idx, byte[] tag) throws RemoteException;
    int size() throws RemoteException;
}