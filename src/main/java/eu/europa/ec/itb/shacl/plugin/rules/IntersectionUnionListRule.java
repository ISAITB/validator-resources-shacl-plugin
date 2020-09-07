package eu.europa.ec.itb.shacl.plugin.rules;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
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
	

	public IntersectionUnionListRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
		int errorUnion = getValuesProperty(unionProperty, ruleDescriptionUnion);
		int errorIntersection = getValuesProperty(intersectionProperty, ruleDescriptionIntersection);
		
		report.setErrors(errorUnion + errorIntersection);
	}
	
	private int getValuesProperty(String propertyName, String ruleDescription) {
		//1. Get sh:intersection / sh:union
		int errors = 0;
		Property pParameter = this.currentModel.getProperty(propertyName);
		
		NodeIterator ri = this.currentModel.listObjectsOfProperty(pParameter);
		
		while(ri.hasNext()) {
			RDFNode res = ri.next();
			
			if(res.isResource()) {
				//2. Validate is a list
				try {
					RDFList list = this.currentModel.getList(res.asResource());	
					
					//3. Validate it has at least 2 members
					if(list.size()<2) {	
						errors++;
						report.setErrorItem(ruleDescription, propertyName, null, null, res.asResource().toString());						
					}
				}catch(Exception e){	
					errors++;
					report.setErrorItem(ruleDescription, propertyName, null, null, res.asResource().toString());					
				}
			}
			
		}
		
		return errors;
	}
	
}
