package org.apache.jcs.utils.servlet.session;

import java.io.Serializable;

/**
 * Session information wrapper.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class SessionInfo implements Serializable
{

    final long creationTime;
    long lastAccessedTime;

    /**
     * At first login this might be twice the set amount, depending on the
     * update ration.
     */
    int maxInactiveInterval = 60 * 60 * 1000;
    // 30 minutes after first hour

    /** Constructor for the SessionInfo object */
    SessionInfo()
    {
        lastAccessedTime = creationTime = System.currentTimeMillis();
    }


    /** Prints data to string. */
    public String toString()
    {
        return "[creationTime=" + creationTime
             + ", lastAccessedTime=" + lastAccessedTime
             + ", maxInactiveInterval=" + maxInactiveInterval
             + "]"
            ;
    }

}
// end SessionInfo
