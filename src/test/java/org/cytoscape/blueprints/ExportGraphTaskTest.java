package org.cytoscape.blueprints;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.NetworkTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j.batch.Neo4jBatchGraph;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;

public class ExportGraphTaskTest {

	private final NetworkTestSupport testSupport = new NetworkTestSupport();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	private final CyNetwork createGraph(int size) {
		CyNetwork network = testSupport.getNetwork();
		for (int i = 0; i < size; i++) {
			CyNode node1 = network.addNode();
			CyNode node2 = network.addNode();
			network.getRow(node1).set(CyNetwork.NAME, "Source " + i);
			network.getRow(node2).set(CyNetwork.NAME, "Target " + i);
			CyEdge edge = network.addEdge(node1, node2, true);
			network.getRow(edge).set(CyNetwork.NAME, "Edge " + i);
		}
		return network;
	}

	@Test
	public void testExport() throws Exception {

		IndexableGraph bgraph = new Neo4jBatchGraph("target/tmp/neo4jdata");
		final Index<Vertex> nodeNameIdx = bgraph.createIndex("name", Vertex.class);
		final Index<Edge> edgeInteractionIdx = bgraph.createIndex("interaction", Edge.class);
		
		CyNetwork network = createGraph(1000);
		assertEquals(1000, network.getEdgeCount());
		
		for (CyEdge edge : network.getEdgeList()) {
			
			final CyNode source = edge.getSource();
			final CyNode target = edge.getTarget();
			
			Vertex sourceV = bgraph.addVertex(null);
			Vertex targetV = bgraph.addVertex(null);

			Edge tEdge = bgraph.addEdge(null, sourceV, targetV, "interact_with");	
		}

		
		
		bgraph.shutdown();
		
		bgraph = new Neo4jGraph("target/tmp/neo4jdata");
		Iterable edges = bgraph.getEdges();
		Iterator itr = edges.iterator();
		while(itr.hasNext()) {
			Object e = itr.next();
			System.out.println("Edge = " + e);
		}
		bgraph.shutdown();
	}

}
