import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Ball extends Actor
{
    public static final int WIDTH = 30;
    public static final int HEIGHT = 30;
    private double rotation;
    private double vx = 8;
    private double vy = 8;
    private double x;
    private double y;
    
    public Ball(int x, int y) {
        this.x = x;
        this.y = y;
        randomizeRotation();
        draw();
    }
    
    public void act() {
        double realVx = vx * Math.cos(Math.toRadians(rotation));
        double realVy = vy * Math.sin(Math.toRadians(rotation));
        
        if ((x + realVx) <= 0 || (x + realVx) >= getWorld().getWidth()) {
            vx *= -1;
            handleScore(realVx, realVy);
        }
        if ((y + realVy) <= 0 || (y + realVy) >= getWorld().getHeight()) {
            vy *= -1;
        }
        
        changeLocation(realVx, realVy);
    }
    
    public void handleScore(double realVx, double realVy) {
        if ((x + realVx) >= getWorld().getWidth()) {
             getWorld().getObjects(Score.class).get(0).addToLeftScore();
        }
        if ((x + realVx) <= 0) {
             getWorld().getObjects(Score.class).get(0).addToRightScore();
        }
    }
    
    public void resetPosition() {
        this.x = getWorld().getWidth()/2;
        this.y = getWorld().getHeight()/2;
        setLocation(getWorld().getWidth()/2, getWorld().getHeight()/2);
    }
    
    public void randomizeRotation() {
         rotation = Greenfoot.getRandomNumber(361);
    }
    
    public void changeDirectionX() {
        vx *= -1;
    }
    
    public void changeDirectionY() {
        vy *= -1;
    }
    
    public void changeLocation(double vx, double vy) {
        x += vx;
        y += vy;
        setLocation((int) x, (int) y);
    }
    
    public void draw() {
        GreenfootImage img = new GreenfootImage("ball.png");
        img.scale(WIDTH, HEIGHT);
        setImage(img);
    }
}
