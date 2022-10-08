package chess3;

public class Move {

	public int start;
	public int end;
	public boolean isEnPassant = false;
	public boolean isCastle = false;
	public int promoteType = 0;

	public Move(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public Move(int start, int end, int pieceType) {
		this.start = start;
		this.end = end;
		this.promoteType = pieceType;
	}

	public Move(int start, int end, String flag) {
		this.start = start;
		this.end = end;
		if (flag == "castle")
			this.isCastle = true;
		else if (flag == "ep")
			this.isEnPassant = true;
	}

	// constructor to generator a Move object using console string input
	// formatted as: a1a2, b3c4, etc.
	// a7a8=N promotes to a knight. =Q, =R, =B, etc.
	// case insensitive
	public Move(String sMove) {
		String sStart = sMove.substring(0, 2);
		String sEnd = sMove.substring(2, 4);
		this.start = new Coordinate(sStart).getSquare();
		this.end = new Coordinate(sEnd).getSquare();
		if (sMove.length() == 6) {
			switch (Character.toUpperCase(sMove.charAt(sMove.length() - 1))) {
			case 'N':
				promoteType = Chess.KNIGHT;
				break;
			case 'B':
				promoteType = Chess.BISHOP;
				break;
			case 'R':
				promoteType = Chess.ROOK;
				break;
			case 'Q':
				promoteType = Chess.QUEEN;
				break;
			}
		}
	}

	@Override
	public String toString() {
		if (isCastle) {
			if (end > start)
				return "O-O";
			else
				return "O-O-O";
		}
		return new Coordinate(start).toString() + new Coordinate(end).toString();
	}

}
