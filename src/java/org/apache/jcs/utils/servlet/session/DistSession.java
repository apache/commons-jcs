package org.apache.jcs.utils.servlet.session;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache JCS" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache JCS", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.Serializable;

import java.util.Enumeration;
import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.jcs.access.GroupCacheAccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DistSession uses the CompositeCache and GroupCache to create a failover-safe
 * distributed session.
 *
 * @author asmuts
 * @author <a href="mailto:dlr@finemaltcoding.com">Daniel Rall</a>
 * @created January 15, 2002
 */
public class DistSession
     implements HttpSession, ISessionConstants
{
    private final static Log log =
        LogFactory.getLog( DistSession.class );

    private static boolean SET_ATTR_INVOCATION = true;
    private static boolean REMOVE_ATTR_INVOCATION = false;

    private ServletContext context;

    private String session_id;
    private SessionInfo sessInfo;

    private GroupCacheAccess sessCache;

    private boolean isNew;
    private boolean isValid = true;


    /**
     * Constructor for a new instance with no <code>ServletContext</code>. This
     * method will eventually be deprecated in favor of
     * DistSession(ServletContext).
     */
    public DistSession()
    {
        this( null );
    }


    /**
     * Creates a new instance with the specified <code>ServletContext</code>.
     *
     * @param context
     */
    public DistSession( ServletContext context )
    {
        this.context = context;

        try
        {
            sessCache = GroupCacheAccess.getGroupAccess( SESS_CACHE_NAME );
        }
        catch ( Exception e )
        {
        }
    }


    /**
     * Initialization for an existing session. The session must be initialized
     * to be used. The sessInfo is not passed int he constructor, becaue these
     * will be pooled.
     *
     * @return true if the init is successful;
     */
    public boolean init( String session_id )
    {
        this.isNew = false;
        this.session_id = session_id;

        //sessInfo = (SessionInfo)sessInfoCache.get( session_id );
        // not necessary if you don't set default ElementAttributes
        // try to get data in the session_id group under the session_id key

        sessInfo = ( SessionInfo ) sessCache.getFromGroup( session_id, session_id );

        if ( sessInfo == null )
        {
            log.info( "session not found for " + session_id );
            return false;
        }
        log.info( "found session" + sessInfo );
        return true;
    }


    /** Initialization for a new session. */
    public void initNew()
    {
        try
        {
            this.isNew = true;
            sessInfo = new SessionInfo();
            session_id = SessionIdGenerator.createSessionId();

            // key, group, value
            sessCache.putInGroup( session_id, session_id, sessInfo );
        }
        catch ( Exception e )
        {
        }
        log.info( "createUserSession " + this );
    }


    /** Clears the session information. */
    public void clean()
    {
        sessInfo = null;
    }


    /**
     * Returns a reference to the <code>ServletContext</code> this session is a
     * part of.
     */
    public ServletContext getServletContext()
    {
        return context;
    }


    //REQUIRED INTERFACE METHODS
    /**
     * Gets the attribute attribute of the DistSession object
     *
     * @return The attribute value
     */
    public Object getAttribute( String name )
    {
        if ( !isValid )
        {
            throw new IllegalStateException( "Cannot setAttribute on an invalid session " + this );
        }
        return sessCache.getFromGroup( name, session_id );
    }


    /**
     * Gets the attributeNames attribute of the DistSession object
     *
     * @return The attributeNames value
     */
    public Enumeration getAttributeNames()
    {
        return Collections.enumeration(sessCache.getGroupKeys(session_id));
    }


    /**
     * Gets the creationTime attribute of the DistSession object
     *
     * @return The creationTime value
     */
    public long getCreationTime()
    {
        return sessInfo.creationTime;
    }


    /**
     * Gets the id attribute of the DistSession object
     *
     * @return The id value
     */
    public String getId()
    {
        return session_id;
    }


    /**
     * Gets the lastAccessedTime attribute of the DistSession object
     *
     * @return The lastAccessedTime value
     */
    public long getLastAccessedTime()
    {
        return sessInfo.lastAccessedTime;
    }


    /** Update the last access time. */
    public void access()
    {
        try
        {
            log.info( "updating lastAccess for " + this );
            sessInfo.lastAccessedTime = System.currentTimeMillis();
            sessCache.putInGroup( session_id, session_id, sessInfo );
        }
        catch ( Exception e )
        {
        }
        return;
    }


    /**
     * Gets the maxInactiveInterval attribute of the DistSession object
     *
     * @return The maxInactiveInterval value
     */
    public int getMaxInactiveInterval()
    {
        return sessInfo.maxInactiveInterval;
    }


    /**
     * @return The sessionContext value
     * @deprecated As of Version 2.1, this method is deprecated and has no
     *      replacement. It will be removed in a future version of the Java
     *      Servlet API.
     */
    public HttpSessionContext getSessionContext()
    {
        return null;
    }


    /**
     * Gets the value attribute of the DistSession object
     *
     * @return The value value
     */
    public Object getValue( String name )
    {
        return getAttribute( name );
    }


    /**
     * Gets the valueNames attribute of the DistSession object
     *
     * @return The valueNames value
     */
    public String[] getValueNames()
    {
        return (String[]) sessCache
            .getGroupKeys(session_id).toArray( new String[0] );
    }


    /** Description of the Method */
    public void invalidate()
    {
        if ( !isValid )
        {
            throw new IllegalStateException( "Cannot setAttribute on an invalid session " + this );
        }
        log.info( "destroying session " + this );
        isValid = false;
        sessCache.invalidateGroup( session_id );
        return;
    }


    /**
     * Gets the new attribute of the DistSession object
     *
     * @return The new value
     */
    public boolean isNew()
    {
        if ( !isValid )
        {
            throw new IllegalStateException( "Cannot setAttribute on an invalid session " + this );
        }
        return isNew;
    }


    // need to remove the serialzable restrction
    /** Description of the Method */
    public void putValue( String name, Object value )
    {
        setAttribute( name, value );
    }


    /** Description of the Method */
    public void removeAttribute( String name )
    {
        removeAttribute( name, REMOVE_ATTR_INVOCATION );
    }


    /** Description of the Method */
    private void removeAttribute( String name, boolean invocation )
    {
        if ( !isValid )
        {
            throw new IllegalStateException( "Cannot setAttribute on an invalid session " + this );
        }
        // Needs to retrive the attribute so as to do object unbinding, if necessary.
        Serializable val = ( Serializable ) sessCache.getFromGroup( name, session_id );

        if ( val == null )
        {
            return;
        }
        if ( invocation == REMOVE_ATTR_INVOCATION )
        {
            // remove attribute - name set taken care of by the session cache.
            sessCache.remove( name, session_id );
        }
        // Generate object unbinding event if necessary.
        if ( val instanceof HttpSessionBindingListener )
        {
            ( ( HttpSessionBindingListener ) val ).valueUnbound( new HttpSessionBindingEvent( this, name ) );
        }
        return;
    }


    /** Description of the Method */
    public void removeValue( String name )
    {
        removeAttribute( name );
    }


    /**
     * Sets the attribute attribute of the DistSession object
     *
     * @param name The new attribute value
     * @param value The new attribute value
     */
    public void setAttribute( String name, Object value )
    {
        if ( !isValid )
        {
            throw new IllegalStateException( "Cannot setAttribute on an invalid session " + this );
        }
        // unbind object first if any.
        //removeAttribute(name, SET_ATTR_INVOCATION);
        //try {
        //  sessCache.put(new GroupAttrName(session_id, name), (Serializable)value);
        //} catch( Exception e ) {}

        try
        {
            sessCache.putInGroup( name, session_id, value );
        }
        catch ( Exception e )
        {
        }

        // Generate object binding event if necessary.
        if ( value instanceof HttpSessionBindingListener )
        {
            ( ( HttpSessionBindingListener ) value ).valueBound( new HttpSessionBindingEvent( this, name ) );
        }
        return;
    }


    /**
     * Sets the maxInactiveInterval attribute of the DistSession object
     *
     * @param i The new maxInactiveInterval value
     */
    public void setMaxInactiveInterval( int i )
    {
        sessInfo.maxInactiveInterval = i;
    }


    /** Description of the Method */
    public String toString()
    {
        return "[sesson_id=" + session_id + ", sessInfo=" + sessInfo + "]";
    }
}
