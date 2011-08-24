package org.cytoscape.blueprints.graphdb;

import java.util.Map;

import com.tinkerpop.blueprints.pgm.Graph;

public interface GraphDatabaseManager {
	
	Map<String, Graph> getDatabaseMap();

}
