/*
 * %W% %E% %U%
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTAIL. Use is subject to license terms.
 */

package javax.script;
import java.util.Map;

/**
 * A mapping of key/value pairs, all of whose keys are
 * <code>Strings</code>.
 *
 * @version 1.0
 * @author Mike Grogan
 * @since 1.6
 */
public interface Bindings extends Map<String, Object> {
    /**
     * Set a named value.
     *
     * @param name The name associated with the value.
     * @param value The value associated with the name.
     *
     * @return The value previously associated with the given name.
     * Returns null if no value was previously associated with the name.
     *
     * @throws <code>NullPointerException</code> if the name is null.
     * @throws <code>IllegalArgumentException</code> if the name is empty String.
     */
    public Object put(String name, Object value);
    
    /**
     * Adds all the mappings in a given <code>Map</code> to this <code>Bindings</code.
     * @param toMerge The <code>Map</code> to merge with this one.
     *
     * @throws <code>NullPointerException</code> if some key in the map is null.
     * @throws <code>IllegalArgumentException</code> if some key in the map is an empty String.
     */
    public void putAll(Map<? extends String, ? extends Object> toMerge);
}
