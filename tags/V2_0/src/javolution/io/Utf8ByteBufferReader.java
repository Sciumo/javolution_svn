/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.io;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.Reader;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import javolution.lang.Text;

/**
 * <p> This class represents a non-blocking <code>java.nio.ByteBuffer</code> 
 *     reader (UTF-8 encoding).</p>
 *
 * <p> This reader can be used for efficient decoding of native byte 
 *     buffers (e.g. <code>MappedByteBuffer</code>), high-performance 
 *     messaging (no intermediate buffer), etc.</p>
 *     
 * <p> This reader supports surrogate <code>char</code> pairs (representing
 *     characters in the range [U+10000 .. U+10FFFF]). It can also be used
 *     to read characters unicodes (31 bits) directly
 *     (ref. {@link #read()}).</p>
 *
 * <p> Each invocation of one of the <code>read()</code> methods may cause one
 *     or more bytes to be read from the underlying byte buffer.
 *     The end of stream is reached when the byte buffer position and limit
 *     coincide.</p>
 *
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 2.0, November 26, 2004
 * @see     Utf8ByteBufferWriter
 */
public final class Utf8ByteBufferReader extends Reader {

    /**
     * Holds the byte buffer source.
     */
    private ByteBuffer _byteBuffer;

    /**
     * Default constructor.
     */
    public Utf8ByteBufferReader() {
    }

    /**
     * Sets the <code>ByteBuffer</code> to use for reading available bytes
     * from current buffer position.
     *
     * @param  byteBuffer the <code>ByteBuffer</code> source.
     * @return this UTF-8 reader.
     */
    public Utf8ByteBufferReader setByteBuffer(ByteBuffer byteBuffer) {
        _byteBuffer = byteBuffer;
        return this;
    }

    /**
     * Indicates if this stream is ready to be read.
     *
     * @return <code>true</code> if the byte buffer has remaining bytes to 
     *         read; <code>false</code> otherwise.
     * @throws  IOException if an I/O error occurs.
     */
    public boolean ready() throws IOException {
        if (_byteBuffer != null) {
            return _byteBuffer.hasRemaining();
        } else {
            throw new IOException("Reader closed");
        }
    }

    /**
     * Closes the stream. Once a stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously-closed stream, however, has no effect.
     *
     * @throws IOException if an I/O error occurs.
     */
    public void close() throws IOException {
        _byteBuffer = null;
    }

    /**
     * Reads a single character.  This method does not block, <code>-1</code>
     * is returned if the buffer's limit has been reached.
     *
     * @return the 31-bits Unicode of the character read, or -1 if there is 
     *         no more remaining bytes to be read.
     * @throws IOException if an I/O error occurs (e.g. incomplete 
     *         character sequence being read).
     */
    public int read() throws IOException {
        if (_byteBuffer != null) {
            if (_byteBuffer.hasRemaining()) {
                byte b = _byteBuffer.get();
                return (b >= 0) ? b : read2(b);
            } else {
                return -1;
            }
        } else {
            throw new IOException("Reader closed");
        }
    }

    // Reads one full character, throws CharConversionException if limit reached.
    private int read2(byte b) throws IOException {
        try {
            // Decodes UTF-8.
            if ((b >= 0) && (_moreBytes == 0)) {
                // 0xxxxxxx
                return b;
            } else if (((b & 0xc0) == 0x80) && (_moreBytes != 0)) {
                // 10xxxxxx (continuation byte)
                _code = (_code << 6) | (b & 0x3f); // Adds 6 bits to code.
                if (--_moreBytes == 0) {
                    return _code;
                } else {
                    return read2(_byteBuffer.get());
                }
            } else if (((b & 0xe0) == 0xc0) && (_moreBytes == 0)) {
                // 110xxxxx
                _code = b & 0x1f;
                _moreBytes = 1;
                return read2(_byteBuffer.get());
            } else if (((b & 0xf0) == 0xe0) && (_moreBytes == 0)) {
                // 1110xxxx
                _code = b & 0x0f;
                _moreBytes = 2;
                return read2(_byteBuffer.get());
            } else if (((b & 0xf8) == 0xf0) && (_moreBytes == 0)) {
                // 11110xxx
                _code = b & 0x07;
                _moreBytes = 3;
                return read2(_byteBuffer.get());
            } else if (((b & 0xfc) == 0xf8) && (_moreBytes == 0)) {
                // 111110xx
                _code = b & 0x03;
                _moreBytes = 4;
                return read2(_byteBuffer.get());
            } else if (((b & 0xfe) == 0xfc) && (_moreBytes == 0)) {
                // 1111110x
                _code = b & 0x01;
                _moreBytes = 5;
                return read2(_byteBuffer.get());
            } else {
                throw new CharConversionException("Invalid UTF-8 Encoding");
            }
        } catch (BufferUnderflowException e) {
            throw new CharConversionException("Incomplete Sequence");
        }
    }

    private int _code;

    private int _moreBytes;

    /**
     * Reads characters into a portion of an array.  This method does not 
     * block.
     *
     * <p> Note: Characters between U+10000 and U+10FFFF are represented
     *     by surrogate pairs (two <code>char</code>).</p>
     *
     * @param  cbuf the destination buffer.
     * @param  off the offset at which to start storing characters.
     * @param  len the maximum number of characters to read
     * @return the number of characters read,  or -1 if there is 
     *         no more remaining bytes to be read.
     * @throws IOException if an I/O error occurs.
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        if (_byteBuffer != null) {
            final int off_plus_len = off + len;
            for (int i = off; i < off_plus_len;) {
                if (_byteBuffer.hasRemaining()) {
                    byte b = _byteBuffer.get();
                    if (b >= 0) {
                        cbuf[i++] = (char) b; // Most common case.
                    } else {
                        if (i < off_plus_len - 1) { // Up to two 'char' can be read.
                            int code = read2(b);
                            if (code < 0x10000) {
                                cbuf[i++] = (char) code;
                            } else if (code <= 0x10ffff) { // Surrogates.
                                cbuf[i++] = (char) (((code - 0x10000) >> 10) + 0xd800);
                                cbuf[i++] = (char) (((code - 0x10000) & 0x3ff) + 0xdc00);
                            } else {
                                throw new CharConversionException(
                                        "Cannot convert U+"
                                                + Text.valueOf(code, 16)
                                                + " to char (code greater than U+10FFFF)");
                            }
                        } else { // Not enough space in destination (go back).
                            _byteBuffer.position(_byteBuffer.position() - 1);
                            return i - off;
                        }
                    }
                } else {
                    return i - off;
                }
            }
            return len;
        } else {
            throw new IOException("Reader closed");
        }
    }
}