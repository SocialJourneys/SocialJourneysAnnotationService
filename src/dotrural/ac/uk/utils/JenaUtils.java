package dotrural.ac.uk.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.json.JSONObject;
import org.topbraid.spin.inference.SPINExplanations;
import org.topbraid.spin.inference.SPINInferences;
import org.topbraid.spin.system.SPINModuleRegistry;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import dotrural.ac.uk.utils.AnnotationTimeUtils.Interval;

public class JenaUtils {

	public static OntModel addAnnotationsToModel (OntModel modelToWriteTo, ArrayList <String []> list, String tweetUri, String tweetText) {
		
				
			for (int i = 0;i<list.size();i++) {
				
				String annotationURI = "http://sj.abdn.ac.uk/ozStudyD2R/resource/annotation/"+UUID.randomUUID();
				//String annotationTypeUri = (String) resultTypeList.get(i);
				
				//add annotation instance 
				Individual 	newAnnotation = modelToWriteTo.createIndividual(annotationURI,modelToWriteTo.createClass("http://www.w3.org/ns/oa#Annotation"));
				
				//relate annotation to tweet 
				Individual tweet = modelToWriteTo.getIndividual(tweetUri);
				newAnnotation.setPropertyValue(modelToWriteTo.createProperty("http://www.w3.org/ns/oa#hasTarget"), tweet);
				
				//record place where found in text
				Individual 	textPositionSelector = modelToWriteTo.createIndividual(annotationURI,modelToWriteTo.createClass("http://www.w3.org/ns/oa#TextPositionSelector"));				
				
				//link selector to annotation
				newAnnotation.setPropertyValue(modelToWriteTo.createProperty("http://www.w3.org/ns/oa#hasSelector"),textPositionSelector);
				
				//set string found
	
				if ((String) list.get(i)[4] != null) {
				Literal name = modelToWriteTo.createTypedLiteral((String) list.get(i)[4]);
				textPositionSelector.setPropertyValue (RDF.value,name);
				newAnnotation.setPropertyValue (RDF.value,name);
				}
				//set start offset
				
				Literal start = modelToWriteTo.createTypedLiteral((String) list.get(i)[1]);
				textPositionSelector.setPropertyValue (modelToWriteTo.createProperty("http://www.w3.org/ns/oa#start"),start);
				
				//set end offset 
				
				Literal end = modelToWriteTo.createTypedLiteral((String) list.get(i)[2]);
				textPositionSelector.setPropertyValue (modelToWriteTo.createProperty("http://www.w3.org/ns/oa#end"),end);
				
				//create kim instance individual that the identified string corresponds to 
				
				OntClass kimType = modelToWriteTo.getOntClass((String) list.get(i)[3]);
				
				if (kimType == null) {
					kimType = modelToWriteTo.createClass((String) list.get(i)[3]);
				}
				
				Individual 	kimInstance = modelToWriteTo.createIndividual((String) list.get(i)[0],kimType);
				
				//assign kim instance type as proton Entity subclass
				
				kimType.setSuperClass(modelToWriteTo.createClass("http://www.ontotext.com/proton/protontop#Entity"));
				
				
				//assign annotation with body
				newAnnotation.setPropertyValue(modelToWriteTo.createProperty("http://www.w3.org/ns/oa#hasBody"),kimInstance);
				
				
				// date time interval handling code from David
				
				AnnotationTimeUtils atu = new AnnotationTimeUtils();
				if  ( ((String) list.get(i)[3]).equals("http://proton.semanticweb.org/2006/05/protont#TimeInterval")) {
					//Kim sometimes doesnt return value
					if ((String) list.get(i)[4] != null) {
				    Interval d = atu.parseTimeInterval(tweetText,(String) list.get(i)[4], Integer.parseInt(list.get(i)[1]));
				    // Interval d = atu.parseDate("25 May 2015");
				  //  Individual i = m.createIndividual( "http://sj.abdn.ac.uk/resource/interval/"+UUID.randomUUID(),m.createClass("http://www.example.com/class"));
				     if (d!=null)
				     atu.addToModel(d, modelToWriteTo, "http://sj.abdn.ac.uk/resource/interval/", newAnnotation);
					}
				}
				if  ( ((String) list.get(i)[3]).equals("http://proton.semanticweb.org/2006/05/protonu#CalendarMonth")) {
					//Kim sometimes doesnt return value
					if ((String) list.get(i)[4] != null) {
				    Interval d = atu.parseMonth((String) list.get(i)[4]);
				    if (d!=null)
				    atu.addToModel(d, modelToWriteTo, "http://sj.abdn.ac.uk/resource/month/", newAnnotation);
					}		
				}
				if  ( ((String) list.get(i)[3]).equals("http://proton.semanticweb.org/2006/05/protonu#Date")) {
					//Kim sometimes doesnt return value
					if ((String) list.get(i)[4] != null) {
				    Interval d = atu.parseDate((String) list.get(i)[4]);
				    if (d!=null)
				    atu.addToModel(d, modelToWriteTo, "http://sj.abdn.ac.uk/resource/date/", newAnnotation);
					}		
				}
				
			} 
		
		return modelToWriteTo;
	}

	public static void initialiseDomainModel(OntModel domainModel, Utils utils) throws FileNotFoundException {
		// load the transport disruption ontology and infer superclasses
		OntModel disruptionOntology = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
		utils.loadIntoModel(disruptionOntology, "TTL",
				"http://sj.abdn.ac.uk/SocialJourneysAnnotationOntology/transportdisruption.ttl");
		// disruptionOntology.write(System.out);

		// load the rules model and add

		OntModel rulesModel = utils
				.loadOntologyModels("TTL",
						"http://sj.abdn.ac.uk/SocialJourneysAnnotationOntology/inferencerules.ttl");
		rulesModel.addSubModel(disruptionOntology);

		// add disruption ontology to domain model
		domainModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		domainModel.add(rulesModel);
	}
	
	
	public static OntModel getTweetModel(String uri, ServletContext context) {

		OntModel m = ModelFactory
				.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);

		// m.read("http://purl.org/td/transportdisruption");

		// read required ontologies
		// m.read("http://sj.abdn.ac.uk/SocialJourneysAnnotationOntology/TD.ttl");
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/bottari.n3")).toURI().toString(),"N3");
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/protontop2.ttl")).toURI().toString(),"N3");
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/oa.owl")).toURI().toString());
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/annotate3.n3")).toURI().toString(),"N3");
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/spin.owl")).toURI().toString());
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/sp.owl")).toURI().toString());
		// m.read(new
		// File(context.getRealPath("/WEB-INF/resources/ns.owl")).toURI().toString());
		// m.read("http://www.old.ontotext.com/sites/default/files/proton/protontop.ttl","TTL");

		return m;
	}

	// rule file URI shoul refer to ttl file containing spin rules
	public synchronized Model performSPINinferences(OntModel dataModel, OntModel domainModel) {

		// need to change so accepts array of files
		
		/* moved to the constructor
		OntModel rules = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		rules.read(ruleFileURI, "TTL");
		OntModel ontology = ModelFactory.createOntologyModel();
		ontology.read("http://sj.abdn.ac.uk/SocialJourneysAnnotationOntology/transportdisruption.ttl");
		
		
		
		
		instanceModel.add(ontology);

		rules.addSubModel(instanceModel);
		// pw.println(" instance model size after rules added: " +
		// ontologyModel.size());

		SPINModuleRegistry.get().init();

		// Model inferedModel = ModelFactory.createDefaultModel();
		// instanceModel.add(inferedModel);

		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(rules, null);

		// Run all inferences
		SPINInferences.run(rules, instanceModel, null, null, true, null);
		instanceModel.remove(ontology);*/
		
		
		Model inferedModel = ModelFactory.createDefaultModel();
		domainModel.addSubModel(inferedModel);
		inferedModel.setNsPrefixes(domainModel.getNsPrefixMap());
		domainModel.addSubModel(dataModel);

		// initialise the spin registry
		SPINModuleRegistry.get().init();
		SPINModuleRegistry.get().registerAll(domainModel, null);

		// Run all inferences
		SPINExplanations explain = new SPINExplanations();
		long t1 = System.currentTimeMillis();
		SPINInferences
				.run(domainModel, inferedModel, explain, null, true, null);
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
		// return the domain model to its original state
		domainModel.removeSubModel(inferedModel);
		domainModel.removeSubModel(dataModel);
		

		return inferedModel;
	}

}
