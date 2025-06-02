public class WaitCell extends Cell {
    @Override
    public void meet(Player player) {
        player.setState(PlayerState.TO_WAITING);
    }
}