package Functions.Utils;
import Functions.MQDBConnections.DBConstants;
import Functions.MQDBConnections.MQConnections;
import Functions.MQDBConnections.dbConnections;
import Functions.Narratives.*;
import Functions.Utils.TestRporter.BaseClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static Functions.MQDBConnections.MQConnections.*;
import static Functions.MQDBConnections.MQConnections.HVPP_SANCTIONS_RESPONSE_MESSAGE;
import static Functions.MQDBConnections.SearchLog.errorStatuses;
import static Functions.MQDBConnections.dbConnections.*;
import static Functions.Utils.Constant.*;
import java.util.Map;
import static Functions.Utils.RatesAndCalculation.*;



public class CommonMethods extends BaseClass {
    static String end2EndId, transactionId, IdentifierId, UniqueBisSvCode, Original_IntrBankSttlmDate, GroupHeader_Original_IntrBankSttlmDate, Original_CrDate_Time, msgId, bizMsgId, instructionID, Ustrd,
            uetr, Msgid, BizmsgId, Orignaluetr = null;
    static String IntrBankSttlmAmt, IntrBankSttlmAmtCcy, intraBankSettlementDate, UniqueMsgNameID, SetTimeonMessage, UniqueMsgId,UniqueBisMsgId, CdtDbtInd, MatchingPropriety;
    static String XML_BusinessSerivceCodeValue, ExpectedBizSvcCode = "";

    String[] output;

    public static Map<String, String> columnMap = new HashMap<String, String>();
    public static Map<String, String> columnMapTestData = new HashMap<String, String>();
    public static Map<String, String> columnMapSimulation = new HashMap<String, String>();

    public String[] split(String str, String delimiter) {

        for (int i = 0; i < str.length(); i++) {
            output = str.split(delimiter);
        }
        System.out.println(output.toString());
        return output;
    }

    //check if the payment is a vostro or not
    //@STORY ::469688
    public static boolean isBICValidVostro(String bic, String country) {
        boolean match =false;

        // Handle null or invalid country
        if (country == null || country.length() < 2) {
            System.out.println("[BIC CHECK] Country code is null or too short: " + country + " → Defaulting to non-match.");
            return false;
        }

        // Handle null, empty, or too short BIC
        if (bic == null || bic.trim().isEmpty() || bic.length() < 6) {
            System.out.println("[BIC CHECK] BIC is null, empty, or too short: " + bic
                    + " → Will count as non-matching for LESAKA logic.");
            //return false; // count as non-match
        } else {
            // Extract the 5th and 6th characters from the BIC
            String bicCountry = bic.substring(4, 6);
            String countryCode = country.substring(0, 2);

            // Check if BIC matches country
             match = bicCountry.equalsIgnoreCase(countryCode);

            if (match) {
                System.out.println("[BIC CHECK] ✅ BIC " + bic + " matches country " + countryCode);
            } else {
                System.out.println("[BIC CHECK] ❌ BIC " + bic + " does NOT match country " + countryCode
                        + " (found " + bicCountry + ") → Will go to LESAKA.");
            }
        }
        return match;
    }


    //Convert Bulk PACS TO String
    public static String convertBulkPacsToString(File file) throws IOException {
        String fileContent = new String();
        HashMap captureValues = new HashMap<String, String>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // fileContent = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent = fileContent.concat(line) + System.lineSeparator();
            }
            if (fileContent.contains("BIZMSGID") && fileContent.contains("MSGID")
                    //First Set
                    && fileContent.contains("uniqueInstrId001")
                    && fileContent.contains("uniqueEndToEndId001")
                    && fileContent.contains("uniqueTransactionId001")
                    && fileContent.contains("uniqueUETR001")
                    //Second Set
                    && fileContent.contains("uniqueInstrId002") &&
                    fileContent.contains("uniqueEndToEndId002")
                    && fileContent.contains("uniqueTransactionId002")
                    && fileContent.contains("uniqueUETR002")
                    //Third Set
                    && fileContent.contains("uniqueInstrId003") &&
                    fileContent.contains("uniqueEndToEndId003")
                    && fileContent.contains("uniqueTransactionId003")
                    && fileContent.contains("uniqueUETR003")
            ) {
                //Instructed ID
                instructionID = "SIT" + "I" + generateUniqueString();
                captureValues.put("InstructedID", instructionID + "001");
                fileContent = fileContent.replaceAll("uniqueInstrId001", instructionID + "001");

                //End2EndID
                end2EndId = "E2E" + "ID" + generateUniqueString();
                captureValues.put("End2EndID", end2EndId + "001");
                fileContent = fileContent.replaceAll("uniqueEndToEndId001", end2EndId + "001");

                //Transaction ID
                transactionId = "TRANS" + "TD" + generateUniqueString();
                captureValues.put("TransactionID", transactionId + "001");
                fileContent = fileContent.replaceAll("uniqueTransactionId001", transactionId + "001");


                uetr = UUID.randomUUID().toString();
                captureValues.put("uniqueUETR001", uetr);
                fileContent = fileContent.replaceAll("uniqueUETR001", uetr);
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //Instructed ID
                instructionID = "SIT" + "I" + generateUniqueString();
                captureValues.put("InstructedID", instructionID + "002");
                fileContent = fileContent.replaceAll("uniqueInstrId002", instructionID + "002");

                //End2EndID
                end2EndId = "E2E" + "ID" + generateUniqueString();
                captureValues.put("End2EndID", end2EndId + "002");
                fileContent = fileContent.replaceAll("uniqueEndToEndId002", end2EndId + "002");


                //Transaction ID
                transactionId = "TRANS" + "TD" + generateUniqueString();
                captureValues.put("TransactionID", transactionId + "002");
                fileContent = fileContent.replaceAll("uniqueTransactionId002", transactionId + "002");

                uetr = UUID.randomUUID().toString();
                captureValues.put("uniqueUETR002", uetr);
                fileContent = fileContent.replaceAll("uniqueUETR002", uetr);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //Instructed ID
                instructionID = "SIT" + "I" + generateUniqueString();
                captureValues.put("InstructedID", instructionID + "003");
                fileContent = fileContent.replaceAll("uniqueInstrId003", instructionID + "003");

                //End2EndID
                end2EndId = "E2E" + "ID" + generateUniqueString();
                captureValues.put("End2EndID", end2EndId + "003");
                fileContent = fileContent.replaceAll("uniqueEndToEndId003", end2EndId + "003");

                //Transaction ID
                transactionId = "SIT" + "T" + generateUniqueString();
                captureValues.put("TransactionID", transactionId + "003");
                fileContent = fileContent.replaceAll("uniqueTransactionId003", transactionId + "003");

                uetr = UUID.randomUUID().toString();
                captureValues.put("uniqueUETR003", uetr);
                fileContent = fileContent.replaceAll("uniqueUETR003", uetr);

                //Intra Bank Settlement Date
                intraBankSettlementDate = getTodayDate() + "T";
                captureValues.put("IntrBankStlDate", intraBankSettlementDate);
                fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);

                //Intra Bank Settlement Date
                SetTimeonMessage = getTodayDate();
                captureValues.put("DtTm", SetTimeonMessage);
                fileContent = fileContent.replaceAll("HH:MM:SS", SetTimeonMessage);

                msgId = "SIT" + "M" + generateUniqueString();
                captureValues.put("MsgID", msgId);
                fileContent = fileContent.replaceAll("MSGID", msgId);

                bizMsgId = "SIT" + "BizMsg" + generateUniqueString();
                captureValues.put("BIZMSGID", bizMsgId);
                fileContent = fileContent.replaceAll("BIZMSGID", bizMsgId);

                try {
                    fileContent = fileContent.replaceAll("PPPP-PP-PP", getPreviousDate());
                } catch (ParseException e) {
                    System.out.println("Invalid date string");
                    e.printStackTrace();
                }
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent + "::" + instructionID;
    }

    public static String extractPacsTye(String str) {
        String getGroup = "";

        Pattern pattern = Pattern.compile("PACS.([\\d]+COV|[\\d]+)");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            getGroup = matcher.group();
        }

        return getGroup;
    }

    //Convert PACS Messages to the String
    public static String convertPacsToString(File file, Map<String,String> columnMap) throws IOException {

        String fileContent = new String();
        columnMapTestData=columnMap;
        String MessageType = file.getName().replace(".xml", "");

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // fileContent = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent = fileContent.concat(line) + System.lineSeparator();
            }

            /**
             *  This is to enable framework to use one message for both Inwards and outwards
             */
            switch (SystemUsed){
                case "Inwards":
                    //Handle Metadata PI
                    fileContent = fileContent.replaceAll("PICaseNumber", "");
                    fileContent = fileContent.replaceAll("CustData_Content","");
                    fileContent = fileContent.replaceAll("RmtInf_Strd_RfrdDocInf_Nb","");
                    fileContent = fileContent.replaceAll("Initiating_System","SWIFT");
                    fileContent = fileContent.replaceAll("DirectionTag","INCOMING");
                    fileContent = fileContent.replaceAll("DirectionTag","INCOMING");

                    break;

                case "Outwards":

                    //Handle Metadata PI
                    fileContent = fileContent.replaceAll("PICaseNumber", "PICaseNumber");
                    fileContent = fileContent.replaceAll("CustData_Content","uniqueInstrId");
                    fileContent = fileContent.replaceAll("DirectionTag","OUTGOING");

                    break;
            }
            //Handle Tags only used in LESAKA
            if(columnMap.getOrDefault("System_From","").contains("LESAKA")){

                fileContent = fileContent.replaceAll("CBSS_scheme_ID", columnMap.getOrDefault("CBSS_scheme_ID", ""));
                fileContent = fileContent.replaceAll("CBSS_schemeID_Content", columnMap.getOrDefault("CBSS_scheme_ID_Content", ""));

            }else{
                fileContent = fileContent.replaceAll("CBSS_scheme_ID", "");
                fileContent = fileContent.replaceAll("CBSS_schemeID_Content", "");

            }

            //***************************************Normal Transaction********************************************************************************
            if (fileContent.contains("uniqueEndToEndId") || fileContent.contains("uniqueTransactionId") || fileContent.contains("uniqueInstrId") || fileContent.contains("uniqueMsgIdId") || fileContent.contains("uniqueUETR")) {
               if(country.equalsIgnoreCase("SYC")){
                   fileContent = fileContent.replaceAll("Business_Entity", "BARCSC");
                   fileContent = fileContent.replaceAll("MsgWrapperCountry", "SC");
               }else {
                   fileContent = fileContent.replaceAll("Business_Entity", "BARC" + country.substring(0, 2));
                   fileContent = fileContent.replaceAll("MsgWrapperCountry", country.substring(0, 2));
               }
                //End2EndID only applicable for inwards
                //TesData.get(40) is End to EndID on the spreadsheet
                //and the tags above are only applicable to inwards
                if (file.getName().replace(".xml", "").contains("PACS009") && columnMap.get("End to EndID").contains("I81")) {
                        if (columnMap.getOrDefault("Test","").contains("LESAKA")) {
                            end2EndId = "LSKA"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                        } else if (columnMap.getOrDefault("Test","").contains("SADC")) {
                            end2EndId = "SADC"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                        } else if (columnMap.getOrDefault("Test","").contains("BAPS")) {
                            end2EndId = "BAPS"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                        } else {
                            end2EndId = country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");
                        }
                }
                else if (file.getName().replace(".xml", "").contains("SECL10"))
                {
                end2EndId = "SBK" + generateUniqueString();
                }
                else {
                    if (columnMap.getOrDefault("Test","").contains("LESAKA")) {
                        end2EndId = "LSKA"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                    } else if (columnMap.getOrDefault("Test","").contains("SADC")) {
                        end2EndId = "SADC"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                    } else if (columnMap.getOrDefault("Test","").contains("BAPS")) {
                        end2EndId = "BAPS"+country + generateUniqueString() + columnMap.getOrDefault("End to EndID","");

                    } else {
                        end2EndId = "SIT" +country + generateUniqueString();
                    }
                    //BWa and GHA only require end to end to be less than 16 characters
                    if (country.equalsIgnoreCase("BWA") || country.equalsIgnoreCase("GHA") || country.equalsIgnoreCase("MUS") ) {
                        if (columnMap.getOrDefault("Test","").contains("LESAKA")) {
                            end2EndId = "LSK" + generateUniqueString() + columnMap.getOrDefault("End to EndID","");
                        } else {
                            end2EndId = "SIT" + generateUniqueString();
                        }
                    }
                }

                if (country.equalsIgnoreCase("MOZ") ) {
                    if (columnMap.getOrDefault("Test","").contains("LESAKA")) {
                        end2EndId = "LSK" + generateUniqueString() + columnMap.getOrDefault("End to EndID", "");
                    }
                        end2EndId = "SITM" + generateUniqueString();
                         msgId = end2EndId;
                        fileContent = fileContent.replaceAll("uniqueMsgIdId", msgId);

                        BizmsgId = end2EndId;
                        fileContent = fileContent.replaceAll("uniqueBIZMsgId", BizmsgId);
                    } else {
                        msgId = "BK"  + generateUniqueString();
                        fileContent = fileContent.replaceAll("uniqueMsgIdId", msgId);

                        BizmsgId = "BK"+ generateUniqueString();
                        fileContent = fileContent.replaceAll("uniqueBIZMsgId", BizmsgId);

                }


                fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId.trim());

                //Transaction ID
                transactionId = "TXD" + generateUniqueString();
                fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);

                //uniqueIdentifier ID
                IdentifierId = "SIT/Automation/" + generateUniqueString();
                fileContent = fileContent.replaceAll("unique_Identifier_ID", IdentifierId);

                //Instructed ID
                //TesData.get(41) is End to InstructedID on the spreadsheet
                //and the tags above are only applicable to inwards
                if (file.getName().replace(".xml", "").contains("PACS009") && columnMap.getOrDefault("InstructedID","").contains("I81")) {

                    end2EndId = "INS" + generateUniqueString() + columnMap.getOrDefault("InstructedID","");
                } else {
                    instructionID = "INS" + generateUniqueString();
                    fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                }

                //===============================Handle BizSvc code======================================
                if(columnMap.getOrDefault("BizSvc","").isEmpty()){
                    String marketStructure ="";
                    if(columnMap.getOrDefault("Test","").contains("SADC")){
                        marketStructure ="RTGSSADC";
                    }else{
                        marketStructure ="RTGSDOM";

                    }
                    UniqueBisSvCode =extractMatchingBizSvcCodes(MessageType,marketStructure,country,"DropMessage");
                    fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);
                }
                else{
                    UniqueBisSvCode =columnMap.getOrDefault("BizSvc","");
                    fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);
                }


                //Intra Bank Settlement Date | Settlement Account | Settlement Time Required | Underlying Creditor Agent Account
                if (columnMap.get("Test").contains("SADC") || columnMap.get("BizSvc").contains("sarb.sadc")) {
                    fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "");

                    fileContent = fileContent.replaceAll("settlementTimeRequired", "");

                    fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "");
                    fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");

                    fileContent = fileContent.replaceAll("underlyingCreditorAgentAccount", "");
                    fileContent = fileContent.replaceAll("ServiceLevel_Properiety", "");
                    fileContent = fileContent.replaceAll("ServiceLevel_Code", "G001");

                    //Unmatched CMT054
                    fileContent = fileContent.replaceAll("CMT054SADC_DT", "YYYY-MM-DD");
                    fileContent = fileContent.replaceAll("CMT054ValueDate_DtTm", "");

                } else {


                    fileContent = fileContent.replaceAll("underlyingCreditorAgentAccount", "196546057");
                    fileContent = fileContent.replaceAll("ServiceLevel_Properiety", "TTC:1030,REC:0607");

                    //this is for Zambia ,need to create if statement, if we will have different countries
                    if (country.equalsIgnoreCase("ZMB")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "ZIS");
                    } else if (country.equalsIgnoreCase("TZA")) {
                        //this is for Tanzania ,need to create else if statement, if we will have different countries
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "TIS");
                    }
                    else if (country.equalsIgnoreCase("MUS")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "MUP");
                    }
                    else if (country.equalsIgnoreCase("KEN")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "KPS");
                    }
                    else if (country.equalsIgnoreCase("BWA")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "BIS");
                    }
                    else if (country.equalsIgnoreCase("GHA")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "GIS");
                    }
                    else if (country.equalsIgnoreCase("UGA")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "UIS");
                    }    else if (country.equalsIgnoreCase("SYC")) {
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "SIS");
                    }    else{      //Here we are making sure that clearing system is not available in SA and also Settlement account is visible

                        if (country.equalsIgnoreCase("ZAF")) {
                            if(UniqueBisSvCode.contains("sarb.samos.02") || UniqueBisSvCode.contains("sarb.samos.cov.02")){
                                fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "");
                                fileContent = fileContent.replaceAll("settlementTimeRequired", "");

                            }else {
                                fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "YYYY-MM-DD");
                                fileContent = fileContent.replaceAll("settlementTimeRequired", "");

                              //  fileContent = fileContent.replaceAll("settlementTimeRequired", "22:00:00.00");

                            }
                            fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");
                            fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "50101552");

                        }

                    }

                    //This is to remove fields that are not applicable to other countries and its only for SA
                    fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "");
                    fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");
                    fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "");
                    fileContent = fileContent.replaceAll("settlementTimeRequired", "");

                    //=============================================================================================

                    fileContent = fileContent.replaceAll("ServiceLevel_Code", "GOO1");

                    // Camt.054 specific fields
                    fileContent = fileContent.replaceAll("CMT054SADC_DT", "");
                    fileContent = fileContent.replaceAll("CMT054ValueDate_DtTm", "YYYY-MM-DDT10:23:06+02:00");
                }

                intraBankSettlementDate = getTodayDate();
                fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);




                //============================================================================================

                switch (file.getName().replace(".xml", "")) {
                    case "SECL10Debit":
                    case "SECL10Credit":
                        fileContent = fileContent.replaceAll("FROM_BIC", columnMap.getOrDefault("FROM_BIC",""));
                        fileContent = fileContent.replaceAll("To_BIC", columnMap.getOrDefault("TO_BIC",""));
                        fileContent = fileContent.replaceAll("BizSeriveCode", columnMap.getOrDefault("BizSvc",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMap.getOrDefault("MktPrctc_Regy",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Id", columnMap.getOrDefault("MktPrctc_Id",""));

                        fileContent = fileContent.replaceAll("CrdtDbtInd", columnMap.getOrDefault("CdtDbtInd",""));
                        fileContent = fileContent.replaceAll("ClrMm_Bic", columnMap.getOrDefault("ClrMm_Bic",""));

                        fileContent = fileContent.replaceAll("ClrSgmt_ProprietaryID", columnMap.getOrDefault("ClrSgmt_ProprietaryID",""));

                        fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""));

                        fileContent = fileContent.replaceAll("IntrBankSettlmAmt", columnMap.getOrDefault("IntrBkSttlmAmt",""));

                        uetr = UUID.randomUUID().toString();
                        fileContent = fileContent.replaceAll("uniqueUETR", uetr);


                        fileContent = fileContent.replaceAll("scheme_ID_Code", columnMap.getOrDefault("schemeID",""));

                        fileContent = fileContent.replaceAll("DlvrgSttlmPties_BIC", columnMap.getOrDefault("DlvrgSttlmPties_BIC",""));
                        fileContent = fileContent.replaceAll("DlvrgSttlmPties_AccountID", columnMap.getOrDefault("DlvrgSttlmPties_AccountID",""));
                        fileContent = fileContent.replaceAll("RcvgSttlmPties_AnyBIC", columnMap.getOrDefault("RcvgSttlmPties_AnyBIC",""));
                        fileContent = fileContent.replaceAll("RcvgSttlmPties_AccountID", columnMap.getOrDefault("RcvgSttlmPties_AccountID",""));
                        break;
                    case "CAMT019":
                    case "CAMT004":
                    case "Admi004":
                        fileContent = fileContent.replaceAll("MsgDefIdr_Tag", columnMap.getOrDefault("MsgDefIdr_Tag",""));

                        fileContent = fileContent.replaceAll("FROM_BIC", columnMap.getOrDefault("FROM_BIC",""));
                        fileContent = fileContent.replaceAll("To_BIC", columnMap.getOrDefault("TO_BIC",""));

                        fileContent = fileContent.replaceAll("BizSeriveCode", columnMap.getOrDefault("BizSvc",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMap.getOrDefault("MktPrctc_Regy",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Id", columnMap.getOrDefault("MktPrctc_Id",""));

                        fileContent = fileContent.replaceAll("UniqPropriety", columnMap.getOrDefault("PrtryID",""));

                        //Intra Bank Settlement Date
                        SetTimeonMessage = getNowTime();
                        fileContent = fileContent.replaceAll("HH:MM:SS", SetTimeonMessage);
                        fileContent = fileContent.replaceAll("EventCode", columnMap.getOrDefault("EventCode",""));

                        fileContent = fileContent.replaceAll("EventParam", columnMap.getOrDefault("EventParam",""));
                        fileContent = fileContent.replaceAll("EventParaDate",getTodayDate().replaceAll("-","") );



                        if(country.equalsIgnoreCase("SYC")){
                            fileContent = fileContent.replaceAll("Business_Entity", "BARCSC");
                            fileContent = fileContent.replaceAll("MsgWrapperCountry", "SC");
                        }else {
                            fileContent = fileContent.replaceAll("Business_Entity", "BARC" + country.substring(0, 2));
                            fileContent = fileContent.replaceAll("MsgWrapperCountry", country.substring(0, 2));
                        }

                        break;
                    case "CAMT.054":

                        fileContent = fileContent.replaceAll("FROM_BIC", columnMap.getOrDefault("FROM_BIC",""));
                        fileContent = fileContent.replaceAll("To_BIC", columnMap.getOrDefault("TO_BIC",""));
                        fileContent = fileContent.replaceAll("BizSeriveCode", columnMap.getOrDefault("BizSvc",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMap.getOrDefault("MktPrctc_Regy",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Id", columnMap.getOrDefault("MktPrctc_Id",""));

                        fileContent = fileContent.replaceAll("ReportingSrc", columnMap.getOrDefault("Reporting Source",""));
                        fileContent = fileContent.replaceAll("DomesticSettlementAcc", columnMap.getOrDefault("Domestic_Settlement_Acc",""));
                        fileContent = fileContent.replaceAll("BkTxCd_Propriety_Code", columnMap.getOrDefault("BkTxCd_Propriety_Code",""));
                        fileContent = fileContent.replaceAll("MsgRcpt_AnyBIC", columnMap.getOrDefault("MsgRcpt_AnyBIC",""));

                        fileContent = fileContent.replaceAll("Domn_DomnCD", columnMap.getOrDefault("Domn_Domain_Code",""));
                        fileContent = fileContent.replaceAll("Domn_FamilyCd", columnMap.getOrDefault("Domn_Family_Code",""));
                        fileContent = fileContent.replaceAll("Domn_SubFamilyCode", columnMap.getOrDefault("Domn_Sub_Family_Code",""));
                        fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""));
                        fileContent = fileContent.replaceAll("CrdtDbtInd", columnMap.getOrDefault("CreditDebit_Indicator",""));

                        if(columnMap.getOrDefault("BizSvc","").equalsIgnoreCase("sarb.samos.01")){
                            //Unmatched CMT054
                            fileContent = fileContent.replaceAll("CMT054ValueDate_DT", "YYYY-MM-DD");
                            fileContent = fileContent.replaceAll("CMT054ValueDate&Time_DtTm", "");


                        }else{
                            fileContent = fileContent.replaceAll("CMT054ValueDate_DT", "");
                            fileContent = fileContent.replaceAll("CMT054ValueDate&Time_DtTm", "YYYY-MM-DDT10:23:06+02:00");


                        }
                        fileContent = fileContent.replaceAll("uniqueUETR", "");

                        fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMap.getOrDefault("RmtInf_Ustrd",""));

                        fileContent = fileContent.replaceAll("Purpose_Prop", columnMap.getOrDefault("Purpose_Prop",""));
                        fileContent = fileContent.replaceAll("Purpose_Code", columnMap.getOrDefault("Purpose_Code",""));

                        break;
                    default:

                        fileContent = fileContent.replaceAll("System_From", columnMap.getOrDefault("System_From",""));
                        fileContent = fileContent.replaceAll("System_To", columnMap.getOrDefault("System_To",""));
                        /**
                         * This is for PAIN001 and putting the correct system to
                         */
                        fileContent = fileContent.replaceAll("System_To_PAIN001", columnMap.getOrDefault("System_To","") + country);

                        fileContent = fileContent.replaceAll("FROM_BIC", columnMap.getOrDefault("FROM_BIC",""));
                        fileContent = fileContent.replaceAll("To_BIC", columnMap.getOrDefault("TO_BIC",""));

                        //fileContent = fileContent.replaceAll("BizSeriveCode", columnMap.get("BizSvc"));


                        fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMap.getOrDefault("MktPrctc_Regy",""));
                        fileContent = fileContent.replaceAll("MktPrctc_Id", columnMap.getOrDefault("MktPrctc_Id",""));

                        uetr = UUID.randomUUID().toString();
                        fileContent = fileContent.replaceAll("uniqueUETR", uetr);

                        fileContent = fileContent.replaceAll("SettlemtnMethod", columnMap.getOrDefault("Settlement_Method",""));

                        if(columnMap.getOrDefault("System_To", "").equalsIgnoreCase("CBSS") && country.equalsIgnoreCase("KEN")){

                            fileContent = fileContent.replaceAll("Prtry_CategoryPurp", columnMap.getOrDefault("CategoryPurp", ""));
                            fileContent = fileContent.replaceAll("CategoryPurp", "");

                        }else {
                            fileContent = fileContent.replaceAll("Prtry_CategoryPurp", "");

                            fileContent = fileContent.replaceAll("CategoryPurp", columnMap.getOrDefault("CategoryPurp", ""));
                        }

                        fileContent = fileContent.replaceAll("ChargeBar", columnMap.getOrDefault("ChargeBar","SHAR"));
                        fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""));

                        Random random = new Random();
                        int rand = 500;
                        while (true) {
                            rand = random.nextInt(10000);
                            if (rand != 500) break;
                        }
                        System.out.println(rand);
                        //fileContent = fileContent.replaceAll("IntrBankSettlmAmt", String.valueOf(rand));

                        //For Credit Queue test we need to get the current balance via rest and then add 100 on the balance so that
                        //the case is able to fail insufficient funds
                        Double ConvertedAmnt,Amount =0.0;
                        String AccountUsedCurrency ="";

                        if(columnMap.getOrDefault("Test Discription","").contains("Credit_Queue_Insufficient_Funds")){
                            fileContent = fileContent.replaceAll("IntrBankSettlmAmt",  GetAvailableBalance(columnMap.getOrDefault("Debtor_Account","")).get("AvailableBalance"));
                            System.out.println("Debit Account used is "+columnMap.getOrDefault("Debtor_Account","")+" and the Balance of the Account is "+columnMap.getOrDefault("IntrBkSttlmAmt","")+" and Amount to be used is "+columnMap.getOrDefault("IntrBkSttlmAmt","")+100);
                        }else
                        {
                            fileContent = fileContent.replaceAll("IntrBankSettlmAmt", columnMap.getOrDefault("IntrBkSttlmAmt",""));
                        }

                        fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                        //get rate and add it to the message and also get the ranking
                        if(columnMap.getOrDefault("InstdAmt_Ccy","").isEmpty() || columnMap.getOrDefault("InstdAmt_Ccy","").length() <0 ||country.equalsIgnoreCase("ZAF")){
                            fileContent = fileContent.replaceAll("ExchangeRate", "");
                            fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));

                            fileContent = fileContent.replaceAll("EqvtAmt_Amt", columnMap.getOrDefault("EqvtAmt_Amt", Amount.toString()));
                            fileContent = fileContent.replaceAll("EqvtAmt_CCY", columnMap.getOrDefault("EqvtAmt_CCY",""));
                            fileContent = fileContent.replaceAll("EqvtAmtCCYTrans", columnMap.getOrDefault("EqvtAmt_CCYTrans",""));

                        }
                        else {
                            if(MessageType.equalsIgnoreCase("PAIN001") && columnMap.getOrDefault("Test Discription","").contains("Credit_Queue_Insufficient_Funds")){

                                    fileContent = fileContent.replaceAll("InstructedAmt",  GetAvailableBalance(columnMap.get("Debtor_Account")).get("AvailableBalance").replaceAll("-",""));
                                    System.out.println("Debit Account used is "+columnMap.getOrDefault("Debtor_Account","")+" and the Balance of the Account is "+columnMap.getOrDefault("IntrBkSttlmAmt","")+" and Amount to be used is "+columnMap.getOrDefault("IntrBkSttlmAmt","")+100);
                            }
                            else {
                                if (SystemUsed.equalsIgnoreCase("Inwards")) {
                                    /**
                                     * THIS SECTION IS FOR INWARDS
                                     */
                                    Amount = Double.parseDouble(columnMap.getOrDefault("IntrBkSttlmAmt", ""));
                                    ConvertedAmnt = getCurrencyConvertedAmount(columnMap.getOrDefault("IntrBkSttlmAmt_CCY", ""), columnMap.getOrDefault("InstdAmt_Ccy", ""), Amount, "Principal");
                                    fileContent = fileContent.replaceAll("InstructedAmt", String.valueOf(ConvertedAmnt));
                                    fileContent = fileContent.replaceAll("ExchangeRate", String.valueOf(CaclRate));
                                }
                                else {
                                    /**
                                     * THIS SECTION IS FOR OUTWARDS
                                     */
                                    AccountUsedCurrency = GetAvailableBalance(columnMap.getOrDefault("Debtor_Account", "")).get("AccountCurrency");

                                    if (columnMap.getOrDefault("IntrBkSttlmAmt_CCY", "").equalsIgnoreCase("")&& MessageType.equalsIgnoreCase("PAIN001")&&
                                            columnMap.getOrDefault("InstdAmt_Ccy", "").equalsIgnoreCase("")&& MessageType.equalsIgnoreCase("PAIN001"))
                                    {
                                            if(!columnMap.getOrDefault("EqvtAmt_CCY","").equalsIgnoreCase(AccountUsedCurrency))
                                            {
                                                fileContent = fileContent.replaceAll("EqvtAmtCCYTrans", columnMap.getOrDefault("EqvtAmt_CCYTrans",""));

                                                Amount = getCurrencyConvertedAmount(columnMap.getOrDefault("EqvtAmt_CCYTrans",""),AccountUsedCurrency,Double.parseDouble(columnMap.getOrDefault("EqvtAmt_Amt","")),"Principal");

                                                fileContent = fileContent.replaceAll("EqvtAmt_Amt", columnMap.getOrDefault("EqvtAmt_Amt", Amount.toString()));
                                                fileContent = fileContent.replaceAll("EqvtAmt_CCY", AccountUsedCurrency);

                                                //Make these fields blank or according to what is porvided in the spreadsheet
                                                fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                                                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));

                                            }else{
                                                //Make these fields blank or according to what is porvided in the spreadsheet

                                                fileContent = fileContent.replaceAll("EqvtAmt_Amt", columnMap.getOrDefault("EqvtAmt_Amt", Amount.toString()));
                                                fileContent = fileContent.replaceAll("EqvtAmt_CCY", columnMap.getOrDefault("EqvtAmt_CCY",""));
                                                fileContent = fileContent.replaceAll("EqvtAmtCCYTrans", columnMap.getOrDefault("EqvtAmt_CCYTrans",""));

                                                //Make these fields blank or according to what is porvided in the spreadsheet
                                                fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                                                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));

                                            }
                                    }else if (columnMap.getOrDefault("IntrBkSttlmAmt_CCY", "").equalsIgnoreCase("") && MessageType.equalsIgnoreCase("PAIN001")
                                             && columnMap.getOrDefault("EqvtAmt_CCY", "").equalsIgnoreCase("") && MessageType.equalsIgnoreCase("PAIN001"))
                                            {
                                                Amount = getCurrencyConvertedAmount(columnMap.getOrDefault("InstdAmt_Ccy",""),AccountUsedCurrency,Double.parseDouble(columnMap.getOrDefault("InstdAmt","")),"Principal");

                                                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt", Amount.toString()));
                                                fileContent = fileContent.replaceAll("InstructAmt_CCY", AccountUsedCurrency);
                                                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));


                                                //Make these fields blank or according to what is porvided in the spreadsheet
                                                fileContent = fileContent.replaceAll("EqvtAmt_Amt", columnMap.getOrDefault("EqvtAmt_Amt",""));
                                                fileContent = fileContent.replaceAll("EqvtAmt_CCY", columnMap.getOrDefault("EqvtAmt_CCY",""));
                                                fileContent = fileContent.replaceAll("EqvtAmtCCYTrans", columnMap.getOrDefault("EqvtAmt_CCYTrans",""));

                                            }
                                            else {
                                            Amount = Double.parseDouble(columnMap.getOrDefault("IntrBkSttlmAmt", ""));

                                            ConvertedAmnt = getCurrencyConvertedAmount(columnMap.getOrDefault("IntrBkSttlmAmt_CCY", ""), columnMap.getOrDefault("InstdAmt_Ccy", ""), Amount, "Principal");

                                            fileContent = fileContent.replaceAll("InstructedAmt", String.valueOf(ConvertedAmnt));
                                            fileContent = fileContent.replaceAll("ExchangeRate", String.valueOf(CaclRate));

                                            //Make these fields blank or according to what is porvided in the spreadsheet
                                            fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                                            fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));

                                        }
                                }
                            }
                        }
                        fileContent = fileContent.replaceAll("ServiceLevel_Properiety", columnMap.getOrDefault("ServiceLevel_Properiety", ""));
                        fileContent = fileContent.replaceAll("LocalInstrm", columnMap.getOrDefault("LocalInstrm", ""));

                        fileContent = fileContent.replaceAll("ChargInfo_CCY", columnMap.getOrDefault("ChargInfo_CCY", ""));
                        fileContent = fileContent.replaceAll("Charginfo_Amt", columnMap.getOrDefault("Charginfo_Amt", ""));
                        fileContent = fileContent.replaceAll("ChargsInf_agt_BIC", columnMap.getOrDefault("ChargsInf_agt_BIC", ""));

                        fileContent = fileContent.replaceAll("Previous_instr_Agt_1_BIC", columnMap.getOrDefault("PreviousInstructingAgent1_BIC", ""));
                        fileContent = fileContent.replaceAll("Previous_instr_Agt_2_BIC", columnMap.getOrDefault("PreviousInstructingAgent2_BIC", ""));
                        fileContent = fileContent.replaceAll("Previous_instr_Agt_3_BIC", columnMap.getOrDefault("PreviousInstructingAgent3_BIC", ""));

                        fileContent = fileContent.replaceAll("Instructing_BIC", columnMap.getOrDefault("InstructingAgent_BIC", ""));
                        fileContent = fileContent.replaceAll("Instructed_BIC", columnMap.getOrDefault("InstructedAgent_BIC", ""));
                        fileContent = fileContent.replaceAll("Debtor_BIC", columnMap.getOrDefault("Debtor_BIC", ""));
                        fileContent = fileContent.replaceAll("Debtor_Name", columnMap.getOrDefault("Debtor_Name", ""));
                        fileContent = fileContent.replaceAll("Local_Indicator", columnMap.getOrDefault("Local_Indicator", ""));
                        fileContent = fileContent.replaceAll("Debtor_Account", columnMap.getOrDefault("Debtor_Account", ""));
                        fileContent = fileContent.replaceAll("DebtorAccount_IBAN", columnMap.getOrDefault("DebtorAccount_IBAN", ""));

                        fileContent = fileContent.replaceAll("Debtor_Agt_BIC", columnMap.getOrDefault("Debtor_Agt_BIC", ""));
                        fileContent = fileContent.replaceAll("Debtor_Agt_Account", columnMap.getOrDefault("Debtor_Agt _Account", ""));
                        fileContent = fileContent.replaceAll("Debtor_Agt_Account", columnMap.getOrDefault("Debtor_Agt_Account", ""));
                        fileContent = fileContent.replaceAll("Creditor_Agt_BIC", columnMap.getOrDefault("Creditor_Agt_BIC", ""));
                        fileContent = fileContent.replaceAll("Creditor_Agt_Account", columnMap.getOrDefault("Creditor_Agt_Account", ""));
                        fileContent = fileContent.replaceAll("Creditor_BIC", columnMap.getOrDefault("Creditor_BIC", ""));
                        fileContent = fileContent.replaceAll("Creditor_Name", columnMap.getOrDefault("Creditor_Name", ""));
                        fileContent = fileContent.replaceAll("Creditor_Account", columnMap.getOrDefault("Creditor_Account", ""));
                        fileContent = fileContent.replaceAll("InstructionForNextAgent", columnMap.getOrDefault("Instruction for Next Agent", ""));
                        fileContent = fileContent.replaceAll("InstructionForCreditAgt", columnMap.getOrDefault("Instruction For Creditor Agent", ""));

                        fileContent = fileContent.replaceAll("CreditorAgent_Name", columnMap.getOrDefault("CreditorAgent_Name", ""));
                        fileContent = fileContent.replaceAll("Instructing_Reimbursement_Agent", columnMap.getOrDefault("Instructing_Reimbursement_Agent", ""));
                        fileContent = fileContent.replaceAll("IntermediaryAgent1_BIC", columnMap.getOrDefault("IntermediaryAgent1_BIC", ""));
                        fileContent = fileContent.replaceAll("ClearingSystem", columnMap.getOrDefault("ClearingSystem", ""));
                        fileContent = fileContent.replaceAll("Purp_Prtry", columnMap.getOrDefault("Purp_Prtry", ""));
                        fileContent = fileContent.replaceAll("Purpose_Code", columnMap.getOrDefault("Purpose_Code", ""));
                        fileContent = fileContent.replaceAll("BatchBooking", columnMap.getOrDefault("BatchBooking", ""));


                        /**
                         * This condition below is based on this story #349234 and #349229
                         * GHA ==#574330 and #574332
                         */
                        if (country.equalsIgnoreCase("ZAF") && MessageType.equalsIgnoreCase("PACS008") ||
                                country.equalsIgnoreCase("ZAF") && MessageType.equalsIgnoreCase("PAIN001") ||
                                country.equalsIgnoreCase("ZMB") && MessageType.equalsIgnoreCase("PACS008") ||
                                country.equalsIgnoreCase("ZMB") && MessageType.equalsIgnoreCase("PAIN001")||
                                country.equalsIgnoreCase("GHA") && MessageType.equalsIgnoreCase("PAIN001")
                        ) {
                            fileContent = fileContent.replaceAll("RmtInf_TaxRmt_Strd", columnMap.getOrDefault("RmtInf_TaxRmt_Strd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_CdtrRefInf_Strd", columnMap.getOrDefault("RmtInf_CdtrRefInf_Strd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Strd_AddtlRmtInf", columnMap.getOrDefault("RmtInf_Strd_AddtlRmtInf", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMap.getOrDefault("RmtInf_Ustrd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Strd_RfrdDocInf_Nb", columnMap.getOrDefault("RmtInf_Strd_RfrdDocInf_Nb", ""));



                        } else {
                            fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMap.getOrDefault("RmtInf_Ustrd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_TaxRmt_Strd", columnMap.getOrDefault("RmtInf_TaxRmt_Strd", ""));
                            fileContent = fileContent.replaceAll("ClearingSystem", columnMap.getOrDefault("ClearingSystem", ""));

                            fileContent = fileContent.replaceAll("RmtInf_TaxRmt_Strd", columnMap.getOrDefault("RmtInf_TaxRmt_Strd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_CdtrRefInf_Strd", columnMap.getOrDefault("RmtInf_CdtrRefInf_Strd", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Strd_RfrdDocInf_Nb", columnMap.getOrDefault("RmtInf_Strd_RfrdDocInf_Nb", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Strd_AddtlRmtInf", columnMap.getOrDefault("RmtInf_Strd_AddtlRmtInf", ""));
                            fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMap.getOrDefault("RmtInf_Ustrd", ""));

                        }


                        //need to understand the purpose of this
                        if (SystemUsed.equalsIgnoreCase("Inwards")) {
                            fileContent = fileContent.replaceAll("ServiceLevelCode", "");
                            fileContent = fileContent.replaceAll("ServiceLevel_Proprietary", "");
                            fileContent = fileContent.replaceAll("InstructionForNextAgent", "");

                        }
                        fileContent = fileContent.replaceAll("Purpose_Code", "");
                        fileContent = fileContent.replaceAll("Initiating_System", columnMap.getOrDefault("Intiating_System", ""));
                        fileContent = fileContent.replaceAll("System_From", columnMap.getOrDefault("System_From", ""));
                        fileContent = fileContent.replaceAll("System_To", columnMap.getOrDefault("System_To", ""));

                        break;
                }


                try {
                    fileContent = fileContent.replaceAll("PPPP-PP-PP", getPreviousDate());
                } catch (ParseException e) {
                    System.out.println("Invalid date string");
                    e.printStackTrace();
                }
            } else if (fileContent.contains("END2BusDupCheck") || fileContent.contains("MSGIDBusDupCheck") || fileContent.contains("INSIDBusDupCheck") || fileContent.contains("BusDupCheckUETR")) {

                fileContent = fileContent.replaceAll("END2BusDupCheck", end2EndId);
                fileContent = fileContent.replaceAll("TxIDBusDupCheck", transactionId);
                fileContent = fileContent.replaceAll("MSGIDBusDupCheck", msgId);
                fileContent = fileContent.replaceAll("BIZMsgIDDupCheck", BizmsgId);
                fileContent = fileContent.replaceAll("INSIDBusDupCheck", instructionID);
                fileContent = fileContent.replaceAll("YYYY-MM-DD", getTodayDate());
                fileContent = fileContent.replaceAll("BusDupCheckUETR", UUID.randomUUID().toString());

                fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""));
                fileContent = fileContent.replaceAll("IntrBankSettlmAmt", columnMap.getOrDefault("IntrBkSttlmAmt",""));
                fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));
                //get rate and add it to the message and also get the ranking
                if(columnMap.getOrDefault("InstdAmt_Ccy","").isEmpty()){
                    fileContent = fileContent.replaceAll("ExchangeRate", "");

                }else {
                    String ExchangeRateFromDB = deriveRateTypeRateAndOperator(columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""), columnMap.getOrDefault("InstdAmt_Ccy",""),"Principal", columnMap.getOrDefault("System_To","")).get("RateToBeUsed");
                    fileContent = fileContent.replaceAll("ExchangeRate", ExchangeRateFromDB);
                }

                 } else if (fileContent.contains("techDupEndToEndId") || fileContent.contains("techDupTransactionId") ||
                    fileContent.contains("techDupMsgIdId") || fileContent.contains("techDupInstrId")
                    || fileContent.contains("techDupUETR")) {

                fileContent = fileContent.replaceAll("techDupEndToEndId", end2EndId);
                //Transaction ID
                fileContent = fileContent.replaceAll("techDupTransactionId", transactionId);
                //Instructed ID
                fileContent = fileContent.replaceAll("techDupInstrId", instructionID);
                //Intra Bank Settlement Date
                fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);
                //Message ID
                fileContent = fileContent.replaceAll("techDupMsgIdId", msgId);
                //UETR
                fileContent = fileContent.replaceAll("techDupUETR", uetr);

                fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMap.getOrDefault("IntrBkSttlmAmt_CCY",""));
                fileContent = fileContent.replaceAll("IntrBankSettlmAmt", columnMap.getOrDefault("IntrBkSttlmAmt",""));
                fileContent = fileContent.replaceAll("InstructAmt_CCY", columnMap.getOrDefault("InstdAmt_Ccy",""));
                fileContent = fileContent.replaceAll("InstructedAmt", columnMap.getOrDefault("InstdAmt",""));
                //get rate and add it to the message and also get the ranking
                if(columnMap.getOrDefault("InstdAmt_Ccy","").isEmpty()){
                    fileContent = fileContent.replaceAll("ExchangeRate", "");

                }else {
                    String ExchangeRateFromDB = deriveRateTypeRateAndOperator(columnMap.getOrDefault("InstdAmt_Ccy", ""), columnMap.getOrDefault("IntrBkSttlmAmt_CCY", ""), "Principal", columnMap.getOrDefault("System_To", "")).get("RateToBeUsed");
                    fileContent = fileContent.replaceAll("ExchangeRate", ExchangeRateFromDB);
                }         }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return fileContent + "::" + end2EndId;

    }

    public static String convertSimPacsToString(File file, Map<String,String> columnMapSimulation, String E2EID, String OriginalMsgType, String SimMessageType, String dbName) throws IOException, SQLException {

        String fileContent = new String();
        HashMap captureValues = new HashMap<String, String>();
        String BizsvcUsed ="";
        try {
            //with this function we want to get the original message to the DB so that we can store it under location "src/main/resources/Outwards/Testdata/Orignal_MessageFromDb.xml"
            // and then extract data to match with the PACS002 or Cam54 sim
            if(file.getAbsolutePath().contains("PACS.002_BAPS_Response")) {
                GetxmlMessageFromDB(E2EID, OriginalMsgType, dbName, "BAPSTRISIM");

            }else if(SimMessageType.equalsIgnoreCase("PACS.002") && dbName.equalsIgnoreCase("HVPP")){
                GetxmlMessageFromDB(E2EID, OriginalMsgType, dbName, "Sanctions");
            }
            else{
                //since we send out MT103 message we are unable to get TXD_ID and UETR so we will check message in the landing Area for MUS
                if(country.equalsIgnoreCase("TZA")
                        ||country.equalsIgnoreCase("MUS")
                        ||country.equalsIgnoreCase("UGA")
                        ||OriginalMsgType.equalsIgnoreCase("SECL.010")
                        ||SimMessageType.equalsIgnoreCase("PACS004") &&dbName.equalsIgnoreCase("HVPP")){
                    if(IsTestLESAKA || E2EID.contains("LSK")){
                        GetxmlMessageFromDB(E2EID, OriginalMsgType, dbName, "LandingArea");

                    }else{
                        GetxmlMessageFromDB(E2EID, OriginalMsgType, dbName, "LandingArea");
                    }

                }else {
                    GetxmlMessageFromDB(E2EID, OriginalMsgType, dbName, "GeneratedMessageOut");
                }
            }

            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // fileContent = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent = fileContent.concat(line) + System.lineSeparator();
            }

            //***************************************Normal Transaction********************************************************************************
            if (fileContent.contains("uniqueEndToEndId") || fileContent.contains("uniqueTransactionId") ||
                    fileContent.contains("uniqueInstrId") || fileContent.contains("YYYY-MM-DD") || fileContent.contains("uniqueMsgIdId") || fileContent.contains("uniqueUETR")) {
                String BizSvcCode="";

                if(SimMessageType.equalsIgnoreCase("PACS004")) {
                    if (columnMapSimulation.get("PACS004BizSvc").contains("sarb.sadc")) {
                        BizSvcCode = "RTGSSADC";
                    } else {
                        BizSvcCode = "RTGSDOM";

                    }
                     BizsvcUsed = extractMatchingBizSvcCodes(SimMessageType, BizSvcCode, country, "DropMessage");
                }

                //End2EndID
                if (E2EID.length() == 0) {
                    end2EndId = "BKN" + generateUniqueString();

                } else {
                    end2EndId = E2EID;
                }

                if(country.equalsIgnoreCase("SYC")){
                    fileContent = fileContent.replaceAll("Business_Entity", "BARCSC");
                    fileContent = fileContent.replaceAll("MsgWrapperCountry", "SC");
                }else {
                    fileContent = fileContent.replaceAll("Business_Entity", "BARC" + country.substring(0, 2));
                    fileContent = fileContent.replaceAll("MsgWrapperCountry", country.substring(0, 2));
                }

                //This method is responsible to convert the sample message that we have from SADC to Domestic and Vice versa
                if (
                        (columnMapSimulation.containsKey("Test") && columnMapSimulation.get("Test").contains("SADC")) ||
                                (columnMapSimulation.containsKey("TestScenario #") && columnMapSimulation.get("TestScenario #").contains("SADC")) ||
                                (columnMapSimulation.containsKey("BizSvc") && columnMapSimulation.get("BizSvc").contains("sarb.sadc")) ||
                                (columnMapSimulation.containsKey("PACS004BizSvc") && columnMapSimulation.get("PACS004BizSvc").contains("sarb.sadc"))
                )
                {
                    fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "");
                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                    fileContent = fileContent.replaceAll("settlementTimeRequired", "");

                    fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "");
                    fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");

                    fileContent = fileContent.replaceAll("underlyingCreditorAgentAccount", "");
                    fileContent = fileContent.replaceAll("ServiceLevel_Properiety", "");
                    fileContent = fileContent.replaceAll("ServiceLevel_Code", "G001");

                    //Unmatched CMT054
                    fileContent = fileContent.replaceAll("CMT054ValueDate_DT", "YYYY-MM-DD");
                    fileContent = fileContent.replaceAll("CMT054ValueDate&Time_DtTm", "");

                } else {

                    fileContent = fileContent.replaceAll("settlementTimeRequired", "22:00:00.00");

                   /* if(!country.equalsIgnoreCase("ZAF")&& columnMapSimulation.get("PACS004BizSvc").contains("swift.iap")&&SimMessageType.equalsIgnoreCase("PACS004")){
                        fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "");

                    }else
                    {
                        fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "50101552");
                    }*/

                    fileContent = fileContent.replaceAll("underlyingCreditorAgentAccount", "196546057");
                    fileContent = fileContent.replaceAll("ServiceLevel_Properiety", "TTC:1030,REC:0607");

                    //Here we are making sure that clearing system is not available in SA and also Settlement account is visible
                    if (country.equalsIgnoreCase("ZAF")) {
                        if(BizsvcUsed.contains("sarb.samos.02")){
                            fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "");
                            fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                        }else {
                            fileContent = fileContent.replaceAll("groupHeader_intraBankSettlementDate", "YYYY-MM-DD");
                            fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "YYYY-MM-DD");

                        }
                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");
                        fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "50101552");

                    } else {

                            if (country.equalsIgnoreCase("ZMB")) {
                                fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "ZIS");
                            } else if (country.equalsIgnoreCase("TZA")) {
                                    //this is for Tanzania ,need to create else if statement, if we will have different countries
                                    fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "TIS");
                                }
                                        else if (country.equalsIgnoreCase("MUS")) {
                                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "MUP");
                                    }
                                            else if (country.equalsIgnoreCase("KEN")) {
                                            fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "KPS");
                                        }
                                                else if (country.equalsIgnoreCase("BWA")) {
                                                fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "BIS");
                                            }
                                                    else if (country.equalsIgnoreCase("GHA")) {
                                                    fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "GIS");
                                                }
                                                        else if (country.equalsIgnoreCase("UGA")) {
                                                        fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "UIS");
                                                    }    else if (country.equalsIgnoreCase("SYC")) {
                                                                fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "SIS");
                                                            }                   //ARO Countries do not have settlement Account field

                            else {
                                fileContent = fileContent.replaceAll("settlementInfo_ClearingSystem_Code", "");

                            }
                        //ARO Countries do not have settlement Account field
                        fileContent = fileContent.replaceAll("settlementInfo_settlementAccount", "");
                        fileContent = fileContent.replaceAll("Camt054DomesticSettlementAcc", "");

                    }

                    fileContent = fileContent.replaceAll("ServiceLevel_Code", "");

                    //unmatched Camt.054 specific fields
                    fileContent = fileContent.replaceAll("CMT054ValueDate_DT", "");
                    fileContent = fileContent.replaceAll("CMT054ValueDate&Time_DtTm", "YYYY-MM-DDT10:23:06+02:00");
                }
                fileContent = fileContent.replaceAll("ChargeBar", columnMap.getOrDefault("ChargeBar","SHAR"));

                //Intra Bank Settlement Date
                intraBankSettlementDate = getTodayDate();
                fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);

                fileContent = fileContent.replaceAll("FROM_BIC", columnMapSimulation.getOrDefault("FROM_BIC",""));
                fileContent = fileContent.replaceAll("To_BIC", columnMapSimulation.getOrDefault("TO_BIC",""));
                //get Values from orignal message to simulate pacs002 or Camt.54

                String appHeaderPath = "";
                String Credit_Trans_Info_pmtIdPath = "";
                String creditTransInfoPath = "";
                String groupHeaderPath = "";
                String intrBkSttlmAmtCCYPath = "";
                String PmtRtr_TxInf_pmtIdPath,TxInf_OrgnlGrpInfPath,RtrdIntrBkSttlmAmtCCYPath ="";
                String CaseNumber =dbConnections.GetCaseNumber(end2EndId, "");

                switch (OriginalMsgType.toUpperCase()) {
                    case "PACS.008":
                    case "PAIN.001":
                        appHeaderPath = XmlXpaths.PACS0008_AppHeader_path;
                        Credit_Trans_Info_pmtIdPath = XmlXpaths.PACS0008_Credit_Trans_Info_PmtId_Path;
                        creditTransInfoPath = XmlXpaths.PACS0008_Credit_Trans_Info_Path;
                        groupHeaderPath = XmlXpaths.PACS0008_GroupHeader_Path;
                        intrBkSttlmAmtCCYPath = XmlXpaths.PACS0008_Credit_Trans_Info_IntrBkSttlmAmtCCY_Path;

                        // Apply replacement if root element is envelop
                        if (IsRootElemenEnvelop(Outward_CBSS_Orignal_MESSAGE)) {
                            String prefixToRemove = "/messageWrapper/messages/message/content/";
                            appHeaderPath = appHeaderPath.replace(prefixToRemove, "");
                            Credit_Trans_Info_pmtIdPath = Credit_Trans_Info_pmtIdPath.replace(prefixToRemove, "");
                            creditTransInfoPath = creditTransInfoPath.replace(prefixToRemove, "");
                            groupHeaderPath = groupHeaderPath.replace(prefixToRemove, "");
                            intrBkSttlmAmtCCYPath = intrBkSttlmAmtCCYPath.replace(prefixToRemove, "");
                        }

                        UniqueBisSvCode = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizSvc");
                        Orignaluetr = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "UETR");
                        transactionId = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "TxId");
                        UniqueMsgNameID = CommonMethods.extract_Data_FromMessage(appHeaderPath, "MsgDefIdr");
                        UniqueBisMsgId = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizMsgIdr");
                        UniqueMsgId = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "MsgId");

                        //#435503 the following below assist us with making sure that the PACS004 Inwards does STP and if the
                        //if statement is removed then the PACS004 will be partially Mapped causing it to go to BI

                        if (SimMessageType.equalsIgnoreCase("PACS004") && !E2EID.contains("LSK") && !SystemUsed.equalsIgnoreCase("Outwards")) {
                            instructionID = "ROF" + generateUniqueString();
                            fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);

                        } else {
                            instructionID = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "InstrId");

                        }
                        Original_IntrBankSttlmDate = CommonMethods.extract_Data_FromMessage(creditTransInfoPath, "IntrBkSttlmDt");
                        GroupHeader_Original_IntrBankSttlmDate = Optional.ofNullable(CommonMethods.extract_Data_FromMessage(groupHeaderPath, "IntrBkSttlmDt"))
                                        .filter(v -> !v.equalsIgnoreCase("NOTHING")).orElse("");

                        Original_CrDate_Time = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "CreDtTm");
                        IntrBankSttlmAmtCcy = CommonMethods.extract_Data_FromMessage(intrBkSttlmAmtCCYPath, "");
                        IntrBankSttlmAmt = CommonMethods.extract_Data_FromMessage(creditTransInfoPath, "IntrBkSttlmAmt");


                        //Mapping the simulation Message
                        switch (country) {

                            case "ZAF":
                                //the path for IntrBank_Sttlm_Date in SA is different from ARO messages
                                if (UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.01") ||UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.02")) {
                                    //when country is SA and bizSvc is sadc groupHeader_intraBankSettlementDate is not in use so we need to remove it from message specific for pacs004
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("50101552", "");
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);
                                }

                                if (UniqueBisSvCode.equalsIgnoreCase("Sarb.samos.02")) {
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);

                                    //for SA Biz samos 02 the GroupHeader_Original_IntrBankSttlmDate is not in use so we need to remove it from message
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                                }else {

                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);

                                    //when country is SA and bizSvc is not samos02 interbank settlement to be use is GroupHeader_Original_IntrBankSttlmDatem and IntrBank_Sttlm_Date should be blank
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", "");
                                }


                            break;
                            case "TZA":

                                    fileContent = fileContent.replaceAll("uniqueMsgIdId", UniqueMsgId);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", UniqueMsgId);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);


                                break;
                            case "MUS":
                                //for Simulation to work in since we use MT message to send to Morongwa ,End to end must be the same as insdID and TxdID which is the Case number
                                if ( !SimMessageType.equalsIgnoreCase("PACS004") && dbName.equalsIgnoreCase("CBSS")) {
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", CaseNumber);


                                }else{
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);

                                }
                                fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                                break;
                            case "GHA":
                                if(UniqueMsgId.startsWith("HG")) {
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", end2EndId);
                                }
                                break;

                            default:
                                fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);

                        }
                        break;
                    case "PACS.009":
                    case "PACS.009COV":
                         appHeaderPath = XmlXpaths.PACS0009_AppHeader_path;
                         Credit_Trans_Info_pmtIdPath = XmlXpaths.PACS0009_Credit_Trans_Info_PmtId_Path;
                         creditTransInfoPath = XmlXpaths.PACS0009_Credit_Trans_Info_Path;
                         groupHeaderPath = XmlXpaths.PACS0009_GroupHeader_Path;
                         intrBkSttlmAmtCCYPath = XmlXpaths.PACS0009_Credit_Trans_Info_IntrBkSttlmAmtCCY_Path;

                        // Apply replacement if root element is envelop
                        if (IsRootElemenEnvelop(Outward_CBSS_Orignal_MESSAGE)) {
                            String prefixToRemove = "/messageWrapper/messages/message/content/";
                            appHeaderPath = appHeaderPath.replace(prefixToRemove, "");
                            Credit_Trans_Info_pmtIdPath = Credit_Trans_Info_pmtIdPath.replace(prefixToRemove, "");
                            creditTransInfoPath = creditTransInfoPath.replace(prefixToRemove, "");
                            groupHeaderPath = groupHeaderPath.replace(prefixToRemove, "");
                            intrBkSttlmAmtCCYPath = intrBkSttlmAmtCCYPath.replace(prefixToRemove, "");
                        }

                        UniqueBisSvCode = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizSvc");
                        Orignaluetr = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "UETR");
                        UniqueMsgNameID = CommonMethods.extract_Data_FromMessage(appHeaderPath, "MsgDefIdr");
                        UniqueBisMsgId = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizMsgIdr");
                        instructionID = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "InstrId");
                        transactionId = CommonMethods.extract_Data_FromMessage(Credit_Trans_Info_pmtIdPath, "TxId");
                        UniqueMsgId = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "MsgId");

                        Original_IntrBankSttlmDate = CommonMethods.extract_Data_FromMessage(creditTransInfoPath, "IntrBkSttlmDt");
                        GroupHeader_Original_IntrBankSttlmDate = Optional.ofNullable(CommonMethods.extract_Data_FromMessage(groupHeaderPath, "IntrBkSttlmDt"))
                                .filter(v -> !v.equalsIgnoreCase("NOTHING")).orElse("");
                        Original_CrDate_Time = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "CreDtTm");
                        IntrBankSttlmAmtCcy = CommonMethods.extract_Data_FromMessage(intrBkSttlmAmtCCYPath, "");
                        IntrBankSttlmAmt = CommonMethods.extract_Data_FromMessage(creditTransInfoPath, "IntrBkSttlmAmt");

                        //Mapping the simulation Message
                        switch (country) {
                            case "ZAF":
                                //the path for IntrBank_Sttlm_Date in SA is different from ARO messages
                                if (UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.01") ||UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.02")) {
                                    //this below is the same as groupHeader_intraBankSettlementDate and specific for pacs004
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("50101552", "");
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);
                                }

                                if (UniqueBisSvCode.equalsIgnoreCase("Sarb.samos.02")||UniqueBisSvCode.equalsIgnoreCase("swift.iap.02")) {
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);

                                    //for SA Biz samos 02 the GroupHeader_Original_IntrBankSttlmDate is not in use so we need to remove it from message
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                                }else {

                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);

                                    //when country is SA and bizSvc is not samos02 interbank settlement to be use is GroupHeader_Original_IntrBankSttlmDatem and IntrBank_Sttlm_Date should be blank
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", "");
                                }

                            case "TZA":
                                if(dbName.equalsIgnoreCase("CBSS") && end2EndId.contains("LSK")) {

                                    fileContent = fileContent.replaceAll("uniqueMsgIdId", UniqueMsgId);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", UniqueMsgId);

                                }else{
                                    fileContent = fileContent.replaceAll("uniqueMsgIdId", end2EndId);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", end2EndId);
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", end2EndId);
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", end2EndId);

                                }
                                break;
                            case "MUS":
                                //for Simulation to work in since we use MT message to send to Morongwa ,End to end must be the same as insdID and TxdID which is the Case number
                                if ( !SimMessageType.equalsIgnoreCase("PACS004") && dbName.equalsIgnoreCase("CBSS")) {
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", instructionID);

                                }else{
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);

                                }
                                break;
                            default:
                                fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);

                        }
                        break;
                    case "PACS.004":
                        appHeaderPath = XmlXpaths.PACS0004_AppHeader_path;
                        PmtRtr_TxInf_pmtIdPath = XmlXpaths.PACS0004_PmtRtr_TxInf_Path;
                        TxInf_OrgnlGrpInfPath = XmlXpaths.PACS0004_PmtRtr_TxInf_OrgnlGrpInf_Path;
                        groupHeaderPath = XmlXpaths.PACS0004_GroupHeader_Path;
                        RtrdIntrBkSttlmAmtCCYPath = XmlXpaths.PACS0004_PmtRtr_TxInf_RtrdIntrBkSttlmAmtCCY_Path;

                        // Apply replacement if root element is envelop
                        if (IsRootElemenEnvelop(Outward_CBSS_Orignal_MESSAGE)) {
                            String prefixToRemove = "/messageWrapper/messages/message/content/";
                            appHeaderPath = appHeaderPath.replace(prefixToRemove, "");
                            PmtRtr_TxInf_pmtIdPath = PmtRtr_TxInf_pmtIdPath.replace(prefixToRemove, "");
                            creditTransInfoPath = creditTransInfoPath.replace(prefixToRemove, "");
                            groupHeaderPath = groupHeaderPath.replace(prefixToRemove, "");
                            RtrdIntrBkSttlmAmtCCYPath = RtrdIntrBkSttlmAmtCCYPath.replace(prefixToRemove, "");
                        }


                        UniqueBisSvCode = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizSvc");
                        UniqueMsgNameID = CommonMethods.extract_Data_FromMessage(appHeaderPath, "MsgDefIdr");
                        UniqueBisMsgId = CommonMethods.extract_Data_FromMessage(appHeaderPath, "BizMsgIdr");
                        UniqueMsgId = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "MsgId");

                        instructionID = CommonMethods.extract_Data_FromMessage(PmtRtr_TxInf_pmtIdPath, "OrgnlInstrId");
                        transactionId = CommonMethods.extract_Data_FromMessage(PmtRtr_TxInf_pmtIdPath, "OrgnlTxId");
                        Orignaluetr = CommonMethods.extract_Data_FromMessage(PmtRtr_TxInf_pmtIdPath, "OrgnlUETR");

                        Original_IntrBankSttlmDate = CommonMethods.extract_Data_FromMessage(PmtRtr_TxInf_pmtIdPath, "IntrBkSttlmDt");
                        GroupHeader_Original_IntrBankSttlmDate = Optional.ofNullable(CommonMethods.extract_Data_FromMessage(groupHeaderPath, "IntrBkSttlmDt"))
                                .filter(v -> !v.equalsIgnoreCase("NOTHING")).orElse("");

                        Original_CrDate_Time = CommonMethods.extract_Data_FromMessage(groupHeaderPath, "CreDtTm");
                        IntrBankSttlmAmtCcy = CommonMethods.extract_Data_FromMessage(intrBkSttlmAmtCCYPath, "");


                        //Mapping the simulation Message
                        switch (country) {
                            case "ZAF":
                                //the path for IntrBank_Sttlm_Date in SA is different from ARO messages
                                if (UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.01") ||UniqueBisSvCode.equalsIgnoreCase("sarb.sadc.02")) {
                                    //this below is the same as groupHeader_intraBankSettlementDate and specific for pacs004
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("50101552", "");
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);
                                }

                                if (UniqueBisSvCode.equalsIgnoreCase("Sarb.samos.02") ||UniqueBisSvCode.equalsIgnoreCase("swift.iap.02")) {
                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", intraBankSettlementDate);

                                    //for SA Biz samos 02 the GroupHeader_Original_IntrBankSttlmDate is not in use so we need to remove it from message
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", "");

                                }else {

                                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                    fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);

                                    //when country is SA and bizSvc is not samos02 interbank settlement to be use is GroupHeader_Original_IntrBankSttlmDatem and IntrBank_Sttlm_Date should be blank
                                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", "");
                                }
                                break;
                            case "TZA":
                                if(dbName.equalsIgnoreCase("CBSS") && end2EndId.contains("LSK")) {

                                    fileContent = fileContent.replaceAll("uniqueMsgIdId", UniqueMsgId);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", UniqueMsgId);

                                }else{
                                    fileContent = fileContent.replaceAll("uniqueMsgIdId", CaseNumber);
                                    fileContent = fileContent.replaceAll("uniqueInstrId", CaseNumber);
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", CaseNumber);
                                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", CaseNumber);

                                }
                                break;
                            case "MUS":
                                //for Simulation to work in since we use MT message to send to Morongwa ,End to end must be the same as insdID and TxdID which is the Case number
                                if ( !SimMessageType.equalsIgnoreCase("PACS004") && dbName.equalsIgnoreCase("CBSS")) {
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", instructionID);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", instructionID);

                                }else{
                                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                                    fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);

                                }
                                break;
                            default:
                                fileContent = fileContent.replaceAll("GroupHeader_Originl_InterBankSttlment_Date", GroupHeader_Original_IntrBankSttlmDate);
                                fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", Original_IntrBankSttlmDate);
                        }
                        break;

                    case "SECL.10":
                        Orignaluetr = UUID.randomUUID().toString();

                        fileContent = fileContent.replaceAll("UniqueMsgNameID", "secl.010.001.03");
                        fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", end2EndId);
                        fileContent = fileContent.replaceAll("uniqueMsgIdId", end2EndId);
                        fileContent = fileContent.replaceAll("uniqueTransactionId", end2EndId);
                        fileContent = fileContent.replaceAll("uniqueUETR", Orignaluetr);
                        fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);

                        break;

                    case "CAMT.050":
                        Orignaluetr = CommonMethods.extract_Data_FromMessage(XmlXpaths.CAM50_Credit_Trans_Info_LqdtyTrfId, "UETR");
                        Msgid = CommonMethods.extract_Data_FromMessage(XmlXpaths.CAM50_NsgHdr_path, "MsgId");
                        UniqueMsgNameID = CommonMethods.extract_Data_FromMessage(XmlXpaths.CAM50_AppHdr_path, "MsgDefIdr");

                        fileContent = fileContent.replaceAll("uniqueOrgnalIBisMsgeId", Msgid);
                        fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", Msgid);
                        fileContent = fileContent.replaceAll("uniqueMsgIdId", end2EndId);
                        fileContent = fileContent.replaceAll("UniqueMsgNameID", UniqueMsgNameID);
                        fileContent = fileContent.replaceAll("uniqueTransactionId", end2EndId);

                        if (Orignaluetr.equalsIgnoreCase("NOTHING")) {
                            fileContent = fileContent.replaceAll("uniqueUETR", "");
                        } else {
                            fileContent = fileContent.replaceAll("uniqueUETR", Orignaluetr);
                        }
                        break;

                        default:
//                        fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);


                        break;


                }

                fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);

                if(!SimMessageType.equalsIgnoreCase("CAMT.054")) {
                    fileContent = fileContent.replace("Original_CrtDate_Time", Original_CrDate_Time);

                    fileContent = fileContent.replaceAll("IntrBank_SttlmAmt_Ccy", IntrBankSttlmAmtCcy);
                    fileContent = fileContent.replaceAll("IntrBank_SttlmAmt", IntrBankSttlmAmt);

                    fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);

                    fileContent = fileContent.replaceAll("uniqueOrgnalIMsgeId", UniqueMsgId);
                    fileContent = fileContent.replaceAll("uniqueMsgIdId", UniqueBisMsgId);
                    fileContent = fileContent.replaceAll("UniqueMsgNameID", UniqueMsgNameID);

                    fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                    fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);
                    fileContent = fileContent.replaceAll("uniqueUETR", Orignaluetr);

                    fileContent = fileContent.replaceAll("IntrBank_Sttlm_Date", Original_IntrBankSttlmDate);
                    fileContent = fileContent.replaceAll("SA_DOM_SettlementDate", "");
                    //when country is ARO interbank settlement to be use is the one after RtrdIntrBkSttlmAmt and leave the one  before SttlmInf empty
                    fileContent = fileContent.replaceAll("ARO_SADC_SettlementDate", intraBankSettlementDate);
                    fileContent = fileContent.replaceAll("Original_IntrBankSettlment_Date", intraBankSettlementDate);

                    if (columnMapSimulation.getOrDefault("StsRsnInf_Reason", "").isEmpty()) {
                        fileContent = fileContent.replaceAll("uniqueEndToEndId", instructionID);
                    } else {
                        fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId);

                    }
                }

                //Simulation Messages
                switch (SimMessageType.toUpperCase()) {
                    case "CAMT.054":

                        if (columnMapSimulation.get("Test").contains("SADC") || columnMapSimulation.get("BizSvc").contains("sarb.sadc")) {
                            fileContent = fileContent.replaceAll("CMT054SADC_DT", "");
                            fileContent = fileContent.replaceAll("CMT054ValueDate_DtTm", "YYYY-MM-DDT10:23:06+02:00");


                        }else{
                        // Camt.054 specific fields

                            fileContent = fileContent.replaceAll("CMT054SADC_DT", "YYYY-MM-DD");
                            fileContent = fileContent.replaceAll("CMT054ValueDate_DtTm", "");
                         }

                        //Intra Bank Settlement Date
                        intraBankSettlementDate = getTodayDate();
                        fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);

                        if (SimMessageType.equalsIgnoreCase("camt.054") && OriginalMsgType.equalsIgnoreCase("SECL.010")) {
                            fileContent = fileContent.replaceAll("uniqueInstrId", dbConnections.GetCaseNumber(end2EndId, "H"));

                            //When matching CAMT.054 with SECL10 we use the following below codes

                            /*fileContent = fileContent.replaceAll("RptgSrc1", "PFRE");
                            fileContent = fileContent.replaceAll("DomnCD", "PMNT");
                            fileContent = fileContent.replaceAll("FamilyCd", "ICDT");
                            fileContent = fileContent.replaceAll("SubFamilyCode", "ASET");*/


                            fileContent = fileContent.replaceAll("MsgRcpt_AnyBIC", columnMapSimulation.getOrDefault("MsgRcpt_AnyBIC",""));

                            fileContent = fileContent.replaceAll("ReportingSrc", columnMapSimulation.getOrDefault("Reporting Source",""));
                            fileContent = fileContent.replaceAll("DomesticSettlementAcc", columnMapSimulation.getOrDefault("Domestic_Settlement_Acc",""));
                            fileContent = fileContent.replaceAll("BkTxCd_Propriety_Code", columnMapSimulation.getOrDefault("BkTxCd_Propriety_Code",""));

                            fileContent = fileContent.replaceAll("Domn_DomnCD", columnMapSimulation.getOrDefault("Domn_Domain_Code",""));
                            fileContent = fileContent.replaceAll("Domn_FamilyCd", columnMapSimulation.getOrDefault("Domn_Family_Code",""));
                            fileContent = fileContent.replaceAll("Domn_SubFamilyCode", columnMapSimulation.getOrDefault("Domn_Sub_Family_Code",""));
                            fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMapSimulation.getOrDefault("IntrBkSttlmAmt_CCY",""));
                            fileContent = fileContent.replaceAll("CrdtDbtInd", columnMapSimulation.getOrDefault("CreditDebitIndicator",""));

                            fileContent = fileContent.replaceAll("uniqueUETR", "");
                            fileContent = fileContent.replaceAll("uniqueMsgIdId", "SECL10/"+end2EndId);

                            fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMapSimulation.getOrDefault("RmtInf_Ustrd",""));
                            fileContent = fileContent.replaceAll("Purpose_Prop", columnMapSimulation.getOrDefault("Purpose_Prop",""));
                            fileContent = fileContent.replaceAll("Purpose_Code", columnMapSimulation.getOrDefault("Purpose_Code",""));

                            MatchingPropriety = columnMapSimulation.getOrDefault("","");
                            fileContent = fileContent.replaceAll("MatchingPropriety", "");

                            CdtDbtInd = columnMapSimulation.getOrDefault("","");
                            fileContent = fileContent.replaceAll("CrdtDbtInd", CdtDbtInd);

                            fileContent = fileContent.replaceAll("FROM_BIC", columnMapSimulation.getOrDefault("FROM_BIC",""));
                            fileContent = fileContent.replaceAll("To_BIC", columnMapSimulation.getOrDefault("TO_BIC",""));
                            fileContent = fileContent.replaceAll("BizSeriveCode", columnMapSimulation.getOrDefault("BizSvc",""));
                            fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMapSimulation.getOrDefault("MktPrctc_Regy",""));
                            fileContent = fileContent.replaceAll("MktPrctc_Id", columnMapSimulation.getOrDefault("MktPrctc_Id",""));
                            fileContent = fileContent.replaceAll("MsgRcpt_AnyBIC", columnMapSimulation.getOrDefault("MsgRcpt_AnyBIC",""));


                        } else
                        //Unmatched CAMT.054 and matched
                        {
                            fileContent = fileContent.replaceAll("BizSeriveCode", columnMapSimulation.getOrDefault("BizSvc",""));
                            fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", CountryCurrency);

                            String Casenumber = "";

                            if (country.equalsIgnoreCase("ZAF")) {
                                Casenumber = dbConnections.GetCaseNumber(end2EndId, "C");

                            } else if (country.equalsIgnoreCase("KEN")) {
                                instructionID = dbConnections.GetCaseNumber(end2EndId, "H");
                                ;
                                fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);
                            } else {
                                Casenumber = dbConnections.GetCaseNumber(end2EndId, "B");
                            }
                            //if message type is CAMT.50 the get case number from the message

                            if (country.equalsIgnoreCase("ZMB") && OriginalMsgType.equalsIgnoreCase("camt.050")) {
                                Casenumber = CommonMethods.extract_Data_FromMessage(XmlXpaths.CAM50_Credit_Trans_Info_LqdtyTrfId, "InstrId");
                            }

                            instructionID = Casenumber;
                            fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);

                            fileContent = fileContent.replaceAll("FROM_BIC", columnMapSimulation.getOrDefault("FROM_BIC",""));
                            fileContent = fileContent.replaceAll("To_BIC", columnMapSimulation.getOrDefault("TO_BIC",""));
                           // fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);
                            fileContent = fileContent.replaceAll("MktPrctc_Regy", columnMapSimulation.getOrDefault("MktPrctc_Regy",""));
                            fileContent = fileContent.replaceAll("MktPrctc_Id", columnMapSimulation.getOrDefault("MktPrctc_Id",""));
                            fileContent = fileContent.replaceAll("MsgRcpt_AnyBIC", columnMapSimulation.getOrDefault("MsgRcpt_AnyBIC",""));

                            fileContent = fileContent.replaceAll("ReportingSrc", columnMapSimulation.getOrDefault("Reporting Source",""));
                            fileContent = fileContent.replaceAll("DomesticSettlementAcc", columnMapSimulation.getOrDefault("Domestic_Settlement_Acc",""));
                            fileContent = fileContent.replaceAll("BkTxCd_Propriety_Code", columnMapSimulation.getOrDefault("BkTxCd_Propriety_Code",""));

                            fileContent = fileContent.replaceAll("Domn_DomnCD", columnMapSimulation.getOrDefault("Domn_Domain_Code",""));
                            fileContent = fileContent.replaceAll("Domn_FamilyCd", columnMapSimulation.getOrDefault("Domn_Family_Code",""));
                            fileContent = fileContent.replaceAll("Domn_SubFamilyCode", columnMapSimulation.getOrDefault("Domn_Sub_Family_Code",""));
                            fileContent = fileContent.replaceAll("IntrBankSttlmAmt_CCY", columnMapSimulation.getOrDefault("IntrBkSttlmAmt_CCY",""));
                            fileContent = fileContent.replaceAll("CrdtDbtInd", columnMapSimulation.getOrDefault("CreditDebitIndicator",""));

                            fileContent = fileContent.replaceAll("uniqueUETR", "");

                            fileContent = fileContent.replaceAll("RmtInf_Ustrd", columnMapSimulation.getOrDefault("RmtInf_Ustrd",""));
                            fileContent = fileContent.replaceAll("Purpose_Prop", columnMapSimulation.getOrDefault("Purpose_Prop",""));
                            fileContent = fileContent.replaceAll("Purpose_Code", columnMapSimulation.getOrDefault("Purpose_Code",""));

                        }
                        break;

                    case "PACS.002":

                        fileContent = fileContent.replaceAll("FROM_Instg_BIC", PACS004_PACS002_FROM_BIC.get(country));
                        fileContent = fileContent.replaceAll("TO_Instd_BIC", PACS004_PACS002_TO_BIC.get(country));

                        fileContent = fileContent.replaceAll("UniqueTransStatus", columnMapSimulation.getOrDefault("Transact Status",""));

                        fileContent = fileContent.replaceAll("UniquePropValue", columnMapSimulation.getOrDefault("UniquePropValue",""));

                        fileContent = fileContent.replaceAll("ExternalReasonCode", columnMapSimulation.getOrDefault("ExternalReasonCode",""));

                        fileContent = fileContent.replaceAll("AdditionalInfo", columnMapSimulation.getOrDefault("AddtlInf",""));
                        System.out.println("Current Country is :::: " + country);

                        if (country.equalsIgnoreCase("ZAF")) {

                            fileContent = fileContent.replaceAll("StsRsnInf_Reason", columnMapSimulation.getOrDefault("StsRsnInf_Reason",""));
                            fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);
                            fileContent = fileContent.replaceAll("MktPrctc_Regy", "3,");
                            fileContent = fileContent.replaceAll("MktPrctc_Id", "1110,,");
                            fileContent = fileContent.replaceAll("uniqueTransactionId", instructionID);


                        } else {
                            fileContent = fileContent.replaceAll("StsRsnInf_Reason", columnMapSimulation.getOrDefault("StsRsnInf_Reason",""));
                            fileContent = fileContent.replaceAll("BizSeriveCode", UniqueBisSvCode);
                            fileContent = fileContent.replaceAll("MktPrctc_Regy", "");
                            fileContent = fileContent.replaceAll("MktPrctc_Id", "");

                            fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);

                        }


                        break;
                    case "PACS004":
                        if(dbName.equalsIgnoreCase("CBSS")) {
                            fileContent = fileContent.replaceAll("FROM_Instg_BIC", PACS004_PACS002_FROM_BIC.getOrDefault(country, "FROM_BIC_NOT_SETUP"));
                            fileContent = fileContent.replaceAll("TO_Instd_BIC", PACS004_PACS002_TO_BIC.getOrDefault(country, "TO_BIC_NOT_SET_UP"));
                        }else{

                            fileContent = fileContent.replaceAll("FROM_Instg_BIC", Outward_PACS004_PACS002_FROM_BIC.getOrDefault(country, "FROM_BIC_NOT_SETUP"));
                            fileContent = fileContent.replaceAll("TO_Instd_BIC", Outward_PACS004_PACS002_TO_BIC.getOrDefault(country, "TO_BIC_NOT_SET_UP"));

                        }


                        //Inwards PACS004 KEN Uses swift.iap.02
                        if (country.equalsIgnoreCase("KEN")) {
                            fileContent = fileContent.replaceAll("uniqueBizSvCode", BizsvcUsed);

                        } else {
                           // UniqueBisSvCode = CommonMethods.extract_Data_FromMessage(XmlXpaths.PACS0008_AppHeader_path, "BizSvc");
                            fileContent = fileContent.replaceAll("uniqueBizSvCode", BizsvcUsed);
                        }

                        break;


                }
                try {
                    fileContent = fileContent.replaceAll("PPPP-PP-PP", getPreviousDate());
                } catch (ParseException e) {
                    System.out.println("Invalid date string");
                    e.printStackTrace();
                }
            }

            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed in Common Methods Generate Pacs to string");

        }

        return fileContent + "::" + end2EndId;

    }

    public static String GenerateID(File file, String bic) throws IOException, InterruptedException {
        String fileContent = new String();
        HashMap captureValues = new HashMap<String, String>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            // fileContent = "";
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                fileContent = fileContent.concat(line) + System.lineSeparator();
            }
            //***************************************Normal Transaction********************************************************************************
            if (fileContent.contains("uniqueEndToEndId") || fileContent.contains("uniqueTransactionId") || fileContent.contains("uniqueInstrId") || fileContent.contains("uniqueMsgIdId") || fileContent.contains("uniqueUETR")) {
                Thread.sleep(3000);

                //fileContent = fileContent.replaceAll("uniqueBICS",bic);
                //End2EndID
                end2EndId = generateUniqueString();
                fileContent = fileContent.replaceAll("uniqueEndToEndId", end2EndId.trim());

                //Transaction ID
                transactionId = generateUniqueString();
                fileContent = fileContent.replaceAll("uniqueTransactionId", transactionId);

                //Instructed ID
                instructionID = "INS" + generateUniqueString();
                fileContent = fileContent.replaceAll("uniqueInstrId", instructionID);

                //Intra Bank Settlement Date
                intraBankSettlementDate = getTodayDate();
                fileContent = fileContent.replaceAll("YYYY-MM-DD", intraBankSettlementDate);

                msgId = "MSGID" + generateUniqueString();
                fileContent = fileContent.replaceAll("uniqueMsgIdId", msgId);


                //BizmsgId =generateUniqueString();
                //fileContent = fileContent.replaceAll("uniqueBIZMsgId", BizmsgId);

                uetr = UUID.randomUUID().toString();
                fileContent = fileContent.replaceAll("uniqueUETR", uetr);

                //============================================================================================
                try {
                    fileContent = fileContent.replaceAll("PPPP-PP-PP", getPreviousDate());
                } catch (ParseException e) {
                    System.out.println("Invalid date string");
                    e.printStackTrace();
                }
            }
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileContent;

    }

    public static String generateUniqueString() {
        Date currentDate = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmss");
        return ft.format(currentDate);
    }

    //Get Current Date
    public static String getTodayDate() {
        String pattern = "yyyy-MM-dd";
        DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        System.out.println("date" + date);
        return date;
    }

    public static String getNowTime() {
        String pattern = "HH:mm:ss";
        DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(new Date());
        System.out.println("date" + date);
        return date;
    }

    public String extract_unwanted_string(String frontEndValue) {
        String newFrontEndValue = null;
        String step = "Step: ";
        String outcome = "Outcome: ";
        if (frontEndValue.contains(step))
            newFrontEndValue = frontEndValue.replaceAll(step, "");
        else if (frontEndValue.contains(outcome))
            newFrontEndValue = frontEndValue.replaceAll(outcome, "");
        else
            newFrontEndValue = frontEndValue;

        return newFrontEndValue;

    }

    //Get Previous Date
    public static String getPreviousDate() throws ParseException {

        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Today's date is " + dateFormat.format(cal.getTime()));

        cal.add(Calendar.DATE, -1);
        String yesterday = dateFormat.format(cal.getTime());
        System.out.println("Yesterday's date was " + yesterday);
        return yesterday;

    }

    public static void GetxmlMessageFromDB(String E2EID, String OriginalMessageType, String DBname,String MessageQueue) throws SQLException {
        String Status = "";
        String OriginalMessage = "";
        String Field = "";
        System.out.println("Get orignal GetxmlMessageFromDB for " + E2EID);

        if (DBname.equals("CBSS")) {
            dbConnections.connectToDataBase(DBConstants.cbssDBConnection);

        } else if (DBname.equals("HVPP")) {

            dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
        }
        switch (OriginalMessageType) {
            case "PACS008":
            case "PACS.008":
            case "PAIN001":
            case "PAIN.001":
                OriginalMessageType = "pacs.008";
                break;
            case "PACS009GEN":
            case "PACS.009":
            case "PACS009COV":
            case "PACS.009COV":
                OriginalMessageType = "pacs.009";
                break;

            case "CAMT054":
                OriginalMessageType = "camt.054";
                break;

            case "CAMT050":
                OriginalMessageType = "camt.050";
                break;
        }
        switch(MessageQueue){
            case "Sanctions":
            case "GeneratedMessageOut":
            case "LandingArea":

                try {
                        OriginalMessage = dbConnections.GetSourceMessage(E2EID, System.getProperty("country"), OriginalMessageType.replaceAll("COV", "Cov"), DBname, MessageQueue);

                    } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(OriginalMessage);
                break;

            case "OnsentMessage":
                OriginalMessage = dbConnections.GetOnSentMessage(E2EID);
                System.out.println(OriginalMessage);
                break;

            case "BAPSTRISIM":
                OriginalMessage = dbConnections.Get_BAPS_TRI_Message(E2EID);
                System.out.println(OriginalMessage);
                break;
        }

        //This below replace all the values/ characters so that we are able to get data from xml xpaths
         if (OriginalMessage == null) {
            // Skip this iteration and move to next test case
        }else {
             String myXml = OriginalMessage.replace('[', '.').replace(']', ' ').replaceAll("<!.CDATA.", "").
                     replaceAll("xmlns=\"urn:swift:xsd:envelope\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "").
                     replaceAll("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.009.001.08\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.10\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.050.001.05\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:secl.010.001.03\"", "").
                     replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09\"", "");



             //store the xml data in to a xml file
             try {
                 String filename = Outward_CBSS_Orignal_MESSAGE;
                 FileWriter fw = new FileWriter(filename, false); //the true will append the new data
                 fw.write(myXml);//appends the string to the file
                 fw.close();
             } catch (IOException ioe) {
                 System.err.println("IOException: " + ioe.getMessage());
             }
         }

    }


    public static void Simulate_responses(String E2EID, String MessageType, String IntegrationSystem) throws Exception {
        String dbName = "";
        String Quetype = "";
        String MessageLoc ="";

        if (IntegrationSystem.equalsIgnoreCase("SanctionsHVPP") ||IntegrationSystem.equalsIgnoreCase("BAPSTRISIM")) {
            ExcelValues PACS002excelValues = new ExcelValues();
            List<Map<String, String>> Sanction_PACS002testDataList = PACS002excelValues.readExcelDataAsListOfMaps(TEST_DATA_PACS002, "PACS002_Positive");

            for (Map<String, String> SanctionRowDataPASC002 : Sanction_PACS002testDataList) {
                // Filter only relevant tests
                if (!SanctionRowDataPASC002.getOrDefault("Test", "").contains("HVPPSanctionsPACS002")) continue;

                // Set columnMap (used throughout your framework)
                columnMapSimulation.clear();
                columnMapSimulation.putAll(SanctionRowDataPASC002);  // Use full row as test input
                if(IntegrationSystem.equalsIgnoreCase("SanctionsHVPP")) {

                    dbName = hvppText;
                    Quetype = "SanctionScreen";
                    MessageLoc =HVPP_SANCTIONS_RESPONSE_MESSAGE;
                }else {
                    dbName = hvppText;
                    Quetype ="BAPSTRISIM" ;
                    MessageLoc =BAPS_TRI_RESPONSE_MESSAGE;
                }

                setCAMTMessagesLoc();
                    //BYPASSED
                    String OriginalMessageType = "";
                    switch (MessageType) {
                        case "PACS008":
                        case "PAIN001":
                        case "PACS.008":
                        case "PAIN.001":
                            OriginalMessageType = "PACS.008";
                            dropSimulationMessagesToMQ(MessageLoc, hvppText, columnMapSimulation, E2EID, OriginalMessageType, Quetype, "PACS.002");
                            break;

                        case "PACS009GEN":
                        case "PACS.009":
                        case "PACS009":
                            OriginalMessageType = "PACS.009";
                            dropSimulationMessagesToMQ(MessageLoc, hvppText, columnMapSimulation, E2EID, OriginalMessageType, Quetype, "PACS.002");

                            break;

                        case "PACS009COV":
                        case "PACS.009COV":
                            OriginalMessageType = "PACS.009COV";
                            dropSimulationMessagesToMQ(MessageLoc, hvppText, columnMapSimulation, E2EID, OriginalMessageType, Quetype, "PACS.002");

                            break;

                        case "SECL10Debit":
                        case "SECL10Credit":

                            OriginalMessageType = "secl.010";
                            dropSimulationMessagesToMQ(MessageLoc, hvppText, columnMapSimulation, E2EID, OriginalMessageType, Quetype, "PACS.002");

                            break;
                        case "PACS.004":

                            OriginalMessageType = "PACS.004";
                            dropSimulationMessagesToMQ(MessageLoc, hvppText, columnMapSimulation, E2EID, OriginalMessageType, Quetype, "PACS.002");

                            break;
                    }
                    //SA system is slow, and it takes a minute to process the PACS002 sanctions response
                    //So the thread sleep below will ensure that we do not have timeout failures
                    if (country.equalsIgnoreCase("ZAF")) {
                        Thread.sleep(60000);
                    }
                }
            }
        else {
            List<Map<String, String>> SIMTestData =null;
            String TESTName,SimMessageType ,SimMessagePath="";

        if(IntegrationSystem.equalsIgnoreCase("SETTLEMENTSIM")) {
            ExcelValues PACS002excelValues = new ExcelValues();
            SIMTestData = PACS002excelValues.readExcelDataAsListOfMaps(TEST_DATA_PACS002, "PACS002_Positive");

            TESTName ="SETTLEMENTPACS002";
            SimMessageType ="PACS.002";
            SimMessagePath =PACS002_CBSS_Simulation;
        }else{
            ExcelValues CAMT045excelValues = new ExcelValues();
            SIMTestData = CAMT045excelValues.readExcelDataAsListOfMaps(TEST_DATA_CAMT_MESSAGE, "CAMT054");

            TESTName =MessageType.replace(".","")+country;
            SimMessageType ="CAMT.054";
            SimMessagePath =CAMT_054_MESSAGES;
        }

        for (Map<String, String> rowDataPASC002_camt045 : SIMTestData) {
            // Filter only relevant tests
            if (!rowDataPASC002_camt045.getOrDefault("Test", "").contains(TESTName)) continue;

            // Set columnMap (used throughout your framework)
            columnMapSimulation.clear();
            columnMapSimulation.putAll(rowDataPASC002_camt045);  // Use full row as test input

            //ACCEPTED //SWIFT_ACK //SETTLEMENTACCEPTED
                    //parameter for MessageType ,put in the original message type that you want to do Simulation on e.g PACS008 ,cam.50
                    String OriginalMessageType = "";
                    switch (MessageType) {
                        case "PACS008":
                        case "PAIN001":
                        case "PACS.008":
                        case "PAIN.001":
                            OriginalMessageType = "PACS.008";
                            break;

                        case "PACS009GEN":
                        case "PACS.009":
                            OriginalMessageType = "PACS.009";
                            break;

                        case "PACS009COV":
                        case "PACS.009COV":
                        case "PACS.009Cov":
                            OriginalMessageType = "PACS.009COV";
                            break;
                        case "PACS.004":
                            OriginalMessageType = "PACS.004";
                            break;

                        default:
                            OriginalMessageType=MessageType  ;
                        break;
                    }
                    dropSimulationMessagesToMQ(SimMessagePath, cbssText, columnMapSimulation, E2EID, OriginalMessageType, "UniversalQueue", SimMessageType);

                }
            }

        }

    public static void Get_PACS002_responses_backto_Origin(String E2EID, String Status, String MessageType) {
        String MessageSentOut = "";
        String Field = "";
        String OutMessageName = "";
        System.out.println("Get xmlMessage sent out for " + E2EID);

        if (MessageType.equalsIgnoreCase("PAIN001")) {
            OutMessageName = "pain.002";

            MessageSentOut = dbConnections.GetMessageSentout(E2EID, Status, OutMessageName);
        } else {
            OutMessageName = "pacs.002";
            MessageSentOut = dbConnections.GetMessageSentout(E2EID, Status, OutMessageName);

        }

        // System.out.println(MessageSentOut);
        try {
            if (MessageSentOut.length() > 0) {
                listener.addTestStep(Status + " " + OutMessageName + " has been generated and sent out successfully");

                //This below replace all the values/ characters so that we are able to get data from xml xpaths

                String myXml = MessageSentOut.replace('[', '.').replace(']', ' ').replaceAll("<!.CDATA.", "").
                        replaceAll("xmlns=\"urn:swift:xsd:envelope\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "").
                        replaceAll("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "").
                        replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.02\"", "").
                        replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:head.001.001.01\"", "").
                        replaceAll("xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10\"", "");


                //store the xml data in to a xml file
                try {
                    String filename = Outward_CBSS_Orignal_MESSAGE;
                    FileWriter fw = new FileWriter(filename, false); //the true will append the new data
                    fw.write(myXml);//appends the string to the file
                    fw.close();
                } catch (IOException ioe) {
                    System.err.println("IOException: " + ioe.getMessage());
                }
            } else {
                listener.FailTestStep(Status + " " + MessageType + " is not generated as expected");

            }

        } catch (NullPointerException Ne) {

            listener.FailTestStep(Status + " " + MessageType + " is not generated as expected");

        }
    }

    public static String extract_Data_FromMessage(String XMLPath, String FieldName) throws NullPointerException {

        String Field = null;

        //Get DOM Node for XML
        try {
            File inputFile = new File("src/main/resources/Outwards/Testdata/Orignal_MessageFromDb.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "";
            //Check the message type first and have Xpaths according
            expression = XMLPath;
            NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(
                    doc, XPathConstants.NODESET);

            Field = "NOTHING";

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node nNode = nodeList.item(i);
                System.out.println("\nCurrent Element :" + nNode.getNodeName());

                // Check if FieldName is null or empty
                if (FieldName == null || FieldName.isEmpty() || FieldName == "") {
                    // If FieldName is null/empty, get the text content of the current node
                    Field = nNode.getTextContent();
                } else {

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        System.out.println(FieldName + " : " + eElement.getElementsByTagName(FieldName).item(0).getTextContent());


                        Field = eElement.getElementsByTagName(FieldName).item(0).getTextContent();

                    }
                }

            }

        } catch (Exception e) {
            Field = "NOTHING";
        }

        return Field.trim();
    }
    public static Boolean IsRootElemenEnvelop(String xmlPath) {
       Boolean Flag =false;
        try {
            // Initialize the DocumentBuilderFactory and parse the XML file
            File inputFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();  // Normalize the XML structure

            // Get the root element (docElement)
            Element rootElement = doc.getDocumentElement();

            // Print out the root element's tag name (this is the <Envelope> or <messageWrapper>)
            System.out.println("Root element: " + rootElement.getTagName());

            // Check if the root element starts with <Envelope> or <messageWrapper>
            if (rootElement.getTagName().contains("Envelope")) {
                System.out.println("The XML starts with <Envelope> ");
                Flag =true;
            } else {
                System.out.println("The XML does not start with <messageWrapper>");
                Flag =false;
            }

        } catch (Exception e) {
            e.printStackTrace();  // Handle errors such as invalid XML
        }
        return Flag;
    }

    public static String generateCaseNumber(String Messagetype) {
        String caseNumber="";
        if (System.getProperty("country").equalsIgnoreCase("ZAF"))
            caseNumber = "CZA" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("BWA"))
            caseNumber = "CBW" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("GHA"))
            caseNumber = "CGH" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("UGA"))
            caseNumber = "CUG" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("KEN"))
            caseNumber = "CKE" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("KEN"))
            caseNumber = "CTZ" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("KEN"))
            caseNumber = "CMU" + generateUniqueString();
        else if (System.getProperty("country").equalsIgnoreCase("KEN"))
            caseNumber = "CZM" + generateUniqueString();
        return caseNumber;
    }
    public static void CaseCreation_Validation(String DocqGroup) throws InterruptedException, SQLException {
        String CaseLogStatus = "";
        String BranchCode = "";
        // Get the current date
        LocalDate currentDate = LocalDate.now();

        if (SystemUsed.equalsIgnoreCase("Outwards") && !country.equalsIgnoreCase("UGA")) {
            // Get the day of the year
            int dayOfYear = currentDate.getDayOfYear();
            // Get the last two digits of the year
            int yearLastTwoDigits = currentDate.getYear() % 100;

            // Format as dddyy
            String formattedDayOfYear = String.format("%03d%02d", dayOfYear, yearLastTwoDigits);
            // Print the result
            System.out.println("Formatted day of the year (dddyy): " + formattedDayOfYear);

            if ((CaseLogStatus = validateExpectedLog(DocqGroup, "created successfully")) == null);

            //get the department saved in for the case so that we can run a second query to get the branchcode linked to it
            //Index 6 will have the Department value

            //get branch code Number
           // BranchCode = get_Branch_Code(GetCaseDetails(DocqGroup).get("Department"));
            BranchCode = get_Branch_Code(GetCaseDetails(DocqGroup).get("Department"));

            String Expected_Case_Formatt="";
            switch (country) {
                case "BWA":
                    //Story ::341900
                     Expected_Case_Formatt=country.substring(0, 2) + BranchCode + formattedDayOfYear + "H";
                    if (CaseLogStatus.contains(Expected_Case_Formatt)) {
                        listener.addTestStep("Case number is in an expected Format " + GetCaseDetails(DocqGroup).get("GroupCode"));
                    } else {
                        listener.WarningTestStep("Case number is not in the correct format expected format :: Expected is "+"BARCBWGX " + BranchCode + formattedDayOfYear + "H");
                    }
                    break;
                case "GHA":
                case "MUS":
                case "TZA":
                case "KEN":
                    //Story KEN::402056
                    //Story MUS::468190
                    //Story GHA:PROD CR 394544
                    //Story TZA:Story not found need to check with BA but formatt is the same as MUS and GHA
                    Expected_Case_Formatt =country.substring(0, 2) + BranchCode + formattedDayOfYear + "H";
                    if (CaseLogStatus.contains(Expected_Case_Formatt)) {
                        listener.addTestStep("Case number is in an expected Format " + GetCaseDetails(DocqGroup).get("GroupCode"));
                    } else {
                        listener.WarningTestStep("Case number in the DB  is "+GetCaseDetails(DocqGroup).get("GroupCode")+" and not in the correct format , expected format is ::" + Expected_Case_Formatt);
                    }
                    break;
                case "ZAF":
                    //Story ::99254
                    Expected_Case_Formatt ="H" + country.substring(0, 2) + BranchCode + formattedDayOfYear;
                    if (CaseLogStatus.contains(Expected_Case_Formatt)) {
                        listener.addTestStep("Case number is in an expected Format " + GetCaseDetails(DocqGroup).get("GroupCode"));
                    } else {
                        listener.WarningTestStep("Case number in the DB  is "+GetCaseDetails(DocqGroup).get("GroupCode")+" and not in the correct format , expected format is ::" + Expected_Case_Formatt);
                    }
                    break;
                case "ZMB":
                    //Story ::309071
                    Expected_Case_Formatt="BAF" + BranchCode + formattedDayOfYear;
                    if (CaseLogStatus.contains(Expected_Case_Formatt)) {
                        listener.addTestStep("Case number is in an expected Format " + GetCaseDetails(DocqGroup).get("GroupCode"));
                    } else {
                        listener.WarningTestStep("Case number in the DB  is "+GetCaseDetails(DocqGroup).get("GroupCode")+" and not in the correct format , expected format is ::" + Expected_Case_Formatt);
                    }
                    break;
                default:

                    break;

            }
        }else
        //For inwards @story ::203240
        if (SystemUsed.equalsIgnoreCase("Inwards")) {
            // Extract the year and day of the year
            int yearLastTwoDigits = currentDate.getYear() % 100; // Last two digits of the year
            int dayOfYear = currentDate.getDayOfYear(); // Day of the year (1-365/366)
            // Format as dddyy
            String formattedDayOfYear = String.format("%02d%03d", yearLastTwoDigits, dayOfYear);
            // Print the result
            System.out.println("Formatted day of the year (YYDDD): " + formattedDayOfYear);

            if(GetCaseDetails(DocqGroup).get("GroupCode").contains("C"+country.substring(0, 2)+"000"+formattedDayOfYear)) {
                System.out.println("C"+country.substring(0, 2)+"000"+formattedDayOfYear +" The Case number :: " +GetCaseDetails(DocqGroup).get("GroupCode"));

                listener.addTestStep("Case number is in an expected Format " + GetCaseDetails(DocqGroup).get("GroupCode"));
            }else{
                listener.failStep("Case number is not in the correct format, expected format ::"+ GetCaseDetails(DocqGroup).get("GroupCode") );
                System.out.println("C"+country.substring(0, 2)+"000"+formattedDayOfYear +" The Case number :: " +GetCaseDetails(DocqGroup).get("GroupCode"));

            }
        }

    }
    public static String MopDerivation(String DocqGroup, String BusinessServiceCode, String ExpectedMopType) throws InterruptedException, SQLException {
        String CaseStatus = "";
        String MopDerived = GetCaseDetails(DocqGroup).get("MOPID");


        if (SystemUsed.equalsIgnoreCase("Inwards")) {
            if ((CaseStatus = validateExpectedLog(DocqGroup, "MOP has been derived successfully: ")) == null) ;

            listener.CompareString(ExpectedMopType, MopDerived, "Mop derived in HVPP");
        }
                                            /** OUTWARDS**/
        else
        {
                if (BusinessServiceCode.contains("sarb.samos.") ||
                    BusinessServiceCode.contains("swift.iap."))
                {
                    ExpectedMopType = "RTGSDOM";
                }
                else {
                    ExpectedMopType = "RTGSREG";
                }

            if ((CaseStatus = validateExpectedLog(DocqGroup, "MoP has been derived. Mop: " + ExpectedMopType)) == null) ;

            listener.CompareString(ExpectedMopType, MopDerived, "Mop derived in HVPP");
        }

        return MopDerived;
    }

    public static Boolean Balance_check(String DocqGroup, String Debit,String DebtorAgent_Acc,String Test_Description,String ProcessExcemptionBalanceCheck) throws Exception {
        String CaseLogStatus,AccountType = "";
        Boolean Flag = false;

        String BalanceCheckAccountsTableflag = "";

            switch (country){

                case"ZAF":
                    //query to check the debit account if Balance check is true and element 3 is the Balance check column
                    BalanceCheckAccountsTableflag = GetAccountDetails(Debit, System.getProperty("country")).get(6);

                    break;
                default:
                    if(Test_Description.contains("Account is GL Account")) {

                        AccountType = GetAccountDetails(Debit, System.getProperty("country")).get(0);

                        if (AccountType.equalsIgnoreCase("GENERAL_LEDGER")) {
                            BalanceCheckAccountsTableflag = "G";

                        }
                    } else {
                        //When an account is populated on Debtor agent Account
                        // it will be an account from Local Table and Balance check will not be performed
                        if (DebtorAgent_Acc.isEmpty()){
                            BalanceCheckAccountsTableflag = "t";

                        }else{
                            //if the Debtor agent account is populated then no Balance check should be performed
                            BalanceCheckAccountsTableflag = "f";
                        }
                    }
                        break;

            }

            if (BalanceCheckAccountsTableflag.equalsIgnoreCase("t")) {
                //trim first 3 numbers if the account number has branch code
                if(Debit.length() > 7){
                    Debit =Debit.substring(3, 10);
                }

                if(ProcessExcemptionBalanceCheck.equalsIgnoreCase("t")) {
                    listener.addInfoTestStep("ProcessExcemption Do Balance Check and Earmarking is marked as True/Yes");

                    listener.addInfoTestStep("Balance check for Account " + Debit + " is marked as true");
                    System.out.println("Balance check for Account " + Debit + " is marked as True");
                    if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Balance Check Request-Response for ")) == null);


                    if (CaseLogStatus.contains("Balance Check Request-Response ")) {

                        if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Balance Check Response- Sufficient funds")) == null)
                            ;
                        if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Earmarking of funds - Successful")) == null)
                            ;
                        if ((CaseLogStatus = validateExpectedLog(DocqGroup, "narratives have been added to the accounting entries table")) == null)
                            ;

                        Flag = true;

                    } else {
                        Flag = false;

                    }
                }else{
                    if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Transaction is exempted to Balance Check and Funds Reservation process")) == null);
                    if ((CaseLogStatus = validateExpectedLog(DocqGroup, "narratives have been added to the accounting entries table")) == null);

                    Flag = true;
                }

            } else if(BalanceCheckAccountsTableflag.equalsIgnoreCase("f")) {

                System.out.println("Balance check for Account " + Debit + " is marked as False");
                listener.addInfoTestStep("Balance check for Account " + Debit + " is marked as False");
                Flag =false;
            }else if(BalanceCheckAccountsTableflag.equalsIgnoreCase("G")){

                    System.out.println("Debit Account " + Debit + " is GENERAL lEDGER ");
                    listener.addInfoTestStep("Used Debit Account " + Debit + " is GENERAL lEDGER");
                    Flag =false;
            }



        return Flag;
    }

    public static void String_To_JsonLogBlock(String title, String jsonString, String docqGroup) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            listener.addCodeBlock(title, "❌ " + title + " for DocqGroup: " + docqGroup);
        } else {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement jsonElement = JsonParser.parseString(jsonString);
                String prettyJson = gson.toJson(jsonElement);
                listener.addCodeBlock(title, prettyJson);
            } catch (Exception e) {
                listener.addCodeBlock(title + " (unformatted)", "⚠️ Failed to parse/format JSON. Raw data:\n" + jsonString);
                System.err.println("⚠️ Failed to pretty print JSON: " + e.getMessage());
            }
        }
    }

    public static String Check_Account_Posting(String DocqGroup, String Amount,String Direction,String MessageType) throws Exception {
        MQConnections.setOutwards_PACS_MessagesLoc();//getting the location of the pacs to be used in the Scrip
        String Principal_Amount_AccountingRequest,Charge_Amount_AccountingRequest =null;
        String CaseLogStatus = "";
        String Debit_Account_Type,Credit_Account_Type,ActualMopDerived="";
        String Json_Domicile_ChargesCurrency ="";
        Double Json_Domicile_ChargesAmount =0.0;

        try {
                            if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Accounting response matched")) == null) ;
                            //================================ACCOUNT POSTING REQUEST=====================================================================

                            Principal_Amount_AccountingRequest = dbConnections.GetPostingRecords(DocqGroup, "Request","Principal");
                            String_To_JsonLogBlock("<p style=\"background-color:Orange;\"><strong>***" +Direction+" Account Posting Request </strong></p>",Principal_Amount_AccountingRequest,DocqGroup);

                            try{
                                if(!country.equalsIgnoreCase("ZAF") && !Direction.equalsIgnoreCase("CBSS")){

                                    if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Posting Success")) == null) ;
                                    Thread.sleep(10000);
                                    Charge_Amount_AccountingRequest = dbConnections.GetPostingRecords(DocqGroup, "Request","Charge");

                                    if (Charge_Amount_AccountingRequest == null || Charge_Amount_AccountingRequest.trim().isEmpty()) {
                                        listener.addInfoTestStep("⚠️ Charges JSON request not found for DocqGroup: " + DocqGroup);
                                    } else {
                                        String_To_JsonLogBlock("<p style=\"background-color:Orange;\"><strong>***" + Direction + " Charges Posting Response </strong></p>",
                                                Charge_Amount_AccountingRequest,
                                                DocqGroup
                                        );
                                         Json_Domicile_ChargesAmount = JsonPath.read(Charge_Amount_AccountingRequest,"debit.ccyAmount.amount");
                                         Json_Domicile_ChargesCurrency = JsonPath.read(Charge_Amount_AccountingRequest,"debit.ccyAmount.currency");


                                    }
                                }
                                }catch (Exception e){

                                listener.addInfoTestStep("***No Charge Amount Narratives for "+MessageType+" ********");
                            }

                            String Posting_Request_DebitAccount = JsonPath.read(Principal_Amount_AccountingRequest,"debit.accountNumber");
                            String Posting_Request_creditAccount = JsonPath.read(Principal_Amount_AccountingRequest,"credit.accountNumber");


                            //================================ACCOUNT POSTING Response=====================================================================
                            /**
                             * This is not applicable for inwards cbss Account posting
                             */
                            //get the Json request /response and then get values from the path of Credit or Debit
                            String AccountPostingJson_Response = dbConnections.GetPostingRecords(DocqGroup, "Response", "Principal");
                            String_To_JsonLogBlock("<p style=\"background-color:Orange;\"><strong>***" + Direction + " Account Posting Response </strong></p>", AccountPostingJson_Response, DocqGroup);

                            System.out.println("Extracting Json Debit amount " + Posting_Request_DebitAccount);
                            System.out.println("Currency : " + CountryCurrency);

                            if(con.getMetaData().getURL().contains("CBSS") && SystemUsed.equalsIgnoreCase("Inwards")){
                                                //this is a place holder for inwards for cbss Account posting to ignore calculations
                            }else {


                                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                                String ChargeBearer = Credit_Transfer_Transaction_Info(DocqGroup, Direction).get("Charge_Bearer");
                                listener.addInfoTestStep("<p style=\"background-color:MediumSeaGreen;\">ChargeBearer used is " + ChargeBearer + "</p>");


                                Map<String, String> CaseDetailsRetrieved = GetCaseDetails(DocqGroup);
                                String DBCurrencyUsed = CaseDetailsRetrieved.get("Currency");
                                String DBDomicileCurrencyUsed = CaseDetailsRetrieved.get("DomicileRateQuoteCurrency");

                                String amountStr = CaseDetailsRetrieved.get("Amount");              // returns String
                                String rateStr = CaseDetailsRetrieved.get("DomicileRate");          // returns String
                                String RateTypeUsed = CaseDetailsRetrieved.get("RateType");          // returns String

                                listener.addInfoTestStep("<p style=\"background-color:MediumSeaGreen;\">RateType used is " + RateTypeUsed + " and Rate Amount is " + rateStr + "</p>");

                                // For both debit and credit sides
                                String[] sides = {"Debit", "Credit"};

                                for (String side : sides) {
                                    String Json_accountNumber = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".accountNumber");

                                    Double Json_amountValue = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".amount.amount");
                                    String Json_amount_CurrencyValue = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".amount.currency");

                                    Double Json_ccyAmountValue = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".ccyAmount.amount");
                                    String Json_ccyAmount_CurrencyValue = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".ccyAmount.currency");

                                    Map<String, Object> amountMap = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".amount");
                                    Map<String, Object> ccyAmountMap = JsonPath.read(AccountPostingJson_Response, "requestDetails." + side.toLowerCase() + ".ccyAmount");
                                    String amountJson = gson.toJson(amountMap);
                                    String ccyAmountJson = gson.toJson(ccyAmountMap);


                                    listener.addCodeBlock(side + " Amount JSON", "\"amount\": " + amountJson);
                                    listener.addCodeBlock(side + " CCYAmount_JSON", "\"ccyAmount\": " + ccyAmountJson);


                                    Double PaymentAmount = (amountStr != null && !amountStr.trim().isEmpty())
                                            ? Double.parseDouble(amountStr.trim())  // ✅ parse string to double
                                            : 0.0;

                                  //  Double DomicileRateUsed = (rateStr != null && !rateStr.trim().isEmpty())
                                    //        ? Double.parseDouble(rateStr.trim())
                                      //      : 0.0;


                                    String passColor = "background-color:MediumSeaGreen;";
                                    String failColor = "background-color:Tomato;";

                                    if (DBCurrencyUsed.equalsIgnoreCase(DBDomicileCurrencyUsed)) {
                                        // Same currency: no conversion needed
                                        String description = side + " Posting Amount";

                                        // Subtract charge amount if present
                                        if (Json_Domicile_ChargesAmount != 0.0) {
                                            PaymentAmount = PaymentAmount - Json_Domicile_ChargesAmount;
                                        }

                                        boolean amountMatches = String.valueOf(Json_amountValue).equalsIgnoreCase(String.valueOf(PaymentAmount));
                                        boolean ccyMatches = String.valueOf(Json_ccyAmountValue).equalsIgnoreCase(String.valueOf(PaymentAmount));

                                        if (amountMatches) {
                                            listener.addInfoTestStep("<p style=\"" + passColor + "\"><strong>" + description + " Passed Assertion <br />" +
                                                    "Expected Amount :: " + PaymentAmount + " <br />" +
                                                    "Actual Amount   :: " + Json_amountValue + "</strong></p>");
                                        } else {
                                            listener.addInfoTestStep("<p style=\"" + failColor + "\"><strong>" + description + " Failed Assertion <br />" +
                                                    "Expected Amount :: " + PaymentAmount + " <br />" +
                                                    "Actual Amount   :: " + Json_amountValue + "</strong></p>");
                                        }

                                        description = side + " Posting CCYAmount";
                                        if (ccyMatches) {
                                            listener.addInfoTestStep("<p style=\"" + passColor + "\"><strong>" + description + " Passed Assertion <br />" +
                                                    "Expected CCYAmount :: " + PaymentAmount + " <br />" +
                                                    "Actual CCYAmount   :: " + Json_ccyAmountValue + "</strong></p>");
                                        } else {
                                            listener.addInfoTestStep("<p style=\"" + failColor + "\"><strong>" + description + " Failed Assertion <br />" +
                                                    "Expected CCYAmount :: " + PaymentAmount + " <br />" +
                                                    "Actual CCYAmount   :: " + Json_ccyAmountValue + "</strong></p>");
                                        }

                                    } else {
                                        // Different currency: need conversion
                                        double convertedChargeAmount = 0.0;
                                        String chargeBearer = ChargeBearer.toUpperCase();

                                        if (Json_Domicile_ChargesAmount != 0.0) {
                                            convertedChargeAmount = getCurrencyConvertedAmount(Json_Domicile_ChargesCurrency, DBCurrencyUsed, Json_Domicile_ChargesAmount, " Charges"
                                            );

                                            switch (chargeBearer) {
                                                case "CRED":
                                                case "SHAR":
                                                    listener.addInfoTestStep("<p style=\"" + passColor + "\">Charges Calculation: " +
                                                            "Subtracting " + convertedChargeAmount + " from " + PaymentAmount + " gives new PaymentAmount = " + (PaymentAmount - convertedChargeAmount) + "(" + DBCurrencyUsed + ")</p>");
                                                    PaymentAmount = PaymentAmount - convertedChargeAmount;

                                                    break;

                                                case "DEBT":
                                                    listener.addInfoTestStep("<p style=\"" + passColor + "\">Charges Calculation: " +
                                                            "Adding " + convertedChargeAmount + " to amount gives new PaymentAmount = " + (PaymentAmount + convertedChargeAmount) + "(" + DBCurrencyUsed + ")</p>");
                                                    PaymentAmount = PaymentAmount + convertedChargeAmount;

                                                    break;

                                                default:
                                                    listener.addInfoTestStep("<p style=\"background-color:Orange;\">Unsupported ChargeBearer: " + ChargeBearer + "</p>");
                                                    break;
                                            }
                                        } else {
                                            listener.addInfoTestStep("<p style=\"" + passColor + "\">ChargeBearer used is " + chargeBearer + " and there are no charges to apply</p>");
                                        }

                                        double convertedAmount = getCurrencyConvertedAmount(DBCurrencyUsed, DBDomicileCurrencyUsed, PaymentAmount,  " Principal");

                                        System.out.println(CountryCurrency + " Amount is: " + convertedAmount);

                                        listener.addInfoTestStep("Payment is in currency " + DBCurrencyUsed +
                                                ", Domicile Currency is " + DBDomicileCurrencyUsed +
                                                ", which equals " + convertedAmount + " (" + DBDomicileCurrencyUsed + ")");

                                        boolean amountMatches = String.valueOf(Json_amountValue).equalsIgnoreCase(String.valueOf(PaymentAmount));
                                        boolean ccyMatches = String.valueOf(Json_ccyAmountValue).equalsIgnoreCase(String.valueOf(convertedAmount));
                                        String description = side + (side.equalsIgnoreCase("Credit") ? " Posting Amount" : " Posting CCYAmount Compare");

                                        if (amountMatches) {
                                            listener.addInfoTestStep("<p style=\"" + passColor + "\"><strong>" + description + " Passed Assertion <br />" +
                                                    "Expected Amount :: " + PaymentAmount + " <br />" +
                                                    "Actual Amount   :: " + Json_amountValue + "</strong></p>");
                                        } else {
                                            listener.addInfoTestStep("<p style=\"" + failColor + "\"><strong>" + description + " Failed Assertion <br />" +
                                                    "Expected Amount :: " + PaymentAmount + " <br />" +
                                                    "Actual Amount   :: " + Json_amountValue + "</strong></p>");
                                        }

                                        if (ccyMatches) {
                                            listener.addInfoTestStep("<p style=\"" + passColor + "\"><strong>" + description + " Passed Assertion <br />" +
                                                    "Expected CCYAmount :: " + convertedAmount + " <br />" +
                                                    "Actual CCYAmount   :: " + Json_ccyAmountValue + "</strong></p>");
                                        } else {
                                            listener.addInfoTestStep("<p style=\"" + failColor + "\"><strong>" + description + " Failed Assertion <br />" +
                                                    "Expected CCYAmount :: " + convertedAmount + " <br />" +
                                                    "Actual CCYAmount   :: " + Json_ccyAmountValue + "</strong></p>");
                                        }
                                    }
                                }
                            }
                                if (System.getProperty("SystemType").equalsIgnoreCase("Inwards")) {
                                    switch (country) {
                                        case "BWA":
                                            listener.addInfoTestStep("BWA Inwards Narratives needs to be Implemented");
                                            //Botswana_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "GHA":

                                            Ghana_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "KEN":
                                            listener.addInfoTestStep("KEN Inwards Narratives needs to be Implemented");
                                            //Kenya_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "MUS":
                                            Mauritius_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "TZA":

                                            Tanzania_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "UGA":

                                            Uganda_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "ZAF":
                                            SA_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;

                                        case "ZMB":
                                            listener.addInfoTestStep("ZMB Inwards Narratives needs to be Implemented");
                                            //Zambia_Narratives.InwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);

                                            break;
                                    }
                                } else if (System.getProperty("SystemType").equalsIgnoreCase("Outwards")) {

                                    switch (country) {
                                        case "BWA":
                                            listener.addInfoTestStep("BWA Outwards Narratives needs to be checked");
                                            Botswana_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "KEN":
                                            listener.addInfoTestStep("KEN Outwards Narratives needs to be Implemented");
                                            Kenya_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "GHA":
                                            Ghana_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "MUS":
                                            listener.addInfoTestStep("MUS Outwards Narratives needs to be Implemented");
                                            Mauritius_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "TZA":
                                            Tanzania_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "UGA":
                                            Uganda_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                        case "ZAF":
                                            SA_Narratives.OutwardsNarrativesCheck(MessageType, Posting_Request_DebitAccount, Posting_Request_creditAccount, AccountPostingJson_Response, ActualMopDerived);
                                            break;
                                        case "ZMB":
                                            Zambia_Narratives.OutwardsNarrativesCheck(MessageType, Principal_Amount_AccountingRequest, Charge_Amount_AccountingRequest, Direction);
                                            break;
                                    }
                                }


                    } catch (Exception e) {
                    listener.failStep("Failed in Posting validation " + e.getMessage());

                    }
        return CaseLogStatus;
    }

    public static String Check_WindowManager(String DocqGroup,String dbname) throws Exception {
        String CaseLogStatus ="";

        if(country.equalsIgnoreCase("ZMB") ||
                country.equalsIgnoreCase("KEN")||
                country.equalsIgnoreCase("GHA")||
                country.equalsIgnoreCase("MUS")||
                country.equalsIgnoreCase("UGA"))
        {
            //Counties like ZMB do not have Window Manager component
            listener.addInfoTestStep("Window Manager not implemented for "+country);
            if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Window check bypassed")) == null) ;


        }else
        {
            if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Check processing window request sent")) == null) ;

            try {
                dbConnections.connectToDataBase(DBConstants.cbssDBConnection);

                List<String> WindowManagerResults = dbConnections.WindowManagerApiRequestDetails(DocqGroup);
                String WindowManagerRequestID =WindowManagerResults.get(0);
                String RequestContent = WindowManagerResults.get(1);

                String_To_JsonLogBlock("WindowManager Request",RequestContent,DocqGroup);

                listener.addInfoTestStep("Your WindowManagerApiRequestId is " + WindowManagerRequestID + "<br /> Your WindowManagerApiRequest Contents are " + RequestContent);
                System.out.println("Your WindowManagerApiRequest Contents are " + RequestContent + "\n Your WindowManagerApiRequestId is " + WindowManagerRequestID);

                String ReqCMessageTypeName = JsonPath.read(RequestContent,"MessageType");
                String ReqCurrentServiceCode = JsonPath.read(RequestContent,"BusinessServiceCode");

                listener.addInfoTestStep("Request ReqCurrentWindowName " + ReqCMessageTypeName + " <br /> Request ReqCurrentServiceCode " + ReqCurrentServiceCode);

                // listener.CompareString(MopType,ReqCurrentServiceCode,"Compare the current Service Code for WindowManager");
                System.out.println("Request ReqCurrentWindowName " + ReqCMessageTypeName + " \n Request ReqCurrentServiceCode " + ReqCurrentServiceCode);

                ///================================Window Manager response================================================================
                String ResponseContent = dbConnections.WindowManagerApiRespondsDetails(WindowManagerRequestID);

                String_To_JsonLogBlock("WindowManager Response",ResponseContent,DocqGroup);

                String ResCurrentWindowName = JsonPath.read(ResponseContent,"CurrentWindowName");
                String ResIopen = JsonPath.read(ResponseContent,"IsOpen").toString();

                //=================================Compare Json response results with Expected Result===========================================
                listener.addInfoTestStep("Response Isopen " + ResIopen + "<br /> Response CurrentWindowName " + ResCurrentWindowName);
                listener.CompareString("true", ResIopen, "Window is open ");

                dbConnections.connectToDataBase(DBConstants.hvppDBConnection);
                if ((CaseLogStatus = validateExpectedLog(DocqGroup, "Window check response - Window Open")) == null) ;

            } catch (Exception e) {
                System.out.println("Failed go get WindowManager Data " + e.getMessage());
                listener.failStep("Failed go get WindowManager Data " + e.getMessage());
                CaseLogStatus =null;
            }
        }

        return CaseLogStatus;
    }


    public static boolean SkipTest(String Market,String PACSMessageType) {
                Boolean Flag =true;

                //if Below condition is FALSE the Test will be skipped
                if(
                Market.contains("SADC") && country.equalsIgnoreCase("KEN")||
                Market.contains("SADC") && country.equalsIgnoreCase("GHA")||
                Market.contains("SADC") && country.equalsIgnoreCase("UGA")||

                //ARO does not have Cover messages that are processed, so we need to skip testes
                Market.contains("SADC") && country.equalsIgnoreCase("BWA")&& PACSMessageType.contains("PACS009COV")||
                Market.contains("SADC") && country.equalsIgnoreCase("ZMB")&& PACSMessageType.contains("PACS009COV")||
                Market.contains("SADC") && country.equalsIgnoreCase("TZA")&& PACSMessageType.contains("PACS009COV")||
                Market.contains("SADC") && country.equalsIgnoreCase("MUS")&& PACSMessageType.contains("PACS009COV"))
        {
            Flag =false;
        }

        else if (
                country.equalsIgnoreCase("TZA") && PACSMessageType.equalsIgnoreCase("SECL10Debit")||
                country.equalsIgnoreCase("TZA") && PACSMessageType.equalsIgnoreCase("SECL10Credit")||

                country.equalsIgnoreCase("GHA") && PACSMessageType.equalsIgnoreCase("SECL10Debit")||
                country.equalsIgnoreCase("GHA") && PACSMessageType.equalsIgnoreCase("SECL10Credit")||

                country.equalsIgnoreCase("MUS") && PACSMessageType.equalsIgnoreCase("PACS009COV")||
                country.equalsIgnoreCase("TZA") && PACSMessageType.equalsIgnoreCase("PACS009COV")
                && SystemUsed.equalsIgnoreCase("Outwards")

              //  country.equalsIgnoreCase("UGA") && PACSMessageType.equalsIgnoreCase("PACS009GEN")||
               // country.equalsIgnoreCase("UGA") && PACSMessageType.equalsIgnoreCase("PACS009COV")

        ) {
            Flag =false;
        }
        else if (
                        PACSMessageType.equalsIgnoreCase("CAMT019") && country.equalsIgnoreCase("KEN")||
                        PACSMessageType.equalsIgnoreCase("CAMT019") && country.equalsIgnoreCase("UGA")||
                        PACSMessageType.equalsIgnoreCase("CAMT019") && country.equalsIgnoreCase("GHA")||
                        PACSMessageType.equalsIgnoreCase("Admi004") && country.equalsIgnoreCase("ZAF")||
                        PACSMessageType.equalsIgnoreCase("CAMT019") && Market.contains("RTGSDOM") && !country.equalsIgnoreCase("ZAF"))
        {

                        Flag =false;
        }
        return Flag;
    }

    public static void MesssageGenerationValidation(String EndtoEnd,String PACSMessageType,String MarketStructureType,String dbName,String GenerationType) throws SQLException {
        TestName = "MessageGenerationTest";
        //This hashmap is for stored values on generated sanctions and CBSS message
        HashMap<String, String> Msg_generation_extractedValues = new HashMap<>();
        //Verify fields /Business service code from the message found in Sanctions Queue to check if the correct code is use
        GetxmlMessageFromDB(EndtoEnd, PACSMessageType, dbName, GenerationType);
        if (IsRootElemenEnvelop(Outward_CBSS_Orignal_MESSAGE)) {
            Msg_generation_extractedValues.put("BizSvc", CommonMethods.extract_Data_FromMessage(XmlXpaths.PACS0008_AppHeader_path.replaceAll("/messageWrapper/messages/message/content/", ""), "BizSvc"));
        } else {
            Msg_generation_extractedValues.put("BizSvc", CommonMethods.extract_Data_FromMessage(XmlXpaths.PACS0008_AppHeader_path, "BizSvc"));
            Msg_generation_extractedValues.put("CtgyPurp", CommonMethods.extract_Data_FromMessage(XmlXpaths.PACS0008_Credit_Trans_Info_PmtTpInf_Path + "/CtgyPurp", "Cd"));
            Msg_generation_extractedValues.put("Purp", CommonMethods.extract_Data_FromMessage(XmlXpaths.PACS0008_Credit_Trans_Info_Path + "/Purp", "Cd"));
            Msg_generation_extractedValues.put("SystemTo", CommonMethods.extract_Data_FromMessage(XmlXpaths.MessageWrapper_MetaDeta_Path , "systemTo"));
        }
        ExpectedBizSvcCode = extractMatchingBizSvcCodes(PACSMessageType, Region, country, "MessageGen");
           Msg_generation_extractedValues.replaceAll((k, v) ->
                  (v == null || v.trim().isEmpty() || v.equalsIgnoreCase("NOTHING"))
                          ? "NULL"
                          : v.trim());
        columnMapTestData.replaceAll((k, v) ->
                (v == null || v.trim().isEmpty() )
                        ? "NULL"
                        : v.trim());


            GenerationType="Generated message out to " + Msg_generation_extractedValues.get("SystemTo") ;

            if (country.equalsIgnoreCase("TZA")) {

                //From Story #557024

                if (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("GOVT") && columnMapTestData.get("Purpose_Code").isEmpty()) {
                    listener.CompareString(columnMapTestData.get("CategoryPurp"), Msg_generation_extractedValues.get("CtgyPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                }  if (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("TAXS") && columnMapTestData.get("Purpose_Code").isEmpty()) {
                    listener.CompareString("OTHR", Msg_generation_extractedValues.get("CtgyPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("CategoryPurp"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                }  if (columnMapTestData.get("CategoryPurp").isEmpty() && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("GOVT")) {
                    listener.CompareString(Msg_generation_extractedValues.get("Purp"), columnMapTestData.get("CategoryPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("CategoryPurp"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                }  if (columnMapTestData.get("CategoryPurp").isEmpty() && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("TAXS")) {
                    listener.CompareString(columnMapTestData.get("CategoryPurp"), Msg_generation_extractedValues.get("CtgyPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                } if (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("GOVT") && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("TAXS")) {
                    listener.CompareString("GOVT", Msg_generation_extractedValues.get("CategoryPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                } if (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("TAXS") && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("GOVT")) {
                    listener.CompareString("GOVT", Msg_generation_extractedValues.get("CategoryPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                }  if (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("OTHR") && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("TAXS")) {
                    listener.CompareString("OTHR", Msg_generation_extractedValues.get("CategoryPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");
                }
                else if
                (columnMapTestData.get("CategoryPurp").equalsIgnoreCase("GOVT") && columnMapTestData.get("Purpose_Code").equalsIgnoreCase("GOVT"))
                    listener.CompareString("GOVT", Msg_generation_extractedValues.get("CategoryPurp"), GenerationType +" Category purpose Code");
                    listener.CompareString(columnMapTestData.get("Purpose_Code"), Msg_generation_extractedValues.get("Purp"), GenerationType +" purpose Code");

                }

                listener.CompareString(ExpectedBizSvcCode, Msg_generation_extractedValues.get("BizSvc"), GenerationType +" Business Service Code");


            columnMapTestData.clear();
        }



    public static HashMap<String, String> GetAvailableBalance(String DebitAccount) throws Exception {
        HashMap<String, String> CBGAccountDetails = new HashMap<>();

        disableCertificateValidation();
        final String POST_PARAMS = "{\n" +
                "  \"AccountNumber\":\"" + DebitAccount + "\",\n" +
                "  \"AccountNames\": \"LONG_NAME                   1059626\"\n" +
                "}";

        URL url = new URL("https://aro-autoenrichment.cto-ptt-pan-african-rtgs-qa.cto-shared.270-nonprod.caas.absa.co.za/api/Accounts/search");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("challenge", "YzM3OTQ1N2UtNDk3Ni00ODBjLWEyMGItYmRlYWQ4ZDc0MWNm");
        conn.setRequestProperty("systemId", "SFZQUA==");
        conn.setRequestProperty("CountryCode", country);
        conn.setRequestProperty("client", "rtgs");
        conn.setRequestProperty("docQGroupRefNo", "44162132-748b-4593-b0be-9ed81416b324");

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = POST_PARAMS.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300) ?
                conn.getInputStream() : conn.getErrorStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(responseStream));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        conn.disconnect();

        System.out.println("Response Code: " + status);
        System.out.println("Response: " + content);

        String responseStr = content.toString();

        if (JsonPath.read(responseStr, "$.CoreBankingResponseStatus").equals("ACCOUNT_NOT_FOUND_ERROR")) {
            CBGAccountDetails.put("AvailableBalance", "10000000000");
            CBGAccountDetails.put("AccountCurrency", country); // Default currency fallback
        } else {
            Double balance =0.0;
              //  BigDecimal balanceDecimal = JsonPath.read(responseStr, "$.Accounts[0].CurrentAvailableBalance");
              // balance = balanceDecimal.doubleValue();

             //balance = JsonPath.read(responseStr, "$.Accounts[0].CurrentAvailableBalance");
             //BigDecimal balanceDecimal = BigDecimal.valueOf(balance);

            Number balanceNum = JsonPath.read(responseStr, "$.Accounts[0].CurrentAvailableBalance");
            BigDecimal balanceDecimal = new BigDecimal(balanceNum.toString());
            balance = balanceDecimal.doubleValue();

            String currency = JsonPath.read(responseStr, "$.Accounts[0].AccountCurrency");

            DecimalFormat df = new DecimalFormat("#,##0.00");
            String formattedBalance = df.format(balance + 10).replaceAll(",", "");

            System.out.println("✅ Current Available Balance (raw): " + balance);
            System.out.println("✅ Current Available Balance (formatted): " + formattedBalance);
            System.out.println("✅ Account Currency: " + currency);

            CBGAccountDetails.put("AvailableBalance", formattedBalance);
            CBGAccountDetails.put("AccountCurrency", currency);
        }

        return CBGAccountDetails;
    }

    public static void disableCertificateValidation() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Optional: Disable hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    /**
     * Validates if the expected log exists and has no error.
     * Returns the log result string if found and valid; otherwise returns null.
     *
     * @param docqGroup   The docqGroup being validated
     * @param expectedLog The expected log string to search for
     * @return The actual log string if found and valid; null if missing or error detected
     */
    private static String lastLogSeen = null;
    private static int repeatCount = 0;
    public static String validateExpectedLog(String docqGroup, String expectedLog) throws InterruptedException {
        String logResult = Get_ValidProgressLogLike(docqGroup, expectedLog, errorStatuses);

        if (logResult == null || logResult.isEmpty() || errorStatuses.contains(logResult)) {
            listener.failStep("❌ Expected log not found or error detected: " + expectedLog);
            return null; // signal failure
        }

        /**
         * 🆕 Check for repeated fallback logs (i.e., same log seen in previous step)
         * This helps detect if the case is stuck and not progressing through stages
         */
        if (logResult.equalsIgnoreCase(lastLogSeen)) {
            // If current log is the same as previous one, increment repeat count
            repeatCount++;

            // If it's seen more than once consecutively, treat it as a failure
            if (repeatCount > 1) { // You can raise this threshold to 2+ if needed
                listener.failStep("❌ Case stuck on Log <b>'" + logResult + "'</b>");
                return null; // Signal failure
            }
        } else {
            // New/different log found — reset tracking
            repeatCount = 1;
            lastLogSeen = logResult;
            System.out.println("✔️️The new Last Log status is:: " + lastLogSeen + " with count reset to "+repeatCount);

        }

        // ✅ Return the current progress log (logResult) as a success indicator.
        // This means that:
        //   - The expected log was found in the progress log table
        //   - OR the fallback log was returned, and it passed all validation checks
        //       • It is not one of the known error logs
        //       • It is not a repeating log that indicates the case is stuck
        //
        // Returning this value allows the calling script to:
        //   - Use the returned log for further verification or logging
        //   - Confirm that the case has successfully reached the expected stage in processing
        //
        // Example use case:
        //   If you're checking for "Accounting Matched" and the method returns "Accounting Matched",
        //   it means the system reached that stage and your test can continue.
        //
        // If null had been returned instead, it would mean:
        //   - The expected log wasn't found,
        //   - Or an error/stuck condition was detected (e.g., "Waiting for response" repeated twice),
        //   - So your main script should skip or fail the test case.
        return logResult; // signal success with actual log
    }
    public static boolean  GetListOfLogs(String HVPPDocqGroup,List ExpectedValidation) throws InterruptedException {
        // Validate all expected logs and stop if any errors found
        Map<String, Boolean> logsFound = Get_AllProgressLogsStatus(HVPPDocqGroup, ExpectedValidation, errorStatuses);
        // Collect all logs that failed (i.e., where the value is false)
        List<String> failedLogs = logsFound.entrySet().stream()
                .filter(entry -> !entry.getValue())   // Keep only entries where value is false (log missing or error)
                .map(Map.Entry::getKey)               // Extract the key (log description) from each entry
                .collect(Collectors.toList());        // Collect the keys into a List

        // If there are any failed logs, handle the failure
        /**if (!failedLogs.isEmpty()) {
            // Report an overall failure indicating the test is stopped due to these errors
            listener.failStep("Test stopped due to error/missing logs.");

            return false; // signal failure
        }*/

        // If there are any failed logs, report each one clearly
        if (!failedLogs.isEmpty()) {
            for (String failedLog : failedLogs) {
                listener.WarningTestStep("❌ Expected log NOT found : \"" + failedLog + "\"");
            }

            // Report overall failure after listing individual missing logs
            if(failedLogs.size() >1) {
                listener.failStep("Test stopped due to " + failedLogs.size() + " missing or invalid progress logs.");
                return false; // signal failure
            }else {
                return true;
            }
        }

        return true; // all validations passed
    }
    public static void saveCheckedIdToFile(String filename, String id) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, true))) {
            bw.write(id);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<String> loadCheckedIdsFromFile(String filename) {
        Set<String> ids = new HashSet<>();
        File file = new File(filename);
        if (!file.exists()) return ids;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                ids.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ids;
    }

}

