package dotrural.ac.uk.utils;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ontotext.kim.client.GetService;
import com.ontotext.kim.client.KIMService;
import com.ontotext.kim.client.corpora.CorporaAPI;
import com.ontotext.kim.client.corpora.KIMAnnotation;
import com.ontotext.kim.client.corpora.KIMAnnotationSet;
import com.ontotext.kim.client.corpora.KIMFeatureMap;
import com.ontotext.kim.client.documentrepository.DocumentRepositoryAPI;
import com.ontotext.kim.client.semanticannotation.SemanticAnnotationAPI;

import dotrural.ac.uk.servlets.InfferEffectsOnBusRoutes;

public class KimUtils {

	public static  ArrayList <String []> annotateText (String content) throws RemoteException {
		
			String jsonString = "{\"entities\":\"error\"}";
			
			 KIMService serviceKim = GetService.from();
			 // obtain CorporaAPI and DocumentRepositoryAPI components
			 CorporaAPI apiCorpora = serviceKim.getCorporaAPI();
			 DocumentRepositoryAPI apiDR = serviceKim.getDocumentRepositoryAPI();
			 SemanticAnnotationAPI apiSemAnn = serviceKim.getSemanticAnnotationAPI();
			 KIMAnnotationSet kimASet = apiSemAnn.execute(content);
			 
			 Iterator it = kimASet.iterator();
			
			
			ArrayList <String []> annotationsListResult = new ArrayList<String []>  ();
			 
			 
			    
			     while (it.hasNext()) {
			         KIMAnnotation kimAnnotation = (KIMAnnotation) it.next();
			         
			         KIMFeatureMap kimFeatures = kimAnnotation.getFeatures();
			 
			 if (kimFeatures != null) {
		    	 
		     	
		     		//Object featureKey = "inst"; 
		     		String keyString1 = "inst";
		     		Object objKey1 = keyString1;
		     		
		     		String keyString2 = "originalName";
		     		Object objKey2 = keyString2;
		     		
		     		String keyString3 = "class";
		     		Object objKey3 = keyString3;
		     		
		     		String startOffset = String.valueOf(kimAnnotation.getStartOffset());
		     		String endOffset = String.valueOf(kimAnnotation.getEndOffset());
		     		String instance = (String) kimFeatures.get(objKey1);
		     		String name = (String) kimFeatures.get(objKey2);
		     		String classType = (String) kimFeatures.get(objKey3);
		     		
		     		
		     		
		     			
		     	    String [] annotations = new String [5];
		     	    annotations[0] = instance;
		     		annotations[1] = startOffset;
		     		annotations[2] = endOffset;
		     		annotations[3] = classType;
		     		annotations[4] = name;
		     		annotationsListResult.add(annotations);
		     			
		     		
		     	 
		      }
			     } 
			
			
			return annotationsListResult; 
		
		
	}
	
}
