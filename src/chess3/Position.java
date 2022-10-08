package chess3;

public class Position {

	final static int flagEXACT = 0; // eval is exact value
	final static int flagALPHA = 1; // eval is maximum value
	final static int flagBETA = 2; // eval is minimum value

	long key;
	int depth;
	int flag;
	float eval;
	Move bestmove;

	Position(long zobrist, int depthRemaining, int isExactScore, float score, Move m) {
		key = zobrist;
		depth = depthRemaining;
		flag = isExactScore;
		eval = score;
		bestmove = m;
	}

}
