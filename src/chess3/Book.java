package chess3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public abstract class Book {

	private static ArrayList<ArrayList<String>> bookDB = new ArrayList<ArrayList<String>>();
	private static ArrayList<String> codesDB = new ArrayList<String>();
	private static ArrayList<String> namesDB = new ArrayList<String>();
	private static ArrayList<String> movesDB = new ArrayList<String>();

	public static void getOpenings() {
		long startTime = System.currentTimeMillis();
		File openingData = null;
		Scanner sc = null;
		codesDB.ensureCapacity(2730);
		namesDB.ensureCapacity(2730);
		movesDB.ensureCapacity(2730);
		bookDB.add(codesDB);
		bookDB.add(namesDB);
		bookDB.add(movesDB);
		try {
			openingData = new File("C:\\Users\\graha\\OneDrive\\Documents\\Java Projects\\Chess3\\books\\OpeningBook.csv");
			sc = new Scanner(openingData);
			sc.useDelimiter(",|\r\n");
			String line = null;
			sc.next();
			sc.next();
			sc.next();
			System.out.println("Opening book located succesfully.");
			while (sc.hasNext()) {
				line = sc.next();
				if (line.length() != 0) {
					codesDB.add(line);
//					if(codesDB.get(codesDB.size()-1).length() != 3)
//					System.out.printf("codes: %-5s", line);
				}
				line = sc.next();
				if (line.length() != 0) {
					namesDB.add(line);
//					if(codesDB.get(codesDB.size()-1).length() != 3)
//					System.out.printf("names: %-60s", line);
				}
				line = sc.next();
				if (line.length() != 0) {
					movesDB.add(line);
//					if(codesDB.get(codesDB.size()-1).length() != 3)
//					System.out.println("moves: " + line);
				}
			}
			System.out.print("size of codes: " + codesDB.size());
			System.out.print("\tsize of names: " + namesDB.size());
			System.out.print("\tsize of moves: " + movesDB.size());
			System.out.println("\t"+(System.currentTimeMillis() - startTime) + "ms!");
		} catch (FileNotFoundException e0) {
			System.err.println("Can not find specified file!");
			e0.printStackTrace();
		} finally {
			if (sc != null)
				sc.close();
		}
	}

	// Method to find opening lines from the book
	public static ArrayList<ArrayList<String>> readABook(Chess ch) {
		ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
		ArrayList<String> codes = new ArrayList<String>();
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> moves = new ArrayList<String>();

		Chess test = new Chess(ch.startpos);
		String moveHistory = new String();
		// get each move from move history
		for (Move m : ch.moveHistory) {
			moveHistory += test.moveText(m) + " ";
			test.makeHistory(m);
		}
//		System.out.println("Move sequence to find in book: \"" + moveHistory + "\"");
		// find that sequence in book moves
		Scanner histSc = null;
		Scanner moveSc = null;
		for (int i = 0; i < Book.movesDB.size(); i++) {
			histSc = new Scanner(moveHistory);
			histSc.useDelimiter(" ");
			moveSc = new Scanner(movesDB.get(i));
			moveSc.useDelimiter(" ");
			do {
				// this line may be unnessesary or causeing an issue
				// if (!histSc.hasNext() && moveHistory.length() != 0) break;
				// UPDATE: this statement needed to happen at the end of the do - while loop,
				// not the beginning
				// ALSO: changed the while to a do - while to bundle the .hasNext() checks for
				// both scanners in the loop
				// steps through the game history and checks that the book entry has the same
				// moves
				if (histSc.hasNext() && moveHistory.length() != 0)
					if (!histSc.next().equals(moveSc.next()))
						break;
				// if we have reached the end of move history, are there further book moves we
				// could make?
				if (!histSc.hasNext() && moveSc.hasNext()) {
					// if there are, write them down!
					codes.add(codesDB.get(i));
					names.add(namesDB.get(i));
					moves.add(moveSc.next());
				}
			} while (moveSc.hasNext() && histSc.hasNext());
		}
		if (moves.size() == 0) return null;
		data.add(codes);
		data.add(names);
		data.add(moves);
		return data;
	}
	/*
	 * private static void addBookMove(Move nextMove, ArrayList<ArrayList<?>>
	 * arrList, int bookIndex) { boolean addmove = true; if (!arrList.isEmpty()) {
	 * for (Move m : arrList.get(2)) { if (m.start == nextMove.start && m.end ==
	 * nextMove.end) { addmove = false; } } if (addmove) arrList.add(nextMove); }
	 * else { arrList.add(nextMove); } }
	 */
}

/* OLD BOOK MOVE GENERATION CODE: GB 9/3/2022
 * // Should be an unnessesary sanity check; remove? // if
 * (nextMoveString.length() == 0) continue; int start; // find the square the
 * book move points too int end = (new
 * Coordinate((nextMoveString.charAt(nextMoveString.length() - 1) == '#' ||
 * nextMoveString.charAt(nextMoveString.length() - 1) == '+') ?
 * nextMoveString.substring(nextMoveString.length() - 3) :
 * nextMoveString.substring(nextMoveString.length() - 2))).getSquare(); // Find
 * the starting square for the move // Look at the board and deduce which piece
 * could make the move // TODO: ambigous moves confuse the algorithm switch
 * (nextMoveString.charAt(0)) { case 'N': // knight for (int dirIndex =
 * Compass.getStartIndex(Chess.KNIGHT); dirIndex <
 * Compass.getEndIndex(Chess.KNIGHT); dirIndex++) { if
 * (Compass.numSquaresToEdge[end][dirIndex] > 0) { if (ch.b1.board[end +
 * Compass.directions[dirIndex]] == (Chess.KNIGHT | ch.b1.getActiveColour())) {
 * start = end + Compass.directions[dirIndex]; nextMove = new Move(start, end);
 * addBookMove(nextMove, data, moves.indexOf(str)); } } } break; case 'B': //
 * bishop for (int dirIndex = Compass.getStartIndex(Chess.BISHOP); dirIndex <
 * Compass.getEndIndex(Chess.BISHOP); dirIndex++) { for (int step = 1; step <=
 * Compass.numSquaresToEdge[end][dirIndex]; step++) { if (ch.b1.board[end + step
 * * Compass.directions[dirIndex]] == (Chess.BISHOP | ch.b1.getActiveColour()))
 * { start = end + step * Compass.directions[dirIndex]; nextMove = new
 * Move(start, end); addBookMove(nextMove, data, moves.indexOf(str)); } } }
 * break; case 'R': // rook for (int dirIndex =
 * Compass.getStartIndex(Chess.ROOK); dirIndex <
 * Compass.getEndIndex(Chess.ROOK); dirIndex++) { for (int step = 1; step <=
 * Compass.numSquaresToEdge[end][dirIndex]; step++) { if (ch.b1.board[end + step
 * * Compass.directions[dirIndex]] == (Chess.ROOK | ch.b1.getActiveColour())) {
 * start = end + step * Compass.directions[dirIndex]; nextMove = new Move(start,
 * end); addBookMove(nextMove, data, moves.indexOf(str)); } } } break; case 'Q':
 * // queen for (int dirIndex = Compass.getStartIndex(Chess.QUEEN); dirIndex <
 * Compass.getEndIndex(Chess.QUEEN); dirIndex++) { for (int step = 1; step <=
 * Compass.numSquaresToEdge[end][dirIndex]; step++) { if (ch.b1.board[end + step
 * * Compass.directions[dirIndex]] == (Chess.QUEEN | ch.b1.getActiveColour())) {
 * start = end + Compass.directions[dirIndex]; nextMove = new Move(start, end);
 * addBookMove(nextMove, data, moves.indexOf(str)); } } } break; case 'K': //
 * king for (int dirIndex = Compass.getStartIndex(Chess.KING); dirIndex <
 * Compass.getEndIndex(Chess.KING); dirIndex++) { if
 * (Compass.numSquaresToEdge[end][dirIndex] > 0) { if (ch.b1.board[end +
 * Compass.directions[dirIndex]] == (Chess.KING | ch.b1.getActiveColour())) {
 * start = end + Compass.directions[dirIndex]; nextMove = new Move(start, end);
 * addBookMove(nextMove, data, moves.indexOf(str)); } } } break; case 'O': //
 * castle switch (nextMoveString) { case "O-O": // castle kingside start = 4 +
 * (8 * 7 * ch.b1.activeColourIndex); end = start + 2; nextMove = new
 * Move(start, end, "castle"); addBookMove(nextMove, data, moves.indexOf(str));
 * break; case "O-O-O": // castle queenside start = 4 + (8 * 7 *
 * ch.b1.activeColourIndex); end = start - 2; nextMove = new Move(start, end,
 * "castle"); addBookMove(nextMove, data, moves.indexOf(str)); break; } break;
 * default: // pawn if (nextMoveString.charAt(1) != 'x') { if
 * (Piece.type(ch.b1.board[end - Compass.pMoves[ch.b1.activeColourIndex]]) ==
 * Chess.PAWN) { start = end - Compass.pMoves[ch.b1.activeColourIndex]; nextMove
 * = new Move(start, end); addBookMove(nextMove, data, moves.indexOf(str)); }
 * else if (Piece.type(ch.b1.board[end - 2 *
 * Compass.pMoves[ch.b1.activeColourIndex]]) == Chess.PAWN) { start = end - 2 *
 * Compass.pMoves[ch.b1.activeColourIndex]; nextMove = new Move(start, end);
 * addBookMove(nextMove, data, moves.indexOf(str)); } } else { start = (new
 * Coordinate(nextMoveString.charAt(0) +
 * Integer.toString(Character.getNumericValue(nextMoveString.charAt(3))-1))).
 * getSquare(); nextMove = new Move(start, end); addBookMove(nextMove, data,
 * moves.indexOf(str)); } }
 */
