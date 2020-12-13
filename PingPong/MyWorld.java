import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class MyWorld extends World
{   
    public MyWorld()
    {    
        // Create a new world with 800x800 cells with a cell size of 1x1 pixels.
        super(800, 800, 1);
        
        // The four objects of the game are created and added to the game
        Ball ball = new Ball(getWidth()/2, getHeight()/2, 360);
        Paddle leftPaddle = Paddle.createKeyboardControlledPaddle("w", "s");
        Paddle rightPaddle = Paddle.createMouseControlledPaddle();
        Score score = new Score();
        
        addObject(ball, getWidth()/2, getHeight()/2);
        addObject(leftPaddle, 60, getHeight()/2);
        addObject(rightPaddle, getWidth() - 60, getHeight()/2);
        addObject(score, getWidth()/2, score.FONT_SIZE);
        
        Vector v1 = new Vector(1,2);
        Vector v2 = new Vector(2,1);
        
        System.out.println(v1.getPhiRadians());
        System.out.println(v1.getPhiDegrees());
    }

}
