import javax.swing.*;


import java.awt.BorderLayout;
import java.awt.event.*;




class ChatPanel extends JPanel 
{
	JButton postButton;
  	JTextArea messageBuildArea;
  	JTextArea messageFeedArea;
	ClientGUI cgui;

	public ChatPanel(ClientGUI cGui)
	{
		cgui = cGui;
		postButton = new JButton("Post");
  		postButton.addActionListener(new postListener());
  		messageBuildArea =  new JTextArea(15,15);
  		
  		messageBuildArea.setEditable(true);
  		messageBuildArea.setLineWrap(true);
  		messageBuildArea.setText("Type Here!");
  		
  		messageFeedArea = new JTextArea(15,15);
  		messageFeedArea.setEditable(false);
  		messageFeedArea.setLineWrap(true);

  		
  		
  		add(messageBuildArea, BorderLayout.NORTH);
  		add(postButton, BorderLayout.CENTER);
  		add(messageFeedArea, BorderLayout.SOUTH);
  		

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
