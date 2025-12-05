package Functions.Utils;

public class XmlXpaths {

    public  static String MessageWrapper_MetaDeta_Path ="/messageWrapper/metadata";

    public static String PACS0008_AppHeader_path ="/messageWrapper/messages/message/content/Envelope/AppHdr";

    public static String PACS0008_AppHeader_path_ToBIC ="/messageWrapper/messages/message/content/Envelope/AppHdr/To/FIId/FinInstnId/";
    public static String PACS0008_GroupHeader_Path="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/GrpHdr";
    public  static String PACS0008_Credit_Trans_Info_PmtId_Path="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtId";
    public static String PACS0008_Credit_Trans_Info_PmtTpInf_Path="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/PmtTpInf";

    public static String PACS0008_Credit_Trans_Info_IntrBkSttlmAmtCCY_Path="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/IntrBkSttlmAmt/@Ccy";
    public static String PACS0008_Credit_Trans_Info_Path="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf";


    public  static String PACS0008_Credit_Trans_Info_RmtInf_Path ="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/RmtInf";
    public  static String PACS0008_Credit_Trans_Info_Creditor_Path ="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Cdtr";
    public  static String PACS0008_Credit_Trans_Info_Debtor_Path ="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/Dbtr";

    public  static String PACS0008_Credit_Trans_Info_InstdAgt_Path ="/messageWrapper/messages/message/content/Envelope/Document/FIToFICstmrCdtTrf/CdtTrfTxInf/InstdAgt/FinInstnId/";

    //===============================================================================================================================

    public  static String PACS0009_AppHeader_path ="/messageWrapper/messages/message/content/Envelope/AppHdr";
    static String PACS0009_GroupHeader_Path="/messageWrapper/messages/message/content/Envelope/Document/FICdtTrf/GrpHdr";

    public static String PACS0009_Credit_Trans_Info_PmtId_Path="/messageWrapper/messages/message/content/Envelope/Document/FICdtTrf/CdtTrfTxInf/PmtId";

    public static String PACS0009_Credit_Trans_Info_IntrBkSttlmAmtCCY_Path="/messageWrapper/messages/message/content/Envelope/Document/FICdtTrf/CdtTrfTxInf/IntrBkSttlmAmt/@Ccy";
    public static String PACS0009_Credit_Trans_Info_Path="/messageWrapper/messages/message/content/Envelope/Document/FICdtTrf/CdtTrfTxInf";


    //=============================================================================================================================================================
    public static String SECL10_Properoiaty ="/messageWrapper/messages/message/content/Envelope/Document/SttlmOblgtnRpt/RptDtls/SttlmOblgtnDtls/FinInstrmId/OthrId/Tp";
    public static String SECL10_FromBIC ="/messageWrapper/messages/message/content/Envelope/AppHdr/Fr/FIId/FinInstnId";

    public static String CAM50_AppHdr_path ="/messageWrapper/messages/message/content/Envelope/AppHdr";
    public static String CAM50_NsgHdr_path ="/messageWrapper/messages/message/content/Envelope/Document/LqdtyCdtTrf/MsgHdr";
    public static String CAM50_Credit_Trans_Info_LqdtyTrfId ="/messageWrapper/messages/message/content/Envelope/Document/LqdtyCdtTrf/LqdtyCdtTrf/LqdtyTrfId";

    //================================================================================================================================================================
    public  static String PACS0004_AppHeader_path ="/messageWrapper/messages/message/content/Envelope/AppHdr";
    static String PACS0004_GroupHeader_Path="/messageWrapper/messages/message/content/Envelope/Document/PmtRtr/GrpHdr";

    public static String PACS0004_PmtRtr_TxInf_Path="/messageWrapper/messages/message/content/Envelope/Document/PmtRtr/TxInf";

    public static String PACS0004_PmtRtr_TxInf_RtrdIntrBkSttlmAmtCCY_Path="messageWrapper/messages/message/content/Envelope/Document/PmtRtr/TxInf/RtrdIntrBkSttlmAmt/@Ccy";
    public static String PACS0004_PmtRtr_TxInf_OrgnlGrpInf_Path="/messageWrapper/messages/message/content/Envelope/Document/PmtRtr/TxInf/OrgnlGrpInf/";

}
