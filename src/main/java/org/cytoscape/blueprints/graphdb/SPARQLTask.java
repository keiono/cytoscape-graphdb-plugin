package org.cytoscape.blueprints.graphdb;

import java.util.List;
import java.util.Map;

import com.tinkerpop.blueprints.Vertex;

public interface SPARQLTask {
	
	List<Map<String, Vertex>> getResult();

}
