package org.apache.jcs.utils.servlet.session;

/**
 * Constants used by the session tracker.
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface ISessionConstants
{
    /*
     * probably not necessary
     */
    /** Description of the Field */
    public final static String SESS_CACHE_NAME = "sessionCache";
    /** Description of the Field */
    public final static int SESS_CACHE_SIZE = 2000;
    /** Description of the Field */
    public final static String SESS_INFO_CACHE_NAME = "sessionInfoCache";
    /** Description of the Field */
    public final static int SESS_INFO_CACHE_SIZE = 2000;
    // 30 minutes after first hour
    /** Description of the Field */
    public final static int DFLT_INACTIVE_INTERVAL = 60 * 60 * 1000;
}

