package com.smoke.shadowtechandroid;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Engine {
    public vec3 lastPos = new vec3();
    public vec3 camsize = new vec3(0.1f, 1.7f, 0.1f);

    public float[] lightPositions = {
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

    public float[] lightColors = {
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

    public int[] usedLights = {
            1,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    };

    private final float[] scrsurf = {
            -1, -1, 0,
            -1, 1, 0,
            1, 1, 0,
            -1, -1, 0,
            1, 1, 0,
            1, -1, 0
    };

    private final String shadowVertex =
                    "#version 320 es\n" +
                    "in vec3 positions;" +
                    "uniform mat4 sproj[10];" +
                    "uniform mat4 stranslate[10];" +
                    "uniform mat4 sxrot[10];" +
                    "uniform mat4 syrot[10];" +
                    "uniform mat4 meshm;" +
                            "uniform mat4 meshx;" +
                            "uniform mat4 meshy;"+
                            "uniform mat4 meshz;"+
                            "uniform mat4 meshs;"+
                    "uniform int sCnt;" +
                    "void main() {" +
                            "  vec4 tr = meshs * vec4(positions, 1.0f);"+
                            "  tr = meshm * meshx * meshy * meshz * tr;"+
                    "  gl_Position = sproj[sCnt] * sxrot[sCnt] * syrot[sCnt] * stranslate[sCnt] * tr;" +
                    "}";

    private final String shadowFragment =
                    "#version 320 es\n" +
                    "precision mediump float;" +
                    "void main() {" +
                    "}";

    public String vshader =
                    "#version 320 es\n" +
                    "in vec3 positions;" +
                    "void main() {" +
                    "  gl_Position = vec4(positions, 1.0f);" +
                    "}";

    public String fshader =
                    "#version 320 es\n" +
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "uniform sampler2D dtex1;"+
                    "uniform vec2 scrres;"+
                    "out vec4 color;" +
                    "void main() {" +
                    "   vec2 uv = gl_FragCoord.xy/scrres.xy;" +
                    "   vec3 toOut = texture(tex1, uv).rgb * vec3(min((1.0-texture(dtex1, uv).r)*96.0, 1.0));"+
                    "   color = vec4(toOut, 1);" +
                    "}";
    public vec3 pos = new vec3(1.5f, -5f, -1.5f);
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
    private int program;

    public int[] sFrm = new int[10];
    public int[] shadowimg = new int[10];

    public int sprogram;
    public int shadowMapResolution = 4000;
    private FloatBuffer vertexbuf;
    public boolean shadowpass = false;

    public mat40 shadowProj = new mat40();
    public mat40 shadowTrans = new mat40();
    public mat40 shadowxrot = new mat40();
    public mat40 shadowyrot = new mat40();

    private int positionHandle;
    public int[] frstpassfrm = new int[1];
    public int[] frstpasstex = new int[1];
    public int[] frstpassdtex = new int[1];
    public ivec2 passRes = new ivec2(1280, 720);

    public vec3 clearCol = new vec3(0, 0, 0);

    public boolean enablePhysics = true;

    public boolean enableColision = true;

    public static boolean between(float x, float n1, float n2){
        return x >= n1 && x <= n2;
    }
    public boolean aabbPlayer(vec3 meshPos, vec3 meshBorder, boolean enableColision){
        boolean toreturn  = false;
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
    private void setupRPass(){
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, frstpassfrm[0]);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, frstpasstex[0]);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGB, passRes.x ,passRes.y, 0, GLES32.GL_RGB, GLES32.GL_UNSIGNED_BYTE, null);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, frstpassdtex[0]);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_DEPTH_COMPONENT32F, passRes.x ,passRes.y, 0, GLES32.GL_DEPTH_COMPONENT, GLES32.GL_FLOAT, null);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_MODE, GLES32.GL_COMPARE_REF_TO_TEXTURE);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_FUNC, GLES32.GL_LEQUAL);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

        GLES32.glFramebufferTexture(GLES32.GL_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0, frstpasstex[0], 0);
        GLES32.glFramebufferTexture(GLES32.GL_FRAMEBUFFER, GLES32.GL_DEPTH_ATTACHMENT, frstpassdtex[0], 0);

        int[] frdrw = {GLES32.GL_COLOR_ATTACHMENT0};
        GLES32.glDrawBuffers(1, IntBuffer.wrap(frdrw));
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
    }
    private void setupShadowMapping(int sFrm, int shadowimg){
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, sFrm);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);

        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, shadowimg);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_DEPTH_COMPONENT32F, shadowMapResolution, shadowMapResolution, 0, GLES32.GL_DEPTH_COMPONENT, GLES32.GL_FLOAT, null);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_MODE, GLES32.GL_COMPARE_REF_TO_TEXTURE);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_COMPARE_FUNC, GLES32.GL_LEQUAL);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_BORDER);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_BORDER);
        float[] borderColor = { 1.0f, 1.0f, 1.0f, 1.0f };
        GLES32.glTexParameterfv( GLES32.GL_TEXTURE_2D,  GLES32.GL_TEXTURE_BORDER_COLOR, FloatBuffer.wrap(borderColor));
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

        GLES32.glFramebufferTexture(GLES32.GL_FRAMEBUFFER, GLES32.GL_DEPTH_ATTACHMENT, shadowimg, 0);

        int[] frdrw = {0};
        GLES32.glDrawBuffers(1, IntBuffer.wrap(frdrw));
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
    }
    public void Init(){
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES32.glEnable(GLES32.GL_DEPTH_TEST);

        int fshaderprog = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        int vshaderprog = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(fshaderprog, fshader);
        GLES32.glShaderSource(vshaderprog, vshader);
        GLES32.glCompileShader(fshaderprog);
        GLES32.glCompileShader(vshaderprog);
        program = GLES32.glCreateProgram();
        GLES32.glAttachShader(program, fshaderprog);
        GLES32.glAttachShader(program, vshaderprog);
        GLES32.glLinkProgram(program);

        fshaderprog = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        vshaderprog = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(fshaderprog, shadowFragment);
        GLES32.glShaderSource(vshaderprog, shadowVertex);
        GLES32.glCompileShader(fshaderprog);
        GLES32.glCompileShader(vshaderprog);
        sprogram = GLES32.glCreateProgram();
        GLES32.glAttachShader(sprogram, fshaderprog);
        GLES32.glAttachShader(sprogram, vshaderprog);
        GLES32.glLinkProgram(sprogram);

        ByteBuffer bb = ByteBuffer.allocateDirect(scrsurf.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexbuf = bb.asFloatBuffer();
        vertexbuf.put(scrsurf);
        vertexbuf.position(0);

        GLES32.glGenFramebuffers(1, frstpassfrm, 0);
        GLES32.glGenTextures(1, frstpasstex, 0);
        GLES32.glGenTextures(1, frstpassdtex, 0);
        setupRPass();

        GLES32.glGenFramebuffers(10, sFrm, 0);
        GLES32.glGenTextures(10, shadowimg, 0);
        for(int i = 0; i!= 10; i++) {
            setupShadowMapping(sFrm[i], shadowimg[i]);
        }
    }
    public void setLight(int n, vec3 position, vec3 color, int lightState) {
        int selected = n*3;
        lightPositions[selected] = position.x;
        lightPositions[selected+1] = position.y;
        lightPositions[selected+2] = position.z;
        lightColors[selected] = color.x;
        lightColors[selected+1] = color.y;
        lightColors[selected+2] = color.z;
        usedLights[n] = lightState;
    }
    public void copyFloatArray(float[] tocop, float[] dest){
        dest = tocop;
    }
    public void copyucharArray(byte[] tocop, byte[] dest){
        dest = tocop;
    }
    public void beginShadowPass(int cnt){
        shadowpass = true;
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, sFrm[cnt]);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glViewport(0, 0, shadowMapResolution, shadowMapResolution);
        GLES32.glUseProgram(sprogram);
        GLES32.glUniform1i(GLES32.glGetUniformLocation(sprogram, "sCnt"), cnt);
    }
    public void beginMainPass(ivec2 resolution){
        if(enablePhysics == true){
            pos.y += 0.01f;
        }
        shadowpass = false;
        passRes = resolution;
        setupRPass();
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, frstpassfrm[0]);
        GLES32.glClearColor(clearCol.x, clearCol.y, clearCol.z, 1.0f);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glViewport(0, 0, passRes.x, passRes.y);

        if(allowmove == true && touchControls == true){
            if(touchpos.x < resolution.x/2){
                pos.z +=  (float) Math.cos(rot.y) * Math.cos(rot.x) * (((((-touchpos.y/resolution.y)*2) +1)*0.01)*2);
                pos.x -= Math.cos(rot.y) * Math.sin(rot.x) * (((((-touchpos.y/resolution.y)*2) +1)*0.01)*2);
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
    public void endFrame(ivec2 resolution){
        GLES32.glBindFramebuffer(GLES32.GL_FRAMEBUFFER, 0);
        GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT | GLES32.GL_DEPTH_BUFFER_BIT);
        GLES32.glViewport(0, 0, resolution.x, resolution.y);
        GLES32.glUseProgram(program);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, frstpasstex[0]);
        GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "tex1"), 0);

        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, frstpassdtex[0]);
        GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "dtex1"), 1);

        GLES32.glUniform2f(GLES32.glGetUniformLocation(program, "scrres"), passRes.x, passRes.y);

        positionHandle = GLES32.glGetAttribLocation(program, "positions");
        GLES32.glEnableVertexAttribArray(positionHandle);
        GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexbuf);

        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, scrsurf.length/3);
        GLES32.glDisableVertexAttribArray(positionHandle);
        lastPos.y = pos.y;
        lastPos.x = pos.x;
        lastPos.z = pos.z;
    }
}