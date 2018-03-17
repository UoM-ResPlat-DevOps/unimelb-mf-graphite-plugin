package unimelb.io.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

public class StreamUtils {

    public static void writeUnsignedInt(OutputStream out, int value, ByteOrder byteOrder) throws IOException {
        writeUnsignedInt(out, Integer.toUnsignedLong(value), byteOrder);
    }

    public static void writeUnsignedInt(OutputStream out, long value, ByteOrder byteOrder) throws IOException {
        byteOrder = byteOrder == null ? ByteOrder.nativeOrder() : byteOrder;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            out.write((byte) ((value >>> 24) & 0xFF));
            out.write((byte) ((value >>> 16) & 0xFF));
            out.write((byte) ((value >>> 8) & 0xFF));
            out.write((byte) ((value >>> 0) & 0xFF));
        } else {
            out.write((byte) ((value >>> 0) & 0xFF));
            out.write((byte) ((value >>> 8) & 0xFF));
            out.write((byte) ((value >>> 16) & 0xFF));
            out.write((byte) ((value >>> 24) & 0xFF));
        }
    }

    public static void writeInt(OutputStream out, int value, ByteOrder byteOrder) throws IOException {
        byteOrder = byteOrder == null ? ByteOrder.nativeOrder() : byteOrder;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            out.write((byte) (value >>> 24));
            out.write((byte) (value >>> 16));
            out.write((byte) (value >>> 8));
            out.write((byte) (value));
        } else {
            out.write((byte) (value));
            out.write((byte) (value >>> 8));
            out.write((byte) (value >>> 16));
            out.write((byte) (value >>> 24));
        }
    }

    public static void writeShort(OutputStream out, short value, ByteOrder byteOrder) throws IOException {
        byteOrder = byteOrder == null ? ByteOrder.nativeOrder() : byteOrder;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            out.write((byte) (value >>> 8));
            out.write((byte) (value));
        } else {
            out.write((byte) (value));
            out.write((byte) (value >>> 8));
        }
    }
}
