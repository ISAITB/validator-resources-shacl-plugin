package eu.europa.ec.itb.shacl.plugin;

public interface Rules {
	
	public void validateRule();
	
	public Report getReport();
	
	public void setReport(Report report);
}
