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

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

public class GraphConverterImpl implements GraphConverter {

	private final CyNetworkFactory networkFactory;

	GraphConverterImpl(final CyNetworkFactory networkFactory) {
		this.networkFactory = networkFactory;

	}

	@Override
	public CyNetwork createCyNetwork(final Collection<Vertex> vertices, final Collection<Edge> edges) {

		final Map<Vertex, CyNode> v2node = new HashMap<Vertex, CyNode>();

		final CyNetwork network = networkFactory.createNetwork();
		for (final Edge edge : edges) {
			final Vertex target = edge.getVertex(Direction.IN);
			final Vertex source = edge.getVertex(Direction.OUT);
			Object targetID = target.getId();
			Object sourceID = source.getId();

			CyNode cySource = v2node.get(source);
			CyNode cyTarget = v2node.get(target);

			if (cySource == null) {
				cySource = network.addNode();
				final CyRow sourceRow = network.getRow(cySource);
				network.getRow(cySource).set(CyNetwork.NAME, sourceID.toString());
				processRowData(sourceRow, source);
				v2node.put(source, cySource);
			}

			if (cyTarget == null) {
				cyTarget = network.addNode();
				final CyRow targetRow = network.getRow(cyTarget);
				network.getRow(cyTarget).set(CyNetwork.NAME, targetID.toString());
				processRowData(targetRow, target);
				v2node.put(source, cyTarget);
			}

			// Create edge
			final CyEdge cEdge = network.addEdge(cySource, cyTarget, true);
			final CyRow edgeRow = network.getRow(cEdge);
			edgeRow.set(CyNetwork.NAME, sourceID.toString() + "(" + edge.getId() + ")" + targetID.toString());
			edgeRow.set(CyEdge.INTERACTION, edge.getId());
			processRowData(edgeRow, edge);

		}
		return network;
	}

	private void processRowData(final CyRow row, final Element v) {
		final Set<String> pKeys = v.getPropertyKeys();

		for (String key : pKeys) {
			if (row.getTable().getColumn(key) == null) {
				final Class<? extends Object> propType = v.getProperty(key).getClass();
				System.out.println(key + ": Column Class = " + propType);
				// if(propType != String.class && propType != Integer.class &&
				// PropType != )
				row.getTable().createColumn(key, String.class, false);

			}
		}

		for (String key : pKeys) {
			final Object prop = v.getProperty(key);
			System.out.println(key + ": Value Class = " + prop.getClass());
			row.set(key, prop.toString());
		}
	}
}
