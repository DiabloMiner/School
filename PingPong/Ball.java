import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Ball extends Actor
{   
    public static final int SPEED = 8;
    public static final int WIDTH = 30;
    public static final int HEIGHT = 30;
    private double rotation;
    private double vx = SPEED;
    private double vy = SPEED;
    private double x;
    private double y;
    
    public Ball(int x, int y, int maxRotation) {
        this.x = x;
        this.y = y;
        randomizeRotation(maxRotation > 360 ? 361 : maxRotation + 1);
        draw();
    }
    
    public void act() {
        double realVx = getRealVx();
        double realVy = getRealVy();
        
        // Here it is checked if the ball is on the border of the world and the ball is bounced back
        if ((x + realVx) <= 0 || (x + realVx) >= getWorld().getWidth()) {
            // If it is the x border of the world, a point is added to the score of the player, who scored a point
            vx *= -1;
            handleScore(realVx, realVy);
        }
        if ((y + realVy) <= 0 || (y + realVy) >= getWorld().getHeight()) {
            vy *= -1;
        }
        
        changeLocation(realVx, realVy);
    }
    
    public void handleScore(double realVx, double realVy) {
        // Here the current position is tested, to find out who gets a point to their score
        if ((x + realVx) >= getWorld().getWidth()) {
             getWorld().getObjects(Score.class).get(0).addToLeftScore();
        }
        if ((x + realVx) <= 0) {
             getWorld().getObjects(Score.class).get(0).addToRightScore();
        }
    }
    
    public void randomizeRotation(int upperLimit) {
        // Internal rotation is randomized, but if the rotation is 90, 180 , 270 or 360 it is randomized again
        rotation = 0;
        while (rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270 || rotation == 360) {
            rotation = Greenfoot.getRandomNumber(upperLimit);
        }
    }
    
    public void correctCollissionX(Paddle paddle) {
        // The ball is moved out of the colliding paddle in x direction with a while loop
        boolean isIntersecting = intersects(paddle);
        while (isIntersecting) {
            double realVx = getRealVx();
            this.x += 0.5 * realVx;
            changeLocation(realVx, getRealVy());
            if (!intersects(paddle)) {
                isIntersecting = false;
                break;
            }
        }
    }
    
    public void correctCollissionY(Paddle paddle) {
        // The ball is moved out of the colliding paddle in y direction with a while loop
        boolean isIntersecting = intersects(paddle);
        while (isIntersecting) {
            double realVy = getRealVy();
            this.y += 3 * realVy;
            changeLocation(getRealVx(), realVy);
            if (!intersects(paddle)) {
                isIntersecting = false;
                break;
            }
        }
    }
    
    public void changeDirectionX() {
        // The x speed is multiplied with negative one
        vx *= -1;
    }
    
    public void changeDirectionY() {
        // The y speed is multiplied with negative one
        vy *= -1;
    }
    
    public void changeLocation(double vx, double vy) {
        // The speed is added to the internal position and the position is set to the typed casted version of the internal position
        x += vx;
        y += vy;
        setLocation((int) x, (int) y);
    }
    
    public double getRealVx() {
        // The x speed multplied with the cosinus of the rotation is returned
        return (vx * Math.cos(Math.toRadians(rotation)));
    }
    
    public double getRealVy() {
        // The y speed multplied with the sinus of the rotation is returned
        return (vy * Math.sin(Math.toRadians(rotation)));
    }
    
    public void draw() {
        // The image is imported, scaled and set
        GreenfootImage img = new GreenfootImage("ball.png");
        img.scale(WIDTH, HEIGHT);
        setImage(img);
    }
    
    public void addToSimulationRotation(double rotation) {
        this.rotation += rotation;
    }
}
