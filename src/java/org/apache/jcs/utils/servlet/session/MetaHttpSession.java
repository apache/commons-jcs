package org.apache.jcs.utils.servlet.session;

import javax.servlet.http.HttpSession;

/**
 * Used to contain an http session and/or the associated information.
 *
 * @author asmuts
 * @created January 15, 2002
 */
class MetaHttpSession
{
    /** The corresponding cookie id of the http session. */
    final String session_id;
    /** The http session. */
    final HttpSession sess;


    /**
     * Constructs with the given session id, and the http session. Both values
     * can be null.
     *
     * @param session_id
     * @param sess
     */
    MetaHttpSession( String session_id, HttpSession sess )
    {
        this.session_id = session_id;
        this.sess = sess;
    }


    /**
     * Returns true iff both the session id and the http session are not null.
     */
    boolean valid()
    {
        return session_id != null && sess != null;
    }
}

