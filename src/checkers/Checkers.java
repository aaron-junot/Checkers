package checkers;

import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Map;
import javax.swing.border.Border;

/**
 * This is the main class for the game of Checkers. This class draws the GUI,
 * and initiates the game for the users. The game requires two players.
 *
 * @author Aaron S/Brent H
 */
public class Checkers implements ActionListener {

    // GUI class variables    
    private JFrame frame;
    private JPanel primaryPanel;
    private JPanel board;
    private JLabel spacer;
    private JButton playerTurn;
    private Color redColor = new Color(200, 0, 0);
    private Border redTurnBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(redColor, 8), 
            BorderFactory.createLineBorder(Color.WHITE, 1));
    private Border blackTurnBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 8), 
            BorderFactory.createLineBorder(Color.WHITE, 1));
    
    private HashMap<Integer, Piece> pieceMap = new HashMap<>();
    private HashMap<Integer, JButton> boardSpaces = new HashMap<>();
    private HashSet<Integer> mandatoryJump = new HashSet<>();
    
    // Image class variables
    private static final ImageIcon RED_CHECKER = 
                new ImageIcon("src/images/red.jpg");
    private static final ImageIcon BLACK_CHECKER = 
                new ImageIcon("src/images/black.jpg");
    private static final ImageIcon RED_KING = 
                new ImageIcon("src/images/redKing.jpg");
    private static final ImageIcon BLACK_KING = 
                new ImageIcon("src/images/blackKing.jpg");
    private static final ImageIcon BACKGROUND_HORIZONTAL = 
                new ImageIcon("src/images/sandstoneHorizontal.jpg");
    private static final ImageIcon BACKGROUND_VERTICAL = 
                new ImageIcon("src/images/sandstoneVertical.jpg");
    
    // Constants for use with the highlightSpace() method
    private static final boolean YELLOW = false;
    private static final boolean GREEN = true;
    
    private boolean selectionMade = false;
    private int spaceSelected = -1; //-1 is default value
    private char currentPlayer = 'r'; //red by default, toggled in play
    
    //Movement related variables
    private boolean pieceJumped = false;
    private boolean jumpAvailable = false;
    private boolean forceJump = false;
    private boolean openingJump = false;
    private int[] legalMoves;
    private int[] jumpMoves;
    
    // Constructor
    public Checkers() {
        createGUI();
        setBoard();
    }

    /**
     * Creates the GUI and displays it for the user
     */
    private void createGUI() {
        frame = new JFrame("Checkers");
        primaryPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridwidth = 3;
        c.gridheight = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.fill = GridBagConstraints.BOTH;
        
        spacer = new JLabel(BACKGROUND_HORIZONTAL){
            @Override
            public void paintComponent (Graphics g) {
                super.paintComponent (g);
                g.drawImage (BACKGROUND_HORIZONTAL.getImage(), 
                        0, 0, getWidth (), getHeight (), null);
            }
        };
        spacer.setLayout(new FlowLayout());
        playerTurn = new JButton("<html>Your Turn, Red<br></html>");
        playerTurn.setHorizontalTextPosition(JLabel.CENTER);
        playerTurn.setVerticalTextPosition(JLabel.CENTER);
        playerTurn.setHorizontalAlignment(JLabel.CENTER);
        playerTurn.setVerticalAlignment(JLabel.CENTER);
        playerTurn.setForeground(Color.BLACK);
        spacer.setOpaque(true);
        playerTurn.setOpaque(true);
        playerTurn.setFont(new Font("Serif", Font.BOLD, 34));
        playerTurn.setBorder(null);
        spacer.add(playerTurn);
        primaryPanel.add(spacer, c);
        
       
        JLabel playerOne = new JLabel(BACKGROUND_VERTICAL){
            @Override
            public void paintComponent (Graphics g) {
                super.paintComponent (g);
                g.drawImage (BACKGROUND_VERTICAL.getImage(), 
                        0, 0, getWidth (), getHeight (), null);
            }
        };
        playerOne.setBorder(null);        
        playerOne.setForeground(Color.WHITE);
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 1;
        primaryPanel.add(playerOne, c);
        
        // size is the size of each space on the board
        Dimension size = new Dimension(70, 70);
        board = new JPanel(new GridLayout(8, 8));
        board.setBorder(redTurnBorder);
        
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
                    space.setBackground(redColor);
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
        c.gridx = 1;
        primaryPanel.add(board, c);
        
        JLabel playerTwo = new JLabel(BACKGROUND_VERTICAL){
            @Override
            public void paintComponent (Graphics g) {
                super.paintComponent (g);
                g.drawImage (BACKGROUND_VERTICAL.getImage(), 
                        0, 0, getWidth (), getHeight (), null);
            }
        };
        playerTwo.setForeground(Color.WHITE);
        c.gridx = 2;
        primaryPanel.add(playerTwo, c);
        
        ImageIcon menuIcon = new ImageIcon("src/images/gear.png");
        JButton menuButton = new JButton(menuIcon);
        menuButton.setBorder(null);
        menuButton.setContentAreaFilled(true);
       
        JLabel bottom = new JLabel(BACKGROUND_HORIZONTAL){
            @Override
            public void paintComponent (Graphics g) {
                super.paintComponent (g);
                g.drawImage (BACKGROUND_HORIZONTAL.getImage(), 
                        0, 0, getWidth (), getHeight (), null);
            }
        };
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(menuButton);
        bottom.revalidate();
        bottom.repaint();
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 2;
        primaryPanel.add(bottom, c);
        
        frame.add(primaryPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        int key = Integer.parseInt(e.getActionCommand());
        takeAction(key);
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
    private void takeAction(int key){
        
        if(selectionMade){
            if(key == spaceSelected && !forceJump){
                removeHighlight();
                selectionMade = false;
                spaceSelected = -1; //return to default value
            } else if (key == spaceSelected && forceJump) {
                JOptionPane.showMessageDialog(null, "You must continue to "
                        + "jump until no more jumps are available!",
                        "Mandatory Jump Available", JOptionPane.ERROR_MESSAGE);
            } else {
                if(isMoveLegal(key)) {
                    movePiece(key, spaceSelected);
                } else {
                    JOptionPane.showMessageDialog(null, "A piece has already "
                        + "been selected or an illegal move was chosen. To "
                        + "deselect the piece, click it again. Otherwise, "
                        + "choose one of the highlighted squares to move.",
                        "Illegal Selection", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            if(pieceMap.containsKey(key) && 
                        pieceMap.get(key).getColor() != currentPlayer){
                    if(currentPlayer == 'r'){
                        JOptionPane.showMessageDialog(null, 
                            "Whoops, not your turn yet. It's red's turn.",
                            "Wait for your turn!", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, 
                            "Whoops, not your turn yet. It's black's turn.",
                            "Wait for your turn!", 
                            JOptionPane.INFORMATION_MESSAGE);
                    }
            
            //If the player has a jump immediately available
            } else if(openingJump){    
                if(pieceMap.containsKey(key) && 
                        pieceMap.get(key).getColor() == currentPlayer &&
                        mandatoryJump.contains(key)){
                    
                    highlightSpace(key, YELLOW);
                    checkMoves(key);
                    for(int move : jumpMoves){
                        highlightSpace(move, GREEN);
                    }
                } else if(pieceMap.containsKey(key) &&
                        pieceMap.get(key).getColor() == currentPlayer){
                    
                    JOptionPane.showMessageDialog(null,
                        "It looks like one of your pieces can jump an "
                        + "opponent. If you have a jump available, "
                        + "you must take it.", "Mandatory Jump Available",
                        JOptionPane.ERROR_MESSAGE);
                }
            
            //If the player has no immediate jump and can choose a piece
            } else if(pieceMap.containsKey(key) && 
                                pieceMap.get(key).getColor() == currentPlayer){
                highlightSpace(key, YELLOW);
                checkMoves(key);
                if(forceJump){
                    for(int move : jumpMoves){
                        highlightSpace(move, GREEN);
                    }
                } else {
                    for(int move : legalMoves){
                        highlightSpace(move, GREEN);
                    }
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
        jumpMoves = new int[possibilities];
        
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
                    option[i] = checkJump(option[i], color, i, isKing);
                    jumpMoves[i] = 1; //1 indicates that a jump was available
                                      //at this index for a later check

                    /*If the jump landing spot contains ANY piece, it's not an
                    option*/
                    if(pieceMap.containsKey(option[i])){
                        option[i] = -1;
                        jumpMoves[i] = -1; //During a later check, -1 will mean
                                          //that that index was not a jump opt
                    }
                }
            } else { 
                jumpMoves[i] = -1;
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
        
        //Finalize jumpMoves after option[] was checked for out of bounds
        for(int i=0;i<possibilities;i++){
            if(jumpMoves[i] == 1 && option[i] != -1){
                jumpMoves[i] = option[i]; //set that index to the key value
                jumpAvailable = true;
            } else {
                jumpMoves[i] = -1;
            }
        }
        
        legalMoves = option;
    }
    
    private boolean isMoveLegal(int key){
        boolean test = false;
        
        if(forceJump){
            for(int move : jumpMoves){
                if(key == move)
                    test = true;
            }
        } else {
            for(int move : legalMoves){
                if (key == move)
                    test = true;
            }
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
    private int checkJump(int key, char color, int i, boolean isKing){
        int direction = i; //0=down-left, 1=d-right, 2=up-left, 3=u-right 
        int newKey = -1;
        
        //Adjust direction if color is black
        if(color == 'b' && !isKing)
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
    
    /**
     * Removes piece from board after being jumped
     * @param key is the map key of the piece to be removed
     * @param spaceSelected is the map key of the space that needs to be blank
     */
    private void jumpPiece(int key, int spaceSelected){
                                         
        if (key - spaceSelected == 18) { // piece is moving down-left
            pieceMap.remove(key - 9);
            boardSpaces.get(key - 9).setIcon(null);
            return;
        }
        if (key - spaceSelected == 22) { // piece is moving up-left
            pieceMap.remove(key - 11);
            boardSpaces.get(key - 11).setIcon(null);
            return;
        }
        if (spaceSelected - key == 18) { // piece is moving down-right
            pieceMap.remove(key + 9);
            boardSpaces.get(key + 9).setIcon(null);
            return;
        }
        if (spaceSelected - key == 22) { // piece is moving down-right
            pieceMap.remove(key + 11);
            boardSpaces.get(key + 11).setIcon(null);
        }
    }
    
    /**
     * Moves piece to the indicated key, removes the icon from the old key and
     * creates an icon on the new space.
     * @param key the new position of the piece
     * @param spaceSelected the current position of the piece (and therefore the
     *                      current position on the board)
     */
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
            pieceJumped = true;
            jumpAvailable = false;
            //Check for additional jump options
            checkMoves(key);
        }
        
        // king the piece if necessary
        if ((piece.getColor() == 'r' && key % 10 == 8) ||
                piece.getColor() == 'b' && key % 10 == 1) {
            kingMe(piece, toSpace);
        }
        
        if(pieceJumped && jumpAvailable){
            /*This creates a clean slate so each subsequent move is fresh, but
            also keeps track of whether that a move is required*/
            cleanUp();
            forceJump = true;
            takeAction(key);
        } else {
            cleanUp();
            changePlayer();
        }
    }
    
    /**
     * Promotes the piece
     * @param piece the piece to be promoted
     * @param space the space to be repainted with a king icon
     */
    private void kingMe(Piece piece, JButton space){
        if (piece.getColor() == 'b'){
            space.setIcon(BLACK_KING);
        } else {
            space.setIcon(RED_KING);
        }
        piece.promotePiece();        
    }
    
    /**
     * Called after a player's turn ends. Toggles the currentPlayer variable,
     * checks the pieceMap entry set to ensure the player still has pieces,
     * and simultaneously checks to see if the player has any jumps that they
     * have to make with their move. If the player has no pieces, the game ends
     * and the board is reset.
     */
    private void changePlayer(){
        int pieceCount = 0;

        // toggle current player
        if (currentPlayer == 'r'){
            currentPlayer = 'b';
            board.setBorder(blackTurnBorder);
            playerTurn.setText("<html>Your Turn, Black<br></html>");
         } else {
            currentPlayer = 'r';
            board.setBorder(redTurnBorder);
            playerTurn.setText("<html>Your Turn, Red<br></html>");
         }
        
        //Check for required jumps at the beginning of the turn
        for(Map.Entry<Integer, Piece> entry : pieceMap.entrySet()){
            if (pieceMap.get(entry.getKey()).getColor() == currentPlayer){
                pieceCount++;
                checkMoves(entry.getKey());
                if (jumpAvailable){
                    openingJump = true;
                    mandatoryJump.add(entry.getKey());
                    jumpAvailable = false; //necessary to ensure only elements
                                           //with jumps are added
                }      
            }
        }
        
        //Ends the game and restarts with a fresh board
        if(pieceCount == 0){
            if(currentPlayer == 'r'){
                JOptionPane.showMessageDialog(null, "Black wins!", "Game over!",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Red wins!", "Game over!",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            cleanUp();
            setBoard(); 
        }
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
        
        if(type == YELLOW){
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
     * Prepares for a new move by resetting all of the movement variables
     */
    private void cleanUp(){
        selectionMade = false;
        spaceSelected = -1;
        forceJump = false;
        openingJump = false;
        pieceJumped = false; 
        jumpAvailable = false;
        mandatoryJump = new HashSet<>();
        removeHighlight();
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
