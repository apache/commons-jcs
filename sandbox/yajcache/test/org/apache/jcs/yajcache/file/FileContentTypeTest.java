/*
 * Copyright 2005 The Apache Software Foundation.
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

import junit.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jcs.yajcache.lang.annotation.*;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TestOnly
public class FileContentTypeTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());
    /**
     * Test of toByte method, of class org.apache.jcs.yajcache.config.FileContentType.
     */
    public void test() {
        log.debug("test toByte");
        Byte bJavaSerialization = CacheFileContentType.JAVA_SERIALIZATION.toByte();
        Byte bXmlEncoder = CacheFileContentType.XML_ENCODER.toByte();
        assertFalse(bJavaSerialization == bXmlEncoder);
        log.debug("test fromByte");
        assertTrue(CacheFileContentType.JAVA_SERIALIZATION == CacheFileContentType.fromByte(bJavaSerialization));
        assertTrue(CacheFileContentType.XML_ENCODER == CacheFileContentType.fromByte(bXmlEncoder));
        log.debug("test fromByte with unknown type");
        try {
            CacheFileContentType.fromByte((byte)99);
            assert false;
        } catch(IllegalArgumentException ex) {
        }
    }
}
