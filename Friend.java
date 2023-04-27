//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;
import javax.swing.*;

//==================================================================================
class Friend
{
	String	 	friendName;
	boolean	 	online = false;
	ChatWindow  conversationWindow;

//==================================================================================
Friend()
{

}
//==================================================================================
Friend(String nameToSet)
{
	friendName = nameToSet;											//add friend name
}
//==================================================================================
Friend(String nameToSet, boolean status)
{
	friendName = nameToSet;											//add friend name and online statue
	online	   = status;
}
//==================================================================================
void setOnlineStatus(boolean status)
{
	online = status;												//set online status using argument
}
//==================================================================================
boolean isOnline()
{
	return online;													//tell if friend is online
}
//==================================================================================
void startChat(ConnectionToServer ctsIn,String ownerIn)
{
	conversationWindow = new ChatWindow(ctsIn,ownerIn,friendName);	//initialize chatWindow
	conversationWindow.openChat();									//open window GUI and pass the name of this buddy (in ChatWindow.java)
}
//==================================================================================
void addToEditor(String inText,String color)
{
	conversationWindow.addToEditor(inText,color);					//add text to editor (in ChatWindow.java)
}
//==================================================================================
@Override
public String toString()
{
	if(online)
	{
		String temp = ("[ONLINE] " + friendName);					//if friend is online, adjust string accordingly
		return temp;
	}
	else
	{
		String temp = ("[OFFLINE] " + friendName);                 //if friend is offline, adjust string accordingly
		return temp;
	}
}
//==================================================================================
}