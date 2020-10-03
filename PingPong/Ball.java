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
        double realVx = getRealVx();
        double realVy = getRealVy();
        
        if ((x + realVx) <= 0 || (x + realVx) >= getWorld().getWidth()) {
            // System.out.println(realVx + "  |  "  + realVy);
            vx *= -1;
            handleScore(realVx, realVy);
        }
        if ((y + realVy) <= 0 || (y + realVy) >= getWorld().getHeight()) {
            // System.out.println(realVx + "  |  "  + realVy);
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
         // rotation = Greenfoot.getRandomNumber(361);
         rotation = 330;
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
                this.y += 4 * realVy;
                changeLocation(getRealVx(), realVy);
                break;
            }
        }
    }
    
    public void changeDirectionX() {
        vx *= -1;
    }
    
    public void changeDirectionY() {
        vy *= -1;
    }
    
    public void changeLocation(double vx, double vy) {
        /*double realVx = getRealVx();
        double realVy = getRealVy();
        if ((x + realVx) <= 0 && Math.signum(vy) == -1.0) {
            vx = 3;
            changeDirectionY();
            x += vx + 10;
            y += vy + 10;
        } else if ((x + realVx) >= getWorld().getWidth() && Math.signum(vy) == -1.0) {
            vx = -3;
            changeDirectionY();
            x += vx + 10;
            y += vy + 10;
        }
        
        if ((y + realVy) <= 0 && Math.signum(vx) == -1.0) {
            vy = 3;
            changeDirectionX();
            x += vx + 10;
            y += vy + 10;
        } else if ((y + realVy) >= getWorld().getHeight() && Math.signum(vx) == -1.0) {
            vx = -3;
            changeDirectionX();
            x += vx + 10;
            y += vy + 10;
        }*/
        
        x += vx;
        y += vy;
        setLocation((int) x, (int) y);
    }
    
    public boolean isRotationInXDirection() {
        if ((rotation >= 0 && rotation <= 45) || (rotation >= 315 && rotation <= 360) || (rotation >= 135 && rotation <= 225)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isRotationInYDirection() {
        if ((rotation >= 45 && rotation <= 135) || (rotation >= 225 && rotation <= 315)) {
            return true;
        } else {
            return false;
        }
    }
    
    public double getRealVx() {
        return (vx * Math.cos(Math.toRadians(rotation)));
    }
    
    public double getRealVy() {
        return (vy * Math.sin(Math.toRadians(rotation)));
    }
    
    public void draw() {
        GreenfootImage img = new GreenfootImage("ball.png");
        img.scale(WIDTH, HEIGHT);
        setImage(img);
    }
    
    public double getSimulationRotation()
    {
        return this.rotation;
    }
    
    public void setRotation(double rotation)
    {
        this.rotation = rotation;
    }
    
    public double getVx() {
        return vx;
    }
    
    public double getVy() {
        return vy;
    }
}
