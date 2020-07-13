package eu.europa.ec.itb.shacl.plugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;
import org.apache.jena.vocabulary.RDF;

import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

public class PathPositionPlugin extends JenaModelUtils {
	private static String sparqlProperty = shaclNamespace + "sparql";
	private static String selectProperty = shaclNamespace + "select";
	private static String selectBased = shaclNamespace + "SPARQLSelectValidator";
	private static String pathPredicate = "?PATH";
	
	private String ruleDescription = "The only legal use of the variable PATH in the SPARQL queries of SPARQL-based constraints and SELECT-based validators is in the predicate position of a triple pattern.";
	

	public PathPositionPlugin(Model currentModel) {
		super(currentModel);
	}

	public Report validatePathPositionRule(Report report) {
    	//PATH-position
    	//The variable PATH is in the predicate position of a triple pattern.
		List<RDFNode> listNodes = new ArrayList<>();

        //1. SPARQL queries of SPARQL-based constraint
        listNodes.addAll(getSPARQLBased());
        
        //2. SELECT-based validators	
        listNodes.addAll(getSelectBased());        
        
        
    	for(RDFNode node : listNodes) {    		
    		if(node.isLiteral()) {
    			Query query = getQuery(node.asLiteral().getString());
    			boolean invalid = isPathPositionValid(query);
    			
    			if(invalid) {
    				report.setErrors(1);
    				
    				//TODO: set Location, Test and Value
    				report.setErrorItem(ruleDescription, null, null, null, node.asLiteral().getString());
    			}
    		}
    	}
    	
    	return report;
	}
	
	private boolean isPathPositionValid(Query query) {		
		String queryPattern = query.getQueryPattern().toString();		
		final Set<Boolean> subjects = new HashSet<>();
		
		if(queryPattern.contains(pathPredicate)) {
			
			ElementWalker.walk(query.getQueryPattern(),
			    new ElementVisitorBase() {
			        public void visit(ElementPathBlock el) {
			            Iterator<TriplePath> triples = el.patternElts();
			            while (triples.hasNext()) {
			            	TriplePath next = triples.next();
			                
			                if(next.getSubject().toString().equals(pathPredicate) || next.getObject().toString().equals(pathPredicate)) {
			                	subjects.add(false);
			                }
			            }
			        }
			    }
			);
			
			if(!subjects.isEmpty()) {
				return true;
			}
		}

		return false;
	}
	
	private List<RDFNode> getSelectBased(){
		List<RDFNode> listNodes = new ArrayList<>();
		
		//2. SELECT-based validators	
        StmtIterator si = this.currentModel.listStatements(null, RDF.type, this.currentModel.getResource(selectBased));
                    
        while(si.hasNext()) {
        	Statement statement = si.next();
	        
        	NodeIterator ni = this.currentModel.listObjectsOfProperty(statement.getSubject(), this.currentModel.getProperty(selectProperty));

    		listNodes.addAll(ni.toList());	
        }
        
        return listNodes;
	}
	
	private List<RDFNode> getSPARQLBased(){
		List<RDFNode> listNodes = new ArrayList<>();

        //1. SPARQL queries of SPARQL-based constraint
        NodeIterator niSparql = getObjectsOfProperty(sparqlProperty);
        
        while(niSparql.hasNext()) {
        	RDFNode node = niSparql.next();
        	
        	if(node.isResource()) {
        		NodeIterator niSelect = this.currentModel.listObjectsOfProperty(node.asResource(), this.currentModel.getProperty(selectProperty));	    		
	    		
        		listNodes.addAll(niSelect.toList());
        	}
        }
        
        return listNodes;
	}
}
