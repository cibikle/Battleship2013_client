package battleshipclient;

import java.awt.*;
import javax.swing.*;

public class OceanTile extends JPanel {

   String s;
   boolean hit;
   boolean miss;
   boolean ship;
   int xCoordinate;
   String yCoordinate;

   public OceanTile(String s) {
      super();
      this.s = s;
   }

   public OceanTile(int x, String y) {
      this.s = null;
      hit = false;
      miss = false;
      ship = false;
      xCoordinate = x;
      yCoordinate = y;
   }

   @Override
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (s != null) {
         g.setFont(new Font("SansSerif", Font.BOLD, 14));
         g.setColor(Color.white);
         g.drawString(s, 5, 15);
      }
      if (ship) {
         g.setColor(Color.GRAY);
         Dimension dim = getSize();
         g.fillRect(0, 0, dim.width, dim.height);
      }
      if (hit) {
         g.setColor(Color.RED);
         g.fillArc(5, 5, 15, 15, 0, 360);
      }
      if (miss) {
         g.setColor(Color.WHITE);
         g.fillArc(5, 5, 15, 15, 0, 360);
      }
   }

   public void setHit(boolean hit) {
      this.hit = hit;
   }

   public void setMiss(boolean miss) {
      this.miss = miss;
   }

   public void setString(String s) {
      this.s = s;
   }

   public void setShip(boolean ship) {
      this.ship = ship;
   }

   @Override
   public Dimension getPreferredSize() {
      return new Dimension(200, 200);
   }

   public int getXCoordinate() {
      return this.xCoordinate;
   }

   public String getYCoordinate() {
      return this.yCoordinate;
   }
}
