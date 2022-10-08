//package chess3;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Image;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedList;
//import javax.imageio.ImageIO;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JButton;
//
//// legacy class for handling graphical interface
//public class ChessGraphics {
//	Chess chess;
//	public Piece selectedPiece;
//	public LinkedList<Piece> ps;
//	public ArrayList<Move> selectedMoves = new ArrayList<Move>();
//	JPanel pn;
//	public NewPlayer computerPlayer;
//
//	ChessGraphics(Chess ch) throws IOException {
//		this.chess = ch;
//		this.ps = chess.ps;
//		this.computerPlayer = new NewPlayer(this.chess);
//		JFrame frame = new JFrame();
//		frame.setBounds(10, 10, 512, 512);
//		frame.setUndecorated(true);
//
//		BufferedImage pieces = ImageIO.read(new File("C:\\Users\\graha\\OneDrive\\Pictures\\pieces.png"));
//		Image imgs[] = new Image[12];
//		int index = 0;
//		for (int y = 0; y < 640; y += 320) {
//			for (int x = 0; x < 1920; x += 320) {
//				imgs[index] = pieces.getSubimage(x, y, 320, 320).getScaledInstance(64, 64, BufferedImage.SCALE_SMOOTH);
//				index++;
//			}
//		}
//
//		// Construct a Piece object for each piece in the board[] array
//		for (int square = 0; square < 64; square++) {
//			if (chess.b1.board[square] != 0) {
//				new Piece(chess.b1.board[square], square, ps);
//			}
//		}
//
//		JPanel pn = new JPanel() {
//			private static final long serialVersionUID = 4199591837213517399L;
//
//			@Override
//			public void paint(Graphics g) {
//				Color wSquare = new Color(0xeebfa3);
//				Color bSquare = new Color(0x744617);
//				Color wRed = new Color(0xcc3f3f);
//				Color bRed = new Color(0x881818);
//				Color blue = new Color(0x2596be);
//				Color[] wb = { wSquare, bSquare };
//				Color[] reds = { wRed, bRed };
//
//				Coordinate v = new Coordinate(0);
//				setSelectedMoves();
//
//				// if a player has lost a king, the game ends
////				if (chess.b1.kings[Chess.WhiteIndex].count() == 0 || chess.b1.kings[Chess.BlackIndex].count() == 0)
////					System.exit(0);
//
//				// Draw an 8x8 chess board
//				for (int rank = 7; rank >= 0; rank--) {
//					for (int file = 0; file < 8; file++) {
//						v.setCoordinate(rank, file);
//						g.setColor(wb[Coordinate.getColourIndex(v)]);
//						if (selectedPiece != null) {
//							if (selectedPiece.getSquare() == v.getSquare())
//								g.setColor(blue);
//						}
//						g.fillRect(file * 64, (7 - rank) * 64, 64, 64);
//						for (Move m : selectedMoves) {
//							v.setCoordinate(m.end);
//							g.setColor(reds[Coordinate.getColourIndex(v)]);
//							g.fillRect(Coordinate.fileIndex(m.end) * 64, (7 - Coordinate.rankIndex(m.end)) * 64, 64,
//									64);
//						}
//					}
//				}
//
//				// Draw pieces
//				for (Piece p : ps) {
//					if (!p.equals(selectedPiece)) {
//						p.updateGraphics();
//					}
//					int x = p.xGraphic;
//					int y = p.yGraphic;
//					int ind = 0;
//					switch (p.type()) {
//					case Chess.King:
//						ind = 0;
//						break;
//					case Chess.Queen:
//						ind = 1;
//						break;
//					case Chess.Bishop:
//						ind = 2;
//						break;
//					case Chess.Knight:
//						ind = 3;
//						break;
//					case Chess.Rook:
//						ind = 4;
//						break;
//					case Chess.Pawn:
//						ind = 5;
//						break;
//					}
//					if ((p.colour()) == Chess.Black)
//						ind += 6;
//					g.drawImage(imgs[ind], x, y, this);
//				}
//			}
//		};
//
//		JButton aiButton = new JButton("Computer Move");
//		aiButton.setBounds(522, 10, 128, 32);
//		aiButton.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				selectedPiece = null;
//				computerMove(computerPlayer.getMove());
//				selectedPiece = null;
//				frame.repaint();
//			}
//
//		});
//
//		pn.setLayout(null);
//		frame.add(pn);
////		frame.add(aiButton, BorderLayout.EAST);
//		frame.setDefaultCloseOperation(3);
//		frame.addMouseListener(new MouseListener() {
//			@Override
//			public void mouseReleased(MouseEvent e) {
//				if (e.getX() < 512 && e.getY() < 512) {
//					if (selectedPiece != null) {
//						System.out.println(tryMove(7 - (e.getY() >> 6), e.getX() >> 6) + " "
//								+ BoardRepresentation.squareNameFromCoordinate(7 - (e.getY() >> 6), e.getX() >> 6));
//						selectedPiece = null;
//					}
//					if (chess.b1.getActiveColour() == Chess.Black) {
//						computerMove(computerPlayer.getMove());
//						selectedPiece = null;
//					}
//					frame.repaint();
//				}
//			}
//
//			@Override
//			public void mouseClicked(MouseEvent e) {
//				frame.repaint();
//			}
//
//			@Override
//			public void mousePressed(MouseEvent e) {
//				if (e.getX() < 512) {
//					Piece cursorPiece = getActivePiece(e.getY(), e.getX());
//					if (selectedPiece == null) {
//						selectedPiece = cursorPiece;
//					}
//					frame.repaint();
//				}
//			}
//
//			@Override
//			public void mouseEntered(MouseEvent e) {
//
//			}
//
//			@Override
//			public void mouseExited(MouseEvent e) {
//
//			}
//		});
//		frame.addMouseMotionListener(new MouseMotionListener() {
//
//			@Override
//			public void mouseDragged(MouseEvent e) {
//				if (selectedPiece != null) {
//					selectedPiece.xGraphic = e.getX() - 32;
//					selectedPiece.yGraphic = e.getY() - 32;
//					frame.repaint();
//				}
//			}
//
//			@Override
//			public void mouseMoved(MouseEvent e) {
//
//			}
//		});
//
//		frame.setVisible(true);
//	}
//
//	public boolean computerMove(Move mv) {
//		for (Piece p : ps) {
//			int startSquare = p.getSquare();
//			if (mv.start == startSquare) {
//				selectedPiece = p;
//				tryMove(Coordinate.rankIndex(mv.end), Coordinate.fileIndex(mv.end));
//				return true;
//			}
//		}
//		return false;
//	}
//
//	public boolean tryMove(int rank, int file) {
//		setSelectedMoves();
//		for (Move m : selectedMoves) {
//			if (m.end == Coordinate.getSquare(rank, file)) {
//				Piece p = getPiece(m.end);
//				if (p != null)
//					p.kill();
//				chess.makeMove(m);
//				selectedPiece.updatePosition(m.end);
//				selectedPiece.piece = Piece.pieceType(chess.b1.board[selectedPiece.getSquare()]);
//				return true;
//			}
//		}
//		return false;
//	}
//
//	// Function to return the piece at a given rank/file coordinate
//	public Piece getPiece(int rank, int file) {
//		return getPiece(rank * 8 + file);
//	}
//
//	// Function to return the piece at a given square index
//	public Piece getPiece(int squareIndex) {
//		for (Piece p : ps) {
//			if (p.getSquare() == squareIndex) {
//				return p;
//			}
//		}
//		return null;
//	}
//
//	// Function to return the piece at a given rank/file coordinate
//	// Only returns a piece that is the colour of the active player
//	// This funtion is used for mouse input
//	public Piece getActivePiece(int yGraphic, int xGraphic) {
//		int rank = 7 - (yGraphic >> 6);
//		int file = xGraphic >> 6;
//
//		for (Piece p : ps) {
//			if (p.rankIndex() == rank && p.fileIndex() == file && p.colour() == chess.b1.getActiveColour()) {
//				return p;
//			}
//		}
//		return null;
//	}
//
//	// Method to get the possible moves of the selected Piece
//	public void setSelectedMoves() {
//		selectedMoves.clear();
//		if (selectedPiece != null && (chess.b1.board[selectedPiece.getSquare()]) != 0) {
//			MoveGenerator selectedGen = new MoveGenerator(chess.b1);
//			ArrayList<Move> selected = selectedGen.moves;
//			selected.removeIf(m -> (m.start != selectedPiece.getSquare()));
//			if (selectedPiece.colour() == chess.b1.getActiveColour())
//				selectedMoves.addAll(selected);
//		}
//	}
//
//}
