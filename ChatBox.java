import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class ChatBox extends JFrame
{
	
	JButton postButton;
	JTextArea messageBuildArea;
	JTextArea messageFeedArea;
	
	ClientGUI cgui;
	public ChatBox(ClientGUI cg)
	{
		cgui = cg;
		postButton = new JButton("Post");
		postButton.addActionListener(new postListener());
		messageBuildArea =  new JTextArea(15,15);
		
		messageBuildArea.setEditable(true);
		messageBuildArea.setLineWrap(true);
		messageBuildArea.setText("Type Here!");
		
		messageFeedArea = new JTextArea(15,15);
		messageFeedArea.setEditable(false);
		messageFeedArea.setLineWrap(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		add(messageBuildArea, BorderLayout.NORTH);
		add(postButton, BorderLayout.CENTER);
		add(messageFeedArea, BorderLayout.SOUTH);
		
		pack();
		setVisible(true);
	}
	
	public void postRecieved(String msg ){
		messageFeedArea.append(msg + "\n");
		messageFeedArea.repaint();
	}
	
	private class postListener implements ActionListener 
	{
    	public void actionPerformed(ActionEvent e)
    	{
    		String input = messageBuildArea.getText();
    		
    		cgui.out.sendMsg("MSG " + input + "\r\n");
    		
    		messageBuildArea.setText("");
    		messageBuildArea.repaint();
    	}
    }


}
