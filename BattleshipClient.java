//BattleshipServer.java
//C. Bikle
//COS327, Project 5: Battleship
//Spring '13
//04/24/13
package battleshipclient;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
   private ServerIntf receptionist = null;
   private Thread receptionistThread;
   private boolean gameStarted = false;
   private ClientGUI cgui;
   private ChatBox cb;
   private int actualFiringDelay;
   private boolean connectionEstablished = false;
   private String username;
   private int proposedNumPlayers;
   private String host;
   private Socket sock;
   SignInWindow siw;
   private long lastTimeFired;
   private int score = 0;

//------MAIN------//
   public static void main(String[] args) {

      BattleshipClient c = new BattleshipClient();
   }

   public BattleshipClient() {
      msgQueue = new ConcurrentLinkedQueue<ClientMessage>();
      guiInit();
      siw = new SignInWindow();
      queueHandler();
   }
   
   private void guiInit() {
      cgui = new ClientGUI(new MouseFireListener(), new FireListener());
      cgui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      cgui.addWindowListener(new ClientGUIListener());
      cb = new ChatBox(new PostListener(), cgui.getWidth());
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

            cm = msgQueue.poll();
         }

         try {
            System.out.println(Thread.currentThread().getName() + ": About to wait");
            synchronized (msgQueue) {
               msgQueue.wait();
            }
         } catch (InterruptedException ex) {
            Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   private void msgAckHandler(ClientMessage c) {
      if (c.code.equals(SYSTEM_MESSAGE)) {
         connectedPlayers = Integer.parseInt(c.args[1]);
         cb.updatePlayerCount(connectedPlayers);
      }

      String msg = c.args[0];

      System.out.println(msg);

      cb.postRecieved(msg);
   }

   private void firAckHandler(ClientMessage c) {
      String rowCol = c.args[0] + (Integer.parseInt(c.args[1]) + 1);
      if (c.code.equals(ACK_FIR_MISS)) {
         cgui.getOceanDisplay().mapMiss(rowCol, true);
      } else if (c.code.equals(ACK_FIR_SELFHIT)) {
         //no action
      } else {//hit or sunk ship--no actual differnce
         cgui.getOceanDisplay().mapHit(rowCol, true);
         score = Integer.parseInt(c.args[2]);
         cgui.getScorePanel().setScore(score);
      }
   }

   private void connectionAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_CONNECTION)) {
         System.out.println("Connection established.");
         String msg = ELO + " " + username + SEPARATOR_TOKEN + proposedNumPlayers;
         sendMsgToServer(msg);
      }
   }

   private void eloAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_ELO_ACCEPT)) {
         System.out.println("Handshake successful.");
         numberOfPlayers = Integer.parseInt(c.args[0]);
         cb.setMaxPlayers(numberOfPlayers);
         System.out.println("numberOfPlayers:"+numberOfPlayers);
         firingDelay = Integer.parseInt(c.args[1]);
         numberOfShips = Integer.parseInt(c.args[2]);
         numberOfShipsRemaining = numberOfShips;
         String ships = "";
         for (int i = 3; i < c.args.length; i++) {

            if (i % 2 == 1) {
               ships += c.args[i];
            } else {
               ships += (Integer.parseInt(c.args[i]) + 1);
               ships += ":";
            }
         }

         System.out.println("ships[" + ships + "]");
         cgui.placeShips(ships);

         connectionEstablished = true;
      } else if (c.code.equals(ACK_ELO_REJECT_NAMETAKEN)) {
         cb.postRecieved("Name taken; choose another");
         siw.setVisible(true);
      } else {//no room/game started already
         cb.postRecieved("Server full or game already in progress");
         siw.setVisible(true);
      }
   }

   private void attackedHandler(ClientMessage c) {
      String rowCol = c.args[0] + (Integer.parseInt(c.args[1]) + 1);
      if (c.code.equals(SHIPS_SUNK_ALL)) {//separate because this will stop the player shooting
         cgui.allShipsSunk();
         //stop the player from shooting
         //todo
      } else {//hit, sunk
         if (c.code.equals(SHIP_SUNK)) {
            cgui.shipSunk(rowCol);
            numberOfShipsRemaining--;
            if(numberOfShipsRemaining > 0) {
               actualFiringDelay = firingDelay / numberOfShipsRemaining;
            }
         } else {
            cgui.shipHit(rowCol);
         }
      }
   }

   private void startGameHandler(ClientMessage c) {
      gameStarted = true;
      cb.postRecieved("Game started");
      cgui.getCmdPanel().setdebuggerMessage("");
   }

   private void byeAckHandler(ClientMessage c) {
      if (c.code.equals(ACK_BYE)) {
      } else if (c.code.equals(GAMEOVER_PLAYERSLEFT)) {
         cb.postRecieved("Game over--other players all left");
        // siw.setVisible(true);
      } else {//gameover--someone won
         cb.postRecieved("Game over--"+c.args[0]);
         //siw.setVisible(true);
      }
   }

   private void handleFire(String rowColumn) {//TODO: lots of checking
      long curTime = new Date().getTime();
      if (gameStarted) {
         if (numberOfShipsRemaining > 0) {
            if (lastTimeFired + actualFiringDelay <= curTime) {
               if (inbounds(rowColumn)) {
                  sendMsgToServer(FIR + " " + rowColumn.charAt(0) + SEPARATOR_TOKEN + (Integer.parseInt(rowColumn.substring(1)) - 1));
               }
               lastTimeFired = curTime;
            } else {
               cgui.getCmdPanel().setdebuggerMessage("Firing delay in effect; please wait " + (((lastTimeFired + firingDelay) - curTime) / 1000) + " seconds.");
            }
         } else {
            cgui.getCmdPanel().setdebuggerMessage("You have no ships left with which to fire.");
         }
      } else {
         cgui.getCmdPanel().setdebuggerMessage("Game has not started");
      }

   }

   private boolean inbounds(String rowColPair) {
      int[] parsed = OceanDisplay.parseRowCol(rowColPair.replace(SEPARATOR_TOKEN, ""));

      if (parsed[0] < 0 || parsed[0] >= OceanDisplay.rows || parsed[1] < 0 || parsed[1] >= OceanDisplay.columns) {
         return false;
      }
      return true;
   }

   private void sendMsgToServer(String msg) {
      if (receptionist != null) {
         try {
            receptionist.sendMessageToServer(msg);
         } catch (IOException ex) {
            Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   private boolean establishConnection() {
      System.err.println("establishing connection");
      boolean success = false;
      try {
         sock = new Socket(host, port);

         receptionist = new ServerIntf(sock);
         receptionistThread = new Thread(receptionist);
         receptionistThread.start();

         success = true;
      } catch (ConnectException ex) {
         Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
      } catch (UnknownHostException ex) {
         Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
      } catch (IOException ex) {
         Logger.getLogger(BattleshipClient.class.getName()).log(Level.SEVERE, null, ex);
      }
      System.err.println(success);
      return success;
   }

   private void populateServerInfo(String host, int port, String nickname, int numPlayers) {
      this.host = host;
      BattleshipClient.port = port;
      this.username = nickname;
      this.proposedNumPlayers = numPlayers;
   }

//------LISTENER CLASS------//
   private final class ServerIntf implements Runnable {

      Socket socket;
      BufferedReader fromServer;
      DataOutputStream toServer;
      boolean run = true;

      public ServerIntf(Socket s) throws IOException {
         socket = s;
         fromServer = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
         toServer = new DataOutputStream(this.socket.getOutputStream());
      }

      @Override
      public void run() {
         String input;
         ClientMessage cm;
         while (run) {
            try {
               input = listenToServer().trim();

               System.out.println("input:" + input);
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
            arguments = input.substring(posOfSpace).trim();
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

//------SIGN-IN WINDOW CLASS------//
   private final class SignInWindow extends JFrame {

      int maxLenIP = 10;
      int lenPortNum = 3;
      int lenPlayers = 2;
      JTextField hostField = new JTextField("localhost", maxLenIP);
      JTextField portField = new JTextField("8060", lenPortNum);
      JTextField nicknameField = new JTextField("tim", maxLenIP);
      JTextField gamesizeField = new JTextField("3", lenPlayers);
      JLabel hostLabel = new JLabel("Server Address:");
      JLabel portLabel = new JLabel("Port Number:");
      JLabel nicknameLabel = new JLabel("Nickname:");
      JLabel gamesizeLabal = new JLabel("Number of players:");
      JButton connectButton = new JButton("connect");

      public SignInWindow() {
         setTitle("Welcome to Battleship");
         addWindowListener(new ClientGUIListener());
         add(buildPanel());

         pack();
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setAlwaysOnTop(true);
         centerOnScreen();
         setVisible(true);
      }

      private JPanel buildPanel() {
         JPanel panel = new JPanel(new GridLayout(3, 1));

         JPanel serverPanel = new JPanel();
         serverPanel.add(hostLabel);
         serverPanel.add(hostField);
         serverPanel.add(portLabel);
         serverPanel.add(portField);

         JPanel userPanel = new JPanel();
         userPanel.add(nicknameLabel);
         userPanel.add(nicknameField);
         userPanel.add(gamesizeLabal);
         userPanel.add(gamesizeField);



         panel.add(serverPanel);
         panel.add(userPanel);
         panel.add(buildButtonPanel());

         return panel;
      }

      private JPanel buildButtonPanel() {
         JPanel buttonPanel = new JPanel();
         buttonPanel.add(connectButton);
         connectButton.addActionListener(new ConnectButtonListener());

         return buttonPanel;
      }

      private void centerOnScreen() {
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Dimension screenSize = toolkit.getScreenSize();
         int x = (screenSize.width - getWidth()) / 2;
         int y = (screenSize.height - getHeight()) / 2;
         setLocation(x, y);
      }

      private class ConnectButtonListener implements ActionListener {

         @Override
         public void actionPerformed(ActionEvent ae) {
            go();
         }

         private void go() {
            System.out.println("going...");
            String h = hostField.getText();
            int port;
            try {
               port = Integer.parseInt(portField.getText());//lots of checking here, too
            } catch (NumberFormatException e) {
               port = -1;
            }
            String n = nicknameField.getText();
            int players;
            try {
               players = Integer.parseInt(gamesizeField.getText());
            } catch (NumberFormatException e) {
               players = -1;
            }

            if (players > 0 && port > 0) {
               populateServerInfo(h, port, n, players);
               if (establishConnection()) {
                  siw.setVisible(false);
               }
            }
         }
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

      @Override
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
         OceanTile clickedTile = (OceanTile) e.getSource();

         String rowColumn = "";
         rowColumn += clickedTile.getYCoordinate();
         rowColumn += clickedTile.getXCoordinate();

         handleFire(rowColumn);
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

         handleFire(rowColumn);
      }
   }

//------POST LISTENER------//
   private class PostListener implements ActionListener {

      @Override
      public void actionPerformed(ActionEvent e) {
         String input = cb.writingArea.getText();

         sendMsgToServer("MSG " + input + CRLF);

         cb.writingArea.setText("");
         cb.writingArea.repaint();
      }
   }

//------WINDOW LISTENER------//
   private class ClientGUIListener implements WindowListener {

      @Override
      public void windowOpened(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void windowClosing(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
         System.out.println("Closing down");
         sendMsgToServer(BYE);
      }

      @Override
      public void windowClosed(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void windowIconified(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void windowDeiconified(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void windowActivated(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void windowDeactivated(WindowEvent we) {
         //throw new UnsupportedOperationException("Not supported yet.");
      }
   }
}
