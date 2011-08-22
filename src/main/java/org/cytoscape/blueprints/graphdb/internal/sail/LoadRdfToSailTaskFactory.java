package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;
import com.tinkerpop.blueprints.pgm.impls.sail.impls.NativeStoreSailGraph;

public class LoadRdfToSailTaskFactory implements TaskFactory {

	private final SailGraph db;
	
	public LoadRdfToSailTaskFactory(final CyApplicationConfiguration config) {
		db = new NativeStoreSailGraph(config.getSettingLocation().getAbsolutePath() + "/sail");
	}
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new LoadRdfToSailTask(db));
	}

}
