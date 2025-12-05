package Functions.Utils;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Functions.Utils.TestRporter.BaseClass.SADCBizsvcOption;
import static Functions.Utils.TestRporter.BaseClass.TestName;

public class Constant {

    public static final String HVPP_SuspenseAccount ="";

    public static final String CBSS_MirrorAccount="";

    public static final String TEST_DATA_CAMT_MESSAGE = "src/main/resources/Outwards/TestCases/Camt_TestCases.xlsx";
    public static final String TEST_DATA_PACS002 = "src/main/resources/Outwards/TestCases/PACS002_Testcases.xlsx";

    public static final String INWARDS_TEST_DATA_CAMT_MESSAGE = "src/main/resources/Outwards/TestCases/Camt_TestCases.xlsx";
    public static final String INWARDS_TEST_DATA_PACS_ENDTOEND = "src/main/resources/Inwards/TestCases/Inwards_TestCases.xlsx";
    public static final String TEST_DATA_StructureValidation = "src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx";
    public static final String TEST_DATA_Credit_Queue = "src/main/resources/Outwards/TestCases/Credit_Queue/Credit_Queue.xlsx";

    public static HashMap<String, String> ContentValidation_TEST_DATA = new HashMap<>();
    public static HashMap<String, String> BusinessValidation_TEST_DATA = new HashMap<>();
    public static HashMap<String, String> StructureValidation_TEST_DATA = new HashMap<>();

    public static HashMap<String, String> INWARDS_TEST_DATA_ENDTOEND = new HashMap<>();

    public static HashMap<String, String> TEST_DATA_ENDTOEND = new HashMap<>();

    public static HashMap<String, String> PACS004_PACS002_FROM_BIC = new HashMap<>();
    public static HashMap<String, String> PACS004_PACS002_TO_BIC = new HashMap<>();

    public static HashMap<String, String> Outward_PACS004_PACS002_FROM_BIC = new HashMap<>();
    public static HashMap<String, String> Outward_PACS004_PACS002_TO_BIC = new HashMap<>();


    public static HashMap<String, String> Out_HVPP_Suspense = new HashMap<>();
    public static HashMap<String, String> Out_CBSS_Mirro = new HashMap<>();



    public static void setTestDataLocation() {
        ContentValidation_TEST_DATA.put("PACS008","src/main/resources/Outwards/TestCases/Content_Validation/ContentValidation_TestCases.xlsx");
        ContentValidation_TEST_DATA.put("PACS009GEN","src/main/resources/Outwards/TestCases/Content_Validation/ContentValidation_TestCases.xlsx");
        ContentValidation_TEST_DATA.put("PACS009COV","src/main/resources/Outwards/TestCases/Content_Validation/ContentValidation_TestCases.xlsx");
        ContentValidation_TEST_DATA.put("PAIN001","src/main/resources/Outwards/TestCases/Content_Validation/ContentValidation_TestCases.xlsx");
        ContentValidation_TEST_DATA.put("PACS004","src/main/resources/Outwards/TestCases/Content_Validation/ContentValidation_TestCases.xlsx");

        BusinessValidation_TEST_DATA.put("PACS008","src/main/resources/Outwards/TestCases/Business_Validation/BusinessValidation_TestCases.xlsx");
        BusinessValidation_TEST_DATA.put("PACS009GEN","src/main/resources/Outwards/TestCases/Business_Validation/BusinessValidation_TestCases.xlsx");
        BusinessValidation_TEST_DATA.put("PACS009COV","src/main/resources/Outwards/TestCases/Business_Validation/BusinessValidation_TestCases.xlsx");
        BusinessValidation_TEST_DATA.put("PAIN001","src/main/resources/Outwards/TestCases/Business_Validation/BusinessValidation_TestCases.xlsx");
        BusinessValidation_TEST_DATA.put("PACS004","src/main/resources/Outwards/TestCases/Business_Validation/BusinessValidation_TestCases.xlsx");

        StructureValidation_TEST_DATA.put("PACS008","src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx");
        StructureValidation_TEST_DATA.put("PACS009GEN","src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx");
        StructureValidation_TEST_DATA.put("PACS009COV","src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx");
        StructureValidation_TEST_DATA.put("PAIN001","src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx");
        StructureValidation_TEST_DATA.put("PACS004","src/main/resources/Outwards/TestCases/Structure_Validation/StructureValidation_TestCases.xlsx");

        TEST_DATA_ENDTOEND.put("ZAF","src/main/resources/Outwards/TestCases/ZA_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("ZMB","src/main/resources/Outwards/TestCases/ZMB_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("BWA","src/main/resources/Outwards/TestCases/BWA_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("KEN","src/main/resources/Outwards/TestCases/KEN_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("GHA","src/main/resources/Outwards/TestCases/GHA_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("TZA","src/main/resources/Outwards/TestCases/TZA_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("MUS","src/main/resources/Outwards/TestCases/MUS_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("UGA","src/main/resources/Outwards/TestCases/UGA_EndToEnd_TestCases.xlsx");
        TEST_DATA_ENDTOEND.put("SYC","src/main/resources/Outwards/TestCases/SYC_EndToEnd_TestCases.xlsx");


        Out_HVPP_Suspense.put("BWA","");
        Out_HVPP_Suspense.put("GHA","");
        Out_HVPP_Suspense.put("KEN","");
        Out_HVPP_Suspense.put("MUS","");
        Out_HVPP_Suspense.put("TZA","");
        Out_HVPP_Suspense.put("UGA","");
        Out_HVPP_Suspense.put("ZMB","");
        Out_HVPP_Suspense.put("ZAF","");

        Out_CBSS_Mirro.put("BWA","");
        Out_CBSS_Mirro.put("GHA","");
        Out_CBSS_Mirro.put("KEN","");
        Out_CBSS_Mirro.put("MUS","");
        Out_CBSS_Mirro.put("TZA","");
        Out_CBSS_Mirro.put("UGA","");
        Out_CBSS_Mirro.put("ZMB","");
        Out_CBSS_Mirro.put("ZAF","");


    }
    public static void set_Inwards_TestDataLocation() {
        INWARDS_TEST_DATA_ENDTOEND.put("BWA","src/main/resources/Inwards/TestCases/BWA_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("KEN","src/main/resources/Inwards/TestCases/KEN_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("GHA","src/main/resources/Inwards/TestCases/GHA_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("MUS","src/main/resources/Inwards/TestCases/MUS_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("TZA","src/main/resources/Inwards/TestCases/TZA_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("ZAF","src/main/resources/Inwards/TestCases/ZA_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("ZMB","src/main/resources/Inwards/TestCases/ZMB_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("UGA","src/main/resources/Inwards/TestCases/UGA_Inwards_E2E_TestCases.xlsx");
        INWARDS_TEST_DATA_ENDTOEND.put("SYC","src/main/resources/Inwards/TestCases/SYC_Inwards_E2E_TestCases.xlsx");

        PACS004_PACS002_FROM_BIC.put("BWA","SCHBBWGXXXX");
        PACS004_PACS002_TO_BIC.put("BWA","BARCBWGXXXX");

        PACS004_PACS002_FROM_BIC.put("KEN","SBMKKENAXXX");
        PACS004_PACS002_TO_BIC.put("KEN","BARCKENXXXX");

        PACS004_PACS002_FROM_BIC.put("ZAF","FIRNZAJJXXX");
        PACS004_PACS002_TO_BIC.put("ZAF","ABSAZAJJXXX");

        PACS004_PACS002_FROM_BIC.put("TZA","BARCTZT0XXX");
        PACS004_PACS002_TO_BIC.put("TZA","ABSAZAJJXXX");

        PACS004_PACS002_FROM_BIC.put("ZMB","SCBLZMLXXXX");
        PACS004_PACS002_TO_BIC.put("ZMB","BARCZMLXXXX");

        PACS004_PACS002_FROM_BIC.put("GHA","SBICGHACXXX");
        PACS004_PACS002_TO_BIC.put("GHA","BARCGHA0XXX");

        PACS004_PACS002_FROM_BIC.put("MUS","BKONMUM0XXX");
        PACS004_PACS002_TO_BIC.put("MUS","BARCMUM0XXX");

        PACS004_PACS002_FROM_BIC.put("UGA","SCBLUGK0XXX");
        PACS004_PACS002_TO_BIC.put("UGA","BARCUGK0XXX");

        PACS004_PACS002_FROM_BIC.put("SYC","BARCSCS0XXX");
        PACS004_PACS002_TO_BIC.put("SYC","SBICGHA0XXX");


        Outward_PACS004_PACS002_FROM_BIC.put("BWA","BARCBWG0XXX");
        Outward_PACS004_PACS002_TO_BIC.put("BWA","ABSAZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("KEN","BARCKENXXXX");
        Outward_PACS004_PACS002_TO_BIC.put("KEN","CORUTZTZXXX");

        Outward_PACS004_PACS002_FROM_BIC.put("ZAF","ABSAZAJ0XXX");
        Outward_PACS004_PACS002_TO_BIC.put("ZAF","FIRNZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("TZA","BARCTZTZXXX");
        Outward_PACS004_PACS002_TO_BIC.put("TZA","FIRNZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("ZMB","BARCZMLXXXX");
        Outward_PACS004_PACS002_TO_BIC.put("ZMB","ABSAZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("GHA","BARCGHA0XXX");
        Outward_PACS004_PACS002_TO_BIC.put("GHA","ABSAZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("MUS","BKONMUM0XXX");
        Outward_PACS004_PACS002_TO_BIC.put("MUS","ABSAZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("UGA","BARCUGK0XXX");
        Outward_PACS004_PACS002_TO_BIC.put("UGA","ABSAZAJ0XXX");

        Outward_PACS004_PACS002_FROM_BIC.put("SYC","BARCSCSCXXX");
        Outward_PACS004_PACS002_TO_BIC.put("SYC","ABSAZAJ0XXX");
    }




    // Method to extract matching SchemeIDs based on the user's criteria
    public static List<String> extractMatchingSchemeIDs(String messageTypeCriteria,
                                                        String businessServiceCodeCriteria, String currencyCriteria,
                                                        String countryCodeCriteria, String mopidCriteria) {
        List<String> schemeIDs = new ArrayList<>();  // List to hold matching SchemeIDs
        String jsonFilePath = "";

        // Print the input criteria
        System.out.println("MessageType: " + messageTypeCriteria);
        System.out.println("BusinessServiceCode: " + businessServiceCodeCriteria);
        System.out.println("IntrBkSttlmAmtCcy: " + currencyCriteria);
        System.out.println("CountryCode: " + countryCodeCriteria);
        System.out.println("MOPID: " + mopidCriteria);

        // Select the correct JSON file based on the country
        if (countryCodeCriteria.equalsIgnoreCase("ZAF")) {
            jsonFilePath = "src/main/resources/Outwards/Testdata/SA_SchemeIDMOPIDSetup.json";  // Replace with the actual path to your JSON file
        } else {
            jsonFilePath = "src/main/resources/Outwards/Testdata/ARO_SchemeIDMOPIDSetup.json";  // Replace with the actual path to your JSON file
        }

        try {
            // Use JsonPath to read the JSON file
            String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            // Print out the document to debug the structure
            //System.out.println("Document: " + jsonString);

            String jsonPathExpression = "$.select";
            JSONArray records = JsonPath.read(jsonString, jsonPathExpression);

            // Iterate through the records
            for (Object recordObj : records) {
                // Convert the record to a Map
                @SuppressWarnings("unchecked")
                java.util.Map<String, String> record = (java.util.Map<String, String>) recordObj;

                String messageType = record.get("MessageType");
                String businessServiceCode = record.get("BusinessServiceCode");
                String intrBkSttlmAmtCcy = record.get("IntrBkSttlmAmtCcy");
                String countryCode = record.get("CountryCode");
                String mopid = record.get("MOPID");
                String schemeID = record.get("SchemeID");

                // Split the MessageType and BusinessServiceCode by commas and check for criteria
                boolean messageTypeMatch = false;
                if (messageType != null) {
                    for (String type : messageType.split(",\\s*")) {
                        if (type.toLowerCase().contains(messageTypeCriteria.toLowerCase())) {
                            messageTypeMatch = true;
                            break;
                        }
                    }
                }

                boolean businessServiceCodeMatch = false;
                if (businessServiceCode != null) {
                    for (String serviceCode : businessServiceCode.split(",\\s*")) {
                        if (serviceCode.contains(businessServiceCodeCriteria)) {
                            businessServiceCodeMatch = true;
                            break;
                        }
                    }
                }

                // Check if the record matches the specified conditions
                if (messageTypeMatch &&
                        (businessServiceCode != null && businessServiceCodeMatch) &&
                        (intrBkSttlmAmtCcy != null && intrBkSttlmAmtCcy.equalsIgnoreCase(currencyCriteria)) &&
                        (countryCode != null && countryCode.equalsIgnoreCase(countryCodeCriteria)) &&
                        (mopid != null && mopid.equalsIgnoreCase(mopidCriteria))) {

                    // Add the SchemeID to the list if all criteria match
                    schemeIDs.add(schemeID);
                }
            }

        } catch (IOException  e) {
            e.printStackTrace();  // Handle IO or JSON path errors
        }

        return schemeIDs;  // Return the list of matching SchemeIDs
    }


        // Method to extract matching BizSvcCode based on criteria
        public static String extractMatchingBizSvcCodes(String messageTypeCriteria,
                                                        String marketStructureCriteria,
                                                        String countryCodeCriteria,String BizExtractType) {
            List<String> bizSvcCodes = new ArrayList<>();  // List to hold matching BizSvcCodes
            String jsonFilePath = "";

            // Print the input criteria
            System.out.println("MessageType: " + messageTypeCriteria.toUpperCase());
            System.out.println("MarketStructure: " + marketStructureCriteria);
            System.out.println("CountryCode: " + countryCodeCriteria);



            // Select the correct JSON file based on the country
            if(BizExtractType.contains("MessageGen")) {
                jsonFilePath = "src/main/resources/Outwards/Testdata/MessageGen_BusinessSvc.json";
            }else{
                jsonFilePath = "src/main/resources/Outwards/Testdata/BusinessServiceCodes.json";
            }
            try {
                // Read the JSON file content as a String
                String jsonString = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

                // JsonPath expression to get the MessageType section from the JSON
                Map<String, Object> records = JsonPath.read(jsonString, "$");  // Read the entire JSON as a Map

                // Check if the messageTypeCriteria exists in the JSON
                if (records.containsKey(messageTypeCriteria.toUpperCase())) {
                    Map<String, Object> messageTypeData = (Map<String, Object>) records.get(messageTypeCriteria.toUpperCase());

                    // Check if the marketStructureCriteria exists in the messageTypeData
                    if (messageTypeData.containsKey(marketStructureCriteria)) {
                        Map<String, Object> marketStructureData = (Map<String, Object>) messageTypeData.get(marketStructureCriteria);

                        // Check if the countryCodeCriteria exists in the marketStructureData
                        if(marketStructureCriteria.equalsIgnoreCase("RTGSSADC") && !SADCBizsvcOption.isEmpty()){
                            countryCodeCriteria =SADCBizsvcOption;
                        }else if(marketStructureCriteria.equalsIgnoreCase("RTGSSADC") && SADCBizsvcOption.isEmpty()){
                            countryCodeCriteria ="Old";
                        }

                        if (marketStructureData.containsKey(countryCodeCriteria)) {
                            String bizSvcCode = (String) marketStructureData.get(countryCodeCriteria);
                            bizSvcCodes.add(bizSvcCode);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();  // Handle IO or JSON path errors
            }

            return bizSvcCodes.get(0);  // Return the list of matching BizSvcCodes
        }
}


