package chess3;

public abstract class Player {
	
	public Chess ch;
	public boolean outofbook;
	public TranspositionTable ttable;
	String name;

	Player(String playerName, Chess chess) {
		this.name = playerName;
		ch = chess;
		outofbook = false;
	}
	
	public void setChess(Chess chessIn) {
		this.ch = chessIn;
	}
	
	public abstract Move getMove() throws InterruptedException;

	public abstract Move getMove(int depth) throws InterruptedException;
	
}
