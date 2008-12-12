package org.apache.jcs.auxiliary.remote.http.behavior;

/** Constants used throughout the HTTP remote cache. */
public interface IRemoteHttpCacheConstants
{
    /** The prefix for cache server config. */
    public final static String HTTP_CACHE_SERVER_PREFIX = "jcs.remotehttpcache";

    /** All of the RemoteHttpCacheServiceAttributes can be configured this way. */
    public final static String HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX = HTTP_CACHE_SERVER_PREFIX
        + ".serverattributes";
}
