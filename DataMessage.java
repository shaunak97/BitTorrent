
import java.io.UnsupportedEncodingException;

public class DataMessage implements MessageConstants 
{
    private String messageType;
    private String messageLength;
    private int dataLength = DATA_MSG_TYPE;
    private byte[] type = null;
	private byte[] len = null;
	private byte[] payload = null;
	
    public DataMessage(String Type) {
        
        try {
            
            if (Type == DATA_MSG_CHOKE || Type == DATA_MSG_UNCHOKE
                || Type == DATA_MSG_INTERESTED
                || Type == DATA_MSG_NOTINTERESTED)
            {
                this.setMessageLength(1);
                this.setMessageType(Type);
                this.payload = null;
            }
            else
                throw new Exception("DataMessage:: Constructor - Wrong constructor selection.");
            
            
        } catch (Exception e) {
            peerProcess.showLog(e.toString());
        }
        
    }


	
	public DataMessage(String Type, byte[] Payload) 
	{

		try 
		{
			if (Payload != null)
			{
				
                this.setMessageLength(Payload.length + 1);
                if (this.len.length > DATA_MSG_LEN)
                    throw new Exception("DataMessage:: Constructor - message length is too large.");
                
                this.setPayload(Payload);
                
			} 
			else
			{
                if (Type == DATA_MSG_CHOKE || Type == DATA_MSG_UNCHOKE
                    || Type == DATA_MSG_INTERESTED
                    || Type == DATA_MSG_NOTINTERESTED)
                {
                    this.setMessageLength(1);
                    this.payload = null;
                }
                else
                    throw new Exception("DataMessage:: Constructor - Pay load should not be null");

				
			}

			this.setMessageType(Type);
			if (this.getMessageType().length > DATA_MSG_TYPE)
				throw new Exception("DataMessage:: Constructor - Type length is too large.");

		} catch (Exception e) {
			peerProcess.showLog(e.toString());
		}

	}

    public DataMessage()
    {
        
    }
	
    public void setMessageLength(int messageLength) {
        this.dataLength = messageLength;
        this.messageLength = ((Integer)messageLength).toString();
        this.len = ConversionUtil.intToByteArray(messageLength);
    }
	
	public void setMessageLength(byte[] len) {

		Integer l = ConversionUtil.byteArrayToInt(len);
		this.messageLength = l.toString();
		this.len = len;
		this.dataLength = l;  
	}

	
	
	
	public byte[] getMessageLength() {
		return len;
	}
	
	public String getMessageLengthString() {
		return messageLength;
	}

	
	public int getMessageLengthInt() {
		return this.dataLength;
	}
	
	

	public void setMessageType(byte[] type) {
		try {
			this.messageType = new String(type, MSG_CHARSET_NAME);
			this.type = type;
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
	}
	
	public void setMessageType(String messageType) {
		try {
			this.messageType = messageType.trim();
			this.type = this.messageType.getBytes(MSG_CHARSET_NAME);
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
	}
	
	public byte[] getMessageType() {
		return type;
	}

	
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	
	public byte[] getPayload() {
		return payload;
	}


	public String getMessageTypeString() {
		return messageType;
	}

	public String toString() {
		String str = null;
		try {
			str = "[DataMessage] : Message Length - "
					+ this.messageLength
					+ ", Message Type - "
					+ this.messageType
					+ ", Data - "
					+ (new String(this.payload, MSG_CHARSET_NAME)).toString()
							.trim();
		} catch (UnsupportedEncodingException e) {
			peerProcess.showLog(e.toString());
		}
		return str;
	}
    //encodes the object DataMessage to a byte array
    
    public static byte[] encodeMessage(DataMessage msg)
    {
        byte[] msgStream = null;
        int msgType;
        
        try
        {
            
            msgType =Integer.parseInt(msg.getMessageTypeString());
            if (msg.getMessageLength().length > DATA_MSG_LEN)
                throw new Exception("Invalid message length.");
            else if (msgType < 0 || msgType > 7)
                throw new Exception("Invalid message type.");
            else if (msg.getMessageType() == null)
                throw new Exception("Invalid message type.");
            else if (msg.getMessageLength() == null)
                throw new Exception("Invalid message length.");
            
            if (msg.getPayload()!= null) {
                msgStream = new byte[DATA_MSG_LEN + DATA_MSG_TYPE + msg.getPayload().length];
                
                System.arraycopy(msg.getMessageLength(), 0, msgStream, 0, msg.getMessageLength().length);
                System.arraycopy(msg.getMessageType(), 0, msgStream, DATA_MSG_LEN, DATA_MSG_TYPE);
                System.arraycopy(msg.getPayload(), 0, msgStream, DATA_MSG_LEN + DATA_MSG_TYPE, msg.getPayload().length);
                
                
            } else {
                msgStream = new byte[DATA_MSG_LEN + DATA_MSG_TYPE];
                
                System.arraycopy(msg.getMessageLength(), 0, msgStream, 0, msg.getMessageLength().length);
                System.arraycopy(msg.getMessageType(), 0, msgStream, DATA_MSG_LEN, DATA_MSG_TYPE);
                
            }
            
        }
        catch (Exception e)
        {
            peerProcess.showLog(e.toString());
            msgStream = null;
        }
        
        return msgStream;
    }

	
	 //decodes the byte array and send it to object DataMessage
	 
	public static DataMessage decodeMessage(byte[] Message) {

		
		DataMessage msg = new DataMessage();
		byte[] msgLength = new byte[DATA_MSG_LEN];
		byte[] msgType = new byte[DATA_MSG_TYPE];
		byte[] payLoad = null;
		int len;

		try 
		{
			
			if (Message == null)
				throw new Exception("Invalid data.");
			else if (Message.length < DATA_MSG_LEN + DATA_MSG_TYPE)
				throw new Exception("Byte array length is too small...");

			
			System.arraycopy(Message, 0, msgLength, 0, DATA_MSG_LEN);
			System.arraycopy(Message, DATA_MSG_LEN, msgType, 0, DATA_MSG_TYPE);

			msg.setMessageLength(msgLength);
			msg.setMessageType(msgType);
			
			len = ConversionUtil.byteArrayToInt(msgLength);
			
			if (len > 1) 
			{
				payLoad = new byte[len-1];
				System.arraycopy(Message, DATA_MSG_LEN + DATA_MSG_TYPE,	payLoad, 0, Message.length - DATA_MSG_LEN - DATA_MSG_TYPE);
				msg.setPayload(payLoad);
			}
			
			payLoad = null;
		} 
		catch (Exception e) 
		{
			peerProcess.showLog(e.toString());
			msg = null;
		}
		return msg;
	}

	

}
