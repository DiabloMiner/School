import greenfoot.*;

public class GameOver extends Actor {
    
    public static final int FONT_SIZE = 80;
    
    public GameOver() {
        GreenfootImage img = new GreenfootImage("Game Over", FONT_SIZE, Color.BLACK, new Color(255, 255, 255, 255));
        setImage(img);
    }
}
