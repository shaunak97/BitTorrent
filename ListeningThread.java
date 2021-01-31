import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ListeningThread implements Runnable 
{
	private ServerSocket SocketListening;
	private String peerID;
	Socket remoteSocket;
	Thread sendingThread;
	
	public ListeningThread(ServerSocket socket, String peerID) 
	{
		this.SocketListening = socket;
		this.peerID = peerID;
	}
	
	public void run() 
	{
		while(true)
		{
			try
			{
				remoteSocket = SocketListening.accept();
				// instantiates thread for handling individual remote peer
				sendingThread = new Thread(new RemotePeerHandler(remoteSocket,0,peerID));
				peerProcess.showLog(peerID + " Connection is established");
				peerProcess.sendingThread.add(sendingThread);
				sendingThread.start(); 
			}
			catch(Exception e)
			{
				peerProcess.showLog(this.peerID + " Exception in connection: " + e.toString());
			}
		}
	}
	
	public void releaseSocket()
	{
		try 
		{
			if(!remoteSocket.isClosed())
			remoteSocket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}


