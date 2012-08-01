package org.cytoscape.blueprints.graphdb.internal.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.blueprints.graphdb.EndpointManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utility class to get result from remote/local endpoints.
 * 
 */
public class SendSPARQLToEndpointsTask extends AbstractTask {

	private static final Logger logger = LoggerFactory.getLogger(SendSPARQLToEndpointsTask.class);

	private final Map<String, String> eMap;

	@Tunable(description = "SPARQL Endpoint")
	public ListSingleSelection<String> endpoints;

	@Tunable(description = "Create RDF Graph?")
	public Boolean isGraph;

	@Tunable(description = "SPARQL Query")
	public String query;

	private final CyNetworkFactory networkFactory;
	private final CyNetworkManager networkManager;

	SendSPARQLToEndpointsTask(final EndpointManager manager, final CyNetworkFactory networkFactory,
			final CyNetworkManager networkManager) {
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;

		this.eMap = manager.getEndpointMap();
		endpoints = new ListSingleSelection<String>(new ArrayList<String>(eMap.keySet()));
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		String key = endpoints.getSelectedValue();
		String urlString = eMap.get(key);
		System.out.println("Sending query to the endpoint: " + urlString);
		sendQuery(urlString);
	}

	private void sendQuery(final String endpointURL) throws Exception {
		final SPARQLRepository endpoint = new SPARQLRepository(endpointURL, "");
		endpoint.initialize();

		final RepositoryConnection conn = endpoint.getConnection();

		try {
			if (this.isGraph)
				createGraph(conn, endpointURL);
			else
				createTable(conn);
		} finally {
			conn.close();
		}
	}

	private void createTable(RepositoryConnection conn) throws Exception {
		final TupleQuery tQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
		final TupleQueryResult result = tQuery.evaluate();

		while (result.hasNext()) {
			final BindingSet entry = result.next();
			final Set<String> names = entry.getBindingNames();
			for (String name : names)
				System.out.println(name + " = " + entry.getBinding(name));
		}
	}

	private void createGraph(RepositoryConnection conn, String endpointURL) throws Exception {
		final GraphQuery gQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, query);
		final GraphQueryResult result = gQuery.evaluate();

		// RDF Graph returned from the query
		final CyNetwork network = networkFactory.createNetwork();
		network.getRow(network).set(CyNetwork.NAME, "SPARQL Query Result: " + endpointURL);
		final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

		while (result.hasNext()) {
			final Statement entry = result.next();
			final Resource sub = entry.getSubject();
			final URI pr = entry.getPredicate();
			final Value obj = entry.getObject();

			final String subjectValue = sub.stringValue();
			final String objectValue = obj.stringValue();
			CyNode source = nodeMap.get(subjectValue);
			CyNode target = nodeMap.get(objectValue);

			if (source == null) {
				source = network.addNode();
				final CyRow row = network.getRow(source);
				row.set(CyNetwork.NAME, subjectValue);
				nodeMap.put(subjectValue, source);
			}

			if (target == null) {
				target = network.addNode();
				final CyRow row = network.getRow(target);
				row.set(CyNetwork.NAME, objectValue);
				nodeMap.put(objectValue, target);
			}

			final CyEdge edge = network.addEdge(source, target, true);
			final CyRow row = network.getRow(edge);
			row.set(CyNetwork.NAME, pr.stringValue());

		}

		networkManager.addNetwork(network);
	}
}
