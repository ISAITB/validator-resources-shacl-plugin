package eu.europa.ec.itb.shacl.plugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;

import com.gitb.core.AnyContent;
import com.gitb.core.ValidationModule;
import com.gitb.vs.GetModuleDefinitionResponse;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.vs.ValidationService;
import com.gitb.vs.Void;

import eu.europa.ec.itb.shacl.plugin.error.PluginException;
import eu.europa.ec.itb.shacl.plugin.utils.PluginConstants;

public class ValidationServiceShaclPlugin implements ValidationService{
    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceShaclPlugin.class);
    
	public ValidationServiceShaclPlugin() {
		super();
	}

	public GetModuleDefinitionResponse getModuleDefinition(Void parameters) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        
        response.setModule(new ValidationModule());
        response.getModule().setId("SHACL shapes Plugin");
   
        return response;
	}

	public ValidationResponse validate(ValidateRequest validateRequest) {
        LOG.info("Starting plugin validation");
		File tmpFolder = validateTempFolder(validateRequest);
		File contentToValidate = validateContentToValidate(validateRequest);
        Report report = new Report();
		
		Model modelContent = getModel(contentToValidate);
    	    	
        PathPositionPlugin pathRule = new PathPositionPlugin(modelContent);
        report = pathRule.validatePathPositionRule(report);
		
        ValidationResponse response = new ValidationResponse();
        
        response.setReport(report.generateReport());
        
        LOG.info("Ending plugin validation");
		
		return response;
	}
	
	private Model getModel(File contentToValidate) {
		Lang lang = RDFLanguages.contentTypeToLang(RDFLanguages.guessContentType(contentToValidate.getName()));
		
        try (InputStream dataStream = new FileInputStream(contentToValidate)) {       	
        	//Read the model
            Model fileModel = JenaUtil.createMemoryModel();
            fileModel.read(dataStream, null, lang.getName()); 
            
            return fileModel;         
        } catch (IOException e) {
            throw new PluginException("An error occurred while reading a SHACL file.", e);
        }        
	}

	private File validateTempFolder(ValidateRequest validateRequest) {
        String sTempFolderPath = getInputFor(validateRequest, PluginConstants.INPUT_TEMP_FOLDER);
		
        if(!StringUtils.isEmpty(sTempFolderPath)) {        	
        	Path pTempFolder = Paths.get(sTempFolderPath);        	
        	pTempFolder.toFile().mkdirs();
    		
        	return pTempFolder.toFile();
        } else {
        	throw new PluginException(String.format("No content was provided for validation (input parameter [%s]).", PluginConstants.INPUT_TEMP_FOLDER));
        }
	}
	
    /**
     * Validation of the content.
     * @param validateRequest The request's parameters.
     * @param explicitEmbeddingMethod
	 * @param contentSyntax
	 * @return The file to validate.
     */
    private File validateContentToValidate(ValidateRequest validateRequest) {
        String contentToValidatePath = getInputFor(validateRequest, PluginConstants.INPUT_CONTENT);
        
        if(!StringUtils.isEmpty(contentToValidatePath)) { 
        	try{
        		return new File(contentToValidatePath);
        	}catch(Exception e) {
        		throw new PluginException(String.format("No valid content (input parameter [%s]).", PluginConstants.INPUT_CONTENT));        		
        	}
        	
        } else {
        	throw new PluginException(String.format("No content was provided for validation (input parameter [%s]).", PluginConstants.INPUT_CONTENT));
        }
    }

    /**
     * Lookup a provided input from the received request parameters.
     *
     * @param validateRequest The request's parameters.
     * @param name The name of the input to lookup.
     * @return The inputs found to match the parameter name (not null).
     */
    private String getInputFor(ValidateRequest validateRequest, String name) {
    	String input = null;
        if (validateRequest != null && validateRequest.getInput() != null) {
            for (AnyContent anInput: validateRequest.getInput()) {
                if (name.equals(anInput.getName())) {
                    input = anInput.getValue();
                }
            }
        }
        return input;
    }
}
