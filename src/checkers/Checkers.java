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
 * @author Aaron S/Brent H
 */
public class Checkers implements ActionListener {

    private static JFrame frame;
    private static JPanel primaryPanel;
    private static JPanel board;
    private static JLabel spacer;
    private static HashMap<Integer, Piece> pieceMap = new HashMap<>();
    private static HashMap<Integer, JButton> boardSpaces = new HashMap<>();
    private static final ImageIcon RED_CHECKER = 
                new ImageIcon("src/images/red.jpg");
    private static final ImageIcon BLACK_CHECKER = 
                new ImageIcon("src/images/black.jpg");
    private static final ImageIcon RED_KING = 
                new ImageIcon("src/images/redKing.jpg");
    private static final ImageIcon BLACK_KING = 
                new ImageIcon("src/images/blackKing.jpg");
    private static final boolean YELLOW = false;
    private static final boolean GREEN = true;
    
    private static boolean selectionMade = false;
    private static int spaceSelected = -1; //-1 is default value
    private int[] legalMoves;
    private static char currentPlayer = 'r'; //red by default, toggled in play

    
    public Checkers() {
        createGUI();
        setBoard();
    }

    /**
     * Creates the GUI and displays it for the user
     */
    private void createGUI() {
        frame = new JFrame("Checkers");
        primaryPanel = new JPanel(new BorderLayout());
        
        spacer = new JLabel("<html><br>Your Turn, Red<br><br></html>");
        spacer.setHorizontalAlignment(JLabel.CENTER);
        spacer.setFont(new Font("Courier", Font.BOLD, 22));
        primaryPanel.add(spacer, BorderLayout.NORTH);
        
        JPanel player1 = new JPanel();
        player1.add(new JLabel("Player: Red"));
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
                space.setBorder(null);
                
                //This will ensure that the correct number is assigned
                int keyValue = 10*j + i;
                
                //ActionCommand takes a string, we can parse it back later
                space.setActionCommand(""+keyValue);
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
        player2.add(new JLabel("Player: Black"));
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
    public void actionPerformed(ActionEvent e){
        int key = Integer.parseInt(e.getActionCommand());
        selectionMade(key);
    }
    
    
    /**
     * Sets up the board to initial set up. Used to reset the board when the
     * user wishes to play a new game.
     */
    private void setBoard() {
        clearBoard();

        // create Red's pieces
        for (int x = 21; x < 101; x += 20) { // top row
            pieceMap.put(x, new Piece('r'));
            boardSpaces.get(x).setIcon(RED_CHECKER);
        }
        for (int y = 12; y < 92; y += 20) { // second row
            pieceMap.put(y, new Piece('r'));
            boardSpaces.get(y).setIcon(RED_CHECKER);
        }
        for (int z = 23; z < 103; z += 20) { // third row
            pieceMap.put(z, new Piece('r'));
            boardSpaces.get(z).setIcon(RED_CHECKER);
        }

        // create Black's pieces
        for (int a = 16; a < 96; a += 20) { // sixth row
            pieceMap.put(a, new Piece('b'));
            boardSpaces.get(a).setIcon(BLACK_CHECKER);
        }
        for (int b = 27; b < 107; b += 20) { // seventh row
            pieceMap.put(b, new Piece('b'));
            boardSpaces.get(b).setIcon(BLACK_CHECKER);
        }
        for (int c = 18; c < 98; c += 20) { // eighth row
            pieceMap.put(c, new Piece('b'));
            boardSpaces.get(c).setIcon(BLACK_CHECKER);
        }
    }

    /**
     * Removes all pieces from the board.
     */
    private void clearBoard() {
        // loops through each piece on the board and removes the icon 
        // from each space containing a piece
        for (Map.Entry<Integer, Piece> entry : pieceMap.entrySet()) {
            int key = entry.getKey();
            boardSpaces.get(key).setIcon(null);
        }
        // clears out HashMap containing the location of each piece
        pieceMap = new HashMap<>();
    }
    
    /**
     * This is called by the action listener and determines next steps, whether
     * it is to highlight spaces or call the movement method(s)
     * @param key is the selected space's HashMap key.
     */
    private void selectionMade(int key){
        
        if(selectionMade){
            if(key == spaceSelected){
                removeHighlight();
                selectionMade = false;
                spaceSelected = -1; //return to default value
            } else {
                if(isMoveLegal(key)) {
                    movePiece(key, spaceSelected);
                    removeHighlight();
                    selectionMade = false;
                    spaceSelected = -1;
                } else {
                    JOptionPane.showMessageDialog(null, "A piece has already "
                        + "been selected or an illegal move was chosen. To "
                        + "deselect the piece, click it again. Otherwise, "
                        + "choose one of the highlighted squares to move.",
                        "Illegal Selection", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            if(pieceMap.containsKey(key) && pieceMap.get(key).getColor() == currentPlayer){
                highlightSpace(key, Checkers.YELLOW);
                checkMoves(key);
                for(int move : legalMoves){
                    highlightSpace(move, Checkers.GREEN);
                }
            }
        }
    }
    
    /**
     * Checks for legal moves based on key passed by the action listener
     * @param key map key that points to where the piece is in both the
     *            piece map and space map.
     */
    private void checkMoves(int key){
        char color = pieceMap.get(key).getColor();
        boolean isKing = pieceMap.get(key).getKingStatus();
        int possibilities = (isKing) ? 4 : 2; //if it's a king, there are 4
                                              //move options, otherwise 2
        int[] option = new int[possibilities];
        
        if (isKing){
            option[0] = key - 9; //down left
            option[1] = key + 11; //down right
            option[2] = key - 11; //up left
            option[3] = key + 9; //up right
        } else if (color == 'r'){
            option[0] = key - 9; //down left
            option[1] = key + 11; //down right
        } else{
            option[0] = key - 11; //up left
            option[1] = key + 9; // up right
        }
        
        //Check for occupied space
        for(int i=0;i<possibilities;i++){
            if(pieceMap.containsKey(option[i])){
                char tempColor = pieceMap.get(option[i]).getColor();
                
                if(color == tempColor){ //The piece is on the same team
                    option[i] = -1; //Not a move option
                } else {
                    //Get's the jump landing spot
                    option[i] = checkJump(option[i], color, i);
                    
                    /*If the jump landing spot contains ANY piece, it's not an
                    option*/
                    if(pieceMap.containsKey(option[i]))
                        option[i] = -1;
                }
            }
        }

        //Check for out of bounds options
        for(int i=0;i<possibilities;i++){
            if(option[i] < 11 || option[i] > 88 || //left and right limits
                (option[i] > 18 && option[i] < 21) || // 
                (option[i] > 28 && option[i] < 31) || //
                (option[i] > 38 && option[i] < 41) || //Vertical
                (option[i] > 48 && option[i] < 51) || //limits
                (option[i] > 58 && option[i] < 61) || //
                (option[i] > 68 && option[i] < 71) || //
                (option[i] > 78 && option[i] < 81)){  //

                option[i] = -1; //Essentially a null value
            }
        }
        
        legalMoves = option;
        
    }
    
    private boolean isMoveLegal(int key){
        boolean test = false;
        
        for(int move : legalMoves){
            if (key == move)
                test = true;
        }
        
        return test;
    }
    
    /**
     * checkJump() finds the key value of the board space if a jump was to occur
     * @param key is the space which another piece is occupying.
     * @param color the color of the piece in question, used to determine
     *              movement direction
     * @param i the current position in the loop, used to determine movement
     *          direction
     * @return int key for the landing space of a jump.
     */
    private int checkJump(int key, char color, int i){
        int direction = i; //0=down-left, 1=d-right, 2=up-left, 3=u-right 
        int newKey = -1;
        
        //Adjust direction if color is black
        if(color == 'b')
            direction = i + 2;
        
        switch(direction){
            case 0:
                newKey = key - 9;
                break;
            case 1:
                newKey = key + 11;
                break;
            case 2:
                newKey = key - 11;
                break;
            case 3:
                newKey = key + 9;
                break;
            default:
                JOptionPane.showMessageDialog(null,"Oops, something went wrong",
                        "Something's not right...", JOptionPane.ERROR_MESSAGE);
                break;
        }
        
        return newKey; 
    }
    
    private void jumpPiece(int key, int spaceSelected){
                                         // need to double check the accuracy of 
                                         // these comments.... logic works tho
        if (key - spaceSelected == 18) { // piece is moving down and to the right
            pieceMap.remove(key - 9);
            boardSpaces.get(key - 9).setIcon(null);
            return;
        }
        if (key - spaceSelected == 22) { // piece is moving down and to the left
            pieceMap.remove(key - 11);
            boardSpaces.get(key - 11).setIcon(null);
            return;
        }
        if (spaceSelected - key == 18) { // piece is moving up and to the right
            pieceMap.remove(key + 9);
            boardSpaces.get(key + 9).setIcon(null);
            return;
        }
        if (spaceSelected - key == 22) { // piece is moving up and to the left
            pieceMap.remove(key + 11);
            boardSpaces.get(key + 11).setIcon(null);
        }
    }
    
    private void movePiece(int key, int spaceSelected){
        Piece piece = pieceMap.get(spaceSelected);
        JButton toSpace = boardSpaces.get(key);
        JButton fromSpace = boardSpaces.get(spaceSelected);
        
        // move the piece in the HashMap
        pieceMap.put(key, piece);
        pieceMap.remove(spaceSelected);
        
        // move the piece on the GUI        
        toSpace.setIcon(fromSpace.getIcon());
        fromSpace.setIcon(null);
        
        // jump a piece if necessary
        if (Math.abs(key - spaceSelected) > 11) {
            jumpPiece(key, spaceSelected);
        }
        
        // king the piece if necessary
        if ((piece.getColor() == 'r' && key % 10 == 8) ||
                piece.getColor() == 'b' && key % 10 == 1) {
            kingMe(piece, toSpace);
        }
        
        // toggle current player
        if (currentPlayer == 'r'){
            currentPlayer = 'b';
            spacer.setText("<html><br>Your Turn, Black<br><br></html>");
        } else {
            currentPlayer = 'r';
            spacer.setText("<html><br>Your Turn, Red<br><br></html>");
        }
    }
    
    private void kingMe(Piece piece, JButton space){
        if (piece.getColor() == 'b'){
            space.setIcon(BLACK_KING);
        } else {
            space.setIcon(RED_KING);
        }
        piece.promotePiece();        
    }
    
    /**
     * Method highlights specified buttons based on user selection or move
     * options.
     * @param key is the map key for the corresponding button
     * @param type determines the color of highlight applied. Checkers.YELLOW
     *             will highlight yellow, Checkers.GREEN will highlight green
     */
    private void highlightSpace(int key, boolean type){
                
        //Check for "null" entry
        if(key == -1)
            return;
        
        if(type == Checkers.YELLOW){
            boardSpaces.get(key).setBorder(
            BorderFactory.createLineBorder(Color.yellow, 3));
                
            selectionMade = true;
            spaceSelected = key;
            
        } else {
            boardSpaces.get(key).setBorder(
                BorderFactory.createLineBorder(Color.green, 5));
        }
    }
    
    private void removeHighlight(){
        for(Map.Entry<Integer, JButton> entry : boardSpaces.entrySet()){
            boardSpaces.get(entry.getKey()).setBorder(null);
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
