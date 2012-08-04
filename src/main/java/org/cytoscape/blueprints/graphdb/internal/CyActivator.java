package org.cytoscape.blueprints.graphdb.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.blueprints.graphdb.internal.sail.GraphDatabaseManagerImpl;
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

		final CyApplicationConfiguration config = getService(bc, CyApplicationConfiguration.class);
		
		final GraphDatabaseManager graphDatabaseManager = new GraphDatabaseManagerImpl(config);
		
		registerAllServices(bc, graphDatabaseManager, new Properties());
	}
}