package chess3;

public class Coordinate {

	// char || int
	// Ranks: 1 - 8 || 0 - 7
	public int rankIndex;
	// Files: a - h || 0 - 7
	public int fileIndex;

	public static int getColourIndex(Coordinate v) {
		switch ((v.fileIndex + v.rankIndex) % 2) {
		case 0:
			return Chess.BLACK_INDEX;
		case 1:
			return Chess.WHITE_INDEX;
		default:
			return 0;
		}
	}

	public static int rankIndex(int sq) {
		return sq >> 3;
	}

	public static int fileIndex(int sq) {
		return sq % 8;
	}

	public static int yDistance(int sq1, int sq2) {
		return Math.abs(rankIndex(sq2) - rankIndex(sq1));
	}

	public static int xDistance(int sq1, int sq2) {
		return Math.abs(fileIndex(sq2) - fileIndex(sq1));
	}

	public void setCoordinate(String cString) {
		// Assumes a 2-character string like "a4", "g7"
		cString = cString.toLowerCase();

		setCoordinate((int) cString.charAt(0) - 97, Character.getNumericValue(cString.charAt(1)) - 1);
	}

	public void setCoordinate(int sq) {
		this.rankIndex = sq / 8;
		this.fileIndex = sq % 8;
	}

	public void setCoordinate(int rankIndex, int fileIndex) {
		this.rankIndex = rankIndex;
		this.fileIndex = fileIndex;
	}

	public int getSquare() {
		return getSquare(this.rankIndex, this.fileIndex);
	}

	public static int getSquare(int rankIndex, int fileIndex) {
		// Returns the number of the square at v
		// from 0 - 63 to use as the index of Chess.board[]
		return 8 * rankIndex + fileIndex;
	}

	public Coordinate(int sq) {
		this.rankIndex = rankIndex(sq);
		this.fileIndex = fileIndex(sq);
	}

	public Coordinate(int rankIndex, int fileIndex) {
		this.fileIndex = fileIndex;
		this.rankIndex = rankIndex;
	}

	public Coordinate(int rankIndex, char charFile) {
		// Assumes a letter a-h
		this.fileIndex = (int) (charFile - 'a');
		this.rankIndex = rankIndex;
	}

	public Coordinate(String cString) {
//		System.out.println("Making coordinate at: " + cString);
		// Assumes a 2-character string like "a4", "g7"
		this.fileIndex = (int) cString.charAt(0) - 97;
		this.rankIndex = Character.getNumericValue(cString.charAt(1)) - 1;
	}

	@Override
	public String toString() {
		String str = new String();
		str = str.concat(Character.toString((char) (fileIndex + 'a')));
		str = str.concat(Integer.toString(rankIndex + 1));
		return str;
	}

}
