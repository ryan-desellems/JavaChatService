//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;
import javax.swing.*;                        // Client sided application that handles thread and prepares for asynchronous messaging

//==================================================================================
public class ConnectionToServer
								implements Runnable
{
	Talker 			serverTalker;				//data members
	LoginWindow		loginPtr;
	ChatClient		clientPtr;
	ClientInterface	clientInterfacePtr;
	String			clientId;
	String 			serverDomain;
	int				serverPort;

	String 		userKey;

//==================================================================================
ConnectionToServer(Talker inTalker, ChatClient ptrIn)
{
	serverTalker = inTalker;				//pass taker and start thread
	clientPtr	 = ptrIn;

	new Thread(this).start();
}
//==================================================================================
ConnectionToServer(String domainIn, int portIn, String idIn, ChatClient ptrIn)
{
	clientId 	 = idIn;					//create talker and start thread
	serverDomain = domainIn;
	serverPort	 = portIn;
	clientPtr	 = ptrIn;

	new Thread(this).start();
}
//==================================================================================
ConnectionToServer(String domainIn, int portIn, String idIn, LoginWindow ptrIn)
{
	clientId 	 = idIn;					//create talker and start thread
	serverDomain = domainIn;
	serverPort	 = portIn;
	loginPtr	 = ptrIn;

	new Thread(this).start();
}
//==================================================================================
void setKey(String keyIn)
{
	userKey = keyIn;						//get userInfo (PROTOCOL username password)
}
//==================================================================================
public void run()
{
	if(establishUser())						//if user was able to login or register
	{
		loginPtr.dispose();
		clientInterfacePtr = new ClientInterface(clientId,this);
		try
		{
			serverTalker.setUser(clientId);		//let server talker know who owns it
			//clientPtr.openBuddies(clientId);	//pop open friend pane
			while(true)
			{
				String messageIn = serverTalker.getMsg();	//begin asynchronous messaging
				if(!messageIn.trim().equals(""))
				{
					processMessageRecieved(messageIn);		//get message and process
				}
			}
		}
		catch(IOException ioe)								//can't reach server, so its closed
		{
			JOptionPane.showMessageDialog(null,"The server has closed.",
											   "SERVER SHUTDOWN.", JOptionPane.ERROR_MESSAGE); //annoy user
			System.exit(1);
		}
	}
}
//==================================================================================
void processMessageRecieved(String messageIn)
{
	int protoEndPos = messageIn.indexOf(" ");						//grab protocol from incoming message
	String protocolRecieved = messageIn.substring(0,protoEndPos);

	if (protocolRecieved.equals("FRIENDREQUEST"))			//protocol indicating user has sent a friend req
	{
		handleFriendRequest(messageIn);
	}
	else if (protocolRecieved.equals("FRIENDACCEPTED"))		//protocol indicating user accepted a friend req
	{
		handleAcceptedFriend(messageIn);
	}
	else if (protocolRecieved.equals("FRIENDFAILED"))		//protocol indicating failed friend req
	{
		requestFailed();
	}
	else if (protocolRecieved.equals("ADDFRIENDTOLIST"))	//protocol to add friend to list
	{
		addFriendToList(messageIn);
	}
	else if(protocolRecieved.equals("BUILDFRIEND"))   	  //protocol for building friendsList
	{
		buildFriendList(messageIn);
	}
	else if(protocolRecieved.equals("USEROFFLINE"))   	  //protocol for friend offline
	{
		friendOffline(messageIn);
	}
	else if(protocolRecieved.equals("USERONLINE"))   	  //protocol for friend online
	{
		friendOnline(messageIn);
	}
	else if(protocolRecieved.equals("MESSAGE"))   	  //protocol for friend online
	{
		chatMessageRecieved(messageIn);
	}
}
//==================================================================================
void handleFriendRequest(String messageIn)
{
	String userRequesting = messageIn.split(" ")[1];	//grab message and split according to who is who
	String userToFriend	  = messageIn.split(" ")[2];

	SwingUtilities.invokeLater
	( new Runnable()
	 {
		public void run()								//jdialog will open asking recipient if they want to be pals
		{
			int friendChoice = JOptionPane.showConfirmDialog
								(null,userRequesting + " would like to be friends!",
									 "Be friends with " + userRequesting + "?", JOptionPane.YES_NO_OPTION);

			if(friendChoice == 0)						//if they select yes
			{
				try
				{
					serverTalker.sendMsg("ACCEPTEDFRIEND " + userRequesting + " " + userToFriend);	//alert CTC of the good news
				}
				catch(IOException ioeo)
				{
					System.out.println("error in handlefriendrequest");	//debug code
				}
			}
		}
	}
 	);
}
//==================================================================================
void handleAcceptedFriend(String messageIn)
{
	String userRequesting = messageIn.split(" ")[1];	//grab message and split into who is who
	String userToFriend	  = messageIn.split(" ")[2];

	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			JOptionPane.showMessageDialog(null,"You are friends with " + userToFriend + "!", //tell initiating client that friendship has been established
											   "NEW FRIEND!", JOptionPane.INFORMATION_MESSAGE);
			try
			{
				serverTalker.sendMsg("ADDFRIENDTOLIST " + userRequesting + " " + userToFriend); //tell CTC to so it can add them to initaing friend's list
				//clientPtr.addFriendToList(userToFriend,true);									//add that friend to the client Jlist
				clientInterfacePtr.addFriendToList(userToFriend,true);
			}
			catch(IOException whoops)
			{
				System.out.println("error in friend add to list");		//debug
			}
		}
	}
 	);
}
//==================================================================================
void requestFailed()
{
	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			JOptionPane.showMessageDialog(null,"That user is not online or does not exist.",		 //tell initiating client that friendship has not been established
											   "Friend Request Failed", JOptionPane.ERROR_MESSAGE);
		}
	}
 	);
}
//==================================================================================
void addFriendToList(String messageIn)
{
	String userRequesting = messageIn.split(" ")[1];	//grab friend to add to list

	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()								//add them to client JList
		{
			//clientPtr.addFriendToList(userRequesting,true);
			clientInterfacePtr.addFriendToList(userRequesting,true);
	 	}
	}
	);
}
//==================================================================================
void buildFriendList(String messageIn)
{
	String friendToAdd = messageIn.split(" ")[1];					//get name and status from key	(I think this is primarily used to populate on launch)
	boolean status = Boolean.parseBoolean(messageIn.split(" ")[2]);	//make it a boolean

	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			//clientPtr.addFriendToList(friendToAdd,status);			//add friend to JList
			clientInterfacePtr.addFriendToList(friendToAdd,status);
		}
	}
	);
}
//==================================================================================
void friendOffline(String messageIn)
{
	String offlineFriend = messageIn.split(" ")[1];					//grab user name who went offline

	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			//for(int i=0;i<clientPtr.buddiesList.size();i++)		//for each of offline client's buddies
			for(int i=0;i<clientInterfacePtr.buddiesList.size();i++)
			{
				//String temp = clientPtr.buddiesList.elementAt(i).toString();	//(this is the best way i could think of to do this, nothing else wanted to work for me)
				String temp = clientInterfacePtr.buddiesList.elementAt(i).toString();
				if(temp.equals("[ONLINE] " + offlineFriend))					//if they are in the JList
				{
					//clientPtr.buddiesList.elementAt(i).setOnlineStatus(false);
					clientInterfacePtr.buddiesList.elementAt(i).setOnlineStatus(false);   //change online status to offline
					clientInterfacePtr.revalidate();
				}
			}																	//issue with status updating in real time
		}																		//(tried revalidate and stuff to no avail, user has to interact for changes to display)
	}
	);
}
//==================================================================================
void friendOnline(String messageIn)
{
	String onlineFriend = messageIn.split(" ")[1];			//grab user name who came online

	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			//for(int i=0;i<clientPtr.buddiesList.size();i++)	//for each of the online user's buddies
			for(int i=0;i<clientInterfacePtr.buddiesList.size();i++)
			{
				//String temp = clientPtr.buddiesList.elementAt(i).toString();		//check if they're in the DLM
				String temp = clientInterfacePtr.buddiesList.elementAt(i).toString();
				if(temp.equals("[OFFLINE] " + onlineFriend))
				{
					//clientPtr.buddiesList.elementAt(i).setOnlineStatus(true);
					clientInterfacePtr.buddiesList.elementAt(i).setOnlineStatus(true);    //set that status to online
					clientInterfacePtr.revalidate();
				}
			}
		}																		//also issue with status updating in real time
	}																			//(tried revalidate and stuff to no avail, user has to interact for changes to display)
	);
}
//==================================================================================
void chatMessageRecieved(String messageIn)
{
	String sendingUser = messageIn.split(" ")[1].trim();							//get user who sent message
	int nameStartIndex = messageIn.indexOf(sendingUser);							//find where the username starts in the key
	int cutter = nameStartIndex + sendingUser.length();								//find the start of the actual message by adding username length to start index
	String messageOnly = messageIn.substring(cutter,messageIn.length()).trim();		//save the actual message in a string by eliminating the protocol and username

	SwingUtilities.invokeLater														//this approach was used in order to avoid chopping the message with .split(" "),
	( new Runnable()																//as the message will likely contain spaces
	{
		public void run()
		{
			//for(int i=0;i<clientPtr.buddiesList.size();i++)
			for(int i=0;i<clientInterfacePtr.buddiesList.size();i++)								//for each buddy of the client
			{
				//String checkString = clientPtr.buddiesList.elementAt(i).friendName;
				String checkString = clientInterfacePtr.buddiesList.elementAt(i).friendName;		//search for person who send message
				if(checkString.equals(sendingUser))										//if found
				{
					//Friend friendToChat = clientPtr.buddiesList.elementAt(i);
					Friend friendToChat = clientInterfacePtr.buddiesList.elementAt(i);


					if(friendToChat.conversationWindow == null)								//check if conversationWindow exists
					{
						friendToChat.startChat(ConnectionToServer.this,clientId);			//if one doesn't, make one
						friendToChat.conversationWindow.setVisible(true);
						friendToChat.conversationWindow.requestFocus();
						friendToChat.addToEditor(sendingUser + ": " + messageOnly,"#0000FF");	//append message and sender with specified font color
					}
					else
					{
						friendToChat.addToEditor(sendingUser + ": " +messageOnly,"#0000FF");//one exists so append message
						friendToChat.conversationWindow.setVisible(true);
						friendToChat.conversationWindow.requestFocus();						//request window be focused for recieveing user
					}
					break;
				}
			}
		}
	}
	);
}
//==================================================================================
void sendMessage(String messageIn)
{
	try
	{
		serverTalker.sendMsg(messageIn);													//pass message through CTS
	}
	catch(IOException ioe)
	{
		System.out.println("Error in CTS - sendMessage.");
	}
}
//==================================================================================
synchronized boolean establishUser()
{
	try
	{
		serverTalker = new Talker(serverDomain,serverPort);		//create talker
		if(serverTalker.getMsg().equals("Welcome."))			//if connection established and server welcomes
		{
			serverTalker.sendMsg(userKey);						//send protocl username password
			String response = serverTalker.getMsg();
			if(response.equals("Success."))						//if server accepts login/registration
			{
				return true;									//return true
			}
			else if(response.equals("Login Failed."))
			{
				JOptionPane.showMessageDialog					//login information was invalid
					(null, "Invalid Login Information.",
					  	   "Login Error", JOptionPane.ERROR_MESSAGE);;	//alert user
			}
			else if(response.equals("Registration Failed."))	//registration failed
			{
				JOptionPane.showMessageDialog
					(null, "Account is already registered.",
						   "Registration Error", JOptionPane.ERROR_MESSAGE); //alert user
			}
		return false;
		}
		else
		{
			JOptionPane.showMessageDialog						//talker creation failed
				(null, "The server failed to accept client connection.",
					   "Error", JOptionPane.ERROR_MESSAGE);  //alert user of unsuccessful connection
			return false;
		}
	}
	catch(IOException ioe)
	{
		System.out.println("Error in CTS, establishUser.");
		return false;
	}
}
//==================================================================================
}