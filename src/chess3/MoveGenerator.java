package chess3;

import java.util.ArrayList;

public class MoveGenerator {

	// --- Instance Variables ---
	Chess ch;
	ArrayList<Move> moves;
	int activeColour;
	int opColour;
	int activeColourIndex;
	int opColourIndex;
	int epSquare;
	int[] kingSquares; // initialized in getKingAttackMasks();
	int castleRights;
	int checkSquare;
	boolean inCheck;
	boolean inDoubleCheck;
	boolean pinsExist;
	// piece bitboards
	long bbOccupied;
	long bbWhite;
	long bbBlack;
	long[] bbByColour;
	long bbPawns;
	long bbKnights;
	long bbBishops;
	long bbRooks;
	long bbQueens;
	long bbKings;
	long[] bbByType;
	// attack maps
	long opAttackMask; // every square an op's piece is attacking
	long opSlidingAttackMask; // as above, sliding pieces only
	long opPawnAttacks; // as above, pawns only
	long opKnightAttacks; // as above, knights only
	long[] kingAttackMasks; // each of the up to 8 squares around each king
	long checkRayMask; // if any checks exists from a ray attack, this stores that ray
	long pinRayMask; // if any pins exist, this stores those rays
	int gameOver = -1;

	MoveGenerator(Chess chess) {
		this.ch = chess;
		this.activeColourIndex = ch.activeColourIndex;
		this.activeColour = activeColourIndex << 3;
		this.opColourIndex = ~activeColourIndex & 1;
		this.opColour = opColourIndex << 3;
		this.castleRights = ch.castlingRights;
		genBitboards();
		getKingAttackMasks();
		getOpAttackMask();
		checkCheck();
		findPins();
		// we must genMoves before gameOver becuase gameOver counts number of moves to
		// determine stalemate/checkmate
		this.moves = genMoves();
		this.gameOver = gameOver();
	}

	private ArrayList<Move> genMoves() {
		ArrayList<Move> allMoves = new ArrayList<Move>();
//		ArrayList<Move> kingMoves = new ArrayList<Move>();
//		ArrayList<Move> queenMoves = new ArrayList<Move>();
//		ArrayList<Move> slidingMoves = new ArrayList<Move>();
//		ArrayList<Move> knightMoves = new ArrayList<Move>();
//		ArrayList<Move> pawnMoves = new ArrayList<Move>();
		for (int sq = 0; sq < 64; sq++) {
			if (Bitboard.containsSquare(bbByColour[activeColourIndex], sq)) {
				int piece = ch.board[sq];
				switch (Piece.type(piece)) {
				case Chess.PAWN:
					if (!inDoubleCheck)
						allMoves.addAll(getPawnPieceMoves(sq));
					break;
				case Chess.KNIGHT:
					if (!inDoubleCheck)
						allMoves.addAll(getKnightPieceMoves(sq));
					break;
				case Chess.QUEEN:
					if (!inDoubleCheck)
						allMoves.addAll(getSlidingPieceMoves(sq));
					break;
				case Chess.BISHOP, Chess.ROOK:
					if (!inDoubleCheck)
						allMoves.addAll(getSlidingPieceMoves(sq));
					break;
				case Chess.KING:
					allMoves.addAll(getKingPieceMoves(sq));
					break;
				default:
					throw new IllegalArgumentException("getPieceMoves: unknown piece type!");
				}
			}
		}
//		allMoves.addAll(kingMoves);
//		allMoves.addAll(queenMoves);
//		allMoves.addAll(slidingMoves);
//		allMoves.addAll(knightMoves);
//		allMoves.addAll(pawnMoves);
		return allMoves;
	}

	private ArrayList<Move> getPawnPieceMoves(int startSquare) {
		ArrayList<Move> pawnPieceMoves = new ArrayList<Move>();
		// if the pawn is on this rank, it can double move
		int startingRank = 5 * activeColourIndex + 1;
		// the directions this pawn can capture in
		int[] pAttacks = Compass.pAttacks[activeColourIndex];
		int pDir = Compass.pMoves[activeColourIndex];
		int sqEnd;
		// single moves:
		sqEnd = startSquare + pDir;
		if (isEmptySquare(sqEnd)) {
			// pinned pawns can only move along the pin ray
			if (!pinsExist || !Bitboard.containsSquare(pinRayMask, startSquare)
					|| pDir == Compass.getRay(kingSquares[activeColourIndex], startSquare)
					|| pDir == -Compass.getRay(kingSquares[activeColourIndex], startSquare)) {
				// promotions:
				if (Coordinate.rankIndex(sqEnd) == 7 || Coordinate.rankIndex(sqEnd) == 0) {
					pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.QUEEN));
					pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.KNIGHT));
					pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.ROOK));
					pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.BISHOP));
				} else
					// non-promotions:
					pawnPieceMoves.add(new Move(startSquare, sqEnd));
				// double moves:
				sqEnd = startSquare + 2 * pDir;
				if (Coordinate.rankIndex(startSquare) == startingRank && isEmptySquare(sqEnd)) {
					pawnPieceMoves.add(new Move(startSquare, sqEnd));
				}
			}
		}
		// captures:
		// Eastern captures:
		if (Coordinate.fileIndex(startSquare) != 0) {
			sqEnd = startSquare + pAttacks[0];
			if (Bitboard.containsSquare(bbByColour[opColourIndex], sqEnd) || ch.board[sqEnd] == Chess.EN_PASSANT) {
				// pinned pawns can only capture along the pin ray
				if (!pinsExist || !Bitboard.containsSquare(pinRayMask, startSquare)
						|| pAttacks[0] == Compass.getRay(kingSquares[activeColourIndex], startSquare)) {
					// promotions:
					if (Coordinate.rankIndex(sqEnd) == 7 || Coordinate.rankIndex(sqEnd) == 0) {
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.QUEEN));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.KNIGHT));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.ROOK));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.BISHOP));
					} else
					// non-promotions:
					if (ch.board[sqEnd] == Chess.EN_PASSANT) {
						pawnPieceMoves.add(new Move(startSquare, sqEnd, "ep"));
					} else
						pawnPieceMoves.add(new Move(startSquare, sqEnd));
				}
			}
		}
		// Western captures:
		if (Coordinate.fileIndex(startSquare) != 7) {
			sqEnd = startSquare + pAttacks[1];
			if (Bitboard.containsSquare(bbByColour[opColourIndex], sqEnd) || ch.board[sqEnd] == Chess.EN_PASSANT) {
				// pawns can only capture along the pin ray
				if (!pinsExist || !Bitboard.containsSquare(pinRayMask, startSquare)
						|| pAttacks[1] == Compass.getRay(kingSquares[activeColourIndex], startSquare)) {
					// promotions:
					if (Coordinate.rankIndex(sqEnd) == 7 || Coordinate.rankIndex(sqEnd) == 0) {
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.QUEEN));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.KNIGHT));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.ROOK));
						pawnPieceMoves.add(new Move(startSquare, sqEnd, Chess.BISHOP));
					} else
					// non-promotions:
					if (ch.board[sqEnd] == Chess.EN_PASSANT) {
						pawnPieceMoves.add(new Move(startSquare, sqEnd, "ep"));
					} else
						pawnPieceMoves.add(new Move(startSquare, sqEnd));
				}
			}
		}
		if (inCheck) {
			if (Bitboard.containsSquare(bbPawns & bbByColour[opColourIndex], checkSquare) && epSquare > 0) {
				pawnPieceMoves.removeIf(m -> (m.end != checkSquare && m.end != epSquare));
			} else
				pawnPieceMoves.removeIf(m -> (!Bitboard.containsSquare(checkRayMask, m.end)));
		}
		return pawnPieceMoves;
	}

	private ArrayList<Move> getKnightPieceMoves(int sq) {
		ArrayList<Move> knightPieceMoves = new ArrayList<Move>();
		// a pinned knight can never move (it can't move along the ray)
		if (pinsExist && Bitboard.containsSquare(pinRayMask, sq)) {
			return knightPieceMoves;
		}
		int[] endSquares = Compass.knightMoves[sq];
		for (int sqEnd : endSquares) {
			if (!Bitboard.containsSquare(bbByColour[activeColourIndex], sqEnd)) {
				knightPieceMoves.add(new Move(sq, sqEnd));
			}
		}
		if (inCheck) {
			knightPieceMoves.removeIf(m -> (!Bitboard.containsSquare(checkRayMask, m.end)));
		}
		return knightPieceMoves;
	}

	// method to get the moves of a single sliding pieces (B, R, Q)
	private ArrayList<Move> getSlidingPieceMoves(int startSquare) {
		ArrayList<Move> slidingPieceMoves = new ArrayList<Move>();
		int startIndex = Compass.getStartIndex(ch.board[startSquare]);
		int endIndex = Compass.getEndIndex(ch.board[startSquare]);
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			int ed = Compass.numSquaresToEdge64x16[startSquare][dirIndex];
			int dir = Compass.directions[dirIndex];
			int targetSquare = startSquare;
			if (!pinsExist || !Bitboard.containsSquare(pinRayMask, startSquare)
					|| dir == Compass.getRay(kingSquares[activeColourIndex], startSquare)
					|| -dir == Compass.getRay(kingSquares[activeColourIndex], startSquare)) {
				for (int step = 1; step <= ed; step++) {
					targetSquare += dir;
					if (isEmptySquare(targetSquare)
							|| Bitboard.containsSquare(bbByColour[opColourIndex], targetSquare)) {
						slidingPieceMoves.add(new Move(startSquare, targetSquare));
					}
					if (Bitboard.containsSquare(bbOccupied, targetSquare)) {
						break;
					}
				}
			}
		}
		if (inCheck) {
			slidingPieceMoves.removeIf(m -> (!Bitboard.containsSquare(checkRayMask, m.end)));
		}
		return slidingPieceMoves;
	}

	private ArrayList<Move> getKingPieceMoves(int ksq) {
		ArrayList<Move> kingPieceMoves = new ArrayList<Move>();
		// non-castling
		long kingMoves = kingAttackMasks[activeColourIndex] & ~bbByColour[activeColourIndex] & ~opAttackMask;
		int startIndex = Compass.getStartIndex(Chess.KING);
		int endIndex = Compass.getEndIndex(Chess.KING);
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			int targetSquare = ksq + Compass.directions[dirIndex];
			if (Bitboard.containsSquare(kingMoves, targetSquare))
				kingPieceMoves.add(new Move(ksq, targetSquare));
		}
		// castling
		int castle = castleRights >> (activeColourIndex << 1);
		if (!inCheck && castle != 0) {
			long activeRooks = bbRooks & bbByColour[activeColourIndex];
			// castling kingside
			if ((castle & 1) == 1 && Bitboard.containsSquare(activeRooks, ksq + 3)
					&& !Bitboard.containsSquare(opAttackMask, ksq + 1) && !Bitboard.containsSquare(bbOccupied, ksq + 1)
					&& !Bitboard.containsSquare(opAttackMask, ksq + 2)
					&& !Bitboard.containsSquare(bbOccupied, ksq + 2)) {
				kingPieceMoves.add(new Move(ksq, ksq + 2, "castle"));
			}
			// castling queenside
			if ((castle & 2) == 2 && Bitboard.containsSquare(activeRooks, ksq - 4)
					&& !Bitboard.containsSquare(opAttackMask, ksq - 1) && !Bitboard.containsSquare(bbOccupied, ksq - 1)
					&& !Bitboard.containsSquare(opAttackMask, ksq - 2) && !Bitboard.containsSquare(bbOccupied, ksq - 2)
					&& !Bitboard.containsSquare(bbOccupied, ksq - 3)) {
				kingPieceMoves.add(new Move(ksq, ksq - 2, "castle"));
			}
		}
		return kingPieceMoves;
	}

	// setup method
	private void genBitboards() {
		// bitboards of pieces by piece type, colour, side
		long occupied = 0;
		long white = 0;
		long black = 0;
		long pawns = 0;
		long knights = 0;
		long bishops = 0;
		long rooks = 0;
		long queens = 0;
		long kings = 0;
		for (int i = 0; i < 64; i++) {
			// uses Board.isEmptySquare() because MoveGenerator.isEmptySquare()
			// uses bitboards that have not been generated yet
			if (!ch.isEmptySquare(i)) {
				// all pieces
				occupied |= 1L << i;
				// colour
				switch (Piece.colour(ch.board[i])) {
				case Chess.WHITE:
					white |= 1L << i;
					break;
				case Chess.BLACK:
					black |= 1L << i;
					break;
				}
				// piece type
				switch (Piece.type(ch.board[i])) {
				case Chess.PAWN:
					pawns |= 1L << i;
					break;
				case Chess.KNIGHT:
					knights |= 1L << i;
					break;
				case Chess.BISHOP:
					bishops |= 1L << i;
					break;
				case Chess.ROOK:
					rooks |= 1L << i;
					break;
				case Chess.QUEEN:
					queens |= 1L << i;
					break;
				case Chess.KING:
					kings |= 1L << i;
					break;
				}
			}
			if (ch.board[i] == Chess.EN_PASSANT) {
				this.epSquare = i;
			}
		}
		// update the class fields
		this.bbOccupied = occupied;
		this.bbWhite = white;
		this.bbBlack = black;
		this.bbByColour = new long[] { this.bbWhite, this.bbBlack };
		this.bbPawns = pawns;
		this.bbKnights = knights;
		this.bbBishops = bishops;
		this.bbRooks = rooks;
		this.bbQueens = queens;
		this.bbKings = kings;
		this.bbByType = new long[] { 0L, this.bbPawns, this.bbKnights, this.bbBishops, this.bbRooks, this.bbQueens,
				this.bbKings };
	}

	public void checkCheck() {
		// find the active king
		int ksq = kingSquares[activeColourIndex];
		// knights
		int[] knightMoves = Compass.knightMoves[ksq];
		for (int knightIndex = 0; knightIndex < knightMoves.length; knightIndex++) {
			if (Bitboard.containsSquare(bbKnights & bbByColour[opColourIndex], knightMoves[knightIndex])) {
				checkMethod(knightMoves[knightIndex]);
			}
		}
		// pawns
		// uses the attack direction of friendly pawns
		// to represent the opposite of an enemy pawn move direction
		int[] pawnAttacks = Compass.pAttacks[activeColourIndex];
		long pawns = bbPawns & bbByColour[opColourIndex];
		for (int pawnIndex = 0; pawnIndex < pawnAttacks.length; pawnIndex++) {
			int ed = Compass.numSquaresToEdge64x16[ksq][Compass.dirIndexFromDirection(pawnAttacks[pawnIndex])];
			if (ed > 0) {
				int targetSquare = ksq + pawnAttacks[pawnIndex];
				if (Bitboard.containsSquare(pawns, targetSquare)) {
					checkMethod(targetSquare);
				}
			}
		}
		// sliding pieces
		long bishops = bbByColour[opColourIndex] & (bbBishops | bbQueens);
		long rooks = bbByColour[opColourIndex] & (bbRooks | bbQueens);
		int startIndex = 0;
		int endIndex = Compass.KnightStartIndex;
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			int ed = Compass.numSquaresToEdge64x16[ksq][dirIndex];
			int direction = Compass.directions[dirIndex];
			int type = Compass.getSliderFromDirection(direction);
			for (int step = 1; step <= ed; step++) {
				int targetSquare = ksq + step * direction;
				if (Bitboard.containsSquare(bbOccupied, targetSquare)) {
					if (type == Chess.BISHOP) {
						if (Bitboard.containsSquare(bishops, targetSquare)) {
							checkMethod(targetSquare);
						}
					}
					if (type == Chess.ROOK) {
						if (Bitboard.containsSquare(rooks, targetSquare)) {
							checkMethod(targetSquare);
						}
					}
					break;
				}
			}
		}
		// if there is single check, find the ray that the check exists upon
		if (inCheck && !inDoubleCheck) {
			int checkPiece = Piece.type(ch.board[checkSquare]);
			if (checkPiece == Chess.KNIGHT || checkPiece == Chess.PAWN) {
				checkRayMask |= 1L << checkSquare;
			} else {
				int dir = Compass.getRay(checkSquare, ksq);
				for (int index = checkSquare; index != ksq; index += dir) {
					checkRayMask |= 1L << index;
				}
			}
		}
	}

	// Method to record check. used as a separate method to print debug info
	public void checkMethod(int sq) {
		if (inCheck) {
			inDoubleCheck = true;
//			System.out.println("inDoubleCheck : " + inDoubleCheck + " at: " + sq);
		} else {
			inCheck = true;
			checkSquare = sq;
//			System.out.println("inCheck : " + inCheck + " at: " + sq);
		}
	}

	// setup method to get pins in a position
	// returns a bitboard of squares containing pinning pieces
	// called once per position, for the active player only
	public void findPins() {
		// check each ray that contains the king square
		// look for rays that pass an allied piece,
		// then a relevent sliding piece
		long pins = 0;
		// find the active king
		int kSq = kingSquares[activeColourIndex];
		// check each ray for a pin sequence
		int startIndex = 0;
		int endIndex = Compass.KnightStartIndex;
		int[] ed = Compass.numSquaresToEdge64x16[kSq];
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			long pinRay = 0;
			int dir = Compass.directions[dirIndex];
			// determine the type of piece that could pin along this ray
			int type = Compass.getSliderFromDirection(dir);
			byte state = 0;
			for (int step = 1; step <= ed[dirIndex]; step++) {
				int raySq = kSq + step * dir;
				pinRay |= 1L << raySq;
				// check if there is a friendly piece on the ray
				if (state == 0 && Bitboard.containsSquare(bbByColour[activeColourIndex], raySq)) {
					state = 1;
				}
				// check if there is a piece of this type on the ray
				else if (state == 1
						&& Bitboard.containsSquare((bbByType[type] | bbQueens) & bbByColour[opColourIndex], raySq)) {
					pins |= pinRay;
//					System.out.println("Pin found on: " + raySq);2
//					b1.printBoard(Chess.White);
					break;
				} else if (Bitboard.containsSquare(bbOccupied, raySq)) {
					break;
				}
			}
		}
		if (pins == 0)
			this.pinsExist = false;
		else
			this.pinsExist = true;
		this.pinRayMask = pins;

//		System.out.println("pinsExist: " + pinsExist);
//		if (pinsExist)
//			Bitboard.printBinaryString(pinRayMask);
	}

	// setup method to find squares threatened by opponent's pieces
	public void getOpAttackMask() {
		long attacks = 0;
		// step through enemy colour bitboard
		for (int sq = 0; sq < 64; sq++) {
			if (Bitboard.containsSquare(bbByColour[opColourIndex], sq)) {
				attacks |= getOpPieceAttackMask(sq);
			}
		}
		this.opAttackMask = attacks;
	}

	// method to find squares threatened by a given piece of the non-active colour
	public long getOpPieceAttackMask(int sq) {
		long pieceAttacks = 0;
		int piece = ch.board[sq];
		int type = Piece.type(piece);
		switch (type) {
		case Chess.PAWN:
			for (int i = 0; i <= 1; i++) {
				int pDir = Compass.pAttacks[opColourIndex][i];
				int pDirIndex = Compass.dirIndexFromDirection(pDir);
				if (Compass.numSquaresToEdge64x16[sq][pDirIndex] > 0)
					pieceAttacks |= 1L << sq + pDir;
			}
			break;
		case Chess.KNIGHT:
			for (int i = 0; i < Compass.knightMoves[sq].length; i++) {
				pieceAttacks |= 1L << Compass.knightMoves[sq][i];
			}
			break;
		case Chess.BISHOP, Chess.ROOK, Chess.QUEEN:
			pieceAttacks |= getOpSlidingPieceAttackMask(sq);
			break;
		case Chess.KING:
			pieceAttacks |= kingAttackMasks[opColourIndex];
			break;
		default:
			throw new IllegalArgumentException("Invalid piece type! " + type);
		}
		return pieceAttacks;
	}

	// method to find squares threatened by a given sliding piece of the non-active
	// colour. enemy kings cannot block rays during this step.
	private long getOpSlidingPieceAttackMask(int sq) {
		long slidingPieceAttackMask = 0;
		long bbNoKing = bbOccupied & bbByColour[activeColourIndex] & ~bbKings;
		int piece = ch.board[sq];
		int startIndex = Compass.getStartIndex(piece);
		int endIndex = Compass.getEndIndex(piece);
		for (int dirIndex = startIndex; dirIndex < endIndex; dirIndex++) {
			int ed = Compass.numSquaresToEdge64x16[sq][dirIndex];
			for (int step = 1; step <= ed; step++) {
				int offset = sq + Compass.directions[dirIndex] * step;
				if (Bitboard.containsSquare(bbNoKing, offset))
					break;
				slidingPieceAttackMask |= 1L << offset;
				if (Bitboard.containsSquare(bbByColour[opColourIndex], offset))
					break;
			}
		}
		return slidingPieceAttackMask;
	}

	// setup method to find squares threatened by kings
	private void getKingAttackMasks() {
		// location of each player's king
		this.kingSquares = new int[2];
		for (int sqIndex = 0; sqIndex < 64; sqIndex++) {
			if (Bitboard.containsSquare(bbKings & bbWhite, sqIndex)) {
				kingSquares[Chess.WHITE_INDEX] = sqIndex;
			}
			if (Bitboard.containsSquare(bbKings & bbBlack, sqIndex)) {
				kingSquares[Chess.BLACK_INDEX] = sqIndex;
			}
		}
		// edge distance at king squares
		int[][] ed = { Compass.numSquaresToEdge64x16[kingSquares[Chess.WHITE_INDEX]],
				Compass.numSquaresToEdge64x16[kingSquares[Chess.BLACK_INDEX]] };
		// mask squares adjacent to kings
		long[] kingAttackMask = new long[2];
		for (int dirIndex = 0; dirIndex < 8; dirIndex++) {
			int dirOffset = Compass.directions[dirIndex];
			// white
			if (ed[Chess.WHITE_INDEX][dirIndex] > 0) {
				kingAttackMask[Chess.WHITE_INDEX] |= 1L << kingSquares[Chess.WHITE_INDEX] + dirOffset;
			}
			// black
			if (ed[Chess.BLACK_INDEX][dirIndex] > 0) {
				kingAttackMask[Chess.BLACK_INDEX] |= 1L << kingSquares[Chess.BLACK_INDEX] + dirOffset;
			}
		}
		this.kingAttackMasks = kingAttackMask;
	}

	boolean isEmptySquare(int sq) {
		return (bbOccupied & (1L << sq)) == 0;
	}

	// Method to determine the end of the game
	// not over = -1, white = 0, black = 1, repetition = 2, material = 3, draw = 4;
	public int gameOver() {
		// draw offer

		// stalemate
		if (this.moves.size() == 0 && !inCheck) {
			ch.gameOver = true;
			return 5;
		}
		// checkmate
		if (this.moves.size() == 0 && inCheck) {
			ch.gameOver = true;
			return opColourIndex;
		}
		// repetition
		int reps = 0;
		// make a test chess class to test repetition
		Chess test = new Chess(ch.startpos);
		moveLoop: for (Move m : ch.moveHistory) {
			test.makeHistory(m);
			for (int sq = 0; sq < 64; sq++) {
				if (test.board[sq] != ch.board[sq])
					continue moveLoop;
			}
			reps++;
		}
		if (reps >= 3) {
			ch.gameOver = true;
			return 2;
		}
		// insufficient material

		return -1;
	}

}
