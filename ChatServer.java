//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;									//main server for chat application
import java.net.*;

//==================================================================================
class ChatServer
{
	UserHashtable userTable;								//table owned and used by server

//==================================================================================
ChatServer()
{
}
//==================================================================================
void startServer()
{
	System.out.println("===SERVER BOOT===");	//boot server

	try
	{
		ServerSocket 	serverSocket;
		Socket			normalSocket;
		boolean			running = true;

		serverSocket = new ServerSocket(3737);  // listen on port 3737

		constructTable();

		while(running)
		{
			try									//constantly acccept connections
			{
	    		normalSocket = serverSocket.accept();									//if successful
	    		ConnectionToClient ctc = new ConnectionToClient(normalSocket,this);		//create CTC with socket and ptr to server
	    		System.out.println("Connection Established with Client.");
			}
			catch(IOException ioe)
			{
				System.out.println("Couldn't establish connection to client.");
			}
		}
	}
	catch (IOException ioe2)
	{
	    System.out.println("Server closed.");											//if there's another server open close
	    System.exit(1);
    }
}

//==================================================================================
boolean attemptLogin(String usernameIn, String passwordIn)
{
	if(userTable.containsKey(usernameIn))						//if username is in hashtable
	{
		User temp = userTable.get(usernameIn);					//grab associated data
		if(temp.getPassword().equals(passwordIn))				//compare passwords
		{
			return true;										//if username exists and passwords match return true
		}
	}
	return false;
}
//==================================================================================
boolean attemptRegister(String usernameIn, String passwordIn)
{
	if(!userTable.containsKey(usernameIn))						//if user name isnt in hashtable
	{
		User temp = new User(usernameIn,passwordIn);			//create user with name and password
		userTable.put(usernameIn,temp);							//store in hashtable
		saveUsers();
		return true;
	}
	return false;
}
//==================================================================================
User grabUser(String userToGrab)
{
	try
	{
		if(userTable.containsKey(userToGrab))						//if username is in hashtable
		{
			return userTable.get(userToGrab);						//grab and return user
		}
		else
		{
			System.out.println("No such user");
			return null;											//user not in table
		}
	}
	catch(NullPointerException npe)
	{
		System.out.println("npe error in grab user");
		return null;												//guard against null exceptions
	}
}
//==================================================================================
boolean attemptFriendRequest(String friendRequesting, String friendToAdd)
{
	if(userTable.containsKey(friendToAdd.trim()))
	{
		User temp = userTable.get(friendToAdd.trim());			//user to be added is in the table

		if(temp.isOnline())										//user to add is online
		{
			ConnectionToClient friendToAddCTC = temp.getCTC();
			friendToAddCTC.passMsg("FRIENDREQUEST " + friendRequesting + " " + friendToAdd.trim());		//tell client what friend to add and who its from

			return true;
		}
		else if(!temp.isOnline())																		//friend to add is not online
		{
			System.out.println(friendToAdd + " isn't online...");
			return false;
		}
		return true;
	}
	else
	{
		return false;
	}

}
//==================================================================================
void saveUsers()
{
	DataOutputStream dos;													// write users to file
	File			 outFile;

	try
	{
		outFile = new File("ServerUserRegistry");

		dos = new DataOutputStream(new FileOutputStream(outFile));			//feed dos to hashtable and write it to file
		userTable.writeToFile(dos);
	}
	catch(IOException ioe)
	{
		System.out.println("Fatal error in saving users.");
		ioe.printStackTrace();
		System.exit(1);
	}
}
//==================================================================================
void constructTable()
{
	DataInputStream dis;
	File			inFile;

	try																		//launch server table
	{
		inFile = new File("ServerUserRegistry");

		dis = new DataInputStream(new FileInputStream(inFile));				//populate from file
		userTable = new UserHashtable(dis);
	}
	catch(IOException ioe)
	{
		System.out.println("Building new table");							//no file is found, start from scratch
		userTable = new UserHashtable();
	}
}
//==================================================================================
}