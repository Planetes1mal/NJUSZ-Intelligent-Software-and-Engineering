// MoneyCell.java
public class MoneyCell extends Cell {
    private final int gainMoney;

    public MoneyCell(int gainMoney) {
        this.gainMoney = gainMoney;
    }

    @Override
    public void meet(Player player) {
        player.gainMoney(gainMoney);
        player.setRate(2);
    }
}