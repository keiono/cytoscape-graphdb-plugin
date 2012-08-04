package org.cytoscape.blueprints.graphdb.internal.sail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.sail.impls.NativeStoreSailGraph;

public class GraphDatabaseManagerImpl implements GraphDatabaseManager, CyShutdownListener {

	private final Map<String, Graph> databaseMap;

	public GraphDatabaseManagerImpl(final CyApplicationConfiguration config) {
		databaseMap = new HashMap<String, Graph>();

		// TODO: this should be removed
		final File sailDir = new File(config.getConfigurationDirectoryLocation().getAbsolutePath(), "sail_database");
		final String sailLocation = sailDir.getAbsolutePath();
		final NativeStoreSailGraph db = new NativeStoreSailGraph(sailLocation);

		System.out.println("-----------> DB Created: " + sailLocation);

		databaseMap.put(sailLocation, db);
	}

	public void addDatabase() {

	}

	private void shutdownDtabases() {
		for (final Graph graphDB : databaseMap.values()) {

			SailUtil.dumpStatus(graphDB);
			graphDB.shutdown();
		}

		System.out.println("-----------> Shutdown finished!");
	}

	@Override
	public Map<String, Graph> getDatabaseMap() {
		return this.databaseMap;
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		shutdownDtabases();

	}
}
