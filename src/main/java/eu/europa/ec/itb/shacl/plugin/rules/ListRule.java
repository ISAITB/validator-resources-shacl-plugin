package eu.europa.ec.itb.shacl.plugin.rules;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: lists
 * Rule Definition: https://www.w3.org/TR/shacl/#defaultValue
 * Rule Description: Avoid large rdf:list due to its innefficiency.
 * @author mfontsan
 *
 */
public class ListRule extends JenaModelUtils  implements Rules {
	private static String listUri = rdfNamespace + "List";
	private int maxSize = 500;
	
	private String ruleDescription = "Avoid large rdf:list due to its innefficiency.";
	

	public ListRule(Model currentModel, Report report) {
		super(currentModel, report);
	}

	public void validateRule() {
    	Resource listProperty = this.currentModel.getResource(listUri);
    	
    	ResIterator itSubjects = this.currentModel.listResourcesWithProperty(null, this.currentModel.asRDFNode(listProperty.asNode()));
    	
    	while(itSubjects.hasNext()) {
    		Resource subject = itSubjects.next();
    		
    		RDFList list = this.currentModel.getList(subject);
    		
    		if(list.size()>maxSize) {
    			report.setWarningItem(ruleDescription, listUri, null, null, this.getMainShape(subject).toString());
    		}
    	} 	
	}
	
}
