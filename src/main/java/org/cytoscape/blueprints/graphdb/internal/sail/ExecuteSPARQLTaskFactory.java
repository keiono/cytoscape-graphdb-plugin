package org.cytoscape.blueprints.graphdb.internal.sail;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.tinkerpop.blueprints.impls.sail.SailGraph;

public class ExecuteSPARQLTaskFactory extends AbstractTaskFactory {

	private SailGraph sail;

	public void setDB(final SailGraph sail) {
		this.sail = sail;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExecuteSPARQLTask(sail));
	}
}
