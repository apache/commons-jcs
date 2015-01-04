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

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

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
public class CacheFileDAOTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());

    public void test() {
        log.debug("testing cache directory "
                + CacheFileUtils.inst.getCacheDir("testCache").getAbsolutePath());
        log.debug("remove Cache directory");
        assertTrue(CacheFileUtils.inst.rmCacheDir("testCache"));
        log.debug("create Cache directory");
        assertTrue(CacheFileUtils.inst.mkCacheDirs("testCache"));

        log.debug("test writeCacheItem");
        byte[] ba1 = {1, 2, 3, 4};
        CacheFileDAO.inst.writeCacheItem("testCache",
                CacheFileContentType.JAVA_SERIALIZATION, "key1", ba1);
        byte[] ba2 = {'a', 'b', 'c', 'd'};
        CacheFileDAO.inst.writeCacheItem("testCache",
                CacheFileContentType.XML_ENCODER, "key2", ba2);

        log.debug("test readCacheItem");
        byte[] ba1r = CacheFileDAO.inst.readCacheItem("testCache", "key1").getContent();
        assertTrue(Arrays.equals(ba1, ba1r));
        byte[] ba2r = (byte[]) CacheFileDAO.inst.readCacheItem("testCache", "key2").getContent();
        assertTrue(Arrays.equals(ba2, ba2r));

        log.debug("test removeCacheItem");
        assertTrue(CacheFileDAO.inst.removeCacheItem("testCache", "key1"));
        assertTrue(CacheFileDAO.inst.removeCacheItem("testCache", "key2"));
        assertFalse(CacheFileDAO.inst.removeCacheItem("testCache", "key3"));
    }
    public void testCorruptedFile() throws Exception {
        log.debug("create testCacheCorrupt Cache directory");
        CacheFileUtils.inst.mkCacheDirs("testCacheCorrupt");

        log.debug("test readCacheItem missing content");
        CacheFileContent cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyx");
        byte[] ba = cfc == null ? null : cfc.getContent();
        assertTrue(ba == null);

        log.debug("test readCacheItem with corrupted hash code");
        File file = CacheFileUtils.inst.getCacheFile("testCacheCorrupt", "keyy");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        byte[] ba2 = {1, 2, 3, 4};
        cfc = CacheFileContent.getInstance(CacheFileContentType.JAVA_SERIALIZATION, ba2);
        cfc.setContentHashCode(0);
        cfc.write(raf);
        raf.close();

        cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyy");
        byte[] ba2i = cfc == null ? null : cfc.getContent();
        assertTrue(ba2i == null);

        log.debug("test readCacheItem with corrupted length");
        file.delete();
        raf = new RandomAccessFile(file, "rw");
        cfc = CacheFileContent.getInstance(CacheFileContentType.JAVA_SERIALIZATION, ba2);
        cfc.setContentLength(100);
        cfc.write(raf);
        raf.close();

        cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyy");
        ba2i = cfc == null ? null : cfc.getContent();
        assertTrue(ba2i == null);

        log.debug("test readCacheItem with corrupted content");
        file.delete();
        raf = new RandomAccessFile(file, "rw");
        cfc = CacheFileContent.getInstance(CacheFileContentType.JAVA_SERIALIZATION, ba2);
        cfc.setContent(new byte[] {'a', 'b', 'c', 'd', 'e'});
        cfc.write(raf);
        raf.close();

        cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyy");
        ba2i = cfc == null ? null : cfc.getContent();
        assertTrue(ba2i == null);

        log.debug("test readCacheItem with appended content");
        file.delete();
        raf = new RandomAccessFile(file, "rw");
        cfc = CacheFileContent.getInstance(CacheFileContentType.JAVA_SERIALIZATION, ba2);
        cfc.setContent(new byte[] {1, 2, 3, 4, 5});
        cfc.write(raf);
        raf.close();

        cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyy");
        ba2i = cfc == null ? null : cfc.getContent();
        assertTrue(ba2i == null);

        log.debug("test readCacheItem with content less than min length");
        file.delete();
        raf = new RandomAccessFile(file, "rw");
        cfc = CacheFileContent.getInstance(CacheFileContentType.JAVA_SERIALIZATION, ba2);
        cfc.setContent(null);
        cfc.write(raf);
        raf.close();

        cfc = CacheFileDAO.inst.readCacheItem("testCacheCorrupt", "keyy");
        ba2i = cfc == null ? null : cfc.getContent();
        assertTrue(ba2i == null);

        log.debug(CacheFileDAO.inst.toString());

    }
}
