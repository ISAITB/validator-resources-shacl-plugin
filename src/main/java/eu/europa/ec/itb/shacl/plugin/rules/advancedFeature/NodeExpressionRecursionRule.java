package eu.europa.ec.itb.shacl.plugin.rules.advancedFeature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	private static final String filterShapeProperty = shaclNamespace + "filterShape";
	private static final String nodeProperty = shaclNamespace + "nodes";
	private static final String pathProperty = shaclNamespace + "path";
	private static final String thisProperty = shaclNamespace + "this";
	
	private final String ruleDescription = "A node expression cannot recursively have itself as a \"nested\" node expression, e.g. as value of sh:nodes.";

	public NodeExpressionRecursionRule(Model currentModel, Report report, File fileContent) {
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
		for (Statement statement: listFS){
			boolean recursive;
			if (Objects.equals(statement.getPredicate().toString(), statement.getObject().toString())) {
				recursive = true;
			} else {
				recursive = isRecursive(statement.getSubject(), statement.getObject());
			}
			if (recursive) {
				String shape = getMainShape(statement.getObject().asResource()).toString();
				report.setErrorItem(ruleDescription, statement.getPredicate().toString(), shape, null,shape);						
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
					if (Objects.equals(originalObject.toString(), uriResource)) {
						return true;
					}
					if (uriResource != null && uriResource.contains(shaclNamespace)) {
						return false;
					}
					recursive = isRecursive(statement.getSubject(), originalObject);
				}
			}
		}
		return recursive;
	}
	
}
