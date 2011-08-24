package org.cytoscape.blueprints.graphdb;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableEntry;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Element;
import com.tinkerpop.blueprints.pgm.Vertex;

public class GraphConverterImpl implements GraphConverter {
	
	private final CyNetworkFactory networkFactory;
	
	GraphConverterImpl(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;
		
	}
	
	
	@Override
	public CyNetwork createCyNetwork(final Collection<Vertex> vertices, final Collection<Edge> edges ) {
		
		
		final Map<Vertex, CyNode> v2node = new HashMap<Vertex, CyNode>();
		
		final CyNetwork network = networkFactory.getInstance();
		for(final Edge edge: edges) {
			final Vertex target = edge.getInVertex();
			final Vertex source = edge.getOutVertex();
			Object targetID = target.getId();
			Object sourceID = source.getId();
			
			CyNode cySource = v2node.get(source);
			CyNode cyTarget = v2node.get(target);
			
			if(cySource == null) {
				cySource = network.addNode();
				cySource.getCyRow().set(CyTableEntry.NAME, sourceID.toString());
				processRowData(cySource, source);
				v2node.put(source, cySource);
			}
			
			if(cyTarget == null) {
				cyTarget = network.addNode();
				cyTarget.getCyRow().set(CyTableEntry.NAME, targetID.toString());
				processRowData(cyTarget, target);
				v2node.put(source, cyTarget);
			}
			
			// Create edge
			final CyEdge cEdge = network.addEdge(cySource, cyTarget, true);
			cEdge.getCyRow().set(CyTableEntry.NAME, sourceID.toString() + "(" + edge.getId() +")" + targetID.toString());
			cEdge.getCyRow().set(CyEdge.INTERACTION, edge.getId());
			processRowData(cEdge, edge);
			
		}
		return network;
	}
	
	private void processRowData(final CyTableEntry node, final Element v) {
		final CyRow row = node.getCyRow();
		final Set<String> pKeys = v.getPropertyKeys();
		
		for(String key: pKeys) {
			if(row.getTable().getColumn(key) == null) {
				final Class<? extends Object> propType = v.getProperty(key).getClass();
				System.out.println(key + ": Column Class = " + propType);
//				if(propType != String.class && propType != Integer.class && PropType != )
				row.getTable().createColumn(key, String.class, false);
				
			}
		}
		
		for(String key: pKeys) {
			final Object prop = v.getProperty(key);
			System.out.println(key + ": Value Class = " + prop.getClass());
			row.set(key, prop.toString());
		}
	}
}
