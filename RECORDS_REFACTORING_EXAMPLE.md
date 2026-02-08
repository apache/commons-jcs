<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
# Refactoring PropertySetter Pattern to Java Records

## Current Approach: PropertySetter with Reflection

The current codebase uses `PropertySetter` (similar to log4j's implementation) to dynamically populate mutable objects from `Properties` using reflection and JavaBeans introspection.

**Example at line 180 in CompositeCacheConfigurator:**
```java
AuxiliaryCacheAttributes auxAttr = ccm.registryAttrGet( auxName );
auxAttr = auxAttr.clone();
PropertySetter.setProperties( auxAttr, props, attrName + "." );
auxAttr.setCacheName( regName );
```

The pattern:
1. Create/clone a mutable object
2. Use reflection to introspect setter methods
3. Parse and convert property strings to target types
4. Invoke setters via reflection
5. Manual post-setup (e.g., `setCacheName()`)

---

## Problems with Current Approach

- **Boilerplate**: Each attribute class needs getters/setters for every property
- **No compile-time safety**: Property names and types are strings, discovered at runtime
- **Mutability**: Objects are mutable, making debugging harder
- **Performance**: Reflection overhead, introspection on every configuration load
- **Inheritance complexity**: Child classes override parent setters, must maintain parallel hierarchies
- **Cloning requirement**: Objects must be cloneable for reuse, adds complexity

---

## Solution: Java Records + Builder Pattern

Java Records (Java 14+, finalized in Java 16) provide immutable, compact data carriers. Combined with a builder pattern, they eliminate reflection while maintaining flexibility.

### Approach 1: Simple Record + Static Builder

**Before:**
```java
// CompositeCacheAttributes.java (current)
public class CompositeCacheAttributes implements ICompositeCacheAttributes {
    private boolean useLateral = DEFAULT_USE_LATERAL;
    private boolean useRemote = DEFAULT_USE_REMOTE;
    private boolean useDisk = DEFAULT_USE_DISK;
    private int maxObjs = DEFAULT_MAX_OBJECTS;
    private long maxMemoryIdleTimeSeconds = DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS;
    private String cacheName;
    private String memoryCacheName;
    // ... 15+ more properties
    
    public void setUseLateral(boolean val) { this.useLateral = val; }
    public void setUseRemote(boolean val) { this.useRemote = val; }
    public void setUseDisk(boolean val) { this.useDisk = val; }
    // ... more setters
}
```

**After with Records:**
```java
public record CompositeCacheAttributes(
    boolean useLateral,
    boolean useRemote,
    boolean useDisk,
    boolean useMemoryShrinker,
    int maxObjs,
    long maxMemoryIdleTimeSeconds,
    long shrinkerIntervalSeconds,
    int maxSpoolPerRun,
    String cacheName,
    String memoryCacheName,
    DiskUsagePattern diskUsagePattern
) implements ICompositeCacheAttributes {
    
    // Compact constructor for validation
    public CompositeCacheAttributes {
        // Add validation here if needed
    }
    
    // Static factory with builder
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private boolean useLateral = DEFAULT_USE_LATERAL;
        private boolean useRemote = DEFAULT_USE_REMOTE;
        private boolean useDisk = DEFAULT_USE_DISK;
        private boolean useMemoryShrinker = DEFAULT_USE_SHRINKER;
        private int maxObjs = DEFAULT_MAX_OBJECTS;
        private long maxMemoryIdleTimeSeconds = DEFAULT_MAX_MEMORY_IDLE_TIME_SECONDS;
        private long shrinkerIntervalSeconds = DEFAULT_SHRINKER_INTERVAL_SECONDS;
        private int maxSpoolPerRun = DEFAULT_MAX_SPOOL_PER_RUN;
        private String cacheName;
        private String memoryCacheName;
        private DiskUsagePattern diskUsagePattern = DiskUsagePattern.SWAP;
        
        public Builder useLateral(boolean val) { this.useLateral = val; return this; }
        public Builder useRemote(boolean val) { this.useRemote = val; return this; }
        public Builder useDisk(boolean val) { this.useDisk = val; return this; }
        public Builder useMemoryShrinker(boolean val) { this.useMemoryShrinker = val; return this; }
        public Builder maxObjs(int val) { this.maxObjs = val; return this; }
        public Builder maxMemoryIdleTimeSeconds(long val) { this.maxMemoryIdleTimeSeconds = val; return this; }
        public Builder shrinkerIntervalSeconds(long val) { this.shrinkerIntervalSeconds = val; return this; }
        public Builder maxSpoolPerRun(int val) { this.maxSpoolPerRun = val; return this; }
        public Builder cacheName(String val) { this.cacheName = val; return this; }
        public Builder memoryCacheName(String val) { this.memoryCacheName = val; return this; }
        public Builder diskUsagePattern(DiskUsagePattern val) { this.diskUsagePattern = val; return this; }
        
        public CompositeCacheAttributes build() {
            return new CompositeCacheAttributes(
                useLateral, useRemote, useDisk, useMemoryShrinker,
                maxObjs, maxMemoryIdleTimeSeconds, shrinkerIntervalSeconds,
                maxSpoolPerRun, cacheName, memoryCacheName, diskUsagePattern
            );
        }
    }
}
```

### Usage Comparison

**Current (PropertySetter with reflection):**
```java
ICompositeCacheAttributes ccAttr = new CompositeCacheAttributes();
PropertySetter.setProperties(ccAttr, props, attrName + ".");
ccAttr.setCacheName(regName);
```

**New (Type-safe builder):**
```java
var builder = ICompositeCacheAttributes.builder();
for (String key : props.stringPropertyNames()) {
    if (key.startsWith(attrName + ".")) {
        String propName = key.substring(attrName.length() + 1);
        String value = props.getProperty(key);
        builder.setPropertyByName(propName, value);  // see below
    }
}
ICompositeCacheAttributes ccAttr = builder.cacheName(regName).build();
```

---

### Approach 2: Type-Safe Configuration Parser (Recommended)

Instead of generic `PropertySetter`, create a **configuration parser** specific to each record type:

```java
public record CompositeCacheAttributes(...) implements ICompositeCacheAttributes {
    
    public static class Parser {
        private static final Map<String, BiConsumer<Builder, String>> PROPERTY_SETTERS = Map.ofEntries(
            Map.entry("useLateral", (b, v) -> b.useLateral(Boolean.parseBoolean(v))),
            Map.entry("useRemote", (b, v) -> b.useRemote(Boolean.parseBoolean(v))),
            Map.entry("useDisk", (b, v) -> b.useDisk(Boolean.parseBoolean(v))),
            Map.entry("useMemoryShrinker", (b, v) -> b.useMemoryShrinker(Boolean.parseBoolean(v))),
            Map.entry("maxObjs", (b, v) -> b.maxObjs(Integer.parseInt(v))),
            Map.entry("maxMemoryIdleTimeSeconds", (b, v) -> b.maxMemoryIdleTimeSeconds(Long.parseLong(v))),
            Map.entry("shrinkerIntervalSeconds", (b, v) -> b.shrinkerIntervalSeconds(Long.parseLong(v))),
            Map.entry("maxSpoolPerRun", (b, v) -> b.maxSpoolPerRun(Integer.parseInt(v))),
            Map.entry("memoryCacheName", (b, v) -> b.memoryCacheName(v)),
            Map.entry("diskUsagePattern", (b, v) -> b.diskUsagePattern(DiskUsagePattern.valueOf(v.toUpperCase())))
        );
        
        public static CompositeCacheAttributes fromProperties(
                Properties props, 
                String prefix, 
                String cacheName) {
            var builder = CompositeCacheAttributes.builder();
            builder.cacheName(cacheName);
            
            final int prefixLen = prefix.length();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    // Ignore nested properties (containing dots after prefix)
                    if (key.indexOf('.', prefixLen + 1) > 0) {
                        continue;
                    }
                    
                    String propName = key.substring(prefixLen);
                    String value = props.getProperty(key);
                    
                    var setter = PROPERTY_SETTERS.get(propName);
                    if (setter != null) {
                        try {
                            setter.accept(builder, value);
                        } catch (NumberFormatException | IllegalArgumentException e) {
                            log.warn("Failed to parse property {}: {}", key, e.getMessage());
                        }
                    } else {
                        log.warn("Unknown property: {}", propName);
                    }
                }
            }
            
            return builder.build();
        }
    }
}
```

**Usage:**
```java
ICompositeCacheAttributes ccAttr = CompositeCacheAttributes.Parser.fromProperties(
    props, attrName + ".", regName
);
```

**Benefits:**
- ✅ Type-safe: Each property has explicit type conversion
- ✅ Compile-time safety: Property names are constants in the map
- ✅ No reflection: Direct method calls via `BiConsumer`
- ✅ Better error handling: Specific exception handling per property type
- ✅ Immutable: Records are immutable by default
- ✅ Performance: No introspection overhead
- ✅ IDE support: Auto-completion and refactoring work perfectly

---

### Approach 3: Annotation-Based Configuration (Most Elegant)

For a more scalable solution that reduces boilerplate, use annotations:

#### Annotation Definition

```java
package org.apache.commons.jcs4.utils.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a record component as a configurable property that can be loaded from Properties.
 * Applied to individual record components.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface ConfigurableProperty {
    
    /**
     * The property name in the configuration file.
     * If not specified, defaults to the record component name.
     */
    String name() default "";
    
    /**
     * The type of conversion needed: "boolean", "int", "long", "double", "string", "enum"
     */
    String type();
    
    /**
     * Default value if property is not provided (as a string).
     */
    String defaultValue() default "";
    
    /**
     * Whether this property is required (no default).
     */
    boolean required() default false;
    
    /**
     * For enum types, the enum class name (if type="enum").
     */
    String enumClass() default "";
}

/**
 * Applied to record classes to enable configuration parsing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configurable {
    /**
     * Optional description of what this configuration represents.
     */
    String description() default "";
}
```

#### Record Definition

```java
@Configurable(description = "Composite cache region attributes")
public record CompositeCacheAttributes(
    @ConfigurableProperty(name = "useLateral", type = "boolean", defaultValue = "true")
    boolean useLateral,
    
    @ConfigurableProperty(name = "useRemote", type = "boolean", defaultValue = "true")
    boolean useRemote,
    
    @ConfigurableProperty(name = "useDisk", type = "boolean", defaultValue = "true")
    boolean useDisk,
    
    @ConfigurableProperty(name = "useMemoryShrinker", type = "boolean", defaultValue = "false")
    boolean useMemoryShrinker,
    
    @ConfigurableProperty(name = "maxObjs", type = "int", defaultValue = "100")
    int maxObjs,
    
    @ConfigurableProperty(name = "maxMemoryIdleTimeSeconds", type = "long", defaultValue = "7200")
    long maxMemoryIdleTimeSeconds,
    
    @ConfigurableProperty(name = "shrinkerIntervalSeconds", type = "long", defaultValue = "30")
    long shrinkerIntervalSeconds,
    
    @ConfigurableProperty(name = "maxSpoolPerRun", type = "int", defaultValue = "-1")
    int maxSpoolPerRun,
    
    @ConfigurableProperty(name = "memoryCacheName", type = "string", 
                         defaultValue = "org.apache.commons.jcs4.engine.memory.lru.LRUMemoryCache")
    String memoryCacheName,
    
    @ConfigurableProperty(name = "diskUsagePattern", type = "enum", 
                         enumClass = "org.apache.commons.jcs4.engine.DiskUsagePattern", 
                         defaultValue = "SWAP")
    DiskUsagePattern diskUsagePattern,
    
    // Not annotated - injected programmatically
    String cacheName
) implements ICompositeCacheAttributes {
    
    public static CompositeCacheAttributes fromProperties(
            Properties props, 
            String prefix, 
            String cacheName) {
        return ConfigurationBuilder.create(CompositeCacheAttributes.class)
            .fromProperties(props, prefix)
            .set("cacheName", cacheName)
            .build();
    }
}
```

#### Generic Configuration Builder

```java
package org.apache.commons.jcs4.utils.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Generic builder for record types annotated with @Configurable.
 * Automatically parses Properties and creates record instances.
 */
public class ConfigurationBuilder<T> {
    
    private static final Log log = Log.getLog(ConfigurationBuilder.class);
    
    private final Class<T> recordClass;
    private final Map<String, Object> values = new HashMap<>();
    private final Map<String, String> errors = new HashMap<>();
    
    private ConfigurationBuilder(Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException(recordClass.getName() + " is not a record");
        }
        this.recordClass = recordClass;
        initializeDefaults();
    }
    
    /**
     * Create a new builder for the given record class.
     */
    public static <T> ConfigurationBuilder<T> create(Class<T> recordClass) {
        return new ConfigurationBuilder<>(recordClass);
    }
    
    /**
     * Initialize default values from annotations.
     */
    private void initializeDefaults() {
        RecordComponent[] components = recordClass.getRecordComponents();
        for (RecordComponent component : components) {
            ConfigurableProperty prop = component.getAnnotation(ConfigurableProperty.class);
            if (prop != null && !prop.defaultValue().isEmpty()) {
                Object value = parseValue(prop.defaultValue(), prop.type(), prop.enumClass());
                if (value != null) {
                    values.put(component.getName(), value);
                }
            }
        }
    }
    
    /**
     * Load properties from a Properties object with a given prefix.
     */
    public ConfigurationBuilder<T> fromProperties(Properties props, String prefix) {
        if (props == null || props.isEmpty()) {
            return this;
        }
        
        RecordComponent[] components = recordClass.getRecordComponents();
        final int prefixLen = prefix.length();
        
        for (RecordComponent component : components) {
            ConfigurableProperty prop = component.getAnnotation(ConfigurableProperty.class);
            if (prop == null) {
                continue;
            }
            
            String propName = prop.name().isEmpty() ? component.getName() : prop.name();
            String fullKey = prefix + propName;
            String value = props.getProperty(fullKey);
            
            if (value != null) {
                try {
                    Object parsed = parseValue(value, prop.type(), prop.enumClass());
                    if (parsed != null) {
                        values.put(component.getName(), parsed);
                        log.debug("Loaded property {}: {}", fullKey, parsed);
                    }
                } catch (Exception e) {
                    String errMsg = String.format(
                        "Failed to parse property '%s' with value '%s' as %s", 
                        fullKey, value, prop.type()
                    );
                    errors.put(component.getName(), errMsg);
                    log.warn("{}: {}", errMsg, e.getMessage());
                }
            } else if (prop.required()) {
                String errMsg = String.format("Required property '%s' not found", fullKey);
                errors.put(component.getName(), errMsg);
                log.error(errMsg);
            }
        }
        
        return this;
    }
    
    /**
     * Explicitly set a property value.
     */
    public ConfigurationBuilder<T> set(String componentName, Object value) {
        if (value != null) {
            values.put(componentName, value);
        }
        return this;
    }
    
    /**
     * Explicitly set a property value from a string.
     */
    public ConfigurationBuilder<T> setProperty(String componentName, String value) {
        RecordComponent component = findComponent(componentName);
        if (component == null) {
            log.warn("No record component named: {}", componentName);
            return this;
        }
        
        ConfigurableProperty prop = component.getAnnotation(ConfigurableProperty.class);
        if (prop != null) {
            try {
                Object parsed = parseValue(value, prop.type(), prop.enumClass());
                if (parsed != null) {
                    values.put(componentName, parsed);
                }
            } catch (Exception e) {
                errors.put(componentName, e.getMessage());
                log.warn("Failed to set property {}: {}", componentName, e.getMessage());
            }
        } else {
            values.put(componentName, value);
        }
        
        return this;
    }
    
    /**
     * Check if there were any errors during configuration.
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }
    
    /**
     * Get all configuration errors.
     */
    public Map<String, String> getErrors() {
        return new HashMap<>(errors);
    }
    
    /**
     * Build the record instance, throwing if any required properties are missing.
     */
    public T build() {
        // Validate all required properties are present
        for (RecordComponent component : recordClass.getRecordComponents()) {
            ConfigurableProperty prop = component.getAnnotation(ConfigurableProperty.class);
            if (prop != null && prop.required() && !values.containsKey(component.getName())) {
                throw new IllegalStateException(
                    "Required property missing: " + component.getName()
                );
            }
        }
        
        return createInstance();
    }
    
    /**
     * Build the record instance, returning null if any errors occurred.
     */
    public T buildSafely() {
        if (hasErrors()) {
            return null;
        }
        return createInstance();
    }
    
    /**
     * Create the record instance using the canonical constructor.
     */
    private T createInstance() {
        try {
            RecordComponent[] components = recordClass.getRecordComponents();
            Class<?>[] paramTypes = new Class<?>[components.length];
            Object[] params = new Object[components.length];
            
            for (int i = 0; i < components.length; i++) {
                paramTypes[i] = components[i].getType();
                Object value = values.get(components[i].getName());
                if (value == null) {
                    // Use null-safe defaults for primitives
                    value = getDefaultForType(components[i].getType());
                }
                params[i] = value;
            }
            
            Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
            T instance = constructor.newInstance(params);
            log.debug("Created instance of {}", recordClass.getSimpleName());
            return instance;
            
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to create instance of " + recordClass.getName(), e
            );
        }
    }
    
    /**
     * Parse a string value to the appropriate type.
     */
    private Object parseValue(String value, String type, String enumClassName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String trimmed = value.trim();
        
        try {
            return switch (type.toLowerCase()) {
                case "boolean" -> Boolean.parseBoolean(trimmed);
                case "int" -> Integer.parseInt(trimmed);
                case "long" -> Long.parseLong(trimmed);
                case "double" -> Double.parseDouble(trimmed);
                case "string" -> value;
                case "enum" -> parseEnum(trimmed, enumClassName);
                default -> {
                    log.warn("Unknown type for conversion: {}", type);
                    yield value;
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Cannot convert '%s' to type %s", value, type), e
            );
        }
    }
    
    /**
     * Parse an enum value.
     */
    @SuppressWarnings("unchecked")
    private Object parseEnum(String value, String enumClassName) throws Exception {
        Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(enumClassName);
        return Enum.valueOf(enumClass, value.toUpperCase());
    }
    
    /**
     * Get default value for a primitive type.
     */
    private Object getDefaultForType(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0.0d;
        if (type == float.class) return 0.0f;
        return null;
    }
    
    /**
     * Find a record component by name.
     */
    private RecordComponent findComponent(String name) {
        for (RecordComponent component : recordClass.getRecordComponents()) {
            if (component.getName().equals(name)) {
                return component;
            }
        }
        return null;
    }
}
```

#### Usage Examples

```java
// Simple case: load from properties
Properties props = new Properties();
props.load(new FileInputStream("cache.properties"));
CompositeCacheAttributes attrs = ConfigurationBuilder
    .create(CompositeCacheAttributes.class)
    .fromProperties(props, "jcs.region.myregion.cacheattributes.")
    .set("cacheName", "myregion")
    .build();

// With error handling
var builder = ConfigurationBuilder.create(CompositeCacheAttributes.class)
    .fromProperties(props, "jcs.region.myregion.cacheattributes.")
    .set("cacheName", "myregion");

if (builder.hasErrors()) {
    builder.getErrors().forEach((prop, error) ->
        log.error("Configuration error in {}: {}", prop, error)
    );
    return null;
}

CompositeCacheAttributes attrs = builder.build();

// In CompositeCacheConfigurator.parseCompositeCacheAttributes()
protected ICompositeCacheAttributes parseCompositeCacheAttributes(
        final Properties props,
        final String regName,
        final ICompositeCacheAttributes defaultCCAttr,
        final String regionPrefix) {
    
    final String attrName = regionPrefix + regName + CACHE_ATTRIBUTE_PREFIX;
    
    try {
        var builder = ConfigurationBuilder.create(CompositeCacheAttributes.class)
            .fromProperties(props, attrName + ".");
        
        if (builder.hasErrors()) {
            log.error("Configuration errors for region {}: {}", 
                regName, builder.getErrors());
            return defaultCCAttr;
        }
        
        return builder.set("cacheName", regName).build();
    } catch (Exception e) {
        log.error("Failed to parse cache attributes for region {}", regName, e);
        return defaultCCAttr;
    }
}

// Works with other record types too (ElementAttributes, etc.)
ElementAttributes elemAttrs = ConfigurationBuilder
    .create(ElementAttributes.class)
    .fromProperties(props, "jcs.region.myregion.elementattributes.")
    .build();
```

#### Advantages of Annotation-Based Approach

- **Single Source of Truth**: Metadata is embedded in the record definition
- **Type-Safe**: Compiler ensures properties exist and have correct types
- **Minimal Boilerplate**: No need to write parser classes for each record type
- **Reusable Infrastructure**: `ConfigurationBuilder` works for any `@Configurable` record
- **Runtime Flexibility**: Properties can be added without recompilation
- **Better IDE Support**: Annotations provide navigation and quick-fixes
- **Self-Documenting**: Annotations serve as documentation
- **Extensible**: Easy to add new annotation features (validation, transformation, etc.)
```

---

## Handling Inheritance (Child Types)

If you have child attribute classes inheriting from parent classes, records handle this differently:

**Current (with inheritance):**
```java
public abstract class AbstractAuxiliaryCacheAttributes { ... }
public class JDBCAuxiliaryCacheAttributes extends AbstractAuxiliaryCacheAttributes { ... }
```

**With Records (sealed types):**
```java
// Base record
public record AuxiliaryCacheAttributes(
    String name,
    int maxFailureWaitTimeSeconds,
    long failureCountWaitTimeSeconds,
    // common properties
) { }

// Sealed type hierarchy (Java 17+)
public sealed record JDBCAuxiliaryCacheAttributes(
    String name,
    int maxFailureWaitTimeSeconds,
    long failureCountWaitTimeSeconds,
    String username,
    String password,
    String driver,
    // JDBC-specific properties
) extends AuxiliaryCacheAttributes { }

public sealed record RemoteAuxiliaryCacheAttributes(
    String name,
    int maxFailureWaitTimeSeconds,
    long failureCountWaitTimeSeconds,
    String remoteHost,
    int remotePort,
    // Remote-specific properties
) extends AuxiliaryCacheAttributes { }
```

---

## Migration Strategy

1. **Phase 1**: Create new record classes alongside existing ones
2. **Phase 2**: Add parsers for records (Approach 2 above)
3. **Phase 3**: Update configurators to use new parsers
4. **Phase 4**: Gradually deprecate old attribute classes
5. **Phase 5**: Remove PropertySetter dependency

**Example migration in CompositeCacheConfigurator:**

```java
// OLD (lines 258-266)
protected ICompositeCacheAttributes parseCompositeCacheAttributes(...) {
    ICompositeCacheAttributes ccAttr = new CompositeCacheAttributes();
    final String attrName = regionPrefix + regName + CACHE_ATTRIBUTE_PREFIX;
    PropertySetter.setProperties(ccAttr, props, attrName + ".");
    ccAttr.setCacheName(regName);
    return ccAttr;
}

// NEW
protected ICompositeCacheAttributes parseCompositeCacheAttributes(...) {
    final String attrName = regionPrefix + regName + CACHE_ATTRIBUTE_PREFIX;
    return CompositeCacheAttributes.Parser.fromProperties(
        props, attrName + ".", regName
    );
}
```

---

## Summary

| Aspect | PropertySetter | Record + Builder | Record + Parser | Record + Annotation |
|--------|---|---|---|---|
| Boilerplate | High | Medium | Low | Very Low |
| Type Safety | Low | High | High | High |
| Performance | Low (Reflection) | High | High | Medium |
| Error Handling | Generic | Better | Best | Good |
| IDE Support | Limited | Excellent | Excellent | Excellent |
| Maintenance | Difficult | Easier | Easy | Easiest |
| Learning Curve | Moderate | Low | Low | Medium |
| Immutability | No | Yes | Yes | Yes |

**Recommendation**: Start with **Approach 2 (Type-Safe Parser)** for immediate benefits with minimal refactoring. Evolve to **Approach 3 (Annotation-Based)** if configuration complexity grows.
