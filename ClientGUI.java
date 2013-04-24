import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.*;


public class ClientGUI extends JFrame
{
	public static final int tileSize = 25;
	public static final String CRLF = "\r\n";
	private OceanDisplay oceanDisplay;
	private ScorePanel scorePanel;
	private CmdPanel cmdPanel;
	private ChatPanel chatPanel;
	private String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static ArrayList<Coordinate> selfShipLocations;
	ConcurrentLinkedQueue<String> msgStack = new ConcurrentLinkedQueue<String>();

	public int firingDelay = 0;
	private long lastFiredTime;
	private boolean shipsWipedOut;
	public boolean gameStarted = false;

	OutboundListener out;

	WelcomeGui wg;

	public OceanDisplay getOceanDisplay() {
		return oceanDisplay;
	}
	public ScorePanel getScorePanel() {
		return scorePanel;
	}
	public CmdPanel getCmdPanel() {
		return cmdPanel;
	}

	public void analyzeShot( String rowColPair )
	{ 
		if(!gameStarted)
		{
			return;
		}
		
		System.out.println("Client shot called at " + rowColPair);
		
		long curTime = new Date().getTime();
		if(inbounds( rowColPair) && !shipsWipedOut && (lastFiredTime + firingDelay <= curTime))
		{
			String row = rowColPair.substring(0, 1);
			System.out.println("** " + row);
			String col = rowColPair.substring(1, rowColPair.length());
			int indexC = alpha.toUpperCase().indexOf(row);
			String nuRow = "" + alpha.charAt(indexC);
			int indexCol = Integer.parseInt(col);

			String message = "FIR " + nuRow+"&"+indexCol + CRLF;

			System.out.println("CLIENT msg: " + message);
			out.sendMsg(message);

			lastFiredTime = curTime;
		}
		else
		{
			cmdPanel.setdebuggerMessage( "unable to fire." );
		}

	}

	private boolean inbounds(String rowColPair)
	{
		int[] parsed = new int[2];
		try
		{
			parsed = OceanDisplay.translateRowAndColumn(rowColPair);
		} 
		catch (Exception exception)
		{
			return false;
		}

		
		if(parsed[0] == 0 || parsed[1] == 0)
		{
			return false;
		}
		
		if(parsed[0] < 0 || parsed[0] >= OceanDisplay.rows || parsed[1] < 0 || parsed[1] >= OceanDisplay.columns)
		{
			return false;
		}
		
		for (Coordinate c : selfShipLocations)
		{
			if(parsed[0] == c.row & parsed[1] == c.col)
			{
				return false;
			}
			if(parsed[0] == 0 || parsed[1] == 0)
			{
				return false;
			}
		}
		return true;
	}

	//Should be called once you've got a message from server
	public void placeShips( String shipLocations )
	{
		System.out.println( shipLocations );
		shipLocations = shipLocations.trim();
		String ships[] = shipLocations.split( "/" );
		for( int i = 0; i < ships.length; i++ )
		{
			String shipSections[] = ships[i].split( ":" );
			for( int j = 0; j < shipSections.length; j++ )
			{
				oceanDisplay.mapShip( shipSections[j], true );
			}
		}
	}

	//Should be called by a message from server
	public void shipHit( String location )
	{
		location = location.trim();
		oceanDisplay.mapHit( location, true );
		cmdPanel.setdebuggerMessage( "Your ship at " + location + " has been hit!" );
	}

	//Should be called by a message from server
	public void shipSunk( String location )
	{
		location = location.trim();
		oceanDisplay.mapHit( location, true );
		cmdPanel.setdebuggerMessage( "A ship of yours has been sunk at " + location );
	}

	public ClientGUI() {
		selfShipLocations = new ArrayList<Coordinate>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				out.sendMsg("BYE "+CRLF);
				try { Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		});


		setTitle("Battleship");
		scorePanel = new ScorePanel();
		add(scorePanel, BorderLayout.NORTH);
		oceanDisplay = new OceanDisplay( new MouseFireListener( this ) );
		add(oceanDisplay);
		cmdPanel = new CmdPanel(new FireListener(this));
		add(cmdPanel, BorderLayout.SOUTH);
		chatPanel = new ChatPanel(this);
		
		setSize(tileSize*OceanDisplay.columns,tileSize*OceanDisplay.rows);
		setLocationRelativeTo(null);
		setVisible(true);
		shipsWipedOut = false;

	}

	public void connectToServer(String serverName, int port) throws Exception{
		InetAddress serverIPAddr = InetAddress.getByName(serverName);
		try {
			Socket s = new Socket(serverIPAddr, port);
			s.setSoLinger(true, 180000);
			InboundListener in = new InboundListener(s, this);
			Thread t = new Thread(in);
			t.start();
			out = new OutboundListener(s);
			Thread t2 = new Thread(out);
			t2.start();

			BufferedReader br;
			br = new BufferedReader( new InputStreamReader(s.getInputStream()));

			while(true){
				String msg = br.readLine();
				msgStack.add(msg);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws Exception {
		ClientGUI cg = new ClientGUI();
		cg.connectToServer(args[0], Integer.parseInt(args[1]));
	}

	private class InboundListener implements Runnable{

		ClientGUI cgui;

		public InboundListener(Socket s, ClientGUI cg) throws Exception{		
			cgui = cg;
			System.out.println("Inbound listener starting...");
		}

		@Override
		public void run() {
			System.out.println("Inbound listener started.");

			while( true ){
				if(cgui.msgStack.size() > 0){
					System.out.println("Listening for msg...");
					String msg = cgui.msgStack.poll();
					handleMsg(msg);
				}
			}

		}

		public void handleMsg(String msg){
			System.out.println("From Server: " + msg);
			String sA[] = msg.split(" ");
			if(sA[0].equals("220")){
				//connection accepted, create the welcomeGui
				wg = new WelcomeGui(cgui);
			}else if(sA[0].equals("390")){	//rejected, game full or in progress
				//throw up a splash message.
				JOptionPane.showMessageDialog(null, "Game full or in progress.");
			}else if(sA[0].equals("351")){	//rejected, changeusername
				//throw up splash message. 
				JOptionPane.showMessageDialog(null, "Username already in use. Please enter another one.");
			}else if(sA[0].equals("310")){
				//accepted, parse and handle arguments
				cgui.wg.setVisible(false);
				String[] data = sA[1].split("&");
				int delaytime = Integer.parseInt(data[1]);
				int numShips = Integer.parseInt(data[2]);
				firingDelay = delaytime/numShips;

				//parse coordinates
				String[] coords = new String[data.length - 3];
				for(int i = 0; i < coords.length; i++){
					coords[i] = data[i+3];
				}
				for(int i=3; i< coords.length+3; i++)
				{
					oceanDisplay.mapShip(data[i] +data[i+1] ,true);
					i++;
				}

				JOptionPane.showMessageDialog(null, "Name Accepted!");

			}else if(sA[0].equals("800")){
				//game on, we can begin firing
				//also, print message to chatGUI
				gameStarted = true;
			}else if(sA[0].equals("100")){
				//miss at specified coords + curScore
				String[] data = sA[1].split("&");
				oceanDisplay.mapMiss(data[0]+data[1], true);
				scorePanel.setScore(Integer.parseInt(data[2]));

			}else if(sA[0].equals("150")){
				//hit at coords + curScore
				String[] data = sA[1].split("&");			
				oceanDisplay.mapHit(data[0]+data[1], true);
				scorePanel.setScore(Integer.parseInt(data[2]));

			}else if(sA[0].equals("151")){
				//self-hit at coords + curScore
				String[] data = sA[1].split("&");										
				oceanDisplay.mapHit(data[0]+data[1], true);
				scorePanel.setScore(Integer.parseInt(data[2]));

			}else if(sA[0].equals("190")){
				//ship sunk at coords + score

				String[] data = sA[1].split("&");			
				oceanDisplay.mapHit(data[0]+data[1], true);
				scorePanel.setScore(Integer.parseInt(data[2]));
			}else if(sA[0].equals("001")){
				//message from players
				String chat = msg.substring(msg.indexOf(" "), msg.length());
				System.out.println("CLIENT msg from server: " + chat);
				cgui.chatPanel.postRecieved(chat);
			}else if(sA[0].equals("002")){
				//broadcast msg from server
				String chat = msg.substring(msg.indexOf(" "), msg.length()-2);
				System.out.println("CLIENT msg from server: " + chat);
				cgui.chatPanel.postRecieved(chat);
			}else if(sA[0].equals("999")){
				String chat = msg.substring(msg.indexOf(" "), msg.length());
				cgui.chatPanel.postRecieved(chat);
			}else if(sA[0].equals("990")){
				JOptionPane.showMessageDialog(null, "All other players disconnected");
			}else if(sA[0].equals("900")){
				cgui.chatPanel.postRecieved("GoodBye!");
			}else if(sA[0].equals("500")){
				String[] data = sA[1].split("&");
				oceanDisplay.mapHit(data[0]+data[1], true);
			}else if(sA[0].equals("505")){
				String[] data = sA[1].split("&");
				oceanDisplay.mapHit(data[0]+data[1], true);
			}else if(sA[0].equals("555")){
				String[] data = sA[1].split("&");
				oceanDisplay.mapHit(data[0]+data[1], true);
				shipsWipedOut = true;
			}else{
				System.out.println("Unknown code from server: " + msg);
			}
		}
	}

	public class OutboundListener implements Runnable{

		DataOutputStream dos;

		public OutboundListener(Socket s) throws IOException{
			dos = new DataOutputStream(s.getOutputStream());
			System.out.println("Outbound listener starting...");
		}

		@Override
		public void run() {
			System.out.println("Outbound listener started.");
		}

		public void sendMsg(String msg){
			try {
				System.out.println("Messge sending... " + msg);
				dos.writeBytes(msg);
				System.out.println("Messge sent.");
			} catch (IOException e) {
				System.out.println("Error writing msg: " + msg);
				e.printStackTrace();
			}
		}
	}
}