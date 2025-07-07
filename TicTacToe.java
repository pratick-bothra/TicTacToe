import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class TicTacToe extends JFrame implements ActionListener {
    private JButton[] buttons = new JButton[9];
    private boolean player1Turn = true;
    private boolean gameActive = true;
    private JLabel statusLabel;
    private JButton restartButton;
    private JButton modeButton;
    private JComboBox<String> difficultyComboBox;
    private boolean vsComputer = false;
    private Random random = new Random();

    private String player1Name = "Player 1";
    private String player2Name = "Player 2";
    private String computerName = "Computer";

    public TicTacToe() {
        setTitle("Tic Tac Toe");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(45, 52, 54)); // Dark background

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 10, 10));
        boardPanel.setBackground(new Color(222, 184, 135)); // Burlywood wood color
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 70);

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton("");
            buttons[i].setFont(buttonFont);
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(new Color(222, 184, 135)); // Burlywood wood color
            buttons[i].setForeground(Color.BLACK);
            buttons[i].setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 3)); // SaddleBrown border
            buttons[i].addActionListener(this);
            buttons[i].setOpaque(true);
            buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            boardPanel.add(buttons[i]);
        }

        statusLabel = new JLabel(player1Name + "'s turn (X)");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        statusLabel.setForeground(new Color(139, 69, 19)); // SaddleBrown color
        statusLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        restartButton = new JButton("Restart");
        restartButton.setBackground(new Color(0, 184, 148));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restartButton.addActionListener(_ -> restartGame());

        modeButton = new JButton("Mode: Player vs Player");
        modeButton.setBackground(new Color(0, 184, 148));
        modeButton.setForeground(Color.WHITE);
        modeButton.setFocusPainted(false);
        modeButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        modeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        modeButton.addActionListener(_ -> toggleMode());

        difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        difficultyComboBox.setSelectedIndex(0);
        difficultyComboBox.setEnabled(false);
        difficultyComboBox.setBackground(new Color(0, 184, 148));
        difficultyComboBox.setForeground(Color.WHITE);
        difficultyComboBox.setFont(new Font("Segoe UI", Font.BOLD, 16));
        difficultyComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        difficultyComboBox.addActionListener(e -> {
            if (vsComputer) {
                restartGame();
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 3, 15, 15));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        controlPanel.setBackground(new Color(45, 52, 54));
        controlPanel.add(restartButton);
        controlPanel.add(modeButton);
        controlPanel.add(difficultyComboBox);

        add(statusLabel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void toggleMode() {
        playSound("mode_switch.wav");
        vsComputer = !vsComputer;
        difficultyComboBox.setEnabled(vsComputer);
        if (vsComputer) {
            modeButton.setText("Mode: Player vs Computer");
            getPlayerNamesForComputerMode();
            restartGame();
        } else {
            modeButton.setText("Mode: Player vs Player");
            getPlayerNamesForPvPMode();
            restartGame();
        }
    }

    private void restartGame() {
        for (JButton button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }
        player1Turn = true;
        gameActive = true;
        updateStatusLabel();
    }

    private boolean checkWin(String playerSymbol) {
        int[][] winPositions = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
            {0, 4, 8}, {2, 4, 6}             // diagonals
        };

        for (int[] pos : winPositions) {
            if (buttons[pos[0]].getText().equals(playerSymbol) &&
                buttons[pos[1]].getText().equals(playerSymbol) &&
                buttons[pos[2]].getText().equals(playerSymbol)) {
                return true;
            }
        }
        return false;
    }

    private boolean boardFull() {
        for (JButton button : buttons) {
            if (button.getText().equals("")) {
                return false;
            }
        }
        return true;
    }

    private void computerMove() {
        if (!gameActive) return;

        String difficulty = (String) difficultyComboBox.getSelectedItem();
        int move = -1;

        if ("Easy".equals(difficulty)) {
            // Random move
            do {
                move = random.nextInt(9);
            } while (!buttons[move].getText().equals(""));
        } else if ("Medium".equals(difficulty)) {
            // Medium difficulty: try to win or block, else random
            move = findWinningMove("O");
            if (move == -1) {
                move = findWinningMove("X"); // block player
            }
            if (move == -1) {
                do {
                    move = random.nextInt(9);
                } while (!buttons[move].getText().equals(""));
            }
        } else if ("Hard".equals(difficulty)) {
            // Hard difficulty: Minimax algorithm
            move = findBestMove();
        }

        buttons[move].setText("O");
        buttons[move].setEnabled(false);
        playSound("click.wav");

        if (checkWin("O")) {
            playSound("win.wav");
            statusLabel.setText(computerName + " wins!");
            gameActive = false;
            disableAllButtons();
        } else if (boardFull()) {
            playSound("draw.wav");
            statusLabel.setText("It's a draw!");
            gameActive = false;
        } else {
            player1Turn = true;
            updateStatusLabel();
        }
    }

    private int findWinningMove(String playerSymbol) {
        for (int i = 0; i < 9; i++) {
            if (buttons[i].getText().equals("")) {
                buttons[i].setText(playerSymbol);
                boolean win = checkWin(playerSymbol);
                buttons[i].setText("");
                if (win) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;
        for (int i = 0; i < 9; i++) {
            if (buttons[i].getText().equals("")) {
                buttons[i].setText("O");
                int score = minimax(false);
                buttons[i].setText("");
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }
        return bestMove;
    }

    private int minimax(boolean isMaximizing) {
        if (checkWin("O")) return 1;
        if (checkWin("X")) return -1;
        if (boardFull()) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (buttons[i].getText().equals("")) {
                    buttons[i].setText("O");
                    int score = minimax(false);
                    buttons[i].setText("");
                    bestScore = Math.max(score, bestScore);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (buttons[i].getText().equals("")) {
                    buttons[i].setText("X");
                    int score = minimax(true);
                    buttons[i].setText("");
                    bestScore = Math.min(score, bestScore);
                }
            }
            return bestScore;
        }
    }

    private void disableAllButtons() {
        for (JButton button : buttons) {
            button.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameActive) return;

        JButton clickedButton = (JButton) e.getSource();

        if (!clickedButton.getText().equals("")) {
            return; // Ignore if already clicked
        }

            if (player1Turn) {
                clickedButton.setText("X");
                clickedButton.setForeground(new Color(0, 0, 255)); // Blue for X
                //clickedButton.setEnabled(false);
                playSound("click.wav");

                if (checkWin("X")) {
                    playSound("win.wav");
                    statusLabel.setText(player1Name + " wins!");
                    gameActive = false;
                    disableAllButtons();
                    return;
                } else if (boardFull()) {
                    playSound("draw.wav");
                    statusLabel.setText("It's a draw!");
                    gameActive = false;
                    return;
                }

                player1Turn = false;

            if (vsComputer) {
                statusLabel.setText(computerName + "'s turn (O)");
                // Delay computer move slightly for better UX
                Timer timer = new Timer(500, _ -> computerMove());
                timer.setRepeats(false);
                timer.start();
            } else {
                statusLabel.setText(player2Name + "'s turn (O)");
            }
        } else {
            // Player 2 turn
            clickedButton.setText("O");
            clickedButton.setForeground(new Color(255, 0, 0)); // Red for O
            //clickedButton.setEnabled(false);
            playSound("click.wav");

            if (checkWin("O")) {
                playSound("win.wav");
                statusLabel.setText(player2Name + " wins!");
                gameActive = false;
                disableAllButtons();
                return;
            } else if (boardFull()) {
                playSound("draw.wav");
                statusLabel.setText("It's a draw!");
                gameActive = false;
                return;
            }

            player1Turn = true;
            statusLabel.setText(player1Name + "'s turn (X)");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TicTacToe game = new TicTacToe();
            game.getPlayerNamesForPvPMode();
            game.setVisible(true);
        });
    }

    private void getPlayerNamesForPvPMode() {
        String p1 = JOptionPane.showInputDialog(this, "Enter Player 1 name:", player1Name);
        if (p1 != null && !p1.trim().isEmpty()) {
            player1Name = p1.trim();
        }
        String p2 = JOptionPane.showInputDialog(this, "Enter Player 2 name:", player2Name);
        if (p2 != null && !p2.trim().isEmpty()) {
            player2Name = p2.trim();
        }
        updateStatusLabel();
    }

    private void getPlayerNamesForComputerMode() {
        String p1 = JOptionPane.showInputDialog(this, "Enter your name:", player1Name);
        if (p1 != null && !p1.trim().isEmpty()) {
            player1Name = p1.trim();
        }
        String aiName = JOptionPane.showInputDialog(this, "Enter AI player name:", computerName);
        if (aiName != null && !aiName.trim().isEmpty()) {
            computerName = aiName.trim();
        }
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (gameActive) {
            if (player1Turn) {
                statusLabel.setText(player1Name + "'s turn (X)");
            } else {
                if (vsComputer) {
                    statusLabel.setText(computerName + "'s turn (O)");
                } else {
                    statusLabel.setText(player2Name + "'s turn (O)");
                }
            }
        }
    }

    private void playSound(String soundFileName) {
        try {
            File soundFile = new File("sounds/" + soundFileName);
            if (!soundFile.exists()) {
                System.err.println("File does not exist: " + soundFile.getAbsolutePath());
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + soundFileName);
            e.printStackTrace(); // Print the full exception for debugging
        }
    }
}

