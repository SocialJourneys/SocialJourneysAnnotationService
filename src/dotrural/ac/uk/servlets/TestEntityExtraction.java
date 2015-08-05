package dotrural.ac.uk.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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



/**
 * Servlet implementation class tesEntityExtraction
 */

public class TestEntityExtraction extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TestEntityExtraction() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
		
		
		
		res.setContentType("text/html");//setting the content type  
		PrintWriter pw=res.getWriter();//get the stream to write the data  
		  
		//writing html in the stream  
		pw.println("<html><body>");  
		pw.println("Enter the text that you want to process"); 
		pw.println("<br>"); 
		pw.println("<br>");  
		pw.println("<form action='/kim/test' method='post'>"); 
		pw.println("<textarea name='textInput' rows='5' cols='50' > </textarea>"); 
		pw.println("<input type='submit' value='Submit'>"); 
		pw.println("</form>"); 
		pw.println("</body></html>");  
		
		pw.close();//closing the stream  
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
		
		if  (request.getParameter("service")!= null) {
			
			if (request.getParameter("service").equals("getJSON")) {
			
				String text;
			
				if ((request.getParameter("textInput")==null)) {
					text = "";
			    }
				else {
					text = request.getParameter("textInput");
				}
			
			
			String jsonObject = extractEntities (text,request); 
			
			res.setContentType("application/json");     
			PrintWriter out = res.getWriter(); 
			out.print(jsonObject);
			out.flush();
		   
			}
		}
		
		else {
			
		
		
		res.setContentType("text/html");//setting the content type  
		PrintWriter pw=res.getWriter();//get the stream to write the data  
		  
		//writing html in the stream  
		pw.println("<html><body>");  
		
		pw.println("<form action='/kim/test' method='get'>");
		pw.println("<input type='submit' value='Test another text'"); 
		pw.println("name='Submit' id='frm1_submit' />");
		pw.println("</form>");
		
		pw.println("Recieved text:"); 
		pw.println("<br>"); 
		pw.println("<br>");  
		pw.println("<i>"+request.getParameter("textInput")+"</i>"); 
		pw.println("<br>");  
		pw.println("-----------------------------------------------");
		  
		
			
		

		// ----------------------------------------------------------------------------------
		    // connect to KIMService (deployed on specific host and port)
		    KIMService serviceKim = GetService.from();
		    pw.println("<br>");
		    pw.println("Config: KIM Server connected.");
		 
		    // obtain CorporaAPI and DocumentRepositoryAPI components
		    CorporaAPI apiCorpora = serviceKim.getCorporaAPI();
		 
		    DocumentRepositoryAPI apiDR = serviceKim.getDocumentRepositoryAPI();
		    pw.println("<br>");
		    pw.println("Config: CorporaAPI and DocumentRepositoryAPI obtained successfully.");
		    pw.println("<br>");
		    SemanticAnnotationAPI apiSemAnn = serviceKim.getSemanticAnnotationAPI();
		    pw.println("Config: SemanticAnnotationAPI obtained successfully.");
	        pw.println("<br>");
		    // ----------------------------------------------------------------------------------

		 // ----------------------------------------------------------------------------------
		     // annotate texts
		     // ----------------------------------------------------------------------------------
		     String content = request.getParameter("textInput");
		     pw.println("Process: Started Annotating.");
		     pw.println("<br>");
		     KIMAnnotationSet kimASet = apiSemAnn.execute(content);
		     pw.println("Process: Free text annotated.");
		     pw.println("<br>");
		     pw.println("-----------------------------------------------");
		     pw.println("<br>");
		     pw.println("Result of text annotation process:");
		     pw.println("<br>");
		     
		     Iterator annIterator = kimASet.iterator();
		     
		     pw.println("[ Annotations' Features (begin) ]");
		     pw.println("<br>");
		     while (annIterator.hasNext()) {
		         KIMAnnotation kimAnnotation = (KIMAnnotation) annIterator.next();
		         /* pw.println(" = [ Annotation ] : " + kimAnnotation);
		         pw.println("<br>");*/
		         KIMFeatureMap kimFeatures = kimAnnotation.getFeatures();
		        
		         if (kimFeatures != null) {
		             Iterator iterator = kimFeatures.keySet().iterator();
		             pw.println(" = [ Features ] : ");
		             pw.println("<br>");
		      
		             while (iterator.hasNext()) {
		                 Object key = iterator.next();
		                 pw.println(" -- [key: " + key + "] [feature: " + kimFeatures.get(key) + "]");
		                 pw.println("<br>");
		             }
		             pw.println("");
		             pw.println("<br>");
		         }
		     }

		     
	    pw.println("Annotation set END");
	    pw.println("<br>");
	    pw.println("-----------------------------------------------");
	    pw.println("<br>");
	    
	    pw.println("<h3>Effects on Bus Services</h3>");
	    pw.println("<br>");
	    pw.println("<table border='1' style='width:100%'>");
	    pw.println("<tr>");
	    pw.println("<td>Street name</td>");
	    pw.println("<td>Bus Services Affected</td>");
	    pw.println("</tr>");
	   
	    
	    infferEffects (pw, kimASet,request);
	    
	    pw.println("</table>");
	    
	   
	    
	    
	   
	   // ----------------------------------------------------------------------------------
	    pw.println("</body></html>");    
		pw.close();//closing the stream  
		
		}

	}
	
private static void  printResults (HashMap map, PrintWriter pw) {
		
		Iterator it = map.keySet().iterator();
	
		while (it.hasNext() ) {
			pw.println("<tr>");
			Object key = it.next();
		    pw.println("<td>"+(String) key+"</td>");
		    pw.println("<td>");
		    ArrayList resultList = (ArrayList) map.get(key);
		    
		    for (int i=0;i<resultList.size();i++) {
		    	//check if this element last
		    	if (i+1 ==resultList.size() ) {
		    	pw.println(resultList.get(i));
		    	}
		    	//add comma
		    	else {
		    		pw.println(resultList.get(i)+",");
		    	}
		   /* ResultSet results = (ResultSet) resultList.get(i);
		    while (results.hasNext() ) {
	    	QuerySolution  rs =results.next();
	    	//Iterator <String>	it = rs.varNames();
	    			String varName = "label"; 
	    			if (rs.get(varName).isResource()) {
	    				pw.println(rs.get(varName).asResource().getURI()+",");
	    				pw.println(" ");
	    			}
	    			else {
	    				pw.println(rs.get(varName).asLiteral().getString()+",");
	    			}
	    		
	    			pw.println("");
	            }8*/
		    }
		    pw.println("</td>");
		}
		 pw.println("</tr>");
	}
	
 

private void infferEffects (PrintWriter pw,KIMAnnotationSet kimASet,HttpServletRequest request ) {
	 Iterator it = kimASet.iterator();
	    
	    InfferEffectsOnBusRoutes  infferenceEngine = new InfferEffectsOnBusRoutes (request.getServletContext());
	    HashMap effectsMapResult = new HashMap (); 
	    
	    
	     while (it.hasNext()) {
	         KIMAnnotation kimAnnotation = (KIMAnnotation) it.next();
	         
	         KIMFeatureMap kimFeatures = kimAnnotation.getFeatures();
	      
	         if (kimFeatures != null) {
	        	 
	        	if ( kimFeatures.containsValue("http://sj.abdn.ac.uk/ontology/Highway")||kimFeatures.containsValue("http://transport.data.gov.uk/def/naptan/Stop") ) {
	        		//Object featureKey = "inst"; 
	        		String keyString1 = "inst";
	        		Object objKey1 = keyString1;
	        		
	        		String keyString2 = "originalName";
	        		Object objKey2 = keyString2;
	        		
	        		
	        		
	        		String wayResource = (String) kimFeatures.get(objKey1);
	        		String name = (String) kimFeatures.get(objKey2);
	        		
	        		if (!effectsMapResult.containsKey(kimFeatures.get(objKey2))) {
	        			ArrayList affectedBuses  = new ArrayList();
	        			effectsMapResult.put(kimFeatures.get(objKey2), affectedBuses);
	        		}
	        		
	        		ResultSet temp = infferenceEngine.getEffectsOnBusRoutes(wayResource);
	        		ArrayList tempList = (ArrayList) effectsMapResult.get(kimFeatures.get(objKey2));
	        		
	        		while (temp.hasNext() ) {
	        	    	QuerySolution  rs =temp.next();
	        	    	//Iterator <String>	it = rs.varNames();
	        	    			String varName = "label"; 
	        	    			if (rs.get(varName).isResource()) {
	        	    				
	        	    				if (!tempList.contains(rs.get(varName).asResource().getURI())) {
	        		        			tempList.add(rs.get(varName).asResource().getURI());
	        		        		}
	        	    				
	        	    			}
	        	    			else {
	        	    				if (!tempList.contains(rs.get(varName).asLiteral().getString())) {
	        		        			tempList.add(rs.get(varName).asLiteral().getString());
	        		        		}
	        	    			}
	        	    		
	        	    			pw.println("");
	        	            }
	        		
	        	}
	        	 
	         }
	          
	         }
	     
	     printResults(effectsMapResult,pw);
	     
 }





private String extractEntities (String content, HttpServletRequest request) throws RemoteException, UnsupportedEncodingException {
	String jsonString = "{\"entities\":\"error\"}";
	
	 KIMService serviceKim = GetService.from();
	 // obtain CorporaAPI and DocumentRepositoryAPI components
	 CorporaAPI apiCorpora = serviceKim.getCorporaAPI();
	 DocumentRepositoryAPI apiDR = serviceKim.getDocumentRepositoryAPI();
	 SemanticAnnotationAPI apiSemAnn = serviceKim.getSemanticAnnotationAPI();
	 KIMAnnotationSet kimASet = apiSemAnn.execute(content);
	 
	 Iterator it = kimASet.iterator();
	 InfferEffectsOnBusRoutes  infferenceEngine = new InfferEffectsOnBusRoutes (request.getServletContext());
	 HashMap effectsMapResult = new HashMap (); 
	 HashMap <String, Object> classMapResult = new HashMap ();
	 
	 
	    
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
     		
     		if (!effectsMapResult.containsKey(kimFeatures.get(objKey2))) {
     			ArrayList affectedBuses  = new ArrayList();
     			effectsMapResult.put(kimFeatures.get(objKey2), affectedBuses);
     		}
     		
     		if (classMapResult.containsKey(name)) {
     			ArrayList [] tempList = (ArrayList[]) classMapResult.get(name);
     			if (!tempList[0].contains(classType)) {
     				tempList[0].add(classType);
     			}
     			
     			String [] instanceTripple = new String [4];
     			instanceTripple[0] = instance;
     			instanceTripple[1] = startOffset;
     			instanceTripple[2] = endOffset;
     			instanceTripple[3] = classType;
     			tempList[1].add(instanceTripple);
     			
     			classMapResult.put((String) kimFeatures.get(objKey2), tempList);
     		}
     		
     		if (!classMapResult.containsKey(kimFeatures.get(objKey2))) {
     			ArrayList[] newList = new ArrayList [2];
     			newList[0] = new ArrayList ();
     			newList[1] = new ArrayList <String []>();
     			newList[0].add(classType);
     			String [] instanceTripple = new String [4];
     			instanceTripple[0] = instance;
     			instanceTripple[1] = startOffset;
     			instanceTripple[2] = endOffset;
     			instanceTripple[3] = classType;
     			newList[1].add(instanceTripple);
     			classMapResult.put((String) kimFeatures.get(objKey2), newList);
     			
     		}
     		
     		
     		

     		ResultSet temp = infferenceEngine.getEffectsOnBusRoutes(instance);
     		ArrayList tempList = (ArrayList) effectsMapResult.get(kimFeatures.get(objKey2));
     		
     		while (temp.hasNext() ) {
     	    	QuerySolution  rs =temp.next();
     	    	//Iterator <String>	it = rs.varNames();
     	    			String varName = "label"; 
     	    			if (rs.get(varName).isResource()) {
     	    				
     	    				if (!tempList.contains(rs.get(varName).asResource().getURI())) {
     		        			tempList.add(rs.get(varName).asResource().getURI());
     		        		}
     	    				
     	    			}
     	    			else {
     	    				if (!tempList.contains(rs.get(varName).asLiteral().getString())) {
     		        			tempList.add(rs.get(varName).asLiteral().getString());
     		        		}
     	    			}
     	            }
     		
     
     	 
      }
	     }
	
	if (effectsMapResult.keySet()!=null) {
		
	jsonString = "{\"text\":"+  JSONObject.quote(content) +",";	
    jsonString = jsonString + "\"entities\":[";
    
	Iterator keyIt =   effectsMapResult.keySet().iterator(); 
	
	//fix for cases where no entities found
	if (!keyIt.hasNext()) {
		jsonString = jsonString + " ";
	}
	
	while (keyIt.hasNext()) {
	Object key = keyIt.next(); 
	jsonString = jsonString + "{\"name\":\""+  key +"\",";
	
	jsonString = jsonString + "\"types\":[";
	
	ArrayList[] temp = (ArrayList[]) classMapResult.get(key);
	ArrayList resultTypeList = temp[0];
	
	
	
	for (int i = 0;i<resultTypeList.size();i++) {
		if (i+1!=resultTypeList.size()) {
		jsonString = jsonString + "{\"type\":\""+  resultTypeList.get(i) +"\"},";
		}
		else {
			jsonString = jsonString + "{\"type\":\""+  resultTypeList.get(i) +"\"}";
		}
	} 
	
	
	jsonString = jsonString + "],";
	
   jsonString = jsonString + "\"instances\":[";
	
	
	ArrayList <String []> resultInstanceList = temp [1];
	
	
	
	for (int i = 0;i<resultInstanceList.size();i++) {
		
		String [] tempstringArray = resultInstanceList.get(i);
		
		if (i+1!=resultInstanceList.size()) {
		jsonString = jsonString + "{\"instance\":\""+  tempstringArray[0] +"\",\"startOffSet\":\""+  tempstringArray[1] +"\",\"endOffSet\":\""+  tempstringArray[2] +"\",\"type\":\""+  tempstringArray[3] +"\"},";
		}
		else {
			jsonString = jsonString + "{\"instance\":\""+  tempstringArray[0] +"\",\"startOffSet\":\""+  tempstringArray[1] +"\",\"endOffSet\":\""+  tempstringArray[2] +"\",\"type\":\""+  tempstringArray[3] +"\"}";
		}
	} 
	
	
	jsonString = jsonString + "],";
	
	ArrayList resultList = (ArrayList) effectsMapResult.get(key);
	 
	jsonString = jsonString + "\"bus_services_affected\":[";
	 
	    for (int i=0;i<resultList.size();i++) {
	    	if (i+1 ==resultList.size() ) {
	    		jsonString = jsonString + "{\"service\":\""+  resultList.get(i) +"\"}";
		    	}
		    	//add comma
		    	else {
		    		jsonString = jsonString + "{\"service\":\""+  resultList.get(i) +"\"},";
		    	}
	    	
	    }
	 jsonString = jsonString + "]},";
	}
	
	jsonString = jsonString.substring(0, jsonString.length()-1);
	
	jsonString = jsonString +"]}";    
	
	}
	return jsonString; 
}
}
