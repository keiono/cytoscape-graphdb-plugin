package org.cytoscape.blueprints;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sparql.SPARQLRepository;

public class SPARQLEndpointTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConnection() throws Exception {
		String endpointURL = "http://linkedlifedata.com/sparql";
		final String dbPedia = "http://dbpedia.org/sparql";


		String q2 = "CONSTRUCT{" + "?p <http://dbpedia.org/ontology/influenced> ?influenced." + "} WHERE {" + "?p a"
				+ "<http://dbpedia.org/ontology/Philosopher> ."
				+ "?p <http://dbpedia.org/ontology/influenced> ?influenced." + "} LIMIT 10000";
		final SPARQLRepository dbPediaEndPoint = new SPARQLRepository(dbPedia, "");
		dbPediaEndPoint.initialize();
		final RepositoryConnection conn2 = dbPediaEndPoint.getConnection();
		final GraphQuery gQuery = conn2.prepareGraphQuery(QueryLanguage.SPARQL, q2);
		final GraphQueryResult result2 = gQuery.evaluate();

		assertNotNull(result2);
		System.out.println(result2);
	}

}
