/*
package chess3;

import java.util.ArrayDeque;
import java.util.LinkedList;

public class ChessDependsBoard {
	public static final int PAWN = 0b0_001;
	public static final int KNIGHT = 0b0_010;
	public static final int BISHOP = 0b0_011;
	public static final int ROOK = 0b0_100;
	public static final int QUEEN = 0b0_101;
	public static final int KING = 0b0_110;
	public static final int EN_PASSANT = 0b0_111;
	public static final int WHITE = 0b0_000;
	public static final int BLACK = 0b1_000;
	public static final int WHITE_INDEX = 0;
	public static final int BLACK_INDEX = 1;
	public static final int COLOUR_MASK = WHITE | BLACK;
	public static final int TYPE_MASK = ~COLOUR_MASK;
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String YELLOW_BACKGROUND = "\033[43m";
	public static final String GREEN_BACKGROUND = "\033[42m";
	public static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

	public LinkedList<Piece> ps;
	public Board b1;
	public ArrayDeque<Move> moveHistory;
	public int[] board = new int[64];
	boolean gameOver;

	// Castling rights: black_white
	// queenside rook, kingside rook, king
	public int castlingRights = 0b111_111;
	// white = 0, black = 1
	public int activeColourIndex;
	
	// The FEN of the starting position of this game
	public String startpos;

	// 0 = none, 1 = white, 2 = black, 3 = both
	public int humanPlayers;

	ChessDependsBoard() {
		this(DEFAULT_FEN);
	}

	ChessDependsBoard(String FEN) {
		this.startpos = FEN;
		this.b1 = new Board(FEN);
		this.moveHistory = new ArrayDeque<Move>();
		this.ps = new LinkedList<Piece>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				this.board[i * 8 + j] = 0;
			}
		}
		initialize(FEN);
	}

	// Method to make a move and append it to move history
	public void makeHistory(Move m) {
		if (!b1.gameOver) {
			moveHistory.addLast(m);
//			b1.boardHistory.addLast(b1.board.clone());
			makeMove(m);
		}
	}

	public void unmakeMove() {
		if (moveHistory.size() != 0) {
			moveHistory.removeLast();
//			b1.boardHistory.removeLast();
//			bh = b1.boardHistory.clone();
			this.b1 = new Board(startpos);
//			b1.boardHistory = bh.clone();
			for (Move m : moveHistory) {
				makeMove(m);
			}
		}
	}

	// Method to make a move without adding the move to history
	// call this method directly when stepping through move history to avoid
	// doubling
	public void makeMove(Move m) {
		int pieceToMove = b1.board[m.start];
		int type = pieceToMove & TYPE_MASK;
		boolean enPassant = (b1.board[m.end] == ChessDependsBoard.EN_PASSANT) ? true : false;
		// update the board[] array
		b1.board[m.end] = pieceToMove;
		b1.board[m.start] = 0;
		// if the move is a castle, move the rook
		if (m.isCastle == true) {
			if (m.end > m.start) { // kingside castle
				b1.board[m.end - 1] = ROOK | b1.getActiveColour();
				b1.board[m.end + 1] = 0;
				// update castling rights for the rook
				b1.castlingRights ^= 0b010 << (3 * b1.activeColourIndex);
			} else { // queenside castle
				b1.board[m.end + 1] = ROOK | b1.getActiveColour();
				b1.board[m.end - 2] = 0;
				// update castling rights for the rook
				b1.castlingRights ^= 0b100 << (3 * b1.activeColourIndex);
			}
		}
		// clear prior en passant
		for (int sqi = 0; sqi < 64; sqi++)
			if (b1.board[sqi] == ChessDependsBoard.EN_PASSANT)
				b1.board[sqi] = 0;
		// update castling rights
		if (type == KING && (b1.castlingRights & (1 << (3 * b1.activeColourIndex))) != 0)
			b1.castlingRights ^= 0b001 << (3 * b1.activeColourIndex);
		if (type == ROOK && (b1.castlingRights & (5 << (3 * b1.activeColourIndex))) != 0) {
			if (Coordinate.fileIndex(m.start) == 7
					&& (b1.castlingRights & (0b010 << (3 * b1.activeColourIndex))) != 0) {
				// kingside rook was moved
				b1.castlingRights ^= 0b010 << (3 * b1.activeColourIndex);
			} else if (Coordinate.fileIndex(m.start) == 0
					&& (b1.castlingRights & (0b100 << (3 * b1.activeColourIndex))) != 0) {
				// queenside rook was moved
				b1.castlingRights ^= 0b100 << (3 * b1.activeColourIndex);
			}
		}
		// handle pawn promotions and en passant capture and remember new en passant
		// square
		if (type == PAWN) {
			// en passant capture
			if (enPassant)
				b1.board[m.end - Compass.pMoves[b1.activeColourIndex]] = 0;
			// new en passant square
			if (Math.abs(m.end - m.start) == 16) {
				b1.board[(m.start + m.end) / 2] = ChessDependsBoard.EN_PASSANT;
			}
			// promotions
			if (m.promoteType != 0) {
				b1.board[m.end] = b1.getActiveColour() | m.promoteType;
			}
		}
//		(new MoveGenerator(b1)).gameOver();
		// toggle the active colour index of the Board
		b1.activeColourIndex = 1 - b1.activeColourIndex;
		// print castling rights for testing purposes
		// Bitboard.printBinaryString((long) b1.castlingRights);
	}

	public int getActiveColour() {
		return activeColourIndex << 3;
	}
	
	// Method to write a move in algebraic notation before it is made
	public String moveText(Move m) {
		if (m == null)
			return null;
		String alg = new String();
		int opColour = 8 * (1 - b1.activeColourIndex);
		boolean isCapture = !b1.isEmptySquare(m.end) && Piece.colour(b1.board[m.end]) == opColour;
		boolean isEnPassant = b1.board[m.end] == ChessDependsBoard.EN_PASSANT;
		int type = Piece.type(b1.board[m.start]);
		int colour = Piece.colour(b1.board[m.start]);

		switch (type) {
		case KNIGHT:
			alg += "N";
			for (int i = 0; i < Compass.knightMoves[m.end].length; i++) {
				int check = Compass.knightMoves[m.end][i];
				if (b1.board[check] == (colour | KNIGHT)) {
					if (Compass.fileNames[m.start % 8] != Compass.fileNames[check % 8]) {
						alg += Compass.fileNames[m.start % 8];
					} else if (Compass.rankNames[m.start >>> 3] != Compass.rankNames[check >>> 3]) {
						alg += Compass.rankNames[m.start >>> 3];
					}
				}
			}
			break;
		case BISHOP:
			alg += "B";
			break;
		case ROOK:
			alg += "R";
			break;
		case QUEEN:
			alg += "Q";
			break;
		case KING:
			if (m.isCastle) {
				if (m.end - 2 == m.start) {
					// Kingside Castle
					alg = "O-O";
				} else {
					// Queenside Castle
					alg = "O-O-O";
				}
				return alg;
			} else
				alg += "K";
			break;
		default:
		}
		if (isCapture || isEnPassant) {
			if (type == PAWN) {
				alg += Compass.fileNames[m.start % 8];
				alg += "x";
			} else {
				alg += "x";
			}
		}
		alg += Compass.fileNames[m.end % 8];
		if (!isEnPassant)
			alg += Compass.rankNames[m.end >>> 3];
		if (type == PAWN && m.end >>> 3 == 8) {
			switch (m.promoteType) {
			case KNIGHT:
				alg += "=N";
				break;
			case BISHOP:
				alg += "=B";
				break;
			case ROOK:
				alg += "=R";
				break;
			case QUEEN:
				alg += "=Q";
				break;
			default:
				alg += "=?";
			}
		}
		return alg;
	}
	
	// TODO: Finish FEN reader
	private void initialize(String FEN) {
		if (FEN == null) {
			FEN = ChessDependsBoard.DEFAULT_FEN;
		}
		this.activeColourIndex = ChessDependsBoard.WHITE_INDEX;
		// Constructor to setup a chess board using a given FEN code
		// FEN has 6 fields:
		// field 0: piece placement
		// field 1: active color
		// field 2: castling availability (or -)
		// field 3: En passant (or -)
		// field 4: Halfmove clock
		// field 5: Fullmove clock
		char arr[] = FEN.toCharArray();
		int field = 0;
		int square = 56;
		for (int i = 0; i < arr.length; i++) {
			char fenChar = arr[i];
			if (field == 0) {
				if (fenChar == ' ') {
					if (square == 8)
						field++;
					else {
						throw new IllegalArgumentException("Invalid FEN!");
					}
				} else if (Character.isDigit(fenChar)) {
					square += Character.getNumericValue(fenChar);
				} else if (Character.isLetter(fenChar)) {
					switch (Character.toUpperCase(fenChar)) {
					case 'R':
						this.board[square] = ChessDependsBoard.ROOK;
						break;
					case 'N':
						this.board[square] = ChessDependsBoard.KNIGHT;
						break;
					case 'B':
						this.board[square] = ChessDependsBoard.BISHOP;
						break;
					case 'Q':
						this.board[square] = ChessDependsBoard.QUEEN;
						break;
					case 'K':
						this.board[square] = ChessDependsBoard.KING;
						break;
					case 'P':
						this.board[square] = ChessDependsBoard.PAWN;
						break;
					}
					if (Character.isLowerCase(fenChar))
						this.board[square] |= ChessDependsBoard.BLACK;
					square++;
				} else if (fenChar == '/') {
					if (square % 8 != 0) {
						throw new NullPointerException("Invalid FEN!");
					}
					square -= 16;
				}
			} else if (field == 1) {
				if (fenChar == 'b') {
					this.activeColourIndex = ChessDependsBoard.BLACK_INDEX;
				} else if (fenChar == ' ')
					field++;
			} else if (field == 2) {
				if (fenChar == ' ')
					field++;
			} else if (field == 3) {
				if (fenChar == ' ')
					field++;
				else if (fenChar != '-' && !Character.isDigit(fenChar) && arr.length > i + 1) {
//						EnPassant.setCoordinate(Character.toString(fenChar) + Character.toString(arr[i + 1]));
				}
			} else if (field == 4) {
				if (fenChar == ' ')
					field++;

			} else if (field == 5) {
//					this.turnCounter = fenChar;
			}
		}
	}

}
*/