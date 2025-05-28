// Main.java
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int m = sc.nextInt();
        int t = sc.nextInt();
        int v = sc.nextInt();

        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            players.add(new Player(i, v));
        }

        List<Cell> chessboard = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            chessboard.add(CellFactory.createCell(sc.next()));
        }

        int k = sc.nextInt();
        List<Integer> diceNumbers = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            diceNumbers.add(sc.nextInt());
        }

        Game game = new Game(m, new Dice(diceNumbers), chessboard, players, t);
        game.play();
    }
}