// Player.java
import java.util.ArrayList;
import java.util.List;

public class Player {
    private PlayerState state;
    private final int id;
    private int money;
    private int position;
    private int rate;
    public List<PropertyCell> estates = new ArrayList<>();

    public Player(int id, int money) {
        this.state = PlayerState.PLAYING;
        this.id = id;
        this.money = money;
        this.position = 0;
        this.rate = 1;
    }

    public void bankrupt() {
        state = PlayerState.FAILED;
        for (PropertyCell estate : estates) {
            estate.owner = null;
            estate.clearHouse();
        }
        estates.clear();
        System.out.println("Player " + id + " has gone bankrupt and exited the game.");
    }

    public int getTotalAsset() {
        int value = money;
        for (PropertyCell estate : estates) {
            value += estate.getValue();
        }
        return value;
    }

    // Getters and setters
    public int getId() { return id; }
    public int getMoney() { return money; }
    public void gainMoney(int money) {
        this.money += money * rate;
        if (rate == 2) rate = 1;
    }
    public void loseMoney(int money) { this.money -= money; }
    public void setRate(int rate) { this.rate = rate; }
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
}