package org.cytoscape.blueprints.graphdb.internal.sail;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.blueprints.graphdb.GraphConverter;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.impls.sail.SailGraph;

public class LoadRdfToSailTask extends AbstractTask {
	
	private static final String[] FILE_TYPE = {"rdf-xml", "n-triples", "turtle", "n3", "trix", "trig"};

	
	@Tunable(description="Data Type")
	public ListSingleSelection<String> type;

	@Tunable(description="URL of RDF", params = "fileCategory=network;input=true")
	public URL url;
	
	private final SailGraph sailGraph;
	
	LoadRdfToSailTask(final SailGraph sailGraph) {
		final List<String> typeList = new ArrayList<String>();
		
		for(String fileType: FILE_TYPE)
			typeList.add(fileType);
			
		type = new ListSingleSelection<String>(typeList);
		this.sailGraph = sailGraph;
		
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final String selectedType = type.getSelectedValue();
		
		System.out.println("Loading RDF from: ");
		sailGraph.loadRDF(url.openStream(),"", selectedType, null);

		final Iterator<Edge> itr = sailGraph.getEdges().iterator();
		
		int count = 0;
		while(itr.hasNext()) {
			itr.next();
			count++;
		}
		
		System.out.println("======> loaded: total = " + count);
	}

}
