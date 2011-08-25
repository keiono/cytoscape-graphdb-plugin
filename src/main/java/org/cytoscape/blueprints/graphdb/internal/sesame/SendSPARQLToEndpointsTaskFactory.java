package org.cytoscape.blueprints.graphdb.internal.sesame;

import org.cytoscape.blueprints.graphdb.EndpointManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class SendSPARQLToEndpointsTaskFactory implements TaskFactory {

	private final EndpointManager manager;
	private final CyNetworkFactory networkFactory;
	private final CyNetworkManager networkManager;

	SendSPARQLToEndpointsTaskFactory(final EndpointManager manager, final CyNetworkFactory networkFactory,
			final CyNetworkManager networkManager) {
		this.manager = manager;
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
	}

	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new SendSPARQLToEndpointsTask(manager, networkFactory, networkManager));
	}
}
