package chess3;

import java.util.ArrayDeque;
import java.util.LinkedList;

public class Chess {
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
//	public Board b1;
	public ArrayDeque<Move> moveHistory;
	public int[] board;
	boolean gameOver;

	// Castling rights: black_white
	// queenside, kingside
	public int castlingRights = 0b11_11;
	// white = 0, black = 1
	public int activeColourIndex;

	// The FEN of the starting position of this game
	public String startpos;
	// The board representation of that FEN
	public int[] startBoard;
	public int startPlayerIndex;
	public int startCastle;

	Chess() {
		this(DEFAULT_FEN);
	}

	Chess(String FEN) {
		this.startpos = FEN;
		this.moveHistory = new ArrayDeque<Move>();
		this.ps = new LinkedList<Piece>();
		initialize(FEN);
		setStartingPosition();
	}

	// Method to make a move and append it to move history
	public void makeHistory(Move m) {
		if (!gameOver) {
			moveHistory.addLast(m);
//			boardHistory.addLast( board.clone());
			makeMove(m);
		}
	}

	public void unmakeMove() {
		if (moveHistory.size() > 0) {
			/* Move unmade = */ moveHistory.removeLast();
			for (int sq = 0; sq < 64; sq++) {
				board[sq] = startBoard[sq];
			}
			gameOver = false;
			activeColourIndex = startPlayerIndex;
			castlingRights = startCastle;
			for (Move m : moveHistory) {
				makeMove(m);
			}
		}
		return;
	}

	// Method to make a move without adding the move to history
	// call this method directly when stepping through move history to avoid
	// doubling
	public void makeMove(Move m) {
		int pieceToMove = board[m.start];
		int type = pieceToMove & TYPE_MASK;
		boolean enPassant = (board[m.end] == Chess.EN_PASSANT) ? true : false;
		// update the board[] array
		board[m.end] = pieceToMove;
		board[m.start] = 0;
		// clear prior en passant
		for (int sqi = 0; sqi < 64; sqi++)
			if (board[sqi] == Chess.EN_PASSANT) {
				board[sqi] = 0;
				break;
			}
		// if the move is a castle, move the rook
		if (m.isCastle == true) {
			if (m.end > m.start) { // kingside castle
				board[m.end - 1] = ROOK | getActiveColour();
				board[m.end + 1] = 0;
				// update castling rights for the rook
				castlingRights &= 3 << ((1 - activeColourIndex) << 1);
			} else { // queenside castle
				board[m.end + 1] = ROOK | getActiveColour();
				board[m.end - 2] = 0;
				// update castling rights for the rook
				castlingRights &= 3 << ((1 - activeColourIndex) << 1);
			}
		}
		// update castling rights
		else if (type == KING)
			castlingRights &= 3 << ((1 - activeColourIndex) << 1);
		else if (type == ROOK) {
			// kingside rook was moved
			if (Coordinate.fileIndex(m.start) == 7)
				castlingRights ^= castlingRights & (1 << (activeColourIndex << 1));
			// queenside rook was moved
			else if (Coordinate.fileIndex(m.start) == 0)
				castlingRights ^= castlingRights & (2 << (activeColourIndex << 1));
		}
		// handle pawn promotions and en passant capture
		// remember new en passant square
		else if (type == PAWN) {
			// remove the enemy pawn after an en passant capture
			if (enPassant)
				board[m.end - Compass.pMoves[activeColourIndex]] = 0;
			// new en passant square
			if (Math.abs(m.end - m.start) == 16) {
				board[(m.start + m.end) >>> 1] = Chess.EN_PASSANT;
			}
			// promotions
			if (m.promoteType != 0) {
				board[m.end] = getActiveColour() | m.promoteType;
			} else if ((m.end >>> 3) % 7 == 0) {
				board[m.end] = getActiveColour() | Chess.QUEEN;
			}
		}
//		(new MoveGenerator(b1)).gameOver();
		// toggle the active colour index of the Board
		activeColourIndex = 1 - activeColourIndex;
		// print castling rights for testing purposes
//		System.out.println(castlingRights);
	}

	public int getActiveColour() {
		return activeColourIndex << 3;
	}

	// Method return true if there is not a piece on the given square
	public boolean isEmptySquare(int squareIndex) {
		return Piece.type(board[squareIndex]) % 7 == 0 ? true : false;
	}

	// Method to write a move in algebraic notation before it is made
	public String moveText(Move m) {
		if (m == null)
			return null;
		String alg = new String();
		int opColour = (1 - activeColourIndex) << 3;
		boolean isCapture = !isEmptySquare(m.end) && Piece.colour(board[m.end]) == opColour;
		boolean isEnPassant = board[m.end] == Chess.EN_PASSANT;
		int type = Piece.type(board[m.start]);
		int colour = Piece.colour(board[m.start]);

		switch (type) {
		case KNIGHT:
			alg += "N";
			for (int i = 0; i < Compass.knightMoves[m.end].length; i++) {
				int checkSq = Compass.knightMoves[m.end][i];
				if (board[checkSq] == (colour | KNIGHT) && checkSq != m.start) {
					if (m.start % 8 == checkSq % 8) {
						// same file
						alg += BoardRepresentation.rankNames[m.start >>> 8];
					} else if (m.start >>> 3 == checkSq >>> 3) {
						// same rank
						alg += BoardRepresentation.fileNames[m.start % 3];
					}
				}
			}
			break;
		case BISHOP:
			alg += "B";
			for (int dirIndex = Compass.getStartIndex(BISHOP); dirIndex < Compass.getEndIndex(BISHOP); dirIndex++) {
				int dir = Compass.directions[dirIndex];
				for (int step = 0; step < Compass.numSquaresToEdge64x16[m.end][dirIndex]; step++) {
					int checkSq = m.end + step * dir;
					if (board[checkSq] == (colour | BISHOP) && checkSq != m.start) {
						// same file
						if (checkSq >>> 3 == m.start >>> 3) alg += BoardRepresentation.rankNames[m.start >>> 3];
						// same rank
					}
					else if (!isEmptySquare(checkSq)) break;
				}
			}
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
		}
		if (isCapture || isEnPassant) {
			if (type == PAWN) {
				alg += BoardRepresentation.fileNames[m.start % 8];
				alg += "x";
			} else {
				alg += "x";
			}
		}
		alg += BoardRepresentation.fileNames[m.end % 8];
		if (!isEnPassant)
			alg += BoardRepresentation.rankNames[m.end >>> 3];
		if (type == PAWN && (m.end / 8) % 7 == 0) {
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
			case 0:
				break;
			default:
				alg += "=?";
			}
		}
		return alg;
	}

	private void setStartingPosition() {
		startBoard = new int[64];
		for (int sq = 0; sq < 64; sq++) {
			startBoard[sq] = board[sq];
		}
		startPlayerIndex = activeColourIndex;
		startCastle = castlingRights;
	}

	// TODO: Finish FEN reader
	private void initialize(String FEN) {
		gameOver = false;
		if (FEN == null) {
			FEN = Chess.DEFAULT_FEN;
		}
		this.board = new int[64];
		// initialize the board array
		this.activeColourIndex = Chess.WHITE_INDEX;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				this.board[(i << 3) + j] = 0;
			}
		}

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
						this.board[square] = Chess.ROOK;
						break;
					case 'N':
						this.board[square] = Chess.KNIGHT;
						break;
					case 'B':
						this.board[square] = Chess.BISHOP;
						break;
					case 'Q':
						this.board[square] = Chess.QUEEN;
						break;
					case 'K':
						this.board[square] = Chess.KING;
						break;
					case 'P':
						this.board[square] = Chess.PAWN;
						break;
					}
					if (Character.isLowerCase(fenChar))
						this.board[square] |= Chess.BLACK;
					square++;
				} else if (fenChar == '/') {
					if (square % 8 != 0) {
						throw new NullPointerException("Invalid FEN!");
					}
					square -= 16;
				}
			} else if (field == 1) {
				if (fenChar == 'b') {
					this.activeColourIndex = Chess.BLACK_INDEX;
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
