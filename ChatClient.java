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
class ChatClient	implements
							ActionListener,
							DocumentListener,
							ListSelectionListener
{
	JDialog 		window;									//data members for login window
	JLabel			IDLabel;
	JTextField		ID;
	JLabel			passwordLabel;
	JPasswordField	passwordField;
	JButton			register;
	JButton			logIn;
	JPanel			midPanel;
	JPanel			buttonPanel;

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
ChatClient()
{
	System.out.println("===CLIENT BOOT===");

	window 			= new JDialog();
	IDLabel			= new JLabel("ID");						//initalize data members
	ID				= new JTextField(20);
	passwordLabel	= new JLabel("Password");
	passwordField	= new JPasswordField(20);
	logIn			= new JButton("Log In");
	register		= new JButton("Register");
	midPanel 		= new JPanel();
	buttonPanel		= new JPanel();

	midPanel.add(IDLabel);
	midPanel.add(ID);
	midPanel.add(passwordLabel);
	midPanel.add(passwordField);						//add members to panels and buttons
	buttonPanel.add(logIn);
	buttonPanel.add(register);

	logIn.addActionListener(this);
	logIn.setActionCommand("LOGIN");
	register.addActionListener(this);					//set up listeners
	register.setActionCommand("REGISTER");

	ID.getDocument().addDocumentListener(this);
	passwordField.getDocument().addDocumentListener(this);


	GroupLayout grid = new GroupLayout(midPanel);
	midPanel.setLayout(grid);
	grid.setAutoCreateGaps(true);
	grid.setAutoCreateContainerGaps(true);
	GroupLayout.SequentialGroup horizontal	= grid.createSequentialGroup();     //Group layout to align elements
	GroupLayout.SequentialGroup vertical	= grid.createSequentialGroup();

	horizontal.addGroup(grid.createParallelGroup().
					  addComponent(IDLabel).
					  addComponent(passwordLabel));
	horizontal.addGroup(grid.createParallelGroup().
					  addComponent(ID).
					  addComponent(passwordField));
	grid.setHorizontalGroup(horizontal);

	vertical.addGroup(grid.createParallelGroup(GroupLayout.Alignment.BASELINE).
					  addComponent(IDLabel).
					  addComponent(ID));
	vertical.addGroup(grid.createParallelGroup(GroupLayout.Alignment.BASELINE).
				   	  addComponent(passwordLabel).
					  addComponent(passwordField));
	grid.setVerticalGroup(vertical);

	window.add(midPanel,BorderLayout.CENTER);								//set up window
	window.add(buttonPanel,BorderLayout.SOUTH);

	Toolkit tk = Toolkit.getDefaultToolkit();									//set up jdialog
	Dimension d = tk.getScreenSize();
	window.setSize(d.width/4,d.height/4);
	window.setLocation(d.width*3/8,d.height*3/8);
	window.setVisible(true);

	logIn.setEnabled(false);												//buttons are off by default
	register.setEnabled(false);

}
//==================================================================================
public void actionPerformed(ActionEvent e)
{
	String 				fullkey;

	if(e.getActionCommand().equals("LOGIN"))								//if login button pressed
	{
		processLogin();															//handle login
	}
	if(e.getActionCommand().equals("REGISTER"))								//if register button pressed
	{
		processRegister();														//handle registration
	}
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
void processLogin()
{
	String fullkey;

	if(ID.getText().contains(" "))											//if username has a space
	{
		JOptionPane.showMessageDialog										//inform user this isn't allowed, (proper spaces are crucial to the programs function)
				(null,"Username can't contain spaces.",						//login should catch this anyway
					  "Username Error",JOptionPane.ERROR_MESSAGE);
		return;
	}
	else																	//user name is valid
	{
		fullkey = "LOGIN " + ID.getText().trim() + " " + passwordField.getText();	//grab user information and put in key
		clientCTS = new ConnectionToServer(loopback,3737,ID.getText().trim(),this);	//create a cts based on this information
		clientCTS.setKey(fullkey);													//send login info to cts, which will be sent to server when thread starts
	}
}
//==================================================================================
void processRegister()
{
	String fullkey;

	if(ID.getText().contains(" ") || passwordField.getText().contains(" "))	//if username or password have a space
	{
		JOptionPane.showMessageDialog
			(null,"Username/Password can't contain spaces.",				//this isn't allowed so tell user, (proper spaces are crucial to the programs function)
				  "Username/Password Error",JOptionPane.ERROR_MESSAGE);
		return;
	}
	else																				//input is valid(ish)
	{
		fullkey = "REGISTER " + ID.getText().trim() + " " + passwordField.getText();	//grab information and put in key
		clientCTS = new ConnectionToServer(loopback,3737,ID.getText().trim(),this);		//create cts from this info
		clientCTS.setKey(fullkey);														//send registration info to cts, which will be sent to server when thread starts
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

	if(chatBuddy.conversationWindow == null)							//if the selected element (Friend) has a null chat window
	{
		chatBuddy.startChat(clientCTS,clientUser);						//open one (method in Friend.java)
	}
	else
	{
		chatBuddy.conversationWindow.requestFocus();					//else focus it
	}
}
//==================================================================================
public void changedUpdate(DocumentEvent e)
{
	processInput(e);                                               //process input from text fields accordingly
}                                                                  //pretty much just enables/disables login/register button
public void insertUpdate(DocumentEvent e)
{
	processInput(e);
}
public void removeUpdate(DocumentEvent e)
{
	processInput(e);
}
//==================================================================================
public void valueChanged(ListSelectionEvent e)
{
	selectedFriend = buddiesListBox.getSelectedIndex();

	if(selectedFriend != -1)
	{
		chatButton.setEnabled(true);                                //if buddy selection is valid, chat button opens
	}
}
//==================================================================================
void processInput(DocumentEvent e)
{
	if(ID.getText().equals("") || passwordField.getText().equals(""))	//toggle login/register buttons based on input
	{
		logIn.setEnabled(false);
		register.setEnabled(false);
	}
	else																//if theres input, enable buttons
	{
		logIn.setEnabled(true);
		register.setEnabled(true);
	}
}
//==================================================================================
void openBuddies(String ownerName)
{
	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()											//CTS will open the friends panel if a login is successful
		{
			clientUser = ownerName;

			dmFrame			 = new JFrame();						//variables declared in constructor for convenience (maybe has something to do with the scope?)
			buddiesPanel	 = new JPanel();						// i honestly can't remember, but i did it for a reason
			buttonsPanel	 = new JPanel();
			buddiesList		 = new DefaultListModel<Friend>();
			buddiesListBox	 = new JList(buddiesList);
			buddiesScroller  = new JScrollPane(buddiesListBox);
			addBuddiesButton = new JButton("Add Buddy");
			chatButton = new JButton("Chat!");

			buddiesListBox.setFont(new Font("Courier",1,12));		//set up client buddies window

			dmFrame.setTitle(ownerName + "'s Chat Client");			//tell who owns which window, very helpful

			addBuddiesButton.setActionCommand("ADDFRIEND");
			addBuddiesButton.addActionListener(ChatClient.this);
			chatButton.setActionCommand("CHAT");
			chatButton.addActionListener(ChatClient.this);
			chatButton.setEnabled(false);
			buddiesListBox.addListSelectionListener(ChatClient.this);

			buddiesPanel.add(buddiesScroller);
			buttonsPanel.add(addBuddiesButton);						//add elements to frame
			buttonsPanel.add(chatButton);

			dmFrame.add(buddiesPanel,BorderLayout.CENTER);
			dmFrame.add(buttonsPanel,BorderLayout.SOUTH);

			Toolkit tk = Toolkit.getDefaultToolkit();				//set up jframe
			Dimension d = tk.getScreenSize();
			dmFrame.setSize(d.width/4,d.height/2);
			dmFrame.setLocation(d.width*3/8,d.height*1/4);			//make it look okay
			dmFrame.setVisible(true);
		}
	}
	);
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

