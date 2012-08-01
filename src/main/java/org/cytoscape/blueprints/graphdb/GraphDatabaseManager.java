package org.cytoscape.blueprints.graphdb;

import java.util.Map;

import com.tinkerpop.blueprints.Graph;

public interface GraphDatabaseManager {
	
	Map<String, Graph> getDatabaseMap();

}
