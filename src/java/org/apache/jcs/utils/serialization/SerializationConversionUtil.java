package org.apache.jcs.utils.serialization;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheElementSerialized;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICacheElementSerialized;
import org.apache.jcs.engine.behavior.IElementSerializer;

/**
 * This uses a supplied Serialer to convert to and from cache elements.
 * 
 * @author Aaron Smuts
 * 
 */
public class SerializationConversionUtil
{

    private final static Log log = LogFactory.getLog( SerializationConversionUtil.class );

    /**
     * This returns a wrapper that has a serialized version of the value instead
     * of the value.
     * 
     * @param element
     * @param elementSerializer
     *            the serializer to be used.
     * @return null for null;
     * @throws IOException
     */
    public static ICacheElementSerialized getSerializedCacheElement( ICacheElement element,
                                                                    IElementSerializer elementSerializer )
        throws IOException
    {
        if ( element == null )
        {
            return null;
        }

        byte[] serialzedValue = null;

        // if it has already been serialized, don't do it again.
        if ( element instanceof ICacheElementSerialized )
        {
            serialzedValue = ( (ICacheElementSerialized) element ).getSerializedValue();
        }
        else
        {
            if ( elementSerializer != null )
            {
                try
                {
                    serialzedValue = elementSerializer.serialize( element.getVal() );
                }
                catch ( IOException e )
                {
                    log.error( "Problem serializing object.", e );
                    throw e;
                }
            }
            else
            {
                // we could just use the default.
                log.warn( "ElementSerializer is null.  Could not serialize object." );
                throw new IOException( "Could not serialize object.  The ElementSerializer is null." );
            }
        }
        ICacheElementSerialized serialized = new CacheElementSerialized( element.getCacheName(), element.getKey(),
                                                                         serialzedValue, element.getElementAttributes() );

        return serialized;
    }

    /**
     * This returns a wrapper that has a de-serialized version of the value
     * instead of the serialized value.
     * 
     * @param serialized
     * @param elementSerializer
     *            the serializer to be used.
     * @return null for null;
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static ICacheElement getDeSerializedCacheElement( ICacheElementSerialized serialized,
                                                            IElementSerializer elementSerializer )
        throws IOException, ClassNotFoundException
    {
        if ( serialized == null )
        {
            return null;
        }

        Object deSerialzedValue = null;

        if ( elementSerializer != null )
        {
            try
            {
                try
                {
                    deSerialzedValue = elementSerializer.deSerialize( serialized.getSerializedValue() );
                }
                catch ( ClassNotFoundException e )
                {
                    log.error( "Problem de-serializing object.", e );
                    throw e;
                }
            }
            catch ( IOException e )
            {
                log.error( "Problem de-serializing object.", e );
                throw e;
            }
        }
        else
        {
            // we could just use the default.
            log.warn( "ElementSerializer is null.  Could not serialize object." );
        }
        ICacheElement deSerialized = new CacheElement( serialized.getCacheName(), serialized.getKey(), deSerialzedValue );
        deSerialized.setElementAttributes( serialized.getElementAttributes() );

        return deSerialized;
    }

}
