//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;							//class representing User Data
import javax.swing.*;

//==================================================================================
public class User
{
	String		 				username;
	String	 					password;
	DefaultListModel<String>	friendsList;	//track friends of user
	ConnectionToClient 			ctc = null;

//==================================================================================
User()
{
	friendsList	= new DefaultListModel();
}
//==================================================================================
User(String usernameIn, String passwordIn)		//Create a user with name and password
{
	username 	= usernameIn;
	password 	= passwordIn;
	friendsList	= new DefaultListModel();
}
//==================================================================================
User(String usernameIn, String passwordIn, ConnectionToClient ctcIn)		//Create a user with name and password
{
	username 	= usernameIn;
	password 	= passwordIn;
	ctc 	 	= ctcIn;
	friendsList	= new DefaultListModel();
}
//==================================================================================
User(DataInputStream disIn) throws IOException
{												//construct user from DataInputStream
	String userKey 	= disIn.readUTF();
	friendsList		= new DefaultListModel();

	System.out.println("USER KEY: " + userKey);

	System.out.println(userKey);
	String key[] 	= userKey.split(" ");			//split into appropriate components
	username 		= key[0];
	password		= key[1];

	final	int	friendPos	= 3;				//friends start in key on 3rd position (0.username 1.password 2.#offriends 3.friendnamesbegin)
												//							   example (ryan 1234 2 dave chris)

	if(key.length > 2)								//if key is longer than two there are friends
	{
		int friendsSize = Integer.parseInt(key[2]);	//get how many friends

		for (int count = 0; count<friendsSize; count++) //for those friends
		{
			friendsList.addElement(key[count+friendPos]);	//add them to the list
		}
	}
}
//==================================================================================
void store(DataOutputStream dos) throws IOException
{
	String temp 	= "";
	int    count	= 0;

	if(!friendsList.isEmpty())										//if people like the user
	{
		for (count = 0; count<friendsList.size(); count++)			//for each friend in the friends list
		{
			temp = temp.concat(friendsList.elementAt(count) + " ");	//add them to the string, placing a space between (spaces will be used to read and write in the keys)
		}
		temp = count + " " + temp;									//add number of friends to string preceding list of friends

		dos.writeUTF(username + " " + password + " " + temp); 		//write username and password to stream, then add the string with the friend count and friend list
	}
	else
	{
		dos.writeUTF(username + " " + password);					//the user is lonely, just write username and password
	}
}
//==================================================================================
void attachCTC(ConnectionToClient ctcIN)
{
	ctc = ctcIN;									//assign user a CTC
}
//==================================================================================
ConnectionToClient getCTC()
{
	return ctc;									//return user CTC
}
//==================================================================================
String getUsername()
{
	return username;									//return username
}
//==================================================================================
String getPassword()
{
	return password;								//return password
}
//==================================================================================
boolean isOnline()
{
	return ctc != null;								//get password
}
//==================================================================================
void addBuddy(String buddyToAdd)
{
	friendsList.addElement(buddyToAdd);				//add friend to user's list
}
//==================================================================================
void showBuddies()
{
	System.out.println("Showing buddies");			//debug function to check friends in list
	for(int i=0;i<friendsList.size();i++)
	{
		System.out.println("Buddy at index " + i + ": " + friendsList.elementAt(i));
	}
}
//==================================================================================
void build(DataInputStream disIn) throws IOException
{													//construct user from DataInputStream
	String temp 	= disIn.readUTF();				//split into appropriate components

	System.out.println("USER KEY: " + temp);

	String key[]	 		= temp.split(" ");
	username	 			= key[0];
	password				= key[1];

	final	int	friendPos	= 3;				//friends start in key on 3rd position (0.username 1.password 2.#offriends 3.friendnamesbegin)
												//								example(ryan 1234 2 dave chris)
	if(key.length > 2)							//thus a key longer than two indicates friends
	{
		int friendsSize = Integer.parseInt(key[2]);	//get number of friends

		for (int count = 0; count<friendsSize; count++)	//for each friend
		{
			friendsList.addElement(key[count+friendPos]);//add to friends list
		}
	}
}
//==================================================================================
}