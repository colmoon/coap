package com.coap.core.observe;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a container for {@link ObserveRelation}s that resources use to hold
 * their observe relations. When a resource changes it will notify all relations
 * in the container. Each observe relation must only exist once. However, an
 * endpoint could establish more than one observe relation to the same resource.
 */
public class ObserveRelationContainer implements Iterable<ObserveRelation> {
	
	/** The set of observe relations */
	private final ConcurrentHashMap<String, ObserveRelation> observeRelations;
	
	/**
	 * Constructs a container for observe relations.
	 */
	public ObserveRelationContainer() {
		this.observeRelations = new ConcurrentHashMap<String, ObserveRelation>();
	}
	
	/**
	 * Adds the specified observe relation.
	 *
	 * @param relation the observe relation
	 * @return true, if a old relation was replaced by the provided one, 
	 *         false, if the provided relation was added.
	 */
	public boolean add(ObserveRelation relation) {
		if (relation == null)
			throw new NullPointerException();
		ObserveRelation previous = observeRelations.put(relation.getKey(), relation);
		if (null != previous) {
			previous.cancel();
			return true;
		}
		return false;
	}
	
	/**
	 * Removes the specified observe relation.
	 *
	 * @param relation the observe relation
	 * @return true, if successful
	 */
	public boolean remove(ObserveRelation relation) {
		if (relation == null)
			throw new NullPointerException();
		return observeRelations.remove(relation.getKey(), relation);
	}
	
	/**
	 * Gets the number of observe relations in this container.
	 *
	 * @return the number of observe relations
	 */
	public int getSize() {
		return observeRelations.size();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ObserveRelation> iterator() {
		return observeRelations.values().iterator();
	}
	
}
