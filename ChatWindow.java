//Name: 	Ryan DeSellems
//Course: 	Comp 2230
//Date:		3/25/2021
//Prof:		Larue

import java.io.*;   	//simple program to launch client
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.html.*;

//==================================================================================
class ChatWindow extends JDialog
									implements ActionListener,
											   DocumentListener
{
	JEditorPane			conversationPane;
	JScrollPane			conversationWindow;
	JTextField			chatInput;
	JButton				sendButton;
	JPanel				viewPanel;
	JPanel				actionPanel;
	HTMLDocument 		htmlDoc;
 	HTMLEditorKit 		editorKit;


	String				ownerName;
	String				buddyName;

	ConnectionToServer	ctsPointer;
//==================================================================================
ChatWindow()
{
}
//==================================================================================
ChatWindow(ConnectionToServer ctsIn)
{
	ctsPointer = ctsIn;								//constructor with cts for communicating to server
}
//==================================================================================
ChatWindow(ConnectionToServer ctsIn,String ownerIn,String buddyIn)
{
	ownerName = ownerIn;							//constructor with cts for communicating to server
	buddyName = buddyIn;							// also gets owner of this window and which buddy they are chatting to
	ctsPointer = ctsIn;
}
//==================================================================================
void openChat()
{
	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			conversationPane 	 = new JEditorPane("text/html",null);			//build window and activate gui components
			conversationWindow	 = new JScrollPane(conversationPane);
			chatInput			 = new JTextField(50);
			sendButton			 = new JButton("Send");
			viewPanel			 = new JPanel();
			actionPanel			 = new JPanel();

			viewPanel.add(conversationWindow);
			actionPanel.add(chatInput);
			actionPanel.add(sendButton);

			sendButton.addActionListener(ChatWindow.this);
			sendButton.setActionCommand("SEND");							//add send button that only works when input in input field
			sendButton.setEnabled(false);

			setTitle(ownerName + "'s Conversation with -> " + buddyName);	//add title

			chatInput.getDocument().addDocumentListener(ChatWindow.this);	//document listener to update text field

			chatInput.setFont(new Font("Courier",1,12));
			conversationPane.setFont(new Font("Courier",1,12));				//formatting for GUI components
			conversationPane.setEditable(false);							//prevent user from editing display
			chatInput.setSize(new Dimension(100,50));

			viewPanel.setPreferredSize(new Dimension(400,400));
			conversationPane.setPreferredSize(new Dimension(400,400));
			conversationWindow.setPreferredSize(new Dimension(400,400));	//make the chat window look nice

			add(viewPanel,BorderLayout.CENTER);
			add(actionPanel,BorderLayout.SOUTH);

			Toolkit tk = Toolkit.getDefaultToolkit();						//set up jdialog style
			Dimension d = tk.getScreenSize();
			setSize(d.width/4,d.height/4);
			setLocation(d.width*3/8,d.height*3/8);
			setVisible(true);
			pack();															//i found this line on when trying to figure out why my GUI components wouldn't format when using setSize()
		}																	//i think it has to do with borderlayout, but this seems to fix it (not sure if this is the appropriate approach for this)
	}
	);
}
//==================================================================================
public void actionPerformed(ActionEvent e)
{
	if(e.getActionCommand().equals("SEND"))
	{
		sendMessage();							//if send button is pressed send message
	}
}
//==================================================================================
public void insertUpdate(DocumentEvent e)
{
	processInput(e);							//if field type is change, update send button
}
public void removeUpdate(DocumentEvent e)
{
	processInput(e);
}
public void changedUpdate(DocumentEvent e)
{
	processInput(e);
}
//==================================================================================
void sendMessage()
{
	String temp = chatInput.getText();								//get text from text field,
	ctsPointer.sendMessage("MESSAGE " + buddyName + " " + temp);	//append who is it's going to and protocol
	addToEditor(ownerName + ": " + temp, "#FF0000");				//add this content to editorpane
	chatInput.setText("");
}
//==================================================================================
void processInput(DocumentEvent e)
{
	if(chatInput.getText().equals(""))		//toggle send button based on input
	{
		sendButton.setEnabled(false);
	}
	else									//if theres input, enable button
	{
		sendButton.setEnabled(true);
	}
}
//==================================================================================
void addToEditor(String inText, String color)
{
	SwingUtilities.invokeLater
	( new Runnable()
	{
		public void run()
		{
			try
			{
				String temp = conversationPane.getText();						//get existing content in editor pane

				htmlDoc = (HTMLDocument) conversationPane.getDocument();		//change content to html
				editorKit = (HTMLEditorKit) conversationPane.getEditorKit();	//do what an editor kit does

				editorKit.insertHTML(htmlDoc,									//insert messages with proper color
									 htmlDoc.getLength(),
									 "<font color = \"" + color + "\">" + inText + "</font>"
									 , 0, 0, null);
			}
			catch(Exception e)
			{
			}
		}
	}
	);
}
//==================================================================================
}
