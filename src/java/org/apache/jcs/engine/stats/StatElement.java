package org.apache.jcs.engine.stats;

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

import org.apache.jcs.engine.stats.behavior.IStatElement;


/**
 * @author aaronsm
 *
 */
public class StatElement
    implements IStatElement
{

    private String name = null;

    private String data = null;

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.stats.behavior.IStatElement#getName()
     */
    public String getName()
    {
        return name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.stats.behavior.IStatElement#setName(java.lang.String)
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.stats.behavior.IStatElement#getData()
     */
    public String getData()
    {
        return data;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.jcs.engine.stats.behavior.IStatElement#setData(java.lang.String)
     */
    public void setData( String data )
    {
        this.data = data;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( name + " = " + data );
        return buf.toString();
    }
}
