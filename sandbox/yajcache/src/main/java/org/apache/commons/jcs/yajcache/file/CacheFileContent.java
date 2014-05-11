package org.apache.commons.jcs.yajcache.file;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.yajcache.lang.annotation.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * Cache File Content which represents the file persistence format
 * of a cache item.
 *
 *<pre>
 * File format:
 * &lt;FileContentType&gt;    : byte
 * &lt;ByteArrayLength&gt;    : int
 * &lt;ByteArrayHashCode&gt;  : int
 * &lt;ByteArray&gt;          : byte[]
 *</pre>
 *
 * @author Hanson Char
 */
@CopyRightApache
public class CacheFileContent {
    public static final CacheFileContent CORRUPTED = CacheFileContentCorrupted.inst;
    /**
     * Minimum File Length =
     * contentType (1 byte) + contentLength (4 bytes) + contentHashCode (4 bytes)
     */
    static final int MIN_FILE_LENGTH = 1 + 4 + 4;
    private byte contentType;
    private int contentLength;
    private int contentHashCode;

    private byte[] content;

    protected CacheFileContent() {}

    /**
     * Constructs an instance of CacheFileContent from the given content type
     * and content.
     */
    private CacheFileContent(
            @NonNullable CacheFileContentType contentType,
            @NonNullable byte[] content)
    {
        this.contentType = contentType.toByte();
        this.content = content;
        this.contentLength = this.content.length;
        this.contentHashCode = Arrays.hashCode(this.content);
    }
    /**
     * Write the current cache file content to the given random access file.
     */
    void write(@NonNullable RandomAccessFile raf) throws IOException {
        // File content type.
        raf.writeByte(this.contentType);
        // Byte array length.
        raf.writeInt(this.contentLength);
        // Byte array hashcode.
        raf.writeInt(this.contentHashCode);
        // Byte array.
        raf.write(this.content);
    }

    @NonNullable public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content == null ? new byte[0] : content;
    }

    public byte getContentType() {
        return contentType;
    }

    public void setContentType(byte contentType) {
        this.contentType = contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentHashCode() {
        return contentHashCode;
    }

    void setContentHashCode(int contentHashCode) {
        this.contentHashCode = contentHashCode;
    }
    /**
     * Returns true iff the current hash code is consistent with
     * the content.
     */
    public boolean isValid() {
        int hash = Arrays.hashCode(this.content);
        return hash == this.contentHashCode;
    }
    /**
     * Returns an instance of CacheFileContent from the given content type
     * and content.
     */
    @NonNullable
    static CacheFileContent getInstance(
            @NonNullable CacheFileContentType contentType,
            @NonNullable byte[] content)
        throws IOException
    {
        return new CacheFileContent(contentType, content);
    }
    /**
     * Returns an instance of CacheFileContent from the given random access file;
     */
    @NonNullable
    static CacheFileContent getInstance(@NonNullable RandomAccessFile raf)
        throws IOException
    {
        CacheFileContent cfc = new CacheFileContent();
        cfc.setContentType(raf.readByte());
        final int len = raf.readInt();
        cfc.setContentLength(len);
        cfc.setContentHashCode(raf.readInt());
        byte[] ba = new byte[len];
        // Byte array.
        raf.readFully(ba);
        cfc.setContent(ba);
        return cfc;
    }
    /** Returns the deserialized content. */
    public @NonNullable Object deserialize() {
        return CacheFileContentType.fromByte(this.contentType).deserialize(this.content);
    }
}
