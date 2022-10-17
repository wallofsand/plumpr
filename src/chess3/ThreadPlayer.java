package chess3;

public class ThreadPlayer implements Runnable {
    boolean searching = false;
    ChessFrame frame;
    int colourIndex;

    ThreadPlayer(ChessFrame source, int side) {
        super();
        this.frame = source;
        this.colourIndex = side;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            if (!frame.chess.gameOver && frame.chess.activeColourIndex == colourIndex
                    && searching == false && frame.ai_can_move(colourIndex))
                try {
                    searching = true;
                    Move m = frame.gb.computerPlayer.getMove(frame.sliderValue);
                    if (m != null) {
                        frame.gb.computerMove(m);
                    }
                    searching = false;
                } catch (InterruptedException e) {
                    if (!searching)
                        e.printStackTrace();
                    else {
                        searching = false;
                        continue;
                    }
                }
        }
    }
}
