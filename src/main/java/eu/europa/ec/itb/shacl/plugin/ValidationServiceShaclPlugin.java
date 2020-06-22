package eu.europa.ec.itb.shacl.plugin;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.gitb.core.AnyContent;
import com.gitb.core.Metadata;
import com.gitb.core.ValidationModule;
import com.gitb.core.ValueEmbeddingEnumeration;
import com.gitb.vs.GetModuleDefinitionResponse;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;
import com.gitb.vs.ValidationService;
import com.gitb.vs.Void;

import eu.europa.ec.itb.shacl.plugin.error.PluginException;
import eu.europa.ec.itb.shacl.plugin.utils.PluginConstants;

public class ValidationServiceShaclPlugin implements ValidationService{

	public ValidationServiceShaclPlugin() {
		super();
	}

	public GetModuleDefinitionResponse getModuleDefinition(Void parameters) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        String id = "SHACL shapes Plugin";
        
        response.setModule(new ValidationModule());
        response.getModule().setId(id);
        response.getModule().setOperation("V");
        response.getModule().setMetadata(new Metadata());
        response.getModule().getMetadata().setName(id);
        response.getModule().getMetadata().setVersion("1.0.0");
   
        return response;
	}

	public ValidationResponse validate(ValidateRequest validateRequest) {
		File tmpFolder = validateTempFolder(validateRequest);
		File contentSyntax = validateContentToValidate(validateRequest);
		
		
		
		ValidationResponse result = new ValidationResponse();
		
		return result;
	}
	
	private File validateTempFolder(ValidateRequest validateRequest) {
        String sTempFolderPath = getInputFor(validateRequest, PluginConstants.INPUT_TEMP_FOLDER);
		
        if(!StringUtils.isEmpty(sTempFolderPath)) {        	
        	Path pTempFolder = Paths.get(sTempFolderPath);        	
        	pTempFolder.toFile().getParentFile().mkdirs();
    		
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
