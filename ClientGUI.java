package battleshipclient;

import java.awt.BorderLayout;
import javax.swing.*;

public class ClientGUI extends JFrame {

   public static final int tileSize = 25;
   public static final String CRLF = "\r\n";
   private OceanDisplay oceanDisplay;
   private ScorePanel scorePanel;
   private CmdPanel cmdPanel;
   private int firingDelay;
   private long lastFiredTime;
   private boolean shipsWipedOut;

   public OceanDisplay getOceanDisplay() {
      return oceanDisplay;
   }

   public ScorePanel getScorePanel() {
      return scorePanel;
   }

   public CmdPanel getCmdPanel() {
      return cmdPanel;
   }

/*   //Should be called by a message from server
//   public void setFiringDelay(String message) {
//      //message = message.trim();
//      //firingDelay = Integer.parseInt( message );
//      firingDelay = 1000;
//   }
//
//   public void callShot(String rowColPair) {
//      System.out.println("rowCol from callShot:" + rowColPair);
//
//      if (!inbounds(rowColPair)) {
//         cmdPanel.setdebuggerMessage("Coordinates out of bounds or pointed at one of your own ships");
//
//         return;
//      }
//
//      if (!shipsWipedOut) {
//         long curTime = new Date().getTime();
//         if (lastFiredTime + firingDelay <= curTime) {
//            String message = "FIR " + rowColPair + CRLF;
//            String response = "100 And this would be the response.";
//            response = response.substring(0, 3);
//            try {
//               if (Integer.parseInt(response) == 100) {
//                  oceanDisplay.mapMiss(rowColPair, true);
//                  cmdPanel.setdebuggerMessage("Miss");
//               } else if (Integer.parseInt(response) == 150 || Integer.parseInt(response) == 190) {
//                  oceanDisplay.mapHit(rowColPair, true);
//                  cmdPanel.setdebuggerMessage("Hit");
//                  scorePanel.incScore();
//               }
//            } catch (NumberFormatException nfe) {
//               System.err.println("Received the following bad response: " + response);
//               nfe.printStackTrace();
//            }
//            lastFiredTime = curTime;
//         } else {
//            cmdPanel.setdebuggerMessage("Firing delay in effect; please wait " + (((lastFiredTime + firingDelay) - curTime) / 1000) + " seconds.");
//         }
//      } else {
//         cmdPanel.setdebuggerMessage("You have no ships left to fire with.");
//      }
//
//   }*/

   //Should be called once you've got a message from server
   public void placeShips(String shipLocations) {
      System.out.println(shipLocations);
      shipLocations = shipLocations.trim();
      String ships[] = shipLocations.split("/");
      for (int i = 0; i < ships.length; i++) {
         String shipSections[] = ships[i].split(":");
         for (int j = 0; j < shipSections.length; j++) {
            oceanDisplay.mapShip(shipSections[j], true);
         }
      }
   }

   //Should be called by a message from server
   public void shipHit(String location) {
      location = location.trim();
      oceanDisplay.mapHit(location, true);
      cmdPanel.setdebuggerMessage("Your ship at " + location + " has been hit!");
   }

   //Should be called by a message from server
   public void shipSunk(String location) {
      location = location.trim();
      oceanDisplay.mapHit(location, true);
      cmdPanel.setdebuggerMessage("A ship of yours has been sunk at " + location);
   }

   //Should be called by a message from server
   public void allShipsSunk() {
      cmdPanel.setdebuggerMessage("All your ships have been sunk.");
      shipsWipedOut = true;
   }

   public ClientGUI(BattleshipClient.MouseFireListener mfl, BattleshipClient.FireListener fl) {
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle("Battleship");
      scorePanel = new ScorePanel();
      add(scorePanel, BorderLayout.NORTH);
      oceanDisplay = new OceanDisplay(mfl);
      add(oceanDisplay);
      cmdPanel = new CmdPanel(fl);
      add(cmdPanel, BorderLayout.SOUTH);
      setSize(tileSize * OceanDisplay.columns, tileSize * OceanDisplay.rows);
      setVisible(true);
      shipsWipedOut = false;

   }
}
