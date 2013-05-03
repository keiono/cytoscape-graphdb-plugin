package org.cytoscape.blueprints.graphdb.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.Source;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

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

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final Graph graph = new Neo4jGraph(databaseLocation);
		final BatchGraph<?> bgraph = BatchGraph.wrap(graph);

		final List<CyEdge> allEdges = network.getEdgeList();
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();

		for (final CyEdge edge : allEdges) {
			
			
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();

			System.out.println("ID = " + edge.getSUID() + ", s = " + source.getSUID() +", t = " + target.getSUID());
			Vertex sourceV = bgraph.getVertex(source.getSUID());
			if(sourceV == null)
				sourceV = bgraph.addVertex(source.getSUID());
			createProperties(nodeTable, source, sourceV);
			Vertex targetV = bgraph.getVertex(target.getSUID());
			if(targetV == null)
				targetV = bgraph.addVertex(target.getSUID());
			createProperties(nodeTable, target, targetV);

			final String interactionType = network.getRow(edge).get(
					CyEdge.INTERACTION, String.class);
			final Edge tEdge = bgraph.addEdge(edge.getSUID(), sourceV, targetV,
					interactionType);
			createProperties(edgeTable, edge, tEdge);
		}
		bgraph.shutdown();
	}

	private final void createProperties(final CyTable table,
			final CyIdentifiable graphObject, final Element element) {

		final CyRow row = network.getRow(graphObject);

		final Collection<CyColumn> originalColumns = table.getColumns();
		// Skip List for now... TODO: Implement List to String module
		final Set<CyColumn> columns = new HashSet<CyColumn>();
		for (CyColumn col : originalColumns) {
			if (col.getType() != List.class)
				columns.add(col);
		}

		for (final CyColumn column : columns) {
			final String colName = column.getName();
			final Object value = row.get(colName, column.getType());
			element.setProperty(colName, value);
		}
	}
}