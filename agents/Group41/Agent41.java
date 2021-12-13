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
        agent.run();
    }
    
    public static int findShortestPath() {
    	//Djikstra's algorithm
    	return -1;
    }
    
    public static int evaluateBoard(BoardState boardState) { 
    	//
    	//Implement and compare findShortestPath() somehow
    	//
    	return -1;
    }
    
    public static int minimax(int depth, int playerTurn, String player) {
    	//for i < movesAvailable
    	//	if player 0
    	//		makeMove()
    	//		currentScore = minimax(depth+1, 1)
    	//		max = max(currentScore, max)
    	//	else
    	// 		makeMove
    	//		currentScore =minimax(depth+1, 0)
    	//		min = min(currentScore, min)
    	return -1;
    }
    
    public static int maxvalue(int[][] board, int alpha, int beta) {
    	moves = getAvailableMoves(board);
    	int bestValue = Integer.MIN_VALUE;
    		for move in moves {
    			int[][] updatedBoard = updateBoardWithMove(board, move, maxPlayer)
                bestValue = max(bestValue, alphaBetaPrunedMiniMax(board: updatedBoard, maximizingPlayer: false, depth: depth-1, alpha: alpha, beta: beta))
                alpha = max(alpha, bestValue)
                if beta <= alpha {
                	break
                }
    		}
    	return bestValue;
    }
    
    public static ArrayList<Pair<Integer,Integer>> getAvailableMoves(int[][] board) {
    	ArrayList<Pair> moves = new ArrayList<Pair<Integer,Integer>>();
    	for (int i = 0; i < boardSize; i++) 
        {
    		for (int j = 0; j < boardSize; j++) 
            {
    			if(board[i][j] == 0) 
    				moves.add(new Pair(i, j));
    		}
    	}
        return moves;
    }
    
    public static void updateBoardWithMove(int[][] board, Pair<Integer,Integer> move, String player) {
    	// 1 for player red, 2 for player blue
        int move1 = move.getKey();
        int move2 = move.getValue();
    	if(player == "R") 
        {
    		board[move1][move2] = 1;
    	}
    	else if(player == "B")
        {
    		board[move1][move2] = 2;
    	}
    }
}

// public class BoardState {
// 	public int x;
// 	public int y;
	
// 	public int[] getContents() {
// 		return (this.x this.y);
// 	}
// 	public void setContents(int x, int y) {
// 		this.x = x;
// 		this.y = y;
// 	}
// }
