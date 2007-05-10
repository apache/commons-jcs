package org.apache.jcs.utils.serialization;

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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.jcs.engine.behavior.IElementSerializer;

/**
 * Performs default serialization and de-serialization.
 * <p>
 * @author Aaron Smuts
 */
public class StandardSerializer
    implements IElementSerializer
{
    /**
     * Serializes an object using default serilaization.
     */
    public byte[] serialize( Serializable obj )
        throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        try
        {
            oos.writeObject( obj );
        }
        finally
        {
            oos.close();
        }
        return baos.toByteArray();
    }

    /**
     * Uses default de-serialization to turn a byte array into an object. All
     * exceptions are converted into IOExceptions.
     */
    public Object deSerialize( byte[] data )
        throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream bais = new ByteArrayInputStream( data );
        BufferedInputStream bis = new BufferedInputStream( bais );
        ObjectInputStream ois = new ObjectInputStream( bis );
        try
        {
            try
            {
                return ois.readObject();
            }
            catch ( IOException e )
            {
                throw e;
            }
            catch ( ClassNotFoundException e )
            {
                throw e;
            }
        }
        finally
        {
            ois.close();
        }
    }
}
