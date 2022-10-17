package chess3;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ChessFrame extends JFrame implements ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 5877949543883762268L;
    Chess chess;
    JRadioButton queen = new JRadioButton("Queen"), rook = new JRadioButton("Rook"),
            bishop = new JRadioButton("Bishop"), knight = new JRadioButton("Knight");
    // Zobrist hashing counters
    JLabel clashCounter = new JLabel("Clashes: ", JLabel.CENTER);
    JLabel hitCounter = new JLabel(" Hits: ", JLabel.CENTER);
    // w/b/d for sim
    int wld[] = { 0, 0, 0 };
    JLabel scoreboard = new JLabel(String.format(" Score: %d/%d/%d", wld[0], wld[1], wld[2]), JLabel.CENTER);
    JLabel zCounter = new JLabel(" Size: ", JLabel.CENTER);
    GameBoard gb;
    JLabel playerList;
    JTextArea log, gameHistory;
    JSlider depthSlider;
    int sliderValue;
    ArrayList<Thread> tls = new ArrayList<Thread>();
    boolean searching;
    boolean isSim;
    TranspositionTable ttable = new TranspositionTable(TranspositionTable.DEFAULT_SIZE);
    Player zob = new ZobristPlayer(chess, ttable);
    Player que = new QuiescencePlayer(chess, ttable);
    Player players[] = { que, zob };
    final int SIM_DEPTH = 5;

    public int humanarg;

    public ChessFrame(Chess ch, boolean doSim, int args) throws HeadlessException {
        super("Chess Game");
        this.chess = ch;
        this.isSim = doSim;
        this.humanarg = args;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        // This frame has two panels:
        // a GameBoard and an options menu
        // Setting up the options menu
        JPanel options = new JPanel();
        JPanel logbox = new JPanel();
        logbox.setLayout(new BoxLayout(logbox, BoxLayout.X_AXIS));
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        ButtonGroup promotionTypes = new ButtonGroup();
        JButton unmakeMove = new JButton("Undo"), newGame = new JButton("New Game"),
                computerMove = new JButton("Computer Move");
        log = new JTextArea(15, 80);
        Font logfont = new Font("Consolas", Font.PLAIN, log.getFont().getSize());
        log.setFont(logfont);
        gameHistory = new JTextArea(15, 10);
        gameHistory.setLineWrap(false);
        gameHistory.setWrapStyleWord(false);
        JScrollPane logScrollPane = new JScrollPane(log);
        JScrollPane PGNScrollPane = new JScrollPane(gameHistory);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        PGNScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        PGNScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        log.setEditable(false);
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                String str = String.valueOf((char) b);
                log.append(str);
                log.setCaretPosition(log.getDocument().getLength());
            }
        }));
        gameHistory.setEditable(false);
        // Setting up the depth slider
        JLabel sliderLabel = new JLabel("Search Depth", JLabel.CENTER);
        depthSlider = new JSlider(SwingConstants.HORIZONTAL, 0, 10, 0);
        depthSlider.setMajorTickSpacing(1);
        depthSlider.setPaintTicks(true);
        depthSlider.setPaintLabels(true);
        if (!doSim) {
            depthSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    sliderValue = depthSlider.getValue();
                }
            });
            // Add ActionListeners for the undo/redo buttons
            unmakeMove.addActionListener(this);
            if (humanarg == -1 && !doSim) {
                computerMove.addActionListener(this);
            }
        }
        newGame.addActionListener(this);
        // Setting up the GameBoard
        gb = new GameBoard(this);
        gb.setMinimumSize(getPreferredSize());
        // Add buttons to the ButtonGroup
        promotionTypes.add(queen);
        promotionTypes.add(rook);
        promotionTypes.add(bishop);
        promotionTypes.add(knight);
        // Add Simboard
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JPanel simBoard = new JPanel();
        simBoard.setLayout(new BoxLayout(simBoard, BoxLayout.Y_AXIS));
        playerList = new JLabel("White: \nBlack: ");
        simBoard.add(playerList);
        simBoard.add(scoreboard);
        // Add JRadioButtons to the options panel
        options.add(queen);
        options.add(rook);
        options.add(bishop);
        options.add(knight);
        queen.setSelected(true);
        // Add JButtons to the option panel
        options.add(unmakeMove);
        if (humanarg == -1 && !doSim) {
            options.add(computerMove);
        }
        JPanel botPanel = new JPanel();
        botPanel.setLayout(new BoxLayout(botPanel, BoxLayout.Y_AXIS));
        botPanel.add(sliderLabel);
        botPanel.add(depthSlider);
        botPanel.add(logbox);
        logbox.add(logScrollPane);
        logbox.add(PGNScrollPane);
        botPanel.add(newGame);

        JButton perft = new JButton("perft");
        perft.addActionListener(this);
        options.add(perft);

        JPanel hashTracker = new JPanel();
        hashTracker.setLayout(new BoxLayout(hashTracker, BoxLayout.X_AXIS));
        hashTracker.add(clashCounter);
        hashTracker.add(hitCounter);
        hashTracker.add(zCounter);
        botPanel.add(hashTracker);

        // Add the panels to the frame
        this.add(gb);
        topPanel.add(simBoard);
        topPanel.add(options);
        this.add(topPanel);
        this.add(botPanel);
        // Pack and display the window
        pack();
        this.setResizable(false);
        setVisible(true);

        if (doSim) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        sim();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            tls.add(t);
            t.start();
        }
        if (humanarg != -1) {
            playChess();
        }
    }

    public void playChess() {
        int colourIndex = humanarg == 2 ? Zobrist.zRand.nextInt(2) : ~humanarg & 1;
        Thread t = new Thread(new ThreadPlayer(this, colourIndex), "play_chess");
        tls.add(t);
        t.start();
    }

    private void sim() throws InterruptedException, IOException {
    // create a new file with specified file name
    FileWriter fw = new FileWriter("sim.log");
        int go = 0;
        while (wld[0] + wld[1] + wld[2] < 10) {
            for (Player p : players)
                p.setChess(chess);
//            Zobrist.setHits(0);
//            Zobrist.setWrites(0);
            while (!chess.gameOver) {
                updateProgress();
                Move aim = players[chess.activeColourIndex].getMove(SIM_DEPTH);
                if (chess.getActiveColour() == Chess.WHITE || chess.moveHistory.size() == 0) {
                    // Print the turn number
                    if (chess.getActiveColour() == Chess.WHITE)
                        gameHistory.append(Integer.toString(chess.moveHistory.size() / 2 + 1) + ". ");
                    else
                        gameHistory.append(Integer.toString(chess.moveHistory.size() / 2 + 1) + "... ");
                }
                // Print the chosen move
                gameHistory.append(chess.moveText(aim));
                if (chess.activeColourIndex == Chess.WHITE_INDEX)
                    gameHistory.append(" ");
                else
                    gameHistory.append("\n");
                chess.makeHistory(aim);
                go = (new MoveGenerator(chess)).gameOver();
                if (go != -1) {
                    System.out.println();
                    System.out.println("GAME OVER: " + go);
                }
            }
            updateProgress();
            if (go != 0 && go != 1) // draw
                wld[2]++;
            else // white/black checkmate
                wld[go]++;
            // write a string into the IO stream
            fw.write((wld[0] + wld[1] + wld[2]) + " -- " + go);
            fw.write(chess.printBoard(Chess.WHITE) + "\n");
            chess.restart();
        }
        // don't forget to close the stream!
        fw.close();
    }

    public void updateProgress() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Here, we can safely update the GUI
                // because we'll be called from the
                // event dispatch thread
                gb.repaint();
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "New Game":
                chess.gameOver = true;
                for (Thread t : tls) {
                    if (t != null)
                        t.interrupt();
                }
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        new NewGameDialog(Chess.DEFAULT_FEN).setVisible(true);
                    }
                });
                System.err.println(gameHistory.getText());
                this.dispose();
                break;
            case "Undo":
                if (searching) {
                    for (Thread t : tls) {
                        if (t != null)
                            t.interrupt();
                    }
                }
                searching = true;
                for (int i = 0; i < 2; i++) {
                    if (humanarg == -1)
                        i++;
                    // delete the last move
                    while (!gameHistory.getText().isEmpty()) {
                        gameHistory.replaceRange(null, gameHistory.getText().length() - 1,
                                gameHistory.getText().length());
                        if (gameHistory.getText().endsWith(" ") || gameHistory.getText().endsWith("\n"))
                            break;
                    }
                    // delete the turn number too
                    if (chess.getActiveColour() == Chess.BLACK)
                        while (!gameHistory.getText().isEmpty()) {
                            gameHistory.replaceRange(null, gameHistory.getText().length() - 1,
                                    gameHistory.getText().length());
                            if (gameHistory.getText().endsWith(" ") || gameHistory.getText().endsWith("\n"))
                                break;
                        }
                    chess.unmakeMove();
                }
                gb.getPieceObjects();
                updateProgress();
                gb.computerPlayer.outofbook = false;
                searching = false;
                break;
            case "Computer Move":
                if (searching) break;
                JButton source = (JButton) e.getSource();
                Thread aim = new Thread(new Runnable() {
                    public void run() {
                        try {
                            source.setText("Stop Search");
                            source.setActionCommand("Stop Search");
                            searching = true;
                            Move m = gb.computerPlayer.getMove(sliderValue);
                            if (m != null) {
                                gb.computerMove(m);
                            }
                            searching = false;
                            source.setText("Computer Move");
                            source.setActionCommand("Computer Move");
                        } catch (InterruptedException e1) {
                            log.append("\nSearch Canceled\n");
                            log.setCaretPosition(log.getDocument().getLength());
                            searching = false;
                            source.setText("Computer Move");
                            source.setActionCommand("Computer Move");
                        }
                        updateProgress();
                    }
                });
                tls.add(aim);
                aim.start();
                break;
            case "Stop Search":
                for(Thread t : tls){if (t != null)
                    t.interrupt();
                break;}
            case "perft":
                if (searching) break;
                searching = true;
                aim = new Thread(new Runnable() {
                    public void run() {
                        gb.computerPlayer.perftRoot(sliderValue);
                        updateProgress();
                        searching = false;
                    }
                });
                aim.start();
                break;
        }
    }

    public boolean ai_can_move(int ai_colour_index) {
        if (!searching && !chess.gameOver && chess.activeColourIndex == ai_colour_index) return true;
        return false;
    }

    public void computerMove(Player ai, Chess ch, JButton source, int ai_colour_index) {
        if (!ai_can_move(ai_colour_index)) return;
        updateProgress();
        try {
            source.setText("Stop Search");
            source.setActionCommand("Stop Search");
            searching = true;
            Move m = gb.computerPlayer.getMove(sliderValue);
            if (m != null) {
                gb.computerMove(m);
            }
            searching = false;
            source.setText("Computer Move");
            source.setActionCommand("Computer Move");
        } catch (InterruptedException e1) {
            log.append("\nSearch Canceled\n");
            log.setCaretPosition(log.getDocument().getLength());
            searching = false;
            source.setText("Computer Move");
            source.setActionCommand("Computer Move");
        }
        updateProgress();
    }

    public int getPromotion() {
        if (knight.isSelected())
            return Chess.KNIGHT;
        if (bishop.isSelected())
            return Chess.BISHOP;
        if (rook.isSelected())
            return Chess.ROOK;
        return Chess.QUEEN;
    }

}
