package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.impls.sail.SailGraph;

public class LoadRdfToSailTaskFactory extends AbstractTaskFactory {

	private final SailGraph db;

	public LoadRdfToSailTaskFactory(final GraphDatabaseManager dbManager) {
		db = (SailGraph) dbManager.getDatabaseMap().values().iterator().next();
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new LoadRdfToSailTask(db));
	}

}
