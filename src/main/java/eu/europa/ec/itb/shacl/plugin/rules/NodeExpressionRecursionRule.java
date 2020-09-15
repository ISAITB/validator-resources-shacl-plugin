package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: node-expression-recursion
 * Rule Definition: https://www.w3.org/TR/shacl-af/#syntax-rule-node-expressions-recursion
 * Rule Description: A node expression cannot recursively have itself as a "nested" node expression, e.g. as value of sh:nodes.
 * @author mfontsan
 *
 */
public class NodeExpressionRecursionRule extends JenaModelUtils  implements Rules {	
	private static String filterShapeProperty = shaclNamespace + "filterShape";
	private static String nodeProperty = shaclNamespace + "nodes";
	private static String pathProperty = shaclNamespace + "path";
	private static String thisProperty = shaclNamespace + "this";
	
	private String ruleDescription = "A node expression cannot recursively have itself as a \"nested\" node expression, e.g. as value of sh:nodes.";
	

	public NodeExpressionRecursionRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
		//As part of sh:filterShape, sh:node, sh:path, sh:this
		List<Statement> listFS = getListObjects(filterShapeProperty);
		listFS.addAll(getListObjects(pathProperty));
		listFS.addAll(getListObjects(nodeProperty));
		listFS.addAll(getListObjects(thisProperty));
		
		getTripleProperty(listFS);
	}
	
	/**
	 * List statements with the predicate propertyName
	 * @param propertyName
	 * @return
	 * 		returns java.util.List<Statement>
	 */
	private List<Statement> getListObjects(String propertyName) {
		List<Statement> listStatements = new ArrayList<>();
		
		Property pPredicate = getProperty(propertyName);
		NodeIterator ni = this.currentModel.listObjectsOfProperty(pPredicate);
		
		for(RDFNode rObject : ni.toList()) {			
			StmtIterator si = this.currentModel.listStatements(null, pPredicate, rObject);
			
			listStatements.addAll(si.toList());
		}
		
		return listStatements;
	}
	
	/**
	 * Validate the subject is not nested as object of the same Statement.
	 * @param listFS
	 */
	private void getTripleProperty(List<Statement> listFS) {		
		for(Statement statement : listFS){
			boolean recursive = false;
			
			if(StringUtils.equals(statement.getPredicate().toString(), statement.getObject().toString())) {
				recursive = true;
			}else {
				recursive = isRecursive(statement.getSubject(), statement.getObject());
			}
			
			if(recursive) {
				report.setErrorItem(ruleDescription, statement.getPredicate().toString(), null, null, getMainShape(statement.getObject().asResource()).toString());						
			}
		}
	}
	
	/**
	 * Validate the recursivity of a Resource.
	 * @param rSubject
	 * @param originalObject
	 * @return
	 * 		returns boolean
	 */
	private boolean isRecursive(Resource rSubject, RDFNode originalObject) {
		boolean recursive = false;
		
		RDFNode subjectNode = this.currentModel.asRDFNode(rSubject.asNode());
		StmtIterator subjectIterator = this.currentModel.listStatements(null, null, subjectNode);
				
		if(!rSubject.isURIResource()) {
			while(subjectIterator.hasNext()) {
				Statement statement = subjectIterator.next();
				
				if(statement.getPredicate().isURIResource()) {
					String uriResource = statement.getPredicate().getURI();

					if(StringUtils.equals(originalObject.toString(), uriResource)) {
						return true;
					}
					
					if(StringUtils.contains(uriResource, shaclNamespace)) {
						return false;
					}
					recursive = isRecursive(statement.getSubject(), originalObject);
				}
			}
		}
		
		return recursive;
	}
	
}
