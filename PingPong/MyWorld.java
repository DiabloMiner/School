import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class MyWorld extends World
{   
    public MyWorld()
    {    
        // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
        super(800, 800, 1);
        
        Ball ball = new Ball(getWidth()/2, getHeight()/2);
        Paddle leftPaddle = Paddle.createKeyboardControlledPaddle("w", "s");
        Paddle rightPaddle = Paddle.createMouseControlledPaddle();
        Score score = new Score();
        
        addObject(ball, getWidth()/2, getHeight()/2);
        addObject(leftPaddle, 60, getHeight()/2);
        addObject(rightPaddle, getWidth() - 60, getHeight()/2);
        addObject(score, getWidth()/2, score.FONT_SIZE);
    }

}
