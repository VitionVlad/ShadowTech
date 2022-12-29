#include "mat4.hpp"

class mat40 {
    public:
    float mat[160] = {
            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0, // 12 13 14 15 3 7 11 15

            0, 0, 0, 0, // 0 1 2 3    0 4 8 12
            0, 0, 0, 0, // 4 5 6 7    1 5 9 13
            0, 0, 0, 0, // 8 9 10 11  2 6 10 14
            0, 0, 0, 0 // 12 13 14 15 3 7 11 15
    };
    void buildtranslatemat(vec3 pos, int cnt){
        mat[0+cnt*16] = 1;
        mat[5+cnt*16] = 1;
        mat[10+cnt*16] = 1;
        mat[15+cnt*16] = 1;
        mat[12+cnt*16] = pos.x;
        mat[13+cnt*16] = pos.y;
        mat[14+cnt*16] = pos.z;
    }
    void buildxrotmat(float angle, int cnt){
        mat[0+cnt*16] = 1;
        mat[5+cnt*16] = (float) cos(angle);
        mat[6+cnt*16] = (float) -sin(angle);
        mat[9+cnt*16] = (float) sin(angle);
        mat[10+cnt*16] = (float) cos(angle);
        mat[15+cnt*16] = 1;
    }
    void buildyrotmat(float angle, int cnt){
        mat[0+cnt*16] = (float) cos(angle);
        mat[5+cnt*16] = 1.0f;
        mat[2+cnt*16] = (float) sin(angle);
        mat[8+cnt*16] = (float) -sin(angle);
        mat[10+cnt*16] = (float) cos(angle);
        mat[15+cnt*16] = 1;
    }
    void buildperspectivemat(float fov, float zNear, float zFar, float aspect, int cnt){
        float S = (float) tan((fov/2)*(M_PI/180));
        mat[0+cnt*16] = 1/(aspect*S);
        mat[5+cnt*16] = 1/S;
        mat[10+cnt*16] = -zFar/(zFar-zNear);
        mat[11+cnt*16] = -1;
        mat[14+cnt*16] = -zFar*zNear/(zFar-zNear);
    }
    void buildorthomat(float r, float l, float t, float b, float zNear, float zFar, int cnt){
        mat[0+cnt*16] = 2/(r-l);
        mat[5+cnt*16] = 2/(r-l);
        mat[10+cnt*16] = -2/(zFar-zNear);
        mat[15+cnt*16] = 1;
        mat[3+cnt*16] = (r+r)/(r-l);
        mat[7+cnt*16] = (t+b)/(t-b);
        mat[11+cnt*16] = (zFar+zNear)/(zFar-zNear);
    }
    void buildIdentityMat(int cnt){
        mat[0+cnt*16] = 1;
        mat[5+cnt*16] = 1;
        mat[10+cnt*16] = 10;
        mat[15+cnt*16] = 1;
    }
};