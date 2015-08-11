package dotrural.ac.uk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.topbraid.spin.inference.SPINExplanations;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINModuleRegistry;

import arq.qexpr;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class Utils {

	public OntModel loadDisruptionOntology(String type, String file)
			throws FileNotFoundException {
		OntModel model = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
		return loadIntoModel(model, type, file);
	}

	public OntModel loadOntologyModels(String type, String... files)
			throws FileNotFoundException {
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		return loadIntoModel(model, type, files);

	}

	public OntModel loadIntoModel(OntModel model, String type, String... files)
			throws FileNotFoundException {
		for (String file : files) {
			// System.out.println("reading " + f.toURI().toString());
			System.out.println("reading " + file);
			model.read(file, type);
		}
		return model;
	}

	/*public Model performSPINinferences(OntModel instanceModel,
			OntModel rulesModel) {

		// setup model for inferences to be added to
		Model inferedModel = ModelFactory.createDefaultModel();
		rulesModel.addSubModel(inferedModel);
		inferedModel.setNsPrefixes(rulesModel.getNsPrefixMap());

		// initialise the spin registry
		SPINModuleRegistry.get().init();
		SPINModuleRegistry.get().registerAll(rulesModel, null);

		// Run all inferences
		SPINExplanations explain = new SPINExplanations();
		SPINInferences.run(rulesModel, inferedModel, explain, null, true, null);
		System.out.println(" instance model size after inferencing: "
				+ inferedModel.size());

		rulesModel.removeSubModel(inferedModel);
		return inferedModel;
	}*/

	public List<String> executeQuery(String queryString, String var, String endpoint) {
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint,
				query);
		ResultSet results = qexec.execSelect();
		List<String> resultList = new ArrayList<String>();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			Resource x = soln.getResource(var); // Get a result variable by name.
			resultList.add(x.getURI());
		}
		qexec.close();
		return resultList;
	}
}
