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
import org.cytoscape.model.SUIDFactory;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.Parameter;
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

	private final Map<CyNode, Object> node2ID = new HashMap<CyNode, Object>();
	private final Map<CyEdge, Object> edge2ID = new HashMap<CyEdge, Object>();

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

		for (final CyEdge edge : allEdges) {

			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();

			// String sourceName = network.getRow(source).get(CyNetwork.NAME,
			// String.class);
			// String targetName = network.getRow(target).get(CyNetwork.NAME,
			// String.class);

			Vertex sourceV = bgraph.getVertex(source.getSUID());
			if (sourceV == null)
				sourceV = bgraph.addVertex(source.getSUID());
			createProperties(nodeColumns, source, sourceV);
			node2ID.put(source, sourceV.getId());

			Vertex targetV = bgraph.getVertex(target.getSUID());
			if (targetV == null)
				targetV = bgraph.addVertex(target.getSUID());
			createProperties(nodeColumns, target, targetV);
			node2ID.put(target, targetV.getId());

			final String interactionType = network.getRow(edge).get(CyEdge.INTERACTION, String.class);
			final Edge tEdge = bgraph.addEdge(edge.getSUID(), sourceV, targetV, interactionType);
			createProperties(edgeColumns, edge, tEdge);
			edge2ID.put(edge, tEdge.getId());
		}
		bgraph.commit();

		bgraph.shutdown();
		graph.shutdown();

		final IndexableGraph iGraph = new Neo4jGraph(databaseLocation);
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
				// Filter unnecessary columns
				if (!col.getName().equals(CyNetwork.SELECTED) && !col.getName().equals("shared name")
						&& !col.getName().equals("shared interaction")) {

					System.out.println("New col = " + col.getName());
					columns.add(col);
				}
			}
		}
		return columns;
	}

	private void createIdx(Collection<CyColumn> columns, final IndexableGraph graph, Class<? extends Element> type) {

		Map<String, String> idx2colName = new HashMap<String, String>();

		for (final CyColumn column : columns) {

			Index<? extends Element> idx = graph.createIndex(type.getSimpleName() + "." + column.getName(), type,
					new Parameter("analyzer", LowerCaseKeywordAnalyzer.class.getName()));
			idx2colName.put(idx.getIndexName(), column.getName());
		}

		Iterator<? extends Element> elementItr = null;

		for (String idxName : idx2colName.keySet()) {
			Index idx = graph.getIndex(idxName, type);

			final String colName = idx2colName.get(idxName);
			CyTable table = null;
			if (type == Vertex.class) {
				elementItr = graph.getVertices().iterator();
				table = network.getDefaultNodeTable();
			} else {
				elementItr = graph.getEdges().iterator();
				table = network.getDefaultEdgeTable();
			}
			
			while (elementItr.hasNext()) {

				final Element elm = elementItr.next();
				final Long suid = elm.getProperty(CyIdentifiable.SUID);
				// System.out.println(suid.getClass() + " ### SUID = " + suid);
				CyIdentifiable graphObj = null;
				if (type == Vertex.class)
					graphObj = network.getNode(suid);
				else {
					graphObj = network.getEdge(suid);
				}

				final Object val = network.getRow(graphObj).get(colName, table.getColumn(colName).getType());
				if (val == null) {

					continue;
				}

				idx.put(idxName, val, elm);

			}
		}

	}

	private String getIdxName(CyColumn column, Class<? extends Element> type) {
		return type.getSimpleName() + "." + column.getName();
	}

	private final void createProperties(Collection<CyColumn> columns, final CyIdentifiable graphObject,
			final Element element) {

		final CyRow row = network.getRow(graphObject);

		for (final CyColumn column : columns) {
			final String colName = column.getName();
			final Object value = row.get(colName, column.getType());
			if (value != null) {

				Object valueObject = value;
				if (column.getType() == String.class) {
					String newString = ((String) value).replace("'", "");
					valueObject = newString;
				}
				element.setProperty(colName, value);

			}
		}
	}
}