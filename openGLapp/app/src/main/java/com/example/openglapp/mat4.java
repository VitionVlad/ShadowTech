package com.example.openglapp;

/*
 * 0 1 2 3
 * 4 5 6 7
 * 8 9 10 11
 * 12 13 14 15
 * */

import android.opengl.Matrix;

public class mat4 {
    public float[] mat = {
            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0 // 12 13 14 15 3 7 11 15
    };
    public void buildtranslatemat(vec3 pos){
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 1;
        mat[15] = 1;
        mat[12] = pos.x;
        mat[13] = pos.y;
        mat[14] = pos.z;
    }
    public void buildxrotmat(float angle){
        mat[0] = 1;
        mat[5] = (float) Math.cos(angle);
        mat[6] = (float) -Math.sin(angle);
        mat[9] = (float) Math.sin(angle);
        mat[10] = (float) Math.cos(angle);
        mat[15] = 1;
    }
    public void buildyrotmat(float angle){
        mat[0] = (float) Math.cos(angle);
        mat[5] = 1.0f;
        mat[2] = (float) Math.sin(angle);
        mat[8] = (float) -Math.sin(angle);
        mat[10] = (float) Math.cos(angle);
        mat[15] = 1;
    }
    public void buildperspectivemat(float fov, float zNear, float zFar, float aspect){
        float S = (float) Math.tan((fov/2)*(Math.PI/180));
        mat[0] = 1/(aspect*S);
        mat[5] = 1/S;
        mat[10] = -zFar/(zFar-zNear);
        mat[11] = -1;
        mat[14] = -zFar*zNear/(zFar-zNear);
    }
    public void buildorthomat(float r, float l, float t, float b, float zNear, float zFar){
        mat[0] = 2/(r-l);
        mat[5] = 2/(r-l);
        mat[10] = -2/(zFar-zNear);
        mat[15] = 1;
        mat[3] = (r+r)/(r-l);
        mat[7] = (t+b)/(t-b);
        mat[11] = (zFar+zNear)/(zFar-zNear);
    }
    public void buildIdentityMat(){
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 10;
        mat[15] = 1;
    }
}
