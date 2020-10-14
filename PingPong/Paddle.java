import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public abstract class Paddle extends Actor
{
    public static final int WIDTH = 20;
    public static final int HEIGHT = 60;
    public static final double ALPHA_I = Math.toDegrees(Math.atan((HEIGHT / 2) / (WIDTH / 2)));
    public static final double ALPHA_II = 180 - ALPHA_I;
    public static final double ALPHA_III = 180 + ALPHA_I;
    public static final double ALPHA_IV = 360 - ALPHA_I;
    
    public Paddle() {
        draw();
    }
    
    public abstract void act();
    
    public void handleIntersections() {
        // It is tested if there is a intersection with the ball
        Ball ball = getWorld().getObjects(Ball.class).get(0);
        if (intersects(ball)) {
            // If there is a intersection, the angle is determined and according to the angle a fitting collission response function is executed
            double angle = calculateAngle(ball.getX(), ball.getY(), getX(), getY());
            if (angle <= ALPHA_I && angle >= 0) {
                collideX(ball, angle);
            } else if (angle <= ALPHA_II && angle > ALPHA_I){
                collideY(ball, angle);
            } else if (angle <= ALPHA_III && angle > ALPHA_II) {
                collideX(ball, angle);
            } else if (angle <= ALPHA_IV && angle > ALPHA_III) {
                collideY(ball, angle);
            } else if (angle <= 360 && angle >= ALPHA_IV) {
                collideX(ball, angle);
            }
            
        }
    }
    
    private void collideX(Ball ball, double angle) {
        // The paddle tells the ball to change its x direction and correct its position
        ball.changeDirectionX();
        ball.correctCollissionX(this);
    }
    
    private void collideY(Ball ball, double angle) {
        // It is differentiated between four diffferent cases, that need different handling
        // Two of them just need a correction of position and the two remaining also need a direction change
        //    The cases are determined by testing the signum of function of realVy and by testing the angle that is provided
        //
        //    Case 1/3: The ball on its current trajectory has or would have collided with the paddle, so its speed/direction and position need to be corrected
        //    Case 2/4: The ball on its current trajectory would have moved away from the paddle, most likely the player moved into it, so just its position needs to be corrected
        //    Case 1 = Math.signum(realVy) == 1 && (angle <= ALPHA_II && angle > ALPHA_I); Case 3: Math.signum(realVy) == -1 && (angle <= ALPHA_IV && angle > ALPHA_III)
        //    Case 2 = Math.signum(realVy) == -1 && (angle <= ALPHA_II && angle > ALPHA_I); Case 4: Math.signum(realVy) == 1 && (angle <= ALPHA_IV && angle > ALPHA_III)
        double realVy = ball.getRealVy();
        if ((Math.signum(realVy) == 1 && (angle <= ALPHA_II && angle > ALPHA_I)) || (Math.signum(realVy) == -1 && (angle <= ALPHA_IV && angle > ALPHA_III))) {
            ball.changeDirectionY();
            ball.correctCollissionY(this);
        } else if ((Math.signum(realVy) == -1 && (angle <= ALPHA_II && angle > ALPHA_I)) || (Math.signum(realVy) == 1 && (angle <= ALPHA_IV && angle > ALPHA_III))) {
            ball.correctCollissionY(this);
        }
    }
    
    private double calculateAngle(double x1, double y1, double x2, double y2) {
        // This functions calculates the angle between two objects, which centers are at (x1|y1) and (x2|2) respectively
        double distanceX = x2 - x1;
        double distanceY = y2 - y1;
        double angle = Math.toDegrees(Math.atan(distanceY / distanceX));
        if (x2 > x1 && y2 > y1) {
            angle = angle;
        } else if (x1 > x2 && y2 > y1) {
            angle  = 180 + angle;
        } else if (x1 > x2 && y1 > y2) {
            angle = 180 + angle;
        } else if (x2 > x1 && y1 > y2) {
            angle = 360 + angle;
        }
        return angle;
    }
    
    public void draw() {
        // A rectangle is created, it is filled and then set as a image
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.drawRect(0, 0, WIDTH, HEIGHT);
        image.setColor(Color.CYAN);
        image.fill();
        setImage(image);
    }
    
    public static Paddle createKeyboardControlledPaddle(String upKey, String downKey) {
        // This returns a paddle that can be controlled with two given keys
        return new Paddle() {
            public void act(){
                if (Greenfoot.isKeyDown(upKey)) {
                    setLocation(getX(), getY() - 10);
                }
                if (Greenfoot.isKeyDown(downKey)) {
                    setLocation(getX(), getY() + 10);
                }   
                handleIntersections();
            }  
        };
    }
    
    public static Paddle createMouseControlledPaddle() {
        // This returns a paddle that can be controlled with a mouse
        return new Paddle() {
            public void act() {
                if (Greenfoot.getMouseInfo() != null) {
                    if (Greenfoot.getMouseInfo().getY() <= getY()) {
                        setLocation(getX(), getY() - 10);
                    }
                    if (Greenfoot.getMouseInfo().getY() >= getY()) {
                        setLocation(getX(), getY() + 10);
                    }
                }   
                handleIntersections();
            }
        };
    }
}
