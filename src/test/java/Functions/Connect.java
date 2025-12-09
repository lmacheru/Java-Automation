package Functions.MQDBConnections;


import Functions.Utils.TestRporter.BaseClass;
import static java.lang.Thread.sleep;

public class Connect extends BaseClass{

    public String currentCountry = System.getProperty("country");

    public void connectMq(String system, String payload,String QueType,String PACSMessageType) {
        QueueDetails.setQueue();//getting Queue details
        System.out.println("Drop Payload to MQ ");

        switch (system) {
            case DBConstants.CBSS: {

                switch (QueType){
                    case "None" :
                        String MQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.CBSSMQDrop.get(currentCountry), payload);//Sending the xml to CBSS

                        //Checking that the message was dropped successfully

                        if (MQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Message into CBSS MQ :: " + QueueDetails.CBSSMQDrop.get(currentCountry));
                        }
                        break;

                    // This function is used when we want to Drop PACS002 Responses and CAMT.054 messages
                    case "UniversalQueue" :
                        String UniMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.CBSSMQUniversal.get(currentCountry), payload);//Sending the xml to HVPP

                        //Checking that the message was dropped successfully
                        if (UniMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Message in CBSS :: " + QueueDetails.CBSSMQUniversal.get(currentCountry));
                        }

                        break;
                    case "BAPSTRI":
                        String BapsMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.BAPSMQDrop.get(currentCountry), payload);//Sending the xml to BAPS

                        //Checking that the message was dropped successfully
                        if (BapsMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Messages in CBSS BAPS :: " + QueueDetails.BAPSMQDrop.get(currentCountry));
                        }

                        break;

                    case "SanctionScreen" :
                        String SanctionsMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.CBSSMQSanctionsScreen.get(currentCountry), payload);//Sending the xml to HVPP

                        //Checking that the message was dropped successfully
                        if (SanctionsMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Messages in CBSS :: " + QueueDetails.CBSSMQSanctionsScreen.get(currentCountry));
                        }
                        break;

                    case "LESAKA":
                        String LESAKAMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.LESAKAMQDrop.get(currentCountry), payload);//Sending the xml to BAPS

                        //Checking that the message was dropped successfully
                        if (LESAKAMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Messages in CBSS LESAKA :: " + QueueDetails.LESAKAMQDrop.get(currentCountry));
                        }

                        break;

                    case "HVPPTOCBSS":
                        String HVPPTOCBSSMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.HVPPTOCBSSDrop.get(currentCountry), payload);//Sending the xml to BAPS

                        //Checking that the message was dropped successfully
                        if (HVPPTOCBSSMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Messages in HVPPTOCBSSMQ  :: " + QueueDetails.HVPPTOCBSSDrop.get(currentCountry));
                        }
                        break;
                }

                break;

            }
            case DBConstants.HVPP: {

                switch (QueType){
                    case "None" :
                        String MQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.HVPPMQDrop.get(country), payload);//Sending the xml to HVPP
                        //Checking that the message was dropped successfully
                        if (MQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Message into HVPP MQ:: " + QueueDetails.HVPPMQDrop.get(country));
                        }
                        break;

                    case "UniversalQueue" :
                        // This function is used when we want to Drop PACS002 Responses

                        String UniMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.HVPPMQUniversal.get(currentCountry), payload);//Sending the xml to HVPP

                        //Checking that the message was dropped successfully
                        if (UniMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Universal message in HVPP :: " + QueueDetails.HVPPMQUniversal.get(currentCountry));
                        }
                        break;

                    case "SanctionScreen" :
                        // This function is used when we want to Drop PACS002 PAG Response

                        String SanctionsMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.HVPPMQSanctionsScreen.get(currentCountry), payload);//Sending the xml to HVPP
                        //Checking that the message was dropped successfully

                        if (SanctionsMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Message response in HVPP :: " + QueueDetails.HVPPMQSanctionsScreen.get(currentCountry));
                        }
                        break;

                    case "BAPSTRISIM" :
                        // This function is used when we want to Drop PACS002 PAG Response

                        String BAPSTRISIMMQStatus = MQConnections.mqSend(QueueDetails.RTGS_SIT_ENV, QueueDetails.BAPSTRISIM.get(currentCountry), payload);//Sending the xml to HVPP
                        //Checking that the message was dropped successfully

                        if (BAPSTRISIMMQStatus.equalsIgnoreCase("success")) {
                            listener.addInfoTestStep("Dropping "+PACSMessageType+" Message response in HVPP :: " + QueueDetails.BAPSTRISIM.get(currentCountry));
                        }
                        break;
                }

            }

        }
    }


}


