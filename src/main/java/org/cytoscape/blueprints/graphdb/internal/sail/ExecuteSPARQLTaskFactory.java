package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class ExecuteSPARQLTaskFactory implements TaskFactory {

	private String query;
	private SailGraph sail;
	
	@Override
	public TaskIterator getTaskIterator() {
		if(sail == null || query == null)
			throw new NullPointerException("Graph or Query is null.");
		
		return new TaskIterator(new ExecuteSPARQLTask(sail, query));
	}
	
	public void setQuery(final String query) {
		this.query = query;
	}
	
	public void setDB(final SailGraph sail) {
		this.sail = sail;
	}
}
