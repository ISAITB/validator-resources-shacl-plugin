package eu.europa.ec.itb.shacl.plugin.utils;

import java.util.List;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import eu.europa.ec.itb.shacl.plugin.Report;

/**
 * 
 * @author mfontsan on 21/07/2020
 *
 */
public abstract class JenaModelUtils {
	protected Model currentModel;
	protected Report report;
	
	protected static String shaclNamespace = "http://www.w3.org/ns/shacl#";
	protected static String owlNamespace = "http://www.w3.org/2002/07/owl#";
	protected static String rdfNamespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	

	public JenaModelUtils(Model currentModel, Report report) {
		this.currentModel = currentModel;
		this.report = report;
	}
	
	public NodeIterator getObjectsOfProperty(String property) {
		Property p = getProperty(property);
		
        return this.currentModel.listObjectsOfProperty(p);
	}
	
	public Property getProperty(String propertyName) {
		return this.currentModel.getProperty(propertyName);
	}
	
	/**
	 * Transform String to Query
	 * @param sQuery
	 * @return
	 * 		returns org.apache.jena.query.Query
	 */
	public Query getQuery(String sQuery) {
		try {
			ParameterizedSparqlString pss = new ParameterizedSparqlString();
			pss.setCommandText(sQuery);
			pss.setNsPrefixes(this.currentModel.getNsPrefixMap());
			
			return pss.asQuery();
			
		}catch(QueryException e) {
			return null;
		}		
	}
	
	/**
	 * Get shape of a resource
	 * @param resource
	 * @return
	 * 		returns org.apache.jena.rdf.model.Resource
	 */
	public Resource getMainShape(Resource resource) {
		RDFNode resourceRDFNode = this.currentModel.asRDFNode(resource.asNode());
		List<Resource> listSubjects = this.currentModel.listSubjectsWithProperty(null, resourceRDFNode).toList();
		
		if(!listSubjects.isEmpty()) {
			return getMainShape(listSubjects.get(0));
		}
		
		return resource;
	}
	
	public Report getReport() {
		return this.report;
	}
	
	public void setReport(Report report) {
		this.report = report;
	}

}
