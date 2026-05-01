package com.example.server;

import java.io.Serializable;

public class Hitbox implements Serializable{
	public int x = 0;
	public int y = 0;
	public int width = 0;
	public int height = 0;
	public int priority = 0;
	public boolean visible = true;
	public boolean vanished = false;
	public Hitbox(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
	}
	//precondition: h != null
	//postcondition: return if this hitbox colide with the input hit box
	public boolean checkCollision(Hitbox h) {
		if(!vanished)
			if(x + width >= h.x || y + height >= h.y) {
				return true;
			}
		return false;
	}
	public void updatePos(int x, int y) {
		this.x = x;
		this.y = y;
	}
    public void vanish() {
        vanished = true;
		visible = false;
    }
}


