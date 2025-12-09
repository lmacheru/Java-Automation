
package Functions.MQDBConnections;
import Functions.Utils.CommonMethods;
import Functions.Utils.MQClass;
import Functions.Utils.RTGS_Validations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MQConnections extends MQClass {

    public static String hostIP = null;
    public static String queueManagerName = null;
    public static int queueManagerPort = 0;
    public static String channel = null;
    public static String userID = null;
    public static String mqSend(String system, String destinationQueueName, String payload) {
        MQStatus="";
        MQClass mqClass = new MQClass();

        boolean results = false;
        String response = null;

        mqDetails(system);

        mqClass.connect(hostIP, queueManagerName, queueManagerPort, channel, destinationQueueName, userID);

        System.out.println("Dropping PACS message in  -- " +destinationQueueName);

        response = mqClass.sendMsg(payload, null, null);
        System.out.println("MQ Response " + response);

        MQStatus=response;

        return response;
    }

    protected static void mqDetails(String system) {
        switch (system) {
            case QueueDetails.RTGS_SIT_ENV:
                hostIP = QueueDetails.MQ_HOST_IP_SIT;
                queueManagerName = QueueDetails.RTGS_MQ_MANAGER_NAME_SIT;
                queueManagerPort = QueueDetails.RTGS_MQ_PORT_NUMBER_SIT;
                channel = QueueDetails.RTGS_MQ_CHANNEL_SIT;
                userID = QueueDetails.MQ_USER_ID;
                break;
            case QueueDetails.MORANGWA_SIT_ENV:
                hostIP = QueueDetails.MQ_HOST_IP_SIT;
                queueManagerName = QueueDetails.MORANGWA_MQ_MANAGER_NAME_SIT;
                queueManagerPort = QueueDetails.MORANGWA_PORT_NUMBER_SIT;
                channel = QueueDetails.MORANGWA_MQ_CHANNEL_SIT;
                userID = "";
                break;
            default:
                hostIP = "invalid";
                queueManagerName = "invalid";
                queueManagerPort = 0;
                channel = "invalid";
                userID = "invalid";
                break;
        }
    }


    public static ArrayList<String> GenerateMessagesToFile(String pacsLocation) throws IOException, InterruptedException {
        /* ArrayList<String> endToEndIdList = new ArrayList<String>();*/

        ArrayList<String> instructedIDList = new ArrayList<String>();
        String payload = null;

        File folder = new File(pacsLocation);

        File[] files = folder.listFiles();

        System.out.println("Files in the List --" + Arrays.toString(files));
        for (File file : files) {
            if ( file.getName().endsWith(".txt")) {
                try {
                   /* String[] cars = {
                            "ABMZMZM0XXX",
                            "BARCTZT0XXX",
                            "FIRNNAN0XXX",
                            "FIRNZAJ0XXX",
                            "MCBLMUM0XXX",
                            "CITIZAJ0XXX",
                            "NEDSZAJ0XXX",
                            "BARCZWH0XXX",
                            "AZAMZML0XXX",
                            "SSCBSCS0XXX",
                            "ZNCOZML0XXX",
                            "NMBLZWH0XXX",
                            "FIRNBWG0XXX",
                            "EQBLTZT0XXX",
                            "FIRNSZM0XXX",
                            "RAWBCDK0XXX",
                            "NEDSNAN0XXX",
                            "ZICBZML0XXX",
                            "ECOCZWH0XXX",
                            "BWLINAN0XXX",
                            "FIRNZML0XXX",
                            "FMBZZWH0XXX",
                            "SDSBSZM0XXX",
                            "BAIPAOL0XXX",
                            "FMBZZML0XXX",
                            "FMBZBWG0XXX",
                            "NLCBTZTXXXX",
                            "BNICAOL0XXX",
                            "BCCBAOL0XXX",
                            "RBMAMWM0XXX",
                            "IVESZAJ0XXX",
                            "BARCZML0XXX",
                            "MBOZZWH0XXX"

                    };

                    for(int x=0;x<=35;x++) {*/
                        System.out.println("PACS File --" + pacsLocation + file.getName());

                        String Bic = "";
                        payload = CommonMethods.GenerateID(new File(pacsLocation + file.getName()), Bic);//Same as here the array list will be passed to this method

                        String[] splitPayload = payload.split("::");


                        for (int i = 0; i < 5; i++) {
                            //this removes blank tags on xml
                            System.out.println("removing blank tags on xml");

                            String[] patterns = new String[]{
                                    // This will remove empty elements that look like <ElementName/>

                                    "\\s*<\\w+/>",
                                    // This will remove empty elements that look like <ElementName></ElementName>

                                    "\\s*<\\w+></\\w+>",
                                    // This will remove empty elements that look like
                                    // <ElementName>
                                    // </ElementName>
                                    "\\s*<\\w+>\n*\\s*</\\w+>",
                                    "\\s*<(\\w+).*?>\\s*</\\1>"

                            };

                            for (String pattern : patterns) {
                                Matcher matcher = Pattern.compile(pattern).matcher(splitPayload[0]);
                                splitPayload[0] = matcher.replaceAll("");
                            }
                        }


                        System.out.println("File From Path " + pacsLocation);
                        System.out.println("File Name > " + file.getName());
                        //;
                        System.out.println("Messsage Generated " + splitPayload[0]);


                        //save message to File
                        // Creating an instance of file

                        File path = new File("src/test/resources/AutoGenerated_Message/" + file.getName());

                        //passing file instance in filewriter
                        FileWriter wr = new FileWriter(path);

                        //calling writer.write() method with the string
                        wr.write(splitPayload[0]);

                        //flushing the writer
                        wr.flush();

                        //closing the writer
                        wr.close();
                  //  }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        return instructedIDList;
    }


    public static ArrayList<String> dropMessagesToMQ(String pacsLocation, String dbName, Map<String,String> TestData, String msgtype,String MarketStructure, String QueType) throws IOException, SQLException {
        /* ArrayList<String> endToEndIdList = new ArrayList<String>();*/
        System.out.println("----dropMessagesToMQ --For--" +msgtype );
        System.out.println("----get packs location "+pacsLocation+" --For-- "+msgtype );

        ArrayList<String> instructedIDList = new ArrayList<String>();
        String payload = null;
        String msgType;
        Connect connect = new Connect();

        File folder = new File(pacsLocation);

        File[] files = folder.listFiles();
        System.out.println("----get packs location --For--"+msgtype );

        System.out.println("Files in the List --" + Arrays.toString(files));
        for (File file : files) {
            if (file.getName().contains(msgtype) && file.getName().endsWith(".xml")) {
                try {

                    if (pacsLocation.contains("bulk")) {
                        System.out.println("PACS File --" + pacsLocation + file.getName());
                        payload = CommonMethods.convertBulkPacsToString(new File(pacsLocation + file.getName()));
                        String[] splitPayload = payload.split("::");
                        connect.connectMq(dbName, splitPayload[0],QueType,msgtype);
                        if (instructedIDList.contains(splitPayload[1])) ;
                        else
                            instructedIDList.add(splitPayload[1]);
                    }
                    else {
                        System.out.println("PACS File To be Converted--" + pacsLocation + file.getName());

                        if (pacsLocation.contains("Validation")) {
                            payload = RTGS_Validations.RTGSValidation_PacsToString(new File(pacsLocation + file.getName()), TestData,msgtype,MarketStructure);
                        }  else {
                            payload = CommonMethods.convertPacsToString(new File(pacsLocation + file.getName()), TestData);
                        }

                        String[] splitPayload = payload.split("::");

                        if (msgtype.contains("SECL10")) {
                            //Skip Functionality to remove elements as it makes secl10 not be found in HVPP
                        } else {
                            for (int i = 0; i < 5; i++) {
                                //this removes blank tags on xml
                                System.out.println("removing blank tags on xml");

                                String[] patterns = new String[]{
                                        // This will remove empty elements that look like <ElementName/>

                                        "\\s*<\\w+/>",
                                        // This will remove empty elements that look like <ElementName></ElementName>

                                        "\\s*<\\w+></\\w+>",
                                        // This will remove empty elements that look like
                                        // <ElementName>
                                        // </ElementName>
                                        "\\s*<\\w+>\n*\\s*</\\w+>",
                                        "\\s*<(\\w+).*?>\\s*</\\1>"

                                };

                                for (String pattern : patterns) {
                                    Matcher matcher = Pattern.compile(pattern).matcher(splitPayload[0]);
                                    splitPayload[0] = matcher.replaceAll("");
                                }
                            }
                        }

                        System.out.println("File From Path " + pacsLocation);
                        System.out.println("dataBaseName -->" + file.getName().toString());
                        System.out.println("File Name > " + file.getName());

                        Thread.sleep(10000);

                        //Dropping the payload(PACS message) that was edited in to the MQ


                        connect.connectMq(dbName, splitPayload[0], QueType, msgtype);
                        listener.addCodeBlock(" <p style=\"background-color:DodgerBlue;\"><strong>*** "+msgtype+" XML Message ***</strong></p>",splitPayload[0]);

                        if (instructedIDList.contains(splitPayload[1])) ;
                        else
                            instructedIDList.add(splitPayload[1]);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return instructedIDList;
    }

    /**
     * For Zambia PACS002 simulation we follow this stories
     * 313576
     */
    public static String dropSimulationMessagesToMQ(String pacsLocation, String dbName, Map<String, String> TestData, String E2EID, String Originalmsgtype, String QueType, String SimMessageType) throws SQLException {
        /* ArrayList<String> endToEndIdList = new ArrayList<String>();*/
        ArrayList<String> instructedIDList = new ArrayList<String>();
        String MatedCaseStatus = "";

        String payload = null;
        String msgType;
        Connect connect = new Connect();
        File folder = new File(pacsLocation);
        File[] files = folder.listFiles();
        System.out.println("Files in the List --" + Arrays.toString(files));

        for (File file : files) {
            if ( file.getName().contains(SimMessageType.toUpperCase()) && file.getName().endsWith(".xml")) {
                try {
                        System.out.println("Simulating messages --" + pacsLocation + file.getName());

                        payload = CommonMethods.convertSimPacsToString(new File(pacsLocation + file.getName()),TestData ,E2EID,Originalmsgtype,SimMessageType,dbName);//Same as here the array list will be passed to this method

                        String[] splitPayload = payload.split("::");


                    for(int i=0;i<6;i++) {
                        //this removes blank tags on xml
                        System.out.println("removing blank tags on xml");

                        String[] patterns = new String[]{
                                // This will remove empty elements that look like <ElementName/>

                                "\\s*<\\w+/>",
                                // This will remove empty elements that look like <ElementName></ElementName>

                                "\\s*<\\w+></\\w+>",
                                // This will remove empty elements that look like
                                // <ElementName>
                                // </ElementName>
                                "\\s*<\\w+>\n*\\s*</\\w+>",
                                "\\s*<(\\w+).*?>\\s*</\\1>"

                        };

                        for(String pattern : patterns){
                            Matcher matcher = Pattern.compile(pattern).matcher(splitPayload[0]);
                            splitPayload[0] =matcher.replaceAll("");
                        }
                    }


                        System.out.println("File From Path "+pacsLocation);
                        System.out.println("dataBaseName -->" + file.getName().toString());
                        System.out.println("MQConnections Simulation Message preview \n" +splitPayload[0]);

                        connect.connectMq(dbName, splitPayload[0],QueType,SimMessageType);//Dropping the payload(PACS message) that was edited in to the MQ
                         listener.addCodeBlock("<p style=\"background-color:DodgerBlue;\"><strong>***"+SimMessageType+" XML Message ***</strong></p>",splitPayload[0]);

                    if (instructedIDList.contains(splitPayload[1])) ;
                        else
                            instructedIDList.add(splitPayload[1]);

                        //check if the PACS002 /Camt 54 is matched on the landing area and then return the results on this method
                   /* if(dbName.equalsIgnoreCase("CBSS")) {
                        dbConnections.connectToDataBase(DBConstants.cbssDBConnection);
                    }else
                        dbConnections.connectToDataBase(DBConstants.hvppDBConnection);

                    MatedCaseStatus = dbConnections.GetMatchedCaseStatus(instructedIDList.get(0),"ZAF",SimMessageType);*/
                    ;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Failed to create xml message file");

                }
            }
        }
        return MatedCaseStatus;
    }

    //============================Inwards HASMAP ========================================================

    public static HashMap<String, String> Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow = new HashMap<>();
    public static HashMap<String, String> PACS_MESSAGES_FOR_MOP_Derivation = new HashMap<>();

    //============================OUTWARDS HASMAP ========================================================
    public static HashMap<String, String> PACS_MESSAGES_FOR = new HashMap<>();
    public static HashMap<String, String> EndToEnd_PACS_MESSAGES_VanillaFlow = new HashMap<>();
    public static HashMap<String, String> CBSS_EndToEnd_BAPSTRI_LESAKA_flow = new HashMap<>();

    public static HashMap<String, String> PACS_MESSAGES_FOR_SAMOS = new HashMap<>();
    public static HashMap<String, String> PACS_MESSAGES_FOR_SRS = new HashMap<>();
    public static HashMap<String, String> PACS_MESSAGES_FOR_SAMOS_Trigger = new HashMap<>();
    public static HashMap<String, String> PACS_MESSAGES_FOR_SRSS_Trigger = new HashMap<>();
    public static HashMap<String, String> PACS_Window_And_CuttOff = new HashMap<>();

    public static HashMap<String, String> Credit_Failure_MESSAGES_FOR = new HashMap<>();


    public static HashMap<String, String> CAMT_MESSAGES_FOR = new HashMap<>();
    public static String HVPP_SANCTIONS_RESPONSE_MESSAGE ="src/main/resources/Outwards/Sanctions_PAG_HVPP/";

    public static String BAPS_TRI_RESPONSE_MESSAGE ="src/main/resources/Outwards/Simulation_HVPP_BapsResponse/";

    public static String CAMT_054_MESSAGES ="src/main/resources/Outwards/Camt/CAMT_054/";

    public static String CAMT_019_MESSAGES ="src/main/resources/Outwards/Camt/WindowManager/";

    public static String CAMT_004_MESSAGES ="src/main/resources/Outwards/Camt/CAMT_04/";

    public static String Outward_CBSS_Orignal_MESSAGE ="src/main/resources/Outwards/Testdata/Orignal_MessageFromDb.xml";

    public static String HVPP_Validation_MESSAGES="src/main/resources/Outwards/pacs/Validations/";
    public static String CBSS_Validation_MESSAGES="src/main/resources/Outwards/pacs/Validations/JSON_CBSS_MSG/";

    public static String PACS004_inwards_MESSAGES="src/main/resources/Inwards/pacs/";

    public static String PACS002_CBSS_Simulation="src/main/resources/Outwards/Simulation_CBSS_PACS/";

    public static String EndToEnd_PACS_MESSAGES="src/main/resources/End_To_End_MSG/";



    public static void setInwards_PACS_MessagesLoc() {

        PACS_MESSAGES_FOR_MOP_Derivation.put("ZAF","src/main/resources/Inwards/pacs/SouthAfrica/Mop_Derivation/");
        PACS_MESSAGES_FOR_MOP_Derivation.put("KEN","src/main/resources/Inwards/pacs/Kenya/Mop_Derivation/");


        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("BWA","src/main/resources/Inwards/pacs/Botswana/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("KEN","src/main/resources/Inwards/pacs/Kenya/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("ZAF","src/main/resources/Inwards/pacs/SouthAfrica/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("ZMB","src/main/resources/Inwards/pacs/Zambia/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("TZA","src/main/resources/Inwards/pacs/Tanzania/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("GHA","src/main/resources/Inwards/pacs/Ghana/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("UGA","src/main/resources/Inwards/pacs/Uganda/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("MUS","src/main/resources/Inwards/pacs/Mauritius/VanillaFlowMessages/");
        Inwards_EndToEnd_PACS_MESSAGES_VanillaFlow.put("SYC","src/main/resources/Inwards/pacs/Seychelles/VanillaFlowMessages/");

    }

    public static void setOutwards_PACS_MessagesLoc() {

        EndToEnd_PACS_MESSAGES_VanillaFlow.put("BWA","src/main/resources/Outwards/pacs/Botswana/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("KEN","src/main/resources/Outwards/pacs/Kenya/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("ZMB","src/main/resources/Outwards/pacs/Zambia/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("TZA","src/main/resources/Outwards/pacs/Tanzania/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("UGA","src/main/resources/Outwards/pacs/Uganda/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("GHA","src/main/resources/Outwards/pacs/Ghana/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("MUS","src/main/resources/Outwards/pacs/Mauritius/VanillaFlowMessages/");
        EndToEnd_PACS_MESSAGES_VanillaFlow.put("SYC","src/main/resources/Outwards/pacs/Seychelles/VanillaFlowMessages/");


        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("ZMB","src/main/resources/Outwards/pacs/Zambia/BAPSTRI/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/LESAKA/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("KEN","src/main/resources/Outwards/pacs/Kenya/LESAKA/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("MUS","src/main/resources/Outwards/pacs/Mauritius/LESAKA/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("TZA","src/main/resources/Outwards/pacs/Tanzania/LESAKA/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("GHA","src/main/resources/Outwards/pacs/Ghana/LESAKA/");
        CBSS_EndToEnd_BAPSTRI_LESAKA_flow.put("BWA","src/main/resources/Outwards/pacs/Botswana/LESAKA/");

        PACS_Window_And_CuttOff.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/WindowManager_Cutt_Off/");

        PACS_MESSAGES_FOR.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/VanilaFlowMessages/");

        PACS_MESSAGES_FOR_SAMOS.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/WindowManager/SAMOS/");
        PACS_MESSAGES_FOR_SAMOS_Trigger.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/WindowManager/SAMOS/Trigger/");

        PACS_MESSAGES_FOR_SRS.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/WindowManager/SRS/");
        PACS_MESSAGES_FOR_SRSS_Trigger.put("ZAF","src/main/resources/Outwards/pacs/SouthAfrica/WindowManager/SRS/Trigger/");





    }
    public static void setCAMTMessagesLoc() {

        CAMT_MESSAGES_FOR.put("ZAF","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("ZMB","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("BWA","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("KEN","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("MUS","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("TZA","src/main/resources/Outwards/Camt/CAMT_054/");
        CAMT_MESSAGES_FOR.put("GHA","src/main/resources/Outwards/Camt/CAMT_054/");


    }

}
