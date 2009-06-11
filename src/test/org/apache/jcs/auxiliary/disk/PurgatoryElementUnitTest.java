package org.apache.jcs.auxiliary.disk;

import junit.framework.TestCase;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;

/** Simple unit tests for the Purgatory Element. */
public class PurgatoryElementUnitTest
    extends TestCase
{
    /** Verify basic data */
    public void testSpoolable_normal()
    {
        // SETUP
        String cacheName = "myCacheName";
        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement cacheElement = new CacheElement( cacheName, key, value, elementAttributes );
        PurgatoryElement purgatoryElement = new PurgatoryElement( cacheElement );
        purgatoryElement.setSpoolable( false );

        // DO WORK
        boolean result = purgatoryElement.isSpoolable();

        // VERIFY
        assertFalse( "Should not be spoolable.", result );
    }

    /** Verify basic data */
    public void testElementAttributes_normal()
    {
        // SETUP
        String cacheName = "myCacheName";
        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();

        ICacheElement cacheElement = new CacheElement( cacheName, key, value );
        PurgatoryElement purgatoryElement = new PurgatoryElement( cacheElement );
        purgatoryElement.setElementAttributes( elementAttributes );

        // DO WORK
        IElementAttributes result = cacheElement.getElementAttributes();

        // VERIFY
        assertEquals( "Should have set the attributes on the element", elementAttributes, result );
    }

    /** Verify basic data */
    public void testToString_normal()
    {
        // SETUP
        String cacheName = "myCacheName";
        String key = "myKey";
        String value = "myValue";
        IElementAttributes elementAttributes = new ElementAttributes();
        ICacheElement cacheElement = new CacheElement( cacheName, key, value, elementAttributes );
        PurgatoryElement purgatoryElement = new PurgatoryElement( cacheElement );

        // DO WORK
        String result = purgatoryElement.toString();

        // VERIFY
        assertTrue( "Should have the cacheName.", result.indexOf( cacheName ) != -1 );
        assertTrue( "Should have the key.", result.indexOf( key ) != -1 );
        assertTrue( "Should have the value.", result.indexOf( value ) != -1 );
    }
}
