package agents.group41;

import agents.group41.Hex;
import java.net.*;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import java.io.*;

class Agent41{
    public static String HOST = "127.0.0.1";
    public static int PORT = 1234;

    private Socket s;
    private PrintWriter out;
    private BufferedReader in;
    

    private static String colour = "R";
    private int turn = 0;
    private static int boardSize = 11;

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
                if (msg[1].equals("SWAP")) colour = opp(colour);
                if (msg[3].equals(colour)) makeMove(msg[2]);
                break;

            default:
                return false;
        }

        return true;
    }

    // CALL minimax in this 
    private void makeMove(String board){
        if (turn == 2 && new Random().nextInt(2) == 1){
            sendMessage("SWAP\n");
            return;
        }

        String[] lines = board.split(",");
        ArrayList<int[]> choices = new ArrayList<int[]>();

        for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
                if (lines[i].charAt(j) == '0'){
                    int[] newElement = {i, j};
                    choices.add(newElement);
                }

        if (choices.size() > 0){
            int[] choice = choices.get(new Random().nextInt(choices.size()));
            String msg = "" + choice[0] + "," + choice[1] + "\n";
            sendMessage(msg);
        }
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

    public static Hex minimax(Hex[][] board, String player, int depth, int alpha, int beta) {
        Hex bestMove = new Hex(-1, -1, colour, -1);

        ArrayList<Hex> possibleMoves;
        possibleMoves = getPossibleMoves(board, position);
        if(depth == 0 || possibleMoves.size() == 0)
        {
            bestScore = getBoardState(board, marker);
            bestMove.setHeurValue(bestScore);
            return bestMove;
        }

        int bestEval;
        if(player == "B") // max player
        {
            bestEval = Integer.NEGATIVE_INFINITY;
            for(int i = 0; i < possibleMoves.size(); i++)
            {
                int currentX = possibleMoves.get(i).getX();
                int currentY = possibleMoves.get(i).getY();
                Hex currentMove = board[currentX][currentY];

                board[currentX][currentY].setPlayer("B");

                Hex eval = minimax(board, "R", depth - 1, alpha, beta);
                int evalVal = eval.getHeurValue();
                if(bestEval < evalVal)
                {
                    bestEval = evalVal;
                    bestMove.setHeurValue(bestEval);
                    bestMove = currentMove;
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
        else if(player == "R") // min player
        {
            bestEval = Integer.POSITIVE_INFINITY;
            for(int i = 0; i < possibleMoves.size(); i++)
                {
                    int currentX = possibleMoves.get(i).getX();
                    int currentY = possibleMoves.get(i).getY();
                    Hex currentMove = board[currentX][currentY];

                    board[currentX][currentY].setPlayer("R");
             
                    Hex eval = minimax(board, "B", depth - 1, alpha, beta);
                    int evalVal = eval.getHeurValue();
                    if(bestEval > evalVal)
                    {
                        bestEval = evalVal;
                        bestMove.setHeurValue(bestEval);
                        bestMove = currentMove;
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
        int dijkstraHuer = dijkstra(board);

        int playerScore = connectedNodes(board);

        int opponentScore = connectedNodes(board);

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
                    ArrayList<Point> bridges = getPossibleBridges(board, board[i][j]);
                    for (Hex h : bridges) {
                        // Bridge exists for maximising player
                        if (player == "B" && h.getPlayer() == "B") {
                            score += 5;
                        // Bridge exists for minimizing player
                        } else if (player == "R" && h.getPlayer() == "R") {
                            score += -5;
                        }
                    }
                }
            }
        }
        return score;
    }

    // Dijkstra heur

    public static int dijkstra(Hex[][] board, Hex source)
    {
    	boolean visited[][] = new boolean[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j < boardSize; j++) {
        		visited[i][j] = false;
        	}
        }

        
    	ArrayList<Hex> vertices = new ArrayList<Hex>();
    	for (int i = 0; i<boardsize; i++) {
    		for (int j = 0; j<boardsize; j++) {
        		vertices.add(board[i][j]);
        	}
    	}
    	Hex L = new Hex(-1,0,"B",0);
    	Hex R = new Hex(boardSize+1,0,"B",0);
    	Hex T = new Hex(0,boardSize+1,"R",0);
    	Hex D = new Hex(0,-1,"R",0);
    	
    	if (player == "R") {
    		Hex source = T;
    		Hex destination = D;
    	}
    	else if (player == "B") {
    		Hex source = L;
    		Hex destination = R;
    	}
    	
    	for (int i = 0; i < vertices.size(); i++) {
    		Hex vertex = vertices.get(i);
    		vertex.clearVertexCache();
    	}
    	ArrayList<Hex> currentVertices = vertices;
    	source.pathLengthFromSource = 0;
    	source.pathVerticesFromSource.add(source);
    	
    	
    	Queue<E> verticesQueue = new PriorityQueue();
    	
    	if (player == "R") {
    		for (int i = 0; i < boardSize; i++) {
    			verticesQueue.add(board[0][i]);
    			visited[0][i] = true;
    		}
    	}
    	else if (player == "B") {
    		for (int i = 0; i < boardSize; i++) {
    			verticesQueue.add(board[i][0]);
    			visited[i][0] = true;
    		}
    	}
    	
    	
    	
    	while (verticesQueue.size() != 0) {
    		Hex currentVertex = verticesQueue.poll();
    		ArrayList<Hex> neighbours = getNeighbours(board, currentVertex);
    		for (int i = 0; i < neighbours.size(); i++) {
    			currentNeighbour = neighbours.get(i);
                int neighDist = currentVertex.pathLengthFromSource();
                if(currentNeighbour.getPlayer() != player)
                    neighDist++;
    			
    			if (neighDist < currentNeighbour.pathLengthFromSource()) {
    				currentNeighbour.pathLengthFromSource = neighDist;
    				board[currentNeighbour.getX()][currentNeighbour.getY()].setPathLengthFromSource(neighDist);
    			}
    			
    			
    		    //int pathLengthFromSource;
    		    //ArrayList<Hex> pathVerticesFromSource;
    			
    			if (!visited[currentNeighbour.getX()][currentNeighbour.getY()] || currentNeighbour.getPlayer() == player) {
    				verticesQueue.add(currentNeighbour);
    				visited[currentNeighbour.getX()][currentNeighbour.getY()] = true;
    			}
    		}
    		
    	}
    	
    	
    	
    	
    }

    // Connected nodes heur
    public static int connectedNodes(Hex[][] board, String player )
    {
        boolean visited[][] = new boolean[boardSize][boardSize];
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
                        counter--;
                }
        return count;
    }

    public static void DFS(Hex[][] board, int row, int col, boolean[][] visited, String player) 
    {
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};

        visited[row][col] = true;

        for(int i = 0; i < 6; ++i)
            if(isSafe(board, row + rowNo[i], col + colNo[i], visited, player))
                DFS(board, row + rowNo[i], col + colNo[i], visited, player);

    }

    public static boolean isSafe(Hex[][] board, int row, int col, boolean[][] visited, String player) 
    {
        return row >= 0 && row < boardSize &&
                col >= 0 & col < boardSize &&
                !visited[row][col] && board[row][col].getPlayer() == player;
    }

    public static ArrayList<Hex> selectStartingPosition(Hex[][] board)
    {

    }

    public static ArrayList<Hex> getPossibleMoves(Hex[][] board) {
    	ArrayList<Hex> moves = new ArrayList<Hex>();
    	for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
    		    if(board[i][j].getPlayer() == null) 
    			    moves.add(new Hex(i, j, null, board[i][j].getHeurValue()));

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
    public static ArrayList<Hex> getBridgesAtRisk(Hex[][] board, Hex position, String player)
    {
        String opponent = selectOpponent(player);
        // All the bridge coordinate shifts
        ArrayList<Hex> bridges = new ArrayList<Hex>();
        if (position.getPlayer() != player){
            return bridges;
        }
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
        return bridges;
    }

    boolean checkWinForBluePlayer(Hex[][] board) {
        boolean found = false;
        boolean visited[][] = new boolean[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j< boardSize; j++) {
        		visited[i][j] = false;
        	}
        }
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j].getPlayer() == "B" && !visited[i][j]) {
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

    boolean checkWinForRedPlayer(Hex[][] board) {
        boolean found = false;
        boolean visited[][] = new boolean[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j< boardSize; j++) {
        		visited[i][j]=false;
        	}
        }
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < 1; ++j) {
                if (board[i][j].getPlayer() == "R" && !visited[i][j]) {
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


