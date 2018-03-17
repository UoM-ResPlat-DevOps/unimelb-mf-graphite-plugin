package unimelb.graphite;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.Date;

import unimelb.io.util.StreamUtils;

public class Metric {

    private String _path;
    private Date _timestampDate;
    private Object _value;

    public Metric(String path, Date timestampDate, Object value) {
        _path = path;
        _timestampDate = timestampDate;
        _value = value;
    }

    public Metric(String path, long time, Object value) {
        this(path, new Date(time), value);
    }

    public Metric(String path, int timestamp, Object value) {
        this(path, ((long) timestamp) * 1000L, value);
    }

    public int timestamp() {
        return (int) (_timestampDate.getTime() / 1000L);
    }

    public long timestampMillsecs() {
        return _timestampDate.getTime();
    }

    public Date timestampDate() {
        return _timestampDate;
    }

    public String path() {
        return _path;
    }

    public String value() {
        return String.valueOf(_value);
    }

    public void sendPlainText(String host, int port) throws IOException {
        Socket socket = new Socket(InetAddress.getByName(host), port);
        try {
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            try {
                writePlainText(out);
            } finally {
                out.close();
            }
        } finally {
            socket.close();
        }
    }

    public void writePlainText(OutputStream out) throws IOException {
        out.write(toPlainText().getBytes());
        out.write('\n');
        out.flush();
    }

    public String toPlainText() {
        return String.format("%s %s %d", path(), value(), timestamp());
    }

    public void sendPickle(String host, int port) throws IOException {
        Socket socket = new Socket(InetAddress.getByName(host), port);
        try {
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            try {
                writePickle(out);
            } finally {
                out.close();
            }
        } finally {
            socket.close();
        }
    }

    public void writePickle(OutputStream out) throws IOException {
        byte[] payload = toPicklePayload();
        StreamUtils.writeUnsignedInt(out, payload.length, ByteOrder.BIG_ENDIAN);
        out.write(payload);
        out.flush();
    }

    public byte[] toPicklePayload() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writePicklePayload(baos);
            return baos.toByteArray();
        } finally {
            baos.close();
        }
    }

    void writePicklePayload(OutputStream out) throws IOException {
        int ics = 0; // index of char sequence
        out.write(0x80);
        out.write(0x02); // protocol: 2
        out.write(']');
        out.write('q');
        out.write(ics++);
        writePicklePayload(out, ics);
        out.write('a'); // APPEND
        out.write('.'); // STOP
    }

    int writePicklePayload(OutputStream out, int ics) throws IOException {
        writePickleString(out, _path);
        out.write('q');
        out.write(ics++);
        writePickleInt(out, timestamp());
        writePickleString(out, value());
        out.write('q');
        out.write(ics++);
        out.write(0x86); // TUPLE2
        out.write('q');
        out.write(ics++);
        out.write(0x86); // TUPLE2
        out.write('q');
        out.write(ics++);
        return ics;
    }

    public static void writePickleString(OutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes();
        out.write('X');
        StreamUtils.writeInt(out, bytes.length, ByteOrder.LITTLE_ENDIAN);
        out.write(bytes);
    }

    public static void writePickleInt(OutputStream out, int value) throws IOException {
        if (value >= 0 && value <= 255) {
            out.write('K');
            out.write(value);
        } else if (value >= 256 && value <= 65535) {
            out.write('M');
            StreamUtils.writeShort(out, (short) value, ByteOrder.LITTLE_ENDIAN);
        } else {
            out.write('J');
            StreamUtils.writeInt(out, value, ByteOrder.LITTLE_ENDIAN);
        }
    }

}
