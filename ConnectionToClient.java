//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.net.*;								//Server sided application that handles thread and prepares for asynchronous messaging
import java.io.*;
import javax.swing.*;

//==================================================================================
public class ConnectionToClient
								implements Runnable
{
	Talker 						clientTalker;					//data members
	ChatServer  				serverPtr;
	String						clientUser;

//==================================================================================
ConnectionToClient(Talker inTalker, ChatServer inPtr)
{
	clientTalker 	= inTalker;											//accept talker and pointer for server, start thread
	serverPtr 		= inPtr;

	new Thread(this).start();
}
//==================================================================================
ConnectionToClient(Socket inSocket, ChatServer inPtr)
{
	try
	{
		serverPtr 		= inPtr;											    //build talker and pointer for server, start thread
		clientTalker	= new Talker(inSocket);
		new Thread(this).start();
	}
	catch(IOException ioe)
	{
		System.out.println("Error in CTC, talker construction.");
	}
}
//==================================================================================
public void run()
{
	if (establishUser())												//if user was able to login or register
	{
		userIsOnline();													//announce online status to friends
		getOnlineUsers();												//find which friends are online
		while(true)
		{
			try
			{
				String messageIn = clientTalker.getMsg();				//being asynchronous message reception
				if(!messageIn.trim().equals(""))
				{
					processMessageRecieved(messageIn);					//if a message is received, process
				}
			}
			catch(IOException ioe)
			{
				userWentOffline();										//the user if offline, alert friends
				break;													//exit while loop
			}
		}
	}
}
//==================================================================================
void processMessageRecieved(String messageIn) throws IOException
{
	int 	protoEndPos 	 = messageIn.indexOf(" ");					//break apart message into protocol
	String	protocolRecieved = messageIn.substring(0,protoEndPos);

	if (protocolRecieved.equals("FRIENDREQUEST"))						//protocol indicated user had sent a friend req
	{
		handleFriendRequest(messageIn);
	}
	else if (protocolRecieved.equals("ACCEPTEDFRIEND"))					//protocol indicating user accepted friend req
	{
		handleAcceptedFriend(messageIn);
	}
	else if(protocolRecieved.equals("ADDFRIENDTOLIST"))   				//protocol indicating to add friend to list
	{
		addFriendToList(messageIn);
		serverPtr.saveUsers();											//write users to store friend changes
	}
	else if(protocolRecieved.equals("MESSAGE"))    						//protocol indicating chat message was recieved
	{
		forwardChatMessage(messageIn);
	}
}
//==================================================================================
void handleFriendRequest(String messageIn) throws IOException
{
	String[] friendKey = messageIn.split(" ");
	if(serverPtr.attemptFriendRequest(friendKey[1],friendKey[2]))					//attempt a friend request between the two parties
	{
	}
	else
	{
		clientTalker.sendMsg("FRIENDFAILED " + friendKey[1] + " " + friendKey[2]); //inform user that friend request failed
	}
}
//==================================================================================
void handleAcceptedFriend(String messageIn)
{
	String[] friendKey 		= messageIn.split(" ");					//friend was accepted, make necessary updates
	String	 requestingUser = friendKey[1];
	String	 userToAdd		= friendKey[2];							//get the user to add and who requested the friend

	requestingUser	= requestingUser.trim();
	User temp		= serverPtr.grabUser(requestingUser);
	ConnectionToClient tempCTC = temp.getCTC();						//grab user who requested friend

	if(tempCTC != null)												//if they're online
	{
		tempCTC.passMsg("FRIENDACCEPTED " + requestingUser + " " + userToAdd); //let them know they were accepted by requested friend
	}
	else
	{
		System.out.println("Initiaiting client offline.");			//client offline
	}
}
//==================================================================================
void addFriendToList(String messageIn)
{
	String[] friendKey 		= messageIn.split(" ");					//add friend to friends list
	String	 requestingUser = friendKey[1];
	String	 userToAdd		= friendKey[2];							//get to and from users

	requestingUser				= requestingUser.trim();
	User toUser					= serverPtr.grabUser(userToAdd);
	ConnectionToClient tempCTC  = toUser.getCTC();					//grab user the request is too

	requestingUser				= requestingUser.trim();
	User fromUser				= serverPtr.grabUser(requestingUser);	//grab user request is from

	if(tempCTC != null)												//if user friend request is going to is online
	{
		tempCTC.passMsg("ADDFRIENDTOLIST " + requestingUser + " " + userToAdd); //tell them who to add to list

		toUser.addBuddy(requestingUser);
		fromUser.addBuddy(userToAdd);								//add each buddy to the users friend list
	}
	else
	{
		System.out.println("Initiaiting client offline.");
	}
}
//==================================================================================
void forwardChatMessage(String messageIn)
{
	String targetUser = messageIn.split(" ")[1].trim();								//code that will trim the protocol and user from message
	System.out.println("Target user: " + targetUser);								//preserving the spaces and message in its own string
	int placeholder = messageIn.indexOf(targetUser);
	System.out.println("Index of username: " + placeholder);
	int cutter = placeholder + targetUser.length();

	String messageOnly = messageIn.substring(cutter,messageIn.length()).trim();

	User personToMessage = serverPtr.grabUser(targetUser);							//find person message is going to
	if(personToMessage.isOnline())
	{
		ConnectionToClient personToMessageCTC = personToMessage.getCTC();
		personToMessageCTC.passMsg("MESSAGE " + clientUser + " " + messageOnly);	//grab their ctc and send the message content
	}
}
//==================================================================================
void userWentOffline()
{
	User offlineUser = serverPtr.grabUser(clientUser);				//get user that went offline from usertable

	for(int i=0;i<offlineUser.friendsList.size();i++)				//for each friend in the user table
	{
		User temp;
		ConnectionToClient tempCTC;

		temp = serverPtr.grabUser(offlineUser.friendsList.elementAt(i));	//grab the friend
		if(temp.isOnline())													//if the friend is online
		{
			tempCTC = temp.getCTC();
			tempCTC.passMsg("USEROFFLINE " + clientUser);					//notify them that this user is offline
		}
	}
}
//==================================================================================
void userIsOnline()
{
	User onlineUser = serverPtr.grabUser(clientUser);						//grab this user

	if(onlineUser != null && onlineUser.friendsList != null )				//if this user exists and has friends
	{
		for(int i=0;i<onlineUser.friendsList.size();i++)					//for each of this users friends
		{
			User temp;
			ConnectionToClient tempCTC;

			temp = serverPtr.grabUser(onlineUser.friendsList.elementAt(i));	//grab their ctc
			if(temp.isOnline())												//if theyre online
			{
				tempCTC = temp.getCTC();
				tempCTC.passMsg("USERONLINE " + clientUser);				//let them know this user is online
			}
		}
	}
}
//==================================================================================
void getOnlineUsers()
{
	//grab users buddy list and message they are offline
	User updateUser = serverPtr.grabUser(clientUser);						//for this user

	if(updateUser != null)													//if this user exists
	{
		for(int i=0;i<updateUser.friendsList.size();i++)					//for each of this users friends
		{
			User temp;

			temp = serverPtr.grabUser(updateUser.friendsList.elementAt(i)); //grab the friend
			if(temp.isOnline())												//if theyre online
			{
				try
				{
					clientTalker.sendMsg("USERONLINE " + temp.getUsername());	//and tell them that this user is now online
				}
				catch(IOException ioe)
				{
				}
			}
		}
	}
}
//==================================================================================
void passMsg(String messageIn)
{
	try
	{
		clientTalker.sendMsg(messageIn);									//method to pass a message from a ctc, especially when accessing from another user
	}
	catch(IOException eieio)
	{
	}
}
//==================================================================================
void sendBuddyList(User userToBuild)
{
	try
	{
		if(!userToBuild.friendsList.isEmpty())								//if the user to build friends for has friends
		{
			for(int i=0;i<userToBuild.friendsList.size();i++)				//for each friend
			{
				boolean onlineState = serverPtr.grabUser(userToBuild.friendsList.elementAt(i)).isOnline();			//get their online status
				clientTalker.sendMsg("BUILDFRIEND " + userToBuild.friendsList.elementAt(i) + " " + onlineState);	//send buddy and status to be added to client's JList
			}
		}
		else
		{
			System.out.println("empty list");								//debug code
		}
	}
	catch(IOException ioe)
	{
		System.out.println("error in sendbuddylist");						//debug code
	}
}
//==================================================================================
synchronized boolean establishUser()
{
	try
	{
		clientTalker.sendMsg("Welcome.");								//welcome clients

		String[] tempUserInfo = clientTalker.getMsg().split(" ");		//clients will send key
		String tempProtocol = tempUserInfo[0];							//split key into necessary components
		String tempUsername = tempUserInfo[1];
		String tempPassword = tempUserInfo[2];

		if (tempProtocol.equals("LOGIN"))								//if the log in button was pressed
		{
			if(serverPtr.attemptLogin(tempUsername,tempPassword))		//try to login
			{
				clientTalker.sendMsg("Success.");						//let client know it worked
				clientTalker.setUser(tempUsername);
				User thisUser = serverPtr.grabUser(tempUsername);		//build user from hasttable
				thisUser.attachCTC(this);								//hook this ctc on to it
				clientUser = tempUsername;
				sendBuddyList(thisUser);								//build users friends
				return true;
			}
			else
			{
				clientTalker.sendMsg("Login Failed.");
				return false;
			}
		}
		else if(tempProtocol.equals("REGISTER"))                  		//if the register button was pressed
		{
			if(serverPtr.attemptRegister(tempUsername,tempPassword))	//try to register
			{
				clientTalker.sendMsg("Success.");						//let client know it worked
				clientTalker.setUser(tempUsername);
				User thisUser = serverPtr.grabUser(tempUsername);		//server will have created a user in attemptRegister, this will fetch it
				thisUser.attachCTC(this);								//hook this ctc on to it
				serverPtr.saveUsers();
				clientUser = tempUsername;								//if you forget this line you will spend the better part of 2 hours trying to find out why a registered user's
				return true;											//offline and online status doesn't change when they go offline
			}
			else
			{
				clientTalker.sendMsg("Registration Failed.");			//debug code
				return false;
			}
		}
		else
		{
			clientTalker.sendMsg("Failed.");
			return false;													//everything else failed, bad login
		}
	}
	catch(IOException ioe)
	{
		System.out.println("Error in CTC, establishUser.");				//debug code
		return false;
	}
	catch(Exception e)
	{
		System.out.println("Error in key values.");					//debug code
		e.printStackTrace();
		return false;
	}
}
//==================================================================================
}