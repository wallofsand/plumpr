/*
package chess3;

import java.util.ArrayDeque;

public class Board {

	public int[] board = new int[64];
	boolean gameOver;

	// Castling rights: black_white
	// queenside rook, kingside rook, king
	public int castlingRights = 0b111_111;
	// white = 0, black = 1
	public int activeColourIndex;

//	public ArrayDeque<int[]> boardHistory;
	
	public int getActiveColour() {
		return activeColourIndex << 3;
	}

	public Board() {
		this(null);
	}

	public Board(String FEN) {
		super();
//		this.boardHistory = new ArrayDeque<int[]>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				this.board[i * 8 + j] = 0;
			}
		}
		initialize(FEN);
	}

	// Method return true if there is not a piece on the given square
	public boolean isEmptySquare(int squareIndex) {
		return Piece.type(board[squareIndex]) % 7 == 0 ? true : false;
	}

	// TODO: Finish FEN reader
	private void initialize(String FEN) {
		if (FEN == null) {
			FEN = Chess.DEFAULT_FEN;
		}

		this.activeColourIndex = Chess.WHITE_INDEX;
		
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

	// TEST FUNCTION
	// prints a board from the perspective of a given player
	// takes color as input (White: 0, Black: 8)
	public void printBoard(int player) {
		Coordinate v = new Coordinate(0);
		if (player == Chess.WHITE) {
			for (int ranks = 7; ranks >= 0; ranks--) {
				for (int files = 0; files <= 7; files++) {
					v.setCoordinate(ranks, files);
					if (Coordinate.getColourIndex(v) == Chess.WHITE_INDEX) {
						System.out.print(Chess.YELLOW_BACKGROUND);
					} else if (Coordinate.getColourIndex(v) == Chess.BLACK_INDEX) {
						System.out.print(Chess.GREEN_BACKGROUND);
					}
					switch (this.board[Coordinate.getSquare(ranks, files)]) {
					case 1:
						System.out.print("P");
						break;
					case 2:
						System.out.print("N");
						break;
					case 3:
						System.out.print("B");
						break;
					case 4:
						System.out.print("R");
						break;
					case 5:
						System.out.print("Q");
						break;
					case 6:
						System.out.print("K");
						break;
					case 9:
						System.out.print("p");
						break;
					case 10:
						System.out.print("n");
						break;
					case 11:
						System.out.print("b");
						break;
					case 12:
						System.out.print("r");
						break;
					case 13:
						System.out.print("q");
						break;
					case 14:
						System.out.print("k");
						break;
					case 7:
						System.out.print("e");
						break;
					default:
						System.out.print(" ");
						break;
					}
					System.out.print(" ");
				}
				System.out.println(Chess.ANSI_RESET);
			}
		} else if (player == Chess.BLACK) {
			for (int ranks = 0; ranks <= 7; ranks++) {
				for (int files = 0; files <= 7; files++) {
					v.setCoordinate(ranks, files);
					if (Coordinate.getColourIndex(v) == Chess.WHITE_INDEX) {
						System.out.print(Chess.YELLOW_BACKGROUND);
					} else if (Coordinate.getColourIndex(v) == Chess.BLACK_INDEX) {
						System.out.print(Chess.GREEN_BACKGROUND);
					}
					switch (this.board[8 * ranks + files]) {
					case 1:
						System.out.print("P");
						break;
					case 2:
						System.out.print("N");
						break;
					case 3:
						System.out.print("B");
						break;
					case 4:
						System.out.print("R");
						break;
					case 5:
						System.out.print("Q");
						break;
					case 6:
						System.out.print("K");
						break;
					case 9:
						System.out.print("p");
						break;
					case 10:
						System.out.print("n");
						break;
					case 11:
						System.out.print("b");
						break;
					case 12:
						System.out.print("r");
						break;
					case 13:
						System.out.print("q");
						break;
					case 14:
						System.out.print("k");
						break;
					case 7:
						System.out.print("e");
						break;
					default:
						System.out.print(" ");
						break;
					}
					System.out.print(" ");
					System.out.print(Chess.ANSI_RESET);
				}
				System.out.println();
			}
		}
		System.out.println();
	}
	
	// returns true if every square on the chessboard is the same.
	// ignores epSquares
	public Boolean compare(Board b) {
		for (int i = 0; i < 64; i++) {
			if(this.board[i] != b.board[i] && this.isEmptySquare(i)==b.isEmptySquare(i)) return false; 
		}
		return true;
	}
	
}
*/