/*
 * CacheManagerTest.java
 * JUnit based test
 *
 * Created on 18 January 2005, 03:12
 */

package net.sf.yajcache.soft;

import junit.framework.*;
import net.sf.yajcache.core.ICacheSafe;
import net.sf.yajcache.core.SafeCacheManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hanson Char
 */
public class SoftRefCacheTest extends TestCase {
    private Log log = LogFactory.getLog(this.getClass());
    
    public void testSoftRefCache() throws Exception {
        ICacheSafe<byte[]> c = SafeCacheManager.inst.getCache("bytesCache", byte[].class);

        for (int h=0; h < 10; h++) {
            for (int i=h*10, max=i+10; i < max; i++) {
                log.debug("put i="+i);
                c.put(String.valueOf(i), new byte[100*1024]);
//                c.put("0", new byte[100*1024]);
                c.get("0");
            }
            for (int i=0; i < 10; i++) {
                log.debug("get i="+i +":"+ c.get(String.valueOf(i)));
            }
//            for (int i=0; i < h*10+10; i++) {
//                log.debug("get i="+i +":"+ c.get(String.valueOf(i)));
//            }
        }
        log.debug("size: " + c.size());
        SoftRefCache sc = (SoftRefCache)c;
        log.debug("count: " + sc.getCollectorCount());
        log.debug(SoftRefCacheCleaner.inst.toString());
        
//        for (int i=0; i < 100; i++) {
//            log.debug("get i="+i +":"+ c.get(String.valueOf(i)));
//        }
        log.debug("sleeping for 5 secs");
        Thread.sleep(5*1000);

        for (int i=0; i < 100; i++) {
            log.debug("get i="+i +":"+ c.get(String.valueOf(i)));
        }
        log.debug("size: " + c.size());
        log.debug("count: " + sc.getCollectorCount());
        log.debug(SoftRefCacheCleaner.inst.toString());
    }
}
