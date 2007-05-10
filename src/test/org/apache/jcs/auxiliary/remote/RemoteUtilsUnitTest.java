package org.apache.jcs.auxiliary.remote;

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

import java.rmi.RemoteException;

import junit.framework.TestCase;

/**
 * Simple tests for remote utils.  It is difficult to verify most of the things is does.
 *
 * @author Aaron Smuts
 *
 */
public class RemoteUtilsUnitTest
    extends TestCase
{

    /**
     * Call create registry twice.
     *
     * <p>
     * The exception is in the security manager setting.
     *
     */
    public void testCreateRegistryTwice()
    {
        try
        {
            RemoteUtils.createRegistry( 1102 );
        }
        catch ( RemoteException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try
        {
            RemoteUtils.createRegistry( 1102 );
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
