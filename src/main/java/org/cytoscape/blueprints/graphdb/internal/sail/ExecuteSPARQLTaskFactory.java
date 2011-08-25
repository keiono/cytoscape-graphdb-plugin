package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class ExecuteSPARQLTaskFactory implements TaskFactory {

	private SailGraph sail;
	
	@Override
	public TaskIterator getTaskIterator() {
		return new TaskIterator(new ExecuteSPARQLTask(sail));
	}
	
	public void setDB(final SailGraph sail) {
		this.sail = sail;
	}
}
