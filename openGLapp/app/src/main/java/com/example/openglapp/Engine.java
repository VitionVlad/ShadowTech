package com.example.openglapp;

import android.opengl.GLES32;

public class Engine {
    public vec3 pos = new vec3(0, 0, 0);
    public vec2 rot = new vec2(0, 0);
    public vec2 touchpos = new vec2(0, 0);
    public float speed = 0;
    public boolean allowmove = false;
    public mat4 perspective = new mat4();
    public mat4 translate = new mat4();
    public mat4 xrot = new mat4();
    public mat4 yrot = new mat4();
    public boolean touchControls = true;
    public float fov = 110;
    public void perFrame(ivec2 resolution){
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glViewport(0, 0, resolution.x, resolution.y);
        if(allowmove == true && touchControls == true){
            if(touchpos.x < resolution.x/2){
                pos.z +=  (float) Math.cos(rot.y) * Math.cos(rot.x) * ((((-touchpos.y/resolution.y)*2) +1)*0.01);
                pos.x -= Math.cos(rot.y) * Math.sin(rot.x) * ((((-touchpos.y/resolution.y)*2) +1)*0.01);
            }else if(touchpos.x > resolution.x/2){
                rot.x += (((touchpos.x/ (resolution.x*1.7))*2) -1)*0.1;
                rot.y += (float) ((((-touchpos.y/resolution.y)*2) +1)*0.01);
            }
        }
        perspective.buildperspectivemat(fov, 0.1f, 100, resolution.x/resolution.y);
        yrot.buildyrotmat(-rot.x);
        xrot.buildxrotmat(rot.y);
        translate.buildtranslatemat(pos);
    }
}
