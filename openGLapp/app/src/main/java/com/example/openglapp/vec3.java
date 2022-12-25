package com.example.openglapp;

public class vec3 {
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;
    public vec3(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public vec3(){}
    public void normalize(vec3 v1){
        float a = (float) Math.sqrt((v1.x*v1.x)+(v1.y*v1.y)+(v1.z*v1.z));
        this.x = v1.x/a;
        this.y = v1.y/a;
        this.z = v1.z/a;
    }
    public void cross(vec3 v1, vec3 v2){
        this.x = v1.y * v2.z + v2.y * v1.z;
        this.y = v1.z * v2.x + v2.z * v1.x;
        this.z = v1.x * v2.y + v2.x * v1.y;
    }
    public float dot(vec3 v1, vec3 v2){
        return v1.x * v2.x + v1.y + v2.y + v1.z + v2.z;
    }
}
