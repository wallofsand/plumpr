package chess3;

public class Bitboard {

	// does this bitboard contain square sq?
	public static boolean containsSquare(long bb, int sq) {
		return ((bb >>> sq) & 1) == 1;
	}

	// TEST FUNCTION
	// print a bitboard as an 8x8 grid for testing
	public static void printBinaryString(long bb) {
		int leadingZeroCount = Long.numberOfLeadingZeros(bb);
		String zeros = "";
		for (int i = 0; i < leadingZeroCount; i++)
			zeros += "0";
		if (bb != 0)
			zeros = zeros.concat(Long.toBinaryString(bb));
		String reverse = "";
		for (int i = 64; i > 0; i--) {
			reverse = reverse.concat(zeros.substring(i - 1, i));
		}
		Coordinate cursor = new Coordinate(0);
		for (int ranks = 7; ranks >= 0; ranks--) {
			for (int files = 0; files <= 7; files++) {
				cursor.setCoordinate(ranks, files);
				if (Coordinate.getColourIndex(cursor) == Chess.WHITE_INDEX)
					System.out.print(Chess.YELLOW_BACKGROUND);
				else if (Coordinate.getColourIndex(cursor) == Chess.BLACK_INDEX)
					System.out.print(Chess.GREEN_BACKGROUND);
				System.out.print(reverse.substring(cursor.getSquare(), cursor.getSquare() + 1) + " ");
			}
			System.out.println(Chess.ANSI_RESET);
		}
	}
}
