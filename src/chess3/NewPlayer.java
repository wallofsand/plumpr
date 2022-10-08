package chess3;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class NewPlayer extends Player {
	private Chess ch;
	// separate Chess object used for thinking about moves
	private Chess test;
	Random rando = new Random();
	final int MOD = +3;
	final float MAX = 99.99f;
	final float MIN = -MAX;
	final float mateinzero = 60f;
	private float[] pieceValue = { 0f, 1f, 2.8f, 3f, 5f, 9f, 0f, 0f };
	long nodes = 0;
	boolean outofbook = false;
	
	float mobWeight = 0.1f;

	// 1. d4 d5 2. c4 e6 3. Nc3 Nf6 4. Bg5 Be7 5. e3 O-O 6. Nf3 Nfd7 7. Bxe7 Qxe7 8.
	// Qa4 Nb6 9. Qa5 Nxc4 10. Qb5 a6 11. Qb3 Nc6 12. O-O-O N4a5 13. Qa4 Qf6 14. Kd2
	// Bd7 15. Qa3 Nxd4 16. exd4 Qf4 17. Ke2 Nc4 18. Qe7 Nxb2 19. Re1 Kh8 20. Qxd7
	// Kg8 21. Qe7 Kh8 22. Nxd5 exd5 23. Qd7 Qe4 24. Kd2 Qf4 25. Kc2 Rd8 26. Qxd8
	// Rxd8 27. Kxb2 Kg8 28. Bxa6 bxa6 29. Kb3 Qf6 30. Kc3 Qf4 31. Kd3 Qf5 32. Kc3
	// Qf4 33. Kd3 Qxf3 34. gxf3 Rf8 35. Kd2 Rd8 36. Kd3 Rf8 37. Kd2 Kh8 38. Kd3 Rg8
	// 39. Kd2 Rf8 40. Ke2 Kg8 41. Ke3 Re8 42. Kd3 Kf8 43. Rg1 Rc8 44. f4 c5 45. Rc1
	// Kg8 46. Rxc5 Kf8 47. Rxc8 Ke7 48. Rg1 Kd7 49. Rc5 g6 50. Rxd5 Kc7 51. Rc5 Kb8
	// 52. h3 h5 53. Rb1 Ka7 54. Rxh5 g5 55. fxg5 a5 56. Ke4 a4 57. Ke5 Ka6 58. Rh7
	// Ka5 59. Rxf7 Ka6 60. Ke6 a3 61. Kf6 Ka5 62. Kg6 Ka6 63. Kg7 Ka5 64. Kg8 Ka4
	// 65. Kh8 Ka5 66. Kh7 Ka6 67. Kh8 Ka5 68. Kh7 Ka4 69. Kh8 Ka5

	public NewPlayer(Chess chess) {
		super(chess);
		this.ch = chess;
	}

	public void setChess(Chess chessIn) {
		this.ch = chessIn;
	}

	@Override
	public Move getMove() throws InterruptedException {
		return getMove(0);
	}

	long startTime, time;

	@Override
	public Move getMove(int depth) throws InterruptedException {
//		if ((new MoveGenerator(ch)).gameOver() != -1)
//			return null;
		if (!outofbook) {
			startTime = System.currentTimeMillis();
			System.out.println("Searching opening book . . . ");
			Move bookMove = getBookMove(Book.readABook(ch));
//			ArrayList<Move> bookMoves = Book.readABook(ch);
			if (bookMove != null) {
				System.out.println("Book move found in " + /* (int) */ (System.currentTimeMillis() - startTime) + "ms! "
						+ ch.moveText(bookMove));
				return bookMove;
			} else {
				System.out.println("Uh oh -- I'm out of my book now . . . in " + time + "ms!");
				outofbook = true;
			}
		}
		if (depth > 0)
			return rootNegaMax(depth, MIN, MAX);
		int count = countPieces(ch);
		if (count < 5)
			return rootNegaMax(4 + MOD, MIN, MAX);
		else if (count < 10)
			return rootNegaMax(3 + MOD, MIN, MAX);
		else if (count < 20)
			return rootNegaMax(2 + MOD, MIN, MAX);
		else
			return rootNegaMax(0 + MOD, MIN, MAX);
	}

	// Method to convert relevant book data into a move
	// ArrayList bookData contains 3 arrays
	// 0 - ECO codes, 1 - Opening names, 2 - move strings
	private Move getBookMove(ArrayList<ArrayList<String>> bookData) {
		if (bookData == null || bookData.get(2).size() == 0)
			return null;
		ArrayList<Move> bookMoves = new ArrayList<Move>();
		for (int i = 0; i < bookData.get(2).size(); i++) {
			String nextMoveString = bookData.get(2).get(i);
			Move nextMove = null;
			int start = -1;
			int astart = -1;
			// find the square the book move points too
			int end = (new Coordinate((nextMoveString.charAt(nextMoveString.length() - 1) == '#'
					|| nextMoveString.charAt(nextMoveString.length() - 1) == '+')
							? nextMoveString.substring(nextMoveString.length() - 3)
							: nextMoveString.substring(nextMoveString.length() - 2)))
					.getSquare();
			// Find the starting square for the move
			// Look at the board and deduce which piece could make the move
			// TODO: ambigous moves confuse the algorithm
			// UPDATE: should be resolved
			switch (nextMoveString.charAt(0)) {
			case 'N': // knight
				for (int dirIndex = Compass.getStartIndex(Chess.KNIGHT); dirIndex < Compass
						.getEndIndex(Chess.KNIGHT); dirIndex++) {
					if (Compass.numSquaresToEdge64x16[end][dirIndex] > 0) {
						if (ch.board[end + Compass.directions[dirIndex]] == (Chess.KNIGHT | ch.getActiveColour())) {
							if (start == -1)
								start = end + Compass.directions[dirIndex];
							else
								astart = end + Compass.directions[dirIndex];
							break;
						}
					}
				}
				// validate ambiguity
				if (astart != -1) {
					if (Character.isDigit(nextMoveString.charAt(1))) {
						if ((new Coordinate(
								new String(new char[] { nextMoveString.charAt(2), nextMoveString.charAt(1) })))
								.getSquare() == astart) {
							start = astart;
						}
					} else {
						if (Character.isDigit(nextMoveString.charAt(2))) {
							start = (new Coordinate(nextMoveString.substring(1, 3))).getSquare();
						}
					}
				}
				nextMove = new Move(start, end);
				bookMoves.add(nextMove);
				break;
			case 'B': // bishop
				for (int dirIndex = Compass.getStartIndex(Chess.BISHOP); dirIndex < Compass
						.getEndIndex(Chess.BISHOP); dirIndex++) {
					for (int step = 1; step <= Compass.numSquaresToEdge64x16[end][dirIndex]; step++) {
						if (ch.board[end + step * Compass.directions[dirIndex]] == (Chess.BISHOP
								| ch.getActiveColour())) {
							if (start == -1)
								start = end + step * Compass.directions[dirIndex];
							else
								astart = end + step * Compass.directions[dirIndex];
							break;
						}
					}
				}
				// validate ambiguity
				if (astart != -1) {
					if (Character.isDigit(nextMoveString.charAt(1))) {
						if ((new Coordinate(
								new String(new char[] { nextMoveString.charAt(2), nextMoveString.charAt(1) })))
								.getSquare() == astart) {
							start = astart;
						}
					} else {
						if (Character.isDigit(nextMoveString.charAt(2))) {
							start = (new Coordinate(nextMoveString.substring(1, 3))).getSquare();
						}
					}
				}
				nextMove = new Move(start, end);
				bookMoves.add(nextMove);
				break;
			case 'R': // rook
				for (int dirIndex = Compass.getStartIndex(Chess.ROOK); dirIndex < Compass
						.getEndIndex(Chess.ROOK); dirIndex++) {
					for (int step = 1; step <= Compass.numSquaresToEdge64x16[end][dirIndex]; step++) {
						if (ch.board[end + step * Compass.directions[dirIndex]] == (Chess.ROOK
								| ch.getActiveColour())) {
							if (start == -1)
								start = end + step * Compass.directions[dirIndex];
							else
								astart = end + step * Compass.directions[dirIndex];
							break;
						}
					}
				}
				// validate ambiguity
				if (astart != -1) {
					if (Character.isDigit(nextMoveString.charAt(1))) {
						if ((new Coordinate(
								new String(new char[] { nextMoveString.charAt(2), nextMoveString.charAt(1) })))
								.getSquare() == astart) {
							start = astart;
						}
					} else {
						if (Character.isDigit(nextMoveString.charAt(2))) {
							start = (new Coordinate(nextMoveString.substring(1, 3))).getSquare();
						}
					}
				}
				nextMove = new Move(start, end);
				bookMoves.add(nextMove);
				break;
			case 'Q': // queen
				for (int dirIndex = Compass.getStartIndex(Chess.QUEEN); dirIndex < Compass
						.getEndIndex(Chess.QUEEN); dirIndex++) {
					for (int step = 1; step <= Compass.numSquaresToEdge64x16[end][dirIndex]; step++) {
						if (ch.board[end + step * Compass.directions[dirIndex]] == (Chess.QUEEN
								| ch.getActiveColour())) {
							if (start == -1)
								start = end + step * Compass.directions[dirIndex];
							else
								astart = end + step * Compass.directions[dirIndex];
							break;
						}
					}
				}
				// validate ambiguity
				if (astart != -1) {
					if (Character.isDigit(nextMoveString.charAt(1))) {
						if ((new Coordinate(
								new String(new char[] { nextMoveString.charAt(2), nextMoveString.charAt(1) })))
								.getSquare() == astart) {
							start = astart;
						}
					} else {
						if (Character.isDigit(nextMoveString.charAt(2))) {
							start = (new Coordinate(nextMoveString.substring(1, 3))).getSquare();
						}
					}
				}
				nextMove = new Move(start, end);
				bookMoves.add(nextMove);
				break;
			case 'K': // king
				for (int dirIndex = Compass.getStartIndex(Chess.KING); dirIndex < Compass
						.getEndIndex(Chess.KING); dirIndex++) {
					if (Compass.numSquaresToEdge64x16[end][dirIndex] > 0) {
						if (ch.board[end + Compass.directions[dirIndex]] == (Chess.KING | ch.getActiveColour())) {
							start = end + Compass.directions[dirIndex];
							nextMove = new Move(start, end);
							bookMoves.add(nextMove);
						}
					}
				}
				break;
			case 'O': // castle
				switch (nextMoveString.length()) {
				case 3: // castle kingside
					start = 4 + (8 * 7 * ch.activeColourIndex);
					end = start + 2;
					nextMove = new Move(start, end, "castle");
					bookMoves.add(nextMove);
					break;
				case 5: // castle queenside
					start = 4 + (8 * 7 * ch.activeColourIndex);
					end = start - 2;
					nextMove = new Move(start, end, "castle");
					bookMoves.add(nextMove);
					break;
				}
				break;
			default: // pawn
				int pawnToMove = Chess.PAWN | ch.getActiveColour();
				if (nextMoveString.charAt(1) != 'x') {
					if (ch.board[end - Compass.pMoves[ch.activeColourIndex]] == pawnToMove) {
						start = end - Compass.pMoves[ch.activeColourIndex];
						nextMove = new Move(start, end);
						bookMoves.add(nextMove);
					} else if (ch.board[end - 2 * Compass.pMoves[ch.activeColourIndex]] == pawnToMove) {
						start = end - 2 * Compass.pMoves[ch.activeColourIndex];
						nextMove = new Move(start, end);
						bookMoves.add(nextMove);
					}
				} else {
					start = (new Coordinate(nextMoveString.charAt(0) + Integer.toString(
							Character.getNumericValue(nextMoveString.charAt(3)) - 1 + (2 * ch.activeColourIndex))))
							.getSquare();
					nextMove = new Move(start, end);
					bookMoves.add(nextMove);
				}
			}
		}
		// Now we have an array of moves indexed to an array of ECO codes and opening
		// names
		// Now prompt the user to select an opening from the book
//		for (int i = 0; i < bookMoves.size(); i++) {
//			System.out.printf("%-4d|", i);
//			System.out.printf("%-3s:", bookData.get(0).get(i));
//			System.out.printf("%-65s", bookData.get(1).get(i));
//			System.out.println(bookData.get(2).get(i));
//		}
		time = System.currentTimeMillis() - startTime;
//		System.out.println("Choose an opening in the console or -1 for random:");
		int randint = rando.nextInt(bookMoves.size());

		// Pick an opening by ECO code:
//		for (int i = 0; i < bookData.get(0).size(); i++) {
//			if (bookData.get(0).get(i).equalsIgnoreCase("D62"))
//				randint = i;
//		}

		Move moveSelected = bookMoves.get(randint);
		if (moveSelected.start == -1 || moveSelected.end == -1) {
			throw new IllegalArgumentException("Invalid move conversion");
			// return null;
		}

		System.out.println();
		System.out.printf("%-4d|", randint);
		System.out.printf("%-3s:", bookData.get(0).get(randint));
		System.out.printf("%-30s ", bookData.get(1).get(randint));
		System.out.println(bookData.get(2).get(randint));
		return moveSelected;
	}

	// One! Ha, ha, ha
	private int countPieces(Chess ch) {
		int count = 0;
		for (int i = 0; i < 64; i++) {
			if (!ch.isEmptySquare(i))
				count++;
		}
		return count;
	}

	// a stores the score-to-beat for future moves searched
	// b stores the minimum eval allowed by the opponent
	public Move rootNegaMax(int depth, float a, float b) throws InterruptedException {
		this.test = new Chess(ch.startpos);
		if (ch.moveHistory.size() > 0)
			for (Move m : ch.moveHistory)
				test.makeHistory(m);
		MoveGenerator mgen = new MoveGenerator(test);
		if (mgen.moves.size() == 1)
			return mgen.moves.get(0);
		mgen.moves = orderMovesByValue(mgen.moves, test);
		long startTime = System.currentTimeMillis();
		nodes = 0;
		Move bestMove = null;
		// MIN - 1 so that the computer will play into mate
		// Shouldn't be needed now that mate-in-x is adjusted
		a--;
		// counter variable for printing move labels
		int moveCounter = 0;
		float score = 0;
		System.out.println("\nCurrent eval is " + String.format("%.2f", evalMaterial(mgen, 0)));
		System.out.println(
				"Starting search of depth " + depth + " at " + DateFormat.getTimeInstance().format(new Date()));
		for (Move m : mgen.moves) {
			if (Thread.interrupted()) // We've been interrupted: no more crunching.
				throw new InterruptedException();
			test.makeHistory(m);
			moveCounter++;
			nodes++;
			long nodeStart = System.currentTimeMillis();
			long subnodes = nodes;
			System.out.printf("%2d/%-2d: %-7s", moveCounter, mgen.moves.size(), ch.moveText(m));
			score = -negaMax(depth - 1, -b, -a);
			if (score > a) {
				bestMove = m;
				a = score;
			}
			test.unmakeMove();
			subnodes = nodes - subnodes;
			System.out.printf("%+-7.2f|%8d nodes|", score, subnodes);
			long nodeTime = System.currentTimeMillis() - nodeStart;
			// Print time
			if (System.currentTimeMillis() - startTime > 1000) {
				System.out.printf("%3d.%03ds |", (System.currentTimeMillis() - startTime) / 1000,
						System.currentTimeMillis() % 1000);
			} else {
				System.out.printf("%5dms  |", System.currentTimeMillis() - startTime);
			}
			// Print nodes/sec^2
			if (nodeTime > 0 && nodeTime < subnodes) {
				double rate = subnodes / (nodeTime / 1000f);
				System.out.printf("%10.0f nodes/sec%n", rate);
			} else
				System.out.println();
		}
		int timepassed = (int) (System.currentTimeMillis() - startTime);
		// Print the turn number
		System.out.print(ch.moveHistory.size() / 2 + 1);
		if (ch.getActiveColour() == Chess.WHITE)
			System.out.print(". ");
		else
			System.out.print("... ");
		// Print the chosen move
		System.out.print(ch.moveText(bestMove));
		// Print the eval of the predicted position
		System.out.printf(" \teval:\t%+f\t%n", a);
		// Print the search statistics
		System.out.print("Search of " + nodes + " nodes finished in ");
		if (timepassed < 1000) {
			System.out.printf("%dms", timepassed);
		} else
			System.out.printf("%d.%ds", timepassed / 1000, timepassed % 1000);
		float speed;
		speed = nodes * 1000 / timepassed;
		System.out.printf(" at %.0f nodes/sec%n", speed);
		return bestMove;
	}

	public float negaMax(int depth, float a, float b) throws InterruptedException {
		MoveGenerator mgen = new MoveGenerator(test);
		if (depth == 0 || mgen.moves.size() == 0 || mgen.gameOver != -1) {
			// TODO: replace this eval call with a quiescence search
			float score = evalMaterial(mgen, depth);
			return score;
		}
		mgen.moves = orderMovesByValue(mgen.moves, test);
		for (Move m : mgen.moves) {
			if (Thread.interrupted()) // We've been interrupted: no more crunching.
				throw new InterruptedException();
			test.makeHistory(m);
			nodes++;
			float score = -negaMax(depth - 1, -b, -a);
			test.unmakeMove();
			if (score >= b)
				return b;
			if (score > a)
				a = score;
		}
		return a;
	}

	// Orders moves based on the values of pieces on the given board
	// Checks capturing then pawn promotions then moving pieces by descending value
	ArrayList<Move> orderMovesByValue(ArrayList<Move> movein, Chess b0) {
		ArrayList<Move> moveout = new ArrayList<Move>();
		// store the indecies of sorted elements so they can be ignored later
		boolean map[] = new boolean[movein.size()];
		// moves by captures by captured type by descending value
		for (int type = Chess.QUEEN; type >= Chess.PAWN; type--)
			for (int i = 0; i < movein.size(); i++)
				if ((b0.board[movein.get(i).end] & Chess.TYPE_MASK) == type) {
					moveout.add(movein.get(i));
					map[i] = true;
				}
		// promotions!
		for (int i = 0; i < movein.size(); i++)
			if (!map[i] && movein.get(i).promoteType != 0) {
				moveout.add(movein.get(i));
				map[i] = true;
			}
		for (int type = Chess.QUEEN; type >= Chess.PAWN; type--)
			for (int i = 0; i < movein.size(); i++)
				if (!map[i] && (b0.board[movein.get(i).start] & Chess.TYPE_MASK) == type) {
					map[i] = true;
					moveout.add(movein.get(i));
				}
		for (int i = 0; i < movein.size(); i++)
			if (!map[i]) {
				moveout.add(movein.get(i));
			}
		assert moveout.size() == movein.size() : movein.size() - moveout.size();
		return moveout;
	}

	private float evalMaterial(MoveGenerator evalgen, int mateOffset) {
		float score = 0;
		int gameOver = evalgen.gameOver;
		if (gameOver != -1) {
			if (gameOver == Chess.WHITE_INDEX) {
				// If white has checkmate, eval the position high
				return mateinzero - mateOffset;
			} else if (gameOver == Chess.BLACK_INDEX) {
				// If black has checkmate, eval the position low
				return -mateinzero + mateOffset;
			} else {
				// If it is a draw (repetition or stalemate) return 0
				return 0.0f;
			}
		} else {
			// are we in the endgame?
			int tables[][] = (countPieces(test) < 12) ? PieceLocationTables.endgamePieceTables
					: PieceLocationTables.middlegamePieceTables;
			for (int sq = 0; sq < 64; sq++) {
				if (test.isEmptySquare(sq))
					continue;
				else if (test.board[sq] >>> 3 == Chess.WHITE) {
					score += pieceValue[Piece.type(test.board[sq])];
					score += (PieceLocationTables.read(tables[Piece.type(test.board[sq])], sq, true)) / 100f;
				} else {
					score -= pieceValue[Piece.type(test.board[sq])];
					score -= (PieceLocationTables.read(tables[Piece.type(test.board[sq])], sq, false)) / 100f;
				}
			}
		}
		// round to the nearest hundreth
		score = Math.round(score * 100) / 100f;

		// mobility score:
//		float mob = evalgen.moves.size();
//		test.activeColourIndex = ~test.activeColourIndex & 1;
//		MoveGenerator opgen = new MoveGenerator(test);
//		mob -= opgen.moves.size();
//		test.activeColourIndex = ~test.activeColourIndex & 1;
//		mob *= mobWeight;
//		score += mob;
//		System.out.printf("mobility: %+-7.2f | score: %+-7.2f | ratio: %+-7.2f\n", mob, score, mob/score);
		
		// adjust the eval so the player to move is positive
		return (evalgen.activeColour == Chess.WHITE) ? score : -score;
	}

}
