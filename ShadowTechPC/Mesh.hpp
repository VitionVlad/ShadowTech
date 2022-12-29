#include <iostream>

#include <vector>

#include <string>

#include "Engine.hpp"

using namespace std;

class Mesh {
    public:
    float vertexes[16383];
    float normals[16383];
    float uv[16383];
    int totalv;

    unsigned char texture[65535];
    unsigned char specular[65535];
    ivec2 texResolution;

    GLuint program;
    GLuint positionHandle;
    GLuint normalHandle;
    GLuint uvHandle;
    GLuint albedoHandle;
    GLuint specularHandle;
    vec3 meshPosition;
    mat4 meshMatrix;
    vec3 aabb;

    GLuint VBO;
    GLuint NBO;
    GLuint UVBO;

    GLuint VAO;

    bool enablePLayerInteract = true;

    void CalcAABB(){
        for(int i = 0; i!= 16383; i+=3){
            if(abs(vertexes[i]) >= aabb.x ){
                aabb.x = abs(vertexes[i]);
            }
            if(abs(vertexes[i+1]) >= aabb.y ){
                aabb.y = abs(vertexes[i+1]);
            }
            if(abs(vertexes[i+2]) >= aabb.z ){
                aabb.z = abs(vertexes[i+2]);
            }
        }
    }
    void initMesh(const char* fshader, const char* vshader, Engine handle){
        glGenVertexArrays(1, &VAO);

        glGenBuffers(1, &VBO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO); 
        glBufferData(GL_ARRAY_BUFFER, sizeof(vertexes), vertexes, GL_STATIC_DRAW); 

        glGenBuffers(1, &NBO);
        glBindBuffer(GL_ARRAY_BUFFER, NBO); 
        glBufferData(GL_ARRAY_BUFFER, sizeof(normals), normals, GL_STATIC_DRAW);

        glGenBuffers(1, &UVBO);
        glBindBuffer(GL_ARRAY_BUFFER, UVBO); 
        glBufferData(GL_ARRAY_BUFFER, sizeof(uv), uv, GL_STATIC_DRAW);

        int fshaderprog = glCreateShader(GL_FRAGMENT_SHADER);
        int vshaderprog = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(fshaderprog, 1, &fshader, NULL);
        glShaderSource(vshaderprog, 1, &vshader, NULL);
        glCompileShader(fshaderprog);

        int  success;
        char infoLog[512];
        glGetShaderiv(fshaderprog, GL_COMPILE_STATUS, &success);
        if(!success)
        {
            glGetShaderInfoLog(fshaderprog, 512, NULL, infoLog);
            std::cout << "shader_error:fragment compilation error:\n" << infoLog << std::endl;
        }

        glCompileShader(vshaderprog);

        glGetShaderiv(vshaderprog, GL_COMPILE_STATUS, &success);
        if(!success)
        {
            glGetShaderInfoLog(fshaderprog, 512, NULL, infoLog);
            std::cout << "shader_error:vertex compilation error:\n" << infoLog << std::endl;
        }

        program = glCreateProgram();
        glAttachShader(program, fshaderprog);
        glAttachShader(program, vshaderprog);
        glLinkProgram(program);
        
        glGenTextures(1, &albedoHandle);
        glBindTexture(GL_TEXTURE_2D, albedoHandle);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texResolution.x ,texResolution.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, texture);
        glActiveTexture(GL_TEXTURE0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
        float borderColor[4] = { 0.0f, 0.0f, 0.0f, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);

        glGenTextures(1, &specularHandle);
        glBindTexture(GL_TEXTURE_2D, specularHandle);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texResolution.x ,texResolution.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, specular);
        glActiveTexture(GL_TEXTURE1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, borderColor);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
        CalcAABB();
    }
    void Draw(Engine& handle){
        if(handle.shadowpass == false){
            if(handle.enablePhysics == true && enablePLayerInteract == true){
                handle.aabbPlayer(meshPosition, aabb);
            }
            glUseProgram(program);

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, albedoHandle);
            glUniform1i(glGetUniformLocation(program, "tex1"), 0);

            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, specularHandle);
            glUniform1i(glGetUniformLocation(program, "spec1"), 1);

            for(int i = 2; i!=12; i++){
                string n = "shadowMap"+to_string(i-2);
                glActiveTexture(GL_TEXTURE0+i);
                glBindTexture(GL_TEXTURE_2D, handle.shadowimg[i-2]);
                glUniform1i(glGetUniformLocation(program, n.c_str()), i);
            }

            glUniform3fv(glGetUniformLocation(program, "lightsPos"), 10, handle.lightPositions);

            glUniform3fv(glGetUniformLocation(program, "lightsCol"), 10, handle.lightColors);

            glUniform3f(glGetUniformLocation(program, "viewPos"), handle.pos.x, handle.pos.y, handle.pos.z);

            glBindVertexArray(VAO);
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferData(GL_ARRAY_BUFFER, sizeof(vertexes), vertexes, GL_STATIC_DRAW); 
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, (void*)0);

            glBindBuffer(GL_ARRAY_BUFFER, NBO);
            glBufferData(GL_ARRAY_BUFFER, sizeof(normals), normals, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, (void*)0);

            glBindBuffer(GL_ARRAY_BUFFER, UVBO);
            glBufferData(GL_ARRAY_BUFFER, sizeof(uv), uv, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, (void*)0);
            meshMatrix.buildtranslatemat(meshPosition);

            glUniformMatrix4fv(glGetUniformLocation(program, "proj"), 1, false, handle.perspective.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "translate"), 1, false, handle.translate.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "xrot"), 1, false, handle.xrot.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "yrot"), 1, false, handle.yrot.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "meshm"), 1, false, meshMatrix.mat);
            glUniform1iv(glGetUniformLocation(program, "lightStates"), 10, handle.usedLights);

            glUniformMatrix4fv(glGetUniformLocation(program, "sproj"), 10, false, handle.shadowProj.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "stranslate"), 10, false, handle.shadowTrans.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "sxrot"), 10, false, handle.shadowxrot.mat);
            glUniformMatrix4fv(glGetUniformLocation(program, "syrot"), 10, false, handle.shadowyrot.mat);
        }else{
            meshMatrix.buildtranslatemat(meshPosition);

            glBindVertexArray(VAO);
            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBufferData(GL_ARRAY_BUFFER, sizeof(vertexes), vertexes, GL_STATIC_DRAW); 
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 3*sizeof(float),  (void*)0);


            glUniformMatrix4fv(glGetUniformLocation(handle.sprogram, "sproj"), 10, false, handle.shadowProj.mat);
            glUniformMatrix4fv(glGetUniformLocation(handle.sprogram, "stranslate"), 10, false, handle.shadowTrans.mat);
            glUniformMatrix4fv(glGetUniformLocation(handle.sprogram, "sxrot"), 10, false, handle.shadowxrot.mat);
            glUniformMatrix4fv(glGetUniformLocation(handle.sprogram, "syrot"), 10, false, handle.shadowyrot.mat);
            glUniformMatrix4fv(glGetUniformLocation(handle.sprogram, "meshm"), 1, false, meshMatrix.mat);
        }

        glDrawArrays(GL_TRIANGLES, 0, totalv);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
};
