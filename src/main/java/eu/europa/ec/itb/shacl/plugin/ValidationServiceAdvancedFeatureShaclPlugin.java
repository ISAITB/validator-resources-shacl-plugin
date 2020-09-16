package eu.europa.ec.itb.shacl.plugin;


import com.gitb.vs.ValidationService;

/**
 * Main class to execute all validations.
 * @author mfontsan
 *
 */
public class ValidationServiceAdvancedFeatureShaclPlugin extends ValidationServiceShaclPlugin implements ValidationService{
    
	public ValidationServiceAdvancedFeatureShaclPlugin() {
		super();
		setPackageName(packagAFeName);
	}
}
