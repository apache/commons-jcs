/*
 * BeanUtils.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Hanson Char
 */
public enum BeanUtils {
    inst;
    private static final boolean debug = false;
    private Log log = debug ? LogFactory.getLog(this.getClass()) : null;
    
    public <B> B cloneDeep(B bean) {
        if (bean == null
        ||  ClassUtils.inst.isImmutable(bean))
            return bean;
        return (B)this.fromXmlByteArray(this.toXmlByteArray(bean));
    }
    public <B> B cloneShallow(B bean) {
        if (bean == null
        ||  ClassUtils.inst.isImmutable(bean))
            return bean;
        try {
            return (B)org.apache.commons.beanutils.BeanUtils.cloneBean(bean);
        } catch(Exception ex) {
            LogFactory.getLog(this.getClass()).error("", ex);
            throw new RuntimeException(ex);
        }
    }
    public byte[] toXmlByteArray(Object bean) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLEncoder out = new XMLEncoder(bos);
        out.writeObject(bean);
        out.close();
        return bos.toByteArray();
    }
    public Object fromXmlByteArray(byte[] bytes) {
        if (debug)
            log.debug(new String(bytes));
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        XMLDecoder in = new XMLDecoder(bis);
        Object toBean = in.readObject();
        in.close();
        return toBean;
    }
}
