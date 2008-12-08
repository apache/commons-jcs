package org.apache.jcs.auxiliary.remote.http.behavior;

/** Constants used throughout the HTTP remote cache. */
public interface IRemoteHttpCacheConstants
{
    /** Get request type. */
    public static final byte REQUEST_TYPE_GET = 0;

    /** Get Multiple request type. */
    public static final byte REQUEST_TYPE_GET_MULTIPLE = 1;

    /** Get Matching request type. */
    public static final byte REQUEST_TYPE_GET_MATCHING = 2;

    /** Update request type. */
    public static final byte REQUEST_TYPE_UPDATE = 3;

    /** Remove request type. */
    public static final byte REQUEST_TYPE_REMOVE = 4;

    /** Remove All request type. */
    public static final byte REQUEST_TYPE_REMOVE_ALL = 5;

    /** The prefix for cache server config. */
    public final static String HTTP_CACHE_SERVER_PREFIX = "jcs.remotehttpcache";

    /** All of the RemoteHttpCacheServiceAttributes can be configured this way. */
    public final static String HTTP_CACHE_SERVER_ATTRIBUTES_PROPERTY_PREFIX = HTTP_CACHE_SERVER_PREFIX
        + ".serverattributes";
}
