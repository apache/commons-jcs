package org.apache.jcs.auxiliary.disk.indexed;


/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Used to dump out a Disk cache from disk for debugging.
 *
 */
public class IndexedDiskDumper
{
    /**
     * The main program for the DiskDumper class
     *
     * @param args The command line arguments
     */
    public static void main( String[] args )
    {
        if ( args.length != 1 )
        {
            System.out.println( "Usage: java org.apache.jcs.auxiliary.disk.DiskDump <cache_name>" );
            System.exit( 0 );
        }

        IndexedDiskCacheAttributes attr = new IndexedDiskCacheAttributes();

        attr.setCacheName( args[0] );
        attr.setDiskPath( args[0] );

        final IndexedDiskCache rc = new IndexedDiskCache( attr );
        rc.dump();
        System.exit( 0 );
    }
}
