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
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.jcs3.engine.behavior.IElementSerializer;

/**
 * Performs serialization and de-serialization. It encrypts and decrypts the
 * value.
 *
 * @since 3.1
 */
public class EncryptingSerializer extends StandardSerializer
{
    private static final String DEFAULT_SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String DEFAULT_CIPHER = "AES/ECB/PKCS5Padding";
    private static final int KEYHASH_ITERATION_COUNT = 1000;
    private static final int KEY_LENGTH = 256;
    private static final int TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int SALT_LENGTH = 16;

    /** The pre-shared key */
    private String psk;

    /** The cipher transformation */
    private String cipherTransformation = DEFAULT_CIPHER;

    /** The random source */
    private final SecureRandom secureRandom;

    /** The secret-key factory */
    private final SecretKeyFactory secretKeyFactory;

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

        try
        {
            this.secureRandom = new SecureRandom();
            this.secretKeyFactory = SecretKeyFactory.getInstance(DEFAULT_SECRET_KEY_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("Could not set up encryption tools", e);
        }
    }

    /**
     * Set the pre-shared key for encryption and decryption
     *
     * @param psk the key
     */
    public void setPreSharedKey(String psk)
    {
        this.psk = psk;
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

    private byte[] getRandomBytes(int length)
    {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);

        return bytes;
    }

    private SecretKey createSecretKey(String password, byte[] salt) throws InvalidKeySpecException
    {
        /* Derive the key, given password and salt. */
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt,
                KEYHASH_ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = secretKeyFactory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] encrypt(byte[] source) throws IOException
    {
        try
        {
            byte[] salt = getRandomBytes(SALT_LENGTH);
            byte[] iv = getRandomBytes(IV_LENGTH);

            SecretKey secretKey = createSecretKey(psk, salt);
            Cipher cipher = Cipher.getInstance(cipherTransformation);
            if (cipher.getAlgorithm().startsWith("AES/GCM"))
            {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            }
            else
            {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }

            byte[] encrypted = cipher.doFinal(source);

            // join initial vector, salt and encrypted data for later decryption
            return ByteBuffer.allocate(IV_LENGTH + SALT_LENGTH + encrypted.length)
                    .put(iv)
                    .put(salt)
                    .put(encrypted)
                    .array();
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException |
                InvalidAlgorithmParameterException e)
        {
            throw new IOException("Error while encrypting", e);
        }
    }

    private byte[] decrypt(byte[] source) throws IOException
    {
        try
        {
            // split data in initial vector, salt and encrypted data
            ByteBuffer wrapped = ByteBuffer.wrap(source);

            byte[] iv = new byte[IV_LENGTH];
            wrapped.get(iv);

            byte[] salt = new byte[SALT_LENGTH];
            wrapped.get(salt);

            byte[] encrypted = new byte[wrapped.remaining()];
            wrapped.get(encrypted);

            SecretKey secretKey = createSecretKey(psk, salt);
            Cipher cipher = Cipher.getInstance(cipherTransformation);

            if (cipher.getAlgorithm().startsWith("AES/GCM"))
            {
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            }
            else
            {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            return cipher.doFinal(encrypted);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException |
                IllegalBlockSizeException | InvalidKeyException | InvalidKeySpecException |
                InvalidAlgorithmParameterException e)
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
