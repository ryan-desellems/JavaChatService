//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;						//Talker class to communicate between server and client
import java.net.*;

//===============================================================
public class Talker
{
private BufferedReader 		inputReader;									//force users to use Talker methods
private DataOutputStream	outputWriter;
private	String				clientID = "CLIENT PENDING ID...";

//===============================================================
Talker(Socket inSocket) throws IOException
{
	inputReader	 = new BufferedReader(new InputStreamReader(inSocket.getInputStream()));
	outputWriter = new DataOutputStream(inSocket.getOutputStream());		//create talker from socket
}
//===============================================================
Talker(String domainIn, int portIn) throws IOException
{
	Socket clientSocket;

	clientSocket = new Socket(domainIn,portIn);
	inputReader	 = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	outputWriter = new DataOutputStream(clientSocket.getOutputStream());  //create talker from domain and port
}
//===============================================================
void setUser(String userIn) throws IOException
{
	clientID = userIn;														//set talker ID
}
//===============================================================
void sendMsg(String stringToSend) throws IOException
{
	outputWriter.writeBytes(stringToSend + '\n');							//send message and echo to console
	System.out.println("[ID: " + clientID + "] out: " + stringToSend);
}
//===============================================================
String getMsg() throws IOException
{
	String temp = inputReader.readLine();									//recieve message and echo to console
	String fromUser = "???";

	System.out.println("[ID: " + fromUser + "] in: "+ temp + '\n');
	return temp;
}
//===============================================================
}
