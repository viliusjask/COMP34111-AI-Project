package agents.group41;

import agents.group41.Hex;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

class Agent41{
    public static String HOST = "127.0.0.1";
    public static int PORT = 1234;

    private Socket s;
    private PrintWriter out;
    private BufferedReader in;
    

    private String colour = "R";
    private int turn = 0;
    private int boardSize = 11;

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
            bestMove.setHeurValue(bestScore)
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
        
        int bridgeHeur = bridgeFactor(board);
        int dijkstraHuer = dijkstra(board);

        int playerScore = connectedNodes(board);

        int opponentScore = connectedNodes(board);

        return 6 * bridgeHeur + dijkstraHuer + (playerScore - opponentScore);
    }

    // bridge heuristics
    public static int bridgeFactor(Hex[][] board)
    {

    }

    // Dijkstra heur
    public static int dijkstra(Hex[][] board, Hex source)
    {

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

    public static void DFS(Hex[][] board, row, col, boolean[][] visited, String player) 
    {
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};

        visited[row][col] = true;

        for(int i = 0; i < 6; ++i)
            if(isSafe(board, row + rowNo[i], col + colNo[i], visited, player))
                DFS(board, row + rowNo[i], col + colNo[i], visited, player);

    }

    public static boolean isSafe(Hex[][] board, row, col, boolean[][] visited, String player) 
    {
        return row >= 0 && row < boardSize &&
                col >= 0 & col < boardSize &&
                !visited[row][col] && board[row][col].getPlayer() == player;
    }

    public static ArrayList<Hex> getPossibleMoves(Hex[][] board) {
    	ArrayList<Hex> moves = new ArrayList<Hex>();
    	for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
    		    if(board[i][j].getPlayer() == null) 
    			    moves.add(new Hex(i, j, null, board[i][j].getHeurValue());

        Collections.shuffle(moves); // shuffling the positions
        return moves;
    }

    public static ArrayList<Hex> getNeighbours(Hex[][] board, Hex source)
    {
        ArrayList<Hex> moves = new ArrayList<Hex>();
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};
        int posX = position.getX();
        int posY = position.getY();
    	for (int i = 0; i < 6; ++i)
    		if(board[posX + rowNo[i]][posY + colNo[i]].getPlayer() == null && posX + rowNo[i] >= 0 && posX + rowNo[i] < boardSize
               && posY + colNo[i] >= 0 && posY + colNo[i] < boardSize) 
    			moves.add(new Hex(posX + rowNo[i], posY + colNo[i], null, position.getHeurValue());
        return moves;
    }

    public static ArrayList<Hex> getBridges(Hex[][] board, Hex source)
    {

    }

    boolean checkWinForBluePlayer(Hex[][] board) {
        boolean found = false;
        boolean visited[][] = new boolean[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
        	for (int j = 0; j< boardSize; j++) {
        		visited[i][j] = false;
        	}
        }
        for (int i = 0; i < 1; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                if (board[i][j].getPlayer() == "B" && !visited[i][j]) {
                    DFS(game, i, j, visited, 2);
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
        for (int i=0; i <boardSize; ++i) {
        	for (int j=0; j<boardSize; ++j) {
        		visited[i][j]=false;
        	}
        }
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < 1; ++j) {
                if (board[i][j].getPlayer() =="R" && !visited[i][j]) {
                    DFS(game, i, j, visited, 1);
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

    public static void DFS(Hex[][] board, int row, int col, boolean visited[][], int value)
    {

    }

    public static boolean isSafe(Hex[][] board, int row, int col, boolean visited[][], int value)
    {

    }

    public static ArrayList<Hex> getLegalMoves(Hex[][] board, int marker) 
    {

    }

    public static ArrayList<Hex> selectStartingPosition(Hex[][] board)
    {

    }

    public static boolean checkValidPositions(int x, int y)
    {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    public static String selectOpponent(String c){
        if (c.equals("R")) return "B";
        if (c.equals("B")) return "R";
        return "None";
    }
    
    // public static Hex[][] dijkstra(Hex[][] board, Hex source) {
    //     // initialise heuristics
    // 	for (int i = 0; i < boardSize; i++)
    //         for (int j = 0; j < boardSize; j++)
    //         {
    //             board[i][j].setHeurValue = Float.POSITIVE_INFINITY;
    //         }
    //     // Source distance is 0
    //     board[source.getX()][source.getY()].setHeurValue(0);

    //     dijkstraHelper(board, source)
        
    // 	return board;
    // }
    
    // public static Hex[][] dijkstraHelper(Hex[][] board, Hex source) {
    //     ArrayList<Hex> movesFromSource, movesFromPos;
    //     // Get moves from source
    //     movesFromSource = getNeighbours(board, source);
    //     Set<Hex> setOfMoves = new HashSet<Hex>(movesFromSource);
    //     Iterator<Hex> itr = setOfStocks.iterator();
    //     while(itr.hasNext())
    //     {
    //         Hex move = itr.next();
    //         int x = move.getX();
    //         int y = move.getY();
    //         Hex pos = board[x][y];
    //         movesFromPos = getNeighbours(board, pos);
    //         for(int i = 0; i < movesFromPos.size(); i++)
    //         {
    //             Hex neigh = movesFromPos.get(i);
    //             // Considering distance from one neighbour to other as 1
    //             // SET HEUR VALUE BACK TO INT SINCE WE ADD 1 ???
    //             float newDist = pos.getHeurValue() + 1;
    //             if(newDist < neigh.getHeurValue())
    //                 board[neigh.getX()][neigh.getY()] = newDist;

    //             if(board[neigh.getX()][neigh.getY()].getHeurValue() == Float.POSITIVE_INFINITY)
    //                 setOfMoves.add(neigh);
    //         }
    //     }
    // }

}


