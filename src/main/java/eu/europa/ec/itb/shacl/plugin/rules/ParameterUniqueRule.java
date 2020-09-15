package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: parameter-name-unique
 * Rule Definition: https://www.w3.org/TR/shacl/#syntax-rule-parameter-name-unique
 * Rule Description: A constraint component where two or more parameter declarations use the same parameter names is ill-formed.
 * @author mfontsan
 *
 */
public class ParameterUniqueRule extends JenaModelUtils  implements Rules {
	private static String parameterProperty = shaclNamespace + "parameter";
	private static String pathProperty = shaclNamespace + "path";
	
	private String ruleDescription = "A constraint component where two or more parameter declarations use the same parameter names is ill-formed.";
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#path";
	
	public ParameterUniqueRule(Model currentModel, Report report) {		
		super(currentModel, report);
	}


	public void validateRule() {
		Property pParameter = getProperty(parameterProperty);
		Property pPath = getProperty(pathProperty);
		List<Resource> subjectsParameter = this.currentModel.listResourcesWithProperty(pParameter).toList();
		
		for(Resource subject : subjectsParameter) {			
			List<RDFNode> objectParameter = this.currentModel.listObjectsOfProperty(subject, pParameter).toList();
			List<String> pathValues = new ArrayList<>();
			List<Resource> pathDuplicates = new ArrayList<>();
			
			for(RDFNode object : objectParameter) {
				if(object.isResource()) {
					Statement st = object.asResource().getProperty(pPath);
					
					if(!pathValues.contains(st.getObject().toString())) {
						pathValues.add(st.getObject().toString());						
					}else {
						pathDuplicates.add(st.getObject().asResource());
					}
				}
			}
			
			for(Resource duplicate : pathDuplicates) {  				
				report.setErrorItem(ruleDescription, reportAssertionID, subject.toString(), null, getMainShape(duplicate).toString());				
			}
		}

	}

}
