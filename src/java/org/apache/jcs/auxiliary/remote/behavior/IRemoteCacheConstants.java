package org.apache.jcs.auxiliary.remote.behavior;

/**
 * Description of the Interface
 *
 * @author asmuts
 * @created January 15, 2002
 */
public interface IRemoteCacheConstants
{

    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_NAME = "remote.cache.service.name";
    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_VAL = IRemoteCacheService.class.getName();
    /** Mapping to props file value */
    public final static String TOMCAT_XML = "remote.tomcat.xml";
    /** Mapping to props file value */
    public final static String TOMCAT_ON = "remote.tomcat.on";
    /** Mapping to props file value */
    public final static String REMOTE_CACHE_SERVICE_PORT = "remote.cache.service.port";
    /** Mapping to props file value */
    public final static String REMOTE_LOCAL_CLUSTER_CONSISTENCY = "remote.cluster.LocalClusterConsistency";

    /** Mapping to props file value */
    public final static String REMOTE_ALLOW_CLUSTER_GET = "remote.cluster.AllowClusterGet";

}
