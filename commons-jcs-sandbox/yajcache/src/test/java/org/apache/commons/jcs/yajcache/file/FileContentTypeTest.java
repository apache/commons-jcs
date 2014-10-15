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

import junit.framework.TestCase;

import org.apache.commons.jcs.yajcache.lang.annotation.CopyRightApache;
import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TestOnly
public class FileContentTypeTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());
    /**
     * Test of toByte method, of class org.apache.commons.jcs.yajcache.config.FileContentType.
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
