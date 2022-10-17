package chess3;

import java.util.Random;

public abstract class Zobrist {

	static Random zRand;

	// private static final int pawnIndex = 0;
	// private static final int knightIndex = 1;
	// private static final int bishopIndex = 2;
	// private static final int rookIndex = 3;
	// private static final int queenIndex = 4;
	// private static final int kingIndex = 5;
	// private static final int blackOffset = 6;
	private static final int castleShort = 0;
	private static final int castleLong = 1;

	private static long clashes = 0;
	private static long hits = 0;
	private static long writes = 0;

	// array of random bitstrings for each piece at each square
	// pawns are special case: first "rank" is used for white ep-able pawns
	// while the second "rank" is used for black's ep-able pawns
	static long[][][] sqColourType = new long[64][2][6];

	// indexed white/black, short/long
	static long[][] castleRights = new long[2][2];

	static long[] epFile = new long[8];

	static long blackToMove;

	static void init() {
		zRand = new Random();
		blackToMove = zRand.nextLong();
		castleRights[Chess.WHITE_INDEX][castleShort] = zRand.nextLong();
		castleRights[Chess.WHITE_INDEX][castleLong] = zRand.nextLong();
		castleRights[Chess.BLACK_INDEX][castleShort] = zRand.nextLong();
		castleRights[Chess.BLACK_INDEX][castleLong] = zRand.nextLong();
		for (int sq = 0; sq < 64; sq++) {
			for (int type = 0; type < 6; type++) {
				sqColourType[sq][Chess.WHITE_INDEX][type] = zRand.nextLong();
				sqColourType[sq][Chess.BLACK_INDEX][type] = zRand.nextLong();
			}
		}
	}

	static long hash(Chess ch) {
		long h = 0;
		// who's turn is it?
		h ^= ch.activeColourIndex * blackToMove;
		// board representation
		for (int sq = 0; sq < 64; sq++)
			if (!ch.isEmptySquare(sq)) // normal pieces
				h ^= sqColourType[sq][ch.board[sq] >>> 3][(ch.board[sq] & 7) - 1];
			else if (ch.board[sq] == Chess.EN_PASSANT) // ep file
				h ^= epFile[sq & 7];
		// Castling rights: black_white
		// queenside rook, kingside rook
		// Chess.castlingRights = 0b11_11;
		if ((ch.castlingRights & 3) != 0) { // white
			h ^= ((ch.castlingRights >>> 1) & 1) * castleRights[Chess.WHITE_INDEX][castleLong];
			h ^= ((ch.castlingRights) & 1) * castleRights[Chess.WHITE_INDEX][castleShort];
		}
		if ((ch.castlingRights & 12) != 0) { // black
			h ^= ((ch.castlingRights >>> 3) & 1) * castleRights[Chess.BLACK_INDEX][castleLong];
			h ^= ((ch.castlingRights >>> 2) & 1) * castleRights[Chess.BLACK_INDEX][castleShort];
		}
		return h;
	}

//	public static long getClashes() {
//		return clashes;
//	}
//
//	public static void incrementClashes() {
//		hits++;
//	}
//
//	public static void decrementClashes() {
//		hits--;
//	}
//
//	public static void setHits(long h) {
//		hits = h;
//	}
//
//   public static void setWrites(long w) {
//        writes = w;
//    }
//   
//    public static void setClashes(long c) {
//        clashes = c;
//    }
//	
//	public static long getHits() {
//		return hits;
//	}
//
//	public static void incrementHits() {
//		hits++;
//	}
//
//	public static void decrementHits() {
//		hits--;
//	}
//
//	public static long getWrites() {
//		return writes;
//	}
//
//	public static void incrementWrites() {
//		writes++;
//	}
//
//	public static void decrementWrites() {
//		writes--;
//	}

}
