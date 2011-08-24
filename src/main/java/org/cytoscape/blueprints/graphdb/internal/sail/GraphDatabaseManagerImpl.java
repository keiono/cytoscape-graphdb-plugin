package org.cytoscape.blueprints.graphdb.internal.sail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CytoscapeShutdownEvent;
import org.cytoscape.application.events.CytoscapeShutdownListener;
import org.cytoscape.blueprints.graphdb.GraphDatabaseManager;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.sail.impls.NativeStoreSailGraph;

public class GraphDatabaseManagerImpl implements GraphDatabaseManager, CytoscapeShutdownListener {
		
	private final Map<String, Graph> databaseMap;
	
	GraphDatabaseManagerImpl(final CyApplicationConfiguration config) {
		databaseMap = new HashMap<String, Graph>();
		
		// TODO: this should be removed
		final File sailDir = new File(config.getSettingLocation().getAbsolutePath(), "sail_database");
		final String sailLocation = sailDir.getAbsolutePath();
		final NativeStoreSailGraph db = new NativeStoreSailGraph(sailLocation);
		
		
		System.out.println("-----------> DB Created: " + sailLocation);

		databaseMap.put(sailLocation,db);
	}

	public void addDatabase() {
		
	}
	
	@Override
	public void handleEvent(CytoscapeShutdownEvent e) {
		shutdownDtabases();
	}

	
	private void shutdownDtabases() {
		for(final Graph graphDB: databaseMap.values()) {
			
			SailUtil.dumpStatus(graphDB);
			graphDB.shutdown();
		}
		
		System.out.println("-----------> Shutdown finished!");
	}

	@Override
	public Map<String, Graph> getDatabaseMap() {
		return this.databaseMap;
	}
}
