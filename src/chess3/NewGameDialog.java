package chess3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JRadioButton;

public class NewGameDialog extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7605046593541300340L;
	ButtonGroup opponent;
	JRadioButton white, black, random, otb, ai;
	JButton newGame;
	Chess chess;

//	static String testfen = "r3r1k1/pp3ppp/2p1b3/8/3P1P1n/1P2R3/P3NP1P/4R1K1 b - - 0 1";
	static String testfen = "rnbqkb1r/ppp2ppp/8/2PBp3/8/8/PP1P1PPP/R1BQK1NR w KQkq - 0 9";
	String fen;

	public static void main(String[] args) {
		/*
		 * System.out.print("char squareNames[64][2] = { "); for (int sq = 0; sq < 64;
		 * sq++) { String name = (new Coordinate(sq)).toString();
		 * System.out.print("{ "); System.out.print("'" + name.substring(0, 1) + "'");
		 * System.out.print(" , "); System.out.print("'" + name.substring(1, 2) + "'");
		 * System.out.print(" }, "); if (sq % 8 == 7) { System.out.println(); } }
		 * System.out.print("};");
		 */

		Book.getOpenings();
		Compass.initialize();
		Zobrist.init();
		
//		ZobristPlayer perftPlayer = new ZobristPlayer(new Chess(Chess.DEFAULT_FEN));
//		perftPlayer.perftRoot(2);
		
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new NewGameDialog(Chess.DEFAULT_FEN).setVisible(true);
			}
		});

	}

	NewGameDialog(String fen) {
		super("New Game");
		this.fen = fen;

		setDefaultCloseOperation(3);
		opponent = new ButtonGroup();
		white = new JRadioButton("White");
		opponent.add(white);
		white.setSelected(true);
		black = new JRadioButton("Black");
		opponent.add(black);
		random = new JRadioButton("Random");
		opponent.add(random);
		otb = new JRadioButton("OTB Game");
		opponent.add(otb);
		ai = new JRadioButton("AI Game");
		opponent.add(ai);
		newGame = new JButton("New Game");
		white.setBounds(80, 20, 100, 30);
		black.setBounds(80, 50, 100, 30);
		random.setBounds(80, 80, 100, 30);
		otb.setBounds(80, 110, 100, 30);
		ai.setBounds(80, 140, 100, 30);
		newGame.setBounds(80, 180, 100, 40);
		newGame.addActionListener(this);
		add(white);
		add(black);
		add(random);
		add(otb);
		add(ai);
		add(newGame);
		setSize(300, 300);
		setLayout(null);
	}

	public void actionPerformed(ActionEvent e) {
		try {
			if (ai.isSelected()) {
				sim();
			} else if (white.isSelected()) {
				startGame(1);
			} else if (black.isSelected()) {
				startGame(2);
			} else if (random.isSelected()) {
				startGame(3);
			} else startGame(0);
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void sim() throws InterruptedException {
		chess = new Chess(fen);
		new ChessFrame(chess, true, 0);
		this.setVisible(false);
	}
	
	// arg is the human player of the Chess object: 0 = both, 1 = white, 2 = black, 3 = random
	public void startGame(int arg) throws IOException {
		chess = new Chess(fen);
		this.setVisible(false);
		// Create and set up the new window.
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ChessFrame(chess, arg);
			}
		});
	}

}
