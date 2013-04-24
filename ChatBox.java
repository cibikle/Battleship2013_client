package battleshipclient;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

public class ChatBox extends JFrame {

   JButton postButton;
   JTextArea messageBuildArea;
   JTextArea messageFeedArea;

   public ChatBox(ActionListener postListener) {
      postButton = new JButton("Post");
      postButton.addActionListener(postListener);
      messageBuildArea = new JTextArea(15, 15);

      messageBuildArea.setEditable(true);
      messageBuildArea.setLineWrap(true);
      messageBuildArea.setText("Type Here!");

      messageFeedArea = new JTextArea(15, 15);
      messageFeedArea.setEditable(false);
      messageFeedArea.setLineWrap(true);

      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


      add(messageBuildArea, BorderLayout.NORTH);
      add(postButton, BorderLayout.CENTER);
      add(messageFeedArea, BorderLayout.SOUTH);

      pack();
      setVisible(true);
   }

   public void postRecieved(String msg) {
      messageFeedArea.append(msg + "\n");
      messageFeedArea.repaint();
   }
}
