//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.awt.*;
import javax.swing.*;
import java.lang.*;                //Class that handles the UI and login
import java.awt.event.*;
import java.io.*;
import javax.swing.event.*;


//==================================================================================
class ClientInterface extends JFrame	 implements
												ActionListener,
												DocumentListener,
												ListSelectionListener,
												MouseListener
{
	String 			clientUser;

	JFrame						dmFrame;					//date members for buddy window
	JList						buddiesListBox;
	DefaultListModel<Friend>	buddiesList;
	JPanel						buddiesPanel;
	JPanel						buttonsPanel;
	JButton						addBuddiesButton;
	JButton						chatButton;
	JScrollPane					buddiesScroller;

	ConnectionToServer 	clientCTS = null;

	int	selectedFriend;

	static String loopback = "127.0.0.1";

//==================================================================================
ClientInterface(String ownerName, ConnectionToServer ctsIn)
{
	clientUser = ownerName;
	clientCTS  = ctsIn;

	buddiesPanel	 = new JPanel();
	buttonsPanel	 = new JPanel();
	buddiesList		 = new DefaultListModel<Friend>();
	buddiesListBox	 = new JList(buddiesList);
	buddiesScroller  = new JScrollPane(buddiesListBox);
	addBuddiesButton = new JButton("Add Buddy");
	chatButton = new JButton("Chat!");

	buddiesListBox.setFont(new Font("Courier",1,12));		//set up client buddies window

	setTitle(ownerName + "'s Chat Client");			//tell who owns which window, very helpful

	addBuddiesButton.setActionCommand("ADDFRIEND");
	addBuddiesButton.addActionListener(this);
	chatButton.setActionCommand("CHAT");
	chatButton.addActionListener(this);
	chatButton.setEnabled(false);
	buddiesListBox.addListSelectionListener(this);
	buddiesListBox.addMouseListener(this);

	buddiesPanel.add(buddiesScroller);
	buttonsPanel.add(addBuddiesButton);						//add elements to frame
	buttonsPanel.add(chatButton);

	add(buddiesPanel,BorderLayout.CENTER);
	add(buttonsPanel,BorderLayout.SOUTH);

	Toolkit tk = Toolkit.getDefaultToolkit();				//set up jframe
	Dimension d = tk.getScreenSize();
	setSize(d.width/4,d.height/2);
	setLocation(d.width*3/8,d.height*1/4);			//make it look okay
	setVisible(true);
}
//==================================================================================
public void actionPerformed(ActionEvent e)
{
	if(e.getActionCommand().equals("ADDFRIEND"))							//if add friend button pressed
	{
		processAddFriend();														//handle friending
	}
	if(e.getActionCommand().equals("CHAT"))									//if chat friend button pressed
	{
		chatFriend();														//open or show window of selected Friend
	}
}
//==================================================================================
void processAddFriend()
{
	String fullkey;

	JOptionPane friendRequester = new JOptionPane();					//open add friend window
	String userToFind = friendRequester.showInputDialog					//get userinput
						(null, "Add Friend: ", "Add A Friend", 3);	 	//----------------------------------------------------------figure out how fix error when dialog is closed or cancelled, not sure on this

	if(!userToFind.trim().contains(" ") && userToFind.trim() != null && !userToFind.trim().equals(clientUser))	// if friendname isn't empty, doens't contain spaces
	{																											// and isn't trying to friend itself
		try
		{
			clientCTS.serverTalker.sendMsg("FRIENDREQUEST " + clientUser + " " + userToFind);					//send friend request protocol to with who sent and who its going to
		}
		catch(IOException ioe)
		{
			System.out.println("Error in ADDFRIEND IN actionPerformed");										//debug code
		}
	}
	else
	{
		JOptionPane.showMessageDialog																			//friend input wasn't valid
					(null,"Invalid friend!",
				  		  "Invalid friend!",JOptionPane.ERROR_MESSAGE);
	}
}
//==================================================================================
void chatFriend()
{
	Friend chatBuddy = buddiesList.elementAt(selectedFriend);			//store buddy in temp variable

	if(chatBuddy.isOnline())
	{
		if(chatBuddy.conversationWindow == null)							//if the selected element (Friend) has a null chat window
		{
			chatBuddy.startChat(clientCTS,clientUser);						//open one (method in Friend.java)
		}
		else
		{
			chatBuddy.conversationWindow.requestFocus();					//else focus it
		}
	}
	else
	{
		JOptionPane.showMessageDialog(null,
									  chatBuddy.friendName + " is offline.",
									  "Friend Offline",
									  JOptionPane.ERROR_MESSAGE);
	}
}
//==================================================================================
public void changedUpdate(DocumentEvent e)
{
}
public void insertUpdate(DocumentEvent e)
{
}
public void removeUpdate(DocumentEvent e)
{
}
//==================================================================================
public void mouseExited(MouseEvent e)
{
}
public void mouseEntered(MouseEvent e)
{
}
public void mouseReleased(MouseEvent e)
{
}
public void mousePressed(MouseEvent e)
{
}
public void mouseClicked(MouseEvent e)
{
	if (e.getClickCount() == 2)
	{
		selectedFriend = buddiesListBox.locationToIndex(e.getPoint());	//clicky click makes friend window open
		chatFriend();
	}
}
//==================================================================================
public void valueChanged(ListSelectionEvent e)
{
	selectedFriend = buddiesListBox.getSelectedIndex();					//if friend selected is valid, enabled chat button

	if(selectedFriend != -1)
	{
		System.out.println("Chatting with " + buddiesList.elementAt(selectedFriend));
		chatButton.setEnabled(true);
	}
}
//==================================================================================
void addFriendToList(String friendName, boolean status)
{
	SwingUtilities.invokeLater										//add friend to DFM with friend Generic
	( new Runnable()
	{
		public void run()											//toString is overwritten to displayed based on given status
		{
			buddiesList.addElement(new Friend(friendName,status));
		}
	}
 	);
}
//==================================================================================
}

