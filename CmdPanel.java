import javax.swing.*;
import java.awt.event.*;




public class CmdPanel extends JPanel 
{
	public JTextField rowEntry;
	public JTextField columnEntry;
	private JButton fireBtn;
	private JTextField debuggerMessage;
	
	
	public JTextField getRowEntry() 
	{
		return rowEntry;
	}
	
	public void setRowEntry( String entry )
    {
        rowEntry.setText( entry );
    }
	
	
	public JTextField getColumnEntry() 
	{
		return columnEntry;
	}
	
	
    public void setColumnEntry( String entry )
    {
        columnEntry.setText( entry );
    }
	
	public JButton getFireBtn() 
	{
		return fireBtn;
	}
	
	private class TextListen implements MouseListener
	{
		 //this keeps up with any debugging messages need to be displayed on the screen   
		    
		    public void mousePressed( MouseEvent e ) {}
		    
		    public void mouseReleased( MouseEvent e ) {}

			public void mouseClicked(MouseEvent e) {}
			
			public void mouseExited( MouseEvent e ) {}
		    
		    public void mouseEntered( MouseEvent e ) {}
	}
	
	public CmdPanel(ActionListener btnListener)
	{
		add(new JLabel("Row"));
		rowEntry = new JTextField(2);
		add(rowEntry);
		add(new JLabel("Column"));
		columnEntry = new JTextField(2);
		add(columnEntry);
		fireBtn = new JButton("Fire!");
		fireBtn.addActionListener(btnListener);
		add(fireBtn);
		
		debuggerMessage = new JTextField("Debug info goes here");
		debuggerMessage.setEditable(false);
		
		add(debuggerMessage);
	}
	
	public void setdebuggerMessage(String message)
	{
		 debuggerMessage.setText(message);
	}
}
