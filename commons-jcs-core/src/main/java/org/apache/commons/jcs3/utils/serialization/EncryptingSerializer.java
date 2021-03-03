package org.apache.commons.jcs3.utils.serialization;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;
import org.apache.commons.jcs3.log.Log;
import org.apache.commons.jcs3.log.LogManager;

/**
 * Performs serialization and de-serialization. It encrypts and decrypts the
 * value.
 */
public class EncryptingSerializer extends StandardSerializer
{
    /** The logger */
    private static final Log log = LogManager.getLog( EncryptingSerializer.class );

    private static final String DEFAULT_CIPHER = "AES/ECB/PKCS5Padding";

    /** The pre-shared key */
    private SecretKeySpec secretKey;

    /** The cipher transformation */
    private String cipherTransformation = DEFAULT_CIPHER;

    /** Wrapped serializer */
    private final IElementSerializer serializer;

    /**
     * Default constructor
     */
    public EncryptingSerializer()
    {
        this(new StandardSerializer());
    }

    /**
     * Wrapper constructor
     *
     * @param serializer
     *            the wrapped serializer
     */
    public EncryptingSerializer(IElementSerializer serializer)
    {
        this.serializer = serializer;
    }

    /**
     * Set the pre-shared key for encryption and decryption
     *
     * @param psk the key
     */
    public void setPreSharedKey(String psk)
    {
        try
        {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = sha.digest(psk.getBytes(StandardCharsets.UTF_8));
            secretKey = new SecretKeySpec(key, 0, 16, "AES");
        }
        catch (NoSuchAlgorithmException e)
        {
            log.error("Cannot set pre-shared key", e);
        }
    }

    /**
     * Set the cipher transformation for encryption and decryption
     * Default is AES/ECB/PKCS5Padding
     *
     * @param transformation the transformation
     */
    public void setAesCipherTransformation(String transformation)
    {
        this.cipherTransformation = transformation;
    }

    private byte[] encrypt(byte[] source) throws IOException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(source);
        }
        catch (Exception e)
        {
            throw new IOException("Error while encrypting", e);
        }
    }

    private byte[] decrypt(byte[] source) throws IOException
    {
        try
        {
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(source);
        }
        catch (Exception e)
        {
            throw new IOException("Error while decrypting", e);
        }
    }

    /**
     * Serializes an object using default serialization. Encrypts the byte array.
     * <p>
     * @param obj object
     * @return byte[]
     * @throws IOException on i/o problem
     */
    @Override
    public <T> byte[] serialize( final T obj )
        throws IOException
    {
        final byte[] unencrypted = serializer.serialize(obj);
        return encrypt(unencrypted);
    }

    /**
     * Uses default de-serialization to turn a byte array into an object. Decrypts the value
     * first. All exceptions are converted into IOExceptions.
     * <p>
     * @param data data bytes
     * @param loader class loader to use
     * @return Object
     * @throws IOException on i/o problem
     * @throws ClassNotFoundException if class is not found during deserialization
     */
    @Override
    public <T> T deSerialize( final byte[] data, final ClassLoader loader )
        throws IOException, ClassNotFoundException
    {
        if ( data == null )
        {
            return null;
        }

        final byte[] deccrypted = decrypt(data);
        return serializer.deSerialize(deccrypted, loader);
    }
}
