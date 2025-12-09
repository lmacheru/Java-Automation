package Functions.MQDBConnections;

import java.util.HashMap;

public class QueueDetails {
    public static final String RTGS_SIT_ENV = "RTGS_SIT";
    public static final String RTGS_MQ_CHANNEL_SIT = "RTGS.SVRCONN";
    public static final String RTGS_MQ_MANAGER_NAME_SIT = "RTGSSIT01AQMGR";


    //Morangwa Queue Details
    public static final String MORANGWA_SIT_ENV = "Morongwa_SIT";
    public static final String MORANGWA_MQ_CHANNEL_SIT = "MOR.SVRCONN";
    public static final String MORANGWA_MQ_MANAGER_NAME_SIT = "MRNGWASIT01";

    //Host, Port and User id for SIT Env
    public static final String MQ_HOST_IP_SIT = "22.144.141.113";
    public static final int RTGS_MQ_PORT_NUMBER_SIT = 3515;
    public static final String MQ_USER_ID = "";
    public static final int MORANGWA_PORT_NUMBER_SIT = 3115;


    public static HashMap<String, String> CBSSMQDrop = new HashMap<>();
    public static HashMap<String, String> BAPSMQDrop = new HashMap<>();

    public static HashMap<String, String> HVPPMQDrop = new HashMap<>();

    public static HashMap<String, String> LESAKAMQDrop = new HashMap<>();
    public static HashMap<String, String> HVPPTOCBSSDrop = new HashMap<>();

    public static HashMap<String, String> BAPSTRISIM = new HashMap<>();

    public static HashMap<String, String> HVPPMQSanctionsScreen = new HashMap<>();
    public static HashMap<String, String> CBSSMQSanctionsScreen = new HashMap<>();
    public static HashMap<String, String> HVPPMQUniversal = new HashMap<>();
    public static HashMap<String, String> CBSSMQUniversal = new HashMap<>();


public static void setQueue() {

    LESAKAMQDrop.put("ZAF","LESAKA.CBSSZAF.OUTWSYS.SCREENPMNT.RES.RCV");
    LESAKAMQDrop.put("KEN","LESAKA.CBSSKEN.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("BWA","LESAKA.CBSSBWA.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("TZA","LESAKA.CBSSTZAABT.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("ZMB","LESAKA.CBSSZMB.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("GHA","LESAKA.CBSSGHA.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("UGA","LESAKA.CBSSUGA.OUTWSYS.SCREENPMNT.REQ.RCV");
    LESAKAMQDrop.put("MUS","LESAKA.CBSSMUMAF.OUTWSYS.SCREENPMNT.REQ.RCV");


    BAPSMQDrop.put("ZMB","BAPSTRI.CBSS.PACS.REQ.ZMB.RCV");

    CBSSMQDrop.put("ZAF","MOR.CBSS.PACS.REQ.ZAF.QLCL");
    CBSSMQDrop.put("ZMB", "MOR.CBSS.PACS.REQ.ZMB.QLCL");
    CBSSMQDrop.put("BWA", "MOR.CBSS.PACS.REQ.BWA.QLCL");
    CBSSMQDrop.put("KEN", "MOR.CBSS.PACS.REQ.KEN.QLCL");
    CBSSMQDrop.put("GHA","MOR.CBSS.PACS.REQ.GHA.QLCL");
    CBSSMQDrop.put("UGA","MOR.CBSS.PACS.REQ.UGA.QLCL");
    CBSSMQDrop.put("MUS", "MOR.CBSS.PACS.REQ.MUMAF.QLCL");
    CBSSMQDrop.put("TZA","MOR.CBSS.PACS.REQ.TZAABT.QLCL");
    CBSSMQDrop.put("SYC","MOR.CBSS.PACS.REQ.SYC.QLCL");

    HVPPTOCBSSDrop.put("BWA","HVPP.CBSS.BWA.RTGS.QLCL");
    HVPPTOCBSSDrop.put("UGA","HVPP.CBSS.UGA.RTGS.QLCL");
    HVPPTOCBSSDrop.put("KEN","HVPP.CBSS.KEN.RTGS.QLCL");
    HVPPTOCBSSDrop.put("ZMB","HVPP.CBSS.ZMB.RTGS.QLCL");
    HVPPTOCBSSDrop.put("ZAF","HVPP.CBSS.RTGS.QLCL");
    HVPPTOCBSSDrop.put("TZA","HVPP.CBSS.TZAABT.RTGS.QLCL");
    HVPPTOCBSSDrop.put("MUS","HVPP.CBSS.MUMAF.RTGS.QLCL");
    HVPPTOCBSSDrop.put("GHA","HVPP.CBSS.GHA.RTGS.QLCL");

    HVPPMQDrop.put("BWA", "MOR.HVPP.PACS.REQ.BWA.QLCL");
    HVPPMQDrop.put("GHA","MOR.HVPP.PACS.REQ.GHA.QLCL");
    HVPPMQDrop.put("KEN","MOR.HVPP.PACS.REQ.KEN.QLCL");
    HVPPMQDrop.put("UGA","MOR.HVPP.PACS.REQ.UGA.QLCL");
    HVPPMQDrop.put("MUS", "MOR.HVPP.PACS.REQ.MUMAF.QLCL");
    HVPPMQDrop.put("TZA","MOR.HVPP.PACS.REQ.TZAABT.QLCL");
    HVPPMQDrop.put("ZAF","MOR.HVPP.PACS.REQ.ZAF.QLCL");
    HVPPMQDrop.put("ZMB", "MOR.HVPP.PACS.REQ.ZMB.QLCL");
    HVPPMQDrop.put("SYC", "ISI.DHVSYC.TODHV.MSG.QLCL");


    HVPPMQUniversal.put("ZAF","MOR.HVPP.UNIVERSAL.ZAF.QLCL");

    CBSSMQUniversal.put("BWA","MOR.CBSS.UNIVERSAL.BWA.QLCL");
    CBSSMQUniversal.put("KEN","MOR.CBSS.UNIVERSAL.KEN.QLCL");
    CBSSMQUniversal.put("ZAF","MOR.CBSS.UNIVERSAL.ZAF.QLCL");
    CBSSMQUniversal.put("ZMB","MOR.CBSS.UNIVERSAL.ZMB.QLCL");
    CBSSMQUniversal.put("TZA","MOR.CBSS.UNIVERSAL.TZAABT.QLCL");
    CBSSMQUniversal.put("UGA","MOR.CBSS.UNIVERSAL.UGA.QLCL");
    CBSSMQUniversal.put("GHA","MOR.CBSS.UNIVERSAL.GHA.QLCL");
    CBSSMQUniversal.put("MUS","MOR.CBSS.UNIVERSAL.MUMAF.QLCL");
    CBSSMQUniversal.put("SYC","MOR.CBSS.UNIVERSAL.SYC.RCV");


    CBSSMQSanctionsScreen.put("ZAF", "MOR.CBSS.PACS.REQ.ZMB.RVC");

    HVPPMQSanctionsScreen.put("BWA", "PAG.HVPPBWA.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("GHA", "PAG.HVPPGHA.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("KEN", "PAG.HVPPKEN.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("UGA", "PAG.HVPPUGA.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("MUS", "PAG.HVPPMUS.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("TZA", "PAG.HVPPTZAABT.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("ZAF", "PAG.HVPPZAF.OUTWSYS.SCREENPMNT.RES.QLCL");
    HVPPMQSanctionsScreen.put("ZMB", "PAG.HVPPZMB.SCREENPMNT.RES.RCV");
    HVPPMQSanctionsScreen.put("SYC", "PAG.HVPPSYC.SCREENPMNT.RES.RCV");


    BAPSTRISIM.put("ZAF", "BAPSTRI.HVPP.PACS002.MSG.QLCL");

}

}

