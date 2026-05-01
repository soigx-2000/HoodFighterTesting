package com.example.server;
import java.io.Serializable;

import com.example.PlayerInput;
import com.example.client.KeyHandler;

public class Player implements Serializable{
	public int xPos;
    public int yPos;
    public int yVelocity = 0;
    public int xVelocity = 0;
    public int hp = 100;
	public int flow = 1000;
	public Hitbox hitbox = new Hitbox(0,0,0,0);
	public Hitbox attackingHitbox = new Hitbox(xPos,yPos,0,0);
	private int ID = 0;
	private int actionDuration = 0;//duration remaining of an action in frame
	
	final int g = 1;//acceleration due to gravity
	public boolean hasHit = false;    public enum Status{
        IDLE/* code 0 */, JUMPING/* code 1 */, PUNCHING/* code 2 */, CROUCHING/* code 3 */,
		RUNNING_LEFT/* code 4 */, RUNNING_RIGHT/* code 5 */, BLOCKING/* code 6 */,  
		STUNNED/* code 7 */, DOWN/* code 8 */;
    }
    //player's x/y location with respect to map
    public Status status = Status.IDLE;
    public Player(int x, int y, int ID){
    	this.ID = ID;
        xPos = x;
        yPos = y;
	}
    
	public void updateHitboxes(){
		hitbox.x = xPos+40;
		hitbox.y = yPos+20;
		hitbox.width = 90;
		hitbox.height = 250;
		if(status == Status.CROUCHING){
			hitbox.y = yPos;
			hitbox.height = 128;
		}
		if(status == Status.PUNCHING){
			attackingHitbox.x = hitbox.x + hitbox.width;
			attackingHitbox.y = hitbox.y + hitbox.height/3*2+5;
			attackingHitbox.height = 20;
			attackingHitbox.width = 40;
			attackingHitbox.vanished = false;
			if(actionDuration <25 && actionDuration >= 5){//damaging frame
				attackingHitbox.width = 80;
			}
		}
		else{
			hitbox.width = 200;
			hitbox.height = 300;
		}
		
	}
    
    
    public void update(PlayerInput input) {
		if(actionDuration != 0){
			actionDuration--;
		}
		else if(actionDuration == 0){//a duration based action
			if(status == Status.PUNCHING){
				status = Status.IDLE;
				attackingHitbox.vanish();
				hasHit = false;
			}
		}
		if(input == null) {//no key pressed, no action
			updateHitboxes();
    		return;
    	}
		//if movement key released: change the status
		if(!input.leftPressed) {
				xVelocity = 0;
				if(status == Status.RUNNING_LEFT)
					status = Status.IDLE;
		}
		if(!input.rightPressed) {
			xVelocity = 0;
			if(status == Status.RUNNING_RIGHT)
				status = Status.IDLE;
		}
		if (flow >= 0){
			//movement
			if(input.leftPressed){
				xVelocity = -2;
				flow -= 2;
				if(status != Status.JUMPING && status == Status.IDLE)
					status = Status.RUNNING_LEFT;
			}
			if(input.rightPressed){
				xVelocity = 2;
				flow -= 2;
				if(status != Status.JUMPING && status == Status.IDLE)
					status = Status.RUNNING_RIGHT;
			}
		}
		if (flow >= 200){
			xPos += xVelocity;
			if(status == Status.IDLE && input.jumpPressed) {//jump
				status = Status.JUMPING;
				yVelocity = 15;
			}
		}
		if(yPos == 0 && yVelocity < 0){//landing
			status = Status.IDLE;
			yVelocity = 0;
		}
		if(status == Status.JUMPING){//in air;
			yVelocity = yVelocity - g;
		}
		//attack
		if(flow >= 300){
			if(status != Status.JUMPING && status != Status.PUNCHING) {
				if(input.jabPressed){
					status = Status.PUNCHING;
					actionDuration = 15;
					flow -= 300;
					hasHit = false;
				}
			}
		}
        //denfense
        if(input.crouchPressed){
			status = Status.CROUCHING;
	    }
        if(!input.crouchPressed && status == Status.CROUCHING){
			status = Status.IDLE;
	    }
		if(flow <= 990){
			if(status == Status.IDLE){
				if(flow > 990)
					flow = 1000;
				else
					flow += 10;
				
			}
			if(status == Status.BLOCKING || status == Status.CROUCHING){
				if(flow > 995)
					flow = 1000;
				else
					flow += 5;
			}
		}
		//update sprite:
		updateHitboxes();
    }
}