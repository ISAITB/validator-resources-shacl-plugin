package eu.europa.ec.itb.shacl.plugin;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.gitb.tr.BAR;
import com.gitb.tr.ObjectFactory;
import com.gitb.tr.TAR;
import com.gitb.tr.TestAssertionGroupReportsType;
import com.gitb.tr.TestAssertionReportType;
import com.gitb.tr.TestResultType;
import com.gitb.tr.ValidationCounters;

/**
 * 
 * @author mfontsan 21/07/2020
 *
 */
public class Report {
    private static final com.gitb.tr.ObjectFactory objectFactory = new ObjectFactory();
    
	List<JAXBElement<TestAssertionReportType>> items;
	int assertions = 0;
	int errors = 0;
	int warnings = 0;
	

	public Report() {
		this.items = new ArrayList<>();
	}
	
	/**
	 * Generate TAR report from the results.
	 * @return
	 * 		returns com.gitb.tr.TAR
	 */
	public TAR generateReport() {
		TAR report = new TAR();
        report.setResult(getResult());
        report.setCounters(new ValidationCounters());
        report.getCounters().setNrOfAssertions(BigInteger.valueOf(assertions));
        report.getCounters().setNrOfErrors(BigInteger.valueOf(errors));
        report.getCounters().setNrOfWarnings(BigInteger.valueOf(warnings));
        report.setReports(new TestAssertionGroupReportsType());
        report.getReports().getInfoOrWarningOrError().addAll(items);
        
        return report;
     }
	
	/**
	 * Get the result of the report.
	 * @return
	 * 		returns com.gitb.tr.TestResultType
	 */
	private TestResultType getResult(){
		if(errors>0) {
			return TestResultType.FAILURE;
		}
		if(warnings>0) {
			return TestResultType.WARNING;
		}
		
		return TestResultType.SUCCESS;
	}
	
	public void addError() {
		this.errors++;
	}
	
	public void addWarning() {
		this.warnings++;
	}
	
	public void addAssertion() {
		this.assertions++;
	}

	private BAR setItem(String description, String assertionId, String location, String test, String value) {
        BAR item = new BAR();
        
        item.setDescription(description);
        item.setAssertionID(assertionId);
        item.setLocation(location);
        item.setTest(test);
        item.setValue(value);
        
        return item;
    }


	public void setErrorItem(String description, String assertionId, String location, String test, String value) {
        BAR item = setItem(description, assertionId, location, test, value);
        
        items.add(objectFactory.createTestAssertionGroupReportsTypeError(item));
        addError();
    }


	public void setInfoItem(String description, String assertionId, String location, String test, String value) {
        BAR item = setItem(description, assertionId, location, test, value);
        
        items.add(objectFactory.createTestAssertionGroupReportsTypeInfo(item));
        addAssertion();
    }


	public void setWarningItem(String description, String assertionId, String location, String test, String value) {
        BAR item = setItem(description, assertionId, location, test, value);
        
        items.add(objectFactory.createTestAssertionGroupReportsTypeWarning(item));
        addWarning();
    }
}
