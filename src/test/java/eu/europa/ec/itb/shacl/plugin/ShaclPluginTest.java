package eu.europa.ec.itb.shacl.plugin;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.apache.jena.atlas.logging.Log;
import org.junit.jupiter.api.Test;

import com.gitb.core.AnyContent;
import com.gitb.core.ValueEmbeddingEnumeration;
import com.gitb.tr.TAR;
import com.gitb.tr.TestResultType;
import com.gitb.vs.GetModuleDefinitionResponse;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;

import eu.europa.ec.itb.shacl.plugin.utils.PluginConstants;

class ShaclPluginTest {
	
	@Test
	void getModulePlugin() {
		ValidationServiceShaclPlugin service = new ValidationServiceShaclPlugin();
		
		GetModuleDefinitionResponse resp = service.getModuleDefinition(null);
		
		assertNotNull(resp, "Modul definition not retreived.");
		assertNotNull(resp.getModule().getId(), "Module identifier not retreived.");		
	}
	
	
	@Test
	void initPlugin() {
		String inputFile = "Extended_SPARQL Examples.ttl";
		
		ValidationServiceShaclPlugin service = new ValidationServiceShaclPlugin();
		
		ValidateRequest req = new ValidateRequest();
		
		ClassLoader classLoader = getClass().getClassLoader();
			

        req.getInput().add(createPluginInputItem("C:\\Users\\mfontsan\\git\\docker-validator-shacl-plugin\\src\\main\\resources\\tmp", PluginConstants.INPUT_TEMP_FOLDER));
        req.getInput().add(createPluginInputItem("C:\\Users\\mfontsan\\Documents\\02_Projectes\\23- TestBed\\SHACL for SHACL examples\\Plugin\\Extended_SPARQL Examples.ttl", PluginConstants.INPUT_CONTENT));
        
        ValidationResponse vResp = service.validate(req);
        TAR report = vResp.getReport();

		assertNotNull(vResp, "Validation failed.");
		assertNotNull(report, "Report not returned.");
		assertTrue(Arrays.asList(TestResultType.values()).contains(report.getResult()), "Result type incorrect.");
		System.out.println("Result: " + report.getResult());
		assertNotNull(report.getReports());
		assertNotNull(report.getCounters());
		System.out.println("Counters errors: " + report.getCounters().getNrOfErrors());
		System.out.println("Counters warnings: " + report.getCounters().getNrOfWarnings());
		
	}
	
    private static AnyContent createPluginInputItem(String value, String name) {
        AnyContent input = new AnyContent();
        input.setName(name);
        input.setValue(value);
        input.setEmbeddingMethod(ValueEmbeddingEnumeration.STRING);
        return input;
    }

}
