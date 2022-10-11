package chess3;

public abstract class PieceLocationTables {
	// method to interpolate normal and endgame tables
	// middlegameWeight is the number of pieces / 32 - a value between 0.0 and 1.0
	public static float complexRead(int type, int square, float middlegameWeight, boolean isWhite) {
		float opening = read(middlegamePieceTables[type], square, isWhite) * middlegameWeight;
		float endgame = read(endgamePieceTables[type], square, isWhite) * (1 - middlegameWeight);
		return opening + endgame;
	}
	
	// all tables are read from black's perspective
	// when white reads a table, we need to flip it along the Y axis
	public static int read(int[] table, int square, boolean isWhite) {
		if (isWhite) {
			int file = BoardRepresentation.fileIndex(square);
			int rank = 7 - BoardRepresentation.rankIndex(square);
			square = 8 * rank + file;
		}
		return table[square];
	}
	
	public static final int[] pawns = {
			0,  0,   0,   0,   0,   0,   0,   0,
			50, 50,  50,  50,  50,  50,  50,  50,
			40, 40,  40,  40,  40,  40,  40,  40,
			30, 30,  30,  30,  30,  30,  30,  30,
			0,  0,   0,   25,  25,  0,   0,   0,
			0,  -10, -10, 15,  15,  -10, -10, 0,
			-5, 20,  20,  -20, -20, 20,  20,  -5,
			0,  0,   0,   0,   0,   0,   0,   0
	};
	
	public static final int[] knights = {
			-50, -40, -30, -30, -30, -30, -40, -50,
			-30, -20, -20, -10, -10, -20, -20, -30,
			-20, 0,   20,  30,  30,  20,  0,   -20,
			-10, 0,   40,  50,  50,  40,  0,   -10,
			-10, 0,   40,  50,  50,  40,  0,   -10,
			-20, 0,   25,  20,  20,  25,  0,   -20,
			-30, -20, -20, -10, -10, -20, -20, -30,
			-50, -35, -20, -20, -20, -20, -35, -50
	};
	
	public static final int[] bishops = {
			-40, -20, -10, -10, -10, -10, -20, -40,
			0,   0,   10,  10,  10,  10,  0,   0,
			0,   10,  20,  20,  20,  20,  10,  0,
			0,   10,  30,  50,  50,  30,  10,  0,
			0,   10,  40,  50,  50,  40,  10,  0,
			0,   10,  20,  40,  40,  20,  10,  0,
			5,   30,  5,   20,  20,  5,   30,  5,
			-50, -20, -5,  -10, -10, -5,  -20, -50
	};
	
	public static final int[] rooks = {
			 0, 0, 0, 0,  0,  0, 0, 0,
			-5, 0, 0, 0,  0,  0, 0, -5,
			-5, 0, 0, 0,  0,  0, 0, -5,
			-5, 0, 0, 0,  0,  0, 0, -5,
			-5, 0, 0, 0,  0,  0, 0, -5,
			-5, 0, 0, 0,  0,  0, 0, -5,
			-5, 0, 0, 0,  0,  0, 0, -5,
			0,  0, 0, 10, 10, 0, 0, 0
	};
	
	public static final int[] queens = {
			-5, -5, -5, -5, -5, -5, -5, -5,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0,
			0,  0,  0,  0,  0,  0,  0,  0
	};

	// two tables for kings: middlegame and endgame
	public static final int[] kingsMiddle = {
			-50, -50, -50, -50, -50, -50, -50, -50,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-40, -40, -40, -40, -40, -40, -40, -40,
			-30, -30, -30, -50, -50, -40, -30, -20,
			-30, -30, -40, -50, -50, -40, -30, -30,
			-30, -30, -30, -30, -30, -30, -30, -30,
			-30, -30, -30, -30, -30, -30, -30, -30,
			0,   0,   40,  -30, -30, -30, 40,  0
	};
	
	public static final int[] kingsEnd = {
			-30, -20, -10, -10, -10, -10, -20, -30,
			-20, -5,  0,   0,   0,   0,   -5,  -20,
			-20, 0,   10,  15,  15,  10,  0,   -20,
			-20, 0,   15,  25,  25,  15,  0,   -20,
			-30, 0,   15,  25,  25,  15,  0,   -30,
			-30, 0,   10,  15,  15,  10,  0,   -30,
			-40, -20, -5,   -5,   -5,   -5,   -20, -40,
			-50, -30, -10, -10, -10, -10, -30, -50
	};
	
	public static final int[][] middlegamePieceTables = {new int[0], pawns, knights, bishops, rooks, queens, kingsMiddle };
	public static final int[][] endgamePieceTables = {new int[0], pawns, knights, bishops, rooks, queens, kingsEnd };

}
