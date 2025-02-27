package eu.europa.ec.itb.shacl.plugin.rules.advancedFeature;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: Intersection / Union
 * Rule Definition: https://www.w3.org/TR/shacl-af/#syntax-rule-union // https://www.w3.org/TR/shacl-af/#syntax-rule-intersection
 * Rule Description: Intersection expression is a blank node with exactly one value for the property sh:intersection. Union expression is a blank node with exactly one value for the property sh:union. Intersection/Union expression is a well-formed SHACL list with at least two members.
 * @author mfontsan
 *
 */
public class IntersectionUnionListRule extends JenaModelUtils  implements Rules {	
	private static String unionProperty = shaclNamespace + "union";
	private static String intersectionProperty = shaclNamespace + "intersection";
	
	private String ruleDescriptionIntersection = "Intersection expression is a blank node with exactly one value for the property sh:intersection. Intersection expression is a well-formed SHACL list with at least two members.";
	private String ruleDescriptionUnion = "Union expression is a blank node with exactly one value for the property sh:union. Union expression is a well-formed SHACL list with at least two members.";
	

	public IntersectionUnionListRule(Model currentModel, Report report, File fileContent) {
		super(currentModel, report);
	}

	public void validateRule() {
		validateList(unionProperty, ruleDescriptionUnion);
		validateList(intersectionProperty, ruleDescriptionIntersection);		
	}
	
	/**
	 * Validate the propertyName is a list with at least 2 members.
	 * @param propertyName
	 * @param ruleDescription
	 */
	private void validateList(String propertyName, String ruleDescription) {
		//1. Get sh:intersection / sh:union		
		NodeIterator ri = getObjectsOfProperty(propertyName);
		
		while(ri.hasNext()) {
			RDFNode res = ri.next();
			
			if(res.isResource()) {
				//2. Validate is a list
				try {
					RDFList list = this.currentModel.getList(res.asResource());	
					
					//3. Validate it has at least 2 members
					if(list.size()<2) {	
						String shape = getMainShape(res.asResource()).toString();
						report.setErrorItem(ruleDescription, propertyName, shape, null, shape);						
					}
				}catch(Exception e){	
					String shape = getMainShape(res.asResource()).toString();
					report.setErrorItem(ruleDescription, propertyName, shape, null, shape);					
				}
			}
			
		}
	}
	
}
