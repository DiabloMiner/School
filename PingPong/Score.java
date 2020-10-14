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
        // The text is created with GreenfootImage and set
        GreenfootImage img = new GreenfootImage(this.scoreLeft  + " : " + this.scoreRight, FONT_SIZE, Color.BLACK, new Color(255, 255, 255, 255));
        setImage(img);
    }
    
    public void addToLeftScore() {
        // A point is added to the score of the left player and the score is drawn anew
        this.scoreLeft += 1;
        draw();
    }
    
    public void addToRightScore() {
        // A point is added to the score of the right player and the score is drawn anew
        this.scoreRight += 1;
        draw();
    }
}
