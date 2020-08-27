package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementVisitorBase;
import org.apache.jena.sparql.syntax.ElementWalker;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: pre-binding-limitations
 * Rule Definition: https://www.w3.org/TR/shacl/#syntax-rule-pre-binding-limitations
 * Rule Description: Subqueries must return all potentially pre-bound variables, except shapesGraph and currentShape which are optional as already mentioned in 5.3.1 Pre-bound Variables in SPARQL Constraints ($this, $shapesGraph, $currentShape).
 * @author mfontsan
 *
 */
public class PreBindingLimitationsRule extends JenaModelUtils  implements Rules {
	private static String selectProperty = shaclNamespace + "select";
	private static String askProperty = shaclNamespace + "ask";
	private static String preBindingThis = "this";
	
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#select";
	
	private String ruleDescription = "Subqueries must return all potentially pre-bound variables, except shapesGraph and currentShape which are optional as already mentioned in 5.3.1 Pre-bound Variables in SPARQL Constraints ($this, $shapesGraph, $currentShape).";
	

	public PreBindingLimitationsRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
    	//pre-binding-limitations
		List<RDFNode> listNodes = new ArrayList<>();

        //1. SHACL-SPARQL queries via sh:select
        listNodes.addAll(getSPARQLSelectQuery());
        
        //2. SHACL-SPARQL queries via sh:ask
        listNodes.addAll(getSPARQLAskQuery()); 
        
        
    	for(RDFNode node : listNodes) {    		
    		if(node.isLiteral()) {
    			Query query = getQuery(node.asLiteral().getString());
    			if(query!=null) {
	    			boolean invalid = isSubqueryValid(query);
	    			
	    			if(invalid) {
	    				report.setErrors(1);    				
	    				report.setErrorItem(ruleDescription, reportAssertionID, getLocation(node), null, node.asLiteral().getString());
	    			}
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
	 * Validate that subqueries return all potentially pre-bound variables ("$this").
	 * @param query
	 * @return
	 * 		boolean
	 */
	private boolean isSubqueryValid(Query query) {		
		final Set<Boolean> subjects = new HashSet<>();
				
		ElementWalker.walk(query.getQueryPattern(),
		    new ElementVisitorBase() {
				public void visit(ElementSubQuery el) {		
					List<Var> queryVar = el.getQuery().getProjectVars();
					
					if(queryVar==null || !queryVar.contains(Var.alloc(preBindingThis))) {
						subjects.add(false);
					}
		        }
		    }
		);
		
		if(!subjects.isEmpty()) {
			return true;
		}

		return false;
	}
	
	/**
	 * Get the list of RDFNode that are SPARQL via sh:select.
	 * @return
	 * 		returns java.util.List<RDFNode>
	 */
	private List<RDFNode> getSPARQLSelectQuery(){
        //1. SHACL-SPARQL queries via sh:select
    	NodeIterator ni = this.currentModel.listObjectsOfProperty(this.currentModel.getProperty(selectProperty));
                  
        return ni.toList();
	}
	
	/**
	 * Get the list of RDFNode that are SPARQL via sh:ask.
	 * @return
	 * 		returns java.util.List<RDFNode>
	 */
	private List<RDFNode> getSPARQLAskQuery(){
        //2. SHACL-SPARQL queries via sh:ask
    	NodeIterator ni = this.currentModel.listObjectsOfProperty(this.currentModel.getProperty(askProperty));
                  
        return ni.toList();
	}
}
