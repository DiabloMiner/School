import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public abstract class Paddle extends Actor
{
    public static final int WIDTH = 20;
    public static final int HEIGHT = 60;
    public static final double ALPHA_I = Math.atan((60/2)/(20.0/2));
    public static final double ALPHA_II = 180 - ALPHA_I;
    public static final double ALPHA_III = 180 + ALPHA_I;
    public static final double ALPHA_IV = 360 - ALPHA_I;
    
    public Paddle() {
        draw();
    }
    
    public abstract void act();
    
    public void handleIntersections() {
        Ball ball = getWorld().getObjects(Ball.class).get(0);
        if (intersects(ball)) {
            /*double correctionX = getPenetrationDepth(super.getX(), ball.getX(), HEIGHT, Ball.WIDTH);
            double correctionY = getPenetrationDepth(super.getY(), ball.getY(), WIDTH, Ball.HEIGHT);
            
            if (correctionX >= 0.5 || correctionY >= 0.5)
            {
                // toggle ball direction
                ball.changeDirectionX();
                ball.changeDirectionY();
                ball.correctCollissionX(correctionX);
            }
            */
            
            double angle = calculateAngle(ball.getX(), ball.getY(), getX(), getY());
            boolean rotationXDirection = ball.isRotationInXDirection();
            boolean rotationYDirection = ball.isRotationInYDirection();
            if (angle <= ALPHA_I && angle >= 0) {
                collideX(ball, 2);
            } else if (angle <= ALPHA_II && angle > ALPHA_I){
                collideY(ball, 2);
                if (rotationXDirection) {
                    collideX(ball, 1);
                }
            } else if (angle <= ALPHA_III && angle > ALPHA_II) {
                collideX(ball, 2);
            } else if (angle <= ALPHA_IV && angle > ALPHA_III) {
                collideY(ball, 2);
                if (rotationXDirection) {
                    collideX(ball, 1);
                }
            } else if (angle <= 360 && angle >= ALPHA_IV) {
                collideX(ball, 2);
            }
            
        }
    }
    
    private void collideX(Ball ball, double divFactor) {
        ball.changeDirectionX();
        ball.correctCollissionX(WIDTH/divFactor);
    }
    
    private void collideY(Ball ball, double divFactor) {
        ball.changeDirectionY();
        ball.correctCollissionY(HEIGHT/divFactor);
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
    
    private double getPenetrationDepth(double center, double center1, double extent, double extent2)
    {
        return (extent / 2 + extent2 / 2) - (center - center1);
    }
    
    public static Paddle createKeyboardControlledPaddle(String upKey, String downKey) {
        return new Paddle() {
            public void act(){   
                handleIntersections();
                if (Greenfoot.isKeyDown(upKey)) {
                    setLocation(getX(), getY() - 10);
                }
                if (Greenfoot.isKeyDown(downKey)) {
                    setLocation(getX(), getY() + 10);
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
                        setLocation(getX(), getY() - 10);
                    }
                    if (Greenfoot.getMouseInfo().getY() >= getY()) {
                        setLocation(getX(), getY() + 10);
                    }
                }
            }
        };
    }
}
