package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: PATH-position 
 * Rule Definition: https://www.w3.org/TR/shacl/#syntax-rule-PATH-position
 * Rule Description: The only legal use of the variable PATH in the SPARQL queries of SPARQL-based constraints and SELECT-based validators is in the predicate position of a triple pattern.
 * @author mfontsan
 *
 */
public class PathPositionRule extends JenaModelUtils  implements Rules {
	private static String sparqlProperty = shaclNamespace + "sparql";
	private static String selectProperty = shaclNamespace + "select";
	private static String selectBased = shaclNamespace + "SPARQLSelectValidator";
	private static String pathPredicate = "?PATH";
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#select";
	
	private String ruleDescription = "The only legal use of the variable PATH in the SPARQL queries of SPARQL-based constraints and SELECT-based validators is in the predicate position of a triple pattern.";
	

	public PathPositionRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
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
    				report.setErrorItem(ruleDescription, reportAssertionID, getLocation(node), null, node.asLiteral().getString());
    			}
    		}
    	}
	}
	
	/**
	 * Get the location in the SHACL shape of the error.
	 * @param node
	 * @return
	 * 		returns java.lang.String
	 */
	private String getLocation(RDFNode node) {			
		StmtIterator statementIt = this.currentModel.listStatements(null, null, node);
		
		while(statementIt.hasNext()) {
			Statement statement = statementIt.next();
			
			if(statement.getSubject().isURIResource()) {
				return statement.getSubject().getURI();
			}
			if(statement.getPredicate().isURIResource()) {
				return statement.getPredicate().getURI();					
			}
		}
		
		return StringUtils.EMPTY;
	}
	
	/**
	 * Validate that "$PATH" is in predicate position of the SPARQL query.
	 * @param query
	 * @return
	 * 		boolean
	 */
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
	
	/**
	 * Get the list of RDFNode that are SELECT-based validators.
	 * @return
	 * 		returns java.util.List<RDFNode>
	 */
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
	
	/**
	 * Get the list of RDFNode that are SPARQL-based constraint.
	 * @return
	 * 		returns java.util.List<RDFNode>
	 */
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
