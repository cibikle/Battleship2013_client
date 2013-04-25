package battleshipclient;

import java.awt.*;
import javax.swing.*;

public class OceanDisplay extends JPanel {

   public static final int rows = 27;
   public static final int columns = 40;
   public static final int hgap = 1;
   public static final int vgap = 1;
   private OceanTile ocean[][];
   static String rowLabels = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";

   public OceanDisplay(BattleshipClient.MouseFireListener listener) {

      
      setLayout(new GridLayout(rows, columns, hgap, vgap));
      ocean = new OceanTile[rows][columns];
      for (int i = 0; i < rows; i++) {
         for (int j = 0; j < columns; j++) {
            ocean[i][j] = new OceanTile(j, rowLabels.substring(i, i + 1));
            ocean[i][j].addMouseListener(listener);
            ocean[i][j].setBackground(Color.BLUE);
            add(ocean[i][j]);
         }
      }

      for (int i = 1; i < rows; i++) {
         String rowLabel = rowLabels.substring(i, i + 1);
         ocean[i][0].setString(rowLabel);
      }
      for (int j = 1; j < columns; j++) {
         ocean[0][j].setString(String.valueOf(j));
      }
   }

   public void mapHit(String rowCol, boolean choice) {
      System.out.println("rowCol " + rowCol);

      int[] parsed = parseRowCol(rowCol);

      System.out.println(parsed[0] + " " + parsed[1]);

      ocean[parsed[0]][parsed[1]].setHit(choice);
      repaint();
   }

   public void mapMiss(String rowCol, boolean choice) {
      int[] parsed = parseRowCol(rowCol);
      ocean[parsed[0]][parsed[1]].setMiss(choice);
      repaint();
   }

   public void mapShip(String rowCol, boolean choice) {
      int[] parsed = parseRowCol(rowCol);
      ocean[parsed[0]][parsed[1]].setShip(choice);
      repaint();
   }

   public static int[] parseRowCol(String rowCol) {
      System.out.println(rowCol);

      rowCol = rowCol.trim();
      char row = rowCol.charAt(0);
      String col = rowCol.substring(1);
      row = Character.toUpperCase(row);
      int rowIdx = rowLabels.indexOf(row); // skip the header
      int colIdx = Integer.valueOf(col); // skip the header

      int[] coords = {rowIdx, colIdx};

      return coords;
   }
}
