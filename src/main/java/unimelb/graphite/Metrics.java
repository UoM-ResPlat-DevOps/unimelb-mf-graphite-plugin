package unimelb.graphite;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import unimelb.io.util.StreamUtils;

public class Metrics implements Iterable<Metric> {

    public static final int DEFAULT_PLAINTEXT_SEND_INTERVAL = 0;

    private ArrayList<Metric> _metrics;

    public Metrics() {
        _metrics = new ArrayList<Metric>();
    }

    public void add(Metric... metrics) {
        if (metrics != null && metrics.length > 0) {
            for (Metric metric : metrics) {
                _metrics.add(metric);
            }
        }
    }

    public void add(Collection<Metric> metrics) {
        if (metrics != null) {
            for (Metric metric : metrics) {
                _metrics.add(metric);
            }
        }
    }

    public void addMetric(String path, Date time, Object value) {
        add(new Metric(path, time, value));
    }

    public void addMetric(String path, int timestamp, Object value) {
        add(new Metric(path, timestamp, value));
    }

    public void addMetric(String path, long time, Object value) {
        add(new Metric(path, time, value));
    }

    public void sendPlainText(String host, int port, int intervalMillisecs) throws IOException, InterruptedException {
        int n = _metrics.size();
        for (int i = 0; i < n; i++) {
            Metric metric = _metrics.get(i);
            metric.sendPlainText(host, port);
            if (n > 1 && intervalMillisecs > 0) {
                Thread.sleep(intervalMillisecs);
            }
        }
    }

    public void sendPlainText(String host, int port) throws IOException, InterruptedException {
        sendPlainText(host, port, DEFAULT_PLAINTEXT_SEND_INTERVAL);
    }

    public void sendPickle(String host, int port) throws IOException {
        if (_metrics.isEmpty()) {
            return;
        }
        if (_metrics.size() == 1) {
            _metrics.get(0).sendPickle(host, port);
            return;
        }
        Socket socket = new Socket(InetAddress.getByName(host), port);
        try {
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
            try {
                writePickle(out);
                out.flush();
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
        out.write(0x02); // PROTO: 2
        out.write(']'); // EMPTY_LIST
        out.write('q');
        out.write(ics++); // BINPUT: 0
        out.write('(');
        for (Metric metric : _metrics) {
            ics = metric.writePicklePayload(out, ics);
        }
        out.write('e'); // APPENDS (MARK at 5)
        out.write('.'); // STOP
    }

    public Iterator<Metric> iterator() {
        return _metrics.iterator();
    }

    public int size() {
        return _metrics.size();
    }

    public boolean isEmpty() {
        return _metrics.isEmpty();
    }

    public void clear() {
        _metrics.clear();
    }

}
