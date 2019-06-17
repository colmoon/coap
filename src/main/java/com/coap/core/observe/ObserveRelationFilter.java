package com.coap.core.observe;

/**
 * Define a (sub-)selection of observe relations.
 * Used by {@link org.eclipse.californium.core.CoapResource#changed(ObserveRelationFilter)}
 */
public interface ObserveRelationFilter {
	/**
	 * Check, if the observe relation should be accepted by this filter.
	 * @param relation observe relation
	 * @return <code>true</code>, if the relation should be selected,
	 *         <code>false</code>, if not
	 */
	boolean accept(ObserveRelation relation);
}
