import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Score extends Actor
{   
    public static final int FONT_SIZE = 40;
    private int scoreLeft;
    private int scoreRight;
    
    public Score() {
        this.scoreLeft = 0;
        this.scoreRight = 0;
        draw();
    }
    
    public void act() {}    
    
    public void draw() {
        GreenfootImage img = new GreenfootImage(this.scoreLeft  + " : " + this.scoreRight, FONT_SIZE, Color.BLACK, new Color(255, 255, 255, 255));
        setImage(img);
    }
    
    public void addToLeftScore() {
        this.scoreLeft += 1;
        draw();
    }
    
    public void addToRightScore() {
        this.scoreRight += 1;
        draw();
    }
}
