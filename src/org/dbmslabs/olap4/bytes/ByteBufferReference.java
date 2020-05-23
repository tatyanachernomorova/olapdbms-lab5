package org.dbmslabs.olap4.bytes;

import org.apache.lucene.util.BytesRef;

import java.nio.ByteBuffer;
import java.util.Objects;

public class ByteBufferReference extends AbstractBytesReference {

    private final ByteBuffer buffer;
    private final int length;

    public ByteBufferReference(ByteBuffer buffer) {
        this.buffer = buffer.slice();
        this.length = buffer.remaining();
    }

    @Override
    public byte get(int index) {
        return buffer.get(index);
    }

    @Override
    public int getInt(int index) {
        return buffer.getInt(index);
    }

    @Override
    public int indexOf(byte marker, int from) {
        final int remainingBytes = Math.max(length - from, 0);
        Objects.checkFromIndexSize(from, remainingBytes, length);
        if (buffer.hasArray()) {
            int startIndex = from + buffer.arrayOffset();
            int endIndex = startIndex + remainingBytes;
            final byte[] array = buffer.array();
            for (int i = startIndex; i < endIndex; i++) {
                if (array[i] == marker) {
                    return (i - buffer.arrayOffset());
                }
            }
            return -1;
        } else {
            return super.indexOf(marker, from);
        }
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public BytesReference slice(int from, int length) {
        Objects.checkFromIndexSize(from, length, this.length);
        buffer.position(from);
        buffer.limit(from + length);
        ByteBufferReference newByteBuffer = new ByteBufferReference(buffer);
        buffer.position(0);
        buffer.limit(this.length);
        return newByteBuffer;
    }

    /**
     * This will return a bytes ref composed of the bytes. If this is a direct byte buffer, the bytes will
     * have to be copied.
     *
     * @return the bytes ref
     */
    @Override
    public BytesRef toBytesRef() {
        if (buffer.hasArray()) {
            return new BytesRef(buffer.array(), buffer.arrayOffset(), length);
        }
        final byte[] copy = new byte[length];
        buffer.get(copy, 0, length);
        return new BytesRef(copy);
    }

    @Override
    public long ramBytesUsed() {
        return buffer.capacity();
    }
}