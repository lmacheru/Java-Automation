package Functions.Utils.TestRporter;

import com.aventstack.extentreports.Status;
import Functions.Utils.DateHelper;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class TestSuite {

    private String name;
    private Map<String, TestCase> testCases = new TreeMap<>();
    private int totalTestCases;
    private int totalTestCasesPassed;
    private int totalTestCasesFailed;
    private String environment;
    private String testRailID;
    private String testRailProjectID;
    private String action;
    private String description;
    private String projectName;

    public TestSuite() {
        testCases = new TreeMap<>();
    }

    public TestSuite(String name, Map<String, TestCase> testCases) {
        this.name = name;
        this.testCases = new TreeMap<>();
        this.testCases.putAll(testCases);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutionStartTime() throws ParseException {

        Date minDate = new Date();
        Date currentDate;
        TestCase tc;
        for (String key : testCases.keySet()) {
            tc = testCases.get(key);
            currentDate = DateHelper.toDate(tc.getExecutionStartTime());

            if (currentDate.before(minDate)) {
                minDate = currentDate;
            }
        }
        return DateHelper.toString(minDate);
    }

    public String getExecutionEndTime() throws ParseException {
        Date maxDate = DateHelper.toDate("05-Jul-2000 17:17:05");
        Date currentDate;

        for (String key : testCases.keySet()) {
            currentDate = DateHelper.toDate(testCases.get(key).getExecutionEndTime());

            if (currentDate.after(maxDate)) {
                maxDate = currentDate;
            }
        }
        return DateHelper.toString(maxDate);
    }

    public Status getStatus() {

        for (String s : testCases.keySet()) {
            if (testCases.get(s).getStatus() == Status.FAIL) {
                return Status.FAIL;
            }
        }
        return Status.PASS;
    }

    public Map<String, TestCase> getTestcases() {
        return testCases;
    }

    public void addTestCase(String TC_ID, TestCase testCase) {
        testCases.put(TC_ID, testCase);
    }

    public TestCase getTestcase(String name) {
        return testCases.get(name);
    }

    public TestCase getTestcaseByID(int ID) {
    	Set<String> keys = testCases.keySet();
    	TestCase returnCase = null;
    	for (String key : keys) {
    		TestCase ts = testCases.get(key);
    		if (Integer.parseInt(ts.getID()) == ID )
    		{
    			returnCase = ts;
    			break;
    		}
    	}
    	return returnCase;
    }
    

    public void setTestSteps(Map<String, TestCase> testSteps) {
        this.testCases = testSteps;
    }

    @Override
    public String toString() {
        try {
            return "TestSuite {" + "name=" + name + ", executionStartTime=" + getExecutionStartTime() + ", executionEndTime=" + getExecutionEndTime() + ", status=" + getStatus() + ", testCases=" + testCases + "}";
        } catch (ParseException ex) {
            Logger.getLogger(TestSuite.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void calculateTestCaseStats() {
        totalTestCases = testCases.size();
        totalTestCasesPassed = 0;

        for (String s : testCases.keySet()) {
            TestCase tc = testCases.get(s);

            if (tc.getStatus() == Status.PASS) {
                totalTestCasesPassed++;
            }
        }

        totalTestCasesFailed = totalTestCases - totalTestCasesPassed;
    }

    public int getTotalTestCases() {
        return totalTestCases;
    }

    public int getTotalTestCasesPassed() {
        return totalTestCasesPassed;
    }

    public int getTotalTestCasesFailed() {
        return totalTestCasesFailed;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

	public String getTestRailID() {
		return testRailID;
	}

	public void setTestRailID(String testRailID) {
		this.testRailID = testRailID;
	}

	public String getTestRailProjectID() {
		return testRailProjectID;
	}

	public void setTestRailProjectID(String testRailProjectID) {
		this.testRailProjectID = testRailProjectID;
	}

	public String getAction() {
		if (this.action == null || "".equalsIgnoreCase(this.action))
			this.action = "";
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
}
