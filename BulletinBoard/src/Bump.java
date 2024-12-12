import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Bump {

    static void setupBump(ClientImpl alice, String bob) throws NoSuchAlgorithmException, IOException {
        SecureRandom srng = new SecureRandom();

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey keyab = keyGen.generateKey();

        KeyGenerator keyGen2 = KeyGenerator.getInstance("AES");
        keyGen2.init(256);
        SecretKey keyba = keyGen2.generateKey();

        int idxab = srng.nextInt(alice.getBoardSize());
        int idxba = srng.nextInt(alice.getBoardSize());

        byte[] tagab = new byte[32];
        srng.nextBytes(tagab);

        byte[] tagba = new byte[32];
        srng.nextBytes(tagba);

        saveConnection(alice.getName(), bob, keyab, keyba, idxab, idxba, tagab, tagba);
    }

    private static void saveConnection(String aliceName, String bobName, SecretKey keyab, SecretKey keyba, int idxab, int idxba, byte[] tagab, byte[] tagba) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode connection = mapper.createObjectNode();

        connection.put("aliceName", aliceName);
        connection.put("bobName", bobName);
        connection.put("keyab", java.util.Base64.getEncoder().encodeToString(keyab.getEncoded()));
        connection.put("keyba", java.util.Base64.getEncoder().encodeToString(keyba.getEncoded()));
        connection.put("idxab", idxab);
        connection.put("idxba", idxba);
        connection.put("tagab", java.util.Base64.getEncoder().encodeToString(tagab));
        connection.put("tagba", java.util.Base64.getEncoder().encodeToString(tagba));

        String connectionFileName = aliceName + "-" + bobName + ".json";
        File file = new File(connectionFileName);

        mapper.writeValue(file, connection);
    }

    static boolean loadConnection(ClientImpl client, String otherClientName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String keyAliceBob = client.getName() + "-" + otherClientName + ".json";
        String keyBobAlice = otherClientName + "-" + client.getName() + ".json";

        File fileAliceBob = new File(keyAliceBob);
        File fileBobAlice = new File(keyBobAlice);

        if (!fileAliceBob.exists() && !fileBobAlice.exists()) {
            System.err.println("Connection file not found.");
            return false;
        }

        File file = fileAliceBob.exists() ? fileAliceBob : fileBobAlice;
        ObjectNode connection = (ObjectNode) mapper.readTree(file);

        SecretKey keyab = new javax.crypto.spec.SecretKeySpec(java.util.Base64.getDecoder().decode(connection.get("keyab").asText()), "AES");
        SecretKey keyba = new javax.crypto.spec.SecretKeySpec(java.util.Base64.getDecoder().decode(connection.get("keyba").asText()), "AES");

        int idxab = connection.get("idxab").asInt();
        int idxba = connection.get("idxba").asInt();

        byte[] tagab = java.util.Base64.getDecoder().decode(connection.get("tagab").asText());
        byte[] tagba = java.util.Base64.getDecoder().decode(connection.get("tagba").asText());

        if (fileAliceBob.exists()) {
            client.addConnection(new Connection(otherClientName, keyab, keyba, idxab, idxba, tagab, tagba));
        } else if (fileBobAlice.exists()) {
            client.addConnection(new Connection(otherClientName, keyba, keyab, idxba, idxab, tagba, tagab));
        }
        return true;
    }
}
