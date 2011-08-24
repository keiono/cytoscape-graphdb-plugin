package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.List;
import java.util.Map;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class ExecuteSPARQLTask extends AbstractTask {
	
	private final SailGraph sail;
	private final String query;
	
	public ExecuteSPARQLTask(final SailGraph sail, final String query) {
		this.sail = sail;
		this.query = query;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final List<Map<String, Vertex>> result = sail.executeSparql(query);
	}

}
