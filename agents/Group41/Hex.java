package agents.group41;

public class Hex {
    int x;
    int y;
    String player;
    float heurValue;

    public Hex() {
    }

    public Hex(int x, int y, String player, float heurValue) {
        this.x = x;
        this.y = y;
        this.player = player;
        this.heurValue = heurValue;
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

    public float getHeurValue() {
        return this.heurValue;
    }

    public void setHeurValue(float heurValue) {
        this.heurValue = heurValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Hex)) {
            return false;
        }
        Hex hex = (Hex) o;
        return x == hex.x && y == hex.y && player == hex.player && heurValue == hex.heurValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, player, heurValue);
    }

    @Override
    public String toString() {
        return "{" +
            " x='" + getX() + "'" +
            ", y='" + getY() + "'" +
            ", player='" + getPlayer() + "'" +
            ", heurValue='" + getHeurValue() + "'" +
            "}";
    }

}