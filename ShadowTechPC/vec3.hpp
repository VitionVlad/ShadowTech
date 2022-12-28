#include <iostream>

#include <math.h>

class vec3 {
    public:
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;
    vec3(float r, float g, float b){
        x = r;
        y = g;
        z = b;
    }
    vec3(){}
};