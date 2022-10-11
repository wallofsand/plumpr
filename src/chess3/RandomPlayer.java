package chess3;

import java.util.Random;

public class RandomPlayer extends Player {
	private Random rando = new Random();
	private Chess ch;
	private MoveGenerator rmgen;

	public RandomPlayer(Chess chess) {
		super("Randy", chess);
		this.ch = chess;
	}

	public Move getMove() {
		rmgen = new MoveGenerator(ch);
		if (ch.gameOver)
			return null;
		return rmgen.moves.get(rando.nextInt(rmgen.moves.size()));
	}

	@Override
	public Move getMove(int depth) throws InterruptedException {
		// TODO Auto-generated method stub
		return getMove();
	}
}
