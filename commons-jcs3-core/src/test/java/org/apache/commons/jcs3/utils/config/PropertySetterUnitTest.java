package org.apache.commons.jcs3.utils.config;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Test property settings
 */
class PropertySetterUnitTest
{
    enum EnumTest { ONE, TWO, THREE }

    @Test
    void testConvertArg()
    {
        final PropertySetter ps = new PropertySetter(this);
        final Object s = ps.convertArg("test", String.class);
        assertEquals( "test", s, "Should be a string" );

        final Object i = ps.convertArg("1", Integer.TYPE);
        assertEquals( Integer.valueOf( 1 ), i, "Should be an integer" );

        final Object l = ps.convertArg("1", Long.TYPE);
        assertEquals( Long.valueOf( 1 ), l, "Should be a long" );

        final Object b = ps.convertArg("true", Boolean.TYPE);
        assertEquals( Boolean.TRUE, b, "Should be a boolean" );

        final Object e = ps.convertArg("TWO", EnumTest.class);
        assertEquals( EnumTest.TWO, e, "Should be an enum" );

        final Object f = ps.convertArg("test.conf", File.class);
        assertInstanceOf( File.class, f, "Should be a file" );
    }

}
