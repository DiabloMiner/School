import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Ball extends Actor
{   
    public static final int SPEED = 8;
    public static final int WIDTH = 30;
    public static final int HEIGHT = 30;
    private double rotation;
    private double vx = SPEED;
    private double vy = SPEED;
    private double lastVx = vx;
    private double lastVy = vy;
    private double x;
    private double y;
    
    public Ball(int x, int y) {
        this.x = x;
        this.y = y;
        randomizeRotation();
        draw();
    }
    
    public void act() {
        double realVx = getRealVx();
        double realVy = getRealVy();
        
        if ((x + realVx) <= 0 || (x + realVx) >= getWorld().getWidth()) {
            // System.out.println("X: " + realVx + "  |Y: " + realVy);
            vx *= -1;
            handleScore(realVx, realVy);
        }
        if ((y + realVy) <= 0 || (y + realVy) >= getWorld().getHeight()) {
            // System.out.println("X: " + realVx + "  |Y: " + realVy);
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
    
    public void randomizeRotation() {
         rotation = Greenfoot.getRandomNumber(361);
    }
    
    public void correctCollissionX(Paddle paddle) {
        boolean isIntersecting = intersects(paddle);
        while (isIntersecting) {
            double realVx = getRealVx();
            this.x += 0.1 * realVx;
            changeLocation(realVx, getRealVy());
            if (!intersects(paddle)) {
                isIntersecting = false;
                break;
            }
        }
    }
    
    public void correctCollissionY(Paddle paddle) {
        boolean isIntersecting = intersects(paddle);
        while (isIntersecting) {
            double realVy = getRealVy();
            this.y += 2 * realVy;
            changeLocation(getRealVx(), realVy);
            if (!intersects(paddle)) {
                isIntersecting = false;
                break;
            }
        }
    }
    
    public void changeDirectionX() {
        lastVx = vx;
        vx *= -1;
    }
    
    public void changeDirectionY() {
        lastVy = vy;
        vy *= -1;
    }
    
    public void resetPosition() {
        this.x = getWorld().getWidth()/2;
        this.y = getWorld().getHeight()/2;
        setLocation((int) x, (int) y);
    }
    
    public void changeLocation(double vx, double vy) {
        double realVx = getRealVx();
        double realVy = getRealVy();
        if ((getRealLastVx() == realVx && getRealLastVy() == realVy * (-1))) {
            if((y + realVy) <= 0) {
                System.out.println("testHigh");
                vy = SPEED;
                y = 10;
            } else if ((y + realVy) >= getWorld().getHeight()) {
                System.out.println("testLow");
                vy = -SPEED;
                y = getWorld().getHeight() - 10;
            }
        }
        
        x += vx;
        y += vy;
        setLocation((int) x, (int) y);
    }
    
    public double getRealVx() {
        return (vx * Math.cos(Math.toRadians(rotation)));
    }
    
    public double getRealVy() {
        return (vy * Math.sin(Math.toRadians(rotation)));
    }
    
    public double getRealLastVx() {
        return (lastVx * Math.cos(Math.toRadians(rotation)));
    }
    
    public double getRealLastVy() {
        return (lastVy * Math.sin(Math.toRadians(rotation)));
    }
    
    public void draw() {
        GreenfootImage img = new GreenfootImage("ball.png");
        img.scale(WIDTH, HEIGHT);
        setImage(img);
    }
    
    public void setSimulationRotation(double rot) {
        this.rotation = rot;
    }
    
    public void setSimulationXY(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
