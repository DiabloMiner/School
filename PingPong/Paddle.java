import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public abstract class Paddle extends Actor
{
    public static final int WIDTH = 60;
    public static final int HEIGHT = 20;
    
    public Paddle() {
        draw();
        setRotation(90);
    }
    
    public abstract void act();
    
    public void handleIntersections() {
        Ball ball = getWorld().getObjects(Ball.class).get(0);
        if (intersects(ball)) {
            double angle = calculateAngle(ball.getX(), ball.getY(), getX(), getY());
            if (angle <= 71.6 && angle >= 0) {
                ball.changeDirectionX();
                ball.correctCollissionX(HEIGHT/2);
            } else if (angle <= 108.4 && angle > 71.6){
                ball.changeDirectionY();
                ball.correctCollissionY(WIDTH/2);
            } else if (angle <= 251.6 && angle > 108.4) {
                ball.changeDirectionX();
                ball.correctCollissionX(HEIGHT/2);
            } else if (angle <= 288.4 && angle > 251.6) {
                ball.changeDirectionY();
                ball.correctCollissionY(WIDTH/2);
            } else if (angle <= 360 && angle >= 288.4) {
                ball.changeDirectionX();
                ball.correctCollissionX(HEIGHT/2);
            }
        }
    }
    
    private double calculateAngle(double x1, double y1, double x2, double y2) {
        double distanceX = Math.abs(x1 - x2);
        double distanceY = Math.abs(y1 - y2);
        double angle = Math.toDegrees(Math.atan(distanceY / distanceX));
        if (x2 > x1 && y2 > y1) {
            angle = angle;
        } else if (x1 > x2 && y2 > y1) {
            angle  = 180 - angle;
        } else if (x1 > x2 && y1 > y2) {
            angle = 180 + angle;
        } else {
            angle = 360 - angle;
		}
        return angle;
    }
    
    public void draw() {
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.drawRect(0, 0, WIDTH, HEIGHT);
        image.setColor(Color.CYAN);
        image.fill();
        setImage(image);
    }
    
    public static Paddle createKeyboardControlledPaddle(String upKey, String downKey) {
        return new Paddle() {
            public void act(){   
                handleIntersections();
                if (Greenfoot.isKeyDown(upKey)) {
                    move(-10);
                }
                if (Greenfoot.isKeyDown(downKey)) {
                    move(10);
                }
            }  
        };
    }
    
    public static Paddle createMouseControlledPaddle() {
        return new Paddle() {
            public void act() {   
                handleIntersections();
                if (Greenfoot.getMouseInfo() != null) {
                    if (Greenfoot.getMouseInfo().getY() <= getY()) {
                        move(-10);
                    }
                    if (Greenfoot.getMouseInfo().getY() >= getY()) {
                        move(10);
                    }
                }
            }
        };
    }
}
