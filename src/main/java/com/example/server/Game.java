package com.example.server;

import java.io.Serializable;

import com.example.PlayerInput;
import com.example.client.KeyHandler;

public class Game extends Thread implements Serializable{
	//map is 800*400
	public Player player1;
	public Player player2;
	public PlayerInput input1 = new PlayerInput();
	public PlayerInput input2 = new PlayerInput();
	public Game() {
		player1 = new Player(250, 0, 1);
		player2 = new Player(550, 0, 2);
	}
	public void update() {// getting k1 and k2 from the Server
		player1.update(input1);
		player2.update(input2);

		//check for collision and register damage accordingly
		if (!player1.attackingHitbox.vanished && player1.attackingHitbox.checkCollision(player2.hitbox) && !player1.hasHit) {
			player2.hp -= 10;
			player1.hasHit = true;
			player1.flow += 500;
		}
		if (!player2.attackingHitbox.vanished && player2.attackingHitbox.checkCollision(player1.hitbox) && !player2.hasHit) {
			player1.hp -= 10;
			player2.hasHit = true;
			player2.flow += 500;
		}
	}
	@Override
	public void run() {
		double currentTime = System.nanoTime()/1000000.0;//current time in milisecond
        double interval = 1000.0/60.0;
        double nextUpdateTime =  currentTime + interval;
		while(true) {
			update();
			try {
				Thread.sleep((long)(nextUpdateTime - System.nanoTime()/1000000.0));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}


