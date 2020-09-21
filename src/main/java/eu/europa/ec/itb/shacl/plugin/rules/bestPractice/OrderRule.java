package eu.europa.ec.itb.shacl.plugin.rules.bestPractice;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.jena.rdf.model.Model;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.europa.ec.itb.shacl.plugin.Report;
import eu.europa.ec.itb.shacl.plugin.Rules;
import eu.europa.ec.itb.shacl.plugin.utils.JenaModelUtils;

/**
 * Rule ID: lists
 * Rule Definition: https://www.w3.org/TR/shacl/#order
 * Rule Description: If present at property shapes, the recommended use of sh:order is to sort the property shapes in an ascending order, for example so that properties with smaller order are placed above (or to the left) of properties with larger order.
 * @author mfontsan
 *
 */
public class OrderRule extends JenaModelUtils  implements Rules {
	private File contentFile;

	private static String reportAssertionID = shaclNamespace + "order";
	private String ruleDescription = "If present at property shapes, the recommended use of sh:order is to sort the property shapes in an ascending order, for example so that properties with smaller order are placed above (or to the left) of properties with larger order.";
	

	public OrderRule(Model currentModel, Report report, File fileContent) {
		super(currentModel, report);
		
		this.contentFile = fileContent;
	}

	public void validateRule() {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(contentFile);
			
			XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            
            XPathExpression expr = xpath.compile("/RDF/NodeShape");
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < nodes.getLength(); i++) {
            		Node currentNode = nodes.item(i);
    	            XPathExpression exprOrder = xpath.compile("(property | property/PropertyShape)/order");
    	            NodeList nodesOrder = (NodeList) exprOrder.evaluate(currentNode, XPathConstants.NODESET);
            		
                    if(!isOrdered(nodesOrder)) {
	    	            XPathExpression exprAbout = xpath.compile("@about");
	    	            NodeList nodesAbout = (NodeList) exprAbout.evaluate(currentNode, XPathConstants.NODESET);
	    	            
	    	            if(nodesAbout.getLength()>0) {
	                    	report.setWarningItem(ruleDescription, reportAssertionID, nodesAbout.item(0).getTextContent(), null, nodesAbout.item(0).getTextContent());
	    	            }else {
	    	            	report.setWarningItem(ruleDescription, reportAssertionID, reportAssertionID, null, reportAssertionID);
		    	        }
                    }
            }
			
		} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
			
		}
		
	}
	
	private boolean isOrdered(NodeList nodelist) {
		boolean ordered = true;

        int order = -1;
        boolean ascendent = true;
        int j=0;
        if(nodelist.getLength()>1) {
        	try {
				int first = Integer.parseInt(nodelist.item(0).getTextContent());
				int last = Integer.parseInt(nodelist.item(nodelist.getLength()-1).getTextContent());
				
				if(first>last) {
					ascendent = false;
					order = first+1;
				}
        	}catch(Exception e) {
        		ordered = false;
        	}
        }
        while(j<nodelist.getLength() && ordered){
			Node orderNode = nodelist.item(j);

			int currentOrder = Integer.parseInt(orderNode.getTextContent());
        	ordered = false;
        	
            if(ascendent && currentOrder>order) {
            	order = currentOrder;
            	ordered = true;
            }
        	if(!ascendent && currentOrder<order) {
            	order = currentOrder;
            	ordered = true;
            }
            
            j++;
		}
		
		return ordered;
	}
}
