package org.apache.commons.jcs.yajcache.util;

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

import java.io.Serializable;

import org.apache.commons.jcs.yajcache.lang.annotation.CopyRightApache;
import org.apache.commons.jcs.yajcache.lang.annotation.TestOnly;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 *
 * @author Hanson Char
 */
@CopyRightApache
@TestOnly
public class TestSerializable implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    public TestSerializable() {
    }
    /** Creates a new instance of TestSerializable */
    public TestSerializable(String name) {
        this.name = name;
    }
    public int hashCode() {
        return this.name == null ? 0 : this.name.hashCode();
    }
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
