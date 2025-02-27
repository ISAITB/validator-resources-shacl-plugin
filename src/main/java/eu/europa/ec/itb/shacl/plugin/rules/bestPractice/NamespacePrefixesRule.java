package eu.europa.ec.itb.shacl.plugin.rules.bestPractice;

import java.io.File;
import java.util.Map;
import java.util.Set;

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

    private static final String reportAssertionID = "prefix";
	
	public NamespacePrefixesRule(Model currentModel, Report report, File fileContent) {		
		super(currentModel, report);
	}

	public void validateRule() {
		Map<String, String> mNamespace = this.currentModel.getNsPrefixMap();
		if (!mNamespace.isEmpty()) {
			Set<String> setNs = mNamespace.keySet();
			for (String ns: setNs) {
				if (ns == null || ns.isEmpty()) {
                    String ruleDescription = "Prefixes for all namespaces SHOULD be defined.";
                    report.setWarningItem(ruleDescription, reportAssertionID, mNamespace.get(ns), null, mNamespace.get(ns));
				}
			}
		}
	}
	
}
