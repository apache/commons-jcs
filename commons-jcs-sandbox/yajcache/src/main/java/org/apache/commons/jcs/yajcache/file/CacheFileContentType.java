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
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

/**
 * Cache File Content Type.  A cache file represents the file persistence
 * format of a cache item.
 * Currently the content of a cache item can be persisted into an array of
 * bytes via either Java Serialization, or the XMLEncoder.
 *
 * @author Hanson Char
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum CacheFileContentType {
    JAVA_SERIALIZATION,
    XML_ENCODER;

    /** Returns a single byte representing the content type. */
    public byte toByte() {
        switch(this) {
            case JAVA_SERIALIZATION:
                return 0;
            case XML_ENCODER:
                return 1;
            default:
                throw new AssertionError(this);
        }
    }
    /**
     * Returns the corresponding CacheFileContentType enum instance
     * from the given byte value.
     */

    public static @NonNullable CacheFileContentType fromByte(byte b) {
        switch(b) {
            case 0:
                return JAVA_SERIALIZATION;
            case 1:
                return XML_ENCODER;
            default:
                throw new IllegalArgumentException("Unsupported b="+b);
        }
    }
    public byte[] serialize(Object obj) {
        switch(this) {
            case JAVA_SERIALIZATION:
                return SerializationUtils.serialize((Serializable)obj);
            case XML_ENCODER:
                return org.apache.commons.jcs.yajcache.util.BeanUtils.inst.toXmlByteArray(obj);
            default:
                throw new AssertionError(this);
        }
    }
    public Object deserialize(byte[] ba) {
        switch(this) {
            case JAVA_SERIALIZATION:
                return SerializationUtils.deserialize(ba);
            case XML_ENCODER:
                return org.apache.commons.jcs.yajcache.util.BeanUtils.inst.fromXmlByteArray(ba);
            default:
                throw new AssertionError(this);
        }
    }
}
