package org.cytoscape.blueprints.graphdb.internal;

import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.blueprints.graphdb.EndpointManager;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;
import org.cytoscape.blueprints.graphdb.internal.sail.GraphDatabaseManagerImpl;
import org.cytoscape.blueprints.graphdb.internal.sesame.EndpointManagerImpl;
import org.cytoscape.blueprints.graphdb.internal.sesame.SendSPARQLToEndpointsTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
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
		final CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");

		final CyApplicationConfiguration config = getService(bc, CyApplicationConfiguration.class);

		final GraphDatabaseManager graphDatabaseManager = new GraphDatabaseManagerImpl(config);

		registerAllServices(bc, graphDatabaseManager, new Properties());

		EndpointManager manager = new EndpointManagerImpl(cyPropertyServiceRef);
		// Execute SPARQL
		SendSPARQLToEndpointsTaskFactory sendSPARQLToEndpointsTaskFactory = new SendSPARQLToEndpointsTaskFactory(
				manager, networkFactory, networkManager);
		
		Properties sendSPARQLToEndpointsTaskFactoryProps = new Properties();
		sendSPARQLToEndpointsTaskFactoryProps.setProperty(ID,"sendSPARQLToEndpointsTaskFactory");
		sendSPARQLToEndpointsTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Network");
		sendSPARQLToEndpointsTaskFactoryProps.setProperty(TITLE,"Send SPARQL...");
		sendSPARQLToEndpointsTaskFactoryProps.setProperty(MENU_GRAVITY,"10.0");
		registerAllServices(bc, sendSPARQLToEndpointsTaskFactory, sendSPARQLToEndpointsTaskFactoryProps );
	}
}