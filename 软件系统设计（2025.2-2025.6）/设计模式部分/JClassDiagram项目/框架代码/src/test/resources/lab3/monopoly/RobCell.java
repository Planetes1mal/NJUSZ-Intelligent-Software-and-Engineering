// RobCell.java
import java.util.List;

public class RobCell extends Cell {
    @Override
    public void meet(Player player) {
        List<Player> players = game.getPlayers();
        Player closest = findClosestPlayer(player, players);
        if (closest != null) transferMoney(player, closest);
    }

    private Player findClosestPlayer(Player current, List<Player> players) {
        int minDistance = Integer.MAX_VALUE;
        Player closest = null;
        int size = game.getChessboardSize();
        
        for (Player p : players) {
            if (p == current || p.getState() == PlayerState.FAILED) continue;
            int distance = calculateDistance(current.getPosition(), p.getPosition(), size);
            if (distance < minDistance) {
                minDistance = distance;
                closest = p;
            }
        }
        return closest;
    }

    private int calculateDistance(int a, int b, int size) {
        int forward = Math.abs(a - b);
        int backward = size - forward;
        return Math.min(forward, backward);
    }

    private void transferMoney(Player a, Player b) {
        if (a.getMoney() == b.getMoney()) return;
        if (a.getMoney() > b.getMoney()) {
            int amount = a.getMoney() / 2;
            a.loseMoney(amount);
            b.gainMoney(amount);
        } else {
            int amount = b.getMoney() / 2;
            b.loseMoney(amount);
            a.gainMoney(amount);
        }
    }
}