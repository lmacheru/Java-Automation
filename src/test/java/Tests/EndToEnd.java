package Tests;

import Functions.MQDBConnections.DBConstants;
import Functions.MQDBConnections.MQConnections;
import Functions.MQDBConnections.dbConnections;
import Functions.Utils.*;
import Functions.Utils.TestRporter.BaseClass;
//import org.junit.Test;
import org.apache.poi.ss.usermodel.Row;
import org.testng.annotations.Test;


import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static Functions.MQDBConnections.MQConnections.*;
import static Functions.MQDBConnections.dbConnections.*;
import static Functions.MQDBConnections.SearchLog.*;
import static Functions.Utils.ChargesValidations.ChargesCalculations;
import static Functions.Utils.CommonMethods.*;
import static Functions.Utils.CommonMethods.columnMap;
import static Functions.Utils.Constant.*;
import static com.sun.tools.xjc.reader.Ring.add;


public class EndToEnd extends BaseClass {

    static String ExpectedLog="";
    public static String CaseStatus ="";
    public static String HVPPDocqGroup,CBSSDocqGroup ="";
    public static String CaseLogStatus ="";
    public static String IsTestSECL10="false";
    String TestName;
    Object[] data,PACS2_data;
    ArrayList<String> end2EndIdIDList;
    Map<String, String> columnMap = new HashMap<String, String>();
    Map<String, String> CamtcolumnMap = new HashMap<String, String>();
    boolean HVPPAccountingDone=false;

    /**
     *
     * @Story
     *
     */
    @Test(groups = {"Regression","ZMBRegression","BWARegression","KENRegression","GHARegression","TZARegression"})
    public void Outwards_E2E_SAMOS_SRS_Flow_Test() throws Exception {

            // Set the PACS message location for outwards messages
            MQConnections.setOutwards_PACS_MessagesLoc();

            Object[] MessageType = new Object[]{"PAIN001"};
            SADCBizsvcOption = "Old"; // Default service option for SADC


            for (Object tempMsgetype : MessageType) {
                List<String> PACSMessageType = new ArrayList<String>(Arrays.asList(tempMsgetype.toString().split("::")));

                String RegionType = Region;
                String Message_Name_Type = PACSMessageType.get(0);

                // Load test data for the message type from Excel
                ExcelValues excelValues = new ExcelValues();
                List<Map<String, String>> testDataList = excelValues.readExcelDataAsListOfMaps(TEST_DATA_ENDTOEND.get(country), Message_Name_Type);

                // Skip test case if flagged
                if (!SkipTest(RegionType, Message_Name_Type)) continue;

                // Add to report
                listener.addTestSuite("End To End Test " + RegionType + "_" + Message_Name_Type, "SIT");

                String TestName = Message_Name_Type + RegionType;

                for (Map<String, String> rowData : testDataList) {
                    if (!rowData.getOrDefault("Test", "").contains(TestName)) continue;

                    columnMap.clear();
                    columnMap.putAll(rowData);

                    String TestScenario = columnMap.get("TestScenario #");
                    String Test_Description = columnMap.get("Test Discription").replace(".", "<br>");
                    String dbName = columnMap.get("System_To");
                    String HVPP_Expected_Log = columnMap.get("Expected ProgressLog");

                    listener.addTestCase(Message_Name_Type + "_" + TestScenario, RegionType);
                    listener.addInfoTestStep(Test_Description);

                    try {
                        for (int i = 1; i <= 1; i++) {
                            // Drop PACS message into MQ based on country-specific configuration
                            String messageTemplate;

                            messageTemplate = EndToEnd_PACS_MESSAGES;



                            end2EndIdIDList = dropMessagesToMQ(messageTemplate, dbName, columnMap, Message_Name_Type, "", "None");

                            listener.addInfoTestStep("Dropped " + Message_Name_Type + " message with E2EID " + end2EndIdIDList.get(0) + " In " + dbName);
                            System.out.println("⭐⭐⭐⭐ MESSAGE NUMBER: " + i);

                        }

                        // Wait until message reaches HVPP system
                        if (!getLandingArea_StatusValue(end2EndIdIDList, hvppText).equalsIgnoreCase("Complete"))
                            continue;

                        HVPPDocqGroup = dbConnections.GetDOCQGroupRef(end2EndIdIDList.get(0));
                        CaseCreation_Validation(HVPPDocqGroup);

                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Passed: Content Validation.")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Passed: Business Rule Validation")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment Value date Validation Passed")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Cut-Off Time Validation Passed")) == null) continue;

                        // Validates whether the HVPP system has generated the expected progress log message:
                        // "Debit Account has been derived." for the given DOCQ group reference.
                        // The method 'validateExpectedLog' checks the database for this log and returns:
                        //   - the matched log string if it is found and no error statuses are present,
                        //   - or null if the expected log is missing or an error log was detected.
                        //
                        // If the method returns null (i.e., the log was not found or an error log was found),
                        // this condition triggers the 'continue' statement, which skips the current test case iteration.
                        // This prevents further steps from executing in an invalid or incomplete scenario,
                        // and moves on to the next test scenario (next row of test data).
                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Debit Account has been derived.")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Credit Account has been derived.")) == null) continue;

                        if(country.equalsIgnoreCase("SYC")){
                            if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "MoP has been derived")) == null) continue;
                            if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "SanctionsBypassed")) == null) continue;
                            if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Window check bypassed")) == null) continue;


                        }else {
                            // Derive MOP
                            MopDerivation(HVPPDocqGroup, extractMatchingBizSvcCodes(Message_Name_Type, RegionType, country, "DropMessage"), "");

                            // If TZA country, wait for Sanctions to process
                            if (country.equalsIgnoreCase("TZA")) Thread.sleep(30000);

                            if (!country.equalsIgnoreCase("MUS")) {
                                // Wait for Sanctions log
                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to Sanctions: Success")) == null)
                                    continue;


                                // if Flag comes back as true meaning that above status was found then it wont Retry if initial log not found due to delay
                                if (CaseStatus.equalsIgnoreCase("Rates Request error. Failed to get charges rate.")) {


                                    Thread.sleep(60000);
                                    if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to Sanctions: Success")) == null)
                                        continue;

                                }


                                // Simulate response if Sanctions are incomplete or user is test account
                                boolean userIsService = System.getProperty("user.name").contains("SVC-HVRTGS");
                                boolean userIsAB = System.getProperty("user.name").contains("AB");
                                boolean sanctionsIncomplete = !CaseLogStatus.equalsIgnoreCase("SanctionsAccepted") && !CaseLogStatus.equalsIgnoreCase("SanctionsBypassed");


                                if ((userIsService && sanctionsIncomplete) || userIsAB) {
                                    Simulate_responses(end2EndIdIDList.get(0), Message_Name_Type, "SanctionsHVPP");
                                }

                               // MesssageGenerationValidation(end2EndIdIDList.get(0), Message_Name_Type, RegionType, hvppText, "Sanctions");
                            }

                            String BizSvc = extractMatchingBizSvcCodes(PACSMessageType.get(0), RegionType, country, "DropMessage");
                            // Do validations on Balance check
                            List<String> ProcessExcemption = GetProcessExcemption(country, PACSMessageType.get(0), BizSvc, columnMap.get("Intiating_System").replaceAll("RTGS", "").replaceAll("OHM", ""));

                        //NEED TO CHECK CASE DETAILS AND SEE IF BALANCE CHECK IS REQUIRED AND IF IT IS THEN WE WILL CHECK PROCESS EXCEMPTION
                        /*if (Balance_check(
                                HVPPDocqGroup,
                                columnMap.getOrDefault("Debtor_Account", ""),
                                columnMap.getOrDefault("Debtor_Agt_Account", ""),
                                "",
                                ProcessExcemption.get(0)
                        ))
                        {
                            System.out.println("✅ Balance check required. Proceeding with account posting.");
                            Check_Account_Posting(HVPPDocqGroup, "", hvppText, Message_Name_Type);
                            HVPPAccountingDone =true;
                        } else {
                            System.out.println("❌ Balance check not required. Skipping account posting.");
                            HVPPAccountingDone =false;
                        }*/

                           // if ((CaseLogStatus = Check_WindowManager(HVPPDocqGroup, RegionType)) == null) continue;
                        }
                        if((CaseLogStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to CBSS: Success")) == null)continue; ;

                        // Ensure CBSS was reached
                        if (!(CaseLogStatus.equalsIgnoreCase("Payment details sent to CBSS: Success") ||
                                CaseLogStatus.equalsIgnoreCase("SanctionsAccepted") ||
                                CaseLogStatus.equalsIgnoreCase("SanctionsBypassed"))) {
                            listener.failStep("Test Failed: HVPP Message not sent to CBSS");
                            continue;
                        }

                        // Validate message generation to CBSS
                        //MesssageGenerationValidation(end2EndIdIDList.get(0), Message_Name_Type, RegionType, hvppText, "GeneratedMessageOut");

                        // Wait for CBSS message to land
                        getLandingArea_StatusValue(end2EndIdIDList, cbssText);
                        CBSSDocqGroup = dbConnections.GetDOCQGroupRef(end2EndIdIDList.get(0));

                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Passed: Content Validation.")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Passed: Business Validation.")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Rule evaluation passed: Business Duplication Check")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Cut-Off Time Validation Passed")) == null) continue;


                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Debit account derived")) == null) continue;
                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Credit account derived")) == null) continue;

                        // Verify Morongwa message sent depending on country
                        if (country.matches("GHA|MUS|UGA")) {
                            // For these countries, check MT_MORONGWA log
                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "MT_MORONGWA message sent to MT_MORONGWA queue: Success")) == null) continue;
                        } else {
                            // For others, check regular Morongwa settlement log
                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Morongwa Settlement message sent to Morongwa Settlement queue: Success")) == null) continue;
                        }

                        if(!CaseStatus.equalsIgnoreCase(null) ||!errorStatuses.contains(CaseStatus)) {
                            // Simulate settlement processing
                            if(Message_Name_Type.equalsIgnoreCase("PACS009GEN")) {
                                Simulate_responses(end2EndIdIDList.get(0), Message_Name_Type, "SETTLEMENTSIM54");
                            }else{
                                Simulate_responses(end2EndIdIDList.get(0), Message_Name_Type, "SETTLEMENTSIM");

                            }
                            // Confirm HVPP message was sent after CBSS success
                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "HVPP message sent to HVPP queue: Success")) == null)
                                continue;

                            // Validate account posting
                            if ((CaseLogStatus = validateExpectedLog(CBSSDocqGroup, "Accounting request sent")) == null)
                                continue;
                            Check_Account_Posting(CBSSDocqGroup, "", cbssText, Message_Name_Type);

                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Accounting response successful")) == null)
                                continue;
                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Posting Successful")) == null)
                                continue;
                            // Final check to confirm case was closed
                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Completing Process . Outcome selected Close Case")) == null)
                                continue;


                            if (!HVPPAccountingDone) {
                                // Final HVPP verification for narrative success
                                listener.addTestStep("**** SWITCHING BACK TO HVPP TO CHECK NARRATIVES ***");
                                dbConnections.connectToDataBase(DBConstants.hvppDBConnection);

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "narratives have been added to the accounting entries table")) == null)
                                    continue;
                                if ((CaseLogStatus = validateExpectedLog(HVPPDocqGroup, "Accounting request sent")) == null)
                                    continue;

                                Check_Account_Posting(HVPPDocqGroup, "", hvppText, Message_Name_Type);
                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Accounting Posting Success: Core banking response successful")) == null)
                                    continue;
                            }
                        }else {
                            listener.failStep("Case failed in CBSS due to case being stuck or not sent to Morongwa");

                        }
                    } catch (Exception e) {
                        listener.failStep("Test Failed due to: " + e.getMessage());
                    }
                }
            }
    }


    //@Test(priority = 1,groups = {"Regression","ZMBRegression","BWARegression","KENRegression","GHARegression","TZARegression","MUSRegression"})
    public void E2E_BAPSTRI_Flow_Test() throws Exception {

        MQConnections.setOutwards_PACS_MessagesLoc();//getting the location of the pacs to be used in the Scrip

        Object[] MarketStructure = new Object[]{"LESAKA","BAPSTRI"};
        Object[] MessageType = new Object[] {"PACS008","PACS009GEN","PACS009COV"};
        //loop for MarketStructure type
        for (Object MarketStrucObj : MarketStructure) {
            List<String> MarketStructureType = new ArrayList<String>(Arrays.asList(MarketStrucObj.toString().split("::")));

            //loop for PACS message Type
            for (Object tempMsgetype : MessageType) {
                List<String> PACSMessageType = new ArrayList<String>(Arrays.asList(tempMsgetype.toString().split("::")));

                // 🔁' NEW: Read all Excel data rows as List<Map<String, String>>
                ExcelValues excelValues = new ExcelValues();
                List<Map<String, String>> testDataList = excelValues.readExcelDataAsListOfMaps(TEST_DATA_ENDTOEND.get(country), PACSMessageType.get(0));

                String Market = MarketStructureType.get(0);
                String Message_Name_Type = PACSMessageType.get(0);
                String TestName =Message_Name_Type+Market;

                            //if Below condition is false the Test will be skipped
                            if (!SkipTest(Market, Message_Name_Type)) continue;

                                for (Map<String, String> rowData : testDataList) {
                                    // Filter only relevant tests
                                    if (!rowData.getOrDefault("Test", "").contains(TestName)) continue;
                                    listener.addTestSuite(Market + "_" + Message_Name_Type, "SIT");

                                    // Set columnMap (used throughout your framework)
                                    columnMap.clear();
                                    columnMap.putAll(rowData);  // Use full row as test input

                                    //Data Collected from the Excel
                                    String TestScenario = columnMap.get("TestScenario #");
                                    String dbName =columnMap.get("System_To");
                                    //Creating new TestCase for Reporting
                                    listener.addTestCase(TestScenario, Market/*GroupName*/);

                                    try {
                                        //=========================DROPPING MESSAGE IN MQ================================
                                        //Function below is responsible for dropping messages to the MQ
                                          for(int i=0;i<1;i++){
                                        end2EndIdIDList = dropMessagesToMQ(CBSS_EndToEnd_BAPSTRI_LESAKA_flow.get(country), dbName, columnMap, Message_Name_Type, "", Market);
                                        listener.addInfoTestStep("Dropped " + Message_Name_Type + " message with E2EID " + end2EndIdIDList.get(0) + " In " + dbName);
                                          }
                                        if (getLandingArea_StatusValue(end2EndIdIDList, cbssText).equalsIgnoreCase("Complete")) {

                                            CBSSDocqGroup = dbConnections.GetDOCQGroupRef(end2EndIdIDList.get(0));
                                            System.out.println("CBSS DocqGroup " + CBSSDocqGroup);

                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Passed: Content Validation.")) == null)
                                                continue;
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Passed: Business Validation")) == null)
                                                continue;
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Payment Value date Validation Passed")) == null)
                                                continue;
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Cut-Off Time Validation Passed")) == null)
                                                continue;
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Routing to Step Sanctions Screening. Outcome selected Success Assignment Type PULL Outcome Reason Text: ")) == null)
                                                continue;

                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Morongwa Settlement message sent to Morongwa Settlement queue: Success")) == null)
                                                continue;

                                            // Simulate settlement processing
                                            Simulate_responses(end2EndIdIDList.get(0), Message_Name_Type, "SETTLEMENTSIM");


                                            if (Market.equalsIgnoreCase("LESAKA")) {
                                                if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "LESAKA message sent to LESAKA queue: Success")) == null)
                                                    continue;

                                            } else {
                                                if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "BAPS message sent to BAPS queue: Success")) == null)
                                                    continue;

                                            }
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Accounting response successful")) == null)
                                                continue;
                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Posting Successful")) == null)
                                                continue;
                                            Check_Account_Posting(CBSSDocqGroup, "", cbssText, Message_Name_Type);

                                            if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Completing Process . Outcome selected Close Case")) == null)
                                                ;
                                        }
                                        } catch(NullPointerException nE){
                                            listener.failStep("Test Failed ");
                                        }

                            }
                        }
                    }
                }

    /**
     *
     * @Story
     * 258057
     */
   // @Test(groups = {"Regression","ZMBRegression","BWARegression","KENRegression"})
    public void SECL10_EndToEnd_Test() throws Exception {
        listener.addTestSuite("End To End SECL10", "SIT");
        IsTestSECL10 ="true";

        MQConnections.setOutwards_PACS_MessagesLoc();//getting the location of the pacs to be used in the Scrip
        Object[] MessageType = new Object[] {"SECL10Debit","SECL10Credit"};

            //loop for PACS message Type
            for (Object tempMsgetype : MessageType) {
                List<String> PACSMessageType = new ArrayList<String>(Arrays.asList(tempMsgetype.toString().split("::")));

                // 🔁' NEW: Read all Excel data rows as List<Map<String, String>>
                ExcelValues excelValues = new ExcelValues();
                List<Map<String, String>> testDataList = excelValues.readExcelDataAsListOfMaps(TEST_DATA_ENDTOEND.get(country), "SECL10");

                    if (!SkipTest(Region, PACSMessageType.get(0))) continue;

                    //getting Test data from excel to use in our PACS messages

                    for (Map<String, String> rowData : testDataList) {
                        // Filter only relevant tests
                        if (!rowData.getOrDefault("Test", "").contains(PACSMessageType.get(0))) continue;

                        // Set columnMap (used throughout your framework)
                        columnMap.clear();
                        columnMap.putAll(rowData);  // Use full row as test input

                        String TestScenario = columnMap.get("TestScenario #");
                        String Test_Description =columnMap.get("Test Discription").replace(".","<br>");
                        String dbName =columnMap.get("System_To");

                        //Creating new TestCase for Reporting
                        listener.addTestCase(TestScenario, PACSMessageType+Region/*GroupName*/);

                        try {
                            //=========================DROPPING MESSAGE IN MQ================================
                            //Function below is responsible for dropping messages to the MQ
                            end2EndIdIDList = dropMessagesToMQ(EndToEnd_PACS_MESSAGES_VanillaFlow.get(country), dbName, columnMap, PACSMessageType.get(0), "","None");
                            listener.addInfoTestStep("Dropped " + " " + PACSMessageType.get(0) + " message with E2EID " + end2EndIdIDList.get(0) + " In " + dbName);

                            //========================GET STATUS FROM LANDING AREA and GET PROGRESS LOG=======================================
                                if (!getLandingArea_StatusValue(end2EndIdIDList, hvppText).equalsIgnoreCase("Complete"))
                                    continue;
                                HVPPDocqGroup = dbConnections.GetDOCQGroupRef(end2EndIdIDList.get(0));
                                /**CaseLogStatus = dbConnections.Get_ProgressLogLIKE(HVPPDocqGroup, "Routing to Step Business Exceptions", "Routing to Step Business Interventions");

                                 //===============================Account Derivation HVPP===============================================
                                 if (CaseLogStatus.contains("Routing to Step Business Exceptions") || CaseLogStatus.equalsIgnoreCase("Routing to Step Business Interventions")) {
                                 listener.failStep("Test Failed Due to " + CaseLogStatus);
                                 }
                                 else
                                 {*/
                                //===============================Account Derivation HVPP===============================================
                                CaseLogStatus = dbConnections.Get_ProgressLogLIKE(HVPPDocqGroup, "Debit Account has been derived.", "");
                                String hvppDebitAccount = CaseLogStatus.replaceAll("[^0-9]", " ").trim();

                                Get_PACS002_responses_backto_Origin(end2EndIdIDList.get(0),"RCVD",PACSMessageType.get(0));

                                if (dbConnections.Get_ProgressLog(HVPPDocqGroup, "Credit transfer message Generated").equalsIgnoreCase("Credit transfer message Generated")) {
                                    ///================================Window Manager Request================================================================
                                    if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to Sanctions: Success")) == null)
                                        continue;
                                    // Simulate_responses(end2EndIdIDList.get(0),PACSMessageType.get(0),"SanctionsHVPP");

                                    CaseLogStatus = Check_WindowManager(HVPPDocqGroup, Region);

                                    if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to CBSS: Success")) == null)
                                        continue;

                                            getLandingArea_StatusValue(end2EndIdIDList, cbssText);
                                            Thread.sleep(10000);

                                            CBSSDocqGroup = dbConnections.GetDOCQGroupRef(end2EndIdIDList.get(0));
                                            System.out.println("CBSS DocqGroup " + CBSSDocqGroup);

                                            /**
                                             * @Story 325201 for Zambia Matching CAMT54
                                             */
                                            //=========================================SIMULATE CAMT54 IN CBSS ================================//
                                            ExcelValues CamtexcelValues = new ExcelValues();
                                            List<Map<String, String>> Camt_Excel_data = CamtexcelValues.readExcelDataAsListOfMaps(TEST_DATA_CAMT_MESSAGE, "CAMT054");



                                            for (Map<String, String> CamtrowData : Camt_Excel_data) {
                                                // Filter only relevant tests
                                                if (!CamtrowData.getOrDefault("Test", "").contains(PACSMessageType.get(0) + country)) continue;

                                                // Set columnMap (used throughout your framework)
                                                CamtcolumnMap.clear();
                                                CamtcolumnMap.putAll(CamtrowData);  // Use full row as test input

                                                setCAMTMessagesLoc();

                                                //CAMT.054
                                                //Simulate_responses(end2EndIdIDList.get(0), PACSMessageType.get(0), "SETTLEMENTSIM45");

                                                switch (PACSMessageType.get(0)) {
                                                    case "SECL10Debit":

                                                        //get SECL10 message and retrieve values to use when check retail batch
                                                        GetxmlMessageFromDB(end2EndIdIDList.get(0), "SECL.010", cbssText, "LandingArea");

                                                        if (dbConnections.GetCodeMapping_Retail_batch().get(7).equalsIgnoreCase("t")) {
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "No AUTO-FUNDING Confirmation Received");

                                                           /* for (Map<String, String> CamtrowData1 : Camt_Excel_data) {
                                                                // Set columnMap (used throughout your framework)
                                                                CamtcolumnMap.clear();
                                                                CamtcolumnMap.putAll(CamtrowData1);  // Use full row as test input

                                                                setCAMTMessagesLoc();
                                                            }*/
                                                            dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "SECL.010", "UniversalQueue", "camt.054");

                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "AUTO-FUNDING is Successful");

                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Accounting request sent");
                                                        } else
                                                        {
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "IAT Funding Request Initiated");

                                                            //if Retail Batch is false for Debit SECl10 then simulate CAMT54 with SECL10
                                                            dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "camt.050", "UniversalQueue", "PACS.002");


                                                            //Need to check why we are simulating 054 for SECL10 because accounting will be done already
                                                            dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "SECL.010", "UniversalQueue", "camt.054");

                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "HVPP message sent to HVPP queue: Success");
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Accounting request sent");
                                                        }
                                                        break;

                                                    /**
                                                     *
                                                     *@Story
                                                     *322425
                                                     *322428
                                                     *
                                                     */
                                                    case "SECL10Credit":

                                                        //check the schema of the case and then check on the dependent Table
                                                        Boolean Dependent = GetCAMT054_Dependent(CBSSDocqGroup);

                                                        /**
                                                         * Need to match CAMT 054 with SECL10 when case is on No Confirmation PSO
                                                         */
                                                        if (Dependent.equals(true)) {
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "No Confirmation that PSO Settlement process is successful - Withdrawal process is not ready yet");

                                                            dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "SECL.010", "UniversalQueue", "CAMT.054");
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "HVPP message sent to HVPP queue: Success");

                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "PSO Settlement process is successful - Withdrawal process is ready");

                                                            /**
                                                             * @Story 329434 for SA
                                                             * We need to check if the retail batch is true or false ,if its true then the case must move until closure but if its false
                                                             * the case must wait for a second CAMT.054
                                                             */
                                                            if (dbConnections.GetCodeMapping_Retail_batch().get(7).equalsIgnoreCase("t")) {
                                                                listener.addInfoTestStep("Retail Batch is true for Country  " + country);
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Retail Batch Settlement - Withdrawal  Notification: WITHDRAWAL SUCCESSFUL, Camt.054, CRDT, Amount: 100");
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Retail Batch Withdrawal Settlement is Successful");
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Accounting request sent");

                                                            }
                                                            else {
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "IAT Withdrawal Request Initiated");

                                                                /**Once IAT request has been initiated , a CAMT_50 will be generated,so we need to
                                                                 drop another camt 54 so that we process the Case further to account posting*/

                                                                dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "camt.050", "UniversalQueue", "camt.054");
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "HVPP message sent to HVPP queue: Success");

                                                            }

                                                        } else
                                                        //if Dependent is false then the system must not wait for the Camt 54
                                                        {
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "IAT Withdrawal Request Initiated");

                                                            dropSimulationMessagesToMQ(CAMT_MESSAGES_FOR.get(country), cbssText, CamtcolumnMap, end2EndIdIDList.get(0), "camt.050", "UniversalQueue", "camt.054");
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "HVPP message sent to HVPP queue: Success");

                                                        }

                                                        if (CaseLogStatus.equalsIgnoreCase("HVPP message sent to HVPP queue: Success") || CaseLogStatus.equalsIgnoreCase("AUTO-FUNDING is Successful")
                                                                || CaseLogStatus.equalsIgnoreCase("Accounting request sent")) {

                                                            Check_Account_Posting(CBSSDocqGroup, "", cbssText, "SECL10");
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Accounting response successful");
                                                            CaseLogStatus = dbConnections.Get_ProgressLog(end2EndIdIDList.get(0), "Posting Successful");

                                                            CaseLogStatus = dbConnections.Get_ProgressLogLIKE(CBSSDocqGroup, "Completing Process . Outcome selected Close Case", "Completing Process . Outcome selected CloseCaseOutMGt");


                                                            if (CaseLogStatus.equalsIgnoreCase("Completing Process . Outcome selected Close Case") || CaseLogStatus.equalsIgnoreCase("Completing Process . Outcome selected CloseCaseOutMGt")) {
                                                                //Connecting BACK to HVPP
                                                                listener.addInfoTestStep("**** SWITCHING BACK TO HVPP TO CHECK NARRATIVES ***");

                                                                dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(HVPPDocqGroup, "narratives have been added to the accounting entries table");

                                                                //Now check hvpp narratives
                                                                //================================CHECK ACCOUNT POSTING IN HVPP=====================================================================
                                                                Check_Account_Posting(HVPPDocqGroup, "", hvppText, PACSMessageType.get(0));
                                                                CaseLogStatus = dbConnections.Get_ProgressLog(HVPPDocqGroup, "Accounting Posting Success: Core banking response successful");

                                                            } else {
                                                                listener.failStep("Test Failed Due to Status [Completing Process . Outcome selected Close Case] not found in CBSS ");
                                                            }
                                                        }
                                                        else {
                                                            listener.failStep("Test Failed Due to Status [HVPP message sent to HVPP queue: Success] not found in CBSS ");
                                                        }
                                                }

                                            }
                                        }

                        } catch (Exception e) {
                            System.out.println("Whole Test Failed  "+e.getMessage());
                        }
                    }
                }
    }

    //@Test(groups = {"Regression","ZMBRegression","BWARegression","KENRegression"})

    public void E2E_ROF_Flow_Test() throws Exception {
        listener.addTestSuite("End To End PACS004 Message Test", "SIT");

        Object[] Outwards_OriginalMessage = new Object[]{"PACS.008"};
        Object[] MessageType = new Object[] {"PACS004"};

        SADCBizsvcOption = "New";
        String RegionType = Region;
        IsTestLESAKA =true;

        //loop for Outwards_OriginalMessage type
        for (Object Outwards_OriginalMessageObj : Outwards_OriginalMessage) {
            List<String> OrignalMessageType = new ArrayList<String>(Arrays.asList(Outwards_OriginalMessageObj.toString().split("::")));

            //loop for PACS message Type
            for (Object tempMsgetype : MessageType) {
                List<String> PACSMessageType = new ArrayList<String>(Arrays.asList(tempMsgetype.toString().split("::")));
                String BizsvcUsed = extractMatchingBizSvcCodes(OrignalMessageType.get(0).replaceAll("\\.", ""), RegionType, country, "DropMessage");

                String OrignalMessageTypeUsed = OrignalMessageType.get(0).replaceAll("PACS.009GEN","PACS.009");

                    //Creating new TestCase for Reporting
                    listener.addTestCase("Outwards "+PACSMessageType.get(0) + "End to End Flow", OrignalMessageType.get(0)+"_"+BizsvcUsed/*GroupName*/);

                    try {
                        //=========================GETTING a Closed Case MESSAGE IN MQ================================'

                        // String StatusCheck ="Case Closed";
                        connectToDataBase(DBConstants.hvppDBConnection);
                        List<String> Outwards_Close_CaseDetails = GetLastClosedCase("IN", "OutwardsPACS004", "PACS.008", BizsvcUsed, "CBSS.HVPP");


                        //Function below is responsible for dropping messages to the MQ
                        ExcelValues PACS002excelValues = new ExcelValues();
                        List<Map<String, String>> PACS002testDataList = PACS002excelValues.readExcelDataAsListOfMaps(TEST_DATA_PACS002, "PACS002_Positive");

                        for (Map<String, String> rowDataPASC002 : PACS002testDataList) {
                            // Filter only relevant tests
                            if (!rowDataPASC002.getOrDefault("Test", "").contains("PACS004SIMTEST")) continue;

                            // Set columnMap (used throughout your framework)
                            columnMapSimulation.clear();
                            columnMapSimulation.putAll(rowDataPASC002);  // Use full row as test input
                            //putting the Business Svc on the Map so that we are able to differenciate between Domestic and SADC flow when generating Message
                            columnMapSimulation.put("PACS004BizSvc",Outwards_Close_CaseDetails.get(4));
                            setCAMTMessagesLoc();
                            // Outwards_Close_CaseDetails.set(0,"LSK250919040353");
                            dropSimulationMessagesToMQ("src/main/resources/Outwards/pacs/", hvppText, columnMapSimulation, Outwards_Close_CaseDetails.get(0), OrignalMessageTypeUsed, "None", "PACS004");



                            //========================GET STATUS FROM LANDING AREA and GET PROGRESS LOG=======================================
                            if (getLandingArea_StatusValue((ArrayList<String>) Outwards_Close_CaseDetails, hvppText).equalsIgnoreCase("Complete")) {

                                HVPPDocqGroup = dbConnections.GetDOCQGroupRef(Outwards_Close_CaseDetails.get(0));

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Passed: Content Validation.")) == null) continue;
                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment Value date Validation Passed")) == null) continue;
                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "MoP has been derived")) == null) continue;

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "narratives have been added to the accounting entries table")) == null) continue;
                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Accounting response retrieved")) == null) continue;

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Accounting Posting Success: Core banking response successful")) == null) continue;

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Window check response - Window Open")) == null) continue;

                                if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "HVPPToCBSSSendOutPacs004 message has been generated")) == null) continue;

                                    if ((CaseStatus = validateExpectedLog(HVPPDocqGroup, "Payment details sent to CBSS: Success")) == null) continue;

                                    try {
                                        getLandingArea_StatusValue((ArrayList<String>) Outwards_Close_CaseDetails, cbssText);

                                        CBSSDocqGroup = dbConnections.GetDOCQGroupRef(Outwards_Close_CaseDetails.get(0));
                                        System.out.println("HVPP DocqGroup " + CBSSDocqGroup);
                                        //======================================================================================================//
                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Passed: Content Validation.")) == null) continue;
                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Rule evaluation passed: Business Duplication Check")) == null) continue;
                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Payment Value date Validation Passed")) == null) continue;

                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Window check response - Window Open")) == null) continue;
                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Morongwa Settlement message sent to Morongwa Settlement queue: Success")) == null) continue;

                                        Simulate_responses(Outwards_Close_CaseDetails.get(0), "PACS.004", "SETTLEMENTSIM");

                                        Check_Account_Posting(CBSSDocqGroup, "", cbssText, "PACS.004");
                                        // listener.addTestStep("**** SWITCHING BACK TO HVPP TO CHECK NARRATIVES ***");


                                        if ((CaseStatus = validateExpectedLog(CBSSDocqGroup, "Funds Returned - Case Closed")) == null) continue;


                                    } catch (NullPointerException nE) {
                                        listener.failStep("Test Failed ");
                                    }
                                }
                        }
                    } catch (Exception e) {
                        System.out.println("Whole Test Failed  " + e.getMessage());
                    }

                }

            }

    }
}
