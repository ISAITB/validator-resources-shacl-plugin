package eu.europa.ec.itb.shacl.plugin.utils;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;

public abstract class JenaModelUtils {
	protected Model currentModel;
	
	protected static String shaclNamespace = "http://www.w3.org/ns/shacl#";
	

	public JenaModelUtils(Model currentModel) {
		this.currentModel = currentModel;
	}
	
	public NodeIterator getObjectsOfProperty(String property) {
		Property p = this.currentModel.getProperty(property);
		
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

}
