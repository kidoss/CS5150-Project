package view;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import controller.GameController;

public class NewGameWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static int minLength = 10, maxLength = 50, maxMarks = 25;
	private static double minWater = 1.25, maxWater = 3;
	private static double minBlockDensity = 0.1, maxBlockDensity = 0.5;
	private static double minBlockSpread = 0.1, maxBlockSpread = 0.5;
	private GameController game;
	private JTextField widthField, heightField, waterField, markField, blockDensityField, blockSpreadField, pathField;
	private JComboBox<String> visibilityField;
	
	public NewGameWindow(GameController game) {
		this.game = game;
		
		setTitle("New Game");
		setSize(600, 450);
		setLocation(0, 0);
		setFocusable(true);
		setLayout(new GridLayout(9, 2, 20, 20));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel widthLabel = new JLabel("Board Width (" + minLength + " - " + maxLength + "):", SwingConstants.RIGHT);
		widthField = new JTextField();
		widthField.setText("25");
		
		JLabel heightLabel = new JLabel("Board Height (" + minLength + " - " + maxLength + "):", SwingConstants.RIGHT);
		heightField = new JTextField();
		heightField.setText("25");
		
		JLabel markLabel = new JLabel("Number of X's (1 - " + maxMarks + "):", SwingConstants.RIGHT);
		markField = new JTextField();
		markField.setText("9");
		
		JLabel waterLabel = new JLabel("Water Progression (" + minWater + " - " + maxWater + "):", SwingConstants.RIGHT);
		waterField = new JTextField();
		waterField.setText("1.5");
		
		JLabel blockDensityLabel = new JLabel("Block Density (" + minBlockDensity + " - " + maxBlockDensity + "):", SwingConstants.RIGHT);
		blockDensityField = new JTextField();
		blockDensityField.setText("0.25");
		
		JLabel blockSpreadLabel = new JLabel("Block Spread (" + minBlockSpread + " - " + maxBlockSpread + "):", SwingConstants.RIGHT);
		blockSpreadField = new JTextField();
		blockSpreadField.setText("0.3");
		
		JLabel visibilityLabel = new JLabel("Full visibility:", SwingConstants.RIGHT);
		visibilityField = new JComboBox<String>();
		visibilityField.addItem("True");
		visibilityField.addItem("False");
		
		JLabel pathLabel = new JLabel("Neural Network Opponent Filename:", SwingConstants.RIGHT);
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
		
		add(widthLabel);
		add(widthField);
		add(heightLabel);
		add(heightField);
		add(markLabel);
		add(markField);
		add(waterLabel);
		add(waterField);
		add(blockDensityLabel);
		add(blockDensityField);
		add(blockSpreadLabel);
		add(blockSpreadField);
		add(visibilityLabel);
		add(visibilityField);
		add(pathLabel);
		add(pathField);
		add(start);
		add(cancel);
		
		setVisible(true);
	}
	
	private void createGame() {
		int width, height, marks;
		double water, density, spread;
		boolean visibility;
		File path;
		
		try {
			width = Integer.parseInt(widthField.getText());
			height = Integer.parseInt(heightField.getText());
			marks = Integer.parseInt(markField.getText());
			water = Double.parseDouble(waterField.getText());
			density = Double.parseDouble(blockDensityField.getText());
			spread = Double.parseDouble(blockSpreadField.getText());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Please enter valid numbers");
			return;
		}
		
		visibility = Boolean.parseBoolean((String) visibilityField.getSelectedItem());
		path = new File(pathField.getText() + ".nn");
		
		if(width < minLength || width > maxLength || height < minLength || height > maxLength) {
			JOptionPane.showMessageDialog(null, "Please enter valid board dimensions");
			return;
		} else if(marks < 1 || marks > maxMarks) {
			JOptionPane.showMessageDialog(null, "Please enter a valid nubmer of X's");
			return;
		} else if(water < minWater || water > maxWater) {
			JOptionPane.showMessageDialog(null, "Please enter a valid water progression value");
			return;
		} else if(density < minBlockDensity || density > maxBlockDensity) {
			JOptionPane.showMessageDialog(null, "Please enter a valid block density value");
			return;
		} else if(spread < minBlockSpread || spread > maxBlockSpread) {
			JOptionPane.showMessageDialog(null, "Please enter a valid block spread value");
			return;
		} else if(!path.exists()) {
			JOptionPane.showMessageDialog(null, "Please enter a valid neural network file");
			return;
		}
		
		game.createGame(width, height, marks, water, density, spread, visibility, path);
		dispose();
	}
}
