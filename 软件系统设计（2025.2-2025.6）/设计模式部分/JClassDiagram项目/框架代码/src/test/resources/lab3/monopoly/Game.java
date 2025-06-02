// Game.java
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final int chessboardSize;
    private final Dice dice;
    private final List<Player> players;
    private final List<Cell> chessboard;
    private final int rounds;

    public Game(int chessboardSize, Dice dice, List<Cell> chessboard, 
               List<Player> players, int rounds) {
        this.chessboardSize = chessboardSize;
        this.dice = dice;
        this.players = new ArrayList<>(players);
        this.chessboard = new ArrayList<>(chessboard);
        this.rounds = rounds;
        
        for (Cell cell : this.chessboard) {
            cell.setGame(this);
        }
    }

    public void play() {
        for (int round = 1; round <= rounds; round++) {
            processRound(round);
            if (checkGameEnd()) return;
        }
        declareWinner();
    }

    private void processRound(int round) {
        for (Player player : players) {
            if (shouldSkipPlayer(player)) continue;
            movePlayer(player);
        }
        updatePlayerStates();
        printRoundStatus(round);
    }

    private boolean shouldSkipPlayer(Player player) {
        return player.getState() == PlayerState.WAITING || 
               player.getState() == PlayerState.FAILED;
    }

    private void movePlayer(Player player) {
        int steps = dice.getSteps() + dice.getSteps();
        int oldPos = player.getPosition();
        int newPos = (oldPos + steps) % chessboardSize;
        player.setPosition(newPos);

        if (oldPos + steps > chessboardSize) {
            player.gainMoney(200);
        }

        chessboard.get(newPos).meet(player);
    }

    private void updatePlayerStates() {
        for (Player player : players) {
            if (player.getState() == PlayerState.TO_WAITING) {
                player.setState(PlayerState.WAITING);
            } else if (player.getState() == PlayerState.WAITING) {
                player.setState(PlayerState.PLAYING);
            }
        }
    }

    private void printRoundStatus(int round) {
        System.out.println("Round " + round);
        for (Player player : players) {
            if (player.getState() == PlayerState.FAILED) continue;
            System.out.printf("%d %d %d%n", 
                player.getId(), 
                player.getMoney(), 
                player.getTotalAsset() - player.getMoney());
        }
    }

    private boolean checkGameEnd() {
        int alive = 0;
        Player winner = null;
        for (Player p : players) {
            if (p.getState() != PlayerState.FAILED) {
                alive++;
                winner = p;
            }
        }
        if (alive == 1) {
            System.out.println("Player " + winner.getId() + " wins the game!");
            return true;
        }
        return false;
    }

    private void declareWinner() {
        Player winner = null;
        int maxAsset = -1;
        for (Player p : players) {
            if (p.getState() == PlayerState.FAILED) continue;
            int asset = p.getTotalAsset();
            if (asset > maxAsset) {
                maxAsset = asset;
                winner = p;
            }
        }
        System.out.println("Player " + winner.getId() + " wins the game!");
    }

    public void addHouse(Player player) {
        List<Player> candidates = findWeakestPlayers(player);
        upgradeProperties(candidates);
    }

    private List<Player> findWeakestPlayers(Player current) {
        List<Player> weakest = new ArrayList<>();
        int minAsset = Integer.MAX_VALUE;
        
        for (Player p : players) {
            if (p.getState() == PlayerState.FAILED) continue;
            int asset = p.getTotalAsset();
            if (asset < minAsset) {
                minAsset = asset;
                weakest.clear();
                weakest.add(p);
            } else if (asset == minAsset) {
                weakest.add(p);
            }
        }
        
        if (!weakest.contains(current)) {
            weakest.add(current);
        }
        return weakest;
    }

    private void upgradeProperties(List<Player> candidates) {
        for (Player p : candidates) {
            PropertyCell best = null;
            for (PropertyCell prop : p.estates) {
                if (best == null || prop.getPrice() > best.getPrice()) {
                    best = prop;
                }
            }
            if (best != null) best.upgrade();
        }
    }

    // Getters
    public List<Player> getPlayers() { return new ArrayList<>(players); }
    public int getChessboardSize() { return chessboardSize; }
}