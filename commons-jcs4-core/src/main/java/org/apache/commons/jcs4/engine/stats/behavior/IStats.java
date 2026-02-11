package org.apache.commons.jcs4.engine.stats.behavior;

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
import java.util.List;

/**
 * This interface defines the common behavior for a stats holder.
 */
public interface IStats
    extends Serializable
{

    /**
     * Return generic statistical or historical data.
     *
     * @return list of IStatElements
     */
    List<IStatElement<?>> getStatElements();

    /**
     * Gets the type name, such as "LRU Memory Cache." No formal type is defined.
     *
     * @return String
     */
    String getTypeName();

    /**
     * Adds generic statistical or historical data.
     *
     * @param stats
     */
    void addStatElements( List<IStatElement<?>> stats );

    /**
     * Adds generic statistical or historical data.
     *
     * @param stats
     */
    void addStatElement(IStatElement<?> stats);

    /**
     * Adds generic statistical or historical data.
     *
     * @param name name of the StatElement
     * @param data value of the StatElement
     */
    <V> void addStatElement(String name, V data);

    /**
     * Sets the type name, such as "LRU Memory Cache." No formal type is defined.
     * If we need formal types, we can use the cachetype param
     *
     * @param name
     */
    void setTypeName( String name );
}
