package view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import controller.GameController;
import model.game.Game;
import model.game.player.Action;

//Main window of the program
//Contains a menu for creating a new game, training a new network and exiting
//Contains a GamePanel which is passed to a GameController so it can draw a game board in the window
public class MainWindow extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newGame;
	private JMenuItem trainNetwork;
	private JMenuItem exit;
	private GamePanel gamePanel;
	private GameController game;

	//Initializes everything in the window
	public MainWindow() {
		super();
		
		setTitle("Treasure Hunt");
		setSize(800, 600);
		setLocation(0, 0);
		addKeyListener(this);
		setFocusable(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		newGame = new JMenuItem("New Game");
		trainNetwork = new JMenuItem("Train New Network");
		exit = new JMenuItem("Exit");
		newGame.addActionListener(this);
		trainNetwork.addActionListener(this);
		exit.addActionListener(this);
		fileMenu.add(newGame);
		fileMenu.add(trainNetwork);
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);
		
		gamePanel = new GamePanel();
		getContentPane().add(gamePanel);
		setVisible(true);
		
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		gamePanel.draw(new Game(25, 25, 9, 2, .2, .25), true);
	}

	//Handles performed actions, specifically options selected from the menu
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		game = new GameController();
		game.setGamePanel(gamePanel);

		if(action.equals("New Game"))
			new NewGameWindow(game);
		else if(action.equals("Train New Network"))
			new TrainNetworkWindow(game);
		else
			System.exit(0);
	}

	//Not used
	public void keyPressed(KeyEvent e) {
	}

	//Passes input to the GameController when a game is active
	public void keyReleased(KeyEvent e) {
		if(game != null)
	        if(e.getKeyCode()== KeyEvent.VK_RIGHT)
	            game.setAction(Action.RIGHT);
	        else if(e.getKeyCode()== KeyEvent.VK_LEFT)
	        	game.setAction(Action.LEFT);
	        else if(e.getKeyCode()== KeyEvent.VK_DOWN)
	        	game.setAction(Action.DIG);
	        else if(e.getKeyCode()== KeyEvent.VK_UP)
	        	game.setAction(Action.MOVE);
	        else if(e.getKeyCode() == KeyEvent.VK_SPACE)
	        	game.setAction(Action.ATTACK);
	        else if(e.getKeyCode() == KeyEvent.VK_V)
	        	game.setVisibility(!game.getVisibility());
	}

	//Not used
	public void keyTyped(KeyEvent e) {
	}
}
