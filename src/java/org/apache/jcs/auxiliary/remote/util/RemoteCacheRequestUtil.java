package org.apache.jcs.auxiliary.remote.util;

import org.apache.jcs.auxiliary.remote.value.RemoteCacheRequest;

/** Utilities for the request object */
public class RemoteCacheRequestUtil
{
    /**
     * @param type
     * @return name for the type
     */
    public static String getRequestTypeName( byte type )
    {
        switch ( type )
        {
            case RemoteCacheRequest.REQUEST_TYPE_ALIVE_CHECK:
                return "AliveCheck";
            case RemoteCacheRequest.REQUEST_TYPE_GET:
                return "Get";
            case RemoteCacheRequest.REQUEST_TYPE_GET_MULTIPLE:
                return "GetMultiple";
            case RemoteCacheRequest.REQUEST_TYPE_GET_MATCHING:
                return "GetMatching";
            case RemoteCacheRequest.REQUEST_TYPE_REMOVE:
                return "Remove";
            case RemoteCacheRequest.REQUEST_TYPE_REMOVE_ALL:
                return "RemoveAll";
            case RemoteCacheRequest.REQUEST_TYPE_UPDATE:
                return "Update";
            case RemoteCacheRequest.REQUEST_TYPE_GET_GROUP_KEYS:
                return "GetGroupKeys";
            case RemoteCacheRequest.REQUEST_TYPE_GET_GROUP_NAMES:
                return "GetGroupNames";
            case RemoteCacheRequest.REQUEST_TYPE_DISPOSE:
                return "Dispose";
            default:
                return "Unknown";
        }
    }
}
