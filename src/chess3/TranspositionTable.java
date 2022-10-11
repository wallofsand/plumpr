package chess3;

import java.lang.Math;

public class TranspositionTable {

	public final static int DEFAULT_SIZE = 64000;

	private int size;
	Position[] table;

	TranspositionTable(int size) {
		this.size = size;
		this.table = new Position[size];
	}

	// create an entry to store in the table
	Position makePosition(long zobrist, int depthRemaining, int isExactScore, float score, Move m) {
//		Zobrist.incrementWrites();
		table[(int) Math.abs(zobrist % getSize())] = new Position(zobrist, depthRemaining, isExactScore, score, m);
		return table[(int) Math.abs(zobrist % getSize())];
	}

	// return a value stored in the transposition table
	Position getPosition(long hash) {
		return table[(int) Math.abs(hash % getSize())];
	}

	// method to 
	void add(Position newPos) {
		Position p0 = table[(int) Math.abs(newPos.key % getSize())];
		if (p0 == null || p0.key == newPos.key) {
			table[(int) Math.abs(newPos.key % getSize())] = newPos;
//			Zobrist.incrementWrites();
		} else {
			table[(int) Math.abs(newPos.key % getSize())] = newPos;
//			Zobrist.incrementClashes();
//			Zobrist.incrementWrites();
		}
	}

	// Method to get the depth of a position within the ttable
	int getDepth(long hash) {
		if (!containsPosition(hash))
			return -1;
//		if (table[(int) Math.abs(hash % getSize())].key != hash)
//			return -1; // return -1 if there is a clash
		return table[(int) Math.abs(hash % getSize())].depth;
	}

	int getSize() {
		return size;
	}

	boolean containsPosition(long hash) { // should we record clashes here?
		Position p0 = getPosition(hash);
		if (p0 == null) return false;
		else if (p0.key == hash) return true;
//		Zobrist.incrementClashes();
		return false;
//		return getPosition(hash) != null && getPosition(hash).key == hash;
	}

	float probeTable(long hash, int depth, float alpha, float beta) {
		Position p0 = table[(int) Math.abs(hash % getSize())];
		if (p0.depth >= depth) {
			if (p0.flag == Position.flagEXACT)
				return p0.eval;
			else if (p0.flag == Position.flagALPHA && p0.eval <= alpha)
				return alpha;
			else if (p0.flag == Position.flagBETA && p0.eval >= beta)
				return beta;
		}
		return beta;
	}

	Move getMove(long hash) {
		return table[(int) Math.abs(hash % getSize())] != null ? table[(int) Math.abs(hash % getSize())].bestmove
				: null;
	}

}
