package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.Collection;

import org.cytoscape.blueprints.graphdb.GraphConverter;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class ConvertGraphTaskFactory implements TaskFactory {
	
	private final GraphConverter converter;
	private final CyNetworkManager manager;
	
	private Collection<Vertex> vertices;
	private Collection<Edge> edges;

	
	ConvertGraphTaskFactory(final GraphConverter converter, final CyNetworkManager manager) {
		this.converter = converter;
		this.manager = manager;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ConvertGraphTask(converter, vertices, edges, manager));
	}
	
	public void setVertices(final Collection<Vertex> vertices) {
		this.vertices = vertices;
	}
	
	public void setEdges(final Collection<Edge> edges) {
		this.edges = edges;
	}

}
