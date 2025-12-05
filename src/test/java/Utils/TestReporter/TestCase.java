package Functions.Utils.TestRporter;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;


@XmlAccessorType(XmlAccessType.FIELD)
public class TestCase {

    private String ID = "" ;
    private String name = "";
    private ArrayList<Functions.Utils.TestRporter.TestStep> testSteps = new ArrayList<>();
    private String testRailID;
    private ExtentTest extentRef;
    public TestCase() {
    }

    public TestCase(String ID, String name, ArrayList<Functions.Utils.TestRporter.TestStep> testSteps) {
        this.ID = ID;
        this.name = name;
        this.testSteps = new ArrayList<>();
        this.testSteps.addAll(testSteps);
    }

    public TestCase(String ID, String name, Functions.Utils.TestRporter.TestStep testStep) {
        this.ID = ID;
        this.name = name;
        this.testSteps = new ArrayList<>();
        this.testSteps.add(testStep);
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Functions.Utils.TestRporter.TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(ArrayList<Functions.Utils.TestRporter.TestStep> testSteps) {
        this.testSteps = testSteps;
    }

    public Status getStatus() {

        for (Functions.Utils.TestRporter.TestStep ts : testSteps) {
            if (ts.getStatus() == Status.FAIL  || ts.getStatus() == Status.SKIP) {
                return Status.FAIL;
            }
        }
        return Status.PASS;
    }

    public String getExecutionStartTime() {
        return testSteps.get(0).getTimestamp();
    }

    public String getExecutionEndTime() {
        return testSteps.get(testSteps.size() - 1).getTimestamp();
    }

    public void add(TestStep testStep) {
        testSteps.add(testStep);
    }

    @Override
    public String toString() {
        return "TestCase{" + "ID=" + ID + ", name=" + name
                + ", testSteps=" + testSteps + ", status=" + getStatus()
                + ", executionStartTime=" + getExecutionStartTime()
                + ", executionEndTime=" + getExecutionEndTime() + '}';
    }

	public String getTestRailID() {
		return testRailID;
	}

	public void setTestRailID(String testRailID) {
		this.testRailID = testRailID;
	}

	public ExtentTest getExtentRef() {
		return extentRef;
	}

	public void setExtentRef(ExtentTest extentRef) {
		this.extentRef = extentRef;
	}
}

