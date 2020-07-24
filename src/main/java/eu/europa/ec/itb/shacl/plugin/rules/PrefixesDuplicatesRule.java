package eu.europa.ec.itb.shacl.plugin.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: prefixes-duplicates 
 * Rule Definition: https://www.w3.org/TR/shacl/#syntax-rule-prefixes-duplicates
 * Rule Description: A SHACL processor collects a set of prefix mappings as the union of all individual prefix mappings that are values of the SPARQL property path sh:prefixes/owl:imports/sh:declare of the SPARQL-based constraint or validator. If such a collection of prefix declarations contains multiple namespaces for the same value of sh:prefix, then the shapes graph is ill-formed.
 * @author mfontsan
 *
 */
public class PrefixesDuplicatesRule extends JenaModelUtils  implements Rules {
	private static String declareProperty = shaclNamespace + "declare";
	private static String importProperty = owlNamespace + "imports";
	private static String prefixProperty = shaclNamespace + "prefix";
	private static String prefixesProperty = shaclNamespace + "prefixes";
	private static String namespaceProperty = shaclNamespace + "namespace";
	
	private Map<String, List<String>> listPrefixes;
	
	private String ruleDescription = "A SHACL processor collects a set of prefix mappings as the union of all individual prefix mappings that are values of the SPARQL property path sh:prefixes/owl:imports*/sh:declare of the SPARQL-based constraint or validator. If such a collection of prefix declarations contains multiple namespaces for the same value of sh:prefix, then the shapes graph is ill-formed.";
	private static String reportAssertionID = "http://www.w3.org/ns/shacl#declare";
	
	public PrefixesDuplicatesRule(Model currentModel, Report report) {		
		super(currentModel, report);
		listPrefixes = new HashMap<>();
	}


	public void validateRule() {
    	//prefixes-duplicates
		
		//1. sh:declare/sh:prefix and sh:namespace
		getDeclarePrefix();

		//2. owl:imports
		getImports();
		
		//3. sh:prefixes
		getPrefixes();
		
		Set<String> lKey = listPrefixes.keySet();
		
		for(String sKey : lKey) {
			List<String> namespaces = listPrefixes.get(sKey);
			
			if(namespaces.size()>1) {
				report.setErrors(1);
				report.setErrorItem(ruleDescription, reportAssertionID, null, null, sKey);
			}
		}
	}
	
	/**
	 * Get the list of sh:prefixes
	 */
	private void getPrefixes() {
		//3. sh:prefixes
        NodeIterator niSparql = getObjectsOfProperty(prefixesProperty);
        
        findPrefix(niSparql);
	}
	
	/**
	 * Get the list of owl:imports
	 */
	private void getImports() {
		//2. owl:imports
        NodeIterator niSparql = getObjectsOfProperty(importProperty);
        
        findPrefix(niSparql);
	}
	
	/**
	 * Get the prefix from a namespace
	 * @param niSparql
	 */
	private void findPrefix(NodeIterator niSparql) {
		Map<String, String> mNamespace = this.currentModel.getNsPrefixMap();
		Set<String> lKey = mNamespace.keySet();
        
        while(niSparql.hasNext()) {
        	RDFNode node = niSparql.next();
        	String sNode = node.toString();
        	
        	if(mNamespace.containsValue(sNode)) {
        		for(String sKey: lKey) {
        			if(mNamespace.get(sKey).equals(sNode)) {
        				insertPrefixNamespace(sKey, sNode);
        			}        				
        		}
        	}
        }
	}
	
	/**
	 * Get the list of prefixes and namespaces from sh:declare property.
	 * 
	 */
	private void getDeclarePrefix(){		
		//1. sh:declare/sh:prefix
        NodeIterator niSparql = getObjectsOfProperty(declareProperty);
        
        while(niSparql.hasNext()) {
        	RDFNode node = niSparql.next();
        	
        	if(node.isResource()) {
        		NodeIterator niPrefix = this.currentModel.listObjectsOfProperty(node.asResource(), this.currentModel.getProperty(prefixProperty));	    		
        		NodeIterator niNamespace = this.currentModel.listObjectsOfProperty(node.asResource(), this.currentModel.getProperty(namespaceProperty));	    		
        		
        		insertPrefixNamespace(niPrefix.toList(), niNamespace.toList());
        	}
        }
	}
	
	/**
	 * Insert the prefix and namespace to the result list.
	 * @param sPrefix
	 * @param sNamespace
	 */
	private void insertPrefixNamespace(String sPrefix, String sNamespace) {
		List<String> lNamespaces = new ArrayList<>();
		if(listPrefixes.containsKey(sPrefix)) {
			lNamespaces = listPrefixes.get(sPrefix);
		}
		
		if(!lNamespaces.contains(sNamespace)) {
			lNamespaces.add(sNamespace);
			listPrefixes.put(sPrefix, lNamespaces);	
		}					
	}
	
	/**
	 * Insert the list of prefixes and namespaces to the result list.
	 * @param listPrefix
	 * @param listNamespaces
	 */
	private void insertPrefixNamespace(List<RDFNode> listPrefix, List<RDFNode> listNamespaces) {
		for(RDFNode nPrefix: listPrefix) {
			for(RDFNode nNamespaces: listNamespaces) {
				if(nPrefix.isLiteral() && nNamespaces.isLiteral()) {
					List<String> sNamespaces = new ArrayList<>();
					if(listPrefixes.containsKey(nPrefix.asLiteral().getString())) {
						sNamespaces = listPrefixes.get(nPrefix.asLiteral().getString());
					}

					if(!sNamespaces.contains(nNamespaces.asLiteral().getString())) {
						sNamespaces.add(nNamespaces.asLiteral().getString());					
						listPrefixes.put(nPrefix.asLiteral().getString(), sNamespaces);
					}	
					
				}
			}
		}
	}
}
