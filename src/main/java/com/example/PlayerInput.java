package com.example;

import java.io.Serializable;

public class PlayerInput implements Serializable{
    //just a bunch of cheap boolean representing player input
    //so I don't have to send a whole KeyHandler, which is a KeyListener to the Server
    public boolean playerControl, jumpPressed, leftPressed, rightPressed, jabPressed, shieldPressed, crouchPressed;
    public PlayerInput(){
        
    }
    public PlayerInput(boolean player, boolean jump, boolean left, boolean right, boolean jab, boolean sheild, boolean crouch){
        playerControl = player;
        jumpPressed = jump;
        leftPressed = left;
        rightPressed = right;
        jabPressed = jab;
        shieldPressed = sheild;
        crouchPressed = crouch;
    }
}
