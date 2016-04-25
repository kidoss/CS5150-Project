package view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import model.game.Game;
import model.game.board.BlockType;
import model.game.player.Player;

public class GamePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static Color dark = Color.gray;
	private static Color sand = new Color(239, 228, 176);
	private static Color water = Color.blue;
	private static Color block = new Color(50, 83, 26);
	
	public void draw(Game game, boolean fullVision) {
		Graphics g = getGraphics();
		int xInt = getWidth() / game.board.size(), yInt = getHeight() / game.board.get(0).size();
		g.drawLine(xInt, yInt, 0, 0);
		
		for(int i = 0; i < game.board.size(); i++)
			for(int j = 0; j < game.board.get(i).size(); j++) {
				if(fullVision || game.players.get(0).inSight(i, j))
					switch(game.board.get(i).get(j)) {
					case WATER: g.setColor(water); break;
					case BLOCK: g.setColor(block); break;
					default: g.setColor(sand); break;
					}
				else
					g.setColor(dark);
				
				g.fillRect(i * xInt, j * yInt, xInt, yInt);
				
				if(game.board.get(i).get(j) == BlockType.MARK && (fullVision || game.players.get(0).inSight(i, j))) {
					g.setColor(Color.red);
					g.drawLine(i * xInt, j * yInt, (i + 1) * xInt, (j + 1) * yInt);
					g.drawLine(i * xInt, (j + 1) * yInt, (i + 1) * xInt, j * yInt);
				}
				
				for(Player player : game.players)
					if(player.x == i && player.y == j) {
						g.setColor(Color.green);
						int startAngle = 0, x = 0, y = 0, w = 0, h = 0;
						
						switch(player.direction) {
						case 0: 
							startAngle = 225;
							x = i * xInt;
							y = (j - 1) * yInt;
							w = xInt;
							h = 2 * yInt;
							break;
						case 1: 
							startAngle = 135; 
							x = i * xInt;
							y = j * yInt;
							w = 2 * xInt;
							h = yInt;
							break;
						case 2: 
							startAngle = 45;
							x = i * xInt;
							y = j * yInt;
							w = xInt;
							h = 2 * yInt;
							startAngle = 45; 
							break;
						default: 
							x = (i - 1) * xInt;
							y = j * yInt;
							w = 2 * xInt;
							h = yInt;
							startAngle = 315; 
							break;
						}
						
						if(player.id == 1) {
							if(fullVision || game.players.get(0).inSight(player.x, player.y))
								g.fillArc(x, y, w, h, startAngle, 90);
						} else
							g.fillArc(x, y, w, h, startAngle, 90);
					}
			}
	}
	
	public void postMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}
