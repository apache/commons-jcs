package org.apache.jcs.engine;

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
 * Constants used throughout the JCS cache engine
 * <p>
 * @version $Id$
 */
public interface CacheConstants
{
    /** This is the name of the config file that we will look for by default. */
    public static final String DEFAULT_CONFIG = "/cache.ccf";

    /** Cache alive status. */
    public final static int STATUS_ALIVE = 1;

    /** Cache disposed status. */
    public final static int STATUS_DISPOSED = 2;

    /** Cache in error. */
    public final static int STATUS_ERROR = 3;

    /** Delimiter of a cache name component. This is used for hierarchical deletion */
    public final static String NAME_COMPONENT_DELIMITER = ":";
}
