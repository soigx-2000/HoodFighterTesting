package com.example.client;
import javax.swing.*;

import com.example.server.Game;
import com.example.server.Hitbox;
import com.example.server.Player.Status;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
public class GameGraphic extends JPanel implements Runnable{
    private Game game;
    private KeyHandler keyH = new KeyHandler();
    //These are animation image for both players so that they can be rendered
    private PlayerSprite sprite1 = new PlayerSprite(1);
    private PlayerSprite sprite2 = new PlayerSprite(2);
    private Status status1 = Status.IDLE;
    private Status status2 = Status.IDLE;
    private int drawInterval1 = 1;//time interval to update player1's sprite, in frame
    private int drawIndex1 = 0;//time since last sprite update for player 1, in frame
    private int drawInterval2 = 2;//time interval to update player1's sprite
    private int drawIndex2 = 0;//time since last sprite update for player 1, in frame
    private boolean playerControlled;//false for player 1 and true for player 2
    final int panelWidth = 1000;
    final int paneHeight = 500;
    private final int PORT = 9880;
    private InetAddress host;
    private Socket socket = null;
    private ObjectOutputStream oos = null;
    private ObjectInputStream ois = null;
    private Thread graphicsThread;
    public GameGraphic(Game game, InetAddress host){
        this.host = host;//connect to server
        try{
            socket = new Socket(host.getHostName(), PORT);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        }
        catch(Exception e){
            System.err.println(e);
        }
        try{
            int id = ((Integer)ois.readObject()).intValue();
            System.out.println("My id is: " + id);
            if(id == 1){
                playerControlled = false;
            }
            else if(id == 2){
                playerControlled = true;
            }
        }
        catch(IOException e){
            System.err.print("Recieving ID failed " + e);
        }
        catch(ClassNotFoundException e){
            System.err.print("Something crazy happened to my virtue machine "+ e);
        }
        this.game = game;
        this.setPreferredSize(new Dimension(panelWidth, paneHeight));
        this.setBackground(Color.GRAY);
        this.setDoubleBuffered(true);//no idea what this does
    }
    public void startGraphicThread(){
    	System.out.println("Graphic started");
    	graphicsThread = new Thread(this);
        graphicsThread.start();
    }

    public void run(){
        while(graphicsThread != null){
            double currentTime = System.nanoTime()/1000000.0;//current time in milisecond
            double interval = 1000.0/60.0;
            double nextDrawTime =  currentTime + interval;
            System.out.println("Time before update, repaint, and send input: " + currentTime + "Next draw time: " + nextDrawTime);
            //to be implemented: sending this client's player input to the server
            updateGame();
            updateSprite();
            repaint();
            try {
                oos.writeObject(keyH.toPlayerInput(playerControlled));
                oos.flush();
                System.out.println(playerControlled + ": Input sent");
            } catch (IOException e) {
                System.err.println("send input failed");
                e.printStackTrace();
            }
            System.out.println("Time after update, repaint, and send input: " + System.nanoTime()/1000000.0);
            long sleepTime = (long) (nextDrawTime - System.nanoTime()/1000000.0);//this is negative some time for some reason
            try{
                Thread.sleep(Math.max(0, sleepTime));// Claude suggested short term fix for negative sleep time.
            }
           catch(InterruptedException e){
               System.err.println("WE CAN CRY NOW BECAUSE WE SHOULDN'T GET HERE");
            }

        }
    }
    public void updateGame() {
        try {
            game = (Game) ois.readObject();
            System.out.println("game updated");
        } catch (IOException e) {
            System.err.println("game update failed");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Something crazy happened to my virtue machine");
            e.printStackTrace();
        }
    }
    public void updateSprite(){
        if(game.player1.status != status1){//player1 status change
            status1 = game.player1.status;
            sprite1.yoMyStatusChangedTo(status1);
        }
        if(game.player2.status != status2){//player2 status change
            status2 = game.player2.status;
            sprite2.yoMyStatusChangedTo(status2);
        }
        //push current image accordingly
        //player 1
        if(drawIndex1 == drawInterval1-1){
			sprite1.update();
			drawIndex1 = 0;
		}
		else
			drawIndex1++;
        //player 2
        if(drawIndex2 == drawInterval2-1){
			sprite2.update();
			drawIndex2 = 0;
		}
		else
			drawIndex2++;
    }
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        //draw Player 1:
        int xPos = game.player1.xPos;
        int yPos = game.player1.yPos;
        Hitbox hitbox = game.player1.hitbox;
        Hitbox attackingHitbox = game.player1.attackingHitbox;
        BufferedImage image = sprite1.currentImage();
        if(image != null) {
        	g2.drawImage(image, xPos, 500-(image.getHeight()+yPos + 50), image.getWidth(), image.getHeight(), null);
        }
        else {
        	g2.drawRect(xPos, 500/* screen height */ - (200 /* height */ + yPos + 50/*ground level*/), 200, 200);
        }
        //render player 1 hitbox
        if(hitbox.visible) {
        	g2.drawRect(hitbox.x, 500/* screen height */- (hitbox.height + hitbox.y + 50/*ground level*/)/*ground level*/, hitbox.width, hitbox.height);
        }
		if(!attackingHitbox.vanished){
			g2.drawRect(attackingHitbox.x, 500/* screen height */- (attackingHitbox.height + attackingHitbox.y + 50/*ground level*/)/*ground level*/, attackingHitbox.width, attackingHitbox.height);
		}
        // draw player 2
        //draw Player 1:
        xPos = game.player2.xPos;
        yPos = game.player2.yPos;
        hitbox = game.player2.hitbox;
        attackingHitbox = game.player2.attackingHitbox;
        image = sprite1.currentImage();
        if(image != null) {
        	g2.drawImage(image, xPos, 500-(image.getHeight()+yPos + 50), image.getWidth(), image.getHeight(), null);
        }
        else {
        	g2.drawRect(xPos, 500/* screen height */ - (200 /* height */ + yPos + 50/*ground level*/), 200, 200);
        }
        if(hitbox.visible) {
        	g2.drawRect(hitbox.x, 500/* screen height */- (hitbox.height + hitbox.y + 50/*ground level*/)/*ground level*/, hitbox.width, hitbox.height);
        }
		if(!attackingHitbox.vanished){
			g2.drawRect(attackingHitbox.x, 500/* screen height */- (attackingHitbox.height + attackingHitbox.y + 50/*ground level*/)/*ground level*/, attackingHitbox.width, attackingHitbox.height);
		}
        // Player 1 bars (left side)
        int p1Hp = game.player1.hp;
        int p1Flow = game.player1.flow;
        int hpBarWidth = (int)(p1Hp/100.0*200);
        int flowBarWidth = (int)(p1Flow/1000.0*250);
        
        g2.setColor(Color.BLUE);
        g2.fillRect(0, 0, flowBarWidth, 20);
        g2.setColor(Color.RED);
        g2.fillRect(0, 20, hpBarWidth, 20);
        
        // Player 2 bars (right side)
        int p2Hp = game.player2.hp;
        int p2Flow = game.player2.flow;
        int p2HpBarWidth = (int)(p2Hp/100.0*200);
        int p2FlowBarWidth = (int) (p2Flow/1000.0*250);
        
        g2.setColor(Color.BLUE);
        g2.fillRect(600, 0, p2FlowBarWidth, 20);
        g2.setColor(Color.RED);
        g2.fillRect(600, 20, p2HpBarWidth, 20);
    }

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        //get the localhost IP address, if server is running on some other IP, you need to use that
        InetAddress host = InetAddress.getLocalHost();
        JFrame window = new JFrame();
        Game game = new Game();
        GameGraphic g = new GameGraphic(game, host);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//allow us to close the window by clicking "X" botton
        window.setTitle("hood fighter");
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.add(g);
        g.startGraphicThread();
        window.pack();
        window.setVisible(true);
        
    }
}