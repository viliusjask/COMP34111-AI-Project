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
    private int[][] board = new int[boardSize][boardSize];

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

    public static String opp(String c){
        if (c.equals("R")) return "B";
        if (c.equals("B")) return "R";
        return "None";
    }

    public static void main(String args[]){
        Agent41 agent = new Agent41();
        Hex[][] hexBoard = new Hex[boardSize][boardSize];
        agent.run();
    }
    
    public static Hex[][] dijkstra(Hex[][] board, Hex source) {
        // initialise heuristics
    	for (int i = 0; i < boardSize; i++)
            for (int j = 0; j < boardSize; j++)
            {
                board[i][j].setHeurValue = Float.POSITIVE_INFINITY;
            }
        // Source distance is 0
        board[source.getX()][source.getY()].setHeurValue(0);

        dijkstraHelper(board, source)
        
    	return board;
    }
    
    public static Hex[][] dijkstraHelper(Hex[][] board, Hex source) {
        ArrayList<Hex> movesFromSource, movesFromPos;
        // Get moves from source
        movesFromSource = getPossibleMoves(board, source);
        for(int i = 0; i < movesFromSource.size(); i++)
        {
            int x = movesFromSource.get(i).getX();
            int y = movesFromSource.get(i).getY();
            Hex pos = board[x][y];
            movesFromPos = getPossibleMoves(board, pos);
            for(int i = 0; i < movesFromPos.size(); i++)
            {
                Hex neigh = movesFromPos.get(i);
                // Considering distance from one neighbour to other as 1
                // SET HEUR VALUE BACK TO INT SINCE WE ADD 1 ???
                float newDist = pos.getHeurValue() + 1;
                if(newDist < neigh.getHeurValue())
                    board[neigh.getX()][neigh.getY()] = newDist;

                dijkstraHelper(board, neigh)
            }
        }
    }
    
    public static int evaluateBoard() { 
    	//
    	//Implement and compare findShortestPath() somehow
    	//
    	return -1;
    }
    
    public static float minimax(Hex position, Hex[][] board, int depth, String player, int alpha, int beta) {
        if(depth == 0 || getPossibleMoves(board, position).size() == 0)
        {
            return position.getHeurValue();
        }

        ArrayList<Hex> possibleMoves;
        possibleMoves = getPossibleMoves(board, position);

        
        // Hex bestMove = getBestMove();
        // DECISION HERE TO BE ADDED
        // Make move
        // board[x][y].setPlayer(player);

        if(player == "B") // max player
        {
            float maxEval = Float.NEGATIVE_INFINITY;
            for(int i = 0; i < possibleMoves.size(); i++)
                float eval = minimax(possibleMoves.get(i), board, depth - 1, player, alpha, beta);
                maxEval = max(maxEval, eval);
                alpha = max(alpha, eval);
                if(beta <= alpha)
                    break;
            return maxEval;
        } 
        else if(player == "R") // min player
        {
            float minEval = Float.POSITIVE_INFINITY;
            for(int i = 0; i < possibleMoves.size(); i++)
                int eval = minimax(possibleMoves.get(i), board, depth - 1, player, alpha, beta);
                minEval = min(minEval, eval);
                beta = min(beta, eval);
                if(beta <= alpha)
                    break;
            return minEval;
        }
        //Undo Move
        // board[currentMove.getX()][currentMove.getY()].setPlayer(null);
        
            
    	return -1;
    }
    
    // public static int maxvalue(Hex[][] board, int alpha, int beta) {
    // 	moves = getAvailableMoves(board);
    // 	int bestValue = Integer.MIN_VALUE;
    // 		for move in moves {
    // 			int[][] updatedBoard = updateBoardWithMove(board, move, maxPlayer)
    //             bestValue = max(bestValue, alphaBetaPrunedMiniMax(board: updatedBoard, maximizingPlayer: false, depth: depth-1, alpha: alpha, beta: beta))
    //             alpha = max(alpha, bestValue)
    //             if beta <= alpha {
    //             	break
    //             }
    // 		}
    // 	return bestValue;
    // }
    
    public static ArrayList<Hex> getPossibleMoves(Hex[][] board, Hex position ) {
    	ArrayList<Hex> moves = new ArrayList<Hex>();
        int rowNo[] = new int[]{-1, -1, 0, 0, 1, 1};
        int colNo[] = new int[]{0, 1, -1, 1, -1, 0};
        int posX = position.getX();
        int posY = position.getY();
    	for (int i = 0; i < 6; ++i)
    		if(board[posX + rowNo[i]][posY + colNo[i]].getPlayer() == null) 
    			moves.add(new Hex(posX + rowNo[i], posY + colNo[i], null, position.getHeurValue());
    		
    	}
        return moves;
    }
    
    // Possible implementation for some special case
    public static ArrayList<Hex> checkBridges(Hex[][] board, Hex position ) {
        report null;
    }

    
    public static void updateBoardWithMove(Hex[][] board, Pair<Hex> move, String player) {
        int move1 = move.getX();
        int move2 = move.getY();
    	if(player == "R") 
        {
    		board[move1][move2].setPlayer("R");
    	}
    	else if(player == "B")
        {
    		board[move1][move2].setPlayer("B");
    	}
    }
}


