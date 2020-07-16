package eu.europa.ec.itb.shacl.plugin.utils;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;

import eu.europa.ec.itb.shacl.plugin.Report;

public abstract class JenaModelUtils {
	protected Model currentModel;
	protected Report report;
	
	protected static String shaclNamespace = "http://www.w3.org/ns/shacl#";
	

	public JenaModelUtils(Model currentModel, Report report) {
		this.currentModel = currentModel;
		this.report = report;
	}
	
	public NodeIterator getObjectsOfProperty(String property) {
		Property p = this.currentModel.getProperty(property);
		this.currentModel.listSubjectsWithProperty(p);
		
        return this.currentModel.listObjectsOfProperty(p);
	}
	
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
