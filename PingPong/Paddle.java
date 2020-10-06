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
        Ball ball = getWorld().getObjects(Ball.class).get(0);
        if (intersects(ball)) {
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
        ball.changeDirectionX();
        ball.correctCollissionX(this);
    }
    
    private void collideY(Ball ball, double angle) {
        double realVy = ball.getRealVy();
        if (Math.signum(realVy) == 1 && (angle <= ALPHA_II && angle > ALPHA_I)) {
            ball.changeDirectionY();
            ball.correctCollissionY(this);
        } else if (Math.signum(realVy) == -1 && (angle <= ALPHA_II && angle > ALPHA_I)) {
            ball.correctCollissionY(this);
        } else if (Math.signum(realVy) == -1 && (angle <= ALPHA_IV && angle > ALPHA_III)) {
            ball.changeDirectionY();
            ball.correctCollissionY(this);
        } else if (Math.signum(realVy) == 1 && (angle <= ALPHA_IV && angle > ALPHA_III)) {
            ball.correctCollissionY(this);
        }
    }
    
    private double calculateAngle(double x1, double y1, double x2, double y2) {
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
        GreenfootImage image = new GreenfootImage(WIDTH, HEIGHT);
        image.drawRect(0, 0, WIDTH, HEIGHT);
        image.setColor(Color.CYAN);
        image.fill();
        setImage(image);
    }
    
    public static Paddle createKeyboardControlledPaddle(String upKey, String downKey) {
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
