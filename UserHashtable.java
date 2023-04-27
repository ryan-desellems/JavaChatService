//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.util.*;									//hashtable of users
import java.io.*;

//==================================================================================
class UserHashtable extends Hashtable<String,User>
{
//==================================================================================
UserHashtable()
{
}
//==================================================================================
UserHashtable(DataInputStream disIn)
{
	int size  = 0;
	int count = 0;

	try
	{
		size = disIn.readInt();

		while(count < size)								//read from line from file until empty
		{
			User tempUser = new User();
			tempUser.build(disIn);						//create user from file line
			this.put(tempUser.username,tempUser);		//store in table
			count++;
		}
	}
	catch(IOException ioe)
	{
		ioe.printStackTrace();
		System.out.println("Error in UserHashtable ");
	}
}
//==================================================================================
void writeToFile(DataOutputStream dosIn) throws IOException
{
	int 			  tableSize = this.size();
	Enumeration<User> userList;

	userList = this.elements();							//write each element in hashtable to file

	dosIn.writeInt(tableSize);
	while (userList.hasMoreElements())
	{
		userList.nextElement().store(dosIn);
	}
}
//==================================================================================
}