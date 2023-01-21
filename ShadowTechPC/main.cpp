#include <iostream>

#include "Prop.hpp"

#include "cube/cube_model.hpp"
#include "cube/cube_normals.hpp"
#include "cube/cube_uv.hpp"
#include "cube/cube_texture.hpp"
#include "cube/cubespec_texture.hpp"

#include "monitor/monitor_model.hpp"
#include "monitor/monitor_normals.hpp"
#include "monitor/monitor_uv.hpp"

#include "plane/plane_model.hpp"
#include "plane/plane_normals.hpp"
#include "plane/plane_uv.hpp"

using namespace std;

const char* vertexShaderCode =
                    "#version 400\n"
                    "layout (location = 0) in vec3 positions;" 
                    "layout (location = 1) in vec3 normals;" 
                    "layout (location = 2) in vec2 uv;" 
                    "uniform mat4 proj;" 
                    "uniform mat4 translate;" 
                    "uniform mat4 xrot;" 
                    "uniform mat4 yrot;" 
                    "uniform mat4 meshm;" 
                    "uniform mat4 meshx;" 
                    "uniform mat4 meshy;" 
                    "uniform mat4 meshz;" 
                    "uniform mat4 meshs;" 

                    "uniform mat4 sproj[10];" 
                    "uniform mat4 stranslate[10];" 
                    "uniform mat4 sxrot[10];" 
                    "uniform mat4 syrot[10];" 

                    "out vec2 fuv;"
                    "out vec3 fnormals;"
                    "out vec3 fpos;"
                    "out vec4 projlightmat;"
                    "void main() {" 
                    "  vec4 tr = meshs * vec4(positions, 1.0f);" 
                    "  tr = meshm * meshx * meshy * meshz * tr;" 
                    "  gl_Position = proj * xrot * yrot * translate * tr;" 
                            "fuv = uv;"
                            "fnormals = mat3(transpose(inverse(mat4(1.0f)))) * normals;"
                            "fpos = vec3(mat4(1.0f) * vec4(positions, 1.0f));"
                            "projlightmat = sproj[0] * sxrot[0] * syrot[0] * stranslate[0] * tr;"
                    "}";

const char* fragmentShaderCode =
                    "#version 400\n" 
                    "uniform sampler2D tex1;"
                    "uniform sampler2D spec1;"
                    "uniform sampler2D shadowMap0;"
                    "uniform vec3 lightsPos[10];"
                    "uniform vec3 lightsCol[10];"
                    "uniform int lightStates[10];"
                    "uniform vec3 viewPos;"
                    "in vec4 projlightmat;"
                    "in vec2 fuv;"
                    "in vec3 fnormals;"
                    "in vec3 fpos;"
                    "layout(location = 0) out vec4 color;"
                    "float shadowMapping(){" 
                    "  vec3 projected = projlightmat.xyz / projlightmat.w;" 
                    "  float shadow = 0.0f;" 
                    "  if(projected.z <= 1.0f){" 
                    "   projected = (projected + 1.0f)/2.0f;" 
                    "   float closestDepth = texture(shadowMap0, projected.xy).r;" 
                    "   float currentDepth = projected.z;" 
                    "   if(currentDepth - 0.001 > closestDepth){" 
                    "       shadow+=1.0f;" 
                    "   }" 
                    "  }" 
                    "  return shadow;" 
                    "}" 
                    "float phongl(vec3 lightpos){" 
                    "  float ambient = 0.2;" 
                    "  vec3 norm = normalize(fnormals);" 
                    "  vec3 ldir = normalize(lightpos-fpos);" 
                    "  float diffuse = max(dot(norm, ldir), 0.0);" 
                    "  vec3 viewDir = normalize(-viewPos - fpos);" 
                    "  vec3 halfwayDir = normalize(ldir + viewDir);" 
                    "  float spec = pow(max(dot(norm, halfwayDir), 0.0), 16.0) * texture(spec1, fuv).r;" 
                    "  return float(spec + diffuse)*((1.0-shadowMapping()) + ambient);" 
                    "}" 
                    "void main() {" 
                    "  vec3 toOut = phongl(lightsPos[0]) * lightsCol[0] * texture(tex1, fuv).rgb;" 
                    "  color = vec4( toOut, 1.0);" 
                    "}";

const char* fragmentShaderCode2 =
            "#version 400\n" 
                    "precision mediump float;" 
                    "uniform sampler2D tex1;"
                    "uniform sampler2D spec1;"
                    "uniform sampler2D shadowMap0;"
                    "uniform vec3 lightsPos[10];"
                    "in vec4 projlightmat;"
                    "in vec2 fuv;"
                    "in vec3 fnormals;"
                    "layout(location = 0) out vec4 color;"
                    "void main() {" 
                    "  color = vec4(texture(shadowMap0, fuv).rrr, 1.0);" 
                    "}";

const char* vertexuiShaderCode =
                    "#version 400\n"
                    "layout (location = 0) in vec3 positions;" 
                    "layout (location = 1) in vec3 normals;" 
                    "layout (location = 2) in vec2 uv;" 
                    "uniform mat4 proj;" 
                    "uniform mat4 uiproj;" 
                    "uniform mat4 translate;" 
                    "uniform mat4 xrot;" 
                    "uniform mat4 yrot;" 
                    "uniform mat4 meshm;" 
                    "uniform mat4 meshs;"  

                    "out vec2 fuv;"
                    "out vec3 fnormals;"
                    "out vec3 fpos;"
                    "out vec4 projlightmat;"
                    "void main() {" 
                    "  vec4 tr = meshm * meshs * vec4(positions.xy, 1, 1.0f);" 
                    "  gl_Position = tr * uiproj;" 
                            "fuv = vec2(uv.x, uv.y);"
                    "}";

const char* fragmentuiShaderCode =
                    "#version 400\n" 
                    "uniform sampler2D tex1;"
                    "uniform sampler2D spec1;"
                    "uniform sampler2D shadowMap0;"
                    "uniform vec3 lightsPos[10];"
                    "uniform vec3 lightsCol[10];"
                    "uniform int lightStates[10];"
                    "uniform vec3 viewPos;"
                    "in vec4 projlightmat;"
                    "in vec2 fuv;"
                    "in vec3 fnormals;"
                    "in vec3 fpos;"
                    "layout(location = 0) out vec4 color;"
                    "void main(){"
                    "  color = vec4( texture(tex1, fuv).rgb, 1.0);" 
                    "}";

Engine eng;

float speed = 0.05;

bool mousefocused = true;

void movecallback(){
    int state = glfwGetKey(eng.window, GLFW_KEY_W);
    if (state == GLFW_PRESS){ //w
        eng.pos.z += cos(eng.rot.y) * cos(eng.rot.x) * speed;
        eng.pos.x += cos(eng.rot.y) * sin(eng.rot.x) * -speed;
    }
    state = glfwGetKey(eng.window, GLFW_KEY_A);
    if (state == GLFW_PRESS){ // a
        eng.pos.x += cos(eng.rot.y) * cos(eng.rot.x) * speed;
        eng.pos.z -= cos(eng.rot.y) * sin(eng.rot.x) * -speed;
    }
    state = glfwGetKey(eng.window, GLFW_KEY_S);
    if (state == GLFW_PRESS){ // s
        eng.pos.z -= cos(eng.rot.y) * cos(eng.rot.x) * speed;
        eng.pos.x -= cos(eng.rot.y) * sin(eng.rot.x) * -speed;
    }
    state = glfwGetKey(eng.window, GLFW_KEY_D);
    if (state == GLFW_PRESS){ //d
        eng.pos.x -= cos(eng.rot.y) * cos(eng.rot.x) * speed;
        eng.pos.z += cos(eng.rot.y) * sin(eng.rot.x) * -speed;
    }
    state = glfwGetKey(eng.window, GLFW_KEY_F11);
    if (state == GLFW_PRESS){ //d
        switch(mousefocused){
            case false:
            mousefocused = true;
            break;
            case true:
            mousefocused = false;
            break;
        }
    }
}

int main(){
    eng.Init();
    eng.shadowProj.buildperspectivemat(90, 0.1, 100, 1, 0);
    //eng.shadowProj.buildorthomat(1, -1, 1, -1, speedf, 100f);
    eng.shadowTrans.buildtranslatemat(vec3(0, 0, -1), 0);
    eng.shadowxrot.buildxrotmat(-0.2f, 0);
    eng.shadowyrot.buildyrotmat(0, 0);
    eng.setLight(0, vec3(0, 1, 2), vec3(1, 1, 0.5f), 1);

    Mesh triangle;

    eng.copyFloatArray(cube_model().verts, triangle.vertexes);
    eng.copyFloatArray(cube_normals().normals, triangle.normals);
    eng.copyFloatArray(cube_uv().uv, triangle.uv);
    eng.copyucharArray(cube_texture().pixels, triangle.texture);
    eng.copyucharArray(cubespec_texture().pixels, triangle.specular);
    triangle.totalv = cube_model().totalv;
    triangle.texResolution.x = cube_texture().resx;
    triangle.texResolution.y = cube_texture().resy;
    triangle.meshPosition.z = -1.5f;
    triangle.meshPosition.y = 5;
    triangle.initMesh(fragmentShaderCode, vertexShaderCode);
    triangle.colision = false;

    Mesh triangle2;

    eng.copyFloatArray(cube_model().verts, triangle2.vertexes);
    eng.copyFloatArray(cube_normals().normals, triangle2.normals);
    eng.copyFloatArray(cube_uv().uv, triangle2.uv);
    eng.copyucharArray(cube_texture().pixels, triangle2.texture);
    eng.copyucharArray(cubespec_texture().pixels, triangle2.specular);
    triangle2.totalv = cube_model().totalv;
    triangle2.texResolution.x = cube_texture().resx;
    triangle2.texResolution.y = cube_texture().resy;
    triangle2.meshPosition.z = -1.5f;
    triangle2.meshPosition.y = 5;
    triangle2.meshPosition.x = 2.5f;
    triangle2.initMesh(fragmentShaderCode, vertexShaderCode);

    Mesh plane;

    eng.copyFloatArray(plane_model().verts, plane.vertexes);
    eng.copyFloatArray(plane_normals().normals, plane.normals);
    eng.copyFloatArray(plane_uv().uv, plane.uv);
    eng.copyucharArray(cube_texture().pixels, plane.texture);
    eng.copyucharArray(cubespec_texture().pixels, plane.specular);
    plane.totalv = plane_model().totalv;
    plane.texResolution.x = cube_texture().resx;
    plane.texResolution.y = cube_texture().resy;
    plane.meshPosition.y = -0.5f;
    plane.initMesh(fragmentShaderCode, vertexShaderCode);

    Mesh monitor;

    eng.copyFloatArray(monitor_model().verts, monitor.vertexes);
    eng.copyFloatArray(monitor_normals().normals, monitor.normals);
    eng.copyFloatArray(monitor_uv().uv, monitor.uv);
    eng.copyucharArray(cube_texture().pixels, monitor.texture);
    eng.copyucharArray(cubespec_texture().pixels, monitor.specular);
    monitor.totalv = monitor_model().totalv;
    monitor.texResolution.x = cube_texture().resx;
    monitor.texResolution.y = cube_texture().resy;
    monitor.meshPosition = vec3(-1.5f, 0.5f, 1.5f);
    monitor.meshScale.y = 2;
    monitor.initMesh(fragmentShaderCode2, vertexShaderCode);

    Prop triangleProp;

    vec2 mousepos;

    triangle.meshRot.y = 0.5;
    triangle.meshScale.y = 1.5;

    Mesh scr;
    scr.meshPosition.x = -1;
    scr.makeQuad(fragmentuiShaderCode, vertexuiShaderCode, eng, cube_texture().pixels, cube_texture().resx, cube_texture().resy);

    while (!glfwWindowShouldClose(eng.window)){
        if(mousefocused == true){
            glfwSetInputMode(eng.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            glfwGetCursorPos(eng.window, &mousepos.x, &mousepos.y);
            eng.rot.x = mousepos.x/eng.resolution.x;
            eng.rot.y = -mousepos.y/eng.resolution.y;
        }else{
            glfwSetInputMode(eng.window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            glfwGetCursorPos(eng.window, &mousepos.x, &mousepos.y);
        }

        eng.beginShadowPass(0);

        triangle.Draw(eng);
        plane.Draw(eng);
        monitor.Draw(eng);

        triangle2.Draw(eng);

        eng.beginMainPass();

        movecallback();
        scr.Draw(eng);
        triangleProp.MeshMeshInteract(triangle, plane);
        triangleProp.MeshMeshInteract(triangle2, plane);
        triangle.Draw(eng);
        plane.Draw(eng);
        monitor.Draw(eng);
        triangle2.Draw(eng);

        eng.endFrame();
    }
    return 1;
}