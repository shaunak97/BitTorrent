import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;


public class MessageProcessor implements Runnable, MessageConstants 
{
	private static boolean running = true;
	private static String PeerID_this = null;
	public static int peerState = -1;
	RandomAccessFile raf;
	
	// constructor
	public MessageProcessor(String PeerID_pthis)
	{
		PeerID_this = PeerID_pthis;
	}
	
	// constructor
	public MessageProcessor()
	{
		PeerID_this = null;
	}
	
	public void pTS(String dataType, int state)
	{
		peerProcess.showLog("Message Processor : msgType = "+ dataType + " State = "+state);
	}

	public void run()
	{
		DataMessage d;
		DataMessageWrapper dataWrapper;
		String msgType;
		String rPeerId;
				
		while(running)
		{
			dataWrapper  = peerProcess.removeFromMsgQueue();
			while(dataWrapper == null)
			{
				Thread.currentThread();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				   e.printStackTrace();
				}
				dataWrapper  = peerProcess.removeFromMsgQueue();
			}
			
			d = dataWrapper.getDataMsg();
			
			msgType = d.getMessageTypeString();
			rPeerId = dataWrapper.getFromPeerID();
			int state = peerProcess.remotePeerInfoHash.get(rPeerId).state;
			if(msgType.equals(DATA_MSG_HAVE) && state != 14)
			{
				// LOG 7: Receive Message from peer
				peerProcess.showLog(peerProcess.peerID + " receieved HAVE message from Peer " + rPeerId); 
				if(isInterested(d, rPeerId))
				{
					//peerProcess.showLog(peerProcess.peerID + " is interested in Peer " + rPeerId);
					sendInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
					peerProcess.remotePeerInfoHash.get(rPeerId).state = 9;
				}	
				else
				{
					//peerProcess.showLog(peerProcess.peerID + "is not interested " + rPeerId);
					sendNotInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
					peerProcess.remotePeerInfoHash.get(rPeerId).state = 13;
				}
			}
			else
			{
			 switch (state)
			 {
			 
			 case 2:
			   if (msgType.equals(DATA_MSG_BITFIELD)) {
		 		  peerProcess.showLog(peerProcess.peerID + " receieved a BITFIELD message from Peer " + rPeerId);
	 			  sendBitField(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
 				  peerProcess.remotePeerInfoHash.get(rPeerId).state = 3;  
			   }
			   break;
			 
			 case 3:
			   if (msgType.equals(DATA_MSG_NOTINTERESTED)) {
				 // LOG 9:
					peerProcess.showLog(peerProcess.peerID + " receieved a NOT INTERESTED message from Peer " + rPeerId);
					peerProcess.remotePeerInfoHash.get(rPeerId).isInterested = 0;
					peerProcess.remotePeerInfoHash.get(rPeerId).state = 5;
					peerProcess.remotePeerInfoHash.get(rPeerId).isHandShaked = 1;
			   }
			   else if (msgType.equals(DATA_MSG_INTERESTED)) {
				// LOG 8:
					peerProcess.showLog(peerProcess.peerID + " receieved an INTERESTED message from Peer " + rPeerId);
					peerProcess.remotePeerInfoHash.get(rPeerId).isInterested = 1;
					peerProcess.remotePeerInfoHash.get(rPeerId).isHandShaked = 1;
					
					if(!peerProcess.preferedNeighbors.containsKey(rPeerId) && !peerProcess.unchokedNeighbors.containsKey(rPeerId))
					{
						sendChoke(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
						peerProcess.remotePeerInfoHash.get(rPeerId).isChoked = 1;
						peerProcess.remotePeerInfoHash.get(rPeerId).state  = 6;
					}
					else
					{
						peerProcess.remotePeerInfoHash.get(rPeerId).isChoked = 0;
						sendUnChoke(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
						peerProcess.remotePeerInfoHash.get(rPeerId).state = 4 ;
					}
			   }
			   break;
			   
			 case 4:
				 if (msgType.equals(DATA_MSG_REQUEST)) {
					//peerProcess.showLog(peerProcess.peerID + " receieved a REQUEST message from Peer " + rPeerId);
						sendPeice(peerProcess.peerIDToSocketMap.get(rPeerId), d, rPeerId);
						// Decide to send CHOKE or UNCHOKE message
						if(!peerProcess.preferedNeighbors.containsKey(rPeerId) && !peerProcess.unchokedNeighbors.containsKey(rPeerId))
						{
							sendChoke(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).isChoked = 1;
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 6;
						}  
				 }
				 break;
				 
			 case 8:
				 if (msgType.equals(DATA_MSG_BITFIELD)) {
						//Decide if interested or not.
						if(isInterested(d,rPeerId))
						{
							//peerProcess.showLog(peerProcess.peerID + " is interested in Peer " + rPeerId);
							sendInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 9;
						}	
						else
						{
							//peerProcess.showLog(peerProcess.peerID + " is not interested in Peer " + rPeerId);
							sendNotInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 13;
						}
				 }
				 break;
				 
			 case 9:
				 if (msgType.equals(DATA_MSG_CHOKE)) {
					// LOG 6:
						peerProcess.showLog(peerProcess.peerID + " is CHOKED by Peer " + rPeerId);
						peerProcess.remotePeerInfoHash.get(rPeerId).state = 14;
				 }
				 else if (msgType.equals(DATA_MSG_UNCHOKE)) {
					// LOG 5:
						peerProcess.showLog(peerProcess.peerID + " is UNCHOKED by Peer " + rPeerId);
						int firstdiff = peerProcess.ownBitField.returnFirstDiff(peerProcess.remotePeerInfoHash.get(rPeerId).bitField);
						if(firstdiff != -1)
						{
							//peerProcess.showLog(peerProcess.peerID + " is Requesting PIECE " + firstdiff + " from peer " + rPeerId);
							sendRequest(peerProcess.peerIDToSocketMap.get(rPeerId), firstdiff, rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 11;
							// Get the time when the request is being sent.
							peerProcess.remotePeerInfoHash.get(rPeerId).startTime = new Date();
						}
						else
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 13;
				 }
				 break;
				 
			 case 11:
				 if (msgType.equals(DATA_MSG_PIECE)) {
					    byte[] buffer = d.getPayload();						
						peerProcess.remotePeerInfoHash.get(rPeerId).finishTime = new Date();
						long timeLapse = peerProcess.remotePeerInfoHash.get(rPeerId).finishTime.getTime() - 
									peerProcess.remotePeerInfoHash.get(rPeerId).startTime.getTime() ;
						
						peerProcess.remotePeerInfoHash.get(rPeerId).dataRate = ((double)(buffer.length + DATA_MSG_LEN + DATA_MSG_TYPE)/(double)timeLapse) * 100;
						
						Piece p = Piece.decodePiece(buffer);
						peerProcess.ownBitField.updateBitField(rPeerId, p);			
						
						int toGetPeiceIndex = peerProcess.ownBitField.returnFirstDiff(peerProcess.remotePeerInfoHash.get(rPeerId).bitField);
						if(toGetPeiceIndex != -1)
						{
							//peerProcess.showLog(peerProcess.peerID + " Requesting piece " + toGetPeiceIndex + " from peer " + rPeerId);
							sendRequest(peerProcess.peerIDToSocketMap.get(rPeerId),toGetPeiceIndex, rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state  = 11;
							// Get the time when the request is being sent.
							peerProcess.remotePeerInfoHash.get(rPeerId).startTime = new Date();
						}
						else
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 13;
						
						//updates remote peerInfo
						peerProcess.readPeerInfoAgain();
						
						Enumeration<String> keys = peerProcess.remotePeerInfoHash.keys();
						while(keys.hasMoreElements())
						{
							String key = (String)keys.nextElement();
							RemotePeerInfo pref = peerProcess.remotePeerInfoHash.get(key);
							
							if(key.equals(peerProcess.peerID))continue;
							//peerProcess.showLog(peerProcess.peerID + ":::: isCompleted =" + pref.isCompleted + " isInterested =" + pref.isInterested + " isChoked =" + pref.isChoked);
							if (pref.isCompleted == 0 && pref.isChoked == 0 && pref.isHandShaked == 1)
							{
								//peerProcess.showLog(peerProcess.peerID + " isCompleted =" + pref.isCompleted + " isInterested =" + pref.isInterested + " isChoked =" + pref.isChoked);
								sendHave(peerProcess.peerIDToSocketMap.get(key), key);
								peerProcess.remotePeerInfoHash.get(key).state = 3;
								
							} 
							
						}
										
						buffer = null;
						d = null;
			
				 }
				 else if (msgType.equals(DATA_MSG_CHOKE)) {
					// LOG 6:
						peerProcess.showLog(peerProcess.peerID + " is CHOKED by Peer " + rPeerId);
						peerProcess.remotePeerInfoHash.get(rPeerId).state = 14;
				 }
				 break;
				 
			 case 14:
				 if (msgType.equals(DATA_MSG_HAVE)) {
						//Decide if interested or not.
						if(isInterested(d,rPeerId))
						{
							//peerProcess.showLog(peerProcess.peerID + " is interested in Peer " + rPeerId);
							sendInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 9;
						}	
						else
						{
							//peerProcess.showLog(peerProcess.peerID + " is not interested in Peer " + rPeerId);
							sendNotInterested(peerProcess.peerIDToSocketMap.get(rPeerId), rPeerId);
							peerProcess.remotePeerInfoHash.get(rPeerId).state = 13;
						}
				 }
				 else if (msgType.equals(DATA_MSG_UNCHOKE)) {
                        // LOG 5:
						peerProcess.showLog(peerProcess.peerID + " is UNCHOKED by Peer " + rPeerId);
						peerProcess.remotePeerInfoHash.get(rPeerId).state = 14;
				 }
				 break;
				 
			 }
			}

		}
	}

	 private byte[] intTobyte(int i) {
	        return new byte[] {  
	                (byte) ((i >> 24) & 0xFF),  
	                (byte) ((i >> 16) & 0xFF),     
	                (byte) ((i >> 8) & 0xFF),     
	                (byte) (i & 0xFF)  
	            };  
	    }
	 
	 private int byteToint(byte[] b1) {
	        return  b1[3] & 0xFF |  
	                (b1[2] & 0xFF) << 8 |  
	                (b1[1] & 0xFF) << 16 |  
	                (b1[0] & 0xFF) << 24;  
	    }
	 
	private void sendRequest(Socket socket, int pieceNo, String remotePeerID) {

		// Byte2int....
		byte[] pieceByte = new byte[MessageConstants.PIECE_INDEX_LEN];
		for (int i = 0; i < MessageConstants.PIECE_INDEX_LEN; i++) {
			pieceByte[i] = 0;
		}

		byte[] pieceIndexByte = ConversionUtil.intToByteArray(pieceNo);
		System.arraycopy(pieceIndexByte, 0, pieceByte, 0,
						pieceIndexByte.length);
		DataMessage d = new DataMessage(DATA_MSG_REQUEST, pieceByte);
		byte[] b = DataMessage.encodeMessage(d);
		SendData(socket, b);

		pieceByte = null;
		pieceIndexByte = null;
		b = null;
		d = null;
	}

	private void sendPeice(Socket socket, DataMessage d, String remotePeerID)  //d == requestmessage
	{
		byte[] bytePieceIndex = d.getPayload();
		int pieceIndex = ConversionUtil.byteArrayToInt(bytePieceIndex);
		
		peerProcess.showLog(peerProcess.peerID + " sending a PIECE message for piece " + pieceIndex + " to Peer " + remotePeerID);
		
		byte[] byteRead = new byte[CommonProperties.pieceSize];
		int noBytesRead = 0;
		
		File file = new File(peerProcess.peerID,CommonProperties.fileName);
		try 
		{
			raf = new RandomAccessFile(file,"r");
			raf.seek(pieceIndex*CommonProperties.pieceSize);
			noBytesRead = raf.read(byteRead, 0, CommonProperties.pieceSize);
		} 
		catch (IOException e) 
		{
			peerProcess.showLog(peerProcess.peerID + " ERROR in reading the file : " +  e.toString());
		}
		if( noBytesRead == 0)
		{
			peerProcess.showLog(peerProcess.peerID + " ERROR :  Zero bytes read from the file !");
		}
		else if (noBytesRead < 0)
		{
			peerProcess.showLog(peerProcess.peerID + " ERROR : File could not be read properly.");
		}
		
		byte[] buffer = new byte[noBytesRead + MessageConstants.PIECE_INDEX_LEN];
		System.arraycopy(bytePieceIndex, 0, buffer, 0, MessageConstants.PIECE_INDEX_LEN);
		System.arraycopy(byteRead, 0, buffer, MessageConstants.PIECE_INDEX_LEN, noBytesRead);

		DataMessage sendMessage = new DataMessage(DATA_MSG_PIECE, buffer);
		byte[] b =  DataMessage.encodeMessage(sendMessage);
		SendData(socket, b);
		
		//release memory
		buffer = null;
		byteRead = null;
		b = null;
		bytePieceIndex = null;
		sendMessage = null;
		
		try{
			raf.close();
		}
		catch(Exception e){}
	}
	
	private void sendNotInterested(Socket socket, String remotePeerID) 
	{
		peerProcess.showLog(peerProcess.peerID + " sending a NOT INTERESTED message to Peer " + remotePeerID);
		DataMessage d =  new DataMessage(DATA_MSG_NOTINTERESTED);
		byte[] msgByte = DataMessage.encodeMessage(d);
		SendData(socket,msgByte);
	}

	private void sendInterested(Socket socket, String remotePeerID) {
		peerProcess.showLog(peerProcess.peerID + " sending an INTERESTED message to Peer " + remotePeerID);
		DataMessage d =  new DataMessage(DATA_MSG_INTERESTED);
		byte[] msgByte = DataMessage.encodeMessage(d);
		SendData(socket,msgByte);		
	}

	
//  Compare the bitfield and send TRUE if there is any extra data	
	private boolean isInterested(DataMessage d, String rPeerId) {		
		
		BitField b = BitField.decode(d.getPayload());
		peerProcess.remotePeerInfoHash.get(rPeerId).bitField = b;

		if(peerProcess.ownBitField.compare(b))return true;
		return false;
	}

	private void sendUnChoke(Socket socket, String remotePeerID) {

		peerProcess.showLog(peerProcess.peerID + " sending UNCHOKE message to Peer " + remotePeerID);
		DataMessage d = new DataMessage(DATA_MSG_UNCHOKE);
		byte[] msgByte = DataMessage.encodeMessage(d);
		SendData(socket,msgByte);
	}

	private void sendChoke(Socket socket, String remotePeerID) {
		peerProcess.showLog(peerProcess.peerID + " sending CHOKE message to Peer " + remotePeerID);
		DataMessage d = new DataMessage(DATA_MSG_CHOKE);
		byte[] msgByte = DataMessage.encodeMessage(d);
		SendData(socket,msgByte);
	}

	private void sendBitField(Socket socket, String remotePeerID) {
	
		peerProcess.showLog(peerProcess.peerID + " sending BITFIELD message to Peer " + remotePeerID);
		byte[] encodedBitField = peerProcess.ownBitField.encode();

		DataMessage d = new DataMessage(DATA_MSG_BITFIELD, encodedBitField);
		SendData(socket,DataMessage.encodeMessage(d));
		
		encodedBitField = null;
	}
	
	
	private void sendHave(Socket socket, String remotePeerID) {
		
		peerProcess.showLog(peerProcess.peerID + " sending HAVE message to Peer " + remotePeerID);
		byte[] encodedBitField = peerProcess.ownBitField.encode();
		DataMessage d = new DataMessage(DATA_MSG_HAVE, encodedBitField);
		SendData(socket,DataMessage.encodeMessage(d));
		
		encodedBitField = null;
	}
	
	private int SendData(Socket socket, byte[] encodedBitField) {
		try {
		OutputStream out = socket.getOutputStream();
		out.write(encodedBitField);
		} catch (IOException e) {
            e.printStackTrace();
			return 0;
		}
		return 1;
	}
	

}
