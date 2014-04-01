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
import org.neo4j.index.impl.lucene.LowerCaseKeywordAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

/**
 * Create an IndexedGraph object from CyNetwork.
 * 
 */
public class ExportNetworkToDatabaseTask extends AbstractNetworkTask {

	private static final Logger logger = LoggerFactory.getLogger(ExportNetworkToDatabaseTask.class);

	@Tunable(description = "Export to DB (Type full path):")
	public String databaseLocation;

	@Tunable(description = "Swap Edge Directions:")
	public boolean swapEdgeDirections = false;

	@ProvidesTitle
	public String getTitle() {
		return "Export Current Network to Neo4j";
	}


	public ExportNetworkToDatabaseTask(CyApplicationManager appManager) {
		super(appManager.getCurrentNetwork());
	}

	public ExportNetworkToDatabaseTask(CyNetwork network) {
		super(network);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {

		try {
			final Graph graph = new Neo4jGraph(databaseLocation);

			logger.info("========= Graph Created: " + graph);

			// Build vertex name map
			final Map<String, Vertex> name2VertexMap = new HashMap<String, Vertex>();
			Iterator<Vertex> allExistingNodeItr = graph.getVertices().iterator();
			while (allExistingNodeItr.hasNext()) {
				final Vertex node = allExistingNodeItr.next();
				final String nodeName = node.getProperty("name");
				if (nodeName != null)
					name2VertexMap.put(nodeName, node);
			}

			final BatchGraph<?> bgraph = BatchGraph.wrap(graph);

			final List<CyEdge> allEdges = network.getEdgeList();
			final CyTable nodeTable = network.getDefaultNodeTable();
			final CyTable edgeTable = network.getDefaultEdgeTable();

			Collection<CyColumn> nodeColumns = createValidColumns(nodeTable);
			Collection<CyColumn> edgeColumns = createValidColumns(edgeTable);

			for (final CyEdge edge : allEdges) {

				final CyNode source = edge.getSource();
				final CyNode target = edge.getTarget();

				String sourceName = network.getRow(source).get(CyNetwork.NAME, String.class);

				Vertex sourceV = name2VertexMap.get(sourceName);
				if (sourceV == null) {
					sourceV = bgraph.addVertex(sourceName);
					name2VertexMap.put(sourceName, sourceV);
				}
				createProperties(nodeColumns, source, sourceV);

				String targetName = network.getRow(target).get(CyNetwork.NAME, String.class);
				Vertex targetV = name2VertexMap.get(targetName);
				if (targetV == null) {
					targetV = bgraph.addVertex(targetName);
					name2VertexMap.put(targetName, targetV);
				}
				createProperties(nodeColumns, target, targetV);

				String interactionType = network.getRow(edge).get("NeXO relation type", String.class);
				if (interactionType == null)
					interactionType = network.getRow(edge).get(CyEdge.INTERACTION, String.class);

				Edge tEdge = null;
				if (swapEdgeDirections)
					tEdge = bgraph.addEdge(edge.getSUID(), targetV, sourceV, interactionType);
				else
					tEdge = bgraph.addEdge(edge.getSUID(), sourceV, targetV, interactionType);

				createProperties(edgeColumns, edge, tEdge);
			}

			bgraph.commit();
			bgraph.shutdown();
			graph.shutdown();

			final IndexableGraph iGraph = new Neo4jGraph(databaseLocation);
			createIdx(nodeColumns, iGraph, Vertex.class);
			createIdx(edgeColumns, iGraph, Edge.class);
			iGraph.shutdown();
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("Could not create new DB: ", ex);
		}
	}

	private Collection<CyColumn> createValidColumns(CyTable table) {
		final Collection<CyColumn> originalColumns = table.getColumns();

		final Set<CyColumn> columns = new HashSet<CyColumn>();
		for (CyColumn col : originalColumns) {
			// Filter unnecessary columns
			if (!col.getName().equals(CyNetwork.SELECTED) && !col.getName().equals("shared name")
					&& !col.getName().equals("shared interaction")) {

				System.out.println("New col = " + col.getName());
				columns.add(col);
			}
		}
		return columns;
	}

	private void createIdx(Collection<CyColumn> columns, final IndexableGraph graph, Class<? extends Element> type) {

		Index index = graph.getIndex(type.getSimpleName(), type);
		if (index == null)
			index = graph.createIndex(type.getSimpleName(), type, new Parameter("analyzer",
					LowerCaseKeywordAnalyzer.class.getName()));

		final Set<String> columnNamesSet = new HashSet<String>();
		for (final CyColumn column : columns) {
			columnNamesSet.add(column.getName());
		}

		Iterator<? extends Element> elementItr = null;

		for (final CyColumn column : columns) {

			if (column.getName().equals(CyIdentifiable.SUID))
				continue;

			if (type == Vertex.class) {
				elementItr = graph.getVertices().iterator();
			} else {
				elementItr = graph.getEdges().iterator();
			}

			while (elementItr.hasNext()) {
				final Element elm = elementItr.next();
				final Long suid = elm.getProperty(CyIdentifiable.SUID);

				if (suid == null)
					continue;

				CyIdentifiable graphObj = null;
				if (type == Vertex.class)
					graphObj = network.getNode(suid);
				else
					graphObj = network.getEdge(suid);

				final Object val = network.getRow(graphObj).get(column.getName(), column.getType());
				if (val == null)
					continue;

				// System.out.println("######## Adding: " + column.getName() +
				// " ==== " + val);
				index.put(column.getName(), val, elm);
			}
		}

		// TODO: REMOVE SUID

		if (type == Vertex.class) {
			elementItr = graph.getVertices().iterator();
		} else {
			elementItr = graph.getEdges().iterator();
		}
		while (elementItr.hasNext()) {
			final Element elm = elementItr.next();
			elm.removeProperty(CyIdentifiable.SUID);
		}
	}

	private final void createProperties(Collection<CyColumn> columns, final CyIdentifiable graphObject,
			final Element element) {

		final CyRow row = network.getRow(graphObject);

		for (final CyColumn column : columns) {
			final String colName = column.getName();

			Object value = null;

			value = row.get(colName, column.getType());
			if (value != null) {

				Object valueObject = value;
				if (column.getType() == String.class) {
					String newString = ((String) value).replace("\'", "");
					valueObject = newString;
				}
				element.setProperty(colName, valueObject);
			}
		}
	}
}