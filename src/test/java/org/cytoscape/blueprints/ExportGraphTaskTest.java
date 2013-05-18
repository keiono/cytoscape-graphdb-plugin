package org.cytoscape.blueprints;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.blueprints.graphdb.internal.ExportNetworkToDatabaseTask;
import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.io.internal.read.sif.SIFNetworkReader;
import org.cytoscape.io.internal.util.ReadUtils;
import org.cytoscape.io.internal.util.StreamUtilImpl;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.CyProperty.SavePolicy;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.index.impl.lucene.Hits;


import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

public class ExportGraphTaskTest {

	private static final String DATABASE_LOCATION = "target/dbtest";

	private final NetworkTestSupport testSupport = new NetworkTestSupport();
	private TaskMonitor taskMonitor;
	private CyNetworkFactory netFactory;
	private CyNetworkViewFactory viewFactory;
	private ReadUtils readUtil;
	private CyLayoutAlgorithmManager layouts;
	private CyNetworkManager networkManager;
	private CyRootNetworkManager rootNetworkManager;
	private CyApplicationManager cyApplicationManager;

	private Properties properties;

	static class SimpleTask extends AbstractTask {
		public void run(final TaskMonitor tm) {
		}
	}

	@Before
	public void setUp() throws Exception {
		taskMonitor = mock(TaskMonitor.class);

		CyLayoutAlgorithm def = mock(CyLayoutAlgorithm.class);
		Object context = new Object();
		when(def.createLayoutContext()).thenReturn(context);
		when(def.getDefaultLayoutContext()).thenReturn(context);
		when(
				def.createTaskIterator(Mockito.any(CyNetworkView.class),
						Mockito.any(Object.class), Mockito.anySet(),
						Mockito.any(String.class))).thenReturn(
				new TaskIterator(new SimpleTask()));

		layouts = mock(CyLayoutAlgorithmManager.class);
		when(layouts.getDefaultLayout()).thenReturn(def);

		NetworkTestSupport nts = new NetworkTestSupport();
		netFactory = nts.getNetworkFactory();

		networkManager = nts.getNetworkManager();
		rootNetworkManager = nts.getRootNetworkFactory();

		cyApplicationManager = mock(CyApplicationManager.class);

		properties = new Properties();
		CyProperty<Properties> cyProperties = new SimpleCyProperty<Properties>(
				"Test", properties, Properties.class, SavePolicy.DO_NOT_SAVE);
		NetworkViewTestSupport nvts = new NetworkViewTestSupport();

		viewFactory = nvts.getNetworkViewFactory();
		readUtil = new ReadUtils(new StreamUtilImpl(cyProperties));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExport() throws Exception {

		TransactionalGraph bgraph = new Neo4jGraph(DATABASE_LOCATION);
		bgraph.shutdown();

		clearDB();

		// Read network from real network file
		final CyNetwork[] networks = getNetworks("galFiltered.sif");
		assertNotNull(networks);
		assertEquals(1, networks.length);

		final CyNetwork network = networks[0];
		assertNotNull(network);

		final ExportNetworkToDatabaseTask task = new ExportNetworkToDatabaseTask(
				network);
		task.databaseLocation = DATABASE_LOCATION;
		task.run(taskMonitor);

		System.out.println("Insertion finished!");

		testResult();

	}

	private final void testResult() {
		Graph bgraph = new Neo4jGraph(DATABASE_LOCATION);

		Iterable<Vertex> nodes = bgraph.getVertices();
		Iterator<Vertex> itr2 = nodes.iterator();

		System.out.println("=== Counting...");

		long nodeCount = countResult(itr2);

		Iterable<Edge> edges = bgraph.getEdges();
		Iterator<Edge> itr = edges.iterator();
		long edgeCount = countResult(itr);

		System.out.println("Nodes = " + nodeCount);
		System.out.println("Edges = " + edgeCount);

		// Number of edges
		assertEquals(362l, edgeCount);

		// Number of nodes
		assertEquals(331l, nodeCount);

		testGraphTopology((IndexableGraph) bgraph);

		bgraph.shutdown();
	}

	private final void testGraphTopology(IndexableGraph graph) {

//		Index<Vertex> nodeNameKey = graph.getIndex("name", Vertex.class);
//		Index<Edge> edgeNameKey = graph.getIndex("name", Edge.class);
//		Index<Edge> edgeInteractionKey = graph.getIndex(CyEdge.INTERACTION, Edge.class);
//		assertNotNull(nodeNameKey);
//		assertNotNull(edgeNameKey);
//		assertNotNull(edgeInteractionKey);
//		
//		long keys = countResult(graph.getIndices().iterator());
//		assertEquals(2, graph.getIndexedKeys(Vertex.class).size());

		// Get node by index
		String hub1 = "YNL216W";
		Iterable<Vertex> res1 = graph.getVertices(CyNetwork.NAME, hub1);
		Iterator<Vertex> itr = res1.iterator();
		assertEquals(1, countResult(itr));
		Vertex node1 = graph.getVertices(CyNetwork.NAME, hub1).iterator()
				.next();
		Iterable<Edge> edges = node1.getEdges(Direction.BOTH);
		Iterator<Edge> itr2 = edges.iterator();
		assertEquals(17l, countResult(itr2));

		String[] neighbours = { "YNL216W", "YAL038W", "YBR093C", "YCL030C", "YCR012W",
				"YDR050C", "YDR171W", "YER074W", "YGR254W", "YHR174W",
				"YIL069C", "YIL133C", "YLR044C", "YML024W", "YNL301C",
				"YOL086C", "YOL120C", "YOL127W"};
		
		final List<String> nList = Arrays.asList(neighbours);
		
		Iterator<Edge> edges2 = node1.getEdges(Direction.BOTH).iterator();
		while(edges2.hasNext()) {
			final Edge edge = edges2.next();
			final Vertex in = edge.getVertex(Direction.IN);
			final Vertex out = edge.getVertex(Direction.OUT);
			assertTrue(nList.contains(in.getProperty(CyNetwork.NAME)) && 
					nList.contains(out.getProperty(CyNetwork.NAME)) );
		}
		
		// Test case sensitivity
		final Index<Vertex> idx = graph.getIndex("Vertex.name", Vertex.class);
		assertEquals("Vertex.name", idx.getIndexName());
		CloseableIterable<Vertex> searchResult = idx.query("Vertex.name", "YNL216W");
		Set<Vertex> nodesFound = checkHits(searchResult);
		assertEquals(1, nodesFound.size());

		CloseableIterable<Vertex> searchResult2 = idx.query("Vertex.name", "YN*");
		Set<Vertex> nodesFound2 = checkHits(searchResult2);
		assertEquals(26, nodesFound2.size());

		CloseableIterable<Vertex> searchResult3 = idx.query("Vertex.name", "ynL216w");
		Set<Vertex> nodesFound3 = checkHits(searchResult3);
		assertEquals(1, nodesFound3.size());
		
		final Index<Edge> edgeIdx = graph.getIndex("Edge.interaction", Edge.class);
		assertEquals("Edge.interaction", edgeIdx.getIndexName());
		CloseableIterable<Edge> edgeSearchResult = edgeIdx.query("Edge.interaction", "p*");
		Set<Edge> eHits = checkHits(edgeSearchResult);
		assertEquals(362, eHits.size());
	
		final Index<Edge> edgeIdx2 = graph.getIndex("Edge.name", Edge.class);
		assertEquals("Edge.name", edgeIdx2.getIndexName());
		Iterable<Edge> edgeSearchResult2 = graph.getEdges("name", "YNL216W (pd) YOL086C");
//		CloseableIterable<Edge> edgeSearchResult2 = edgeIdx.query("Edge.name", "Y*");
		Set<Edge> eHits2 = checkHits(edgeSearchResult2);
		assertEquals(1, eHits2.size());
	
		final Index<Edge> edgeIdx3 = graph.getIndex("Edge.name", Edge.class);
		assertEquals("Edge.name", edgeIdx3.getIndexName());
		assertEquals(Edge.class, edgeIdx3.getIndexClass());
		CloseableIterable<Edge> edgeSearchResult3 = edgeIdx3.query("Edge.name", "*YnL*");
		Set<Edge> eHits3 = checkHits(edgeSearchResult3);
		assertEquals(56, eHits3.size());
	}
	
	private final Set checkHits(Iterable<? extends Element> nodes) {
		assertNotNull(nodes);
		final Iterator caseItr = nodes.iterator();
		final Set hitSet = new HashSet();
		while(caseItr.hasNext()) {
			hitSet.add(caseItr.next());
		}
		
		return hitSet;
	}

	private final long countResult(Iterator<?> itr) {
		long counter = 0;
		while (itr.hasNext()) {
			Object val = itr.next();
			counter++;
		}
		return counter;
	}

	private final void clearDB() {
		TransactionalGraph graph = new Neo4jGraph(DATABASE_LOCATION);

		Iterable<Edge> edges = graph.getEdges();
		Iterator<Edge> itr2 = edges.iterator();

		while (itr2.hasNext()) {
			Edge edge = itr2.next();
			graph.removeEdge(edge);
		}

		Iterable<Vertex> nodes = graph.getVertices();
		Iterator<Vertex> itr = nodes.iterator();

		while (itr.hasNext()) {
			Vertex vertex = itr.next();
			graph.removeVertex(vertex);
		}

		graph.commit();

		assertEquals(0, countResult(itr));
		assertEquals(0, countResult(itr2));

		final IndexableGraph iGraph = (IndexableGraph) graph;
		Iterable<Index<? extends Element>> idxs = iGraph.getIndices();
		Iterator<Index<? extends Element>> idxItr = idxs.iterator();
		while (idxItr.hasNext()) {
			iGraph.dropIndex(idxItr.next().getIndexName());
		}

		graph.shutdown();

	}

	private SIFNetworkReader readFile(String file) throws Exception {
		File f = new File("./src/test/resources/" + file);
		SIFNetworkReader snvp = new SIFNetworkReader(new FileInputStream(f),
				layouts, viewFactory, netFactory, this.networkManager,
				this.rootNetworkManager, this.cyApplicationManager);
		new TaskIterator(snvp);
		snvp.run(taskMonitor);

		return snvp;
	}

	private CyNetwork[] getNetworks(String file) throws Exception {
		final SIFNetworkReader snvp = readFile(file);
		return snvp.getNetworks();
	}

}
