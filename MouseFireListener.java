import java.awt.event.*;
import java.io.IOException;

public class MouseFireListener implements MouseListener
{
    ClientGUI clientGUI;
    
    public void mouseClicked( MouseEvent e )
    {
        OceanTile clickedTile = (OceanTile) e.getSource();
        
        String rowColumn ="";
        rowColumn += clickedTile.getYCoordinate();
        rowColumn += clickedTile.getXCoordinate();
        
        clientGUI.analyzeShot( rowColumn );
    }
    
    public void mouseExited( MouseEvent e ) {}
    
    public void mouseEntered( MouseEvent e ) 
    {
        OceanTile enteredTile = (OceanTile) e.getSource();
        
        String row = enteredTile.getYCoordinate();
        String col = Integer.toString(enteredTile.getXCoordinate());
        
        
        clientGUI.getCmdPanel().setRowEntry( row );
        clientGUI.getCmdPanel().setColumnEntry( col );
    }
    
    public void mousePressed( MouseEvent e ) {}
    
    public void mouseReleased( MouseEvent e ) {}
    
    public MouseFireListener( ClientGUI clientGUI )
    {
		this.clientGUI = clientGUI;
	}
}