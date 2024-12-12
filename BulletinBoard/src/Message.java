public class Message {
    private final String value;
    private final byte[] tag;

    public Message(String value, byte[] tag) {
        this.value = value;
        this.tag = tag;
    }

    public String getValue() {
        return value;
    }

    public byte[] getTag() {
        return tag;
    }
}
