import java.nio.charset.StandardCharsets;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class BulletinBoardImpl extends UnicastRemoteObject implements BulletinBoard {
    private final Set<Message>[] board;  // An array of sets to hold messages
    private final int n;  // Size of the board
    private final ReentrantLock[] locks;  // Locks for concurrency control
    private static final int PoW_DIFFICULTY = 4;

    public BulletinBoardImpl(int n) throws RemoteException {
        this.n = n;
        this.board = new Set[n];
        this.locks = new ReentrantLock[n];

        for (int i = 0; i < this.n; i++) {
            board[i] = new HashSet<>();
            locks[i] = new ReentrantLock();
        }
    }

    @Override
    public void addMessage(int idx, String value, byte[] tag, String proofOfWork) throws RemoteException {
        if (!validateProofOfWork(proofOfWork)) {
            throw new RemoteException("Invalid proof of work.");
        }
        locks[idx].lock();
        try {
            System.out.println("added message");
            board[idx].add(new Message(value, tag));
        } finally {
            locks[idx].unlock();
        }

    }

    @Override
    public String getMessage(int idx, byte[] tag) throws RemoteException {
        locks[idx].lock();
        try {
            for (Message msg : board[idx]) {
                if (Arrays.equals(msg.getTag(), tag)) {
                    board[idx].remove(msg);
                    return msg.getValue();
                }
            }
            return null;  // No message found
        } finally {
            locks[idx].unlock();
        }
    }


    @Override
    public int size() {
        return n;
    }

    private boolean validateProofOfWork(String proofOfWork) {
        try {
            String[] parts = proofOfWork.split(":");
            if (parts.length < 3) return false;

            String token = parts[0] + ":" + parts[1];
            String nonce = parts[2];

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String hash = toHexString(digest.digest((token + nonce).getBytes(StandardCharsets.UTF_8)));

            // Check if the hash satisfies the difficulty requirement
            return countLeadingZeros(hash) >= PoW_DIFFICULTY;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static int countLeadingZeros(String hash) {
        int count = 0;
        for (char c : hash.toCharArray()) {
            if (c == '0') count++;
            else break;
        }
        return count;
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
