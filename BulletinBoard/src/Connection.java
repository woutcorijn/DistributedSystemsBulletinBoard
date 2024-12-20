import javax.crypto.SecretKey;

public class Connection {
    private final String name;
    private SecretKey keySend;
    private SecretKey keyReceive;
    private int indexSend;
    private int indexReceive;
    private byte[] tagSend;
    private byte[] tagReceive;

    public Connection(String name, SecretKey keySend, SecretKey keyReceive, int indexSend, int indexReceive, byte[] tagSend, byte[] tagReceive) {
        this.name = name;
        this.keySend = keySend;
        this.keyReceive = keyReceive;
        this.indexSend = indexSend;
        this.indexReceive = indexReceive;
        this.tagSend = tagSend;
        this.tagReceive = tagReceive;
    }

    public String getName() {
        return name;
    }

    public SecretKey getKeySend() {
        return keySend;
    }

    public SecretKey getKeyReceive() {
        return keyReceive;
    }

    public int getIndexSend() {
        return indexSend;
    }

    public int getIndexReceive() {
        return indexReceive;
    }

    public byte[] getTagSend() {
        return tagSend;
    }

    public byte[] getTagReceive() {
        return tagReceive;
    }

    public void setNewSendTag(byte[] newTag) {
        this.tagSend = newTag;
    }
    public void setNewSendIndex(int newIndex) {
        this.indexSend = newIndex;
    }

    public void setNewSendKey(SecretKey secretKey) {
        this.keySend = secretKey;
    }

    public void setNewReceiveIndex(int newIndex) {
        this.indexReceive = newIndex;
    }

    public void setNewReceiveTag(byte[] newTagBytes) {
        this.tagReceive = newTagBytes;
    }

    public void setNewReceiveKey(SecretKey secretKey) {
        this.keyReceive = secretKey;
    }

    @Override
    public String toString() {
        return "Connection{" +
                "name='" + name + '\'' +
                ", keySend=" + keySend +
                ", keyReceive=" + keyReceive +
                ", indexSend=" + indexSend +
                ", indexReceive=" + indexReceive +
                ", tagSend=" + tagSend +
                ", tagReceive=" + tagReceive +
                '}';
    }
}
