package bsh.engine;

import javax.script.ScriptContext;
import java.util.*;
import static javax.script.ScriptContext.*;

/**
 * This class implements an ENGINE_SCOPE centric Map view of the ScriptContext
 * for engine implementations.  This class can be used to simplify engine
 * implementations which have the capability to bind their namespaces to Maps
 * or other external interfaces.
 *
 * Get operations on this view delegate to the
 * ScriptContext inheriting get() method that automatically traverses the
 * binding scopes in order or precedence.  Put operations on this view always
 * store values in the ENGINE_SCOPE bindings.  Other operations such as
 * size() and contains() are implemented appropriately, but perhaps not as
 * efficiently as possible.
 *
*/
public class ScriptContextEngineView implements Map<String,Object>
{
	ScriptContext context;

	public ScriptContextEngineView( ScriptContext context )
	{
		this.context = context;
	}

	/**
	 * Returns the number of unique object bindings in all scopes.
	 * (duplicate, shadowed, bindings count as a single binging).
	 */
	public int size()
	{
		return totalKeySet().size();
	}

	/**
	 * Returns true if no bindings are present in any scope of the context.
	 */
	public boolean isEmpty()
	{
		return totalKeySet().size() == 0;
	}

	/**
	 * Returns true if the key name is bound in any scope in the context.
	 * The key must be a String.
	 *
	 * @param key key whose presence in this map is to be tested.
	 *
	 * @return <tt>true</tt> if this map contains a mapping for the specified key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for this
	 * map (optional).
	 * @throws NullPointerException if the key is <tt>null</tt> and this map does
	 * not permit <tt>null</tt> keys (optional).
	 */
	// Why isn't the compiler allowing this?
	//public boolean containsKey( String key )
	public boolean containsKey( Object key )
	{
		return context.getAttribute( (String)key ) != null;
	}

	/**
	 * Returns <tt>true</tt> if this map maps one or more keys to the specified
	 * value.  More formally, returns <tt>true</tt> if and only if this map
	 * contains at least one mapping to a value <tt>v</tt> such that
	 * <tt>(value==null ? v==null : value.equals(v))</tt>.  This operation will
	 * probably require time linear in the map size for most implementations of the
	 * <tt>Map</tt> interface.
	 *
	 * @param value value whose presence in this map is to be tested.
	 *
	 * @return <tt>true</tt> if this map maps one or more keys to the specified
	 *         value.
	 *
	 * @throws ClassCastException if the value is of an inappropriate type for this
	 * map (optional).
	 * @throws NullPointerException if the value is <tt>null</tt> and this map does
	 * not permit <tt>null</tt> values (optional).
	 */
	public boolean containsValue( Object value )
	{
		Set values = totalValueSet();
		return values.contains( value );
	}

	/**
	 * Returns the value bound in the most specific (lowest numbered)
	 * bindings space for this key.
	 * key must be a String.
	 *
	 * @param key key whose associated value is to be returned.
	 *
	 * @return the value to which this map maps the specified key, or <tt>null</tt>
	 *         if the map contains no mapping for this key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for this
	 * map (optional).
	 * @throws NullPointerException if the key is <tt>null</tt> and this map does
	 * not permit <tt>null</tt> keys (optional).
	 * @see #containsKey(Object)
	 */
	public Object get( Object key )
	{
		return context.getAttribute( (String)key );
	}

	/**
	 * Set the key, value binding in the ENGINE_SCOPE of the context.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 *
	 * @return previous value associated with specified key, or <tt>null</tt> if
	 *         there was no mapping for key.  A <tt>null</tt> return can also
	 *         indicate that the map previously associated <tt>null</tt> with the
	 *         specified key, if the implementation supports <tt>null</tt> values.
	 *
	 * @throws UnsupportedOperationException if the <tt>put</tt> operation is not
	 * supported by this map.
	 * @throws ClassCastException if the class of the specified key or value
	 * prevents it from being stored in this map.
	 * @throws IllegalArgumentException if some aspect of this key or value
	 * prevents it from being stored in this map.
	 * @throws NullPointerException if this map does not permit <tt>null</tt> keys
	 * or values, and the specified key or value is <tt>null</tt>.
	 */
	public Object put( String key, Object value )
	{
		Object oldValue =
			context.getAttribute( key, ENGINE_SCOPE );
		context.setAttribute( key, value, ENGINE_SCOPE );
		return oldValue;
	}


	/**
	 * Put the bindings into the ENGINE_SCOPE of the context.
	 *
	 * @param t Mappings to be stored in this map.
	 *
	 * @throws UnsupportedOperationException if the <tt>putAll</tt> method is not
	 * supported by this map.
	 * @throws ClassCastException if the class of a key or value in the specified
	 * map prevents it from being stored in this map.
	 * @throws IllegalArgumentException some aspect of a key or value in the
	 * specified map prevents it from being stored in this map.
	 * @throws NullPointerException if the specified map is <tt>null</tt>, or if
	 * this map does not permit <tt>null</tt> keys or values, and the specified map
	 * contains <tt>null</tt> keys or values.
	 */
	public void putAll( Map<? extends String, ? extends Object> t )
	{
		context.getBindings( ENGINE_SCOPE ).putAll( t );
	}

	/**
	 * Removes the mapping from the engine scope.
	 * <p/>
	 * <p>Returns the value to which the map previously associated the key, or
	 * <tt>null</tt> if the map contained no mapping for this key.  (A
	 * <tt>null</tt> return can also indicate that the map previously associated
	 * <tt>null</tt> with the specified key if the implementation supports
	 * <tt>null</tt> values.)  The map will not contain a mapping for the specified
	 * key once the call returns.
	 *
	 * @param okey key whose mapping is to be removed from the map.
	 *
	 * @return previous value associated with specified key, or <tt>null</tt> if
	 *         there was no mapping for key.
	 *
	 * @throws ClassCastException if the key is of an inappropriate type for this
	 * map (optional).
	 * @throws NullPointerException if the key is <tt>null</tt> and this map does
	 * not permit <tt>null</tt> keys (optional).
	 * @throws UnsupportedOperationException if the <tt>remove</tt> method is not
	 * supported by this map.
	 */
	// Why is the compiler complaining about this?
	//public Object remove( String key )
	public Object remove( Object okey )
	{
		// This shouldn't be necessary... we don't map Objects, Strings.
		String key = (String)okey;
		Object oldValue =
			context.getAttribute( key, ENGINE_SCOPE );
		context.removeAttribute( key, ENGINE_SCOPE );
		return oldValue;
	}

	/**
	 * Removes all mappings from this map (optional operation).
	 *
	 * @throws UnsupportedOperationException clear is not supported by this map.
	 */
	public void clear()
	{
		context.getBindings( ENGINE_SCOPE ).clear();
	}

	/**
	 * Returns the total key set of all scopes.
	 * This method violates the Map contract by returning an unmodifiable set.
	 *
	 * @return a set view of the keys contained in this map.
	 */
	public Set keySet()
	{
		return totalKeySet();
	}

	/**
	 *
	 * Returns the total values set of all scopes.
	 * This method violates the Map contract by returning an unmodifiable set.
	 *
	 * @return a collection view of the values contained in this map.
	 */
	public Collection values()
	{
		return totalValueSet();
	}

	/**
	 * Returns a set view of the mappings contained in this map.  Each element in
	 * the returned set is a {@link java.util.Map.Entry}.  The set is backed by the
	 * map, so changes to the map are reflected in the set, and vice-versa. If the
	 * map is modified while an iteration over the set is in progress (except
	 * through the iterator's own <tt>remove</tt> operation, or through the
	 * <tt>setValue</tt> operation on a map entry returned by the iterator) the
	 * results of the iteration are undefined.  The set supports element removal,
	 * which removes the corresponding mapping from the map, via the
	 * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
	 * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not support the
	 * <tt>add</tt> or <tt>addAll</tt> operations.
	 *
	 * @return a set view of the mappings contained in this map.
	 */
	public Set<Entry<String,Object>> entrySet()
	{
		throw new Error("unimplemented");
	}

	private Set totalKeySet()
	{
		Set keys = new HashSet();
		List<Integer> scopes = context.getScopes();
		for ( int i : scopes ) {
			keys.addAll( context.getBindings( i ).keySet() );
		}
		return Collections.unmodifiableSet(keys);
	}
	private Set totalValueSet()
	{
		Set values = new HashSet();
		List<Integer> scopes = context.getScopes();
		for ( int i : scopes ) {
			values.addAll( context.getBindings( i ).values() );
		}
		return Collections.unmodifiableSet(values);
	}

}

