package org.apache.jcs.engine.control;

/**
 * Description of the Class
 *
 * @author asmuts
 * @created January 15, 2002
 */
public class CompositeCacheManagerTester
{

    /** Description of the Method */
    public static void main( String args[] )
    {

        CompositeCacheManagerTester ccmt = new CompositeCacheManagerTester();

        String propsFile = "/cache.ccf";
        if ( args.length > 0 )
        {
            propsFile = args[0];
        }
        CompositeCacheManager ccm = new CompositeCacheManager( propsFile );
    }

}
