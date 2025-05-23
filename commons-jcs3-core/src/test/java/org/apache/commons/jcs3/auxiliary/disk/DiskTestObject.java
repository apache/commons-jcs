package org.apache.commons.jcs3.auxiliary.disk;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.Serializable;
import java.util.Arrays;

/**
 * Resembles a cached image.
 */
public class DiskTestObject implements Serializable
{
    /** Don't change */
    private static final long serialVersionUID = 1L;

    /**
     * Key
     */
    public Integer id;

    /**
     * Byte size
     */
    public byte[] imageBytes;

    /**
     * @param id
     * @param imageBytes
     */
    public DiskTestObject(final Integer id, final byte[] imageBytes)
    {
        this.id = id;
        this.imageBytes = imageBytes;
    }

    /**
     * @see Object#equals(Object other)
     */
    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof DiskTestObject)
        {
            final DiskTestObject o = (DiskTestObject) other;
            if (id != null) {
                return id.equals(o.id) && Arrays.equals(imageBytes, o.imageBytes);
            }
            if (id == null && o.id == null) {
                return Arrays.equals(imageBytes, o.imageBytes);
            }
        }
        return false;
    }

    /**
     * @see Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

}
