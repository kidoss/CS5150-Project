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

public class MainWindow extends JFrame implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem newGame;
	private JMenuItem trainNetwork;
	private JMenuItem exit;
	private GamePanel gamePanel;
	private GameController game;

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

	public void keyPressed(KeyEvent e) {
	}

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

	public void keyTyped(KeyEvent e) {
	}
	
	/*private void play(Action action) {
		if(game.finished)
			return;
		
		playing = true;
		boolean valid = game.play(action);
		drawGame();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(game.finished)
			return;
		
		if(valid) {
			valid = false;
			
			while(!valid)
				valid = game.play(getNeuralNetAction());
			
			drawGame();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		playing = false;
	}
	
	private Action getNeuralNetAction() {
		double[] input = game.players.get(1).getNeuralNetInput(game.board, game.players.get(0));
		int act = net.classify(input);
		
		switch(act) {
		case 0: return Action.ATTACK;
		case 1: return Action.DIG;
		case 2: return Action.LEFT;
		case 3: return Action.RIGHT;
		default: return Action.MOVE;
		}
	}
	
	private  Action getRandom() {
		double rnd = Math.random();
		Action action = Action.MOVE;
		
		if(rnd < 0.05)
			action = Action.ATTACK;
		else if(rnd < 0.1)
			action = Action.DIG;
		else if(rnd < 0.3)
			action = Action.LEFT;
		else if(rnd < 0.5)
			action = Action.RIGHT;
		
		return action;
	}*/
}
