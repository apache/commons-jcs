package org.apache.commons.jcs.engine.logging.behavior;

import java.io.Serializable;

/**
 * This defines the behavior for event logging. Auxiliaries will send events to injected event
 * loggers.
 * <p>
 * In general all ICache interface methods should call the logger if one is configured. This will be
 * done on an ad hoc basis for now. Various auxiliaries may have additional events.
 */
public interface ICacheEventLogger
{
    /** ICache update */
    static final String UPDATE_EVENT = "update";

    /** ICache get */
    static final String GET_EVENT = "get";

    /** ICache getMultiple */
    static final String GETMULTIPLE_EVENT = "getMultiple";

    /** ICache getMatching */
    static final String GETMATCHING_EVENT = "getMatching";

    /** ICache remove */
    static final String REMOVE_EVENT = "remove";

    /** ICache removeAll */
    static final String REMOVEALL_EVENT = "removeAll";

    /** ICache dispose */
    static final String DISPOSE_EVENT = "dispose";

    /** ICache enqueue. The time in the queue. */
    //static final String ENQUEUE_EVENT = "enqueue";
    /**
     * Creates an event.
     * <p>
     * @param source - e.g. RemoteCacheServer
     * @param region - the name of the region
     * @param eventName - e.g. update, get, put, remove
     * @param optionalDetails - any extra message
     * @param key - the cache key
     * @return ICacheEvent
     */
    <T extends Serializable> ICacheEvent<T> createICacheEvent( String source, String region,
            String eventName, String optionalDetails, T key );

    /**
     * Logs an event.
     * <p>
     * @param event - the event created in createICacheEvent
     */
    <T extends Serializable> void logICacheEvent( ICacheEvent<T> event );

    /**
     * Logs an event. These are internal application events that do not correspond to ICache calls.
     * <p>
     * @param source - e.g. RemoteCacheServer
     * @param eventName - e.g. update, get, put, remove
     * @param optionalDetails - any extra message
     */
    void logApplicationEvent( String source, String eventName, String optionalDetails );

    /**
     * Logs an error.
     * <p>
     * @param source - e.g. RemoteCacheServer
     * @param eventName - e.g. update, get, put, remove
     * @param errorMessage - any error message
     */
    void logError( String source, String eventName, String errorMessage );
}
