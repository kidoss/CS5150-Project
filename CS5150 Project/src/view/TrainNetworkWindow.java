package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import controller.GameController;

public class TrainNetworkWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static int minGameLength = 10, maxGameLength = 100000;
	private static int minDelayLength = 5, maxDelayLength = 200;
	private static int minMovesLength = 100, maxMovesLength = 5000;
	private static int minLayersLength = 1, maxLayersLength = 5;
	private static int minLayersSize = 5, maxLayersSize = 100;
	private static double minAlpha = 0.01, maxAlpha = .3;
	private GameController game;
	private JTextField gamesField, displayField, delayField, movesField, layersField, layerSizeField, alphaField, pathField;
	
	public TrainNetworkWindow(GameController game) {
		this.game = game;
		
		setTitle("Train New Neural Network");
		setSize(600, 450);
		setLocation(0, 0);
		setFocusable(true);
		setLayout(new GridLayout(9, 2, 20, 20));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel gamesLabel = new JLabel("Number of games (" + minGameLength + " - " + maxGameLength + "):", SwingConstants.RIGHT);
		gamesField = new JTextField();
		gamesField.setText("1000");
		
		JLabel displayLabel = new JLabel("Display every Xth game (0 - All):", SwingConstants.RIGHT);
		displayField = new JTextField();
		displayField.setText("1");
		
		JLabel delayLabel = new JLabel("Display delay in ms per move (" + minDelayLength + " - " + maxDelayLength + "):", SwingConstants.RIGHT);
		delayField = new JTextField();
		delayField.setText("10");
		
		JLabel movesLabel = new JLabel("Max moves per game (" + minMovesLength + " - " + maxMovesLength + "):", SwingConstants.RIGHT);
		movesField = new JTextField();
		movesField.setText("1000");
		
		JLabel layersLabel = new JLabel("Number of hidden layers:", SwingConstants.RIGHT);
		layersField = new JTextField();
		layersField.setText("1");
		
		JLabel layerSizeLabel = new JLabel("Neurons per hidden layer:", SwingConstants.RIGHT);
		layerSizeField = new JTextField();
		layerSizeField.setText("30");
		
		JLabel alphaLabel = new JLabel("Learning Rate (" + minAlpha + " - " + maxAlpha + "):", SwingConstants.RIGHT);
		alphaField = new JTextField();
		alphaField.setText("2");
		
		JLabel pathLabel = new JLabel("Save to file:", SwingConstants.RIGHT);
		pathField = new JTextField();
		
		JButton start = new JButton("Start");
		JButton cancel = new JButton("Cancel");
		
		start.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) { 
				createGame();
			} 
		});
		
		cancel.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent event) { 
				dispose();
			} 
		});
		
		add(gamesLabel);
		add(gamesField);
		add(displayLabel);
		add(displayField);
		add(delayLabel);
		add(delayField);
		add(movesLabel);
		add(movesField);
		add(layersLabel);
		add(layersField);
		add(layerSizeLabel);
		add(layerSizeField);
		add(alphaLabel);
		add(alphaField);
		add(pathLabel);
		add(pathField);
		add(start);
		add(cancel);
		
		setVisible(true);
	}
	
	private void createGame() {
		int games, display, delay, moves, layers, layerSize;
		double alpha;
		String path;
		
		try {
			games = Integer.parseInt(gamesField.getText());
			display = Integer.parseInt(displayField.getText());
			delay = Integer.parseInt(delayField.getText());
			moves = Integer.parseInt(movesField.getText());
			layers = Integer.parseInt(layersField.getText());
			layerSize = Integer.parseInt(layerSizeField.getText());
			alpha = Double.parseDouble(alphaField.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please enter valid numbers");
			return;
		}
		path = pathField.getText();
		
		if(games < minGameLength || games > maxGameLength) {
			JOptionPane.showMessageDialog(null, "Please enter a valid number of games");
			return;
		} else if(display < 0 || display > games) {
			JOptionPane.showMessageDialog(null, "Please enter a valid number of games to display");
			return;
		} else if(delay < minDelayLength || delay > maxDelayLength) {
			JOptionPane.showMessageDialog(null, "Please enter a display delay");
			return;
		} else if(moves < minMovesLength || moves > maxMovesLength) {
			JOptionPane.showMessageDialog(null, "Please enter a number of moves cap");
			return;
		} else if(layers < minLayersLength || layers > maxLayersLength) {
			JOptionPane.showMessageDialog(null, "Please enter a valid number of layers");
			return;
		} else if(layerSize < minLayersSize || layerSize > maxLayersSize) {
			JOptionPane.showMessageDialog(null, "Please enter a valid layer size");
			return;
		} else if(alpha < minAlpha || alpha > maxAlpha) {
			JOptionPane.showMessageDialog(null, "Please enter a valid learning rate");
			return;
		} else if(new File(path).exists()) {
			JOptionPane.showMessageDialog(null, "Please enter a nonexisting filename");
			return;
		}
		
		game.trainNetwork(games, display, delay, moves, layers, layerSize, alpha, path);
		dispose();
	}
}
