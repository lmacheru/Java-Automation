package Functions.MQDBConnections;

import java.util.HashMap;

import static Functions.Utils.CommonMethods.*;

public class DBConstants {
    public static final String CBSS = "CBSS";
    public static final String HVPP = "HVPP";
    public static String country =System.getProperty("country");
    public static final String DRIVER_NAME = "org.postgresql.Driver";

    //New CBSS DB Connection
    public static HashMap<String, String> cbssDBConnection = new HashMap<String, String>();
    static {
        try {
            if (!country.equals("ZAF")) {
               // cbssDBConnection.put("url", "jdbc:postgresql://oss-vip-01208.corp.dsarena.com:5432/CBSS");
                cbssDBConnection.put("url", "jdbc:postgresql://rtgs-aro-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/CBSS");

                cbssDBConnection.put("userName", "app_account");
                cbssDBConnection.put("password", "fUGaZQ01iU");
            } else if (country.equals("ZAF")) {
                cbssDBConnection.put("url", "jdbc:postgresql://rtgs-rsa-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/CBSS");
                cbssDBConnection.put("userName", "app_account");
                cbssDBConnection.put("password", "mWDuEH2BHxEPW3WDjFNt");
            }
        }catch (NullPointerException e){
            System.out.println("Running Main class for simulation");

        }
    }

    //New HVPP DB Connection
    public static HashMap<String, String> hvppDBConnection = new HashMap<String, String>();
    static {

        try {
            if (!country.equals("ZAF")) {
                //   hvppDBConnection.put("url", "jdbc:postgresql://oss-vip-01208.corp.dsarena.com:5432/HVPP");
                hvppDBConnection.put("url", "jdbc:postgresql://rtgs-aro-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com/HVPP");

                hvppDBConnection.put("userName", "app_account");
                hvppDBConnection.put("password", "fUGaZQ01iU");
            } else if (country.equals("ZAF")) {

                hvppDBConnection.put("url", "jdbc:postgresql://rtgs-rsa-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/HVPP");
                hvppDBConnection.put("userName", "app_account");
                hvppDBConnection.put("password", "mWDuEH2BHxEPW3WDjFNt");

            }
        }catch (NullPointerException e){
                System.out.println("Running Main class for simulation");
            }
    }

    //New Amber DB Connection
    public static HashMap<String, String> amberDBConnection = new HashMap<String, String>();
    static {
        try {
            if (!country.equals("ZAF")) {
                // cbssDBConnection.put("url", "jdbc:postgresql://oss-vip-01208.corp.dsarena.com:5432/CBSS");
                amberDBConnection.put("url", "jdbc:postgresql://rtgs-aro-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/amber_collector_logs");

                amberDBConnection.put("userName", "app_account");
                amberDBConnection.put("password", "fUGaZQ01iU");
            } else if (country.equals("ZAF")) {
                amberDBConnection.put("url", "jdbc:postgresql://rtgs-rsa-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/amber_collector_logs");
                amberDBConnection.put("userName", "app_account");
                amberDBConnection.put("password", "mWDuEH2BHxEPW3WDjFNt");
            }
        }catch (NullPointerException e){
            System.out.println("Running Main class for simulation");

        }
    }

    /**
     *
     * RATES COnnection
     */
    public static HashMap<String, String> RatesDBConnection = new HashMap<String, String>();
    static {

        RatesDBConnection.put("url", "jdbc:postgresql://rtgs-aro-inwards-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/Rates");
        RatesDBConnection.put("userName", "app_account");
        RatesDBConnection.put("password", "fUGaZQ01iU");

    }
    /**
     *
     * SLIM COnnection
     */
    public static HashMap<String, String> SlimDBConnection = new HashMap<String, String>();
    static {

        SlimDBConnection.put("url", "jdbc:postgresql://rtgs-slim-sit.c7e2a24aqkvn.af-south-1.rds.amazonaws.com:5432/Slim");
        SlimDBConnection.put("userName", "app_account");
        SlimDBConnection.put("password", "na8U3A4uRQEKbctO9e48");

    }
    public static final String SQL_GET_SWIFT_MESSAGE_DOCQGROUP = "select * from \"DocQGroup\" dq where \"ExternalReference\" = ? order by \"CreateDateTime\" asc";

    public static final String SQL_GET_DOCQGROUP_By_CASE_CHAR = "select * from \"DocQGroup\"  dq where \"ExternalReference\" = ? and \"GroupCode\" like ? order by \"CreateDateTime\" asc;";

    public static final String SQL_GET_WindowManagerApiRequest = "select \"WindowManagerApiRequestId\" ,\"RequestContents\" from winm.\"WindowManagerApiRequest\" where \"DocQGroupRefNo\" ='";

    public static final String SQL_GET_WindowManagerApiResponds = "select \"ResponseContents\" from winm.\"WindowManagerApiResponse\" where \"WindowManagerApiRequestId\"='";

    public static final String SQL_GET_SWIFT_MESSAGE_CASENUMBER_BY_CHAR = "select * from \"DocQGroup\" dq where \"ExternalReference\" = ? and \"GroupCode\" like ?";
    public static final String SQL_GET_SWIFT_MESSAGE_CASENUMBER = "select * from \"DocQGroup\" dq where \"ExternalReference\" = ? ";

    public static final String SQL_PROGRESS_Log = "select * from \"QProgress\" q where \"EntityInstRefNo\" ='";

    public static final String SQL_PROGRESS_Log_With_E2E = "select q.* from \"DocQGroup\" dq  join \"QProgress\" q on q.\"EntityInstRefNo\"  = dq.\"DocQGroupRefNo\"  \n" +
            "where \"ExternalReference\" = '";


    public static final String SQL_Camt054Dependent = "select * from \"data\".\"SettlementObligationCamt054Dependent\" socd where \"CountryCode\" =? and \"SchemeID\" =?";

    public static final String SQL_AccountPosting = "select \"Request\", \"Response\"  from fin.\"AccountingRequest\" ar where \"DocQGroupRefNo\" ='";
    public static final String SQL_Charge_Amount_AccountPosting = "select \"Request\" from fin.\"FinPaymentInterfaceCallLog\" fpicl where \"SettlementInstructionRefNo\" in (select \"SettlementInstructionRefNo\" from \n" +
            "fin.\"SettlementInstruction\" si where \"DocQGroupRefNo\" = '";

    //

    public static final String SQL_getRoutingQueue_Details = "select \"Status\" ,\"QueueName\" ,\"TargetSystem\" ,\"RequestMessage\" from \"RoutingQueue\" rq where \"DocQGroupRefNo\" ='";

    public static final String SQL_getProcessExcemption_Data = "select \"AutoRejection\" ,\"DoBalanceCheckEarmarking\" ,* from \"data\".\"ProcessExemptionConfig\" pec where \"CountryCode\" =? " +
                                                                "and \"MessageName\" =? and \"BusinessServiceCode\" =? and \"InitiatingSystem\" like ? ";

    public static final String SQL_CaseDetails = "select cd.\"MOPID\",cd.\"ClientAcctType\" ,cd.\"DebitAccount\" ,cd.\"CreditAccount\" ,dq.\"GroupCode\",\"NonSTPReason\",\"Department\",cd.\"IntiatingSystem\",cd.\"schemeID\" ,cd.\"TransactionStatus\" ,cd.\"BalanceCheckRequired\" ,\"InstructionType\" \n" +
            ",\"Amount\" ,\"Currency\",\"DomicileRate\",\"DomicileRateAppliedAmount\",\"RateAppliedAmount\",\"DomicileRateQuoteCurrency\",cd.\"RateType\",cd.\"DebitAccount\",cd.\"Department\",\"Channel\"  \n" +
            "from \"DocQGroup\" dq  join tri.\"CaseDetail\" cd  on cd.\"DocQGroupRefNo\"  = dq.\"DocQGroupRefNo\"  \n" +
            "where dq.\"DocQGroupRefNo\"  = '";

    public static final String CBSS_SQL_CaseDetails = "select cd.\"MOPID\",cd.\"ClientAcctType\" ,cd.\"DebitAccount\" ,cd.\"CreditAccount\" ,dq.\"GroupCode\",\"NonSTPReason\",\"Department\",cd.\"IntiatingSystem\",cd.\"schemeID\" ,cd.\"TransactionStatus\" ,\"InstructionType\" \n" +
            ",\"Amount\" ,\"Currency\",\"DomicileRate\",\"DomicileRateAppliedAmount\",\"RateAppliedAmount\",cd.\"RateType\",cd.\"DebitAccount\",cd.\"Department\",\"Channel\"  \n" +
            "from \"DocQGroup\" dq  join tri.\"CaseDetail\" cd  on cd.\"DocQGroupRefNo\"  = dq.\"DocQGroupRefNo\"  \n" +
            "where dq.\"DocQGroupRefNo\"  = '";


    public static final String SQL_get_RetailBatch = "select * from data.\"CodesMapping\" cm where \"Proprietary\" =? and \"From\" like ? ";

    public static final String SQL_AccountDetails = "select at2.\"AccountTypeCode\" ,\"AccountName\" ,\"AccountDescription\" ,\"AccountNumber\" ,\"AKA\"  ,\"BIC\", " +
            "\"BalanceCheckRequired\" ,\"IsPreferred\" ,\"IsPreferredCLS\",\"SarsMultitierAcct\" ,\"SafcomAcct\" \n" +
            "from data.\"Account\" a join data.\"AccountType\" at2 on a.\"AccountTypeRefNo\" = at2.\"AccountTypeRefNo\" and a.\"CountryCode\" =? and a.\"AccountNumber\" =?";

    public static final String SQL_GET_Cutt_Off_Details = "select \"CuttOffName\",\"StartProcessingTime\" ,\"CuttOffTime\" ,\"CuttOffTimeExtension\" ,\"CountryCode\" " +
                                                            ",\"Direction\" ,\"TriggerCode\" ,\"ThresholdAmount\" ," +
                                                            "\"CategoryPurposeCode\"  from \"data\".\"CuttOffTimeProfile\" cotp " +
                                                            "where \"MessageType\" like ? and \"TriggerCode\" =? and \"CategoryPurposeCode\" =?";
    public static final String SQL_GET_Collateral_Details = "select \"TransactionNumber\" ,\"Source\" ,\"MessageId\" ,\"Narrative\" ,\"Currency\",\"Amount\" ,\"ABSASSA\" ,\"AvailableFullCapacity\" from public.\"Collateral\" c where  \"MessageId\" = ?";

    public static final String UpdateCircuitBreaker="update fin.\"ResilientPolicyConfiguration\"\n" +
            "set \"Status\"=?\n" +
            "where \"CountryCode\"=?;";
    public static final String SQL_GET_BreakerValue="SELECT * FROM fin.\"ResilientPolicyConfiguration\" where \"CountryCode\" =?";


    public static final String SQL_GET_STATUS_MESSAGE_LANDING_AREA= "select * from swift.\"Swift_Message_LandingArea\" smla  where 1=1 and \"SourceMessage\" like ? \n" +
            "and \"CountryCode\" = ? and \"DateReceived\" ::date = '"+getTodayDate()+"' order by \"DateReceived\" desc;";
    public static final String SQL_GET_LANDING_AREA_Exception_Message = "select * from swift.\"Swift_Message_LandingArea\" where \"CountryCode\" = ? and \"Message\" like ?  and \"Status\" like ?";

    public static final String SQL_GET_CBSS_OriginalXmlMESSAGE_LANDING_AREA = "select \"OriginalMessage\" from swift.\"Swift_Message_LandingArea\" where \"CountryCode\" = ? and \"MessageType\" like ? and \"Message\" like ? limit 1";
    public static final String SQL_GET_CBSS_MT103XmlMESSAGE_LANDING_AREA = "select \"SourceMessage\" from swift.\"Swift_Message_LandingArea\" where \"CountryCode\" = ?  and \"Message\" like ? limit 1";

    public static final String SQL_GET_HVPP_SanctionOutXml ="select \"GeneratedMessage\"  from \"data\".\"SanctionsOutQueue\" soq where \"GeneratedMessage\" like ?";
    public static final String SQL_GET_GeneratedMessageOut= "select \"Status\" ,\"RequestMessage\" ,\"OriginalMessageName\" ,\"Destination\" from data.\"GeneratedMessage\" gm where \"OriginalEndToEndId\" = ? and \"OriginalMessageName\" ilike ?";

    public static final String SQL_GET_Message_Sent_Out = "select \"Status\" , \"OriginalMessageId\" , \"OriginalEndToEndId\" , \"OriginalMessageName\" , \"Direction\", \"DocQGroupRefNo\", \"RequestMessage\" \n" +
            "from data.\"GeneratedMessage\" gm\n" +
            "where \"RequestMessage\" LIKE ? and \"RequestMessage\" like ? and \"OriginalMessageName\" like ?";

    public static final String SQL_GET_OnsentMessage ="select * from public.\"VostroQueue\" vq where \"RequestMessage\" like ? ";

    public static final String SQL_GET_BAPS_TRI_MSG ="select \"Message\" from logs.\"Messages\" m where \"Message\" like ? and \"MessageDirection\" ='OUT' and \"AdditionalData\" like 'HVPP.TOBAPSTRI.MSG.SND'\n";

    public static final String SQL_GET_BranchNumber ="select \"BranchCode\"  from \"Department\" d where \"CountryCode\" =? and \"Code\" =?";

    public static final String SQL_Update_CaseFailedRMA ="update tri.\"CaseDetail\"\n" +
            "set \"DerivedVostroBIC\"  = ? \n" +
            "where \"DocQGroupRefNo\"  in (select \"DocQGroupRefNo\"  from \"DocQGroup\" dq\n" +
            "where \"DocQGroupRefNo\"  = ? ) ;\n" +
            " \n" +
            "update \"DocQGroup\"\n" +
            "set \"CurrentStepRefNo\"  = '771a29be-bee9-45fb-84cc-60ede3cac07c'\n" +
            " ,\"StepRunServer\"  = null\n" +
            " ,\"Completed\"  =  false\n" +
            "where \"DocQGroupRefNo\"  in (select \"DocQGroupRefNo\"  from \"DocQGroup\" dq\n" +
            "where \"DocQGroupRefNo\"  = ? )";

    public static final String Get_Last_CLOSED_Case_Id ="select  ctti.\"End_To_End_ID\",\"GroupCode\" ,smla.\"MessageType\",dq.\"Completed\",\"Business_service\", dq.\"Status\",dq.\"CountryCode\",smla.\"SourceQueueName\",\"CreateDateTime\",\"Interbnk_Settlmnt_Amt\"\n" +
            "    from \"DocQGroup\" dq\n" +
            "    left join swift.\"Credit_Transfer_Transaction_Info\" ctti on dq.\"DocQGroupRefNo\" = ctti.\"DocQGroupRefNo\"\n" +
            "    left join swift.\"Return_Of_Funds_Credit_Transfer\" rofct on dq.\"DocQGroupRefNo\" = rofct.\"DocQGroupRefNo\"\n" +
            "    left join swift.\"BusinessApplicationHeader\" bah on ctti.\"MsgRefNo\" = bah.\"MsgRefNo\" or rofct.\"MsgRefNo\" = bah.\"MsgRefNo\"\n" +
            "    join swift.\"Swift_Message_LandingArea\" smla on ctti.\"MsgRefNo\" = smla.\"MsgRefNo\" or rofct.\"MsgRefNo\" =smla.\"MsgRefNo\"\n" +
            "    where smla.\"Direction\" = ? and dq.\"CountryCode\" =? and smla.\"MessageType\" =? and bah.\"Business_service\" like ? ";

    public static final String Get_Case_Id_To_Simulate ="select  ctti.\"End_To_End_ID\",\"GroupCode\" ,smla.\"MessageType\",dq.\"Completed\", dq.\"Status\",dq.\"CountryCode\",\"CreateDateTime\",\"Interbnk_Settlmnt_Amt\",\"ExternalReference\"\n" +
            "    from \"DocQGroup\" dq\n" +
            "    left join swift.\"Credit_Transfer_Transaction_Info\" ctti on dq.\"DocQGroupRefNo\" = ctti.\"DocQGroupRefNo\"\n" +
            "    left join swift.\"Return_Of_Funds_Credit_Transfer\" rofct on dq.\"DocQGroupRefNo\" = rofct.\"DocQGroupRefNo\"\n" +
            "    left join swift.\"BusinessApplicationHeader\" bah on ctti.\"MsgRefNo\" = bah.\"MsgRefNo\" or rofct.\"MsgRefNo\" = bah.\"MsgRefNo\"\n" +
            "    join swift.\"Swift_Message_LandingArea\" smla on ctti.\"MsgRefNo\" = smla.\"MsgRefNo\" or rofct.\"MsgRefNo\" =smla.\"MsgRefNo\"\n" +
            "    where smla.\"Direction\" = ? and dq.\"CountryCode\" =? and smla.\"MessageType\" =?  ";

    public static final String SQL_GET_RateDB ="select  *  from \"Rates\" r where \"BaseCurrency\"= ? and \"QuoteCurrency\" =? and \"CountryCode\" =?";
    public static final String SQL_GET_HVPP_RateType = "select * from rates.\"RateType\" rt where \n" +
            "\"CurrencyPair\"= ? and \"Direction\" = ? and \"CountryCode\" = ?;";
    public static final String SQL_GET_RateRanking ="select \"Rank\" from \"CurrencyRanking\" cr where \"CurrencyCode\" = ?;";
    public static final String SQL_Get_Cal_Charge_Validation_Details = "select public.\"dummy_charges_calculation\"" +
            "(?,?,?,?,?,'N','N',?,?,?,0.00);\n";
    public static final String SQL_Get_Flat_R_Waive_Charges = "select \"CreditChargesCurrency\",\"CreditChargesAmount\" from charges.\"Charges\" c,public.\"DocQGroup\" dq, tri.\"CaseDetail\" cd \n" +
            "                where dq.\"GroupCode\"= ? and c.\"DocQGroupRefNo\" = dq.\"DocQGroupRefNo\" and\n" +
            "                cd.\"DocQGroupRefNo\" = dq.\"DocQGroupRefNo\" and UPPER(c.\"ChargesType\") = upper(trim(replace(cd.\"ClientTypeCode\", ' ', '_'))|| trim(?));";
    public static final String SQL_Get_Charges_Rate = "select public.dummy_charges_rate_retrieval(?,?,?,?);";

    public static final String SQL_GET_Credit_Transfer_Transaction_Info ="select \"RemittanceInfo_ID\",\"End_To_End_ID\",\"Category_Purpose_Code\",\"Purpose\"  ,\"Charge_Bearer\",\"UETR\" ,* \n" +
            "from swift.\"Credit_Transfer_Transaction_Info\" ctti where \"DocQGroupRefNo\" = ";

}
