package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: namespace-prefixes
 * Rule Definition: https://joinup.ec.europa.eu/collection/semantic-interoperability-community-semic (ID: 82)
 * Rule Description: Prefixes for all namespaces SHOULD be defined.
 * @author mfontsan
 *
 */
public class NamespacePrefixesRule extends JenaModelUtils  implements Rules {	
	private String ruleDescription = "Prefixes for all namespaces SHOULD be defined.";
	private static String reportAssertionID = "prefix";
	
	public NamespacePrefixesRule(Model currentModel, Report report) {		
		super(currentModel, report);
	}


	public void validateRule() {		
		Map<String, String> mNamespace = this.currentModel.getNsPrefixMap();
		
		if(!mNamespace.isEmpty()) {
			Set<String> setNs = mNamespace.keySet();
			
			for(String ns: setNs) {
				if(StringUtils.isEmpty(ns)) {
					report.setWarningItem(ruleDescription, reportAssertionID, mNamespace.get(ns), null, mNamespace.get(ns));					
				}
			}
		}
	}
	
}
