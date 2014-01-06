package org.apache.commons.jcs.auxiliary.remote.value;


/**
 * The different types of requests
 */
public enum RemoteRequestType
{
    /** Alive check request type. */
    ALIVE_CHECK,

    /** Get request type. */
    GET,

    /** Get Multiple request type. */
    GET_MULTIPLE,

    /** Get Matching request type. */
    GET_MATCHING,

    /** Update request type. */
    UPDATE,

    /** Remove request type. */
    REMOVE,

    /** Remove All request type. */
    REMOVE_ALL,

    /** Get keys request type. */
    GET_KEYSET,

    /** Dispose request type. */
    DISPOSE,
}
