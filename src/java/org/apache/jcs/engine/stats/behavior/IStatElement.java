package org.apache.jcs.engine.stats.behavior;

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

/**
 * IAuxiliaryCacheStats will hold these IStatElements.
 *
 * @author aaronsm
 *
 */
public interface IStatElement
{

    /**
     * Get the name of the stat element, ex. HitCount
     *
     * @return the stat element name
     */
    public abstract String getName();

    /**
     *
     * @param name
     */
    public abstract void setName( String name );

    /**
     * Get the data, ex. for hit count you would get a String value for some
     * number.
     *
     * @return String data
     */
    public abstract String getData();

    /**
     * Set the data for this element.
     *
     * @param data
     */
    public abstract void setData( String data );

}
