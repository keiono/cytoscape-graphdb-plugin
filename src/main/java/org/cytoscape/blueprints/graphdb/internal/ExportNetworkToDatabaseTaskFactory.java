package org.cytoscape.blueprints.graphdb.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExportNetworkToDatabaseTaskFactory extends AbstractTaskFactory {

	private final CyApplicationManager appManager;

	public ExportNetworkToDatabaseTaskFactory(
			final CyApplicationManager appManager) {
		this.appManager = appManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExportNetworkToDatabaseTask(appManager));
	}

}
