/*
 * TestSerializable.java
 *
 * $Revision$ $Date$
 */

package net.sf.yajcache.util;

import java.io.Serializable;
import org.apache.commons.lang.builder.EqualsBuilder;
import net.sf.yajcache.annotate.*;

/**
 *
 * @author Hanson Char
 */
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
