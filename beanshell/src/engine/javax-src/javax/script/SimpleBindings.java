/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;

import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Set;

/**
 * A simple implementation of Bindings backed by
 * a <code>HashMap</code> or some other specified <code>Map</code>.
 * 
 * @author Mike Grogan
 * @version 1.0
 * @since 1.6
 */
public class SimpleBindings implements Bindings {

    /**
     * The <code>Map</code> field stores the attributes.
     */
    private Map<String,Object> map;
    
    /**
     * Constructor uses an existing <code>Map</code> to store the values.
     * @param m The <code>Map</code> backing this <code>SimpleBindings</code>.
     * @throws NullPointerException if m is null
     */
    public SimpleBindings(Map<String,Object> m) {
        if (m == null) {
            throw new NullPointerException();
        }
        this.map = m;
    }
    
    /**
     * Default constructor uses a <code>HashMap</code>.
     */
    public SimpleBindings() {
        this(new HashMap<String,Object>());
    }

    /**
     * Sets the specified key/value in the underlying <code>map</code> field.
     *
     * @param name Name of value
     * @param value Value to set.
     *
     * @return Previous value for the specified key.  Returns null if key was previously 
     * unset.
     *
     * @throws <code>NullPointerException</code> if the name is null.
     * @throws <code>IllegalArgumentException</code> if the name is empty.
     */
    public Object put(String name, Object value) {
        checkKey(name);
        map.put(name,value);
        return value;
    }
    
    /**
     * <code>putAll</code> is implemented using <code>Map.putAll</code>.
     *
     * @param toMerge The <code>Map</code> of values to add.
     *
     * @throws <code>NullPointerException</code> if some key in the specified <code>Map</code>
     * is null.
     * @throws <code>IllegalArgumentException</code> if some key in the specified <code>Map</code> is empty String.
     */
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        for (Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) { 
            String key = entry.getKey();
            checkKey(key);
            put(key, entry.getValue());
        }
    }
    
    /** {@inheritDoc} */
    public void clear() {
        map.clear();
    }    
    
    /** {@inheritDoc} */
    public boolean containsKey(Object key) {
        checkKey(key);
        return map.containsKey(key);
    }    
    
    /** {@inheritDoc} */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }    
    
    /** {@inheritDoc} */
    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
    
    /** {@inheritDoc} */
    public Object get(Object key) {
        checkKey(key);
        return map.get(key);
    }
    
    /** {@inheritDoc} */
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    /** {@inheritDoc} */
    public Set<String> keySet() {
        return map.keySet();
    }
    
    
    /** {@inheritDoc} */
    public Object remove(Object key) {
        checkKey(key);
        return map.remove(key);
    }
    
    /** {@inheritDoc} */
    public int size() {
        return map.size();
    }
    
    /** {@inheritDoc} */
    public Collection<Object> values() {
        return map.values();
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("key should be a String");
        }
        if (key.equals("")) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }
}
