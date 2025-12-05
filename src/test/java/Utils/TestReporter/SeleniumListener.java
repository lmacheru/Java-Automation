
package Functions.Utils.TestRporter;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import Functions.Utils.*;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.TreeMap;


import static Functions.Utils.TestRporter.BaseClass.extent;
import static Functions.Utils.TestRporter.BaseClass.logger;

public class SeleniumListener {

    private Map<String, TestSuite> instanceSuites = new TreeMap<>();
    private Functions.Utils.TestRporter.TestSuite activeTestSuite;
    private Functions.Utils.TestRporter.TestCase activeTestCase;
    private Functions.Utils.TestRporter.TestStep activeTestStep;


    public Map<String, TestSuite> getInstanceSuites() {
        return instanceSuites;
    }

    public void addTestSuite(final String testSuiteName, final String description) {

        logger = extent.createTest(testSuiteName, description);

        activeTestSuite = new TestSuite();
        activeTestSuite.setName(testSuiteName);
        activeTestSuite.setEnvironment("SIT");
        getInstanceSuites().put(testSuiteName, activeTestSuite);

        }

    public void addTestCase(final String testName,String GroupName) {
        activeTestCase = new TestCase();
        activeTestCase.setName(testName);

        if (logger != null && logger instanceof ExtentTest) {
            activeTestCase.setExtentRef(logger.createNode(testName, "TestNG Executed").assignCategory(GroupName));
            //activeTestCase.setExtentRef(logger = extent.createTest(testName).assignCategory(GroupName));

            activeTestCase.setID(Integer.toString(activeTestSuite.getTestcases().size() + 1));
            activeTestSuite.addTestCase(activeTestCase.getID(), activeTestCase);


        }
    }

    public void addTestCaseNode(final String testName,String Category) {
        activeTestCase = new TestCase();
        activeTestCase.setName(testName);

        if (logger != null && logger instanceof ExtentTest) {
            //activeTestCase.setExtentRef(logger = extent.createTest(testName).assignCategory(Category));
            //activeTestCase.setExtentRef(logger.createNode(testName, "TestNG Executed").assignCategory(Category));
            logger = logger.createNode(Category);

            activeTestCase.setID(Integer.toString(activeTestSuite.getTestcases().size() + 1));
            activeTestSuite.addTestCase(activeTestCase.getID(), activeTestCase);
        }
    }
    public void addInfoTestStep(final String testStepDescription) {
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());
        activeTestCase.getExtentRef().log(Status.INFO, testStepDescription);
        activeTestCase.add(activeTestStep);
        //Allure.step(testStepDescription, io.qameta.allure.model.Status.fromValue("Info"));


    }
    public void addTestStep(final String testStepDescription) {
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());
        activeTestCase.getExtentRef().log(Status.PASS, testStepDescription);
        activeTestCase.add(activeTestStep);

        //Allure.step(testStepDescription);
    }
    public void addCodeBlock(final String testStepDescription, String XMLCode) {
        // Increment the step number
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;

        // Create a new test step
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());

        // Log the description as a passing test step
        activeTestCase.getExtentRef().log(Status.INFO, testStepDescription);

        // Ensure the XML code is formatted correctly as a code block
        Markup m = MarkupHelper.createCodeBlock(XMLCode);

        // Use the log method to display the code block in the Extent Report
        activeTestCase.getExtentRef().log(Status.INFO, m);
    }
    public void WarningTestStep(final String testStepDescription) {
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());
        activeTestCase.getExtentRef().log(Status.WARNING, testStepDescription);
        activeTestCase.add(activeTestStep);
    }

    public void FailTestStep(final String testStepDescription) {
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());
        activeTestCase.getExtentRef().log(Status.FAIL, testStepDescription);
        activeTestCase.add(activeTestStep);

        //Allure.step(testStepDescription, io.qameta.allure.model.Status.FAILED);

    }

    public TestStep getActiveTestStep() {
        return activeTestStep;
    }

    public void failStep(String Msg) {
        getActiveTestStep().setAttachmentDisplayName("Step Result");
        String captureName = activeTestStep.getStepNumber() + "_" + activeTestStep.getDescription();
        activeTestCase.getExtentRef().log(Status.FAIL, Msg);
        getActiveTestStep().setTimestamp(DateHelper.getDate());
        getActiveTestStep().setStatus(Status.FAIL);
        getActiveTestStep().setMessage(Msg);

    }
    public void failStepExeption(Exception e) {
        getActiveTestStep().setAttachmentDisplayName("Step Result");
        String captureName = activeTestStep.getStepNumber() + "_" + activeTestStep.getDescription();
        activeTestCase.getExtentRef().log(Status.FAIL, e);
        getActiveTestStep().setTimestamp(DateHelper.getDate());
        getActiveTestStep().setStatus(Status.FAIL);
        getActiveTestStep().setMessage(e.getMessage());

    }

    public void SkipStep(final String testStepDescription) {
        TestStep.STEP_NUMBER = TestStep.STEP_NUMBER + 1;
        activeTestStep = new Functions.Utils.TestRporter.TestStep();
        activeTestStep.setStepNumber(TestStep.STEP_NUMBER);
        activeTestStep.setDescription(testStepDescription);
        activeTestStep.setTimestamp(DateHelper.getDate());
        activeTestCase.getExtentRef().log(Status.SKIP, "Skipping test Due to "+testStepDescription+""+System.getProperty("country"));
        activeTestCase.add(activeTestStep);
    }
    public void LogStatusStep() {
        getActiveTestStep().setAttachmentDisplayName("Step Result");
        getActiveTestStep().setTimestamp(DateHelper.getDate());
        getActiveTestStep().setStatus(Status.FAIL);
        getActiveTestStep().setMessage("Skipping test Due to Data not being Available for Country : ");

    }
    public void CompareString(String ExpectedResult, String ActualResults,String Discription) {
        getActiveTestStep().setAttachmentDisplayName("Step Result");
        try {
            if (ActualResults.equalsIgnoreCase(ExpectedResult)) {
                activeTestStep.setStatus(Status.PASS);
                activeTestStep.setTimestamp(DateHelper.getDate());

                if(Discription.contains("Account")) {
                    activeTestCase.getExtentRef().log(Status.PASS, Discription + "<br /> Expected Account Found :: " + ExpectedResult);

                }else{
                    activeTestCase.getExtentRef().log(Status.PASS, "<p style=\"background-color:MediumSeaGreen;\"><strong>"+Discription+ " Test Passed Assertion <br /> " +
                            "expected :: "+ ExpectedResult +" <br />" +
                            "Actual   :: "+ ActualResults +" </strong></p>");
                }
            }else if(ActualResults.contains(ExpectedResult)){
                activeTestStep.setStatus(Status.PASS);
                activeTestStep.setTimestamp(DateHelper.getDate());
                //activeTestCase.getExtentRef().log(Status.PASS, Discription + "<br /> Expected Status Found :: " +ExpectedResult);
                activeTestCase.getExtentRef().log(Status.PASS, "<p style=\"background-color:MediumSeaGreen;\"><strong>"+Discription+ " Test Passed Assertion <br /> " +
                        "expected :: "+ ExpectedResult +" <br />" +
                        "Actual   :: "+ ActualResults +" </strong></p>");
            }else{
                activeTestStep.setStatus(Status.FAIL);
                activeTestStep.setTimestamp(DateHelper.getDate());

                activeTestCase.getExtentRef().log(Status.FAIL, "<p style=\"background-color:Tomato;\"><strong>"+Discription+ " Test Failed Assertion <br /> " +
                        "expected :: "+ ExpectedResult +" <br />" +
                        "Actual   :: "+ ActualResults +" </strong></p>");
            }
        } catch (Exception e) {
            //failStepExeption(e);
            e.printStackTrace();
        }

    }
    public void PostingAssertion(String Discription,String ExpectedNarrative, String ExpectedNarrativeRef3,String ActualNarrative, String ActualNarrativeRef3) {
        getActiveTestStep().setAttachmentDisplayName("Step Result");

        if(!ExpectedNarrativeRef3.isEmpty()) {
            if (ExpectedNarrative.equalsIgnoreCase(ActualNarrative) && ExpectedNarrativeRef3.equalsIgnoreCase(ActualNarrativeRef3)) {
                activeTestStep.setStatus(Status.PASS);
                activeTestStep.setTimestamp(DateHelper.getDate());
                activeTestCase.getExtentRef().log(Status.PASS, "<p style=\"background-color:MediumSeaGreen;\">"+Discription + " Narratives Assertion " +
                        "<br /> Found Expected Narratives :: " + ExpectedNarrative +
                        "<br /> Found Expected Extended_NarrativesRef3 :: " + ExpectedNarrativeRef3+" </p>");
            } else {
                activeTestStep.setStatus(Status.FAIL);
                activeTestStep.setTimestamp(DateHelper.getDate());
                activeTestCase.getExtentRef().log(Status.FAIL, "<p style=\"background-color:Tomato;\"><strong>"+Discription + " Narratives Failed Assertion " +
                        "<br /> Expected Narratives :: " + ExpectedNarrative +
                        "<br /> Expected Extended_NarrativesRef3 :: " + ExpectedNarrativeRef3 +
                        "<br /> Actual Narratives :: " + ActualNarrative +
                        "<br /> Actual Extended_NarrativesRef3 :: " + ActualNarrativeRef3 +" </p>");
            }
        }else{
            if (ExpectedNarrative.equalsIgnoreCase(ActualNarrative)) {
                activeTestStep.setStatus(Status.PASS);
                activeTestStep.setTimestamp(DateHelper.getDate());
                activeTestCase.getExtentRef().log(Status.PASS, Discription + " Narratives Assertion " +
                        "<br /> Found Expected "+Discription+" :: " + ExpectedNarrative );
            } else {
                activeTestStep.setStatus(Status.FAIL);
                activeTestStep.setTimestamp(DateHelper.getDate());
                activeTestCase.getExtentRef().log(Status.FAIL, Discription + " Narratives Failed Assertion " +
                        "<br /> Expected "+Discription+" :: " + ExpectedNarrative +
                        "<br /> Actual "+Discription+" :: " + ActualNarrative);
            }
        }
    }
    public void PostingAssertion(String Discription,String ExpectedNarrative,String ActualNarrative, String FieldName) {
        getActiveTestStep().setAttachmentDisplayName("Step Result");


        if (ExpectedNarrative.equalsIgnoreCase(ActualNarrative)) {
            activeTestStep.setStatus(Status.PASS);
            activeTestStep.setTimestamp(DateHelper.getDate());
            activeTestCase.getExtentRef().log(Status.PASS, Discription + " Assertion " +
                    "<br /> Found Expected <mark>"+FieldName+"</mark> :: " + ExpectedNarrative );
        } else {
            activeTestStep.setStatus(Status.FAIL);
            activeTestStep.setTimestamp(DateHelper.getDate());
            activeTestCase.getExtentRef().log(Status.FAIL, Discription + " Failed Assertion " +
                    "<br /> Expected "+FieldName+" :: " + ExpectedNarrative +
                    "<br /> Actual "+FieldName+" :: " + ActualNarrative);
        }

    }
   /* public void ErrorCaseNumber() {
        getActiveTestStep().setAttachmentDisplayName("Step Result");
        String captureName = activeTestStep.getStepNumber() + "_" + activeTestStep.getDescription();
        activeTestCase.getExtentRef().log(Status.FAIL, "Failing test Due to Case Number not found  for Country : "+System.getProperty("country"), MediaEntityBuilder.createScreenCaptureFromPath(storeImage(captureName)).build());
        getActiveTestStep().setTimestamp(DateHelper.getDate());
        getActiveTestStep().setStatus(Status.FAIL);
        getActiveTestStep().setMessage("Failing test Due to Case Number not found  for Country  : "+System.getProperty("country"));

    }*/




}
