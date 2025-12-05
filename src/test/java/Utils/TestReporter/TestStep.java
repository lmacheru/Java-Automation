
package Functions.Utils.TestRporter;

import com.aventstack.extentreports.Status;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public class TestStep {

    private String description = "";
    private String attachmentLink = "";
    private String attachmentDisplayName = "";
    private Status status;
    private String timestamp = "";
    public static int STEP_NUMBER=0;
    private int stepNumber=0;
    public String getMessage() {
        return message;
    }
    public int getStepNumber() {return this.stepNumber;}
    public void setStepNumber(int i) { this.stepNumber = i;}

    public void setMessage(String message) {
    	
        this.message = (!message.contains("Build Info"))?message:message.substring(0, message.indexOf("Build info"));
    }

    private  String message = "";

    public TestStep() {
    }

    public TestStep(String description, String attachmentLink, String attachmentDisplayName, Status status, String timestamp) {
        this.description = description;
        this.attachmentLink = attachmentLink;
        this.attachmentDisplayName = attachmentDisplayName;
        this.status = status;
        this.timestamp = timestamp;
    }

    public TestStep(String description, String attachmentLink, String attachmentDisplayName, String status, String timestamp) {
        this.description = description;
        this.attachmentLink = attachmentLink;
        this.attachmentDisplayName = attachmentDisplayName;

        if (status == null) {
            this.status = null;
        } else if (status.trim().length() == 0) {
            this.status = null;
        } else if (status.equalsIgnoreCase("Passed")) {
            this.status = Status.PASS;
        } else if (status.equalsIgnoreCase("Failed")) {
            this.status = Status.FAIL;
        }
        this.timestamp = timestamp;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttachmentLink() {
        return attachmentLink;
    }

    public void setAttachmentLink(String attachmentLink) {
        this.attachmentLink = attachmentLink;
    }

    public String getAttachmentDisplayName() {
        return attachmentDisplayName;
    }

    public void setAttachmentDisplayName(String attachmentDisplayName) {
        this.attachmentDisplayName = attachmentDisplayName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TestSteps{" + "description=" + description + ", attachmentLink=" + attachmentLink + ", attachmentDisplayName=" + attachmentDisplayName + ", status=" + status + ", timestamp=" + timestamp + '}';
    }


}
