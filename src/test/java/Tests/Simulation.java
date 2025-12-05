package Tests;

import Functions.MQDBConnections.DBConstants;
import Functions.MQDBConnections.dbConnections;
import Functions.Utils.TestRporter.BaseClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static Functions.MQDBConnections.dbConnections.GetCasesToSimulate;
import static Functions.Utils.CommonMethods.*;

public class Simulation extends BaseClass {


    ArrayList<String> end2EndIdIDList;
    @Test(groups = {"Regression","ZMBRegression","BWARegression","KENRegression","GHARegression","TZARegression"})

    public void Outwards_SimulateCases() throws Exception {
        String systemType ="";
        //String Status ="Payment details sent to Sanctions: Success";
        String Status ="Morongwa Settlement message sent to Morongwa Settlement queue: Success";
       // String Status ="Sent to TRI.";

        String MessageType ="PACS.004";

        listener.addTestSuite("Simulation Test ", "SIT");
        listener.addTestCase("Simulation Test ", "Simulation");

         if(Status.equalsIgnoreCase("Payment details sent to Sanctions: Success")||Status.equalsIgnoreCase("Sent to TRI.")) {

            dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
        }else{
            dbConnections.connectToDataBase(DBConstants.cbssDBConnection);
        }

        //To Manually Add Cases use the code below and change Line 49 from CaseSentMorongwaIDs to CaseSentMorongwaIDs_Manual
        List<String> CaseSentMorongwaIDs = new ArrayList<>();
       // CaseSentMorongwaIDs.add("NKPZA66799"); //this field can be duplicated as much as you want so that you can add all the End to End ID you want to simulate

        // Retrieve all E2E IDs where CBSS message was sent to Morongwa
        if(CaseSentMorongwaIDs.isEmpty()) {
            CaseSentMorongwaIDs = GetCasesToSimulate(
                    "OUT",                  // Direction IN or OUT
                    country,               // Current country -Country is fetched from POM file
                    MessageType,             // Message type Ensure that Message type is , "PACS.008 /PACS.009 /PACS009Cov
                    Status,                    // State to search
                    "5"                  // Limit -Number Of cases/ ID that you want to fetch from DB
            );
        }
        // Loop through all retrieved IDs and simulate SETTLEMENTSIM or Sanctions Response for each
        for (String e2eID : CaseSentMorongwaIDs) {
            if (e2eID != null && !e2eID.trim().isEmpty()) {
                System.out.println("✔️✔️ Simulating settlement for EndToEndID:: " + e2eID);

                if(Status.equalsIgnoreCase("Payment details sent to Sanctions: Success")){
                    Simulate_responses(e2eID, MessageType, "SanctionsHVPP");
                }
                else
                    if(Status.equalsIgnoreCase("Morongwa Settlement message sent to Morongwa Settlement queue: Success")) {
                        /**
                        Note :: if you want to simulate PACS002 intergrationSystem value should be  SETTLEMENTSIM
                         but if you want to simulate camt.054 then make it SETTLEMENTSIM54

                         */

                    Simulate_responses(e2eID, MessageType, "SETTLEMENTSIM");
                }else{
                    Simulate_responses(e2eID, MessageType, "BAPSTRISIM");

                }
            }
        }
    }
}
