// Cell.java
public abstract class Cell {
    protected Game game;

    public void setGame(Game game) {
        this.game = game;
    }

    public abstract void meet(Player player);
}