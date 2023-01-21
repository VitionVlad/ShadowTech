#include <iostream>

#include <math.h>

#include "vec3.hpp"

class mat4 {
    public:
    float mat[16] = {
            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0 // 12 13 14 15 3 7 11 15
    };
    void buildtranslatemat(vec3 pos){
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 1;
        mat[15] = 1;
        mat[12] = pos.x;
        mat[13] = pos.y;
        mat[14] = pos.z;
    }
    void buildxrotmat(float angle){
        mat[0] = 1;
        mat[5] = (float) cos(angle);
        mat[6] = (float) -sin(angle);
        mat[9] = (float) sin(angle);
        mat[10] = (float) cos(angle);
        mat[15] = 1;
    }
    void buildyrotmat(float angle){
        mat[0] = (float) cos(angle);
        mat[5] = 1.0f;
        mat[2] = (float) sin(angle);
        mat[8] = (float) -sin(angle);
        mat[10] = (float) cos(angle);
        mat[15] = 1;
    }
    void buildzrotmat(float angle){
        mat[0] = (float) cos(angle);
        mat[5] = (float) cos(angle);
        mat[4] = (float) sin(angle);
        mat[1] = (float) -sin(angle);
        mat[10] = 1;
        mat[15] = 1;
    }
    void buildperspectivemat(float fov, float zNear, float zFar, float aspect){
        float S = (float) tan((fov/2)*(M_PI/180));
        mat[0] = 1/(aspect*S);
        mat[5] = 1/S;
        mat[10] = -zFar/(zFar-zNear);
        mat[11] = -1;
        mat[14] = -zFar*zNear/(zFar-zNear);
    }
    void buildorthomat(float r, float l, float t, float b, float zNear, float zFar){
        mat[0] = 2/(r-l);
        mat[5] = 2/(r-l);
        mat[10] = -2/(zFar-zNear);
        mat[15] = 1;
        mat[3] = (r+r)/(r-l);
        mat[7] = (t+b)/(t-b);
        mat[11] = (zFar+zNear)/(zFar-zNear);
    }
    void builduimat(float r, float t, float zNear, float zFar, float aspect){
        float lr = aspect*r;
        float l = -r;
        float b = -t;
        mat[0] = 2/(lr-l);
        mat[5] = 2/(lr-l);
        mat[10] = -2/(zFar-zNear);
        mat[15] = 1;
        mat[3] = (lr+r)/(lr-l);
        mat[7] = (t+b)/(t-b);
        mat[11] = (zFar+zNear)/(zFar-zNear);
    }
    void buildIdentityMat(){
        mat[0] = 1;
        mat[5] = 1;
        mat[10] = 1;
        mat[15] = 1;
    }
    void buildScaleMat(vec3 scale){
        mat[0] = scale.x;
        mat[5] = scale.y;
        mat[10] = scale.z;
        mat[15] = 1;
    }
};