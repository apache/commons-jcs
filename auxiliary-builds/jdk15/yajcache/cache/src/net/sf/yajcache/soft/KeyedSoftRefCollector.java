/*
 * $Revision$ $Date$
 */

package net.sf.yajcache.soft;

import java.lang.ref.ReferenceQueue;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Collects and clears state cache entries implemented using Soft References.
 *
 * @author Hanson Char
 */
class KeyedSoftRefCollector<V> implements Runnable {
    private static final boolean debug = true;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    private volatile int count;
    private final ReferenceQueue<V> q;
    private final Map<String, KeyedSoftRef<V>> map;
    /** Creates a new instance of ReferenceQProcessor */
    KeyedSoftRefCollector(ReferenceQueue<V> q, Map<String, KeyedSoftRef<V>> map) {
        this.q = q;
        this.map = map;
    }
    /**
     * Removes stale entries from the cache map collected by GC.
     * Thread safetyness provided by ReferenceQueue.
     */
    public void run() {
//        if (debug)
//            log.debug("Run...");
        KeyedSoftRef<V> ksr;
        
        while ((ksr = (KeyedSoftRef<V>)this.q.poll()) != null) {
            String key = ksr.getKey();
            if (debug)
                log.debug("Remove stale entry with key=" + key);
//            map.remove(key);
            SoftRefCacheCleaner.inst.cleanupKey(map, key);
            ksr.clear();
            count++;
        }        
    }
    public int getCount() {
        return count;
    }
}
