package org.apache.commons.jcs.auxiliary.disk;

import junit.framework.TestCase;

import org.apache.commons.jcs.engine.CacheElement;
import org.apache.commons.jcs.engine.ElementAttributes;
import org.apache.commons.jcs.engine.behavior.ICacheElement;
import org.apache.commons.jcs.engine.behavior.IElementAttributes;

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
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );
        PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<String, String>( cacheElement );
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

        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value );
        PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<String, String>( cacheElement );
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
        ICacheElement<String, String> cacheElement = new CacheElement<String, String>( cacheName, key, value, elementAttributes );
        PurgatoryElement<String, String> purgatoryElement = new PurgatoryElement<String, String>( cacheElement );

        // DO WORK
        String result = purgatoryElement.toString();

        // VERIFY
        assertTrue( "Should have the cacheName.", result.indexOf( cacheName ) != -1 );
        assertTrue( "Should have the key.", result.indexOf( key ) != -1 );
        assertTrue( "Should have the value.", result.indexOf( value ) != -1 );
    }
}
