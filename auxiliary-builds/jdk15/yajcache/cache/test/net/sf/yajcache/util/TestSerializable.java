/*
 * TestSerializable.java
 *
 * Created on 17 January 2005, 23:35
 */

package net.sf.yajcache.util;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;

/**
 *
 * @author Hanson Char
 */
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
