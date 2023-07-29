package org.apache.commons.jcs.auxiliary.disk.file;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.auxiliary.disk.AbstractDiskCacheAttributes;

/**
 * Configuration values for the file disk cache.
 */
public class FileDiskCacheAttributes
    extends AbstractDiskCacheAttributes
{
    /** Don't change. */
    private static final long serialVersionUID = -7371586172678836062L;

    /** Default file count limit: -1 means no limit */
    public static final int DEFAULT_MAX_NUMBER_OF_FILES = -1;

    /** Max number of files */
    private int maxNumberOfFiles = DEFAULT_MAX_NUMBER_OF_FILES;

    /** Default limit on the number of times we will retry a delete. */
    public static final int DEFAULT_MAX_RETRIES_ON_DELETE = 10;

    /** Max number of retries on delete */
    private int maxRetriesOnDelete = DEFAULT_MAX_RETRIES_ON_DELETE;

    /** Default touch rule. */
    public static final boolean DEFAULT_TOUCH_ON_GET = false;

    /** Default limit on the number of times we will retry a delete. */
    public static final int DEFAULT_MAX_RETRIES_ON_TOUCH = 10;

    /** Max number of retries on touch  */
    private int maxRetriesOnTouch = DEFAULT_MAX_RETRIES_ON_TOUCH;

    /**
     * Should we touch on get. If so, we will reset the last modified time. If you have a max file
     * size set, this will make the removal strategy LRU. If this is false, then the oldest will be
     * removed.
     */
    private boolean touchOnGet = DEFAULT_TOUCH_ON_GET;

    /**
     * @param maxNumberOfFiles the maxNumberOfFiles to set
     */
    public void setMaxNumberOfFiles( int maxNumberOfFiles )
    {
        this.maxNumberOfFiles = maxNumberOfFiles;
    }

    /**
     * @return the maxNumberOfFiles
     */
    public int getMaxNumberOfFiles()
    {
        return maxNumberOfFiles;
    }

    /**
     * @param maxRetriesOnDelete the maxRetriesOnDelete to set
     */
    public void setMaxRetriesOnDelete( int maxRetriesOnDelete )
    {
        this.maxRetriesOnDelete = maxRetriesOnDelete;
    }

    /**
     * @return the maxRetriesOnDelete
     */
    public int getMaxRetriesOnDelete()
    {
        return maxRetriesOnDelete;
    }

    /**
     * @param touchOnGet the touchOnGet to set
     */
    public void setTouchOnGet( boolean touchOnGet )
    {
        this.touchOnGet = touchOnGet;
    }

    /**
     * @return the touchOnGet
     */
    public boolean isTouchOnGet()
    {
        return touchOnGet;
    }

    /**
     * @param maxRetriesOnTouch the maxRetriesOnTouch to set
     */
    public void setMaxRetriesOnTouch( int maxRetriesOnTouch )
    {
        this.maxRetriesOnTouch = maxRetriesOnTouch;
    }

    /**
     * @return the maxRetriesOnTouch
     */
    public int getMaxRetriesOnTouch()
    {
        return maxRetriesOnTouch;
    }

    /**
     * Write out the values for debugging purposes.
     * <p>
     * @return String
     */
    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append( "DiskFileCacheAttributes " );
        str.append( "\n diskPath = " + super.getDiskPath() );
        str.append( "\n maxNumberOfFiles   = " + getMaxNumberOfFiles() );
        str.append( "\n maxRetriesOnDelete  = " + getMaxRetriesOnDelete() );
        return str.toString();
    }
}
