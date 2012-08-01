package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.blueprints.graphdb.GraphConverter;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ConvertGraphToCyNetworkTaskFactory extends AbstractTaskFactory {

	final GraphDatabaseManager graphManager;
	final GraphConverter converter;
	final CyNetworkManager manager;

	ConvertGraphToCyNetworkTaskFactory(final GraphDatabaseManager graphManager, final GraphConverter converter,
			final CyNetworkManager manager) {
		this.graphManager = graphManager;
		this.converter = converter;
		this.manager = manager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ConvertGraphToCyNetworkTask(graphManager, converter, manager));

	}
}
