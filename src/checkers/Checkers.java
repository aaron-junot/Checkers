package checkers;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
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
    private JPanel primaryPanel, board, top;
    private JLabel playerTurn;
    private JButton undoButton;
    private final Color redColor = new Color(200, 0, 0);
    private final Border redTurnBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(redColor, 8), 
            BorderFactory.createLineBorder(Color.WHITE, 1));
    private final Border blackTurnBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 8), 
            BorderFactory.createLineBorder(Color.WHITE, 1));
    private Dimension sizeOfSpace;
        
    // Image class variables
    private static final ImageIcon RED_CHECKER = 
                new ImageIcon("src/images/red.jpg");
    private static final ImageIcon BLACK_CHECKER = 
                new ImageIcon("src/images/black.jpg");
    private static final ImageIcon RED_KING = 
                new ImageIcon("src/images/redKing.jpg");
    private static final ImageIcon BLACK_KING = 
                new ImageIcon("src/images/blackKing.jpg");
    private static final Image BACKGROUND = 
                Toolkit.getDefaultToolkit().
                        getImage("src/images/Sandstone.jpg");
    
    // Constants for use with the highlightSpace() method
    private static final boolean YELLOW = false;
    private static final boolean GREEN = true;
    
    // Turn and input variables
    private boolean selectionMade = false;
    private int spaceSelected = -1; //-1 is default value
    private char currentPlayer = 'r'; //red by default, toggled in play
    
    // Movement related variables
    private HashMap<Integer, Piece> pieceMap = new HashMap<>();
    private final HashMap<Integer, JButton> boardSpaces = new HashMap<>();
    private HashSet<Integer> mandatoryJump = new HashSet<>();
    private final ArrayDeque<Move> moveHistory = new ArrayDeque<>();
    private boolean pieceJumped = false, jumpAvailable = false,
            forceJump = false, openingJump = false;
    private int[] legalMoves, jumpMoves;
    
    /**
     * Nested private class for storing moves for undo capability
     */
    private final class Move {        
        char player;
        int from;
        int to;
        boolean pieceJumped;
        boolean pieceKinged;
        boolean availableJump;
        boolean jumpForced;
        Icon jumpedIcon;
    }
    
    /**
     * Constructor
     */
    public Checkers() {
        createGUI();
        setBoard();
    }    

    /**
     * Creates the GUI and displays it for the user
     */
    private void createGUI() {
        frame = new JFrame("Checkers");
        
        // Create background
        primaryPanel = new JPanel(new GridBagLayout()){
            @Override
            public void paintComponent (Graphics g) {
                super.paintComponent (g);
                g.drawImage (BACKGROUND, 
                        0, 0, getWidth(), getHeight(), this);
            }
        };
        
        GridBagConstraints c = new GridBagConstraints();        
        c.gridwidth = 3;
        c.gridheight = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.ipadx = 0;
        c.fill = GridBagConstraints.BOTH;
        
        // Create top panel and label
        top = new JPanel();
        top.setLayout(new FlowLayout());
        top.setOpaque(false);
        
        playerTurn = new JLabel("Your turn, Red");
        playerTurn.setOpaque(true);
        playerTurn.setHorizontalTextPosition(JLabel.CENTER);
        playerTurn.setVerticalTextPosition(JLabel.CENTER);
        playerTurn.setHorizontalAlignment(JLabel.CENTER);
        playerTurn.setVerticalAlignment(JLabel.CENTER);
        playerTurn.setBackground(new Color(158, 136, 91));
        playerTurn.setForeground(new Color(69, 47, 30));
        playerTurn.setFont(new Font("Serif", Font.BOLD, 34));
        playerTurn.setBorder(null);
        
        top.add(playerTurn);
        
        primaryPanel.add(top, c);
        
        // Create left panel
        JPanel left = new JPanel();
        left.setOpaque(false);
        c.ipadx = 60;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 1;
        primaryPanel.add(left, c);
        
        // Create the board
        Dimension size = new Dimension(70, 70); /* size is the size of 
                                                   each space on the board 
                                                */
        board = new JPanel(new GridLayout(8, 8));
        board.setBorder(redTurnBorder);
        
        // Creates all spaces and adds them to the board
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
                
                // Make sure the button doesn't have a border
                space.setBorder(null);
                
                // Ensure the correct number is assigned
                int keyValue = 10 * j + i;
                
                // ActionCommand takes a string
                space.setActionCommand(""+keyValue);
                space.addActionListener(this);  /* ActionListener determines
                                                   action based on keyValue 
                                                */
                
                // Put the value into the HashMap                               
                boardSpaces.put(keyValue, space);
                
                board.add(space);
            } 
        }
        c.gridx = 1;
        primaryPanel.add(board, c);
        
        // Create the right panel
        JPanel right = new JPanel();
        right.setOpaque(false);
        c.gridx = 2;
        primaryPanel.add(right, c);
        
        // Create the menu button
        ImageIcon menuIcon = new ImageIcon("src/images/gear.png");
        JButton menuButton = new JButton(menuIcon);
        menuButton.setBorder(null);
        menuButton.setActionCommand("menu");  // On click, bring up menu
        menuButton.addActionListener(this);
       
        // When CTRL+Z pressed, undo last move
        Action undoAction = new AbstractAction("") {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoMove();
            }
        };
        String key = "Undo";
        undoAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
        menuButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                KeyEvent.CTRL_DOWN_MASK), key);
        menuButton.getActionMap().put(key, undoAction);
        
        // Create bottom panel and add menuButton
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(menuButton);
        bottom.revalidate();
        bottom.repaint();
        c.ipadx = 0;
        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 2;
        primaryPanel.add(bottom, c);
        
        // Create the frame and display
        frame.add(primaryPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e){
        // Display the menu when menuButton is pressed
        if (e.getActionCommand().equals("menu")){
            displaySettings();
        } else { // Must be a board space            
            int key = Integer.parseInt(e.getActionCommand());
            takeAction(key);
        }
    }    
    
    /**
     * Sets up the board to initial set up. Used to reset the board when the
     * user wishes to play a new game.
     */
    private void setBoard() {
        clearBoard();

        // Create Red's pieces
        for (int x = 21; x < 101; x += 20) { // first row
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

        // Create Black's pieces
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
        /* Loops through each piece on the board and removes the icon 
           from each space containing a piece 
        */
        for (Map.Entry<Integer, Piece> entry : pieceMap.entrySet()) {
            int key = entry.getKey();
            boardSpaces.get(key).setIcon(null);
        }
        // Clears out HashMap containing the location of each piece
        pieceMap = new HashMap<>();
        
        // Clears out the move history
        moveHistory.clear();
    }
    
    /**
     * This is called by the action listener and determines next steps, whether
     * it is to highlight spaces or call the movement method(s)
     * @param key is the selected space's HashMap key.
     */
    private void takeAction(int key){
        // If there is already a piece highlighted
        if(selectionMade){
            // Deselect the space already selected
            if(key == spaceSelected && !forceJump){
                removeHighlight();
                selectionMade = false;
                spaceSelected = -1; // Return to default value
            // Require the user to continue jumping
            } else if (key == spaceSelected && forceJump) {
                JOptionPane.showMessageDialog(null, "You must continue to "
                        + "jump until no more jumps are available!",
                        "Mandatory Jump Available", JOptionPane.ERROR_MESSAGE);
            // Move the piece to the correct space
            } else if(isMoveLegal(key)) {
                movePiece(key, spaceSelected);
            } else if(!isMoveLegal(key) && forceJump){
                JOptionPane.showMessageDialog(null, "You must jump the piece.",
                        "Mandatory Jump Available", JOptionPane.ERROR_MESSAGE);
            } else {
                // Deselect the piece
                removeHighlight();
                selectionMade = false;
                // If the current player clicked one of their own pieces
                if(pieceMap.containsKey(key) && 
                        pieceMap.get(key).getColor() == currentPlayer){                        
                    checkMoves(key);
                    // If the player has a jump immediately available
                    if(openingJump){
                        // If they can jump a piece, 
                        // highlight that for them automatically
                        if(pieceMap.containsKey(key) && 
                                pieceMap.get(key).getColor() == currentPlayer &&
                                mandatoryJump.contains(key)){
                    
                            highlightSpace(key, YELLOW);
                            checkMoves(key);
                            for(int move : jumpMoves){
                                highlightSpace(move, GREEN);
                            }
                        // Enforce the mandatory jump
                        } else if(pieceMap.containsKey(key) &&
                                   pieceMap.get(key).getColor() == currentPlayer){
                    
                            JOptionPane.showMessageDialog(null,
                                "It looks like one of your pieces can jump an "
                                + "opponent. If you have a jump available, "
                                + "you must take it.", "Mandatory Jump Available",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    /* Since there's not a jump, 
                       highlight the moves for the selected piece 
                    */
                    } else {
                        // Loop through legalMoves for this piece
                        for(int move: legalMoves){
                            if (move != -1) { // Do nothing if no legal moves
                                highlightSpace(key, YELLOW);
                                if(forceJump){ // If jump available, highlight it
                                    for(int mov : jumpMoves){
                                        highlightSpace(mov, GREEN);
                                    }
                                } else { // Highlight all legal moves
                                    for(int mov : legalMoves){
                                        highlightSpace(mov, GREEN);
                                    }
                                }
                            }
                        }
                    }
                }
                // If they selected an opponent's piece
                if(pieceMap.containsKey(key) && 
                    pieceMap.get(key).getColor() != currentPlayer){
                    
                    // Give an error message telling them to wait their turn
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
                }
                // Do nothing if they clicked a space without a piece on it
            }   
            
        // If there is not already a piece highlighted
        } else {
            // If they select opponent's piece, tell them to wait their turn
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
            
            // If the player has a jump immediately available
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
            
            // If the player has no immediate jump and can choose a piece
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
        int possibilities = (isKing) ? 4 : 2; /* if it's a king, there are 4
                                                 move options, otherwise 2
                                              */
        int[] option = new int[possibilities];
        jumpMoves = new int[possibilities];
        
        if (isKing){
            option[0] = key - 9;  // down left
            option[1] = key + 11; // down right
            option[2] = key - 11; // up left
            option[3] = key + 9;  // up right
        } else if (color == 'r'){
            option[0] = key - 9;  // down left
            option[1] = key + 11; // down right
        } else{
            option[0] = key - 11; // up left
            option[1] = key + 9;  // up right
        }
        
        // Check for occupied space
        for(int i=0;i<possibilities;i++){
            if(pieceMap.containsKey(option[i])){
                char tempColor = pieceMap.get(option[i]).getColor();
                
                if(color == tempColor){ // The piece is on the same team
                    option[i] = -1; // Not a move option
                } else {
                    // Gets the jump landing spot
                    option[i] = checkJump(option[i], color, i, isKing);
                    jumpMoves[i] = 1; /* 1 indicates that a jump was available
                                         at this index for a later check
                                      */

                    /* If the jump landing spot contains ANY piece, it's not an
                       option
                    */
                    if(pieceMap.containsKey(option[i])){
                        option[i] = -1;
                        jumpMoves[i] = -1; /* During a later check, -1 will mean
                                              that that index was not a jump opt
                                           */
                    }
                }
            } else { 
                jumpMoves[i] = -1;
            }
        }

        // Check for out of bounds options
        for(int i=0;i<possibilities;i++){
            if(option[i] < 11 || option[i] > 88 || // left and right limits
                (option[i] > 18 && option[i] < 21) || // 
                (option[i] > 28 && option[i] < 31) || //
                (option[i] > 38 && option[i] < 41) || // Vertical
                (option[i] > 48 && option[i] < 51) || // limits
                (option[i] > 58 && option[i] < 61) || //
                (option[i] > 68 && option[i] < 71) || //
                (option[i] > 78 && option[i] < 81)){  //

                option[i] = -1; // Essentially a null value
            }
        }
        
        // Finalize jumpMoves after option[] was checked for out of bounds
        for(int i=0;i<possibilities;i++){
            if(jumpMoves[i] == 1 && option[i] != -1){
                jumpMoves[i] = option[i]; // Set that index to the key value
                jumpAvailable = true;
                forceJump = true;
            } else {
                jumpMoves[i] = -1;
            }
        }
        
        legalMoves = option;
    }
    
    /**
     * Method to determine if a move is legal
     * @param key The destination space for the piece
     * @return true if move is legal, otherwise false
     */
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
        int direction = i; // 0=down-left, 1=d-right, 2=up-left, 3=u-right 
        int newKey = -1;
        
        // Adjust direction if color is black
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
     * @return the Icon of the jumped piece, to allow for undo
     */
    private Icon jumpPiece(int key, int spaceSelected){
        Icon jumpedIcon = null;                                 
        if (key - spaceSelected == 18) { // piece is moving down-left
            pieceMap.remove(key - 9);
            jumpedIcon = boardSpaces.get(key - 9).getIcon(); 
            boardSpaces.get(key - 9).setIcon(null);
        }
        if (key - spaceSelected == 22) { // piece is moving up-left
            pieceMap.remove(key - 11);
            jumpedIcon = boardSpaces.get(key - 11).getIcon(); 
            boardSpaces.get(key - 11).setIcon(null);
        }
        if (spaceSelected - key == 18) { // piece is moving down-right
            pieceMap.remove(key + 9);
            jumpedIcon = boardSpaces.get(key + 9).getIcon(); 
            boardSpaces.get(key + 9).setIcon(null);
        }
        if (spaceSelected - key == 22) { // piece is moving down-right
            pieceMap.remove(key + 11);
            jumpedIcon = boardSpaces.get(key + 11).getIcon(); 
            boardSpaces.get(key + 11).setIcon(null);
        }
        
        return jumpedIcon;
    }
    
    /**
     * Moves piece to the indicated key, removes the icon from the old key and
     * creates an icon on the new space.
     * @param key the new position of the piece
     * @param spaceSelected the current position of the piece (and therefore the
     *                      current position on the board)
     */
    private void movePiece(int key, int spaceSelected){
        // Capture info about this move to store in the moveHistory ArrayDeque
        Move thisMove = new Move();
        thisMove.from = spaceSelected;
        thisMove.to = key;
        thisMove.player = currentPlayer;
        
        Piece piece = pieceMap.get(spaceSelected);
        JButton toSpace = boardSpaces.get(key);
        JButton fromSpace = boardSpaces.get(spaceSelected);
        
        // Move the piece in the HashMap
        pieceMap.put(key, piece);
        pieceMap.remove(spaceSelected);
        
        // Move the piece on the GUI        
        toSpace.setIcon(fromSpace.getIcon());
        fromSpace.setIcon(null);
        
        // Jump a piece if necessary
        if (Math.abs(key - spaceSelected) > 11) {            
            thisMove.jumpedIcon = jumpPiece(key, spaceSelected);
            thisMove.pieceJumped = true; 
            pieceJumped = true;
            jumpAvailable = false;
            // Check for additional jump options
            checkMoves(key);
        }
        
        // King the piece if necessary
        if ((piece.getColor() == 'r' && key % 10 == 8) ||
                piece.getColor() == 'b' && key % 10 == 1) {
            kingMe(piece, toSpace);
            thisMove.pieceKinged = true;
        }
        
        moveHistory.push(thisMove);
        
        if(pieceJumped && jumpAvailable){
            /* This creates a clean slate so each subsequent move is fresh, but
               also keeps track of whether a move is required
            */
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
     * Called after a player's turn ends. Toggles the currentPlayer variable.
     */
    private void changePlayer(){
            if (currentPlayer == 'r'){
                currentPlayer = 'b';
                board.setBorder(blackTurnBorder);
                playerTurn.setText("Your turn, Black");
             } else {
                currentPlayer = 'r';
                board.setBorder(redTurnBorder);
                playerTurn.setText("Your turn, Red");
            }
            
        evaluateOptions();
    }
    
    /**
     * Evaluates the player's moves and pieces prior to beginning their turn. If
     * the player has no pieces or no available moves, the game ends. If the
     * player has a jump available, sets forceJump to true.
     */
    private void evaluateOptions(){
        int pieceCount = 0;
        boolean hasMove = false;

        /* Check for required jumps at the beginning of the turn, count the
           pieces, and check for available moves. If no pieces or no available
           moves, end the game
        */
        for(Map.Entry<Integer, Piece> entry : pieceMap.entrySet()){
            if (pieceMap.get(entry.getKey()).getColor() == currentPlayer){
                pieceCount++;
                checkMoves(entry.getKey());
                
                for(int move : legalMoves){
                    if (move != -1){
                        hasMove = true;
                    }
                }
                
                if (jumpAvailable){
                    openingJump = true;
                    mandatoryJump.add(entry.getKey());
                    jumpAvailable = false;  /* Necessary to ensure only elements
                                               with jumps are added
                                            */
                }      
            }
        }
        
        // Ends the game and restarts with a fresh board
        if( pieceCount == 0 || !hasMove ){
            endGame();
        }
    }
    
    /**
     * Contains the code to end the game and displays who won
     */
    private void endGame(){
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
    
    /**
     * Method highlights specified buttons based on user selection or move
     * options.
     * @param key is the map key for the corresponding button
     * @param type determines the color of highlight applied. Checkers.YELLOW
     *             will highlight yellow, Checkers.GREEN will highlight green
     */
    private void highlightSpace(int key, boolean type){                
        // Check for "null" entry
        if(key == -1)
            return;
        
        // Highlight width is independent of size of each space
        sizeOfSpace = boardSpaces.get(key).getSize();
        int yellowsize = sizeOfSpace.height / 23;
        int greensize = sizeOfSpace.height / 14;
        
        if(type == YELLOW){
            boardSpaces.get(key).setBorder(
            BorderFactory.createLineBorder(Color.yellow, yellowsize));
                
            selectionMade = true;
            spaceSelected = key;
            
        } else {
            boardSpaces.get(key).setBorder(
                BorderFactory.createLineBorder(Color.green, greensize));
        }
    }
    
    /**
     * Removes all highlights on the board
     */
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
     * Creates and displays a new window containing various settings
     */
    private void displaySettings() {
        JDialog dialog = new JDialog(frame, "Settings");
        JLabel label = new JLabel("Settings");
        label.setHorizontalAlignment(JLabel.LEFT);
        JButton closeButton = new JButton("Close");
        
        closeButton.addActionListener((ActionEvent e) -> {
            dialog.setVisible(false);
            dialog.dispose();
        });
        
        undoButton = new JButton("Undo Last Move");
        if (moveHistory.isEmpty()){
            undoButton.setEnabled(false);
        }
        
        undoButton.addActionListener((ActionEvent e) -> {
            undoMove();
        });
        
        JPanel settingsPanel = new JPanel();
        settingsPanel.add(undoButton); 
        JPanel closePanel = new JPanel();
        closePanel.add(closeButton);
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(label, BorderLayout.PAGE_START);
        contentPane.add(settingsPanel, BorderLayout.CENTER);
        contentPane.add(closePanel, BorderLayout.PAGE_END);
        contentPane.setOpaque(true);
        dialog.setContentPane(contentPane);
        
        dialog.setSize(new Dimension(400, 300));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
    
    /**
     * Called when the user presses CTRL+Z or 
     * the undo button in the settings menu
     */
    private void undoMove(){
        if (moveHistory.isEmpty()){
            return;
        }
        // Get the last move
        Move lastMove = moveHistory.pop();
                
        Piece piece = pieceMap.get(lastMove.to);
        JButton toSpace = boardSpaces.get(lastMove.from);
        JButton fromSpace = boardSpaces.get(lastMove.to);
        
        // Move the piece in the pieceMap
        pieceMap.put(lastMove.from, piece);
        pieceMap.remove(lastMove.to);
        
        // Move the piece on the GUI        
        toSpace.setIcon(fromSpace.getIcon());
        fromSpace.setIcon(null);        
              
        // Unjump a piece if necessary
        if (lastMove.pieceJumped) {
            Piece jumpedPiece;
            if (lastMove.player == 'r'){
                jumpedPiece = new Piece('b');
            } else {
                jumpedPiece = new Piece('r');
            }
            if (lastMove.jumpedIcon == RED_KING || 
                    lastMove.jumpedIcon == BLACK_KING){
                jumpedPiece.promotePiece();
            }
            if (lastMove.to - lastMove.from == 18) { 
                pieceMap.put(lastMove.to - 9, jumpedPiece);
                boardSpaces.get(lastMove.to - 9).setIcon(lastMove.jumpedIcon);
            }
            if (lastMove.to - lastMove.from == 22) { 
                pieceMap.put(lastMove.to - 11, jumpedPiece);
                boardSpaces.get(lastMove.to - 11).setIcon(lastMove.jumpedIcon);
            }
            if (lastMove.from - lastMove.to == 18) { 
                pieceMap.put(lastMove.to + 9, jumpedPiece);
                boardSpaces.get(lastMove.to + 9).setIcon(lastMove.jumpedIcon);
            }
            if (lastMove.from - lastMove.to == 22) { 
                pieceMap.put(lastMove.to + 11, jumpedPiece);
                boardSpaces.get(lastMove.to + 11).setIcon(lastMove.jumpedIcon);
            }
        } 
        
        // Unking the piece if necessary
        if (lastMove.pieceKinged) {
            if (piece.getColor() == 'b'){
                toSpace.setIcon(BLACK_CHECKER);
            } else {
                toSpace.setIcon(RED_CHECKER);
            }
            piece.demotePiece();
        }
        
        // Disable the undoButton in the Settings menu
        if (undoButton != null && moveHistory.isEmpty()){
            undoButton.setEnabled(false);
        }
        
        // Take care of highlights and forced jumps
        cleanUp();
        if(lastMove.player != currentPlayer){
            changePlayer();
        }
        
        // Reset the selection to the piece that was just reverted
        takeAction(lastMove.from);
        
        // Get the move prior to the undo
        Move prior = null;
        try{
            prior = moveHistory.pop();
            if(lastMove.pieceJumped && prior.player == currentPlayer){
            forceJump = true;
            selectionMade = true;
            spaceSelected = lastMove.from;
            } else if(lastMove.pieceJumped && prior.player != currentPlayer){
                evaluateOptions();
            }
        } catch (NoSuchElementException e){}
        
        
        
        // Replace the prior move to avoid a null pointer if multiple undos
        if(prior != null){
            moveHistory.push(prior);
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

