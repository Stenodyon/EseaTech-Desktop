package eseatech;

import com.fazecast.jSerialComm.SerialPort;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Registers a callback to provide new data
public class DataProvider
{
    private static final byte REQUEST_COMMAND = 'r';

    private SerialPort port;
    private MessageUnpacker unpacker;

    public DataProvider(SerialPort port) {
        this.port = port;
        port.openPort();
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        unpacker = MessagePack.newDefaultUnpacker(port.getInputStream());
    }

    public void close() {
        unpacker = null;
        port.closePort();
    }

    public boolean connectedTo(SerialPort otherPort) {
        return otherPort.getSystemPortName().equals(port.getSystemPortName());
    }

    public Map<String, Float>[] fetchData() throws IOException {
        sendFetchRequest();
        return unpackData();
    }

    public void clearBytes() {
        byte[] buffer = new byte[port.bytesAvailable()];
        port.readBytes(buffer, buffer.length);
    }

    private void sendFetchRequest() {
        sendByte(REQUEST_COMMAND);
    }

    private void sendByte(byte value) {
        byte[] command = new byte[1];
        command[0] = value;
        port.writeBytes(command, command.length);
    }

    private Map<String, Float>[] unpackData() throws IOException {
        int length = unpacker.unpackArrayHeader();
        Map<String, Float>[] entries = new Map[length];

        for (int i = 0; i < length; i++) {
            entries[i] = unpackMap(unpacker);
        }

        return entries;
    }

    private Map<String, Float> unpackMap(MessageUnpacker unpacker) throws IOException {
        int length = unpacker.unpackMapHeader();
        Map<String, Float> map = new HashMap<>(length);

        for (int i = 0; i < length; i++) {
            String key = unpacker.unpackString();
            float value = unpacker.unpackFloat();
            map.put(key, value);
        }

        return map;
    }
}
