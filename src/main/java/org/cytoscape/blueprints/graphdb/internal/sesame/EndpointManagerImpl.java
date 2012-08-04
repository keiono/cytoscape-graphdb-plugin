package org.cytoscape.blueprints.graphdb.internal.sesame;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.blueprints.graphdb.EndpointManager;
import org.cytoscape.property.CyProperty;

public class EndpointManagerImpl implements EndpointManager {
	
	private final Map<String, String> endpoints;
	
	private final CyProperty<Properties> prop;
	
	// Preset ENDPOINTS
	private static final String LINKED_LIFE_DATA = "http://linkedlifedata.com/sparql";
	private static final String DBPEDIA = "http://dbpedia.org/sparql";
	
	public EndpointManagerImpl(final CyProperty<Properties> prop) {
		this.prop = prop;
		endpoints = new HashMap<String, String>();	
	}

	@Override
	public Map<String, String> getEndpointMap() {
		final Properties spProps = prop.getProperties();
		for(final Object key: spProps.keySet()) {
			final String keyString = key.toString();
			if(keyString.startsWith(ENDPOINT_PREFIX))
				endpoints.put(keyString, spProps.getProperty(keyString));
		}
		
		endpoints.put("linkedlifedata", LINKED_LIFE_DATA);
		endpoints.put("dbPedia", DBPEDIA);
		System.out.println("===========> Endpoints = " + endpoints.size());
		return endpoints;
	}
	
	

	
}
