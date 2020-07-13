package eu.europa.ec.itb.shacl.plugin;

import com.gitb.core.AnyContent;
import com.gitb.core.ValueEmbeddingEnumeration;
import com.gitb.vs.GetModuleDefinitionResponse;
import com.gitb.vs.ValidateRequest;
import com.gitb.vs.ValidationResponse;

import eu.europa.ec.itb.shacl.plugin.utils.PluginConstants;

public class ProvisionalMain {
	public static void main(String args[]) {
		ValidationServiceShaclPlugin service = new ValidationServiceShaclPlugin();
		
		GetModuleDefinitionResponse resp = service.getModuleDefinition(null);
		
		ValidateRequest req = new ValidateRequest();

        req.getInput().add(createPluginInputItem("C:\\Users\\mfontsan\\git\\docker-validator-shacl-plugin\\src\\main\\resources\\tmp", PluginConstants.INPUT_TEMP_FOLDER));
        req.getInput().add(createPluginInputItem("C:\\Users\\mfontsan\\Documents\\02_Projectes\\23- TestBed\\SHACL for SHACL examples\\Plugin\\Extended_SPARQL Examples.ttl", PluginConstants.INPUT_CONTENT));
        //req.getInput().add(createPluginInputItem("C:\\Users\\mfontsan\\Documents\\02_Projectes\\23- TestBed\\01- Test data\\dcat-ap_1.2_shacl\\dcat-ap.shapes.ttl", PluginConstants.INPUT_CONTENT));
		ValidationResponse vResp = service.validate(req);
		
		System.out.println(vResp.getReport());
	}
	
    private static AnyContent createPluginInputItem(String value, String name) {
        AnyContent input = new AnyContent();
        input.setName(name);
        input.setValue(value);
        input.setEmbeddingMethod(ValueEmbeddingEnumeration.STRING);
        return input;
    }
}
