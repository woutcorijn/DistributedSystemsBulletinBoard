import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

public class ClientImpl extends UnicastRemoteObject implements Client {
    private final String name;
    private final BulletinBoard board;
    private final SecureRandom srng;
    private Map<String, Connection> connections;

    public ClientImpl(String name, BulletinBoard board) throws RemoteException {
        this.name = name;
        this.board = board;
        srng = new SecureRandom();
        connections = new HashMap<>();
    }

    @Override
    public void sendMessage(String receiver, String message) throws RemoteException {
        Connection connection = connections.get(receiver);
        //System.out.println("Connection: " + connection);
        if (connection == null) {
            return;
        }
        int newIndex = srng.nextInt(board.size());
        ByteBuffer buffer = ByteBuffer.allocate(board.size());
        buffer.putInt(newIndex);
        byte[] newIndexToBytes = buffer.array();

        byte[] newTag = generateNewTag();
        byte[] messageToBytes = message.getBytes();

        byte[] u = combineByteArrays(messageToBytes, newIndexToBytes, newTag);

        try {
            // Encrypt the message
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, connection.getKeySend());
            byte[] encryptedMessage = cipher.doFinal(u);
            String messageToBase64 = Base64.getEncoder().encodeToString(encryptedMessage);
            //System.out.println("Encrypted Message: " + messageToBase64);
            String proofOfWork = generateProofOfWork(receiver + ":" + connection.getIndexSend(), 4);

            board.addMessage(connection.getIndexSend(), messageToBase64, connection.getTagSend(), proofOfWork);

            connection.setNewSendTag(newTag);
            connection.setNewSendIndex(newIndex);
            connection.setNewSendKey(deriveNewKey(connection.getKeySend()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String receiveMessage(String sender) throws RemoteException {
        Connection connection = connections.get(sender);
        //System.out.println("Connection: " + connection);
        if (connection == null) {
            return null;
        }
        try {
            String encryptedMessage = board.getMessage(connection.getIndexReceive(), connection.getTagReceive());
            //System.out.println("Encrypted Message: " + encryptedMessage);
            if (encryptedMessage != null) {
                // Decrypt the message
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, connection.getKeyReceive());
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));

                int messageLength = decryptedBytes.length - 32 - board.size(); // message - tag - index
                byte[] messageBytes = Arrays.copyOfRange(decryptedBytes, 0, messageLength);
                byte[] newIndexBytes = Arrays.copyOfRange(decryptedBytes, messageLength, messageLength + board.size());
                byte[] newTagBytes = Arrays.copyOfRange(decryptedBytes, messageLength + board.size(), decryptedBytes.length);

                ByteBuffer buffer = ByteBuffer.wrap(newIndexBytes);
                int newIndex = buffer.getInt();

                String message = new String(messageBytes);

                connection.setNewReceiveIndex(newIndex);
                connection.setNewReceiveTag(newTagBytes);
                connection.setNewReceiveKey(deriveNewKey(connection.getKeyReceive()));

                //System.out.println("Decrypted Message: " + message);
                //System.out.println("New Index: " + newIndex);
                //System.out.println("New Tag: " + Arrays.toString(newTagBytes));
                return message;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // No message found
    }


    public static SecretKey deriveNewKey(SecretKey previousKey) throws Exception {
        // Use SHA-256 to hash the previous key material
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] previousKeyBytes = previousKey.getEncoded();
        byte[] hash = digest.digest(previousKeyBytes);

        return new SecretKeySpec(hash, "AES");

    }

    private byte[] generateNewTag() {
        byte[] tag = new byte[32];
        srng.nextBytes(tag);
        return tag;
    }

    public int getBoardSize() throws RemoteException {
        return board.size();
    }

    public String getName(){
        return name;
    }

    public void addConnection(Connection connection) {
        connections.put(connection.getName(), connection);
    }

    public boolean hasConnection(String name) {
        return connections.containsKey(name);
    }

    private static byte[] combineByteArrays(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }
        byte[] combined = new byte[totalLength];

        int currentPosition = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, combined, currentPosition, array.length);
            currentPosition += array.length;
        }

        return combined;
    }

    private String generateProofOfWork(String token, int difficulty) throws NoSuchAlgorithmException {
        String nonce;
        String hash;
        int leadingZeros;

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        do {
            nonce = Long.toString(System.nanoTime());
            hash = toHexString(digest.digest((token + nonce).getBytes(StandardCharsets.UTF_8)));
            leadingZeros = countLeadingZeros(hash);
        } while (leadingZeros < difficulty);

        return token + ":" + nonce; // Token with nonce
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
