// PropertyCell.java
public class PropertyCell extends Cell {
    public Player owner;
    private final int purchasePrice;
    private final int baseRent;
    private int houseNumber;

    public PropertyCell(int purchasePrice, int baseRent) {
        this.purchasePrice = purchasePrice;
        this.baseRent = baseRent;
        this.houseNumber = 0;
    }

    @Override
    public void meet(Player player) {
        if (owner == null) {
            buy(player);
            return;
        }

        if (owner == player) {
            upgrade();
            return;
        }

        if (owner.getState() == PlayerState.WAITING || 
            owner.getState() == PlayerState.TO_WAITING) return;

        int price = getPrice();
        if (player.getMoney() < price) {
            owner.gainMoney(player.getMoney());
            player.bankrupt();
        } else {
            owner.gainMoney(price);
            player.loseMoney(price);
        }
    }

    private void buy(Player player) {
        if (player.getMoney() >= purchasePrice) {
            owner = player;
            houseNumber = 1;
            player.estates.add(this);
            player.loseMoney(purchasePrice);
        }
    }

    private void upgrade() {
        if (houseNumber < 5) houseNumber++;
    }

    public int getPrice() {
        return baseRent * (1 << (houseNumber - 1));
    }

    public int getValue() {
        return purchasePrice + (houseNumber - 1) * baseRent;
    }

    public void clearHouse() {
        houseNumber = 0;
    }
}