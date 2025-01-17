package eu.europa.ec.itb.shacl.plugin;

public interface Rules {
	
	void validateRule();
	Report getReport();
	void setReport(Report report);

}
