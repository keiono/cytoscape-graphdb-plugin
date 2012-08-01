package org.cytoscape.blueprints.graphdb.internal;

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// Get appropriate factories and managers
		final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		final CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);

	}
}