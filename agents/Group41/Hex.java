package group41;

public class Hex {
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
