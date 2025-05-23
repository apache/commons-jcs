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
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.jcs.yajcache.lang.annotation.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 */
// @CopyRightApache
// http://www.netbeans.org/issues/show_bug.cgi?id=53704
public enum BeanUtils {
    inst;
    private static final boolean debug = false;
    private final Log log = debug ? LogFactory.getLog(this.getClass()) : null;

    public <B> B cloneDeep(final B bean) {
        if (bean == null
        ||  ClassUtils.inst.isImmutable(bean)) {
            return bean;
        }
        return (B)fromXmlByteArray(toXmlByteArray(bean));
    }
    public <B> B cloneShallow(final B bean) {
        if (bean == null
        ||  ClassUtils.inst.isImmutable(bean)) {
            return bean;
        }
        try {
            return (B)org.apache.commons.beanutils.BeanUtils.cloneBean(bean);
        } catch (final Exception ex) {
            LogFactory.getLog(this.getClass()).error("", ex);
            throw new RuntimeException(ex);
        }
    }
    @TODO("Replace XMLEncoder with something fast.  Maybe XStream ?")
    public @NonNullable byte[] toXmlByteArray(@NonNullable final Object bean) {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final XMLEncoder out = new XMLEncoder(bos);
        out.writeObject(bean);
        out.close();
        return bos.toByteArray();
    }
    @TODO("Replace XMLDecoder with something fast.  Maybe XStream ?")
    public @NonNullable Object fromXmlByteArray(@NonNullable final byte[] bytes) {
        if (debug) {
            log.debug(new String(bytes));
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        final XMLDecoder in = new XMLDecoder(bis);
        final Object toBean = in.readObject();
        in.close();
        return toBean;
    }
}
