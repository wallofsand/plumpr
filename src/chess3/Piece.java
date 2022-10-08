package chess3;

import java.util.LinkedList;

public class Piece {

	int piece;
	LinkedList<Piece> ps;

	// Board position with origin at a1 with files counting in +x and ranks in +y
	private int square;

	// for graphics
	int xGraphic;
	int yGraphic;

	public Piece(int p, int sq) {
		this.piece = p;
		this.square = sq;
		updateGraphics();
	}

	public Piece(int p, int sq, LinkedList<Piece> ps) {
		// og constructor
		this(p, sq);
		// yes
		this.ps = ps;
		ps.add(this);
	}
/*	
	public Piece(int p, int sq, LinkedList<Piece> ps) {
		this.type = p & Chess.Type_Mask;
		this.colour = p & Chess.Colour_Mask;
		this.square = sq;
		this.rankIndex = this.square >> 3;
		this.fileIndex = this.square % 8;
		this.yGraphic = (7 - rankIndex) * 64;
		this.xGraphic = fileIndex * 64;
		this.ps = ps;
		ps.add(this);
	}
*/
	public int getSquare() {
		return square;
	}

	public static int type(int piece) {
		return piece & Chess.TYPE_MASK;
	}

	public static int colour(int piece) {
		return piece & Chess.COLOUR_MASK;
	}

	public void kill() {
//		System.out.println("Removing piece with type " + colour() + " at: " + new Coordinate(square));
		ps.remove(this);
	}

	public void updatePosition(int squareIndex) {
		this.square = squareIndex;
		updateGraphics();
	}
	
	public void updateGraphics() {
		this.yGraphic = (7 - rankIndex()) << 6;
		this.xGraphic = fileIndex() << 6;
	}

	public int rankIndex() {
		return (this.square & 0b111_000) >> 3;
	}
	
	public int fileIndex() {
		return this.square & 0b000_111;
	}

	public int colour() {
		return colour(this.piece);
	}
	
	public int type() {
		return type(this.piece);
	}
	
	/*
	 * Move function for moving pieces as a human player via a graphical interface
	 * public void move(int xpNew, int ypNew) { Piece p = ChessGame.getPiece(xp,
	 * yp); if (p != null && p != ChessGame.selectedPiece) { ChessGame.getPiece(xp,
	 * yp).kill(); } ChessGame.b.board[(7 - ypNew) * 8 + xpNew] =
	 * ChessGame.b.board[ChessGame.selectedPiece.getSquare()];
	 * ChessGame.b.board[ChessGame.selectedPiece.getSquare()] = 0; this.xp = xpNew;
	 * this.yp = ypNew; this.square = getSquare(); x = this.xp * 64; y = this.yp *
	 * 64;
	 * 
	 * ChessGame.activePlayer = ~ChessGame.activePlayer & Chess.colourMask;
	 * ChessGame.setActiveMoves(); }
	 */

}
