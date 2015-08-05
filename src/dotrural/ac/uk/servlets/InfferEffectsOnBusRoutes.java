package dotrural.ac.uk.servlets;

import java.util.HashMap;

import javax.servlet.ServletContext;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.FileManager;

public class InfferEffectsOnBusRoutes {

	private Model  model; 
	
	public InfferEffectsOnBusRoutes (ServletContext context) {
		
		
		String fullPath = context.getRealPath("/WEB-INF/resources/busroutes.nt");
		model= FileManager.get().loadModel( fullPath );
		
		
	}
	
	public ResultSet getEffectsOnBusRoutes (String resourceInstanceURI) {
		QueryExecution queryExecution;
		Query query = new Query();
		
		//HashMap res = new HashMap();
		
		String queryString = "Select ?route ?label "
								+ "WHERE {"
								+ "?route <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://sj.abdn.ac.uk/ontology/BusRoute>. "
								+ "{?route <http://sj.abdn.ac.uk/ontology/BusRoute#includesWay> <"+resourceInstanceURI+">.} "
								+ "UNION"
								+ "{?route <http://sj.abdn.ac.uk/ontology/BusRoute#includesStop> <"+resourceInstanceURI+">.}"
								+ "?route <http://www.w3.org/2000/01/rdf-schema#label> ?label."
								+ "}";
        
    	queryExecution = QueryExecutionFactory.create(queryString,model);
		ResultSet results = queryExecution.execSelect();
		
		
		return results;
		
	}

}
