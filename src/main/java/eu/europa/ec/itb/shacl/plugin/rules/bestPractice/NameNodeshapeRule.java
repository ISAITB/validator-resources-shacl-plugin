package eu.europa.ec.itb.shacl.plugin.rules.bestPractice;

import java.io.File;

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

    private static final String reportAssertionID = shaclNamespace+"NodeShape";
	private final String nodeShapeProperty = shaclNamespace + "NodeShape";
	private final String targetClassProperty = shaclNamespace + "targetClass";

    public NameNodeshapeRule(Model currentModel, Report report, File fileContent) {
		super(currentModel, report);
	}

	public void validateRule() {		
		Node nodeShapeNode = getProperty(nodeShapeProperty).asNode();
		ResIterator listSubjects = this.currentModel.listResourcesWithProperty(null, this.currentModel.asRDFNode(nodeShapeNode));
		while (listSubjects.hasNext()) {
			Resource subject = listSubjects.next();
			NodeIterator listObjects = this.currentModel.listObjectsOfProperty(subject, this.currentModel.getProperty(targetClassProperty));
			boolean hasWarning = false;			
			while (listObjects.hasNext() && !hasWarning) {
				RDFNode className = listObjects.next();
				String subjectName = subject.getLocalName();
                String localName = "Shape";
                if (subjectName != null && (!subjectName.contains(localName) || !subjectName.contains(className.asResource().getLocalName()))) {
					hasWarning = true;
					String shape = this.getMainShape(subject).toString();
                    String ruleDescription = "The local name of the NodeShape SHALL be the ClassName + Shape.";
                    report.setWarningItem(ruleDescription, reportAssertionID, shape, null, shape);
				}
			}
		}
	}
	
}
