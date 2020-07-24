package eu.europa.ec.itb.shacl.plugin.utils;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;

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
	

	public JenaModelUtils(Model currentModel, Report report) {
		this.currentModel = currentModel;
		this.report = report;
	}
	
	public NodeIterator getObjectsOfProperty(String property) {
		Property p = this.currentModel.getProperty(property);
		
        return this.currentModel.listObjectsOfProperty(p);
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
	
	public Report getReport() {
		return this.report;
	}
	
	public void setReport(Report report) {
		this.report = report;
	}

}
