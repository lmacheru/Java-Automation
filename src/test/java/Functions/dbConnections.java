package Functions.MQDBConnections;

import Functions.Utils.CommonMethods;
import Functions.Utils.TestRporter.BaseClass;
import Functions.Utils.XmlXpaths;
import Outwards.Tests.EndToEnd.EndToEnd_TestFlow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static Functions.MQDBConnections.MQConnections.EndToEnd_PACS_MESSAGES_VanillaFlow;
import static Functions.MQDBConnections.MQConnections.dropSimulationMessagesToMQ;
import static Functions.Utils.CommonMethods.*;


public class dbConnections extends BaseClass {
    public static Connection con;
   // static SeleniumListener listener = new SeleniumListener();
    public static void connectToDataBase(HashMap<String, String> hashMap) throws SQLException {

        try {
            Class.forName(DBConstants.DRIVER_NAME);

            //Make Connection To the DB
            con = DriverManager.getConnection(hashMap.get("url"), hashMap.get("userName"), hashMap.get("password"));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            listener.failStep("Connection to the DB failed "+e.getMessage());

        } catch (SQLException e) {
            e.printStackTrace();
            listener.failStep("Connection to the DB failed "+e.getMessage());

        }

            DatabaseMetaData meta = con.getMetaData();
            System.out.println("URL: " + meta.getURL());
            System.out.println("User: " + meta.getUserName());
            System.out.println("Product Name: " + meta.getDatabaseProductName());
            System.out.println("Product Version: " + meta.getDatabaseProductVersion());

        }

    public static String GetMessageStatus(String instrID,String currentCountry) {
        String Status = null;
        try {
            String instrctedID1 = "%" + instrID + "%";

           String query= "select * from swift.\"Swift_Message_LandingArea\" smla  where 1=1 and \"SourceMessage\" like ? \n" +
                    "and \"CountryCode\" = ? and \"DateReceived\" ::date = '"+getTodayDate()+"'";
            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_STATUS_MESSAGE_LANDING_AREA);

            pst.setString(1, instrctedID1);
            pst.setString(2, currentCountry);

            System.out.println("Status Query  " +pst);

            int i =0;
            do {
            ResultSet resultSet = pst.executeQuery();

                        if(resultSet.next()) {
                            Status = resultSet.getString("Status");

                        if(resultSet.getString("Status").equals("Complete")) {
                            resultSet.close();
                            break;
                        }

                    }
                System.out.println("Count " +i);
                i++;
                Thread.sleep(1000);
            }while (i<=25);
            //
                    pst.close();

        } catch (SQLException | InterruptedException e) {
            e.printStackTrace();
        }

        return Status;
    }

    public static List<String> GetSpecificMessage_Status(String instrID, String systemType, String CaseStatus) throws SQLException {
        String Status = "";
        List<String> MessageInfo = new ArrayList<String>();

        try {
            String instrctedID1 = "%" + instrID + "%";
            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_LANDING_AREA_Exception_Message);

            pst.setString(1, country);
            pst.setString(2, instrctedID1);

            String Query_Case_status = "%" + CaseStatus + "%";
            pst.setString(3, Query_Case_status);

            System.out.println("Status Query  " +pst);

                int i =0;
                do {
                    ResultSet resultSet = pst.executeQuery();

                    while (resultSet.next()) {

                        if(resultSet.getString("Status").equals(CaseStatus)) {
                            MessageInfo.add(resultSet.getString("Status"));
                            MessageInfo.add(resultSet.getString("ErrorMessage"));
                            MessageInfo.add(resultSet.getString("SourceMessage"));
                            resultSet.close();
                            break;
                        }
                    }


                    System.out.println("Count " +i);
                    i++;
                }while (i<=10);


            } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        if (MessageInfo == null || MessageInfo.isEmpty()) {

            String instrctedID1 = "%" + instrID + "%";
            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_STATUS_MESSAGE_LANDING_AREA);

            pst.setString(1, instrctedID1);
            pst.setString(2, country);

            System.out.println("Status Query  " + pst);

            ResultSet resultSet = pst.executeQuery();

            while (resultSet.next()) {

                MessageInfo.add(resultSet.getString("Status"));
                MessageInfo.add(resultSet.getString("ErrorMessage"));
                MessageInfo.add(resultSet.getString("SourceMessage"));
                resultSet.close();
                break;

            }
        }
        return MessageInfo;
    }

    public static Map<String, String> GetDirectRate(String BaseCurrency, String QuoteCurrency, String Dbname) {
        HashMap<String, String> RatesFromDB = new HashMap<>();
        PreparedStatement pst = null;
        // If currencies are the same, return 1 for all rates
        if (BaseCurrency.equalsIgnoreCase(QuoteCurrency)) {
            RatesFromDB.put("BidRate","1"); // BidRate
            RatesFromDB.put("AskRate","1"); // AskRate
            RatesFromDB.put("MidRate","1"); // MidRate
            return RatesFromDB;
        }
        try {
            // Connect to Rates DB
            dbConnections.connectToDataBase(DBConstants.RatesDBConnection);
            pst = con.prepareStatement(DBConstants.SQL_GET_RateDB);
            pst.setString(1, BaseCurrency);
            pst.setString(2, QuoteCurrency);
            pst.setString(3, country);
            System.out.println("Attempting query: " + BaseCurrency + "/" + QuoteCurrency);
            ResultSet resultSet = pst.executeQuery();
            Thread.sleep(1000);
            while (resultSet.next()) {
                RatesFromDB.put("BidRate",resultSet.getString("BidRate"));
                RatesFromDB.put("AskRate",resultSet.getString("AskRate"));
                RatesFromDB.put("MidRate",resultSet.getString("MidRate"));
            }
            // Reconnect to original DB
            if ("CBSS".equals(Dbname)) {
                dbConnections.connectToDataBase(DBConstants.cbssDBConnection);
            } else if ("HVPP".equals(Dbname)) {
                dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return RatesFromDB;
    }

    //This comes from story
    //#
    public static HashMap<String, String> GetRateType(String CurrencyPairType,String Direction,String CountryCode, String Dbname) {
        PreparedStatement pst =null;
        HashMap<String, String> DerivedRateTypeValue = new HashMap<>();
        try {
            dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
            pst = con.prepareStatement(DBConstants.SQL_GET_HVPP_RateType);
            pst.setString(1, CurrencyPairType);
            pst.setString(2, Direction);
            pst.setString(3, CountryCode);
            System.out.println("Status Query  " +pst);
            ResultSet resultSet = pst.executeQuery();
            Thread.sleep(5000);
            while (resultSet.next()) {
                DerivedRateTypeValue.put("PrimaryRateType", resultSet.getString("RateTypeName"));
                DerivedRateTypeValue.put("SecondaryRateType", resultSet.getString("SecondaryRateTypeName"));
                DerivedRateTypeValue.put("IsLocalCcySettlement", resultSet.getString("IsLocalCcySettlement"));

            }
            // Reconnect to original DB
            if ("CBSS".equals(Dbname)) {
                dbConnections.connectToDataBase(DBConstants.cbssDBConnection);
            } else if ("HVPP".equals(Dbname)) {
                dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return DerivedRateTypeValue;
    }

    public static int GetCurrencyRank(String currency) {
        int rankRetrieved=0;
        PreparedStatement pst =null;
        try {
            dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
            pst = con.prepareStatement(DBConstants.SQL_GET_RateRanking);
            pst.setString(1, currency);
            System.out.println("Status Query  " +pst);
            ResultSet resultSet = pst.executeQuery();
            Thread.sleep(5000);
            while (resultSet.next()) {
                rankRetrieved =Integer.parseInt(resultSet.getString("Rank"));
                System.out.println(rankRetrieved);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return rankRetrieved;
    }

    public static Map<String, String> Credit_Transfer_Transaction_Info(String DocQGroup,String Direction) {
        String Message = null;
        PreparedStatement pst =null;
        HashMap<String, String> RatesRankFromDB = new HashMap<>();

        try {

            pst = con.prepareStatement(DBConstants.SQL_GET_Credit_Transfer_Transaction_Info+" '"+DocQGroup+"'");
            //pst.setString(1, EndtoEnd);
            System.out.println("Status Query  " +pst);

            ResultSet resultSet = pst.executeQuery();
            Thread.sleep(5000);

            while (resultSet.next()) {
                RatesRankFromDB.put("Charge_Bearer", resultSet.getString("Charge_Bearer"));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return RatesRankFromDB;
    }

    public static String GetSourceMessage(String instrID,String currentCountry,String OriginalMessageType,String Dbname,String MessageQueue) throws SQLException, InterruptedException {
        String Message = null;
        PreparedStatement pst = null;

            String instrctedID1 = "%" + instrID + "%";
            String Message_Type = "%" + OriginalMessageType + "%";

            switch (MessageQueue) {
                case "LandingArea":

                    if(GroupTestName.equalsIgnoreCase("CBSSContentValidation")&&currentCountry.equalsIgnoreCase("MUS")||
                       GroupTestName.equalsIgnoreCase("CBSSContentValidation")&&currentCountry.equalsIgnoreCase("TZA")||
                        GroupTestName.equalsIgnoreCase("CBSSContentValidation")&&currentCountry.equalsIgnoreCase("UGA")){
                        pst = con.prepareStatement(DBConstants.SQL_GET_CBSS_MT103XmlMESSAGE_LANDING_AREA);

                        pst.setString(1, currentCountry);
                        pst.setString(2, instrctedID1);

                        int LandACounter = 0;
                        do {
                            ResultSet resultSet = pst.executeQuery();
                            System.out.println("Status Query  " +pst);

                            if (resultSet.next()) {

                                Message = resultSet.getString("SourceMessage");

                                if (resultSet.getString("SourceMessage").length() > 0) {
                                    resultSet.close();
                                    listener.addCodeBlock(" <p style=\"background-color:DodgerBlue;\"><strong>***"+OriginalMessageType+" Landing Area XML Message ***</strong></p>",Message);
                                    break;
                                }

                            }
                            System.out.println("Count " + LandACounter);
                            LandACounter++;
                            Thread.sleep(1000);

                        } while (LandACounter <= 30);
                    }else{
                        pst = con.prepareStatement(DBConstants.SQL_GET_CBSS_OriginalXmlMESSAGE_LANDING_AREA);

                        pst.setString(1, currentCountry);
                        pst.setString(2, Message_Type.toUpperCase());
                        pst.setString(3, instrctedID1);

                        int LandACounter = 0;
                        do {
                            ResultSet resultSet = pst.executeQuery();
                            System.out.println("Status Query  " +pst);

                            if (resultSet.next()) {

                                Message = resultSet.getString("OriginalMessage");

                                if (resultSet.getString("OriginalMessage").length() > 0) {
                                    resultSet.close();
                                    listener.addCodeBlock(" <p style=\"background-color:DodgerBlue;\"><strong>***"+OriginalMessageType+" Landing Area XML Message ***</strong></p>",Message);
                                    break;
                                }

                            }
                            System.out.println("Count " + LandACounter);
                            LandACounter++;
                            Thread.sleep(1000);

                        } while (LandACounter <= 30);
                    }


                    break;
                case "GeneratedMessageOut":

                    pst = con.prepareStatement(DBConstants.SQL_GET_GeneratedMessageOut);

                    pst.setString(1, instrID);
                    pst.setString(2,Message_Type);
                    //Check message generation and if its in CBSS then the destination will be Morongwa else if we in HVPP we do not specify destination

                    if(Dbname.equalsIgnoreCase("HVPP")) {
                        pst = con.prepareStatement(DBConstants.SQL_GET_GeneratedMessageOut);
                    }else{
                        pst = con.prepareStatement(DBConstants.SQL_GET_GeneratedMessageOut+" and \"Destination\" ='MORONGWA'");
                    }
                    pst.setString(1, instrID);
                    pst.setString(2,Message_Type);

                    System.out.println("Query to  Getxml Sent out " + pst);

                    int GmCounter = 0;
                    do {
                        ResultSet resultSet = pst.executeQuery();

                        if (resultSet.next()) {

                            Message = resultSet.getString("RequestMessage");

                            if (resultSet.getString("RequestMessage").length() > 0) {
                                resultSet.close();
                                if(Dbname.equalsIgnoreCase("HVPP")) {
                                    listener.addCodeBlock(" <p style=\"background-color:DodgerBlue;\"><strong>***" + OriginalMessageType + " GeneratedMessageOut to CBSS ***</strong></p>", Message);
                                }else{
                                    listener.addCodeBlock(" <p style=\"background-color:Orange;\"><strong>***" + OriginalMessageType + " GeneratedMessageOut to Morongwa ***</strong></p>", Message);

                                }


                                break;
                            }

                        }
                        System.out.println("Count " + GmCounter);
                        GmCounter++;
                        Thread.sleep(1000);

                    } while (GmCounter <= 30);
                    //
                    pst.close();

                    break;

                case "Sanctions":
                    pst = con.prepareStatement(DBConstants.SQL_GET_HVPP_SanctionOutXml);
                    pst.setString(1, instrctedID1);

                    System.out.println("Query to GET_HVPP_SanctionOutXml From DB " + pst);

                    int ICounter = 0;
                    do {
                        ResultSet resultSet = pst.executeQuery();

                        if (resultSet.next()) {

                            Message = resultSet.getString("GeneratedMessage");

                            if (resultSet.getString("GeneratedMessage").length() > 0) {
                                resultSet.close();
                                listener.addCodeBlock(" <p style=\"background-color:DodgerBlue;\"><strong>***"+OriginalMessageType+" Sanctions Generated Message XML Message ***</strong></p>",Message);

                                break;
                            }

                        }
                        System.out.println("Count " + ICounter);
                        ICounter++;
                        Thread.sleep(1000);

                    } while (ICounter <= 30);
                    break;
            }
            //
            pst.close();

        return Message;
    }

    public static String GetMessageSentout(String EndToEndID,String Status,String MessageType) {
        String Message = null;
        PreparedStatement pst =null;
        try {
            String EndToEnd = "%" + EndToEndID + "%";

            String MessageStatus = "%" + Status + "%";
            String Message_Type = "%" + MessageType + "%";

                pst = con.prepareStatement(DBConstants.SQL_GET_Message_Sent_Out);

                pst.setString(1, EndToEnd);
                pst.setString(2, MessageStatus);
                pst.setString(3, Message_Type);


            System.out.println("Query for Message sent out " + pst);

                int i =0;
                do {
                    ResultSet resultSet = pst.executeQuery();

                    if(resultSet.next()) {

                        Message = resultSet.getString("RequestMessage");

                        if(resultSet.getString("RequestMessage").length() >0) {
                            resultSet.close();
                            break;
                        }

                    }
                    System.out.println("Count " +i);
                    i++;
                    Thread.sleep(1000);

                }while (i<=10);
                //
                pst.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Message;
    }

    public static String GetOnSentMessage(String E2EID) {
        String Message = null;
        PreparedStatement pst =null;


        try {
            String EndToEnd = "%" + E2EID + "%";

            pst = con.prepareStatement(DBConstants.SQL_GET_OnsentMessage);

            pst.setString(1, EndToEnd);
            System.out.println("Query for Message sent out " + pst);
            int i =0;
            do {
                ResultSet resultSet = pst.executeQuery();
                if(resultSet.next()) {
                    Message = resultSet.getString("RequestMessage");

                    if(resultSet.getString("RequestMessage").length() >0) {
                        resultSet.close();
                        break;
                    }
                }
                System.out.println("Count " +i);
                i++;
                Thread.sleep(1000);

            }while (i<=10);
            //
            pst.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Message;
    }

    public static String Get_BAPS_TRI_Message(String E2EID) {
        String Message = null;
        PreparedStatement pst =null;


        try {
            String EndToEnd = "%" + E2EID + "%";

            pst = con.prepareStatement(DBConstants.SQL_GET_BAPS_TRI_MSG);

            pst.setString(1, EndToEnd);
            System.out.println("Query for Message sent out " + pst);
            int i =0;
            do {
                ResultSet resultSet = pst.executeQuery();
                if(resultSet.next()) {
                    Message = resultSet.getString("Message");

                    if(resultSet.getString("Message").length() >0) {
                        resultSet.close();
                        break;
                    }
                }
                System.out.println("Count " +i);
                i++;
                Thread.sleep(1000);

            }while (i<=10);
            //
            pst.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return Message;
    }

    public static String Get_ProgressLog(String DocqGroup, String ExpectedLog) throws InterruptedException, SQLException {
        String ProgressLog = null;
        PreparedStatement pst =null;
        try {

            if( DocqGroup.length() <21){
                pst = con.prepareStatement(DBConstants.SQL_PROGRESS_Log_With_E2E + DocqGroup + "' and \"Description\" = '" + ExpectedLog + "'");

            }else {
                 pst = con.prepareStatement(DBConstants.SQL_PROGRESS_Log + DocqGroup + "' and \"Description\" = '" + ExpectedLog + "'");
            }
            int x =0;
            do {
                ResultSet resultSet = pst.executeQuery();
                System.out.println(pst);

                if(resultSet.next()) {

                    ProgressLog = resultSet.getString("Description");

                    if(resultSet.getString("Description").equals(ExpectedLog)) {
                        System.out.println("============================================================== " +ExpectedLog);
                                if(ProgressLog.equalsIgnoreCase("Accounting request sent")){
                                    //do not print
                                }else {
                                    listener.addTestStep("Expected ProgressLog Found :: " + ProgressLog);
                                }
                        resultSet.close();
                        break;
                    }

                                    }
                Thread.sleep(1000);
                System.out.println("Count from Progresslog " +x);

                x++;

                if(x >=90 && DocqGroup.length() >20) {
                    PreparedStatement pst2 = con.prepareStatement(DBConstants.SQL_PROGRESS_Log + DocqGroup+ "' order by \"DateTime\" desc limit 1");
                    ResultSet resultSetFail = pst2.executeQuery();

                    if(resultSetFail.next()) {

                        ProgressLog = resultSetFail.getString("Description");
                        System.out.println("Expected ProgressLog Not found :: " + ExpectedLog + " Actual Log Found :: " + ProgressLog);
                        listener.FailTestStep("Expected ProgressLog Not found :: " + ExpectedLog + "<br /> Actual Log Found :: "+ ProgressLog );
                        break;
                    }
                }else{
                    ProgressLog=" ";
                }

            }while (x<=100);

            //pst.close();

        } catch (SQLException e) {


                e.printStackTrace();

        }
        return ProgressLog;
    }

    public static Map<String, Boolean> Get_AllProgressLogsStatus(
            String DocqGroup,
            List<String> expectedLogs,
            List<String> errorStatuses
    ) throws InterruptedException {

        Map<String, Boolean> foundLogsMap = new LinkedHashMap<>();
        boolean errorFound = false;

        // Loop over each expected log
        for (String expectedLog : expectedLogs) {
            if (errorFound) {
                System.out.println("Error detected previously, stopping further checks.");
                break;
            }

            boolean foundExpectedLog = false;
            int attempts = 0;
            final int maxAttempts = 50;

            System.out.println("Searching for expected log: " + expectedLog);

            // Retry loop for the current expected log
            while (attempts <= maxAttempts && !foundExpectedLog && !errorFound) {
                // Query for current expected log and all error statuses at once
                StringBuilder condition = new StringBuilder();
                condition.append("\"Description\" LIKE '%").append(expectedLog).append("%'");
                for (String error : errorStatuses) {
                    condition.append(" OR \"Description\" LIKE '%").append(error).append("%'");
                }

                String query = DBConstants.SQL_PROGRESS_Log + DocqGroup + "' AND (" + condition.toString() + ") LIMIT 10";

                try (PreparedStatement pst = con.prepareStatement(query);
                     ResultSet rs = pst.executeQuery()) {

                    while (rs.next()) {
                        String desc = rs.getString("Description");

                        // Check if this is an error status
                        for (String error : errorStatuses) {
                            if (desc.equalsIgnoreCase(error)) {
                                System.out.println("Error status found: " + desc + ". Stopping search.");
                                listener.FailTestStep("Error detected in logs: " + desc);
                                errorFound = true;
                                break;
                            }
                        }

                        if (errorFound) break;

                        // Check if this is the expected log we are searching now
                        if (desc.equalsIgnoreCase(expectedLog)) {
                            System.out.println("Expected log found: " + desc);
                            listener.addTestStep("Found expected log: " + desc);
                            foundExpectedLog = true;
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Optionally decide if you want to break or continue on SQL errors
                }
                System.out.println("attempt: " + attempts);

                if (!foundExpectedLog && !errorFound) {
                    Thread.sleep(1000); // wait 1 second before retrying

                    attempts++;
                }
            }

            foundLogsMap.put(expectedLog, foundExpectedLog);

            if (!foundExpectedLog) {
                System.out.println("Expected log not found after max attempts: " + expectedLog);
                listener.WarningTestStep("Expected log not found after retries: " + expectedLog);
            }
        }

        if (errorFound) {
            System.out.println("Search stopped due to error log found.");
        } else {
            System.out.println("Search completed for all expected logs with no error found.");
        }

        return foundLogsMap;
    }


    public static String Get_ValidProgressLogLike(
            String DocqGroup,
            String expectedLog,                      // single expected log string
            List<String> errorStatusesCheck         // list of error logs to detect
    ) throws InterruptedException {
        String progressLog = "";
        boolean logFound = false;
        final int maxAttempts = 70;
        int attempts = 0;

        try {
            // Retry loop to find either the expected log or any error log
             while (attempts <= maxAttempts) {
                // Build SQL query to search for expected log or any error logs
                String query = DBConstants.SQL_PROGRESS_Log + DocqGroup + "' AND (\"Description\" LIKE '%" + expectedLog + "%'"
                        + errorStatusesCheck.stream()
                        .map(err -> " OR \"Description\" LIKE '%" + err + "%'")
                        .collect(Collectors.joining())
                        + ") LIMIT 10";

                System.out.println("Executing query: " + query);

                try (PreparedStatement pst = con.prepareStatement(query);
                     ResultSet resultSet = pst.executeQuery()) {

                    // Check each found description for error or expected log
                    while (resultSet.next()) {
                        String description = resultSet.getString("Description");

                        // If any error log found, immediately fail and return
                        for (String error : errorStatusesCheck) {
                            if (description.equalsIgnoreCase(error)) {
                                listener.FailTestStep("Error detected in logs: " + description);
                                System.out.println("Error log found: " + description);
                                return description;  // Stop immediately on error
                            }
                        }

                        // If expected log found, mark success and break
                        if (description.contains(expectedLog)) {
                            progressLog = description;
                            listener.addTestStep("✔ Found expected log: [" + description + "]");
                            System.out.println("Expected log found: " + description);
                            logFound = true;
                            break;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (logFound) break;  // Exit loop if expected log found

                // Wait and retry if neither found
                Thread.sleep(1000);
                attempts++;
                System.out.println("Attempt " + attempts + " to find log...");
            }

            if (!logFound) {
                // Fallback to latest log if expected log not found
                String fallbackQuery = DBConstants.SQL_PROGRESS_Log + DocqGroup + "' ORDER BY \"DateTime\" DESC LIMIT 1";
                try (PreparedStatement pstFallback = con.prepareStatement(fallbackQuery);
                     ResultSet resultSetFail = pstFallback.executeQuery()) {

                    if (resultSetFail.next()) {
                        progressLog = resultSetFail.getString("Description");
                        System.out.println("Fallback ProgressLog: " + progressLog);

                        listener.WarningTestStep("❌ Expected log not found : '" + expectedLog + "' was not found.<br>❗Last log found: " + progressLog);

                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return progressLog;
    }


    public static String Get_ProgressLogLIKE(String DocqGroup, String ExpectedLog,String ExpectedLog2) throws InterruptedException {
        String ProgressLog = "";


        try {

            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_PROGRESS_Log + DocqGroup+ "' and \"Description\" LIKE '%"+ExpectedLog+"%' LIMIT 1");
            int x =0;
            System.out.println("Search expected value like query :: "+pst);

            do {
                ResultSet resultSet = pst.executeQuery();

                if(resultSet.next()) {

                        ProgressLog = resultSet.getString("Description");

                        System.out.println("Searched for ProgressLog :: " + ExpectedLog + " and found [ " + ProgressLog+" ]") ;
                        listener.addTestStep("Docqgroup ::"+DocqGroup+" Searched for ProgressLog :: " + ExpectedLog + " <br>" +
                                                             " found [ " + ProgressLog+ " ]");
                       // resultSet.close();
                        break;


                }
                Thread.sleep(1000);
                System.out.println("Count from Progresslog " +x);
                x++;


                if(x >=40 && !ExpectedLog2.isEmpty()) {

                    PreparedStatement pst2 = con.prepareStatement(DBConstants.SQL_PROGRESS_Log + DocqGroup+ "' and \"Description\" LIKE '%"+ExpectedLog2+"%' LIMIT 1");
                    ResultSet resultSet2 = pst2.executeQuery();

                    if(resultSet2.next()) {

                        ProgressLog = resultSet2.getString("Description");

                        System.out.println("ProgressLog :: " + ExpectedLog2 + " found log is " + ProgressLog);
                        listener.addTestStep("Docqgroup ::"+DocqGroup+" ProgressLog Found :: " + ProgressLog + " DocqGroup " + DocqGroup);
                        break;
                    }
                }

            }while (x<=60);

            //pst.close();
            if(ProgressLog.equalsIgnoreCase("")){

                    PreparedStatement pst2 = con.prepareStatement(DBConstants.SQL_PROGRESS_Log + DocqGroup+ "' order by \"DateTime\" desc limit 1");
                    ResultSet resultSetFail = pst2.executeQuery();

                    if(resultSetFail.next()) {
                        ProgressLog = resultSetFail.getString("Description");
                        System.out.println("ProgressLog :: " + ExpectedLog + " not found instead found ::: " + ProgressLog);

                        if(ExpectedLog.equalsIgnoreCase("Routing to Step Business Exceptions") || ExpectedLog2.equalsIgnoreCase("Routing to Step Business Interventions")){
                            listener.addInfoTestStep(" Exceptions Occured");

                        }else {
                            listener.FailTestStep("Docqgroup ::"+DocqGroup+" ProgressLog :: " + ExpectedLog + " not found instead found :: <br />" + ProgressLog);

                        }
                    }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ProgressLog;
    }
    public static String GetMessageError(String instrID,String currentCountry) {
        String Status = null;
        try {
            try {
                String instrctedID1 = "%" + instrID + "%";
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_STATUS_MESSAGE_LANDING_AREA);

                pst.setString(1, instrctedID1);
                pst.setString(2, currentCountry);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(10000);
                while (resultSet.next()) {
                    Status = resultSet.getString("ErrorMessage");
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Status;
    }

    public static String GetPostingRecords(String Docqgroup,String RetrieveType,String TransactionType) {
        String PostingResponse = null;
        PreparedStatement pst =null;
        try {
            try {
                if(TransactionType.equalsIgnoreCase("Principal")){
                    pst = con.prepareStatement(DBConstants.SQL_AccountPosting + Docqgroup + "'");

                }else if(TransactionType.equalsIgnoreCase("Charge")){
                     pst = con.prepareStatement(DBConstants.SQL_Charge_Amount_AccountPosting + Docqgroup + "') and \"Request\" like '%Comm%'");
                }

                System.out.println(TransactionType+" "+RetrieveType+" Account Posting query :: " + pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(8000);

                if(RetrieveType.equalsIgnoreCase("Response")) {
                    while (resultSet.next()) {
                        PostingResponse = resultSet.getString("Response");
                        System.out.println(RetrieveType+" Account Posting response:: " + PostingResponse);

                    }

                }else if(RetrieveType.equalsIgnoreCase("Request")){
                    while (resultSet.next()) {
                        PostingResponse = resultSet.getString("Request");
                    }
                }

                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return PostingResponse;
    }

    public static List<String> GetAccountDetails(String AccountNumber,String currentCountry) {
        String PostingResponse = null;
        List<String> AccountDetails = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_AccountDetails);
                pst.setString(1, currentCountry);
                pst.setString(2, AccountNumber);

                System.out.println("Account Details query :: " + pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(5000);
                while (resultSet.next()) {
                    AccountDetails.add(resultSet.getString("AccountTypeCode"));
                    AccountDetails.add(resultSet.getString("AccountName"));
                    AccountDetails.add(resultSet.getString("AccountDescription"));
                    AccountDetails.add(resultSet.getString("AccountNumber"));
                    AccountDetails.add(resultSet.getString("AKA"));
                    AccountDetails.add(resultSet.getString("BIC"));
                    AccountDetails.add(resultSet.getString("BalanceCheckRequired"));
                    AccountDetails.add(resultSet.getString("IsPreferred"));
                    AccountDetails.add(resultSet.getString("IsPreferredCLS"));
                    AccountDetails.add(resultSet.getString("SarsMultitierAcct"));
                    AccountDetails.add(resultSet.getString("SafcomAcct"));
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return AccountDetails;
    }

    public static List<String> GetCuttOffTimeProfile(String MessageType,String TriggerCode,String CategoryPurp) {
        List<String> CuttOffTimeDetails = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_Cutt_Off_Details);
                pst.setString(1, MessageType);
                pst.setString(2, TriggerCode);
                pst.setString(3, CategoryPurp);


                System.out.println("Cutt off Profile Details query :: " + pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(5000);
                while (resultSet.next()) {
                    CuttOffTimeDetails.add(resultSet.getString("CuttOffName"));
                    CuttOffTimeDetails.add(resultSet.getString("StartProcessingTime"));
                    CuttOffTimeDetails.add(resultSet.getString("CuttOffTime"));
                    CuttOffTimeDetails.add(resultSet.getString("CuttOffTimeExtension"));
                    CuttOffTimeDetails.add(resultSet.getString("CountryCode"));
                    CuttOffTimeDetails.add(resultSet.getString("Direction"));
                    CuttOffTimeDetails.add(resultSet.getString("TriggerCode"));
                    CuttOffTimeDetails.add(resultSet.getString("ThresholdAmount"));
                    CuttOffTimeDetails.add(resultSet.getString("CategoryPurposeCode"));

                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return CuttOffTimeDetails;
    }

    public static List<String> GetCiruitAndUpdate(String ExpectedBreakerValue, String CiruitBreakerUpdate) {
        List<String> CiruitBreakerDetailts = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_BreakerValue);
                pst.setString(1, country);

                System.out.println("Circuit Breaker query :: " + pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(5000);
                while (resultSet.next()) {
                    CiruitBreakerDetailts.add(resultSet.getString("Status"));
                }
                if(CiruitBreakerDetailts.get(0).equalsIgnoreCase(ExpectedBreakerValue)){
                    System.out.println("Circuit Breaker is CLOSED for Country :: " + country);
                    listener.addInfoTestStep("Circuit Breaker is CLOSED for Country "+country);

                }else {
                    System.out.println("Circuit Breaker is OPEN for Country :: " + country);

                    listener.addInfoTestStep("Circuit Breaker is Open for Country "+country +"and will be updated to CLOSED");

                    PreparedStatement Updatequery = con.prepareStatement(DBConstants.UpdateCircuitBreaker);
                    Updatequery.setString(1, CiruitBreakerUpdate);
                    Updatequery.setString(2, country);

                    Updatequery.executeQuery();

                    System.out.println("Circuit Breaker has been updated to Closed :: " + Updatequery);
                    listener.addInfoTestStep("Circuit Breaker is Updated to CLOSED for Country "+country);

                    resultSet.close();
                    Updatequery.close();
                    pst.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return CiruitBreakerDetailts;
    }


    //After landing we run below
    public static String GetDOCQGroupRef(String E2EID) {
        String DocQGroupRefNo= null;
        String CaseNumber=null;
        String CaseChar = "";
        PreparedStatement pst1;
            try {
                if (country.equals("ZMB") && EndToEnd_TestFlow.IsTestSECL10.equalsIgnoreCase("true")) {
                    pst1 = con.prepareStatement(DBConstants.SQL_GET_DOCQGROUP_By_CASE_CHAR);
                    pst1.setString(1, E2EID);
                    pst1.setString(2, "%BAF%");

                } else {
                    pst1 = con.prepareStatement(DBConstants.SQL_GET_SWIFT_MESSAGE_DOCQGROUP);
                    pst1.setString(1, E2EID);
                }

                ResultSet rs1 = pst1.executeQuery();
                while (rs1.next()) {
                    DocQGroupRefNo = rs1.getString("DocQGroupRefNo");
                    CaseNumber = rs1.getString("GroupCode");

                    if(GroupTestName.contains("ContentValidation") || GroupTestName.contains("BusinessValidation")) {
                        //DOnt write the log of the Docqgroup when running validation script
                    }else{
                        listener.addInfoTestStep("DocQGroupRefNo is " + DocQGroupRefNo);
                        listener.addInfoTestStep("GroupCode /Case Number is " + CaseNumber);

                    }
                    System.out.println("Your DocQGroupRefNo is " + DocQGroupRefNo);
                }

                System.out.println("Query to get docqgroupRef is " + pst1);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        return DocQGroupRefNo;
    }

    public static String GetCaseNumber(String E2EID,String Char) {
        String GroupCode = null;
        String CaseChar = "%"+Char+"%";
        PreparedStatement pst1 =null;
        try {
            try {
                if(CaseChar.isEmpty()) {
                    pst1=  con.prepareStatement(DBConstants.SQL_GET_SWIFT_MESSAGE_CASENUMBER);
                    pst1.setString(1, E2EID);

                }else{
                    pst1 =con.prepareStatement(DBConstants.SQL_GET_SWIFT_MESSAGE_CASENUMBER_BY_CHAR);
                    pst1.setString(1, E2EID);
                    pst1.setString(2, CaseChar);
                }

                Thread.sleep(4000);
                ResultSet rs1 = pst1.executeQuery();
                while (rs1.next()) {
                    GroupCode = rs1.getString("GroupCode");
                    listener.addTestStep("Your GroupCode is "+GroupCode);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return GroupCode;
    }

    public static String getLandingArea_StatusValue(ArrayList<String> E2EID, String systemType) throws SQLException {
        String CaseStatus ="";

            if(MQStatus.contains("MQJE001: Completion Code '2', Reason")){
                listener.failStep("❌ Error occurred Failed to Drop Message in CBSS BAPS Due to " + MQStatus);
            }else {

                if(systemType.equals("CBSS")){
                    dbConnections.connectToDataBase(DBConstants.cbssDBConnection);

                }else if(systemType.equals("HVPP")) {

                    dbConnections.connectToDataBase(DBConstants.hvppDBConnection);

                }else if(systemType.equals("SLIM")) {
                    dbConnections.connectToDataBase(DBConstants.SlimDBConnection);
                }

                listener.addInfoTestStep("Get LandingArea status of case with E2EID "+E2EID.get(0) +" in " +systemType);

                CaseStatus = dbConnections.GetMessageStatus(String.valueOf(E2EID.get(0)), DBConstants.country);
                System.out.println("Status is -- " + CaseStatus);

                listener.addInfoTestStep("Checking Case with E2EID " + E2EID.get(0) + " In " + systemType);

                if (CaseStatus == null) {
                    // Skip this iteration and move to next test case
                    listener.failStep("Error occured Message was not found in the DB ");
                    CaseStatus = "Message not found in the DB or failed";
                } else {
                    listener.CompareString("Complete", CaseStatus, "Case Status in " + systemType);
                }
                if (CaseStatus.equalsIgnoreCase("Failed - Structural Validation") || CaseStatus.equalsIgnoreCase("Error")) {
                    //DB connection to check the error behind
                    CaseStatus = dbConnections.GetMessageError(String.valueOf(E2EID.get(0)), DBConstants.country);
                    listener.failStep("Error found from the failed Case is :: " + CaseStatus);
                    System.out.println("Status is -- " + CaseStatus);

                } else {

                }
            }
        return CaseStatus;
    }

    public static List<String> WindowManagerApiRequestDetails(String WindowManagerRequestID) {
        List<String> WindowManagerDetails = new ArrayList<String>();

        String DocQGroupRefNo = null;
        try {
            try {
                PreparedStatement pst1 = con.prepareStatement(DBConstants.SQL_GET_WindowManagerApiRequest+ WindowManagerRequestID+ "'");
                //pst1.setString(1, WindowManagerRequestID);
                Thread.sleep(2000);
                ResultSet rs1 = pst1.executeQuery();
                while (rs1.next()) {
                    WindowManagerDetails.add(rs1.getString("WindowManagerApiRequestId"));
                    WindowManagerDetails.add(rs1.getString("RequestContents"));

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return WindowManagerDetails;
    }

    public static String WindowManagerApiRespondsDetails(String WindowManagerApiRequestId) {

        String WindowManagerResponse = null;
        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_WindowManagerApiResponds + WindowManagerApiRequestId+ "'");

                System.out.println("Your Query for WindowManagerApi Response is "+pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(2000);
                while (resultSet.next()) {
                    WindowManagerResponse = resultSet.getString("ResponseContents");

                    listener.addInfoTestStep("Your WindowManagerApi Response Contents  is "+WindowManagerResponse);
                    System.out.println("Your WindowManagerApi Response Contents are "+WindowManagerResponse);
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return WindowManagerResponse;
    }

    public static Map<String, String> GetCaseDetails(String Docqgroup) throws SQLException {
        Map<String, String> caseDetailsMap = new HashMap<>();

        DatabaseMetaData meta = con.getMetaData();
        System.out.println("URL: " + meta.getURL());
        System.out.println("User: " + meta.getUserName());
        System.out.println("Product Name: " + meta.getDatabaseProductName());
        System.out.println("Product Version: " + meta.getDatabaseProductVersion());
        PreparedStatement pst =null;
        ResultSet resultSet =null;
        try {
            if(meta.getURL().contains("CBSS")) {

                  pst = con.prepareStatement(DBConstants.CBSS_SQL_CaseDetails + Docqgroup + "'");
                System.out.println("CaseDetails query :: " + pst);

                Thread.sleep(8000); // Delay if needed for sync
                 resultSet = pst.executeQuery();

                if (resultSet.next()) {
                    caseDetailsMap.put("MOPID", safe(resultSet.getString("MOPID")));
                    caseDetailsMap.put("ClientAcctType", safe(resultSet.getString("ClientAcctType")));
                    caseDetailsMap.put("DebitAccount", safe(resultSet.getString("DebitAccount")));
                    caseDetailsMap.put("CreditAccount", safe(resultSet.getString("CreditAccount")));
                    caseDetailsMap.put("GroupCode", safe(resultSet.getString("GroupCode")));
                    caseDetailsMap.put("NonSTPReason", safe(resultSet.getString("NonSTPReason")));
                    caseDetailsMap.put("Department", safe(resultSet.getString("Department")));

                    // Additional details
                    caseDetailsMap.put("IntiatingSystem", safe(resultSet.getString("IntiatingSystem")));
                    caseDetailsMap.put("schemeID", safe(resultSet.getString("schemeID")));
                    caseDetailsMap.put("TransactionStatus", safe(resultSet.getString("TransactionStatus")));
                    caseDetailsMap.put("InstructionType", safe(resultSet.getString("InstructionType")));
                    caseDetailsMap.put("Amount", safe(resultSet.getString("Amount")));
                    caseDetailsMap.put("Currency", safe(resultSet.getString("Currency")));
                    caseDetailsMap.put("DomicileRate", safe(resultSet.getString("DomicileRate")));
                    caseDetailsMap.put("DomicileRateAppliedAmount", safe(resultSet.getString("DomicileRateAppliedAmount")));
                    caseDetailsMap.put("RateAppliedAmount", safe(resultSet.getString("RateAppliedAmount")));
                    caseDetailsMap.put("Channel", safe(resultSet.getString("Channel")));
                    caseDetailsMap.put("RateType", safe(resultSet.getString("RateType")));

                }
            }
            else{
                pst = con.prepareStatement(DBConstants.SQL_CaseDetails + Docqgroup + "'");

                System.out.println("CaseDetails query :: " + pst);

                Thread.sleep(8000); // Delay if needed for sync
                 resultSet = pst.executeQuery();

                if (resultSet.next()) {
                    caseDetailsMap.put("MOPID", safe(resultSet.getString("MOPID")));
                    caseDetailsMap.put("ClientAcctType", safe(resultSet.getString("ClientAcctType")));
                    caseDetailsMap.put("DebitAccount", safe(resultSet.getString("DebitAccount")));
                    caseDetailsMap.put("CreditAccount", safe(resultSet.getString("CreditAccount")));
                    caseDetailsMap.put("GroupCode", safe(resultSet.getString("GroupCode")));
                    caseDetailsMap.put("NonSTPReason", safe(resultSet.getString("NonSTPReason")));
                    caseDetailsMap.put("Department", safe(resultSet.getString("Department")));

                    // Additional details
                    caseDetailsMap.put("IntiatingSystem", safe(resultSet.getString("IntiatingSystem")));
                    caseDetailsMap.put("schemeID", safe(resultSet.getString("schemeID")));
                    caseDetailsMap.put("TransactionStatus", safe(resultSet.getString("TransactionStatus")));
                    caseDetailsMap.put("BalanceCheckRequired", safe(resultSet.getString("BalanceCheckRequired")));
                    caseDetailsMap.put("InstructionType", safe(resultSet.getString("InstructionType")));
                    caseDetailsMap.put("Amount", safe(resultSet.getString("Amount")));
                    caseDetailsMap.put("Currency", safe(resultSet.getString("Currency")));
                    caseDetailsMap.put("DomicileRate", safe(resultSet.getString("DomicileRate")));
                    caseDetailsMap.put("DomicileRateAppliedAmount", safe(resultSet.getString("DomicileRateAppliedAmount")));
                    caseDetailsMap.put("RateAppliedAmount", safe(resultSet.getString("RateAppliedAmount")));
                    caseDetailsMap.put("DomicileRateQuoteCurrency", safe(resultSet.getString("DomicileRateQuoteCurrency")));
                    caseDetailsMap.put("Channel", safe(resultSet.getString("Channel")));
                    caseDetailsMap.put("RateType", safe(resultSet.getString("RateType")));

                }
            }


            resultSet.close();
            pst.close();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return caseDetailsMap;
    }

    /**
     * Safely handles string input by ensuring it is not null or empty.
     * This method:
     * - Returns an empty string if the input is null or contains only whitespace.
     * - Otherwise, returns the trimmed version of the string.
     * This helps avoid NullPointerExceptions and ensures consistent string handling.
     */
    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        value = value.trim();
        if (value.isEmpty()) {
            return "";
        }
        return value;
    }
    public static List<String> RoutingQueueTB(String Docqgroup) {
        List<String> CaseDetails = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_getRoutingQueue_Details + Docqgroup+ "'");

                System.out.println("RoutingQueue query :: " + pst);

                Thread.sleep(8000);
                ResultSet resultSet = pst.executeQuery();

                while (resultSet.next()) {

                    CaseDetails.add(resultSet.getString("Status"));
                    CaseDetails.add(resultSet.getString("QueueName"));
                    CaseDetails.add(resultSet.getString("TargetSystem"));
                    CaseDetails.add(resultSet.getString("RequestMessage"));
                    break;
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return CaseDetails;
    }

    public static List<String> GetProcessExcemption(String CountryCode, String MessageName, String BusinessServiceCode, String InitiatingSystem) {
        List<String> CaseDetails = new ArrayList<String>();
        String MessageType="";

        switch (MessageName) {
            case "PACS008":
                MessageType = "PACS.008";
                break;
            case "PAIN001":
                MessageType = "PAIN.001";
                break;

            case "PACS009GEN":
                MessageType = "PACS.009";
                break;

            case "PACS009COV":
                MessageType = "PACS.009COV";
                break;
        }

        if(InitiatingSystem.equalsIgnoreCase("AAH")){
            InitiatingSystem ="H2H";
        }else if (InitiatingSystem.equalsIgnoreCase("AAO")){
            InitiatingSystem ="Corporate Online";
        }

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_getProcessExcemption_Data );
                pst.setString(1, CountryCode);
                pst.setString(2, MessageType);
                pst.setString(3, BusinessServiceCode);

                String InitiatingSystemtag = "%" + InitiatingSystem + "%";
                pst.setString(4, InitiatingSystemtag);

                System.out.println("RoutingQueue query :: " + pst);

                Thread.sleep(4000);
                ResultSet resultSet = pst.executeQuery();

                while (resultSet.next()) {

                    CaseDetails.add(resultSet.getString("DoBalanceCheckEarmarking"));
                    CaseDetails.add(resultSet.getString("AutoRejection"));

                    break;
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //if records are not found then we need to set DoBalanceCheckEarmarking to True due to channels like SAP are not configured
        if (CaseDetails.size() <1){
            CaseDetails.add("t");
            CaseDetails.add("f");

        }

        return CaseDetails;
    }


    public static List<String> Get_Slim_Collateral(String Message_Id) {
        List<String> CaseDetails = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_Collateral_Details);
                pst.setString(1, Message_Id);

                System.out.println("CaseDetails query :: " + pst);

                Thread.sleep(8000);
                ResultSet resultSet = pst.executeQuery();

                while (resultSet.next()) {

                    CaseDetails.add(resultSet.getString("TransactionNumber"));
                    CaseDetails.add(resultSet.getString("Source"));
                    CaseDetails.add(resultSet.getString("MessageId"));
                    CaseDetails.add(resultSet.getString("Narrative"));
                    CaseDetails.add(resultSet.getString("Currency"));
                    CaseDetails.add(resultSet.getString("Amount"));
                    CaseDetails.add(resultSet.getString("ABSASSA"));
                    CaseDetails.add(resultSet.getString("AvailableFullCapacity"));

                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return CaseDetails;
    }

    public static boolean GetCAMT054_Dependent(String DocqgroupRef) {
        Boolean Camt50Flag = false;

        List<String> Case_Details = new ArrayList<String>();
        List<String> Case_Dependent = new ArrayList<String>();

        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.CBSS_SQL_CaseDetails+DocqgroupRef+"'");
                System.out.println("Case Details query :: " + pst);

                ResultSet resultSet = pst.executeQuery();
                Thread.sleep(5000);
                while (resultSet.next()) {
                    Case_Details.add(resultSet.getString("IntiatingSystem"));
                    Case_Details.add(resultSet.getString("schemeID"));

                }
                //After getting the case details use the schemeID to get the dependent Value
                if(country.equalsIgnoreCase("ZAF") && !Case_Details.get(1).equalsIgnoreCase("IATWID_CASR")||
                        country.equalsIgnoreCase("ZAF") && !Case_Details.get(1).equalsIgnoreCase("IATWID_REPO") ){
                    System.out.println("SchemeID :: " + Case_Details.get(1) +" is not configured for CAMT.054 Dependent for Country :: "+country);
                    listener.addInfoTestStep("SchemeID :: " + Case_Details.get(1) +" is not configured for CAMT.054 Dependent for Country :: "+country);

                    Camt50Flag = true;

                }else {
                    PreparedStatement pst_Camt54_dependent = con.prepareStatement(DBConstants.SQL_Camt054Dependent);
                    pst_Camt54_dependent.setString(1, country);
                    pst_Camt54_dependent.setString(2, Case_Details.get(1));

                    System.out.println("Camt54_dependent query :: " + pst);

                    ResultSet Camt54_result = pst_Camt54_dependent.executeQuery();
                    Thread.sleep(5000);
                    while (Camt54_result.next()) {
                        Case_Dependent.add(Camt54_result.getString("Dependent"));
                    }
                    if (Case_Dependent.get(0).equalsIgnoreCase("t")) {
                        Camt50Flag = true;
                        listener.addInfoTestStep("Camt54_dependent is " + Camt50Flag + " for SchemeID " + Case_Details.get(1) + " for CountryCode " + country);

                    } else {
                        listener.addInfoTestStep("Camt54_dependent is " + Camt50Flag + " for SchemeID " + Case_Details.get(1) + " for CountryCode " + country);
                    }
                }
                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Camt50Flag;
    }

    public static List<String> GetCodeMapping_Retail_batch() {

        List<String> CaseDetails = new ArrayList<String>();
        String Propriaty = CommonMethods.extract_Data_FromMessage(XmlXpaths.SECL10_Properoiaty, "Prtry");;
         String FromBIC =CommonMethods.extract_Data_FromMessage(XmlXpaths.SECL10_FromBIC, "BICFI");;
        try {
            try {
                PreparedStatement pst = con.prepareStatement(DBConstants.SQL_get_RetailBatch);
                pst.setString(1, Propriaty);
                pst.setString(2, "%"+FromBIC.replaceAll("XXX","").replaceAll("X","")+"%");

                System.out.println("CaseDetails query :: " + pst);

                Thread.sleep(8000);
                ResultSet resultSet = pst.executeQuery();

                while (resultSet.next()) {

                    CaseDetails.add(resultSet.getString("From"));
                    CaseDetails.add(resultSet.getString("TriggerCode"));
                    CaseDetails.add(resultSet.getString("Proprietary"));
                    CaseDetails.add(resultSet.getString("CPLAgreementNumber"));
                    CaseDetails.add(resultSet.getString("MRTCode_Debit"));
                    CaseDetails.add(resultSet.getString("MRTCode_Credit"));
                    CaseDetails.add(resultSet.getString("MRTCode_Cash"));
                    CaseDetails.add(resultSet.getString("IsRetailBatch"));


                }

                resultSet.close();
                pst.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return CaseDetails;
    }

    public static String readSQLFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public static void InsertRatesToDB() {
        try {
            try {
                String sqlFilePath = "src/test/java/Functions/MQDBConnections/SQL_Files/Script_InsertRates.sql";

                String sql = readSQLFile(sqlFilePath);
                Statement stmt = con.createStatement();
                // Execute SQL commands
                stmt.execute(sql);
                System.out.println("SQL Rates insert commands executed successfully.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String get_Branch_Code(String BranchName) {
        String BranchNumber = "000"; // Default to "000"
        try {
            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_GET_BranchNumber);
            pst.setString(1, country);
            pst.setString(2, BranchName);
            System.out.println("Branch Code/number query :: " + pst);

            Thread.sleep(8000);
            ResultSet resultSet = pst.executeQuery();
            if (resultSet.next()) {
                String code = resultSet.getString("BranchCode");
                if (code != null && !code.trim().isEmpty()) {
                    BranchNumber = code.trim(); // Overwrite default only if valid
                }
            }

            resultSet.close();
            pst.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace(); // Log, but do not throw, so "000" is returned
        } catch (NullPointerException e) {
            e.printStackTrace(); // Just in case `BranchName` is null
        }
        return BranchNumber;
    }

    public static void UpdateRMAFailedInwardCase(String BIC,String DocqgroupRef) {
        try {
            PreparedStatement pst = con.prepareStatement(DBConstants.SQL_Update_CaseFailedRMA);
            pst.setString(1, BIC);
            pst.setString(2, DocqgroupRef);
            pst.setString(3, DocqgroupRef);
            System.out.println("CaseDetails query :: " + pst);

            Thread.sleep(8000);
            ResultSet resultSet = pst.executeQuery();

            resultSet.close();
            pst.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> GetLastClosedCase(String Direction, String Status, String Pacs_Message_Type, String BizSvc, String QueueTosearch) {
        List<String> ClosedCaseDetails = new ArrayList<>();
        File file = new File("src/main/resources/Outwards/Testdata/checked_ids.txt");

        Set<String> alreadyChecked = loadCheckedIdsFromFile(file.getPath());
        String currentEndToEndID = null;

        while (true) {
            PreparedStatement pst1 = null;
            ResultSet rs1 = null;

            try {
                StringBuilder sql = new StringBuilder(
                        "select ctti.\"End_To_End_ID\", dq.\"GroupCode\", smla.\"MessageType\", dq.\"Completed\", " +
                                "bah.\"Business_service\", dq.\"Status\", dq.\"CountryCode\", smla.\"SourceQueueName\", " +
                                "\"CreateDateTime\", \"Interbnk_Settlmnt_Amt\" " +
                                "from \"DocQGroup\" dq " +
                                "left join swift.\"Credit_Transfer_Transaction_Info\" ctti on dq.\"DocQGroupRefNo\" = ctti.\"DocQGroupRefNo\" " +
                                "left join swift.\"Return_Of_Funds_Credit_Transfer\" rofct on dq.\"DocQGroupRefNo\" = rofct.\"DocQGroupRefNo\" " +
                                "left join swift.\"BusinessApplicationHeader\" bah on ctti.\"MsgRefNo\" = bah.\"MsgRefNo\" or rofct.\"MsgRefNo\" = bah.\"MsgRefNo\" " +
                                "join swift.\"Swift_Message_LandingArea\" smla on ctti.\"MsgRefNo\" = smla.\"MsgRefNo\" or rofct.\"MsgRefNo\" = smla.\"MsgRefNo\" " +
                                "where smla.\"Direction\" = ? and dq.\"CountryCode\" = ? and smla.\"MessageType\" = ? " +
                                "and bah.\"Business_service\" ilike ? "
                );

// 🔥 Omit status + queue conditions for ContentValidation
                boolean isContentValidation = GroupTestName.contains("ContentValidation") ||Status.equalsIgnoreCase("OutwardsPACS004");

                if (!isContentValidation) {
                    sql.append("and dq.\"Status\" = ? and \"SourceQueueName\" like ? ");
                }

                if (!alreadyChecked.isEmpty()) {
                    sql.append("and ctti.\"End_To_End_ID\" NOT IN (");
                    sql.append(alreadyChecked.stream().map(id -> "?").collect(Collectors.joining(", ")));
                    sql.append(") ");
                }

                sql.append("order by \"CreateDateTime\" desc limit 1");

                pst1 = con.prepareStatement(sql.toString());

                // ------------------------
                //  SET QUERY PARAMETERS
                // ------------------------
                int index = 1;

                pst1.setString(index++, Direction);            // smla."Direction"
                pst1.setString(index++, country);              // dq."CountryCode"
                pst1.setString(index++, Pacs_Message_Type);    // smla."MessageType"
                pst1.setString(index++, BizSvc);               // bah."Business_service" like ?

                // For NON–ContentValidation, set the extra params
                if (!isContentValidation) {
                    pst1.setString(index++, Status);                  // dq."Status"
                    pst1.setString(index++, "%" + QueueTosearch + "%"); // SourceQueueName like ?
                }

                // Add NOT IN parameters (if any)
                for (String id : alreadyChecked) {
                    pst1.setString(index++, id);
                    System.out.println("ID has ROF done "+id);

                }
                System.out.println("Get last closed Case " + pst1);

                Thread.sleep(4000);
                rs1 = pst1.executeQuery();


                if (rs1.next()) {
                    currentEndToEndID = rs1.getString("End_To_End_ID");

                    ClosedCaseDetails.clear();
                    ClosedCaseDetails.add(currentEndToEndID);
                    ClosedCaseDetails.add(rs1.getString("Interbnk_Settlmnt_Amt"));
                    ClosedCaseDetails.add(rs1.getString("GroupCode"));
                    ClosedCaseDetails.add(rs1.getString("SourceQueueName"));
                    ClosedCaseDetails.add(rs1.getString("Business_service"));

                    //for lesaka tests after getting the case add the ID to the list
                    if (currentEndToEndID.contains("LSK")||GroupTestName.contains("ContentValidation") ||QueueTosearch.equalsIgnoreCase("CBSS.HVPP")){
                        saveCheckedIdToFile(file.getPath(), currentEndToEndID);
                    }

                    // Check if Return Of Funds is already done on this case
                    if (isReturnOfFundsDone(currentEndToEndID))
                    {
                        saveCheckedIdToFile(file.getPath(), currentEndToEndID);
                        alreadyChecked.add(currentEndToEndID);
                        continue; // get next available record
                    } else {
                        return ClosedCaseDetails; // found valid, return it
                    }
                } else {
                    break; // no more matching results
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { if (rs1 != null) rs1.close(); } catch (Exception e) {}
                try { if (pst1 != null) pst1.close(); } catch (Exception e) {}
            }
        }

        listener.addTestStep("Your End to End is " + ClosedCaseDetails.get(0) +
                " and Transaction Amount is " + ClosedCaseDetails.get(1));

        return ClosedCaseDetails; // fallback (nothing valid found)
    }
    private static boolean isReturnOfFundsDone(String endToEndId) throws SQLException {

        //need to connect to hvpp and then check for case if ROF was done
        dbConnections.connectToDataBase(DBConstants.hvppDBConnection);

        String sql = "select \toutwardDocQGroup.\"DocQGroupRefNo\"\n" +
                "\t\t,ctti.\"End_To_End_ID\"\n" +
                "\t\t,outwardRofct.\"OrgnlEndToEndId\"  \"ROFEnd_To_End_ID\"\n" +
                "\t\t,ctti.\"Instruction_ID\"\n" +
                "\t\t,outwardRofct.\"OrgnlInstructionId\" \"ROFInstruction_ID\"\n" +
                "\t\t,d.\"GroupCode\"\n" +
                "\t\t,outwardDocQGroup.\"GroupCode\" \"OutwardGroupCode\"\n" +
                "\t\t,ctti.\"Interbnk_Settlmnt_Amt\"\n" +
                "\t\t,ctti.\"Instructed_Amt_CCY\"\n" +
                "\t\t,outwardRofct.\"RtrdIntrBkSttlmAmt\"\n" +
                "\t\t,outwardRofct.\"RtrdIntrBkSttlmCcy\"\n" +
                "\t\t,cd.\"DebitAccount\" \"ROFDebitAccount\"\n" +
                "\t\t,cd.\"DebitCurrency\" \"ROFDebitCurrency\"\n" +
                "\t\t,cd.\"CreditAccount\" \"ROFCreditAccount\"\n" +
                "\t\t,cd.\"ClientAcctCCY\" \"ROFCreditCurrency\"\n" +
                "from \"DocQGroup\" d\n" +
                "inner join swift.\"Credit_Transfer_Transaction_Info\" ctti on d.\"DocQGroupRefNo\"   =ctti.\"DocQGroupRefNo\"\n" +
                "inner join swift.\"Return_Of_Funds_Credit_Transfer\" outwardRofct on outwardRofct.\"OriginalCreditTransferTransactionInfoRefNo\"  = ctti.\"CreditTransferTransactionInfoRefNo\"\n" +
                "inner join \"DocQGroup\" outwardDocQGroup on outwardRofct.\"DocQGroupRefNo\"  = outwardDocQGroup.\"DocQGroupRefNo\"\n" +
                "inner join tri.\"CaseDetail\" cd on outwardDocQGroup.\"DocQGroupRefNo\"  = cd.\"DocQGroupRefNo\"\n" +
                "where ctti.\"End_To_End_ID\" = ? \n" +
                "order by d.\"CreateDateTime\" desc;";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, endToEndId);
            ResultSet rs = stmt.executeQuery();

            //Connect back to cbss
            dbConnections.connectToDataBase(DBConstants.cbssDBConnection);

            System.out.println("isReturnOfFundsDone " + stmt);
            return rs.next(); // return true if found


        }


    }

    public static List<String> GetCasesToSimulate(String Direction, String currentCountry, String Pacs_Message_Type, String Status, String limit) {
        List<String> closedCaseIDs = new ArrayList<>();
        PreparedStatement pst1 = null;
        String baseQuery = "";
        String chargeBearer = "";

        try {
            // Build query based on message type
            if (Pacs_Message_Type.equalsIgnoreCase("PACS.008") ||
                    Pacs_Message_Type.equalsIgnoreCase("PACS.009") ||
                    Pacs_Message_Type.equalsIgnoreCase("PACS.009Cov")||
                    Pacs_Message_Type.equalsIgnoreCase("PACS.004")) {


                if(Status.equalsIgnoreCase("Payment details sent to Sanctions: Success")) {

                    baseQuery = DBConstants.Get_Case_Id_To_Simulate + " ORDER BY \"CreateDateTime\" DESC LIMIT " + limit;

                    pst1 = con.prepareStatement(baseQuery);
                    pst1.setString(1, Direction);
                    pst1.setString(2, currentCountry);
                    pst1.setString(3, Pacs_Message_Type);

                }else{
                    // Build query with multiple statuses
                    baseQuery = DBConstants.Get_Case_Id_To_Simulate
                            + "and dq.\"Status\" IN (?, ?, ?, ?,?,?) "
                            + "ORDER BY \"CreateDateTime\" DESC LIMIT " + limit;

                    pst1 = con.prepareStatement(baseQuery);
                    pst1.setString(1, Direction);
                    pst1.setString(2, currentCountry);
                    pst1.setString(3, Pacs_Message_Type);

                    // Set multiple statuses
                    pst1.setString(4, "CBSS Message Sent Out To Morongwa");
                    pst1.setString(5, "SWIFTACK");
                    pst1.setString(6, "ACCEPTED");
                    pst1.setString(7, "BYPASSED");
                    pst1.setString(8, "PASSEDVALIDATION");
                    pst1.setString(9, "Sent to TRI");

                }



            } else {
                baseQuery = DBConstants.Get_Case_Id_To_Simulate
                        + " and ctti.\"Charge_Bearer\" = ? ORDER BY \"CreateDateTime\" DESC LIMIT " + limit;

                pst1 = con.prepareStatement(baseQuery);

                Pattern pattern = Pattern.compile("(?i)(CRED|DEBT|SHAR)$");  // Case-insensitive match
                Matcher matcher = pattern.matcher(Pacs_Message_Type);
                if (matcher.find()) {
                    chargeBearer = matcher.group(1).toUpperCase();
                }

                pst1.setString(1, Direction);
                pst1.setString(2, currentCountry);
                pst1.setString(3, Pacs_Message_Type.replaceAll(chargeBearer, ""));
                pst1.setString(4, Status);
                pst1.setString(5, chargeBearer);
            }

            System.out.println("Executing query: " + pst1);
            Thread.sleep(4000); // Optional wait

            ResultSet rs1 = pst1.executeQuery();
            while (rs1.next()) {
                String e2eID = rs1.getString("End_To_End_ID");

                if (e2eID != null && !e2eID.trim().isEmpty()) {
                    closedCaseIDs.add(e2eID.trim());
                    System.out.println("Retrieved End_To_End_ID: " + e2eID);
                } else {
                    String externalRef = rs1.getString("ExternalReference");
                    if (externalRef != null && !externalRef.trim().isEmpty()) {
                        closedCaseIDs.add(externalRef.trim());
                        System.out.println("Used ExternalReference instead: " + externalRef);
                    }
                }
            }

        } catch (InterruptedException | SQLException e) {
            e.printStackTrace();
        }

        return closedCaseIDs;
    }

}

