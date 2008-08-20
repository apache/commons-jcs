package org.apache.jcs.engine.control;

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.auxiliary.AuxiliaryCacheConfigurator;
import org.apache.jcs.auxiliary.MockAuxiliaryCache;
import org.apache.jcs.auxiliary.MockAuxiliaryCacheAttributes;
import org.apache.jcs.auxiliary.MockAuxiliaryCacheFactory;
import org.apache.jcs.engine.CompositeCacheAttributes;
import org.apache.jcs.engine.ElementAttributes;
import org.apache.jcs.engine.logging.MockCacheEventLogger;

/** Unit tests for the configurator. */
public class CompositeCacheConfiguratorUnitTest
    extends TestCase
{
    /**
     * Verify that we can parse the event logger correctly
     */
    public void testParseAuxiliary_CacheEventLogger_Normal()
    {
        // SETUP
        String regionName = "MyRegion";

        String auxName = "MockAux";
        String auxPrefix = "jcs.auxiliary." + auxName;
        String auxiliaryClassName = MockAuxiliaryCacheFactory.class.getCanonicalName();
        String eventLoggerClassName = MockCacheEventLogger.class.getCanonicalName();
        String auxiliaryAttributeClassName =  MockAuxiliaryCacheAttributes.class.getCanonicalName();

        Properties props = new Properties();
        props.put( auxPrefix, auxiliaryClassName );
        props.put( auxPrefix + CompositeCacheConfigurator.ATTRIBUTE_PREFIX, auxiliaryAttributeClassName );
        props.put( auxPrefix + AuxiliaryCacheConfigurator.CACHE_EVENT_LOGGER_PREFIX, eventLoggerClassName );

        System.out.print( props );
        
        CompositeCacheManager manager = CompositeCacheManager.getUnconfiguredInstance();

        CompositeCacheConfigurator configurator = new CompositeCacheConfigurator( manager );

        CompositeCache cache = new CompositeCache( regionName, new CompositeCacheAttributes(), new ElementAttributes() );

        // DO WORK
        MockAuxiliaryCache result = (MockAuxiliaryCache) configurator
            .parseAuxiliary( cache, props, auxName, regionName );

        // VERIFY
        assertNotNull( "Should have an auxcache.", result );
        assertNotNull( "Should have an event logger.", result.cacheEventLogger );
    }
}
