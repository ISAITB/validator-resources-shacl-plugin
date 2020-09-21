package eu.europa.ec.itb.shacl.plugin.rules.bestPractice;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: order
 * Rule Definition: https://www.w3.org/TR/shacl/#defaultValue
 * Rule Description: The value type of the sh:defaultValue should align with the specified sh:datatype or sh:class of the same shape.
 * @author mfontsan
 *
 */
public class DefaultValueDatatypeRule extends JenaModelUtils  implements Rules {
	private static String defaultValue = shaclNamespace + "defaultValue";
	private static String datatypeURI = shaclNamespace + "datatype";
	private static String classURI = shaclNamespace + "class";
	
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#PropertyShape";	
	private String ruleDescription = "The value type of the sh:defaultValue should align with the specified sh:datatype or sh:class of the same shape.";
	

	public DefaultValueDatatypeRule(Model currentModel, Report report, File fileContent) {
		super(currentModel, report);
	}

	public void validateRule() {
    	Property defaultValueProperty = getProperty(defaultValue);
    	Property datatypeProperty = getProperty(datatypeURI);
    	Property classProperty = getProperty(classURI);
    	
    	ResIterator itSubjects = this.currentModel.listResourcesWithProperty(defaultValueProperty);
    	
    	while(itSubjects.hasNext()) {
    		boolean ruleOK = true;
    		Resource subject = itSubjects.next();
    		
    		ruleOK = validateDefaultValue(subject, defaultValueProperty, datatypeProperty, classProperty);
    		
    		if(!ruleOK) {
    			Resource shape = getMainShape(subject);
				report.setWarningItem(ruleDescription, reportAssertionID, shape.toString(), null, shape.toString());	
    		}
    	}
    	
	}
	
	/**
	 * Validate sh:defaultValue is aligned with sh:dataType or sh:class.
	 * @param subject
	 * @param defaultValueProperty
	 * @param datatypeProperty
	 * @param classProperty
	 * @return
	 * 		returns boolean
	 */
	private boolean validateDefaultValue(Resource subject, Property defaultValueProperty, Property datatypeProperty, Property classProperty) {
		boolean ruleOK = true;
		
		List<RDFNode> listDefaultValues = this.currentModel.listObjectsOfProperty(subject, defaultValueProperty).toList();
		List<RDFNode> listDatatypes = this.currentModel.listObjectsOfProperty(subject, datatypeProperty).toList();
		List<RDFNode> listClasses = this.currentModel.listObjectsOfProperty(subject, classProperty).toList();
		
		if((listDatatypes.isEmpty() && listClasses.isEmpty()) || (!listDatatypes.isEmpty() && !listClasses.isEmpty())) {
			//Properties sh:class and sh:datatype do not exist, or both exist at the same time (this case, there is an error in SHACL specs.
			ruleOK = false;
		}else {
			if(listDefaultValues.size()==1) {
				RDFNode defaultValueNode = listDefaultValues.get(0);
				
				if(!listClasses.isEmpty()) {
					ruleOK = getClassesAlignment(listClasses, defaultValueNode);
				}else {
					ruleOK = getDatatypesAlignment(listDatatypes, defaultValueNode);
				}
			}
		}
		
		return ruleOK;
	}

	/**
	 * sh:defaultValue is equal to sh:datatype.
	 * @param rdfNodes
	 * @param defaultValue
	 * @return
	 * 		returns boolean
	 */
	private boolean getDatatypesAlignment(List<RDFNode> rdfNodes, RDFNode defaultValue) {
		boolean isAligned = true;
		
		if(!rdfNodes.isEmpty()) {
			String node = rdfNodes.get(0).toString();
			String value = StringUtils.lowerCase(defaultValue.asLiteral().getString());
			
			try {
				if(StringUtils.equals(node, "xsd:decimal") || StringUtils.contains(node, "int") || StringUtils.contains(node, "long") || StringUtils.contains(node, "short")){
					Integer.valueOf(value);
				}
				if(StringUtils.contains(node, "byte")){
					Byte.valueOf(value);
				}
				if(StringUtils.contains(node, "boolean")){
					Boolean.valueOf(value);
				}
				if(StringUtils.contains(node, "date")){
					Date.valueOf(value);
				}
				if(StringUtils.contains(node, "time")){
					Time.valueOf(value);
				}
				
			}catch(Exception e) {
				isAligned = false;
			}
		}		
		
		return isAligned;
	}

	/**
	 * sh:defaultValue is equal to sh:class.
	 * @param rdfNodes
	 * @param defaultValue
	 * @return
	 * 		returns boolean
	 */
	private boolean getClassesAlignment(List<RDFNode> rdfNodes, RDFNode defaultValue) {
		if(!rdfNodes.isEmpty() && !rdfNodes.get(0).equals(defaultValue)) {
			return false;
		}
		
		return true;
	}
	
}
