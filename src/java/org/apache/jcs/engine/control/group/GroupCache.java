package org.apache.jcs.engine.control.group;

/*
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Velocity", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectNotFoundException;
import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCache;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.utils.locking.ReadWriteLockManager;
import org.apache.jcs.auxiliary.AuxiliaryCache;

/**
 * Group cache is basically a composite cache with the additional capability of
 * providing automatic and safe attribute name list update for each
 * GroupAttrName cache item. TODO: WORKING ON THIS NOW -- reduce the number of
 * methods or describe them better. The complexity of this points to group
 * design problems. I need to fix the locking and the maintenance of the remote
 * list. The list can be infered fromt he contents of the cache. Iterating
 * through the hashtable could be problematic but easier.
 *
 * @author asmuts
 */
public class GroupCache extends CompositeCache implements ICompositeCache
{
    private final static Log log = LogFactory.getLog( GroupCache.class );

    /** Manages locking for group item manipulation. */
    private ReadWriteLockManager locker = new ReadWriteLockManager();

    /**
     * Declare a group_id cache here, so ids and elements don't compete for
     * first with the list. The systemGroupIdCache will have different remote
     * behavior. Local removal of the list will propagate to the remote, but the
     * list will not move back and forth. The list can be maintained locally but
     * the elements themselves need not be.
     */
    ICompositeCache systemGroupIdCache;

    /**
     * Constructor for the GroupCache object
     *
     * @param cacheName The name of the region
     * @param auxCaches The auxiliary caches to be used by this region
     * @param cattr The cache attribute
     * @param attr The default element attributes
     */
    public GroupCache( String cacheName,
                       AuxiliaryCache[] auxCaches,
                       ICompositeCacheAttributes cattr,
                       IElementAttributes attr )
    {
        super( cacheName, auxCaches, cattr, attr );

        if ( log.isDebugEnabled() )
        {
            log.debug( "constructed groupcache " + cacheName + " from super" );
        }
        //ICompositeCache systemGroupIdCache = (ICompositeCache)systemCaches.get( "groupIdCache" );
    }

    /**
     * Constructor for the GroupCache object
     *
     * @param cacheName The name of the region
     * @param auxCaches The auxiliary caches to be used by this region
     * @param cattr The cache attribute
     * @param attr The default element attributes
     * @param systemGroupIdCache The systemGroupIdCache
     */
    public GroupCache( String cacheName,
                       AuxiliaryCache[] auxCaches,
                       ICompositeCacheAttributes cattr,
                       IElementAttributes attr,
                       ICompositeCache systemGroupIdCache )
    {
        super( cacheName, auxCaches, cattr, attr );
        if ( log.isDebugEnabled() )
        {
            log.debug( "constructed (2) groupcache " + cacheName + " from super" );
        }
        this.systemGroupIdCache = systemGroupIdCache;
    }

    /**
     * Overrides to provide read lock on both GroupAttrName read-operation and
     * String read-operation.
     *
     * @param key The key for the element
     * @return Returns element from the cache if found, else null
     */
    public ICacheElement get( Serializable key )
    {
        return get( key, false );
    }

    /**
     * Gets an element fromt he cache
     *
     * @param key The key for the element
     * @param invocation Is the originating method call from a local source
     * @return Returns element from the cache if found, else null
     */
    public ICacheElement get( Serializable key, boolean invocation )
    {

        // GETTING GROUP ELEMENT
        if ( key instanceof GroupAttrName )
        {
            return getGAN( ( GroupAttrName ) key );
        }

        // GROUP ID
        if ( key instanceof GroupId )
        {
            return getGI( ( GroupId ) key );
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( this.getCacheName() + " getting "
                       + key + " from super " );

            if ( invocation == false )
            {
                log.debug( "invokation is LOCAL" );
            }
            else
            {
                log.debug( "invokation is NOT Local" );
            }
        }

        // GETTING NON GROUP RELATED ITEM
        return super.get( key, invocation );
    }

    /**
     * Places a read lock on the group id for a GroupAttrName get-operation.
     *
     * @param key The key for the element
     * @param container Should it return the CacheElement wrapper
     * @return The gAN value
     */
    public ICacheElement getGAN( GroupAttrName key )
    {
        return getGAN( key, false );
    }

    /**
     * Gets the gAN attribute of the GroupCache object
     *
     * @param key The key for the element
     * @param container Should it return the CacheElement wrapper
     * @param invocation Is the originating method call from a local source
     * @return The gAN value
     */
    public ICacheElement getGAN( GroupAttrName key, boolean invocation )
    {
        if ( log.isDebugEnabled() )
        {
            if ( invocation == false )
            {
                log.debug( "invokation is LOCAL" );
            }
            else
            {
                log.debug( "invokation is NOT Local" );
            }
        }

        Object obj = null;
        // not necessary?, stops at getaux
        readLock( key.groupId );
        try
        {
            obj = super.get( key, invocation );
            //p( "got obj" );
        }
        finally
        {
            locker.done( key.groupId );
        }
        return ( ICacheElement ) obj;
    }


    /**
     * Places a read lock on the key for a GroupId get-operation.
     *
     * @param gid The group id
     * @param container Should the cache element wrapper be returned
     * @return The gI value
     */
    // get list from remote if it isn't present
    public ICacheElement getGI( GroupId gid )
    {
        return getGI( gid, false );
    }

    /**
     * removal of a group element will call this to get the list to edit.
     *
     * @param invocation Is the originating method call from a local source
     * @return The gI value
     */
    public ICacheElement getGI( GroupId gid, boolean invocation )
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "getGi(gid,container,invocation)" );
            if ( invocation == false )
            {
                log.debug( "invocation is LOCAL" );
            }
            else
            {
                log.debug( "invocation is NOT Local" );
            }
        }
        Object obj = null;
        readLock( gid.key );
        try
        {
            if ( invocation )
            {
                // Invocation is NOT local
                obj = systemGroupIdCache.localGet( gid.key );
            }
            else
            {
                // Invocation is local
                obj = systemGroupIdCache.get( gid.key );
            }

            if ( log.isDebugEnabled() )
            {
                log.debug( "getGi: got obj " + obj );
            }
        }
        catch ( IOException ioeg )
        {
        }
        finally
        {
            locker.done( gid.key );
        }
        return ( ICacheElement ) obj;
    }

    /**
     * Internally used read lock for group modification.
     *
     * @param id What name to lock on.
     */
    private void readLock( String id )
    {
        try
        {
            locker.readLock( id );
        }
        catch ( InterruptedException e )
        {
            // This previously would wait for console input before
            // continuing if the debug flag was set. I consider this
            // a BAD IDEA since it could introduce signifigant
            // confusion. ( There are other ways to accomplish this,
            // introduction or using a debugger (breakpoints) come to
            // mind. [james@jamestaylor.org]

            log.error( "Was interrupted while acquiring read lock", e );
        }
    }

    /**
     * Internally used write lock for group modification.
     *
     * @param id What name to lock on.
     */
    private void writeLock( String id )
    {
        try
        {
            locker.writeLock( id );
        }
        catch ( InterruptedException e )
        {
            // See note in readLock()

            log.error( "Was interrupted while acquiring read lock", e );
        }
    }

    /**
     * Overrides to special handling for GroupAttrName put-operation.
     *
     * @param key Retrieval key for the element to cache
     * @param val The Object to cache
     * @param attr The element attributes
     */
    public void put( Serializable key,
                     Serializable val,
                     IElementAttributes attr )
        throws IOException
    {
        if ( key instanceof GroupAttrName )
        {
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "putting via method" );
                }
                putGAN( ( GroupAttrName ) key, val, attr );
            }
            catch ( IOException ioe )
            {
            }
            return;
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "put(key,val,attr) > updating "
                       + key + " via super method, attr.getIsRemote() = "
                       + attr.getIsRemote() );
        }

        // NOT GROUP RELATED
        try
        {
            //updateCaches( key, val, attr );
            CacheElement ce = new CacheElement( this.getCacheName(), key, val );
            ce.setElementAttributes( attr );
            super.update( ce );

        }
        catch ( IOException ioe )
        {
        }
        return;
    }

    /** Description of the Method */
    public void put( Serializable key, Serializable val )
        throws IOException
    {
        //public void put (Object key, Object val) throws IOException {

        if ( key instanceof GroupAttrName )
        {
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "putting via putGAN((GroupAttrName)key, val) method" );
                }
                putGAN( ( GroupAttrName ) key, val );
            }
            catch ( IOException ioe )
            {
            }
            return;
        }

        if ( log.isDebugEnabled() )
        {
            log.debug( "put(key,value) -- updating " + key + " via super method" );
        }

        //super.put(key, val);
        IElementAttributes attrE = ( IElementAttributes ) this.attr.copy();
        try
        {
            // must make sure you call the sure here
            // updateCaches( key, val, attrE, ICache.INCLUDE_REMOTE_CACHE );
            // problem super calls back up and the last instruction gets confused
            CacheElement ce = new CacheElement( this.getCacheName(), key, val );
            ce.setElementAttributes( attrE );
            super.update( ce );
        }
        catch ( IOException ioe )
        {
        }
        return;
    }

    /** Description of the Method */
    public synchronized void update( ICacheElement ce )
        throws IOException
    {
        //update( ce, ICache.LATERAL_INVOKATION );
        if ( ce.getKey() instanceof GroupAttrName )
        {
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "update(ce) > putting via putGAN((GroupAttrName)key, val) method" );
                }
                putGAN( ( GroupAttrName ) ce.getKey(), ce.getVal() );
            }
            catch ( IOException ioe )
            {
            }
            return;
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "update(ce) > updating " + ce.getKey() + " via super method" );
        }

        try
        {
            // must make sure you call the sure here
            // updateCaches( key, val, attrE, ICache.INCLUDE_REMOTE_CACHE );
            // problem super calls back up and the last instruction gets confused
            super.update( ce );
        }
        catch ( IOException ioe )
        {
        }
        return;
    }


    /**
     * Update
     */
    public synchronized void update( ICacheElement ce, boolean localOnly )
        throws IOException
    {

        Object key = ce.getKey();
        if ( key instanceof GroupAttrName )
        {
            try
            {
                if ( log.isDebugEnabled() )
                {
                    log.debug( "update(ce,invocation) >putting via ga method" );
                }
                IElementAttributes attrE = ( IElementAttributes ) this.attr.copy();
                putGAN( ( GroupAttrName ) key, ce.getVal(), attrE, localOnly );
            }
            catch ( IOException ioe )
            {
            }
            return;
        }
        if ( log.isDebugEnabled() )
        {
            log.debug( "update(ce,invocation) > updating " + key + " via super method" );
        }

        // TODO: what about id? not possible here?
        // GROUP ID
        //if ( key instanceof GroupId )
        //{
        //}

        try
        {
            super.update( ce, localOnly );
        }
        catch ( IOException ioe )
        {
        }
        return;
    }
    // end update

    /** GroupAttrName specific put-operation. */
    public void putGAN( GroupAttrName key, Serializable val )
        throws IOException
    {
        if ( log.isDebugEnabled() )
        {
            log.debug( "in putGAN(GroupAttrName key, Serializable val) method" );
        }
        if ( key == null || val == null )
        {
            NullPointerException ex
                = new NullPointerException( "key=" + key + " and val="
                                            + val + " must not be null." );
            log.error( ex );
            throw ex;
        }

        IElementAttributes attrE = ( IElementAttributes ) this.attr.copy();
        putGAN( key, val, attrE );
        return;
    }


    /**
     * Special handling for GroupAttrName put-operation. Provides write lock and
     * automatic attribute name set update.
     */
    // TODO: DistCacheMulticaster,etc. currently only supports key of String type
    // Needs to support GroupAttrName type, or do we ?
    private void putGAN( GroupAttrName key,
                         Serializable val,
                         IElementAttributes attrE )
        throws IOException
    {
        log.debug( "in putGAN( gan,val,attr) " );

        putGAN( key, val, attrE, false );
    }

    /**
     * Put an element into a group.
     *
     * @param invocation Is the originating method call from a local source
     */
    public void putGAN( GroupAttrName key, Serializable val,
                        IElementAttributes attrE,
                        boolean localOnly )
        throws IOException
    {

        if ( log.isDebugEnabled() )
        {
            //p( "in putGAN( gan,val,attr,boolean updateRemote) " );
            log.debug( "in putGAN( gan,val,attr,boolean invocation) " );
        }

        writeLock( key.groupId );
        try
        {

            // update the attribute.
            //updateCaches(key, val, attrE, INCLUDE_REMOTE_CACHE);
            CacheElement ce = new CacheElement( this.getCacheName(), key, val );
            ce.setElementAttributes( attrE );

            if ( log.isDebugEnabled() )
            {
                log.debug( "putGAN( gan,val,attr,boolean invocation) > updating group attribute via super" );
            }

            super.update( ce, localOnly );

            // UPDATE THE ATTRIBUTENAME LIST, get it first
            GroupId groupId = new GroupId( this.getCacheName(), key.groupId );
            HashSet attrNameSet = null;
            attrNameSet = ( HashSet ) systemGroupIdCache.get( groupId.key );

            if ( attrNameSet == null )
            {
                attrNameSet = new HashSet();
            }
            attrNameSet.add( key.attrName );

            if ( log.isDebugEnabled() )
            {
                log.debug( "putGAN( gan,val,attr,boolean invocation) > attrNameSet.size()  = " + attrNameSet.size() );
            }

            CacheElement ceID = new CacheElement( this.getCacheName(), groupId.key, attrNameSet );
            ceID.setElementAttributes( attrE );

            // DO NOT SEND THE UPDATE LIST REMOTELY
            // THE ELEMENT WILL BE SENT AND THE LIST MAINTAINED REMOTELY

            systemGroupIdCache.localUpdate( ceID );
        }
        finally
        {
            locker.done( key.groupId );
        }
    }

    /** Description of the Method */
    protected void createGroup( String group )
        throws CacheException
    {
        createGroup( group, this.attr );
    }

    /** Description of the Method */
    protected void createGroup( String group, IElementAttributes attrE )
        throws CacheException
    {
        // update the attribute name set.
        GroupId groupId = new GroupId( this.getCacheName(), group );
        HashSet attrNameSet = null;

        //attrNameSet = (HashSet)super.get(groupId.key);
        try
        {
            attrNameSet = ( HashSet ) systemGroupIdCache.get( groupId.key );
        }
        catch ( IOException ioe )
        {
        }

        if ( attrNameSet == null )
        {
            attrNameSet = new HashSet();
        }
        else
        {
            throw new CacheException( "createGroup(group,attr) > group " + group + " already exists " );
        }
        try
        {
            CacheElement ceID = new CacheElement( this.getCacheName(), groupId.key, attrNameSet );
            ceID.setElementAttributes( attrE );
            //updateCaches(groupId.key, attrNameSet, attrE );
            //super.update( ceID, EXCLUDE_REMOTE_CACHE );
            systemGroupIdCache.localUpdate( ceID );

        }
        catch ( IOException ioe )
        {
        }
    }

    /**
     * Overrides to provide special handling for GroupAttrName remove-operation.
     */
    public boolean remove( Serializable key )
    {
        log.debug( "in basic remove" );

        // if expired super will call remove and we can't have a lock
        // need a third method
        return remove( key, false);
    }// rmove

    /**
     * Easier to classify and send to other methods than relying on type
     * overriding. removeGAN could be called remove
     *
     * @param invocation Is the originating method call from a local source
     */
    public boolean remove( Serializable key, boolean invocation )
    {

        // THIS IS A GROUP ELEMENT
        if ( key instanceof GroupAttrName )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "calling removeGAN" );
                if ( invocation == false )
                {
                    log.debug( "invokation is LOCAL" );
                }
                else
                {
                    log.debug( "invokation is NOT Local" );
                }
            }
            return removeGAN( ( GroupAttrName ) key, invocation );
        }

        // THIS IS A GROUP ID
        if ( key instanceof GroupId )
        {
            if ( log.isDebugEnabled() )
            {
                log.debug( "call removeGI" );
                if ( invocation == false )
                {
                    log.debug( "invokation is LOCAL" );
                }
                else
                {
                    log.debug( "invokation is NOT Local" );
                }
            }
            return removeGI( ( GroupId ) key, invocation );
        }

        // NOT GROUP RELATED
        if ( log.isDebugEnabled() )
        {
            log.debug( "call super.remove, " + invocation + " for " + key );
            if ( invocation == false )
            {
                log.debug( "invokation is LOCAL" );
            }
            else
            {
                log.debug( "invokation is NOT Local" );
            }
        }
        return super.remove( key, invocation );
    }// end remove

    /**
     * Special handling for GroupAttrName remove-operation. Provides write lock
     * and automatic attribute name set update. <br>
     * <br>
     * Note: there is the possibility that all the remove-cache-events for all
     * group attribute names are queued and pending to be processed. Meanwhile,
     * the get-opeation for the attrbute name set will return with a size > 0.
     * Hence, when a groupn is invalidated, it's necessary to queue a
     * remove-attribute-name-set request to clean up garbage due to this race
     * condition.
     *
     * @param invocation Is the originating method call from a local source
     */
    public boolean removeGAN( GroupAttrName key, boolean invocation )
    {

        boolean ret;
        if ( log.isDebugEnabled() )
        {
            log.debug( "in removeGAN" );
            if ( invocation == false )
            {
                log.debug( "invocation is LOCAL" );
            }
            else
            {
                log.debug( "invocation is NOT Local" );
            }

        }

        // TODO: fix locking, got too nested and confusing
        //writeLock(key.groupId);
        try
        {

            // REMOVE THE ELEMENT
            ret = super.remove( key, invocation );

            // UPDATE THE LIST, remove item -- 3rd arg
            updateGroupAttrNameSet( key, invocation, true );

        }
        finally
        {
            //locker.done(key.groupId);
        }
        return ret;
    }

    /**
     * Handles removal, update, and insertion of items into the attrNameSet for
     * a group cache region. The Group Cache listener called the add when it
     * gets a put though it may remove the item it referes to, depending ont he
     * configuration. This saves on local cache space and keeps the list up to
     * date.
     *
     * @param key The key for the group element
     * @param invocation The source of the call
     * @param remove Is this a remove request
     */
    public void updateGroupAttrNameSet( GroupAttrName key,
                                        boolean invocation,
                                        boolean remove )
    {

        // update the attribute name set.
        // Note: necessary to use super.get to avoid read lock within the current write lock.
        GroupId groupId = new GroupId( this.getCacheName(), key.groupId );
        HashSet attrNameSet = null;
        CacheElement ce = null;

        try
        {
            if ( invocation )
            {
                // Invocation is NOT local
                ce = ( CacheElement ) systemGroupIdCache.localGet( groupId.key );
            }
            else
            {
                // Invocation is local
                ce = ( CacheElement ) systemGroupIdCache.get( groupId.key );
            }
        }
        catch ( IOException ioe )
        {
        }

        // IF THE NAME SET IS FOUND
        // TODO: move -- INITIAL INSERTION IS CURRENTLY DONE BY THE PUT
        if ( ce != null )
        {
            attrNameSet = ( HashSet ) ce.val;

            // HANDLE NON REMOVAL SCENARIOS
            if ( attrNameSet != null || !remove )
            {
                // THE GROUP HAS BEEN CREATED BUT NOTHING IS IN IT
                if ( attrNameSet == null )
                {
                    attrNameSet = new HashSet();
                }

                if ( remove )
                {
                    attrNameSet.remove( key.attrName );
                }
                else
                {
                    attrNameSet.add( key.attrName );
                }

                if ( attrNameSet.size() > 0 )
                {
                    // update the changed name set.
                    try
                    {

                        CacheElement ceID
                            = new CacheElement( this.getCacheName(),
                                                groupId.key,
                                                attrNameSet );

                        ceID.setElementAttributes( ce.attr );
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "updateGroupAttrNameSet((groupAttrname)key,invocation,remove) > calling systemGroupIdCache.update( ceID, EXCLUDE_REMOTE_CACHE )" );
                        }
                        // ALWAYS EXCLUDE THE REMOTE CACHE
                        // TODO: should this be configurable? no

                        systemGroupIdCache.localUpdate( ceID );

                    }
                    catch ( IOException ioe )
                    {
                    }
                }

                // HANDLE REMOVAL SCENARIOS
                else if ( remove )
                {
                    // no more attribute, so remove the name set all together, skipLock = true.
                    //super.remove(groupId, invokation );
                    //removeGI(groupId, invokation, true );
                    try
                    {
                        if ( log.isDebugEnabled() )
                        {
                            log.debug( "calling systemGroupIdCache.remove( groupId.key, EXCLUDE_REMOTE_CACHE )" );
                        }

                        // unlike insertion, removal should go remote if locally invoked
                        if ( invocation )
                        {
                            systemGroupIdCache.localRemove( groupId.key );
                        }
                        else
                        {
                            systemGroupIdCache.remove( groupId.key );
                        }
                    }
                    catch ( IOException ioe )
                    {
                    }
                }
            }
        }
    }
    // end updateGroupAttrNameSet

    /**
     * Special handling for GroupId remove-operation. Removes the attribute name
     * set of the session.
     */
    public void removeGI( GroupId groupId )
    {
        log.debug( "removeGI" );

        removeGI( groupId, false);
    }

    /**
     * Skip the lock from the normal remove that is called from the super when
     * an element expires. Keep the read lock method for calls from groupaccess.
     */
    protected boolean removeGI( GroupId groupId, boolean invocation )
    {
        return removeGI( groupId, invocation, false );
    }

    /**
     * Removes the group id. Low level method. We need a higher level for
     * invalidating the group.
     */
    protected boolean removeGI( GroupId groupId, boolean invocation, boolean skipLock )
    {

        boolean ok = false;

        // problem with removing expired while getting!
        skipLock = false;

        log.debug( "in removeGI" );

        if ( !skipLock )
        {
            writeLock( groupId.key );
        }
        try
        {
            if ( invocation )
            {
                ok = systemGroupIdCache.localRemove( groupId.key );
            }
            else
            {
                ok = systemGroupIdCache.remove( groupId.key );
            }
        }
        catch ( IOException ioeg )
        {
        }
        finally
        {
            if ( !skipLock )
            {
                locker.done( groupId.key );
            }
        }
        return ok;
    }

    /**
     * Gets the elementAttributes attribute of the GroupCache object
     *
     * @param key Retrieval value for item.
     * @return The elementAttributes value
     */
    public IElementAttributes getElementAttributes( Serializable key )
        throws CacheException, IOException
    {
        CacheElement ce = ( CacheElement ) get( key );
        if ( ce == null )
        {
            throw new ObjectNotFoundException( "key " + key + " is not found" );
        }
        return ce.getElementAttributes();
    }
}

