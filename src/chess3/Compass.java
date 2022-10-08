package chess3;

import java.util.ArrayList;

// class to store precomputed information about the board geometry
public final class Compass {

	public static final int NORTH = 8, EAST = 1, SOUTH = -8, WEST = -1, NORTHEAST = 9, NORTHWEST = 7, SOUTHWEST = -9,
			SOUTHEAST = -7, NNE = 17, NEE = 10, SEE = -6, SSE = -15, SSW = -17, SWW = -10, NWW = 6, NNW = 15;

	// Knight move increments: KnightStartIndex <= i < directions.length
	// Bishop move increments: BishopStartIndex <= i < KnightStartIndex
	// Rook move increments: RookStartIndex <= i < BishopStartIndex
	// Queen move increments: RookStartIndex <= i < KnightStartIndex
	// King move increments: as queen
	public static final int RookStartIndex = 0;
	public static final int BishopStartIndex = 4;
	public static final int KnightStartIndex = 8;

	public static final int[] directions = { NORTH, EAST, SOUTH, WEST, NORTHEAST, NORTHWEST, SOUTHWEST, SOUTHEAST, NNE,
			NEE, SEE, SSE, SSW, SWW, NWW, NNW };

	// directions sliding pieces can move in:
	public static final int[] rDir = { SOUTH, WEST, EAST, NORTH };
	public static final int[] bDir = { SOUTHWEST, SOUTHEAST, NORTHWEST, NORTHEAST };

	// directions pawns can capture in
	// first dimension is white/black colour index
	public static final int[][] pAttacks = { { NORTHWEST, NORTHEAST }, { SOUTHWEST, SOUTHEAST } };

	// directions pawns can capture in
	// first dimension is white/black colour index
	public static final int[] pMoves = { NORTH, SOUTH };

	// array of edge distance at each square, in each direction
	public static int[][] numSquaresToEdge64x16 = new int[64][16];

	// array to store the possible knight moves from each square
	public static int[][] knightMoves = new int[64][];

	// method to return the index of a direction in the Compass.directions[] array
	public static int dirIndexFromDirection(int dir) {
		for (int i = 0; i < directions.length; i++) {
			if (directions[i] == dir)
				return i;
		}
		throw new IllegalArgumentException("Direction does not exist!");
	}
	
	// Method to return the direction of the ray that passes through two colinear
	// squares from sq1 to sq2
	public static int getRay(int sq1, int sq2) {
		int[] ed1 = numSquaresToEdge64x16[sq1];
		int startIndex = 0;
		int endIndex = KnightStartIndex;
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			int dir = directions[dirIndex];
			for (int step = 1; step <= ed1[dirIndex]; step++) {
				if (sq1 + step * dir == sq2)
					return dir;
			}
		}
		throw new IllegalArgumentException("Squares are not colinear!");
	}

	// Method to return the type of sliding piece that moves in the given direction
	public static int getSliderFromDirection(int dir) {
		switch (dir) {
		case NORTH, SOUTH, EAST, WEST:
			return Chess.ROOK;
		case NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST:
			return Chess.BISHOP;
		default:
			throw new IllegalArgumentException("Not a sliding diection!");
		}
	}

	// Method to calculate all precomputed move and board data
	// called once at the start of each program, before FEN is read
	public static void initialize() {
		calculateEdgeDistance();
		precomputeKnightMoves();

//		printEdgeDistance();
//		printKnightMoves();
	}

	public static void calculateEdgeDistance() {
		for (int sqIndex = 0; sqIndex < 64; sqIndex++) {
			int y = sqIndex / 8;
			int x = sqIndex % 8;
			int nStep = 7 - y;
			int eStep = 7 - x;
			int wStep = x;
			int sStep = y;

			numSquaresToEdge64x16[sqIndex][0] = nStep;
			numSquaresToEdge64x16[sqIndex][1] = eStep;
			numSquaresToEdge64x16[sqIndex][2] = sStep;
			numSquaresToEdge64x16[sqIndex][3] = wStep;
			numSquaresToEdge64x16[sqIndex][4] = Integer.min(nStep, eStep);
			numSquaresToEdge64x16[sqIndex][5] = Integer.min(nStep, wStep);
			numSquaresToEdge64x16[sqIndex][6] = Integer.min(sStep, wStep);
			numSquaresToEdge64x16[sqIndex][7] = Integer.min(sStep, eStep);
			numSquaresToEdge64x16[sqIndex][8] = Integer.min(nStep / 2, eStep);
			numSquaresToEdge64x16[sqIndex][9] = Integer.min(nStep, eStep / 2);
			numSquaresToEdge64x16[sqIndex][10] = Integer.min(sStep, eStep / 2);
			numSquaresToEdge64x16[sqIndex][11] = Integer.min(sStep / 2, eStep);
			numSquaresToEdge64x16[sqIndex][12] = Integer.min(sStep / 2, wStep);
			numSquaresToEdge64x16[sqIndex][13] = Integer.min(sStep, wStep / 2);
			numSquaresToEdge64x16[sqIndex][14] = Integer.min(nStep, wStep / 2);
			numSquaresToEdge64x16[sqIndex][15] = Integer.min(nStep / 2, wStep);
		}
		return;
	}

	// Method to precompute the possible moves for a knight from each square
	// Called once at the start of the program
	public static void precomputeKnightMoves() {
		int startIndex = KnightStartIndex;
		int endIndex = directions.length;
		for (int sqIndex = 0; sqIndex < 64; sqIndex++) {
			// temporary collection of knight moves from a single square
			ArrayList<Integer> knightSquareMoves = new ArrayList<Integer>();
			for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
				if (Compass.numSquaresToEdge64x16[sqIndex][dirIndex] > 0) {
					knightSquareMoves.add(sqIndex + Compass.directions[dirIndex]);
				}
				knightMoves[sqIndex] = knightSquareMoves.stream().mapToInt(i -> i.intValue()).toArray();
			}
		}
	}

	public static int getStartIndex(int piece) {
		int type = piece & Chess.TYPE_MASK;
		return (type == Chess.BISHOP) ? BishopStartIndex : (type == Chess.KNIGHT) ? KnightStartIndex : RookStartIndex;
	}

	public static int getEndIndex(int piece) {
		int type = piece & Chess.TYPE_MASK;
		return (type == Chess.KNIGHT) ? directions.length : (type == Chess.ROOK) ? BishopStartIndex : KnightStartIndex;
	}

	// TEST FUNCTION
	// prints the edge distance of each square in each direction
	public static void printEdgeDistance() {
		for (int dirIndex = 0; dirIndex < Compass.directions.length; dirIndex++) {
			System.out.println(Compass.directions[dirIndex]);
			for (int ranks = 7; ranks >= 0; ranks--) {
				for (int files = 0; files <= 7; files++) {
					System.out.print(numSquaresToEdge64x16[Coordinate.getSquare(ranks, files)][dirIndex]);
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	// TEST FUNCTION
	// prints the number of available moves for a knight on each square
	public static void printKnightMoves() {
		for (int ranks = 7; ranks >= 0; ranks--) {
			for (int files = 0; files <= 7; files++) {
				System.out.print(knightMoves[Coordinate.getSquare(ranks, files)].length);
			}
			System.out.println();
		}
	}

}
