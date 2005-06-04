package org.apache.jcs.auxiliary;

import org.apache.jcs.engine.behavior.ICacheEventQueue;

/**
 * @author aaronsm
 *  
 */
public abstract class AbstractAuxiliaryCacheAttributes
    implements AuxiliaryCacheAttributes
{

    protected String cacheName;

    protected String name;

    protected int eventQueueType;

    protected String eventQueuePoolName;

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setCacheName(java.lang.String)
     */
    public void setCacheName( String s )
    {
        this.cacheName = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getCacheName()
     */
    public String getCacheName()
    {
        return this.cacheName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setName(java.lang.String)
     */
    public void setName( String s )
    {
        this.name = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getName()
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setEventQueueType(java.lang.String)
     */
    public void setEventQueueType( String s )
    {
        if ( s != null )
        {
            if ( s.equalsIgnoreCase( POOLED_QUEUE_TYPE ) )
            {
                this.eventQueueType = ICacheEventQueue.POOLED_QUEUE_TYPE;
            }
            else
            {
                // single by default
                this.eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
            }
        }
        else
        {
            //  null, single by default
            this.eventQueueType = ICacheEventQueue.SINGLE_QUEUE_TYPE;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueueType()
     */
    public String getEventQueueType()
    {
        if ( this.eventQueueType == ICacheEventQueue.POOLED_QUEUE_TYPE )
        {
            return POOLED_QUEUE_TYPE;
        }
        else
        {
            return SINGLE_QUEUE_TYPE;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueueTypeFactoryCode()
     */
    public int getEventQueueTypeFactoryCode()
    {
        return this.eventQueueType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#setEventQueuePoolName(java.lang.String)
     */
    public void setEventQueuePoolName( String s )
    {
        eventQueuePoolName = s;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jcs.auxiliary.AuxiliaryCacheAttributes#getEventQueuePoolName()
     */
    public String getEventQueuePoolName()
    {
        return eventQueuePoolName;
    }

}
