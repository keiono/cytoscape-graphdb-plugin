package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.Collection;

import org.cytoscape.blueprints.graphdb.GraphConverter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class ConvertGraphTask extends AbstractTask {
	
	private final GraphConverter converter;
	private final Collection<Vertex> vertices;
	private final Collection<Edge> edges;
	private final CyNetworkManager manager;
	
	ConvertGraphTask(final GraphConverter converter, Collection<Vertex> vertices, Collection<Edge> edges, CyNetworkManager manager) {
		this.converter = converter;
		this.vertices = vertices;
		this.edges = edges;
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		CyNetwork network = converter.createCyNetwork(vertices, edges);
		manager.addNetwork(network);
	}

}
