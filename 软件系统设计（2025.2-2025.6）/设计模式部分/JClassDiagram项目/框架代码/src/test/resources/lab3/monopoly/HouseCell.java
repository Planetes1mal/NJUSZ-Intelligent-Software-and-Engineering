public class HouseCell extends Cell {
    @Override
    public void meet(Player player) {
        game.addHouse(player);
    }
}