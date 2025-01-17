package eu.europa.ec.itb.shacl.plugin;


import com.gitb.core.AnyContent;
import com.gitb.core.ValidationModule;
import com.gitb.vs.Void;
import com.gitb.vs.*;
import eu.europa.ec.itb.shacl.plugin.error.PluginException;
import eu.europa.ec.itb.shacl.plugin.utils.PluginConstants;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Main class to execute all validations.
 * @author mfontsan
 *
 */
public abstract class ValidationServiceShaclPlugin implements ValidationService{

    private static final Logger LOG = LoggerFactory.getLogger(ValidationServiceShaclPlugin.class);
	protected static final String packageBPName = "eu/europa/ec/itb/shacl/plugin/rules/bestPractice";
	protected static final String packagAFeName = "eu/europa/ec/itb/shacl/plugin/rules/advancedFeature";
	private final ArrayList<String> packageName;

	public ValidationServiceShaclPlugin() {
		super();
		packageName = new ArrayList<>();
	}

	public GetModuleDefinitionResponse getModuleDefinition(Void parameters) {
        GetModuleDefinitionResponse response = new GetModuleDefinitionResponse();
        
        response.setModule(new ValidationModule());
        response.getModule().setId("SHACL shapes Plugin");
   
        return response;
	}

	public ValidationResponse validate(ValidateRequest validateRequest) {
        LOG.info("Starting plugin validation");
        // Validate input data.
		validateTempFolder(validateRequest);
		File contentToValidate = validateContentToValidate(validateRequest);
		ValidationResponse response = new ValidationResponse();
		Model modelContent = getModel(contentToValidate);
		Report report = executeRuleValidations(modelContent, contentToValidate);
        response.setReport(report.generateReport());
        LOG.info("Ending plugin validation");
		return response;
	}
	
	public void setPackageName(String name) {
		packageName.add(name);
	}
	
	/**
	 * Get all classes from package "eu/europa/ec/itb/shacl/plugin/rules" and execute the validation.
	 * @param modelContent
	 * @param contentToValidate 
	 * @return
	 * 		returns eu.europa.ec.itb.shacl.plugin.Report
	 */
	private Report executeRuleValidations(Model modelContent, File contentToValidate) {
		Report report = new Report();
		try {
			for (String resourceName: packageName) {
				URL resource = this.getClass().getClassLoader().getResource(resourceName);
				if (resource != null) {
					URI uriClasses = resource.toURI();
					if (uriClasses.getScheme().contains("jar")){
						URI jarLocation = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI();
						Path jarFile = new File(jarLocation.getPath()).toPath();
						try (FileSystem fs = FileSystems.newFileSystem(jarFile, (ClassLoader) null)) {
							try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(fs.getPath(resourceName))) {
								for (Path path: directoryStream) {
									String className = path.toString().replace("/", ".").replace(".class", "");
									if (!className.contains("$1")) {
										if (className.startsWith(".")) {
											className = className.substring(1);
											report = executeClass(className, modelContent, report, contentToValidate);
										}
									}
								}
							}
						}

					} else {
						File packageFile = new File(uriClasses.toURL().getFile());
						File[] files = packageFile.listFiles();
						if (files != null) {
							for (File f : files) {
								String className = f.getName();
								if(!className.contains("$1")) {
									String classPackageName = resourceName.replace("/", ".") + "." + className.replace(".class", "");
									report = executeClass(classPackageName, modelContent, report, contentToValidate);
								}
							}
						}
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
	private Report executeClass(String className, Model modelContent, Report report, File contentToValidate) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {    	
		Class<?> clazz = Class.forName(className);
		Constructor<?> constructor = clazz.getConstructor(Model.class, Report.class, File.class);
		Rules rule = (Rules)constructor.newInstance(modelContent, report, contentToValidate);
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
        	// Read the model
            Model fileModel = ModelFactory.createModelForGraph(GraphMemFactory.createDefaultGraph());
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
        if (sTempFolderPath != null && !sTempFolderPath.isEmpty()) {
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
        if (contentToValidatePath != null && !contentToValidatePath.isEmpty()) {
        	try {
        		return new File(contentToValidatePath);
        	} catch(Exception e) {
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
