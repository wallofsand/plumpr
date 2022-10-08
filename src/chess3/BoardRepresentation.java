package chess3;

public final class BoardRepresentation {

	// square names are read FILE then RANK
	public static final char[] fileNames = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h' };
	public static final char[] rankNames = { '1', '2', '3', '4', '5', '6', '7', '8' };

	// Rank (0 to 7) of a square
	public static int rankIndex(int squareIndex) {
		return squareIndex >> 3;
	}

	// File (0 to 7) of a square
	public static int fileIndex(int squareIndex) {
		return squareIndex & 0b000_111;
	}

	// Square (0 to 63) for a given file-rank coordinate pair
	public static int indexFromCoord(int fileIndex, int rankIndex) {
		return rankIndex * 8 + fileIndex;
	}

	public static int indexFromCoord(Coordinate v) {
		return indexFromCoord(v.fileIndex, v.rankIndex);
	}

	public static Coordinate coordFromIndex(int squareIndex) {
		return new Coordinate(fileIndex(squareIndex), rankIndex(squareIndex));
	}

	public static boolean lightSquare(int squareIndex) {
		return squareIndex % 2 == 1 ? true : false;
	}

	public static boolean lightSqaure(int fileIndex, int rankIndex) {
		return (fileIndex + rankIndex) % 2 != 0;
	}

	// Methods to return the name of a square in algebraic notation:
	public static String squareNameFromCoordinate(Coordinate Coord) {
		return squareNameFromCoordinate(Coord.fileIndex, Coord.rankIndex);
	}

	public static String squareNameFromCoordinate(int rankIndex, int fileIndex) {
		return Character.toString(fileNames[fileIndex]) + Character.toString(rankNames[rankIndex]);
	}

	public static String squareNameFromIndex(int squareIndex) {
		return squareNameFromCoordinate(rankIndex(squareIndex), fileIndex(squareIndex));
	}

}
