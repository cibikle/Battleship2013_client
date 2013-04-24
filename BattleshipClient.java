//BattleshipServer.java
//C. Bikle
//COS327, Project 5: Battleship
//Spring '13
//04/24/13
package battleshipclient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BattleshipClient {
   /* CODES */
   /* client to server */

   public static final String ELO = "ELO";
   public static final String MSG = "MSG";
   public static final String FIR = "FIR";
   public static final String BYE = "BYE";
   /* server to client */
   public static final String PLAYER_MESSAGE = "001";
   public static final String SYSTEM_MESSAGE = "002";
   public static final String ACK_FIR_MISS = "100";
   public static final String ACK_FIR_HIT = "150";
   public static final String ACK_FIR_SELFHIT = "151";
   public static final String ACK_FIR_SUNK = "190";
   public static final String ACK_CONNECTION = "220";
   public static final String ACK_ELO_ACCEPT = "310";
   public static final String ACK_ELO_REJECT_NAMETAKEN = "351";
   public static final String ACK_ELO_REJECT_NOROOM = "390";
   public static final String SHIP_HIT = "500";
   public static final String SHIP_SUNK = "505";
   public static final String SHIPS_SUNK_ALL = "555";
   public static final String START_GAME = "800";
   public static final String ACK_BYE = "900";
   public static final String GAMEOVER_PLAYERSLEFT = "990";
   public static final String GAMEOVER_WON = "999";
   public static final String CRLF = "\r\n";
   public static final String PROTOTCOL_VERSION = "bsP/2013";
   public static final int lengthOfCmds = 3;
   /* members */
   private static final String SEPARATOR_TOKEN = "&";
   private static final String TEMP_TOKEN = "#";
   private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   private static final int MAP_WIDTH = 39;//number of columns
   private static final int MAP_HEIGHT = 26;//number of rows
   private int firingDelay;//in millis, so 5 seconds
   private static int port = 8060;
   private int numberOfPlayers;
   private int connectedPlayers;
   private int numberOfShips;
   private int numberOfShipsRemaining;
   private final ConcurrentLinkedQueue<ClientMessage> msgQueue;
   private Listener receptionist;
   private Thread receptionistThread;
   private boolean gameStarted = false;
   private ClientGUI cgui;
   private ChatBox cb;
   private int defactoFireDelay;
   private boolean connectionEstablished = false;
   private String username;
   private int proposedNumPlayers;

//------MAIN------//
   public static void main(String[] args) {
   }

   public BattleshipClient() {
      msgQueue = new ConcurrentLinkedQueue<ClientMessage>();
      cgui = new ClientGUI(new MouseFireListener(), new FireListener());
      cb = new ChatBox(new PostListener());
   }

//------QUEUE-HANDLER------//
   private void queueHandler() {
      while (true) {
         ClientMessage cm = msgQueue.poll();
         while (cm != null) {
            if (cm.code.startsWith("" + PLAYER_MESSAGE.charAt(0))) {//MSGs
               msgAckHandler(cm);
            } else if (cm.code.startsWith("" + ACK_FIR_HIT.charAt(0))) {//FIRs
               firAckHandler(cm);
            } else if (cm.code.startsWith("" + ACK_CONNECTION.charAt(0))) {//connection
               connectionAckHandler(cm);
            } else if (cm.code.startsWith("" + ACK_ELO_ACCEPT.charAt(0))) {//ELOs
               eloAckHandler(cm);
            } else if (cm.code.startsWith("" + SHIP_HIT.charAt(0))) {//SHIP attacked
               attackedHandler(cm);
            } else if (cm.code.startsWith("" + START_GAME.charAt(0))) {//Start game
               startGameHandler(cm);
            } else if (cm.code.startsWith("" + ACK_BYE.charAt(0))) {//BYEs
               byeAckHandler(cm);
            } else {//Uh-oh
               //error 
               System.out.println("error, invalid:" + cm.code);
            }
         }
      }
   }

   private void msgAckHandler(ClientMessage c) {
      if (c.code.equals(SYSTEM_MESSAGE)) {
         connectedPlayers = Integer.parseInt(c.args[1]);
      }

      String msg = c.args[0];

      System.out.println(msg);

      cb.postRecieved(msg);
   }

   private void firAckHandler(ClientMessage c) {
      String rowCol = c.args[0] + c.args[1];
      if (c.code.equals(ACK_FIR_MISS)) {
         cgui.getOceanDisplay().mapMiss(rowCol, true);
      } else if (c.code.equals(ACK_FIR_SELFHIT)) {
         //no action
      } else {//hit or sunk ship--no actual differnce
         cgui.getOceanDisplay().mapHit(rowCol, true);
      }
   }

   private void connectionAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_CONNECTION)) {
         System.out.println("Connection established.");
         String msg = ELO + " " + username + SEPARATOR_TOKEN + proposedNumPlayers;
         try {
            receptionist.sendMessageToServer(msg);
            System.out.println("Handshake dispatched.");
         } catch (IOException ex) {
            Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   private void eloAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_ELO_ACCEPT)) {
         System.out.println("Handshake successful.");
         numberOfPlayers = Integer.parseInt(c.args[0]);
         firingDelay = Integer.parseInt(c.args[1]);
         numberOfShips = Integer.parseInt(c.args[2]);
         numberOfShipsRemaining = numberOfShips;
         String ships = "";
         for (int i = 3; i < c.args.length; i++) {
            ships += c.args[i];
            if (i % 2 == 1) {
               ships += ":";
            } else {
               ships += "/";
            }
         }
         cgui.placeShips(ships);
      } else if (c.code.equals(ACK_ELO_REJECT_NAMETAKEN)) {
//prompt the user for a new name
      } else {//no room/game started already
//inform the user
      }
   }

   private void attackedHandler(ClientMessage c) {
      String rowCol = c.args[0] + c.args[1];
      if (c.code.equals(SHIPS_SUNK_ALL)) {//separate because this will stop the player shooting
         cgui.getOceanDisplay().mapHit(rowCol, true);
         //stop the player from shooting
         //todo
      } else {//hit, sunk
         cgui.getOceanDisplay().mapHit(rowCol, true);

         if (c.code.equals(SHIP_SUNK)) {
            numberOfShipsRemaining--;
         }
      }
   }

   private void startGameHandler(ClientMessage c) {
   }

   private void byeAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_BYE)) {
      } else if (c.code.equals(GAMEOVER_PLAYERSLEFT)) {
      } else {//gameover--someone won
      }
   }

   private void handleFire(String rowColumn) {
   }

   private void sendMsgToServer(String msg) {
      try {
         receptionist.sendMessageToServer(msg);
      } catch (IOException ex) {
         Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

//------LISTENER CLASS------//
   private final class Listener implements Runnable {

      Socket socket;
      BufferedReader fromServer;
      DataOutputStream toServer;
      boolean run = true;

      @Override
      public void run() {
         String input;
         ClientMessage cm;
         while (run) {
            try {
               input = listenToServer().trim();
            } catch (IOException ex) {
               Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
               input = "";
            }

            cm = parseInput(input);
            if (cm != null) {
               addClientMessageToQueue(cm);
            }
         }

      }

      private ClientMessage parseInput(String input) {
         int posOfSpace = input.indexOf(" ");
         String code;
         String arguments = "";
         if (posOfSpace == lengthOfCmds) {
            code = input.substring(0, posOfSpace);
            arguments = input.substring(posOfSpace);
         } else {
            code = input;
         }

         String[] args;
         if (!arguments.equals("")) {
            arguments = arguments.replace("\\" + SEPARATOR_TOKEN, "\\" + TEMP_TOKEN);
            args = arguments.split(SEPARATOR_TOKEN);
            for (int i = 0; i < args.length; i++) {
               args[i] = args[i].replace("\\" + TEMP_TOKEN, SEPARATOR_TOKEN);
            }
         } else {
            args = null;
         }

         return new ClientMessage(code, args);
      }

      private void addClientMessageToQueue(ClientMessage cm) {
         msgQueue.add(cm);
         synchronized (msgQueue) {
            msgQueue.notify();
         }
      }

      public void sendMessageToServer(String msg) throws IOException {
         //System.out.println("msg:"+msg);
         if (!msg.endsWith(CRLF)) {
            msg += CRLF;
         }
         if (!socket.isClosed()) {
            toServer.writeBytes(msg);
         }
      }

      public String listenToServer() throws IOException {
         if (!socket.isClosed()) {
            return fromServer.readLine();
         }
         return null;
      }
   }

//------CLIENT MESSAGE CLASS------//
   private final class ClientMessage {

      String code;
      String[] args;

      public ClientMessage(String code, String[] args) {
         this.code = code;
         this.args = args;
      }

      public String toString() {
         String x = code;
         for (String s : args) {
            x += " " + s;
         }
         return x;
      }
   }

//------MOUSE FIRE LISTENER------//
   public class MouseFireListener implements MouseListener {

      @Override
      public void mouseClicked(MouseEvent e) {
         OceanTile clickedTile = (OceanTile) e.getSource();

         String rowColumn = "";
         rowColumn += clickedTile.getYCoordinate();
         rowColumn += SEPARATOR_TOKEN;
         rowColumn += clickedTile.getXCoordinate();

         handleFire(rowColumn);
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
         OceanTile enteredTile = (OceanTile) e.getSource();

         String row = enteredTile.getYCoordinate();
         String col = Integer.toString(enteredTile.getXCoordinate());


         cgui.getCmdPanel().setRowEntry(row);
         cgui.getCmdPanel().setColumnEntry(col);
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      public MouseFireListener() {
      }
   }

//------FIRE LISTENER------//
   public class FireListener implements ActionListener {

      public FireListener() {
      }

      @Override
      public void actionPerformed(ActionEvent e) {

         String row = cgui.getCmdPanel().getRowEntry().getText();
         row = row.trim();


         String column = cgui.getCmdPanel().getColumnEntry().getText();
         column = column.trim();

         String rowColumn = row + column;

         //clientGUI.analyzeShot(rowColumn);
         //TODO: replace with BSC-scope method
      }
   }

//------POST LISTENER------//
   private class PostListener implements ActionListener {

      @Override
      public void actionPerformed(ActionEvent e) {
         String input = cb.messageBuildArea.getText();

         sendMsgToServer("MSG " + input + CRLF);

         cb.messageBuildArea.setText("");
         cb.messageBuildArea.repaint();
      }
   }
}
