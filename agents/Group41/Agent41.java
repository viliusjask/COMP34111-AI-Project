import java.net.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.LinkedList;
import java.util.Comparator;
import java.io.*;

class Agent41{
	static class Hex {
	    int x;
	    int y;
	    String player;
	    int heurValue;
	    int pathLengthFromSource;

	    public Hex() {
	    }

	    public Hex(int x, int y, String player, int heurValue) {
	        this.x = x;
	        this.y = y;
	        this.player = player;
	        this.heurValue = heurValue;
	    }
	    
	    public int getPathLengthFromSource() {
	        return this.pathLengthFromSource;
	    }
	    
	    public void setPathLengthFromSource(int pathLengthFromSource) {
	    	this.pathLengthFromSource = pathLengthFromSource;
	    }
	    
	    public int getX() {
	        return this.x;
	    }

	    public void setX(int x) {
	        this.x = x;
	    }

	    public int getY() {
	        return this.y;
	    }

	    public void setY(int y) {
	        this.y = y;
	    }

	    public String getPlayer() {
	        return this.player;
	    }

	    public void setPlayer(String player) {
	        this.player = player;
	    }

	    public int getHeurValue() {
	        return this.heurValue;
	    }

	    public void setHeurValue(int heurValue) {
	        this.heurValue = heurValue;
	    }


	}

    public static String HOST = "127.0.0.1";
    public static int PORT = 1234;

    private Socket s;
    private PrintWriter out;
    private BufferedReader in;

    private static String colour = "R";
    private int turn = 0;
    private static int boardSize = 11;
    private static boolean visited[][] = new boolean[boardSize][boardSize];

    private void Connect() throws UnknownHostException, IOException{
        s = new Socket(HOST, PORT);
        out = new PrintWriter(s.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
    }

    private String getMessage() throws IOException{
        return in.readLine();
    }

    private void sendMessage(String msg){
        out.print(msg); out.flush();
    }

    private void closeConnection() throws IOException{
        s.close();
        out.close();
        in.close();
    }

    public void run(){
        // connect to the engine
        try{
            Connect();
        } catch (UnknownHostException e){
            System.out.println("ERROR: Host not found.");
            return;
        } catch (IOException e){
            System.out.println("ERROR: Could not establish I/O.");
            return;
        }

        while (true){
            // receive messages
            try{
                String msg = getMessage();
                boolean res = interpretMessage(msg);
                if (res == false) break;
            } catch (IOException e){
                System.out.println("ERROR: Could not establish I/O.");
                return;
            }
        }

        try{
            closeConnection();
        } catch (IOException e){
            System.out.println("ERROR: Connection was already closed.");
        }
    }

    private boolean interpretMessage(String s){
        turn++;

        String[] msg = s.strip().split(";");
        switch (msg[0]){
            case "START":
                boardSize = Integer.parseInt(msg[1]);
                colour = msg[2];
                if (colour.equals("R")){
                    // so sad ):
                    String board = "";
                    for (int i = 0; i < boardSize; i++){
                        String line = "";
                        for (int j = 0; j < boardSize; j++)
                            line += "0";
                        board += line;
                        if (i < boardSize - 1) board += ",";
                    }
                    makeMove(board);
                }
                break;

            case "CHANGE":
                if (msg[3].equals("END")) return false;
                if (msg[1].equals("SWAP")) colour = selectOpponent(colour);
                if (msg[3].equals(colour)) makeMove(msg[2]);
                break;

            default:
                return false;
        }

        return true;
    }

    // CALL minimax in this 
    private void makeMove(String board_str){
        Hex[][] board = boardStringToArray(board_str);
        // Swap Logic
        if (turn == 1) {
            Hex bestMove;
            bestMove = selectStartingPosition();
            board[bestMove.getX()][bestMove.getY()].setPlayer(colour);
        	String msg = "" + bestMove.getX() + "," + bestMove.getY() + "\n";
            sendMessage(msg);
            return;
        }
        if (turn == 2){
            if(shouldSwap(getOpponentFirstMove(board))){
                sendMessage("SWAP\n");
                return;
            }
        }
        // Check if any bridges are at risk

        ArrayList<Hex> bridge_moves = getBridgesAtRisk(board, colour);
        Hex bestMove;
        if (bridge_moves.size() > 0){
            // Prioritize connecting a bridge
            bestMove = bridge_moves.get(0);
        }
        else {
            // Get the best move
            bestMove = new Hex(-1, -1, colour, 0);
            bestMove = minimax(board, colour, 2, Integer.MIN_VALUE, Integer.MAX_VALUE, bestMove);
        }

        if (bestMove != null){
            String msg = "" + bestMove.getX() + "," + bestMove.getY() + "\n";
            sendMessage(msg);
            return;
        }
        
        return;
    }

    // Should only be called on turn if minimizing player for swap rule
    public static Hex getOpponentFirstMove(Hex[][] board){
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getPlayer() != null){
                    return board[i][j];
                }
            }
        }
        return null;
    }

    // Converts the board string from the server into an array of Hex objects
    public static Hex[][] boardStringToArray(String board){
        String[] lines = board.split(",");
        Hex[][] hexBoard = new Hex[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++){
            for (int j = 0; j < boardSize; j++){ 
                Hex h = new Hex(i, j, null, -1);
                switch (lines[i].charAt(j)){
                    case '0':
                        hexBoard[i][j] = h;
                        break;
                    case 'R':
                        h.setPlayer("R");
                        hexBoard[i][j] = h;
                        break;
                    case 'B':
                        h.setPlayer("B");
                        hexBoard[i][j] = h;
                }        
            }
        }
        return hexBoard;
    }

    public static void main(String args[]){
        Agent41 agent = new Agent41();
        Hex[][] hexBoard = new Hex[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
                hexBoard[i][j] = null;
        // Pass hexBoard in run?
        agent.run();
    }

    public static Hex minimax(Hex[][] board, String player, int depth, int alpha, int beta, Hex bestMove) {
        ArrayList<Hex> possibleMoves;

        possibleMoves = getPossibleMoves(board);
        if(depth == 0 || possibleMoves.size() == 0)
        {
            int bestScore = getBoardState(board, player);
            bestMove.setHeurValue(bestScore);
            return bestMove;
        }

        int bestEval;
        if(player.equals("R")) // max player
        {
            bestEval = Integer.MIN_VALUE;
            for(int i = 0; i < possibleMoves.size(); i++)
            {
                int currentX = possibleMoves.get(i).getX();
                int currentY = possibleMoves.get(i).getY();
                board[currentX][currentY].setPlayer("R");
                Hex currentMove = board[currentX][currentY];

                Hex eval = minimax(board, "B", depth - 1, alpha, beta, bestMove);
                int evalVal = eval.getHeurValue();
                if(bestEval < evalVal)
                {
                    bestEval = evalVal;
                   // bestMove = currentMove;
                    bestMove.setX(currentX);
                    bestMove.setY(currentY);
                    
                    bestMove.setHeurValue(bestEval);
                    alpha = Math.max(alpha, evalVal);
                    if(beta <= alpha)
                    {
                        //Undo Move
                        board[currentX][currentY].setPlayer(null);
                        break;
                    }
                }
                board[currentX][currentY].setPlayer(null);
                
            }
        } 
        else if(player.equals("B")) // min player
        {
            bestEval = Integer.MAX_VALUE;
            for(int i = 0; i < possibleMoves.size(); i++)
                {
                    int currentX = possibleMoves.get(i).getX();
                    int currentY = possibleMoves.get(i).getY();
                    board[currentX][currentY].setPlayer("B");
                    Hex currentMove = board[currentX][currentY];
                    
                    Hex eval = minimax(board, "R", depth - 1, alpha, beta, bestMove);
                    int evalVal = eval.getHeurValue();
                    if(bestEval > evalVal)
                    {
                        bestEval = evalVal;
                        //bestMove = currentMove;
                        bestMove.setX(currentX);
                        bestMove.setY(currentY);
                        bestMove.setHeurValue(bestEval);
                        alpha = Math.min(alpha, evalVal);
                        if(beta <= alpha)
                        {
                            board[currentX][currentY].setPlayer(null);
                            break;
                        }
                    }
                    board[currentX][currentY].setPlayer(null);
                }
        }
    
    	return bestMove;
    }

    public static int getBoardState(Hex[][] board, String player)
    {
        String opponent = selectOpponent(player);

        if(checkWinForRedPlayer(board))
            return 1000;

        if(checkWinForBluePlayer(board))
            return -1000;
        
        int bridgeHeur = bridgeFactor(board, player);
        int dijkstraHuer = dijkstra(board, player) - dijkstra(board, opponent);

        int playerScore = connectedNodes(board, player);

        int opponentScore = connectedNodes(board, player);

        return 6 * bridgeHeur + dijkstraHuer + (playerScore - opponentScore);
    }

    // bridge heuristics
    public static int bridgeFactor(Hex[][] board, String player)
    {
        String opponent = selectOpponent(player);
        int score = 0;
        // Looping through the board
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if(board[i][j].getPlayer() != null) {
                    // Get all the possible bridges for each piece
                    ArrayList<Hex> bridges = getPossibleBridges(board, board[i][j]);
                    for (Hex h : bridges) {
                        // Bridge exists for maximising player
                        if (player.equals("R") && "R".equals(h.getPlayer())) {
                            score += 5;
                        // Bridge exists for minimizing player
                        } else if (player.equals("B") && "B".equals(h.getPlayer())) {
                            score += -5;
                        }
                    }
                }
            }
        }
        return score;
    }

    // Dijkstra heur

    public static int dijkstra(Hex[][] board, String player)
    {
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j < boardSize; j++) {
        		visited[i][j] = false;
        	}
        }

        
//    	ArrayList<Hex> vertices = new ArrayList<Hex>();
//    	for (int i = 0; i<boardSize; i++) {
//    		for (int j = 0; j<boardSize; j++) {
//        		vertices.add(board[i][j]);
//        	}
//    	}
//    	Hex L = new Hex(-1,0,"B",0);
//    	Hex R = new Hex(boardSize+1,0,"B",0);
//    	Hex T = new Hex(0,boardSize+1,"R",0);
//    	Hex D = new Hex(0,-1,"R",0);
    	

//     	if (player == "R") {
//     		Hex source = T;
//     		Hex destination = D;
//     	}
//     	else if (player == "B") {
//     		Hex source = L;
//     		Hex destination = R;
//     	}
    	
//    	ArrayList<Hex> currentVertices = vertices;
    	//source.pathLengthFromSource = 0;
    	//source.pathVerticesFromSource.add(source);
    	
    	
    	for (int i = 0; i < boardSize; i++) {
    		for (int j = 0; j < boardSize; j++) {
    			board[i][j].setPathLengthFromSource(Integer.MAX_VALUE);
    		}
    	}
    	
    	// Queue<Hex> verticesQueue = new PriorityQueue<Hex>();
        Queue<Integer> xQueue = new PriorityQueue<Integer>();
        Queue<Integer> yQueue = new PriorityQueue<Integer>();
        Queue<String> playerQueue = new PriorityQueue<String>();
    	
    	if (player.equals("R")) {
    		for (int i = 0; i < boardSize; i++) {
    			if ("R".equals(board[0][i].getPlayer()) || board[0][i].getPlayer() == null) {
    				// verticesQueue.add(board[0][i]);
                    xQueue.add(board[0][i].getX());
                    yQueue.add(board[0][i].getY());
                    playerQueue.add(board[0][i].getPlayer());

        			visited[0][i] = true;
    			}
    		}
    	}
    	else if (player.equals("B")) {
    		for (int i = 0; i < boardSize; i++) {
    			if ("B".equals(board[i][0].getPlayer()) || board[i][0].getPlayer() == null) {
                    xQueue.add(board[i][0].getX());
                    yQueue.add(board[i][0].getY());
                    playerQueue.add(board[i][0].getPlayer());
        			visited[i][0] = true;
    			}
    		}
    	}
    	
    	
    	
    	while (verticesQueue.size() != 0) {
    		// Hex currentVertex = verticesQueue.poll();
            int x;
            int y;
            String player1;
    		ArrayList<Hex> neighbours = getNeighbours(board, new Hex(x, y, player1, 0));
    		for (int i = 0; i < neighbours.size(); i++) {
    			Hex currentNeighbour = neighbours.get(i);
                int neighDist = currentVertex.getPathLengthFromSource();
                if(currentNeighbour.getPlayer() != player)
                    neighDist++;
    			
    			if (neighDist < currentNeighbour.getPathLengthFromSource()) {
    				currentNeighbour.setPathLengthFromSource(neighDist);
    				board[currentNeighbour.getX()][currentNeighbour.getY()].setPathLengthFromSource(neighDist);
    			}
    			
    			
    		    //int pathLengthFromSource;
    		    //ArrayList<Hex> pathVerticesFromSource;
    			
    			if (!visited[currentNeighbour.getX()][currentNeighbour.getY()] || currentNeighbour.getPlayer() == player) {
                    xQueue.add(currentNeighbour.getX());
                    yQueue.add(currentNeighbour.getY());
                    playerQueue.add(currentNeighbour.getPlayer());
    				visited[currentNeighbour.getX()][currentNeighbour.getY()] = true;
    			}
    		}
    		
    	}
    	ArrayList<Integer> redList = new ArrayList<Integer>();
    	ArrayList<Integer> blueList = new ArrayList<Integer>();
    	int minPath = Integer.MAX_VALUE;
    	
    	for (int i = 0; i < boardSize; i++) {
    		if (player.equals("R")) {
    			redList.add(board[boardSize-1][i].getPathLengthFromSource());
    			minPath = Collections.min(redList);
    		}
    		else if (player.equals("B")) {
    			blueList.add(board[i][boardSize-1].getPathLengthFromSource());
    			minPath = Collections.min(blueList);
    		}
    	}
    	return minPath;
    }

    // Connected nodes heur
    public static int connectedNodes(Hex[][] board, String player )
    {
        for(int i = 0; i < boardSize; i++)
            for(int j = 0; j < boardSize; j++)
                visited[i][j] = false;

        int count = 0;
        int length = 0;
        for (int i = 0; i < boardSize; ++i)
            for (int j = 0; j < boardSize; ++j)
                if (board[i][j].getPlayer() == player && !visited[i][j]) 
                {
                    length = DFS(board, i, j, visited, player);
                    if (length > 1)
                        ++count;
                    else if (length <= 1)
                        count--;
                }
        return count;
    }

    public static int DFS(Hex[][] board, int row, int col, boolean[][] visited, String player) 
    {
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};
        
        int length = 0;
        visited[row][col] = true;
        length++;

        for(int i = 0; i < 6; ++i)
            if(isSafe(board, row + rowNo[i], col + colNo[i], visited, player))
                length += DFS(board, row + rowNo[i], col + colNo[i], visited, player);
        return length;
    }

    public static boolean isSafe(Hex[][] board, int row, int col, boolean[][] visited, String player) 
    {
        return row >= 0 && row < boardSize &&
                col >= 0 & col < boardSize &&
                !visited[row][col] && board[row][col].getPlayer() == player;
    }

    public static boolean shouldSwap(Hex firstOppMove)
    {
        int[][] swap_array= {
                    {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, 
                    {0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 1},
                    {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
                    {1, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0},
                    {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                    };

        return swap_array[firstOppMove.getX()][firstOppMove.getY()] == 1;
    }



    public static Hex selectStartingPosition()
    {
        // If player is first to choose, select a random out of * positions
        int[][] maybePos = { {0, 10}, {1, 8}, {1, 3}, {2, 1}, {3, 1}, {4, 1},
                             {5, 1}, {6, 1}, {7, 1}, {3, 10}, {4, 10}, {5, 10},
                             {6, 10}, {7, 10}, {8, 10}, {9, 2}, {9, 8}, {10, 1}
                           }; 
        

        Random rand = new Random();
        int noOfPairs = 18;

        int p = rand.nextInt(noOfPairs);
        return new Hex(maybePos[p][0], maybePos[p][1], null, 0);
    }

    public static ArrayList<Hex> getPossibleMoves(Hex[][] board) {
    	ArrayList<Hex> moves = new ArrayList<Hex>();
    	for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
    		    if(board[i][j].getPlayer() == null) 
    			    moves.add(board[i][j]);

        Collections.shuffle(moves); // shuffling the positions
        return moves;
    }

    public static ArrayList<Hex> getNeighbours(Hex[][] board, Hex position)
    {
        ArrayList<Hex> moves = new ArrayList<Hex>();
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};
        int posX = position.getX();
        int posY = position.getY();
    	for (int i = 0; i < 6; i++)
    	{
    		if( posX + rowNo[i] >= 0 && posX + rowNo[i] < boardSize &&
                posY + colNo[i] >= 0 && posY + colNo[i] < boardSize &&
                (board[posX + rowNo[i]][posY + colNo[i]].getPlayer() == null || board[posX + rowNo[i]][posY + colNo[i]].getPlayer() == position.getPlayer()))
    		{
    			moves.add(new Hex(posX + rowNo[i], posY + colNo[i], position.getPlayer(), position.getHeurValue()));
    		}
    	}
        return moves;
    }

    // Gets all the possible bridge positions for a hex, the two paths leading to the bridge must both be empty
    // Returns an arraylist of the valid bridge positions
    public static ArrayList<Hex> getPossibleBridges(Hex[][] board, Hex position)
    {
        // All the bridge coordinate shifts
        ArrayList<Hex> bridges = new ArrayList<Hex>();
        int posX = position.getX();
        int posY = position.getY();
        // First Path leading to the bridge
        int path1x[] = new int[]{1, 1, 1, -1, -1, -1};
        int path1y[] = new int[]{0, -1, 0, 0, 1, 0};
        // Second Path leading to the bridge
        int path2x[] = new int[]{1, 0, 0, 0, 0, -1};
        int path2y[] = new int[]{-1, -1, 1, -1, 1, 1};
        // Final bridge positions
        int finalx[] = new int[]{2, 1, 1, -1, -1, -2};
        int finaly[] = new int[]{-1, -2, 1, -1, 2, 1};

        for (int i = 0; i < 6; ++i) {
            // Check path 1 empty
            if (checkValidPosition(path1x[i]+posX, path1y[i]+posY) && board[posX + path1x[i]][posY + path1y[i]].getPlayer() == null ) {
                // Check path 2 empty
                if (checkValidPosition(path2x[i]+posX, path2y[i]+posY) && board[posX + path2x[i]][posY + path2y[i]].getPlayer() == null ) {
                    // Check final bridge position valid
                    if (checkValidPosition(finalx[i]+posX, finaly[i]+posY)) {
                        String colour_bridge = board[finalx[i]+posX][finaly[i]+posY].getPlayer();
                        bridges.add(new Hex(posX + finalx[i], posY + finaly[i], colour_bridge, position.getHeurValue()));
                    }
                }
            }
        }
        return bridges;
    }

    // Gets all the instances where the player has a bridge with the opponent blocking one of the paths, the other being empty
    // Returns the coordinates of the empty pathes of such bridges (should be priority moves)
    public static ArrayList<Hex> getBridgesAtRisk(Hex[][] board, String player)
    {
        String opponent = selectOpponent(player);
        // All the bridge coordinate shifts
        ArrayList<Hex> bridges = new ArrayList<Hex>();
        // First Path leading to the bridge
        int path1x[] = new int[]{1, 1, 1, -1, -1, -1};
        int path1y[] = new int[]{0, -1, 0, 0, 1, 0};
        // Second Path leading to the bridge
        int path2x[] = new int[]{1, 0, 0, 0, 0, -1};
        int path2y[] = new int[]{-1, -1, 1, -1, 1, 1};
        // Final bridge positions
        int finalx[] = new int[]{2, 1, 1, -1, -1, -2};
        int finaly[] = new int[]{-1, -2, 1, -1, 2, 1};

        // Loop through each hex
        for (int x = 0; x < boardSize; x++){
            for (int y = 0; y < boardSize; y++){
                Hex position = board[x][y];
                if (position.getPlayer() != player){
                    continue;
                }
                int posX = position.getX();
                int posY = position.getY();

                for (int i = 0; i < 6; i++) {
                    // Check final bridge position has same coloured piece
                    if (checkValidPosition(finalx[i]+posX, finaly[i]+posY) && board[posX + finalx[i]][posY + finaly[i]].getPlayer() == player ) {
                        // Opponent in path 1, path 2 empty
                        if (checkValidPosition(path1x[i]+posX, path1y[i]+posY) && board[posX + path1x[i]][posY + path1y[i]].getPlayer() == opponent ) {
                            if (checkValidPosition(path2x[i]+posX, path2y[i]+posY) && board[posX + path2x[i]][posY + path2y[i]].getPlayer() == null ) {
                                bridges.add(new Hex(posX + path2x[i], posY + path2y[i], null, position.getHeurValue()));
                            }
                        }
                        // Opponent in path 2, path 1 empty
                        else if (checkValidPosition(path1x[i]+posX, path1y[i]+posY) && board[posX + path1x[i]][posY + path1y[i]].getPlayer() == null ) {
                            if (checkValidPosition(path2x[i]+posX, path2y[i]+posY) && board[posX + path2x[i]][posY + path2y[i]].getPlayer() == opponent ) {
                                bridges.add(new Hex(posX + path1x[i], posY + path1y[i], null, position.getHeurValue()));
                            }
                        }
                    }  
                }
            }
        }
        
        return bridges;
    }

    private static boolean checkWinForBluePlayer(Hex[][] board) {
        boolean found = false;
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j< boardSize; j++) {
        		visited[i][j] = false;
        	}
        }
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if ("B".equals(board[i][j].getPlayer()) && !visited[i][j]) {
                    DFS(board, i, j, visited, "B");
                }
            }
        }

        for (int i = 0; i < boardSize; i++) {
            if (visited[boardSize - 1][i] == true) {
                found = true;
                break;
            }
        }
        return found;
    }

    private static boolean checkWinForRedPlayer(Hex[][] board) {
        boolean found = false;
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j< boardSize; j++) {
        		visited[i][j]=false;
        	}
        }
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if ("R".equals(board[i][j].getPlayer()) && !visited[i][j]) {
                    DFS(board, i, j, visited, "R");
                }
            }
        }

        for (int i = 0; i < boardSize; i++) {
            if (visited[i][boardSize - 1] == true) {
                found = true;
                break;
            }
        }
        return found;
    }

    public static boolean checkValidPosition(int x, int y)
    {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    public static String selectOpponent(String c){
        if (c.equals("R")) return "B";
        if (c.equals("B")) return "R";
        return "None";
    }

}

