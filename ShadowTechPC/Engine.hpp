#include <iostream>

#include <vector>

#include <string>

#include <cstring>

#include <GL/glew.h>

#include <GLFW/glfw3.h>

#include "mat40.hpp"

#include "clickZone.hpp"

#include "audio.hpp"

using namespace std;

class Engine{
    public:
    const char* shadowVertex =
                    "#version 400\n" 
                    "layout (location = 0) in vec3 positions;" 
                    "uniform mat4 sproj[10];" 
                    "uniform mat4 stranslate[10];" 
                    "uniform mat4 sxrot[10];" 
                    "uniform mat4 syrot[10];" 
                    "uniform mat4 meshm;" 
                    "uniform mat4 meshx;" 
                    "uniform mat4 meshy;" 
                    "uniform mat4 meshz;" 
                    "uniform mat4 meshs;" 
                    "uniform int sCnt;" 
                    "void main() {" 
                    "  vec4 tr = meshs * vec4(positions, 1.0f);" 
                    "  tr = meshm * meshx * meshy * meshz * tr;" 
                    "  gl_Position = sproj[0] * sxrot[0] * syrot[0] * stranslate[0] * tr;" 
                    "}";

    const char* shadowFragment =
              
                    "#version 400\n" 
                    "void main() {" 
                    "}";

    const char* vshader =
                    "#version 400\n" 
                    "layout (location = 0) in vec3 positions;" 
                    "void main() {" 
                    "  gl_Position = vec4(positions, 1.0f);" 
                    "}";

    const char* fshader =
                    "#version 400\n" 
                    "uniform sampler2D tex1;"
                    "uniform sampler2D dtex1;"
                    "uniform vec2 scrres;"
                    "out vec4 color;" 
                    "void main() {" 
                    "   vec2 uv = gl_FragCoord.xy/scrres.xy;" 
                    "  color = vec4( texture(tex1, uv).rgb, 1);" 
                    "}";

    vec3 lastPos = vec3(0, 0, 0);
    vec3 camsize = vec3(0.1f, 1.7f, 0.1f);

    float lightPositions[30] = {
        0, 0, 0, // 1 0
        0, 0, 0, // 2 3
        0, 0, 0, // 3 6
        0, 0, 0, // 4 9
        0, 0, 0, // 5 12
        0, 0, 0, // 6 15
        0, 0, 0, // 7 18
        0, 0, 0, // 8 21
        0, 0, 0, // 9 24
        0, 0, 0, // 10 27
    };

    float lightColors[30] = {
        0, 0, 0, // 1 0
        0, 0, 0, // 2 3
        0, 0, 0, // 3 6
        0, 0, 0, // 4 9
        0, 0, 0, // 5 12
        0, 0, 0, // 6 15
        0, 0, 0, // 7 18
        0, 0, 0, // 8 21
        0, 0, 0, // 9 24
        0, 0, 0, // 10 27
    };

    int usedLights[10] = {
        1,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0};

    const float scrsurf[18] = {
        -1, -1, 0,
        -1, 1, 0,
        1, 1, 0,
        -1, -1, 0,
        1, 1, 0,
        1, -1, 0
    };
    GLuint surfhandle;
    GLuint surfahandle;

    vec3 pos = vec3(0, -5, -1.5f);
    vec2 rot = vec2(0, 0);
    vec2 touchpos = vec2(0, 0);

    float speed = 0;
    bool allowmove = false;
    mat4 perspective = mat4();
    mat4 translate = mat4();
    mat4 xrot = mat4();
    mat4 yrot = mat4();
    bool touchControls = true;
    float fov = 110;
    GLuint program;

    GLuint sFrm[10];
    GLuint sprogram;
    GLuint shadowimg[10];
    int shadowMapResolution = 4000;
    bool shadowpass = false;

    mat40 shadowProj;
    mat40 shadowTrans;
    mat40 shadowxrot;
    mat40 shadowyrot;

    GLuint positionHandle;
    GLuint frstpassfrm;
    GLuint frstpasstex;
    GLuint frstpassdtex;
    ivec2 passRes = ivec2(1280, 720);
    ivec2 resolution = ivec2(1280, 720);

    vec3 clearCol = vec3(0, 0, 0);

    bool enablePhysics = true;

    bool enableColision = true;

    GLFWwindow* window;

    static bool between(float x, float n1, float n2){
        return x >= n1 && x <= n2;
    }
    bool aabbPlayer(vec3 meshPos, vec3 meshBorder, bool enableColision){
        bool toreturn  = false;
        if (between(-pos.x, meshPos.x - meshBorder.x - camsize.x, meshPos.x + meshBorder.x + camsize.x) && between(-pos.y, meshPos.y - meshBorder.y, meshBorder.y + meshPos.y + camsize.y) && between(-pos.z, meshPos.z - meshBorder.z - camsize.z, meshBorder.z + meshPos.z + camsize.z)){
            if(enableColision == true){
                pos.y = lastPos.y;
            }
            toreturn = true;
            if (between(-pos.y, meshPos.y - meshBorder.y, meshBorder.y + meshPos.y + camsize.y / 2)){
                if(enableColision == true){
                    pos.x = lastPos.x;
                    pos.z = lastPos.z;
                }
            }
        }
        return toreturn;
    }
    void setupRPass(){
        glBindFramebuffer(GL_FRAMEBUFFER, frstpassfrm);
        glEnable(GL_DEPTH_TEST);

        glBindTexture(GL_TEXTURE_2D, frstpasstex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, passRes.x, passRes.y, 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        glBindTexture(GL_TEXTURE_2D, frstpassdtex);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, passRes.x, passRes.y, 0, GL_DEPTH_COMPONENT, GL_FLOAT, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, frstpasstex, 0);
        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, frstpassdtex, 0);

        GLenum frdrw = {GL_COLOR_ATTACHMENT0};
        glDrawBuffers(1, &frdrw);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    void setupShadowMapping(GLuint& sFrm, GLuint& shadowimg)
    {
        glBindFramebuffer(GL_FRAMEBUFFER, sFrm);
        glEnable(GL_DEPTH_TEST);

        glBindTexture(GL_TEXTURE_2D, shadowimg);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32F, shadowMapResolution, shadowMapResolution, 0, GL_DEPTH_COMPONENT, GL_FLOAT, NULL);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);
        float borderColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        glTexParameterfv(GL_TEXTURE_2D,  GL_TEXTURE_BORDER_COLOR, borderColor);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glBindTexture(GL_TEXTURE_2D, 0);

        glFramebufferTexture(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, shadowimg, 0);

        glDrawBuffers(0, NULL);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    void Init(){
        cout << "engine_init:\u001b[36m engine started initializing\u001b[37m" << endl;
        glfwInit();
        cout << "engine_init:\u001b[36m glfw inited!\u001b[37m" << endl;
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        cout << "engine_init:\u001b[36m core profile 4.0!\u001b[37m" << endl;
        window = glfwCreateWindow(resolution.x, resolution.y, "ShadowTech/OpenGl 4.0/Linux", NULL, NULL);
        cout << "engine_init:\u001b[36m window created!\u001b[37m" << endl;
        glfwMakeContextCurrent(window);
        cout << "engine_init:\u001b[36m context focused!\u001b[37m" << endl;
        glewExperimental = true;
        glewInit();
        cout << "engine_init:\u001b[36m glew inited\u001b[37m" << endl;

        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_DEPTH_TEST);

        cout << "engine_init:\u001b[36m depth test enabled\u001b[37m" << endl;

        glGenBuffers(1, &surfhandle);
        glBindBuffer(GL_ARRAY_BUFFER, surfhandle); 
        glBufferData(GL_ARRAY_BUFFER, sizeof(scrsurf), scrsurf, GL_DYNAMIC_DRAW);
        glGenVertexArrays(1, &surfahandle);  
        glBindBuffer(GL_ARRAY_BUFFER, 0); 

        cout << "engine_init:\u001b[36m screen surface created\u001b[37m" << endl;

        int fshaderprog = glCreateShader(GL_FRAGMENT_SHADER);
        int vshaderprog = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(fshaderprog, 1, &fshader, NULL);
        glShaderSource(vshaderprog, 1, &vshader, NULL);
        glCompileShader(fshaderprog);
        glCompileShader(vshaderprog);
        program = glCreateProgram();
        glAttachShader(program, fshaderprog);
        glAttachShader(program, vshaderprog);
        glLinkProgram(program);

        cout << "engine_init:\u001b[36m first program created\u001b[37m" << endl;

        fshaderprog = glCreateShader(GL_FRAGMENT_SHADER);
        vshaderprog = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(fshaderprog, 1, &shadowFragment, NULL);
        glShaderSource(vshaderprog, 1, &shadowVertex, NULL);
        glCompileShader(fshaderprog);
        glCompileShader(vshaderprog);
        sprogram = glCreateProgram();
        glAttachShader(sprogram, fshaderprog);
        glAttachShader(sprogram, vshaderprog);
        glLinkProgram(sprogram);

        cout << "engine_init:\u001b[36m second program created\u001b[37m" << endl;

        glGenFramebuffers(1, &frstpassfrm);
        glGenTextures(1, &frstpasstex);
        glGenTextures(1, &frstpassdtex);
        cout << "engine_init:\u001b[36m first pass generated\u001b[37m" << endl;
        setupRPass();
        cout << "engine_init:\u001b[36m first pass created\u001b[37m" << endl;

        glGenFramebuffers(10, sFrm);
        glGenTextures(10, shadowimg);

        cout << "engine_init:\u001b[36m second pass generated\u001b[37m" << endl;
        
        for(int i = 0; i != 10; i++){
            setupShadowMapping(sFrm[i], shadowimg[i]);
        }

        cout << "engine_init:\u001b[36m second pass created\u001b[37m" << endl;

        lastPos.x = pos.x;
        lastPos.y = pos.y;
        lastPos.z = pos.z;

        cout << "engine_init:\u001b[36m alut inited!\u001b[37m" << endl;

        cout << "engine_init:\u001b[32m finished!\u001b[37m" << endl;
    }
    void setLight(int n, vec3 position, vec3 color, int lightState)
    {
        int selected = n*3;
        lightPositions[selected] = position.x;
        lightPositions[selected + 1] = position.y;
        lightPositions[selected + 2] = position.z;
        lightColors[selected] = color.x;
        lightColors[selected + 1] = color.y;
        lightColors[selected + 2] = color.z;
        usedLights[n] = lightState;
    }
    void copyFloatArray(float tocop[16383], float dest[16383]){
        memcpy(dest, tocop, 16383);
    }
    void copyucharArray(unsigned char tocop[65535], unsigned char dest[65535]){
        memcpy(dest, tocop, 65535);
    }
    void beginShadowPass(int cnt)
    {
        shadowpass = true;
        glBindFramebuffer(GL_FRAMEBUFFER, sFrm[cnt]);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, shadowMapResolution, shadowMapResolution);
        glUseProgram(sprogram);
        glUniform1i(glGetUniformLocation(sprogram, "sCnt"), cnt);
    }
    void beginMainPass()
    {
        glfwGetFramebufferSize(window, &resolution.x, &resolution.y);
        if(enablePhysics == true){
            pos.y += 0.05f;
        }
        shadowpass = false;
        passRes = resolution;
        setupRPass();
        glBindFramebuffer(GL_FRAMEBUFFER, frstpassfrm);
        glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, passRes.x, passRes.y);

        if (allowmove == true && touchControls == true)
        {
            if (touchpos.x < resolution.x / 2)
            {
                pos.z += (float)cos(rot.y) * cos(rot.x) * ((((-touchpos.y / resolution.y) * 2) + 1) * 0.01);
                pos.x -= cos(rot.y) * sin(rot.x) * ((((-touchpos.y / resolution.y) * 2) + 1) * 0.01);
            }
            else if (touchpos.x > resolution.x / 2)
            {
                rot.x += (((touchpos.x / (resolution.x * 1.7)) * 2) - 1) * 0.1;
                rot.y += (float)((((-touchpos.y / resolution.y) * 2) + 1) * 0.01);
            }
        }
        perspective.buildperspectivemat(fov, 0.1f, 100, resolution.x / resolution.y);
        yrot.buildyrotmat(-rot.x);
        xrot.buildxrotmat(rot.y);
        translate.buildtranslatemat(pos);
    }
    void endFrame()
    {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, resolution.x, resolution.y);
        glUseProgram(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, frstpasstex);
        glUniform1i(glGetUniformLocation(program, "tex1"), 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, frstpassdtex);
        glUniform1i(glGetUniformLocation(program, "dtex1"), 1);

        glUniform2f(glGetUniformLocation(program, "scrres"), passRes.x, passRes.y);

        glBindVertexArray(surfahandle);
        glBindBuffer(GL_ARRAY_BUFFER, surfhandle); 
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

        glDrawArrays(GL_TRIANGLES, 0, 6);
        glDisableVertexAttribArray(0);
        glfwSwapBuffers(window);
        glfwPollEvents();
        lastPos.y = pos.y;
        lastPos.x = pos.x;
        lastPos.z = pos.z;
    }
};
