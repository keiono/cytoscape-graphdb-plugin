package org.cytoscape.blueprints.graphdb.internal.sesame;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.blueprints.graphdb.EndpointManager;
import org.cytoscape.property.CyProperty;

public class EndpointManagerImpl implements EndpointManager {
	
	private final Map<String, String> endpoints;
	
	private final CyProperty<Properties> prop;
	
	EndpointManagerImpl(final CyProperty<Properties> prop) {
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
		System.out.println("===========> Endpoints = " + endpoints.size());
		return endpoints;
	}
	
	

	
}
