package battleshipclient;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class ChatBox extends JFrame {

   JButton postButton;
   JTextArea writingArea;
   JTextArea readingArea;
   String defaultText = "Type Here!";
   
   JLabel playerCount;
   int maxPlayers;

   public ChatBox(ActionListener postListener, int x) {
      setLocation(x+5, 0);
      setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      //setLayout(new GridLayout(4, 1));
      
      postButton = buildPostButton(postListener);
      writingArea = buildWritingArea();
      readingArea = buildReadingArea();
      playerCount = buildPlayerCount();
      
      JPanel top = new JPanel(new BorderLayout());
      top.add(playerCount, BorderLayout.NORTH);
      top.add(readingArea, BorderLayout.CENTER);
      
      add(top, BorderLayout.NORTH);
      add(postButton, BorderLayout.CENTER);
      add(writingArea, BorderLayout.SOUTH);
      

      pack();
      setVisible(true);
   }
   
   private JLabel buildPlayerCount() {
      JLabel j = new JLabel("0/0 players connected");
      
      return j;
   }
   
   public void setMaxPlayers(int max) {
      maxPlayers = max;
   }
   
   public void updatePlayerCount(int cur) {
      playerCount.setText(cur+"/"+maxPlayers+" players connected");
      playerCount.repaint();
   }
   
   private JButton buildPostButton(ActionListener postListener) {
      JButton j = new JButton("Post");
      j.addActionListener(postListener);
      
      return j;
   }
   
   private JTextArea buildWritingArea() {
      JTextArea j = new JTextArea(10, 30);
      j.setEditable(true);
      j.setLineWrap(true);
      j.setText(defaultText);
      j.addMouseListener(new WritingAreaListener());
      
      return j;
   }
   
   private JTextArea buildReadingArea() {
      JTextArea j = new JTextArea(15, 30);
      j.setEditable(false);
      j.setLineWrap(true);
      j.setWrapStyleWord(true);
      
      return j;
   }

   public void postRecieved(String msg) {
      readingArea.append(msg + "\n");
      readingArea.repaint();
   }
   
   private class WritingAreaListener implements MouseListener {

      @Override
      public void mouseClicked(MouseEvent me) {
         //throw new UnsupportedOperationException("Not supported yet.");
         if(writingArea.getText().equals(defaultText)) {
            writingArea.setText("");
         }
      }

      @Override
      public void mousePressed(MouseEvent me) {
        // throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void mouseReleased(MouseEvent me) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void mouseEntered(MouseEvent me) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void mouseExited(MouseEvent me) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }
      
   }
}
