CEN5106C COMPUTER NETWORKS : P2P File Sharing Project
Team Members: Arpan Banerjee(9359-9083), Gloria Nazareth(8221-8035), Sanket Nayak(8895-9505)
Group Number: 57

There were some issues with the later half of the video uploaded on the UF one drive. So we have uploaded it on YouTube.
YouTube Link: https://youtu.be/V61NjH0TneA

One Drive Folder Link(Contains Video): https://uflorida-my.sharepoint.com/:f:/g/personal/sanketnayak_ufl_edu/Eq-pBpGSHmBHqlhipaSTtF8BUjAtiRj35LI5wLY63NNrYA?e=B4xtrH



This README.txt gives a complete overview of the project implementation as per the requirement of Final Project Submission.

'Project.zip' contains the Java Project (created in IntelliJ IDEA).

Software Specification: 

– Java version: 'OpenJDK 15' 

Our program meets all of the following requirements:

Part-1: Initializing the peer process– 

	A] Reading of 'Common.cfg' file and set related variables – readCommon() in util/Config.java 

	B] Reading of 'PeerInfo.cfg' file and set related variables – readpeerInfo() in util/Config.java

	C] Establishing a TCP connection between two peer processes – 
	   Peer.java starts socket/PeerClient.java and socket/PeerServer.java; PeerServer listens for TCP connection and Peer Client initiates a TCP Connection with PeerServer

	D] Peer connects, sends handshake messages, starts exchanging pieces and terminates after completion of downloading of file by itself and other peers – 
	   sendHandshake() and recvHandshake() in message/HandshakeMessage.java
	   receiveMessages() in message/Messenger.java handles logic for different types of messages.
	   util/MessageUtil.java handles the different types of messages and checks for termination condition.

Part-2: Post Connecting–

	A] After connection each peer process sends 'handshake' message to each other before sending other messages –  sendHandshake() and recvHandshake() in message/HandshakeMessage.java

	B] Send/Receiving of 'bitfield' message – This is handled by message/BitfieldMessage.java and the logic is handled in message/Messenger.java

	C] Send 'interested'/'not interested' message – InterestedMessage.java and NotInterestedMessage.java; logic handled in message/Messenger.java

	D] Send k unchoke/choke messages every p seconds – handled in timertask/PreferredNeighborsTask.java

	E] Set Optimistically unchoke neighbor every m seconds – handled in timertask/OptimisticUnchokingTask.java

Part–3: File Exchange–

	A] Send 'request', 'have', 'not interested', 'interested', 'piece' – These are in the relevant classes inside the message folder
	
	B] Receive 'have' message and update related bitfield – have message is received in message/Messenger.java and bitfield is updated in handleHaveMessage() message/MessageUtil.java

Part-4: Stop Service – after all peers have received the file, checkTermination() in util/MessageUtil.java checks if bitfield of all the peers is full and accordingly terminates the program.

Executing Instructions–

All the config files should be placed in the cfg folder.

For running on local machines use the following steps–
	A] In terminal navigate to the Project folder (Project_Midpoint)

	B] Enter commands in following order
		1. make

    C] Peer can be started using the command
        make run peerID=1001

        change the peerID for starting different peers

For running on CISE servers use the following steps–
    1. Compile the java files as follows:
      ./compileJava

    2. run the program as follows:
      java StartRemotePeers



