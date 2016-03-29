package checkers;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

/**
 * This is the main class for the game of Checkers. This class draws the GUI,
 * and initiates the game for the users. The game requires two players.
 *
 * @author Aaron S
 */
public class Checkers {

    private static JFrame frame;
    private static JPanel primaryPanel;
    private static JPanel board;
    private static HashMap<Integer, Piece> boardLocations = new HashMap<Integer, Piece>();

    public Checkers() {
        createGUI();
       // setBoard(); /*Will uncomment when method definition is finished */
    }

    /**
     * Creates the GUI and displays it for the user
     */
    public static void createGUI() {
        frame = new JFrame("Checkers");
        primaryPanel = new JPanel(new BorderLayout());
        
        JLabel spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(67, 67));
        primaryPanel.add(spacer, BorderLayout.NORTH);
        
        JPanel player1 = new JPanel();
        player1.add(new JLabel("Player 1"));
        primaryPanel.add(player1, BorderLayout.LINE_START);
        
        // size is the size of each space on the board
        Dimension size = new Dimension(70, 70);
        board = new JPanel(new GridLayout(8, 8));
        // creates all the spaces and add them to the board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int count = j;
                JPanel space = new JPanel();
                space.setPreferredSize(size);
                if (i % 2 != 0) {
                    count++;
                }
                if (count % 2 == 0) {
                    space.setBackground(Color.RED);
                } else {
                    space.setBackground(Color.BLACK);
                }
                board.add(space);
            }
        }
        primaryPanel.add(board, BorderLayout.CENTER);
        
        JPanel player2 = new JPanel();
        player2.add(new JLabel("Player 2"));
        primaryPanel.add(player2, BorderLayout.LINE_END);
        
        ImageIcon menuIcon = new ImageIcon("src/images/gear.png");
        JButton menuButton = new JButton(menuIcon);
        menuButton.setBorder(null);
        menuButton.setContentAreaFilled(false);
        menuButton.setHorizontalAlignment(SwingConstants.RIGHT);
        primaryPanel.add(menuButton, BorderLayout.PAGE_END);
        
        frame.add(primaryPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Sets up the board to initial set up. Used to reset the board when the
     * user wishes to play a new game.
     */
    public static void setBoard() {
        clearBoard();
        // TODO: load up the HashMap and put the pieces on the board
    }

    /**
     * Removes all pieces from the board.
     */
    public static void clearBoard() {
        Component[] component = board.getComponents();
        for (Component component1 : component) {
            if (!(component1 instanceof JPanel)) {
                board.remove(component1);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Checkers();
            }
        });
    }

}
