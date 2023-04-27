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
class LoginWindow extends JDialog implements
										ActionListener,
										DocumentListener

{
	JLabel			IDLabel;		//data members for login window
	JTextField		ID;
	JLabel			passwordLabel;
	JPasswordField	passwordField;
	JButton			register;
	JButton			logIn;
	JPanel			midPanel;
	JPanel			buttonPanel;

	String 			clientUser;

	ConnectionToServer 	clientCTS = null;

	static String loopback = "127.0.0.1";

//==================================================================================
LoginWindow()
{
	System.out.println("===CLIENT BOOT===");

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

	add(midPanel,BorderLayout.CENTER);								//set up window
	add(buttonPanel,BorderLayout.SOUTH);

	Toolkit tk = Toolkit.getDefaultToolkit();									//set up jdialog
	Dimension d = tk.getScreenSize();
	setSize(d.width/4,d.height/4);
	setLocation(d.width*3/8,d.height*3/8);
	setVisible(true);

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
}