package org.cytoscape.blueprints.graphdb.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import scala.xml.NodeSeq;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

/**
 * Create an IndexedGraph object from CyNetwork.
 * 
 */
public class ExportNetworkToDatabaseTask extends AbstractNetworkTask {

	@Tunable(description = "Export to DB:")
	public String databaseLocation;

	@ProvidesTitle
	public String getTitle() {
		return "Export Network to Graph Database";
	}

	public ExportNetworkToDatabaseTask(CyApplicationManager appManager) {
		super(appManager.getCurrentNetwork());
	}

	public ExportNetworkToDatabaseTask(CyNetwork network) {
		super(network);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final Graph graph = new Neo4jGraph(databaseLocation);
		final BatchGraph<?> bgraph = BatchGraph.wrap(graph);

		final List<CyEdge> allEdges = network.getEdgeList();
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();

		Collection<CyColumn> nodeColumns = createValidColumns(nodeTable);
		Collection<CyColumn> edgeColumns = createValidColumns(edgeTable);

		// createIdx(nodeColumns, (IndexableGraph) graph, Vertex.class);
		// createIdx(edgeColumns, (IndexableGraph) graph, Edge.class);

		for (final CyEdge edge : allEdges) {

			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();

			String sourceName = network.getRow(source).get(CyNetwork.NAME,
					String.class);
			String targetName = network.getRow(target).get(CyNetwork.NAME,
					String.class);

			Vertex sourceV = bgraph.getVertex(sourceName);
			if (sourceV == null)
				sourceV = bgraph.addVertex(sourceName);
			createProperties(nodeColumns, source, sourceV);

			Vertex targetV = bgraph.getVertex(targetName);
			if (targetV == null)
				targetV = bgraph.addVertex(targetName);
			createProperties(nodeColumns, target, targetV);

			final String interactionType = network.getRow(edge).get(
					CyEdge.INTERACTION, String.class);
			final Edge tEdge = bgraph.addEdge(null, sourceV, targetV,
					interactionType);
			createProperties(edgeColumns, edge, tEdge);
		}

		bgraph.shutdown();
		graph.shutdown();

		KeyIndexableGraph iGraph = new Neo4jGraph(databaseLocation);
		createIdx(nodeColumns, iGraph, Vertex.class);
		createIdx(edgeColumns, iGraph, Edge.class);
		iGraph.shutdown();

	}

	private Collection<CyColumn> createValidColumns(CyTable table) {
		final Collection<CyColumn> originalColumns = table.getColumns();
		// Skip List for now... TODO: Implement List to String module
		final Set<CyColumn> columns = new HashSet<CyColumn>();
		for (CyColumn col : originalColumns) {
			if (col.getType() != List.class) {
				if (!col.getName().equals(CyNetwork.SUID)
						&& !col.getName().equals(CyNetwork.SELECTED)) {

					System.out.println("New col = " + col.getName());
					columns.add(col);
				}
			}
		}
		return columns;
	}

	private Map<String, Index<?>> idxMap = new HashMap<String, Index<?>>();

	private void createIdx(Collection<CyColumn> columns,
			KeyIndexableGraph graph, Class<? extends Element> type) {
		for (final CyColumn column : columns) {
			System.out.println("### Making key.. = " + column.getName());
			graph.createKeyIndex(column.getName(), type);

		}
		System.out.println("### KEY IDX created = "
				+ graph.getIndexedKeys(type));
	}

	private String getIdxName(CyColumn column, Class<? extends Element> type) {
		return type.getSimpleName() + "." + column.getName();
	}

	private final void createProperties(Collection<CyColumn> columns,
			final CyIdentifiable graphObject, final Element element) {

		final CyRow row = network.getRow(graphObject);

		for (final CyColumn column : columns) {
			final String colName = column.getName();
			final Object value = row.get(colName, column.getType());
			if (value != null) {
				element.setProperty(colName, value);

				// String idxName;
				// if (element instanceof Vertex)
				// idxName = getIdxName(column, Vertex.class);
				// else {
				// idxName = getIdxName(column, Edge.class);
				// }
				//
				// System.out.println("$$ IDX name = " + idxName);
				//
				// final Index idx = idxMap.get(idxName);
				// System.out.println("$$ GOT IDX = " + idx);
				// if (idx != null) {
				// if (element instanceof Vertex)
				// idx.put(idxName, value, (Vertex)element);
				// else {
				// idx.put(idxName, value, (Edge)element);
				// }
				// }
			}
		}
	}
}