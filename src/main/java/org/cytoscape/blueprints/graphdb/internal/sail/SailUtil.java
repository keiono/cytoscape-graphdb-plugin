package org.cytoscape.blueprints.graphdb.internal.sail;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;

public class SailUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(SailUtil.class);
	
	public static void dumpStatus(final Graph graph) {
		
		final Iterable<Edge> edges = graph.getEdges();
		final Iterator<Edge> itr = edges.iterator();
		int counter = 0;
		while(itr.hasNext()) {
			final Edge e = itr.next();
			logger.info(counter + " Edge " + e.getId().toString());
			counter++;
		}
		
		logger.info(graph.toString() + ": has " + counter + " triples.");
	}

}
