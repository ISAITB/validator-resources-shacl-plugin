package eu.europa.ec.itb.shacl.plugin.rules;

import org.apache.jena.rdf.model.Model;

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
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#";
	
	private String ruleDescriptionIntersection = "Intersection expression is a blank node with exactly one value for the property sh:intersection. Intersection expression is a well-formed SHACL list with at least two members.";
	private String ruleDescriptionUnion = "Union expression is a blank node with exactly one value for the property sh:union. Union expression is a well-formed SHACL list with at least two members.";
	

	public IntersectionUnionListRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
		//1. Get sh:intersection / sh:union
		//2. Validate is a list
		//3. Validate it has at least 2 members
	}
	
}
