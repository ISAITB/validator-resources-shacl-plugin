package eu.europa.ec.itb.shacl.plugin.rules;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: namespace-prefixes
 * Rule Definition: https://joinup.ec.europa.eu/collection/semantic-interoperability-community-semic (ID: 84)
 * Rule Description: The local name of the NodeShape SHALL be the ClassName + Shape.
 * @author mfontsan
 *
 */
public class NameNodeshapeRule extends JenaModelUtils  implements Rules {	
	private String ruleDescription = "The local name of the NodeShape SHALL be the ClassName + Shape.";
	private static String reportAssertionID = shaclNamespace+"NodeShape";
	
	private String nodeShapeProperty = shaclNamespace + "NodeShape";
	private String targetClassProperty = shaclNamespace + "targetClass";
	private String localName = "Shape";
	
	public NameNodeshapeRule(Model currentModel, Report report) {		
		super(currentModel, report);
	}


	public void validateRule() {
		int warnings = 0;
		
		Node nodeShapeNode = this.currentModel.getProperty(nodeShapeProperty).asNode();
		
		ResIterator listSubjects = this.currentModel.listResourcesWithProperty(null, this.currentModel.asRDFNode(nodeShapeNode));
		
		while(listSubjects.hasNext()) {
			Resource subject = listSubjects.next();
			
			NodeIterator listObjects = this.currentModel.listObjectsOfProperty(subject, this.currentModel.getProperty(targetClassProperty));
			boolean hasWarning = false;
			
			
			while(listObjects.hasNext() && !hasWarning) {
				RDFNode className = listObjects.next();
				String subjectName = subject.getLocalName();
				
				if(!StringUtils.contains(subjectName, localName) || !StringUtils.contains(subjectName, className.asResource().getLocalName())) {
					warnings++;
					hasWarning = true;
					report.setWarningItem(ruleDescription, reportAssertionID, null, null, subjectName);
				}
			}
		}
		
		report.setWarnings(warnings);
	}
	
}
