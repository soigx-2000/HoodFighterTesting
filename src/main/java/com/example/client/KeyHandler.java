package com.example.client;
import java.awt.event.KeyListener;
import java.io.Serializable;

import com.example.PlayerInput;

import java.awt.event.KeyEvent;
public class KeyHandler implements KeyListener{
    public boolean jumpPressed, leftPressed, rightPressed, jabPressed, shieldPressed, crouchPressed;
    public KeyHandler(){

    }
    public PlayerInput toPlayerInput(boolean playerControlled){
        return new PlayerInput(playerControlled, jumpPressed, leftPressed, rightPressed, jabPressed, shieldPressed, crouchPressed);
    }
    public void keyTyped(KeyEvent e){
        
    }
    public void keyPressed(KeyEvent e){
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_W){
            jumpPressed = true;
        }
        if(code == KeyEvent.VK_A){
            leftPressed = true;
        }
        if(code == KeyEvent.VK_D){
            rightPressed = true;
        }
       
        if(code == KeyEvent.VK_S){
            jabPressed = true;
        }
        if(code == KeyEvent.VK_X) {
        	crouchPressed = true;
        }
    }
    public void keyReleased(KeyEvent e){
        int code = e.getKeyCode();
        if(code == KeyEvent.VK_W){
            jumpPressed = false;
        }
        if(code == KeyEvent.VK_A){
            leftPressed = false;
        }
        if(code == KeyEvent.VK_D){
            rightPressed = false;
        }
        if(code == KeyEvent.VK_S){
            jabPressed = false;
        }
        if(code == KeyEvent.VK_X) {
        	crouchPressed = false;
        }
    }
}


