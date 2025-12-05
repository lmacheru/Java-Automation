package Functions.Utils.TestRporter;

import Functions.MQDBConnections.MQConnections;
import Functions.Utils.EmailClient;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.aventstack.extentreports.reporter.configuration.ViewName;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static Functions.Utils.Constant.setTestDataLocation;
import static Functions.Utils.Constant.set_Inwards_TestDataLocation;


public class BaseClass {
    public static String country = System.getProperty("country");
    public static String SystemUsed = "";

    public static boolean IsTestLESAKA = false;

    public static String TestName = "";
    public static String CountryCurrency="";
    public static String SADCBizsvcOption = "";
    public static String MQStatus = "";

    public static String GroupTestName = System.getProperty("groups");
    public static String Region = System.getProperty("Region");

    static DateTime dt = new DateTime();
    static DateTimeFormatter fmt = DateTimeFormat.forPattern("dd_MMM_yyyy_hh_mm_ss");
    public static String dateGenerated = fmt.print(dt);

    static DateTimeFormatter formatteddate = DateTimeFormat.forPattern("dd_MMM_yyyy");
    public static String DateExecuted = formatteddate.print(dt);

    public static ExtentReports extent;
    public static ExtentTest logger;
    public static double CaclRate =0.0;
    public static SeleniumListener listener = new SeleniumListener();
    public static String cbssText = "CBSS";
    public static String hvppText = "HVPP";

    @BeforeSuite(alwaysRun = true)
    @Parameters("SystemType")
    public void InitializeTests(String systemType,ITestContext testContext) {
        System.out.println("\n*******INITIALIZING TEST 1********");
        SystemUsed = systemType; // Assign value to static variable for global use

        extent = new ExtentReports();
        ExtentSparkReporter reporter = new ExtentSparkReporter("src/test/resources/Reports/"+country+"/"+country+"_"+SystemUsed+"_Report_"+dateGenerated+".html").viewConfigurer()
                .viewOrder()
                .as(new ViewName[]{
                        ViewName.DASHBOARD,
                        ViewName.TEST,
                        ViewName.CATEGORY,
                        ViewName.DEVICE,
                        ViewName.EXCEPTION,
                        ViewName.LOG,
                })
                .apply();
        reporter.config().setTheme(Theme.DARK);
        reporter.config().setTimelineEnabled(false);

        extent.attachReporter(reporter);
        extent.setSystemInfo("Environment","SIT");
        extent.setReportUsesManualConfiguration(true);


        //getting the location of the pacs to be used in the Scrip
        setTestDataLocation();
        set_Inwards_TestDataLocation();
        MQConnections.setOutwards_PACS_MessagesLoc();
        MQConnections.setInwards_PACS_MessagesLoc();
        CountryCodeToCurrency();

    }
    @AfterSuite(alwaysRun = true)
    public void Cleanup() {
        System.out.println("CLEANUP");
        System.out.println("\n********************************");
        try {
            if (extent != null) {
                System.out.println("Flushing the Extent Report...");
                extent.flush();
            }

            EmailClient emailClient = new EmailClient();
            String Resultsfile = "src/test/resources/Reports/" + country + "/" + country + "_" + SystemUsed + "_Report_" + dateGenerated + ".html";

            String ServerLocation = "\\\\ZADRNBMAPP1422\\RTGS Share\\RTGS_Strategic_Test_RESULTS\\" +
                    SystemUsed + "/" + country + "/" + DateExecuted + "/" +
                    country + "_" + Region + "_" + GroupTestName + "_" + SystemUsed + dateGenerated + ".html";

            // Azure DevOps artifact folder
            String artifactFolder = System.getenv("BUILD_ARTIFACTSTAGINGDIRECTORY");

            File f = new File(Resultsfile);
            File f2 = new File(ServerLocation);

            if (System.getProperty("user.name").contains("SVC-HVRTGS")) {
                /*if (SystemUsed.equalsIgnoreCase("Outwards")) {

                    if (GroupTestName.equalsIgnoreCase("Daily_Run") ||
                            GroupTestName.equalsIgnoreCase("ARO_Daily_Run")) {
                        Region = "";
                    }

                    if (GroupTestName.contains("ContentValidation") ||
                            GroupTestName.contains("SimulationPosting") ||
                            GroupTestName.equalsIgnoreCase("MessageGeneration") ||
                            GroupTestName.equalsIgnoreCase("CreditQueue")) {

                        emailClient.sendEmail(
                                "Outwards RTGS", "Backend Automation Flow", "lehlohonolo.macheru@absa.africa",
                                country + "_" + Region + "_" + GroupTestName + " Backend Automation",
                                "lehlohonolo.macheru@absa.africa", "muzikayise.simela@absa.africa",
                                "src/test/java/Functions/Utils/emailClient_Supporting/emailClient_bodyContent.txt",
                                Resultsfile, true
                        );

                    } else {

                        emailClient.sendEmail(
                                "Outwards RTGS", "Backend Automation Flow", "lehlohonolo.macheru@absa.africa",
                                country + "_" + Region + "_" + GroupTestName + " Backend Automation",
                                "muzikayise.simela@absa.africa", "ntokozo.mhlongo@absa.africa,",
                                "src/test/java/Functions/Utils/emailClient_Supporting/emailClient_bodyContent.txt",
                                Resultsfile, true
                        );
                    }

                } else {
                    emailClient.sendEmail(
                            "Inwards RTGS", "Backend Automation Flow", "lehlohonolo.macheru@absa.africa",
                            country + "_" + Region + "_" + GroupTestName + " Backend Automation",
                            "muzikayise.simela@absa.africa", "ntokozo.mhlongo@absa.afsa",
                            "src/test/java/Functions/Utils/emailClient_Supporting/Inwards_emailClient_bodyContent.txt",
                            Resultsfile, true
                    );
                }*/

                // Copy to shared drive
                FileUtils.copyFile(f, f2);

                // ---------------------------
                //   A Z U R E   A R T I F A C T S
                // ---------------------------
                if (artifactFolder == null) {
                    artifactFolder = "target/artifacts";  // fallback when running locally
                }

                // Ensure folder exists
                Files.createDirectories(Paths.get(artifactFolder));

                String reportFolder = "src/test/resources/Reports/" + country + "/";
                File reportDir = new File(reportFolder);

                // Copy all report HTML files from the report folder to artifact folder
                File[] Azurereports = reportDir.listFiles((dir, name) -> name.endsWith(".html"));
                if (Azurereports == null || Azurereports.length == 0) {
                    System.out.println("No HTML reports found in folder: " + reportFolder);
                    return;
                }

                // Scan all countries
                String sharedDriveBase = "\\\\ZADRNBMAPP1422\\RTGS Share\\RTGS_Strategic_Test_RESULTS\\";

                // Start HTML
                StringBuilder indexHtml = new StringBuilder();
                indexHtml.append("<!DOCTYPE html>\n<html>\n<head>\n")
                        .append("    <meta charset=\"UTF-8\">\n")
                        .append("    <title>RTGS Automation Reports</title>\n")
                        .append("    <style>\n")
                        .append("        body { font-family: Arial, sans-serif; padding: 20px; }\n")
                        .append("        h2 { color: #2E86C1; }\n")
                        .append("        a { font-size: 14px; color: #117A65; text-decoration: none; display:block; margin-left: 20px; margin-bottom: 3px; }\n")
                        .append("        a:hover { text-decoration: underline; }\n")
                        .append("        summary { font-size:16px; cursor: pointer; margin-bottom:5px; }\n")
                        .append("        details { margin-left: 20px; margin-bottom: 5px; }\n")
                        .append("    </style>\n")
                        .append("</head>\n<body>\n")
                        .append("    <h2>RTGS Automation Reports (Shared Drive)</h2>\n");

                File systemDir = new File(sharedDriveBase);
                if (!systemDir.exists() || !systemDir.isDirectory()) {
                    System.out.println("Shared drive path not found: " + sharedDriveBase);
                } else {
                    File[] countryDirs = systemDir.listFiles(File::isDirectory);
                    if (countryDirs != null) {
                        for (File countryDir : countryDirs) {
                            indexHtml.append("<details>\n")
                                    .append("  <summary>").append(countryDir.getName()).append("</summary>\n");

                            File[] dateDirs = countryDir.listFiles(File::isDirectory);
                            if (dateDirs != null) {
                                for (File dateDir : dateDirs) {
                                    indexHtml.append("  <details>\n")
                                            .append("    <summary>").append(dateDir.getName()).append("</summary>\n");

                                    File[] reports = dateDir.listFiles((dir, name) -> name.endsWith(".html"));
                                    if (reports != null) {
                                        for (File report : reports) {
                                            String fileLink = "file:///" + report.getAbsolutePath().replace("\\", "/");
                                            indexHtml.append("      <a href=\"").append(fileLink)
                                                    .append("\" target=\"_blank\">")
                                                    .append(report.getName()).append("</a>\n");
                                        }
                                    }
                                    indexHtml.append("  </details>\n");
                                }
                            }
                            indexHtml.append("</details>\n");
                        }
                    }
                }

                indexHtml.append("</body>\n</html>");

                // Write index.html in artifact folder
                Path indexPath = Paths.get(artifactFolder, "index.html");
                Files.write(indexPath, indexHtml.toString().getBytes(StandardCharsets.UTF_8));
                System.out.println("Created collapsible index.html: " + indexPath.toString());

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @AfterMethod(alwaysRun = true)
    public void checkResults(ITestResult result) {
        if (result != null)
            ProcessTestResult(result);
    }
    private void ProcessTestResult(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE
                || result.getStatus() == ITestResult.SKIP
                && listener.getActiveTestStep() != null) {
            listener.getActiveTestStep().setStatus(Status.FAIL);
            listener.getActiveTestStep()
                    .setAttachmentDisplayName("Test Result");
            if (result.getThrowable().getMessage() != null) {
                listener.getActiveTestStep().setMessage(
                        result.getThrowable().getMessage());
            } else {
                listener.getActiveTestStep().setMessage(
                        result.getThrowable().toString());
            }
        } else {
            if (listener.getActiveTestStep() != null) {
                listener.getActiveTestStep().setStatus(Status.PASS);
            }
        }
    }

    public static String getPreviousDate() throws ParseException {


        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Today's date is " + dateFormat.format(cal.getTime()));

        cal.add(Calendar.DATE, -1);
        String yesterday = dateFormat.format(cal.getTime());
        System.out.println("Yesterday's date was " + yesterday);
        return yesterday;

    }
    public static String DirectionType() {
        String SystemDirection="";
        if(SystemUsed.equalsIgnoreCase("Inwards")){
            SystemDirection ="IN";
        }else{
            SystemDirection ="OUT";

        }

        return SystemDirection;
    }
        public static String CountryCodeToCurrency() {


        switch (country) {
            case "ZMB": // Zambia
                CountryCurrency = "ZMW";
                break;
            case "MUS": // Mauritius
                CountryCurrency = "MUR";
                break;
            case "GHA": // Ghana
                CountryCurrency = "GHS";
                break;
            case "UGA": // Uganda
                CountryCurrency = "UGX";
                break;
            case "TZA": // Tanzania
                CountryCurrency = "TZS";
                break;
            case "BWA": // Botswana
                CountryCurrency = "BWP";
                break;
            case "LSO": // Lesotho
                CountryCurrency = "LSL";
                break;
            case "MDG": // Madagascar
                CountryCurrency = "MGA";
                break;
            case "MWI": // Malawi
                CountryCurrency = "MWK";
                break;
            case "MOZ": // Mozambique
                CountryCurrency = "MZN";
                break;
            case "NAM": // Namibia
                CountryCurrency = "NAD";
                break;
            case "ZAF": // South Africa
                CountryCurrency = "ZAR";
                break;
            case "SWZ": // Eswatini (Swaziland)
                CountryCurrency = "SZL";
                break;
            case "AGO": // Angola
                CountryCurrency = "AOA";
                break;
            case "COM": // Comoros
                CountryCurrency = "KMF";
                break;
            case "DJI": // Djibouti
                CountryCurrency = "DJF";
                break;
            case "SYC": // Seychelles
                CountryCurrency = "SCR";
                break;
            case "ZWE": // Zimbabwe
                CountryCurrency = "ZWL";
                break;
            default:
                CountryCurrency = "Unknown"; // Or handle as needed
                break;
        }

        return CountryCurrency;
    }

}

