
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jcs.yajcache.file;

import org.apache.jcs.yajcache.annotate.*;

/**
 * Cache File Content Type.  A cache file represents the file persistence
 * format of a cache item.
 * Currently the content of a cache item can be persisted into an array of
 * bytes via either Java Serialization, or the XMLEncoder.
 *
 * @author Hanson Char
 */
@CopyRightApache
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
                throw new IllegalStateException("Should never happen");
        }
    }
    /** 
     * Returns the corresponding CacheFileContentType enum instance 
     * from the given byte value. 
     */
    @NonNullable 
    public static CacheFileContentType fromByte(byte b) {
        switch(b) {
            case 0:
                return JAVA_SERIALIZATION;
            case 1:
                return XML_ENCODER;
            default:
                throw new IllegalArgumentException("Unsupported b="+b);
        }
    }
}
