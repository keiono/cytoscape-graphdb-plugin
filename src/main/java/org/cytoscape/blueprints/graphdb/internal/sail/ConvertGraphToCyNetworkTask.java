package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cytoscape.blueprints.graphdb.GraphConverter;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;

public class ConvertGraphToCyNetworkTask extends AbstractTask {
	
	@Tunable(description="Database to be Converted")
	public ListSingleSelection<String> database;
	
	private final GraphDatabaseManager graphManager;

	private Graph graph;

	private final GraphConverter converter;
	private final CyNetworkManager manager;

	ConvertGraphToCyNetworkTask(final GraphDatabaseManager graphManager, final GraphConverter converter, final CyNetworkManager manager) {
		this.manager = manager;
		this.converter = converter;
		this.graphManager = graphManager;
		
		final List<String> databaseList = new ArrayList<String>();
		for(String dbLocation: graphManager.getDatabaseMap().keySet()) {
			databaseList.add(dbLocation);
		}
		
		database = new ListSingleSelection<String>(databaseList);
		
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		graph = graphManager.getDatabaseMap().get(database.getSelectedValue());
		
		if (graph == null)
			throw new NullPointerException("Graph is null.");

//		final Iterator<Vertex> vItr = graph.getVertices().iterator();
//
//		final Set<Vertex> vertices = new HashSet<Vertex>();
//		while (vItr.hasNext())
//			vertices.add(vItr.next());

		final Iterator<Edge> eItr = graph.getEdges().iterator();

		final Set<Edge> edges = new HashSet<Edge>();
		while (eItr.hasNext())
			edges.add(eItr.next());

		this.insertTasksAfterCurrentTask(new ConvertGraphTask(converter, null, edges, manager));
	}

}
