package battleshipclient;

import java.awt.*;
import javax.swing.*;

public class ScorePanel extends JPanel {

   private int score;
   private String msg;
   private JLabel label;

   public ScorePanel() {
      score = 0;
      msg = "Your score is: ";
      label = new JLabel(msg + score, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 24));
      add(label);
   }

   public void setScore(int n) {
      score = n;
      label.setText(msg + score);
   }

   public void resetScore() {
      score = 0;
      label.setText(msg + score);
   }
}
