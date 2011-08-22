package org.cytoscape.blueprints.graphdb.internal.sail;

import java.net.URL;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class LoadRdfToSailTask extends AbstractTask {

	@Tunable(description="URL of RDF", params = "fileCategory=network;input=true")
	public URL url;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
	}

}
