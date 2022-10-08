package chess3;

import java.util.ArrayList;
import java.util.Random;

public class CCPPlayer extends Player {
	private Chess ch;
	private Random rando = new Random();

	public CCPPlayer(Chess chess) {
		super(chess);
		this.ch = chess;
	}

	// CCP prefers moves that are captures, checks, or pawn pushes, in that order.
	@Override
	public Move getMove() {
		MoveGenerator mgen = new MoveGenerator(ch);
		if (ch.gameOver)
			return null;
		ArrayList<Move> capture = new ArrayList<Move>(), check = new ArrayList<Move>(), push = new ArrayList<Move>();
		for (Move m : mgen.moves) {
			// captures
			if (!ch.isEmptySquare(m.end) || m.isEnPassant) {
				capture.add(m);
				continue;
			}
			// checks
			ch.makeHistory(m);
			MoveGenerator checkmgen = new MoveGenerator(ch);
			if (checkmgen.inCheck) {
				check.add(m);
				ch.unmakeMove();
				continue;
			}
			ch.unmakeMove();
			// pawn pushes
			if (Piece.type(ch.board[m.start]) == Chess.PAWN) {
				push.add(m);
				continue;
			}
		}
//		System.out.println("Moves found: " + mgen.moves.size() + ", captures found: " + capture.size()
//				+ ", checks found: " + check.size() + ", pushes found: " + push.size());
		if (capture.size() > 0) {
			return capture.get(rando.nextInt(capture.size()));
		}
		if (push.size() > 0) {
			return push.get(rando.nextInt(push.size()));
		}
		if (check.size() > 0) {
			return check.get(rando.nextInt(check.size()));
		}
		return mgen.moves.get(rando.nextInt(mgen.moves.size()));
	}

	@Override
	public Move getMove(int depth) throws InterruptedException {
		// TODO Auto-generated method stub
		return getMove();
	}

}
