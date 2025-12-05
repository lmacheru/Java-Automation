package Functions.Utils;

import Functions.Utils.TestRporter.BaseClass;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Enumeration;

public class MQClass extends BaseClass {
	private static MQQueueManager queueManager = null;
	private static String queueManagerName = null;
//	private static String replyToQueueName = null;
	private static String destinationQueueName = null;
//	private static String messageID;
    private static MQMessage mqMessage = null;
    private static MQPutMessageOptions pmo = null;
	private String usage ="Invalid number of arguments\n******************************\n" + 
			"args[0] : Host\n"+
			"args[1] : Queue Manager Name\n"+
			"args[2] : Queue Manager Port\n"+
			"args[3] : Queue Manager Channel\n"+
			"args[4] : Queue Name\n"+
			"args[5] : User ID\n"+
			"args[6] : action( send, receive, findby, qdepth, folder, watch, clear, countduplicates, sendwithmidandcid, findbycorrelationID )\n"+
			"args[7] : payload when send, timeout when receive(in millis), searchString when findBy, folderToSend when folder, fileToSend when file\n "+
					  "timeout when watch(in millis), correlationID when findbycorrelationID\n"+
		    "args[8] : messageID when sendwithmidandcid\n"+
		    "args[9] : correlationID when sendwithmidandcid, consume/dontconsume when findbycorrelationID\n";
	 private static JmsFactoryFactory ff ;
	    private static JmsConnectionFactory cf ;
	    private static JMSContext context;
	public void connect(String hostIP, String qManager, int queueManagerPort, String channel, String queue,String userID){
        MQEnvironment.port = queueManagerPort;
        MQEnvironment.hostname = hostIP; 
        MQEnvironment.userID = userID;
        MQEnvironment.channel = channel;
        queueManagerName = qManager;
        destinationQueueName = queue;
        try {
        	MQClass.queueManager = new MQQueueManager(qManager);
		} catch (MQException e) {
			e.printStackTrace();
			listener.failStep("Test Failed Due to MQ Exception"+e.getMessage() );

		}
	}
		
	public String processAction(String[] args) {
		String returnString = "Not Processed";
		try {
			System.out.println("MQClass - Reading Args");
			for (int i=0;i< args.length;i++) {
				System.out.println("Args[" + i + "] is " + args[i]) ;
			}
			if (args.length == 0) {
				return usage;
								}
			System.out.println("Calling connect");
			connect(args[0],args[1],Integer.parseInt(args[2]),args[3],args[4],args[5]);
			System.out.println("Checking action[" + args[6]+"]");
			switch(args[6].toLowerCase()) {
				case "send":{
					returnString = sendMsg(args[7],null,null);
					break;
				}
				case "receive":{
					if(args[7] == null || "".equalsIgnoreCase(args[7]))
					{
						returnString = "Action [" + args[6] +"] requires an additional parameter(Maximum Wait Time(in millis))";
					}else {
						returnString = getMessage(true, args[7]);
					}
					break;
				}
				case "findby":{
					if(args[7] == null || "".equalsIgnoreCase(args[7]))
					{
						returnString = "Action [" + args[6] +"] requires an additional parameter(Search Criteria)";
					}else {
						returnString = getMessageByID("20000",getMessageIDUsingFilter("20000", args[7]));
					}
					break;
				}
				case "findbycorrelationid":{
					if(args[7] == null || "".equalsIgnoreCase(args[7]) || args[10] == null || "".equalsIgnoreCase(args[10])){
						returnString = "Action [" + args[6] +"] requires additional parameters(Correlation ID to search for, 'consume' to take the message off the queue, 'dontconsume' to leave it on the queue.)";
					}else {
						returnString = getMessageByCorrelationID("20000", args[7], args[10]);
					}
					break;
				}
				case "qdepth":{
					returnString = getQueueDepth();
					break;
				}	
				case "folder":{
					returnString = sendFolder(args[7]);
					break;
				}
				case "watch":{
					returnString = getNonConsumedMessage(args[7]);
					break;
				}
				case "clear":{
					returnString = clearQueues(args[4]);
					break;
				}
				case "countduplicates":{
					returnString = getMessageDuplicateCount("20000", args[7]) +"";
					break;
				}
				case "sendwithmidandcid":{
					if(args[7] == null || "".equalsIgnoreCase(args[7]) || 
							args[8] == null || "".equalsIgnoreCase(args[8]) ||
							args[9] == null || "".equalsIgnoreCase(args[9])){
						returnString = "Action [" + args[6] +"] requires additional parameters(Payload, MessageID to set, CorrelationID to set)";
					}else {
						returnString = sendMsg(args[7],args[8],args[9]);
					}
					break;
				}
				case "file":{
					returnString = sendFile(args[7]);
					break;
				}
				
				default:{
					returnString =  usage;
					
				}
			}
		}catch(Exception e) {
			returnString = e.getMessage();
		}
		return returnString;
	}
	
public String sendFile(String fileName) {
	StringBuffer sb = new StringBuffer();
	try {
			File f = new File(fileName);
			int c = 0;
			FileInputStream in = new FileInputStream(f);
			while( (c = in.read() ) != -1 ) {
				sb.append((char)c);
			}
			System.out.println("Sending message\n" + sb.toString());
			sendMsg(sb.toString(),null,null);
			sb.setLength(0);
			in.close();
		
		System.out.println("Success. File sent");

	}catch(Exception e) {
		e.printStackTrace();
		return e.getMessage();
	}
	return sb.toString();
}
	
	public String sendFolder(String folderName) {
		String returnString = "Folder not sent";
		try {
			if (new File(folderName).isDirectory()) {
				File[] files = new File(folderName).listFiles();
				for (File f : files) {
					int c = 0;
					FileInputStream in = new FileInputStream(f);
					StringBuffer sb = new StringBuffer();
					while( (c = in.read() ) != -1 ) {
						sb.append((char)c);
					}
					sendMsg(sb.toString(),null,null);
					sb.setLength(0);
					in.close();
				}
				returnString = "Success. "+files.length + " messages sent";
			}else {
				returnString = "The given folder [" + folderName +"] is not a folder.";
			}
		} catch (Exception e) {
			returnString = "Exception occured. " + e.getMessage();		
		}
		return returnString;
	}
	
	public String clearQueues(String queueName){
		String returnValue = "";
		try{
			MQQueue queue = null;
			queueManager = new MQQueueManager(queueManagerName);
	        int openOptions = MQConstants.MQOO_INPUT_SHARED + MQConstants.MQOO_INQUIRE;      
	        queue = queueManager.accessQueue(queueName, openOptions, null, null, null);
	        int depth = queue.getCurrentDepth();
	        int i=0;
	        while(i <= depth){
	        	try{
		        	MQGetMessageOptions gmo =  new MQGetMessageOptions();
			        gmo.options = CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING; 
			        gmo.waitInterval = 1000;
			 	    mqMessage = new MQMessage();
			        queue.get(mqMessage, gmo);
			        mqMessage = null;
			        i++;
	        	}catch(Exception e)
	        	{
	        		break;/*No more messages, Do nothing */
	        	}
	        }
	    	queue.close();
	    	returnValue = queueName + " Cleared Successfully"; 
		}catch(Exception e){
			returnValue = "Cannot clear Queue. " + e.getMessage();
		}
		return returnValue;
	}

	public String getMessage(boolean consume, String maximumWaitTime){
		MQQueue queue = null;
		String returnMessage = null;
		try{
			queueManager = new MQQueueManager(queueManagerName);
			   int openOptions = 0;
	            if(consume) {
	            	openOptions = MQConstants.MQOO_INPUT_SHARED + MQConstants.MQOO_INQUIRE;      
	            }else {
	            	openOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_BROWSE;
	            }  
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
            MQGetMessageOptions gmo =  new MQGetMessageOptions();
            if (consume) {
            	gmo.options =  CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING;	
            }else {
            	gmo.options =  CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_CONVERT + CMQC.MQGMO_BROWSE_NEXT;
            }
            if (Integer.parseInt(maximumWaitTime) > 0 )
            	gmo.waitInterval = Integer.parseInt(maximumWaitTime);
     	    mqMessage = new MQMessage();
            queue.get(mqMessage, gmo);
            returnMessage = mqMessage.readStringOfByteLength(mqMessage.getDataLength());
        	queue.close();
		}catch(Exception e){
			  if ( e.getMessage().contains("2033")) {
				  returnMessage = "Wait time exceeded, No messages on queue";
			  }else {
				  returnMessage = e.getMessage();
			  }
		}finally {
            try {
                if (queue != null)
                    queue.close();
                if (queueManager != null)
                    queueManager.close();
                
            } catch (MQException e) {
            }
        }
		return returnMessage;
	}
	public String getQueueDepth(){
		MQQueue queue = null;
		String returnMessage = null;
		try{
			queueManager = new MQQueueManager(queueManagerName);
			int openOptions = MQConstants.MQOO_INPUT_SHARED + MQConstants.MQOO_INQUIRE;
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
	        returnMessage = queue.getCurrentDepth()+"";
		}catch(Exception e) {
			
		}
		return returnMessage;
	}
	
	public String getNonConsumedMessage(String maximumWaitTime) {
		//System.out.println("Filter is " + filter);
		MQQueue queue = null;
		String returnMessage = "No Message";
		try{
			queueManager = new MQQueueManager(queueManagerName);
			int openOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_BROWSE;
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
    	  MQGetMessageOptions gmo =  new MQGetMessageOptions();
          gmo.options =  CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_CONVERT + CMQC.MQGMO_BROWSE_NEXT ;
          gmo.waitInterval = Integer.parseInt(maximumWaitTime);
   	      mqMessage = new MQMessage();
          queue.get(mqMessage, gmo);
          
          returnMessage = mqMessage.readStringOfByteLength(mqMessage.getDataLength());
        }catch(Exception e) {
        	
        }
		return returnMessage;
	
	}
	
	public int getMessageDuplicateCount(String maximumWaitTime,String filter){
		//System.out.println("Filter is " + filter);
		MQQueue queue = null;
		int count = 0;
		try{
			queueManager = new MQQueueManager(queueManagerName);
			int openOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_BROWSE;
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
	        int depth = queue.getCurrentDepth();
	      //  System.out.println("Depth is "+ depth);
	        for(int i=0 ; i < depth ; i ++) {
	        	  MQGetMessageOptions gmo =  new MQGetMessageOptions();
	              gmo.options =  CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_CONVERT + CMQC.MQGMO_BROWSE_NEXT ;
	              gmo.waitInterval = Integer.parseInt(maximumWaitTime);
	       	      mqMessage = new MQMessage();
	              queue.get(mqMessage, gmo);
	              
	              String msg = mqMessage.readStringOfByteLength(mqMessage.getDataLength());
	            //  System.out.println("Conparing " + filter +" to message content of " + msg);
	              if(msg.toLowerCase().contains(filter.toLowerCase())) 
	              		count++;
	        }
          
        	queue.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
            try {
                if (queue != null)
                    queue.close();
                if (queueManager != null)
                    queueManager.close();
                
            } catch (MQException e) {
    			System.out.println("No message received off queue. " + e.getMessage());
    			//e.printStackTrace();
            }
        }
		//System.out.println("returning byte[]" + returnMessage);
		return count;
	}

	 public String getMessageByCorrelationID(String timeout, String id, String consume) throws JMSException, InterruptedException {
	     ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
         cf = ff.createConnectionFactory();
         cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, MQEnvironment.hostname);
         cf.setIntProperty(WMQConstants.WMQ_PORT, MQEnvironment.port);
         cf.setStringProperty(WMQConstants.WMQ_CHANNEL, MQEnvironment.channel);
         cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
         cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, queueManagerName);
         cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsGet (JMS)");
         cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
         cf.setStringProperty(WMQConstants.USERID, MQEnvironment.userID);
         cf.setStringProperty(WMQConstants.PASSWORD, MQEnvironment.password);
         context = cf.createContext();
	        Queue destination = context.createQueue("queue:///" +  destinationQueueName);
	        String returnString = "Wait time exceeded, No messages on queue";
	        if(consume.equalsIgnoreCase("consume")) {
	            JMSConsumer consumer = context.createConsumer(destination,"JMSCorrelationID='ID:" + id +"'");
	            Message receivedMessage = consumer.receive(Integer.parseInt(timeout)); // in ms or 15 seconds
	            if ( receivedMessage == null)
	                return returnString;
	            returnString = receivedMessage.getBody(String.class);
	        }else {
	            int i=1000;
	            while(i < Integer.parseInt(timeout)) {
	                Thread.currentThread();
	                Thread.sleep(i);
	                i = i+1000;//harded to per second.
	                QueueBrowser qb = context.createBrowser(destination,"JMSCorrelationID='ID:" + id +"'");
	                Enumeration<?> e = qb.getEnumeration();
	                while(e.hasMoreElements()) {
	                    Message m = (Message) e.nextElement();
	                    returnString.concat(m.getBody(String.class));
	                }
	            }
	        }
	        return returnString;
	    }
	
	
	public byte[] getMessageIDUsingFilter(String maximumWaitTime,String filter){
		//System.out.println("Filter is " + filter);
		MQQueue queue = null;
		byte[] returnMessage = null;
		try{
			queueManager = new MQQueueManager(queueManagerName);
			int openOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_BROWSE;
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
	        int depth = queue.getCurrentDepth();
	      //  System.out.println("Depth is "+ depth);
	        for(int i=0 ; i < depth ; i ++) {
	        	  MQGetMessageOptions gmo =  new MQGetMessageOptions();
	              gmo.options =  CMQC.MQGMO_WAIT + CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_CONVERT + CMQC.MQGMO_BROWSE_NEXT ;
	              gmo.waitInterval = Integer.parseInt(maximumWaitTime);
	       	      mqMessage = new MQMessage();
	              queue.get(mqMessage, gmo);
	              
	              String msg = mqMessage.readStringOfByteLength(mqMessage.getDataLength());
	            //  System.out.println("Conparing " + filter +" to message content of " + msg);
	              if(msg.toLowerCase().contains(filter.toLowerCase())) {
	              	returnMessage = mqMessage.messageId;
	              	//queue.ge
		         //   System.out.println("Message ID found with matching filter text " + returnMessage);
	              	break;
	              }
	        }
          
        	queue.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
            try {
                if (queue != null)
                    queue.close();
                if (queueManager != null)
                    queueManager.close();
                
            } catch (MQException e) {
    			System.out.println("No message received off queue. " + e.getMessage());
    			//e.printStackTrace();
            }
        }
		//System.out.println("returning byte[]" + returnMessage);
		return returnMessage;
	}
	public String getMessageByID(String maximumWaitTime,byte[] ID){
		if(ID == null) {
			return "Could not locate message with search criteria.";
		}
		MQQueue queue = null;
		String returnMessage = null;
		try{
			queueManager = new MQQueueManager(queueManagerName);
			int openOptions = MQConstants.MQOO_INPUT_SHARED + MQConstants.MQOO_INQUIRE;
	        queue = queueManager.accessQueue(destinationQueueName, openOptions, null, null, null);
	        MQGetMessageOptions gmo =  new MQGetMessageOptions();
	        gmo.options =  CMQC.MQMO_MATCH_MSG_ID | CMQC.MQGMO_WAIT;
	        gmo.matchOptions = gmo.options;
	        gmo.waitInterval = Integer.parseInt(maximumWaitTime);
	   	    mqMessage = new MQMessage();
	   	    mqMessage.messageId = ID;
	        queue.get(mqMessage, gmo);
	        returnMessage = mqMessage.readStringOfByteLength(mqMessage.getDataLength());
        	queue.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally {
            try {
                if (queue != null)
                    queue.close();
                if (queueManager != null)
                    queueManager.close();
                
            } catch (MQException e) {
    			System.out.println("No message received off queue. " + e.getMessage());
    			//e.printStackTrace();
            }
        }
		return returnMessage;
	}

	
	public String sendMsg(String message, String messageID, String correlationID) {
		  MQQueue queue = null;
		  String returnString = "";
		 // message = message.replace( "&quote","\"");
	        try {	        	
	            int openOptions = MQConstants.MQOO_OUTPUT  ;   
	            queue = queueManager.accessQueue(destinationQueueName,openOptions);
	            pmo = new MQPutMessageOptions(); 
	            pmo.options = CMQC.MQGMO_SYNCPOINT;

	            mqMessage = new MQMessage();
	            mqMessage.persistence = CMQC.MQPER_PERSISTENT;
				mqMessage.format = CMQC.MQFMT_STRING ;

				System.out.println("Preparing payload to send\n" + message);

				message = message.replace("\n","").replace("\t","").trim();


				mqMessage.writeString(message.trim());
				mqMessage.characterSet = 1208;  // Use UTF-8 encoding (1208 is the codepage for UTF-8 in MQ)

				if(messageID != null && correlationID != null) {
	            	mqMessage.messageId = messageID.getBytes();
	            	mqMessage.correlationId = correlationID.getBytes();

	            }
	            queue.put(mqMessage, pmo);
	            queueManager.commit();
	            returnString = "Success";

	        } catch (Exception e) {
	            //e.printStackTrace();
	        	returnString = e.getMessage();
	            try {
	                if (queueManager != null)
	                    queueManager.backout();
	            } catch (MQException e1) {
	               // e1.printStackTrace();
	            }
	        } finally {
	            try {
	                if (queue != null)
	                    queue.close();
	                if (queueManager != null)
	                    queueManager.close();
	            } catch (MQException e) {
	               // e.printStackTrace();
	            }
	        }
	        return returnString;
	    }
//
}
