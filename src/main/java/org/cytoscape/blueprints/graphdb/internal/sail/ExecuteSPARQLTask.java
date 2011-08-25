package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.List;
import java.util.Map;

import org.cytoscape.blueprints.graphdb.SPARQLTask;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;


/**
 * Execute SPARQL
 *
 */
public class ExecuteSPARQLTask extends AbstractTask implements SPARQLTask {
	
	private final SailGraph sail;
	
	@Tunable(description="Enter SPARQL Query")
	public String query;
	
	private List<Map<String, Vertex>> result;
	
	public ExecuteSPARQLTask(final SailGraph sail) {
		this.sail = sail;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(query == null || sail == null)
			throw new NullPointerException("Graph and Query should not be null.");
		
		result = sail.executeSparql(query);
	}
	
	
	@Override
	public List<Map<String, Vertex>> getResult() {
		return result;
	}
}
