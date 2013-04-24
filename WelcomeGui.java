package battleshipclient;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class WelcomeGui extends JFrame {

   ClientGUI cgui;
   JTextField name;
   JTextField numPlayers;

   public WelcomeGui(ClientGUI cg) {
      cgui = cg;

      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      setTitle("Welcome");
      setSize(400, 200);
      add(buildPanel(), BorderLayout.NORTH);
      add(buildButtonPanel());
      setLocationRelativeTo(null);
      setVisible(true);
   }

   public JPanel buildPanel() {
      JPanel p = new JPanel();
      name = new JTextField("Kyte", 14);
      p.add(name);
      numPlayers = new JTextField("2");
      p.add(numPlayers);
      return p;
   }

   public JPanel buildButtonPanel() {
      JPanel p = new JPanel();

      JButton submit = new JButton("Submit");
      submit.addActionListener(new SubmitListener(cgui, this));
      p.add(submit);

      return p;
   }

   //action listener here.
   public class SubmitListener implements ActionListener {

      ClientGUI clientGUI;
      WelcomeGui wg;

      public SubmitListener(ClientGUI clientGUI, WelcomeGui wgui) {
         this.clientGUI = clientGUI;
         wg = wgui;
      }

      public void actionPerformed(ActionEvent e) {
         String name = wg.name.getText();

         if (isNumber(wg.numPlayers.getText())) {
            int num = Integer.parseInt(wg.numPlayers.getText());
            name = name.replaceAll(" ", "");


            System.out.println("Welcome GUI to server " + name + " " + num);

            /**
             * This is where you would handle the "networking" code of sending a
             * message.
             */
            String msg = "ELO " + name + "&" + num + "\r\n";
            //will send default board size, num ships = 5
            clientGUI.out.sendMsg(msg);

         } else {
            //do nothing, input invalid
         }
      }

      public boolean isNumber(String s) {
         try {
            int n = Integer.parseInt(s);
            return true;
         } catch (NumberFormatException e) {
            return false;
         }
      }
   }

   public static void main(String[] args) {
      WelcomeGui wg = new WelcomeGui(null);
   }
}
