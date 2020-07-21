package eu.europa.ec.itb.shacl.plugin;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

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

/**
 * Main class to execute all validations.
 * @author mfontsan
 *
 */
public class ValidationServiceShaclPlugin implements ValidationService{
    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceShaclPlugin.class);
	private static final String packageName = "eu/europa/ec/itb/shacl/plugin/rules";
    
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
        //Validate input data.
		File tmpFolder = validateTempFolder(validateRequest);
		File contentToValidate = validateContentToValidate(validateRequest);
        
		ValidationResponse response = new ValidationResponse();		
		Model modelContent = getModel(contentToValidate);
    	
		Report report = executeRuleValidations(modelContent);	
		       
        response.setReport(report.generateReport());
        
        LOG.info("Ending plugin validation");
		
		return response;
	}
	
	/**
	 * Get all classes from package "eu/europa/ec/itb/shacl/plugin/rules" and execute the validation.
	 * @param modelContent
	 * @return
	 * 		returns eu.europa.ec.itb.shacl.plugin.Report
	 */
	private Report executeRuleValidations(Model modelContent) {
		Report report = new Report();
		
		try {
			URI uriClasses = this.getClass().getClassLoader().getResource(packageName).toURI();
			
			if(uriClasses.getScheme().contains("jar")){
				URI jarLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
				Path jarFile = new File(jarLocation.getPath()).toPath();
				
				FileSystem fs = FileSystems.newFileSystem(jarFile, null);
		        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(packageName));
		        
		        Iterator<Path> it = directoryStream.iterator();
		        
		        while(it.hasNext()){
		        	Path path = it.next();		        	
		        	String className = path.toString().replace("/", ".").replace(".class", "");
		        	
		        	if(!className.contains("$1")) {
			        	if(className.startsWith(".")) {
			        		className = className.substring(1, className.length());
			        		
			        		report = executeClass(className, modelContent, report);
			        	}
		        	}
				}
			}else {
				File packageFile = new File(uriClasses.toURL().getFile());
				
				for(File f : packageFile.listFiles()) {
					String className = f.getName();
					
					if(!className.contains("$1")) {
						String classPackageName = packageName.replace("/", ".") + "." + className.replace(".class", "");
						
		        		report = executeClass(classPackageName, modelContent, report);
					}
				}
			}
			
		} catch (SecurityException | IllegalArgumentException | URISyntaxException | IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new PluginException("An error occurred while executing the validation.", e);
        } 
		
		return report;
	}
	
	/**
	 * Get the constructor from the class and executes validateRule method.
	 * @param className
	 * @param modelContent
	 * @param report
	 * @return
	 * 		returns eu.europa.ec.itb.shacl.plugin.Report
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private Report executeClass(String className, Model modelContent, Report report) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {    	
		Class<?> classe = Class.forName(className);
		Constructor<?> constructor = classe.getConstructor(Model.class, Report.class);
		Rules rule = (Rules)constructor.newInstance(modelContent, report);
		
		rule.validateRule();
		
		return rule.getReport();
	}
		
	/**
	 * Read the input file and returns the model.
	 * @param contentToValidate
	 * @return
	 * 		returns org.apache.jena.rdf.model.Model
	 */
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

	/**
	 * Validate that the temp folder exists.
	 * @param validateRequest
	 * @return
	 * 		returns java.io.File
	 */
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
     * Validate that the input file with the content exists and has valid content.
     * @param validateRequest
     * @return
	 * 		returns java.io.File
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
