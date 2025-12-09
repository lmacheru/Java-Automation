package Functions.MQDBConnections;

import java.util.Arrays;
import java.util.List;

public class SearchLog {
    public static List<String> AccountDerivationexpectedLogs = Arrays.asList(
            "Debit account number has valid status",
            "SDMC payment identified",
            "No CIF lookup required for Charge Account Type: GENERAL_LEDGER",
            "",
            "Credit Account has been derived."
    );

    public static List<String> validStatuses = Arrays.asList(
            "Debit account number has valid status",
            "SDMC payment identified",
            "No CIF lookup required for Charge Account Type: GENERAL_LEDGER"
    );

    public static List<String> ExpectedValidationLogs = Arrays.asList(
            ""
    );

    public static List<String> errorStatuses = Arrays.asList(
            "Generated message failed validation",
            "Failed - Business Rule Validation",
            "ACCOUNT_NOT_FOUND_ERROR",
            "Failed – RTGS Content Validation",
            "Unable to derive the debit account",
            "Debit account has Not been derived",
            "Account Posting Failure",
            "FailedWindowCheck",
            "Account type mismatch",
            "Settlement Agent Not Found",
            "Failed Sanctions",
            "No Response received",
            "FAILEDVALUEDATE",
            "Account posting response Insufficient funds",
            "Rejected To Business Exception",
            "Balance Check Response-Insufficient funds",
            "Balance Request Error",
            "Unable to derive the credit account",
            "Failed to send to CBSS",
            "ValueDate Validation Failed",
            "Perform RTGS Content Validation – Failed",
            "Perform RTGS Content Validation – Failed - RTGS Content.",
            "Unable to derive the debit account- ACCOUNT_NOT_FOUND_ERROR",
            "Routing to Step Business Exceptions. Outcome selected Rejected To Business Exception Assignment Type PULL Outcome Reason Text: Debit account has Not been derived",
            "Routing to Step Business Interventions. Outcome selected Rejected To Business Exception Assignment Type PULL Outcome Reason Text:",
            "Routing to Step Business Exceptions. Outcome selected Rejected To BusinessExecption Out massage gateway Assignment Type PULL Outcome Reason Text:",
            "Routing to Step Business Exceptions. Outcome selected Rejected To BusinessExecption Out massage gateway Assignment Type PULL Outcome Reason Text: Failed to send to CBSS",
            "Routing to Step Business Interventions. Outcome selected Rejected To Business Intervention Assignment Type PULL Outcome Reason Text: Unable to derive the debit account",
            "Routing to Step Business Interventions. Outcome selected Rejected To Business Intervention Assignment Type PULL Outcome Reason Text:",
            "Routing to Step Accounting Circuit Breaker Hold Outwards",
            "Auto Reject: Case Rejected due to Insufficient Funds",
            "Failed to get the settlement agent",
            "Settlement Amount of zero or less than zero is not allowed. Calculated Settlement Amount is:",//this is when amount is less than the charge e.g Principal 1 and charge is 15
            "PACS.002 response message status: SETTLEMENTREJECTED",
            //Inwards Errors
            "Account Not Found",
            "SanctionsSLABreach",
            "Account Name Match < Threshold",
            "Derive Credit Account - Failed",
            "MoP Derivation Failure",
            "Cap Limit Breached",
            "MOP derivation for the transaction has failed.",
            "One Account No. - Multiple Branches Matched"



    );
}

