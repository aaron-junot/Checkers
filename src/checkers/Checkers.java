package checkers;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import java.awt.event.*;
import java.util.Map;
import javax.swing.border.*;

/**
 * This is the main class for the game of Checkers. This class draws the GUI,
 * and initiates the game for the users. The game requires two players.
 *
 * @author Aaron S
 */
public class Checkers implements ActionListener {

    private static JFrame frame;
    private static JPanel primaryPanel;
    private static JPanel board;
    private static HashMap<Integer, Piece> boardLocations = new HashMap<>();
    private static HashMap<Integer, JButton> boardSpaces = new HashMap<>();

    private static boolean selectionMade = false;
    private static final ImageIcon RED_CHECKER = new ImageIcon("src/images/red.jpg");
    private static final ImageIcon BLACK_CHECKER = new ImageIcon("src/images/black.jpg");

    public Checkers() {
        createGUI();
        setBoard();
    }

    /**
     * Creates the GUI and displays it for the user
     */
    public void createGUI() {
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
        for (int i = 1; i < 9; i++) {
            for (int j = 1; j < 9; j++) {
                int count = j;
                JButton space = new JButton();
                space.setPreferredSize(size);
                if (i % 2 != 0) {
                    count++;
                }
                if (count % 2 == 0) {
                    space.setBackground(Color.RED);
                } else {
                    space.setBackground(Color.BLACK);
                }

                //Make sure the button doesn't have a border
                space.setBorder(BorderFactory.createEmptyBorder());
                
                // Make sure the piece icons don't have borders either
                space.setFocusPainted(false);

                //This will ensure that the correct number is assigned
                int keyValue = 10 * j + i;

                //ActionCommand takes a string, we can parse it back later
                space.setActionCommand("" + keyValue);
                space.addActionListener(this); //ActionListener can determine
                //What needs to be done based on
                //Action command

                //Put the value into the map                               
                boardSpaces.put(keyValue, space);

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

    @Override
    public void actionPerformed(ActionEvent e) {
        int key = Integer.parseInt(e.getActionCommand());
        highlightSpace(boardSpaces.get(key));
    }

    /**
     * Sets up the board to initial set up. Used to reset the board when the
     * user wishes to play a new game.
     */
    public static void setBoard() {
        clearBoard();

        // create Red's pieces
        for (int x = 21; x < 101; x += 20) { // top row
            boardLocations.put(x, new Piece('r'));
            boardSpaces.get(x).setIcon(RED_CHECKER);
        }
        for (int y = 12; y < 92; y += 20) { // second row
            boardLocations.put(y, new Piece('r'));
            boardSpaces.get(y).setIcon(RED_CHECKER);
        }
        for (int z = 23; z < 103; z += 20) { // third row
            boardLocations.put(z, new Piece('r'));
            boardSpaces.get(z).setIcon(RED_CHECKER);
        }

        // create Black's pieces
        for (int a = 16; a < 96; a += 20) { // sixth row
            boardLocations.put(a, new Piece('b'));
            boardSpaces.get(a).setIcon(BLACK_CHECKER);
        }
        for (int b = 27; b < 107; b += 20) { // seventh row
            boardLocations.put(b, new Piece('b'));
            boardSpaces.get(b).setIcon(BLACK_CHECKER);
        }
        for (int c = 18; c < 98; c += 20) { // eighth row
            boardLocations.put(c, new Piece('b'));
            boardSpaces.get(c).setIcon(BLACK_CHECKER);
        }
    }

    /**
     * Removes all pieces from the board.
     */
    public static void clearBoard() {
        // loops through each piece on the board and removes the icon 
        // from each space containing a piece
        for (Map.Entry<Integer, Piece> entry : boardLocations.entrySet()) {
            int key = entry.getKey();
            boardSpaces.get(key).setIcon(null);
        }
        // clears out HashMap containing the location of each piece
        boardLocations = new HashMap<>();
    }

    /**
     * Highlights the space to indicate the user's selection or possible moves
     *
     * @param space the Object that was clicked
     */
    public static void highlightSpace(Object space) {
        /* 
        * Need to set border with method from JComponent, but ActionListener
        * needs to be an Object so a typecast is necessary
         */
        JButton selection = (JButton) space;

        if (selection.getBorder() instanceof EmptyBorder) {

            //Check that a selection hasn't already been made
            if(selectionMade){
                JOptionPane.showMessageDialog(null, 
                    "A piece has already been selected. Please select a"
                            + " legal move or reselect the piece to clear"
                            + " your selection.",
                    "Invalid Selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            selection.setBorder(
                    BorderFactory.createLineBorder(Color.YELLOW));

            selectionMade = true; //set boolean value to true, indicating
            //a selection has been made to eliminate
            //multiple selections

        } else {
            selection.setBorder(BorderFactory.createEmptyBorder());
            selectionMade = false;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Checkers();
            }
        });
    }
}
