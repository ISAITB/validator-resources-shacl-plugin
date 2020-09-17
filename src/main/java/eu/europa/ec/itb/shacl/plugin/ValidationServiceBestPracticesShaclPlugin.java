package eu.europa.ec.itb.shacl.plugin;


import com.gitb.vs.ValidationService;

/**
 * Main class to execute all validations.
 * @author mfontsan
 *
 */
public class ValidationServiceBestPracticesShaclPlugin extends ValidationServiceShaclPlugin implements ValidationService{
    
	public ValidationServiceBestPracticesShaclPlugin() {
		super();
		setPackageName(packageBPName);
		setPackageName(packagAFeName);
	}
}
