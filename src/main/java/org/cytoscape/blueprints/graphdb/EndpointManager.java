package org.cytoscape.blueprints.graphdb;

import java.util.Map;

public interface EndpointManager {
	
	public static final String ENDPOINT_PREFIX = "sparql.endpoint";

	// Human readable source name to URI
	Map<String, String> getEndpointMap();
	
}
