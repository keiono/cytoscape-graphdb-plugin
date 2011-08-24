package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class LoadRdfToSailTaskFactory implements TaskFactory {

	private final SailGraph db;
	
	public LoadRdfToSailTaskFactory(final GraphDatabaseManager dbManager) {
		db = (SailGraph) dbManager.getDatabaseMap().values().iterator().next();
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new LoadRdfToSailTask(db));
	}

}
