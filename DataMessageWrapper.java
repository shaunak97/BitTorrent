
public class DataMessageWrapper
{
	DataMessage dataMsg;
	String fromPeerID;
	
	public DataMessageWrapper() 
	{
		dataMsg = new DataMessage();
		fromPeerID = null;
	}
    public void setFromPeerID(String fromPeerID) {
        this.fromPeerID = fromPeerID;
    }
    
    public void setDataMsg(DataMessage dataMsg) {
        this.dataMsg = dataMsg;
    }
	
    public DataMessage getDataMsg() {
		return dataMsg;
	}

	public String getFromPeerID() {
		return fromPeerID;
	}
	
	
	
}
