package org.apache.jcs.admin.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jcs.engine.CacheConstants;
import org.apache.jcs.engine.memory.MemoryCache;
import org.apache.jcs.engine.memory.MemoryElementDescriptor;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;

/**
 * A servlet which provides HTTP access to JCS. Allows a summary of regions
 * to be viewed, and removeAll to be run on individual regions or all regions.
 * Also provides the ability to remove items (any number of key arguments can
 * be provided with action 'remove'). Should be initialized with a properties
 * file that provides at least a classpath resource loader. Since this extends
 * VelocityServlet, which uses the singleton model for velocity, it will share
 * configuration with any other Velocity in the same JVM.
 *
 * Initialization in a webapp will look something like this:
 * <pre>
 *  [servlet]
 *      [servlet-name]JCSAdminServlet[/servlet-name]
 *      [servlet-class]org.apache.jcs.admin.servlet.JCSAdminServlet[/servlet-class]
 *      [init-param]
 *          [param-name]properties[/param-name]
 *          [param-value]WEB-INF/conf/JCSAdminServlet.velocity.properties[/param-value]
 *      [/init-param]
 *  [/servlet]
 * </pre>
 *
 * FIXME: It would be nice to use the VelocityEngine model so this can be truly
 *        standalone. Right now if you run it in the same container as, say,
 *        turbine, turbine must be run first to ensure it's config takes
 *        precedence.
 *
 * @author <a href="mailto:james@jamestaylor.org">James Taylor</a>
 * @version $Id$
 */
public class JCSAdminServlet extends VelocityServlet
{
    private static final String DEFAULT_TEMPLATE_NAME =
        "/org/apache/jcs/admin/servlet/JCSAdminServletDefault.vm";

    private static final String REGION_DETAIL_TEMPLATE_NAME =
        "/org/apache/jcs/admin/servlet/JCSAdminServletRegionDetail.vm";

    // Keys for parameters

    private static final String CACHE_NAME_PARAM = "cacheName";

    private static final String ACTION_PARAM = "action";
    private static final String KEY_PARAM = "key";
    private static final String SILENT_PARAM = "silent";

    // Possible values for 'action' parameter

    private static final String CLEAR_ALL_REGIONS_ACTION = "clearAllRegions";
    private static final String CLEAR_REGION_ACTION = "clearRegion";
    private static final String REMOVE_ACTION = "remove";
    private static final String DETAIL_ACTION = "detail";

    private CompositeCacheManager cacheHub = CompositeCacheManager.getInstance();

    /** @see org.apache.velocity.servlet.VelocityServlet#handleRequest */
    protected Template handleRequest( HttpServletRequest request,
                                      HttpServletResponse response,
                                      Context context )
        throws Exception
    {
        String templateName = DEFAULT_TEMPLATE_NAME;

        // Get cacheName for actions from request (might be null)

        String cacheName = request.getParameter( CACHE_NAME_PARAM );

        // If an action was provided, handle it

        String action = request.getParameter( ACTION_PARAM );

        if ( action != null )
        {
            if ( action.equals( CLEAR_ALL_REGIONS_ACTION ) )
            {
                clearAllRegions();
            }
            else if ( action.equals( CLEAR_REGION_ACTION ) )
            {
                if ( cacheName == null )
                {
                    // Not Allowed
                }
                else
                {
                    clearRegion( cacheName );
                }
            }
            else if ( action.equals( REMOVE_ACTION ) )
            {
                String[] keys = request.getParameterValues( KEY_PARAM );

                for ( int i = 0; i < keys.length; i++ )
                {
                    removeItem( cacheName, keys[ i ] );
                }

                templateName = REGION_DETAIL_TEMPLATE_NAME;
            }
            else if ( action.equals( DETAIL_ACTION ) )
            {
                templateName = REGION_DETAIL_TEMPLATE_NAME;
            }
        }

        if ( request.getParameter( SILENT_PARAM ) != null )
        {
            // If silent parameter was passed, no output should be produced.

            return null;
        }
        else
        {
            // Populate the context based on the template

            if ( templateName == REGION_DETAIL_TEMPLATE_NAME )
            {
                context.put( "cacheName", cacheName );
                context.put( "keys", getSortedKeys( cacheName ) );
            }
            else if ( templateName == DEFAULT_TEMPLATE_NAME )
            {
                context.put( "cacheInfoRecords", buildCacheInfo() );
            }

            return getTemplate( templateName );
        }
    }

    private Object[] getSortedKeys( String cacheName )
    {
        CompositeCache cache = ( CompositeCache ) cacheHub.getCache( cacheName );

        Object[] keys = cache.getMemoryCache().getKeyArray();

        Arrays.sort( keys );

        return keys;
    }

    private LinkedList buildCacheInfo() throws Exception
    {
        String[] cacheNames = cacheHub.getCacheNames();

        Arrays.sort( cacheNames );

        LinkedList cacheInfo = new LinkedList();

        CacheRegionInfo regionInfo;
        CompositeCache cache;

        for ( int i = 0; i < cacheNames.length; i++ )
        {
            cache = ( CompositeCache ) cacheHub.getCache( cacheNames[ i ] );

            regionInfo = new CacheRegionInfo();

            regionInfo.name = cache.getCacheName();
            regionInfo.itemCount = cache.getSize();
            regionInfo.byteCount = getByteCount( cache );
            regionInfo.status = statusAsString( cache );

            cacheInfo.add( regionInfo );
        }

        return cacheInfo;
    }

    public int getByteCount( CompositeCache cache )
        throws Exception
    {
        MemoryCache memCache = cache.getMemoryCache();

        Iterator iter = memCache.getIterator();

        CountingOnlyOutputStream counter = new CountingOnlyOutputStream();
        ObjectOutputStream out = new ObjectOutputStream( counter );

        while ( iter.hasNext() )
        {
            MemoryElementDescriptor node = ( MemoryElementDescriptor )
                ( ( Map.Entry ) iter.next() ).getValue();

            out.writeObject( node.ce.getVal() );
        }

        // 4 bytes lost for the serialization header

        return counter.getCount() - 4;
    }

    private String statusAsString( ICache cache )
    {
        int status = cache.getStatus();

        return ( status == CacheConstants.STATUS_ALIVE ? "ALIVE"
            : status == CacheConstants.STATUS_DISPOSED ? "DISPOSED"
            : status == CacheConstants.STATUS_ERROR ? "ERROR"
            : "UNKNOWN" );
    }

    private void clearAllRegions() throws IOException
    {
        String[] names = cacheHub.getCacheNames();

        for ( int i = 0; i < names.length; i++ )
        {
            cacheHub.getCache( names[ i ] ).removeAll();
        }
    }

    private void clearRegion( String cacheName ) throws IOException
    {
        cacheHub.getCache( cacheName ).removeAll();
    }

    private void removeItem( String cacheName, String key ) throws IOException
    {
        cacheHub.getCache( cacheName ).remove( key );
    }

    /** Stores info on a cache region for the template */
    public class CacheRegionInfo
    {
        String name = null;
        long itemCount = 0;
        long byteCount = 0;
        String status = null;

        public String getName()
        {
            return name;
        }

        public long getItemCount()
        {
            return itemCount;
        }

        public long getByteCount()
        {
            return byteCount;
        }

        public String getStatus()
        {
            return status;
        }
    }

    /**
     * Keeps track of the number of bytes written to it, but doesn't write them
     * anywhere.
     */
    private static class CountingOnlyOutputStream extends OutputStream
    {
        private int count;

        public void write( byte[] b ) throws IOException
        {
            count += b.length;
        }

        public void write( byte[] b, int off, int len ) throws IOException
        {
            count += len;
        }

        public void write( int b ) throws IOException
        {
            count++;
        }

        /**
         * The number of bytes that have passed through this stream.
         */
        public int getCount()
        {
            return this.count;
        }
    }
}
