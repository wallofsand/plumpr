package chess3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/*
 * Class to render the chessboard
 * also handles mouse input
 */
public class GameBoard extends JPanel implements MouseListener, MouseMotionListener {
    /*
     * 
     */
    private static final long serialVersionUID = 7580846255235182438L;
    Chess chess;
    Color wSquare = new Color(0xeebfa3);
    Color bSquare = new Color(0x744617);
    Color wRed = new Color(0xcc3f3f);
    Color bRed = new Color(0x881818);
    Color bluewhite = new Color(0x24bfba);
    Color blueblack = new Color(0x2596be);
    Color[] wb = { wSquare, bSquare };
    Color[] blues = { bluewhite, blueblack };
    Color[] reds = { wRed, bRed };
    Piece selectedPiece = null;
    Image imgs[] = new Image[12];
    LinkedList<Piece> ps;
    ArrayList<Move> legalMoves;
    ArrayList<Move> selectedMoves = new ArrayList<Move>();
    ChessFrame frame;
    QuiescencePlayer computerPlayer;

    GameBoard(ChessFrame frame) {
        super();
        this.frame = frame;
        this.chess = frame.chess;
        this.ps = new LinkedList<Piece>();
        computerPlayer = new QuiescencePlayer(chess, new TranspositionTable(TranspositionTable.DEFAULT_SIZE));
        getPieceImages();
        getPieceObjects();
        if (!frame.isSim) {
            addMouseListener(this);
            addMouseMotionListener(this);
        }
    }

    // before painting:
    // get pieceObjects
    // find selectedMoves
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        int lastMoveStart = -1;
        int lastMoveEnd = -1;
        if (frame.searching) {
            selectedPiece = null;
        } else {
            if (chess.moveHistory.size() > 0) {
                lastMoveStart = chess.moveHistory.peekLast().start;
                lastMoveEnd = chess.moveHistory.peekLast().end;
            }
            if (selectedPiece != null && selectedPiece.colour() != chess.getActiveColour()) {
                selectedPiece = null;
            }
        }
        setSelectedMoves();
        // Draw the chessboard
        Coordinate v = new Coordinate(0);
        for (int ranks = 0; ranks < 8; ranks++) {
            for (int files = 0; files < 8; files++) {
                v.setCoordinate(ranks, files);
                g.setColor(wb[Coordinate.getColourIndex(v)]);
                if (lastMoveEnd == v.getSquare() || lastMoveStart == v.getSquare()) {
                    g.setColor(blues[Coordinate.getColourIndex(v)]);
                }
                if (selectedPiece != null) {
                    if (selectedPiece.getSquare() == v.getSquare()) {
                        g.setColor(blues[Coordinate.getColourIndex(v)]);
                    } else {
                        for (Move m : selectedMoves) {
                            if (m.end == v.getSquare()) {
                                g.setColor(reds[Coordinate.getColourIndex(v)]);
                            }
                        }
                    }
                }
                g.fillRect(files * 64, (7 - ranks) * 64, 64, 64);
            }
        }
        // Draw pieces
        getPieceObjects();
        for (Piece p : ps) {
            if (!p.equals(selectedPiece))
                p.updateGraphics();
            int x = p.xGraphic;
            int y = p.yGraphic;
            int ind = 0;
            switch (p.type()) {
                case Chess.KING:
                    ind = 0;
                    break;
                case Chess.QUEEN:
                    ind = 1;
                    break;
                case Chess.BISHOP:
                    ind = 2;
                    break;
                case Chess.KNIGHT:
                    ind = 3;
                    break;
                case Chess.ROOK:
                    ind = 4;
                    break;
                case Chess.PAWN:
                    ind = 5;
                    break;
            }
            if ((p.colour()) == Chess.BLACK)
                ind += 6;
            g.drawImage(imgs[ind], x, y, this);
        }

        // update zobrist hashing counters
//        frame.clashCounter.setText("Clashes: " + Zobrist.getClashes());
//        frame.hitCounter.setText(" Hits: " + Zobrist.getHits());
//        frame.zCounter.setText(" Writes: " + Zobrist.getWrites());
        frame.scoreboard.setText(String.format(" Score: %d/%d/%d", frame.wld[0], frame.wld[1], frame.wld[2]));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (chess.gameOver || frame.searching)
            return;
        if (e.getX() < 512 && e.getY() < 512) {
            if (selectedPiece == null) {
                selectedPiece = getActivePiece(e.getY(), e.getX());
                setSelectedMoves();
            } else if (selectedPiece == getActivePiece(e.getY(), e.getX())) {
                selectedPiece = null;
                setSelectedMoves();
            }
        }
        frame.updateProgress();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (chess.gameOver || frame.searching)
            return;
        if (e.getX() < 512 && e.getY() < 512) {
            if (selectedPiece != null) {
                int rank = 7 - (e.getY() >> 6);
                int file = e.getX() >> 6;
                if (selectedPiece.getSquare() != Coordinate.getSquare(rank, file)) {
                    tryMove(rank, file);
                }
            }
        }
        frame.updateProgress();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//		if (chess.b1.gameOver || isComputerTurn())
//			return;
//		if (e.getX() < 512 && e.getY() < 512) {
//			if (selectedPiece != null) {
//				int rank = 7 - (e.getY() >> 6);
//				int file = e.getX() >> 6;
//				if (selectedPiece.getSquare() != Coordinate.getSquare(rank, file)) {
//					tryMove(rank, file);
//				} else
//					selectedPiece = null;
//				repaint();
//			}
//		}
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (chess.gameOver || frame.searching)
            return;
        if (selectedPiece != null) {
            selectedPiece.xGraphic = e.getX() - 32;
            selectedPiece.yGraphic = e.getY() - 32;
            frame.updateProgress();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (chess.gameOver || frame.searching)
            return;
        if (selectedPiece != null) {
            selectedPiece.xGraphic = e.getX() - 32;
            selectedPiece.yGraphic = e.getY() - 32;
            frame.updateProgress();
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(512, 512);
    }

    private void getPieceImages() {
        try {
            BufferedImage pieces = ImageIO
                    .read(new File("C:\\Users\\graha\\OneDrive\\Documents\\Java Projects\\Chess3\\pieces\\pieces.png"));
            int index = 0;
            for (int y = 0; y < 640; y += 320) {
                for (int x = 0; x < 1920; x += 320) {
                    this.imgs[index] = pieces.getSubimage(x, y, 320, 320).getScaledInstance(64, 64,
                            BufferedImage.SCALE_SMOOTH);
                    index++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Constructs a piece object for each piece in the board[] array
    public void getPieceObjects() {
        ps.clear();
        for (int sqi = 0; sqi < 64; sqi++) {
            if (!chess.isEmptySquare(sqi)) {
                new Piece(chess.board[sqi], sqi, ps);
            } else {
                if (getPiece(sqi) != null)
                    getPiece(sqi).kill();
            }

        }
    }

    private void genMoves() {
        MoveGenerator mgen = new MoveGenerator(chess);
        this.legalMoves = mgen.moves;
    }

    private void setSelectedMoves() {
        if (selectedMoves != null)
            selectedMoves.clear();
        if (selectedPiece == null)
            return;
        genMoves();
        for (Move m : legalMoves) {
            if (m.start == selectedPiece.getSquare()) {
                selectedMoves.add(m);
            }
        }
    }

    // Method to make a move on the graphical interface
    private void tryMove(int ranks, int files) {
        if (selectedPiece != null && selectedMoves != null) {
            for (Move m : selectedMoves) {
                if (m.end == Coordinate.getSquare(ranks, files)
                        && (m.promoteType == 0 || m.promoteType == frame.getPromotion())) {
                    selectedPiece.updatePosition(m.end);
                    if (m.isCastle) {
                        if (Coordinate.fileIndex(m.end) == 2) {
                            // Queenside Castle
                            getPiece(m.end - 2).updatePosition(m.end + 1);
                        } else if (Coordinate.fileIndex(m.end) == 6) {
                            // Kingside Castle
                            getPiece(m.end + 1).updatePosition(m.end - 1);
                        }
                    }
                    if (m.promoteType != 0)
                        selectedPiece.piece = frame.getPromotion() | chess.getActiveColour();
                    if (chess.getActiveColour() == Chess.WHITE || chess.moveHistory.size() == 0) {
                        // Print the turn number
                        if (chess.getActiveColour() == Chess.WHITE)
                            frame.gameHistory.append(Integer.toString(chess.moveHistory.size() / 2 + 1) + ". ");
                        else
                            frame.gameHistory.append(Integer.toString(chess.moveHistory.size() / 2 + 1) + "... ");
                    }
                    // Print the chosen move
                    frame.gameHistory.append(chess.moveText(m));
                    if (chess.activeColourIndex == Chess.WHITE_INDEX)
                        frame.gameHistory.append(" ");
                    else
                        frame.gameHistory.append("\n");
                    frame.gameHistory.setCaretPosition(frame.gameHistory.getDocument().getLength());
                    /*
                     * chessFrame.gameHistory.append(chess.moveText(m) + " ");
                     * chessFrame.gameHistory.setCaretPosition(chessFrame.gameHistory.getDocument().
                     * getLength());
                     */
                    chess.makeHistory(m);
                    break;
                }
            }
        }
        selectedPiece = null;
        getPieceObjects();
        int go = (new MoveGenerator(chess)).gameOver();
        if (go != -1) {
            System.out.println();
            System.out.println("GAME OVER: " + go);
        }
    }

    /*
     * // Method to handle computer moves public void getResponse() throws
     * InterruptedException { Move m1 = null; // determine if it is the computer's
     * turn to move switch (chess.humanPlayers) { case 0: // no player is human m1 =
     * computerPlayer.getMove(); break; case 1: // white player is human // black
     * player is computer if (chess.b1.activeColourIndex == Chess.BlackIndex) m1 =
     * computerPlayer.getMove(); else return; break; case 2: // black player is
     * human // white player is computer if (chess.b1.activeColourIndex ==
     * Chess.WhiteIndex) m1 = computerPlayer.getMove(); else return; break; case 3:
     * // both players are human return; } if (m1 == null) return; // make the
     * computer's move getPieceObjects(); computerMove(m1); return; }
     */

    public void computerMove(Move m) {
        Piece p;
        if (m == null) {
            System.out.println((chess.getActiveColour() == Chess.WHITE) ? "White" : "Black" + " resigns!");
        }
        if (m.isEnPassant == true) {
            p = getPiece(m.end + Compass.pMoves[1 - chess.activeColourIndex]);
            if (p != null && p.colour() != chess.getActiveColour()) {
                p.kill();
            }
        } else {
            p = getPiece(m.end);
            if (p != null && p.colour() != chess.getActiveColour()) {
                p.kill();
            }
        }
        selectedPiece = getPiece(m.start);
        setSelectedMoves();
        tryMove(Coordinate.rankIndex(m.end), Coordinate.fileIndex(m.end));
        frame.updateProgress();
    }

    // Function to return the piece at a given rank/file coordinate
    // Only returns a piece that is the colour of the active player
    // This funtion is used for mouse input
    private Piece getActivePiece(int yGraphic, int xGraphic) {
        int rank = 7 - (yGraphic >> 6);
        int file = xGraphic >> 6;
        for (Piece p : ps) {
            if (p.rankIndex() == rank && p.fileIndex() == file && p.colour() == chess.getActiveColour()) {
                return p;
            }
        }
        return null;
    }

    // Function to return the piece at a given rank/file coordinate
    public Piece getPiece(int rank, int file) {
        int squareIndex = Coordinate.getSquare(rank, file);
        return getPiece(squareIndex);
    }

    // Function to return the piece at a given square
    private Piece getPiece(int squareIndex) {
        for (Piece p : ps) {
            if (p.getSquare() == squareIndex) {
                return p;
            }
        }
        return null;
    }
}
