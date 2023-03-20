import java.io.Serializable;
import java.util.*;
public class GameProgress implements Serializable {
    private static final long serialVersionUID = 1L;

    private int health;
    private int weapons;
    private int lvl;
    private double distance;

    public GameProgress(int health, int weapons, int lvl, double distance) {
        this.health = health;
        this.weapons = weapons;
        this.lvl = lvl;
        this.distance = distance;
    }
    public static String getID(){
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        sb.append("SAVE-");
        for (int i = 0; i < 15; i++) {
            if (i % 5 == 0 && i != 0) sb.append("-");
            else sb.append((r.nextInt(0,3) == 2) ?
                    (char)r.nextInt(48,57) :
                    (char)r.nextInt(65,90));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "GameProgress{" +
                "health=" + health +
                ", weapons=" + weapons +
                ", lvl=" + lvl +
                ", distance=" + distance +
                '}';
    }
}