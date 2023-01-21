package com.smoke.shadowtechandroid;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mesh {
    public FloatBuffer vertexbuf;
    private FloatBuffer normalbuf;
    private FloatBuffer uvbuf;

    public float[] vertexes;
    public float[] normals;
    public float[] uv;

    public byte[] texture;
    public byte[] specular;
    public ivec2 texResolution;

    private int program;
    private int positionHandle;
    private int normalHandle;
    private int uvHandle;
    private int[] albedoHandle = new int[1];
    private int[] specularHandle = new int[1];

    public vec3 meshPosition = new vec3();
    private mat4 meshMatrix = new mat4();

    public vec3 meshRot = new vec3();
    private mat4[] rotMat = new mat4[3];

    public vec3 meshScale = new vec3(1, 1, 1);
    private mat4 scaleMat = new mat4();

    public vec3 aabb = new vec3();

    public boolean enablePLayerInteract = true;

    public boolean colision = true;

    public boolean isinteracting = false;

    void vecmatmult(vec3 vec, mat4 mat){
        vec3 tof = new vec3();
        tof.x = vec.x * mat.mat[0] + vec.y * mat.mat[4] + vec.z * mat.mat[8] + mat.mat[12];
        tof.y = vec.x * mat.mat[1] + vec.y * mat.mat[5] + vec.z * mat.mat[9] + mat.mat[13];
        tof.z = vec.x * mat.mat[2] + vec.y * mat.mat[6] + vec.z * mat.mat[10] + mat.mat[14];
        float w = mat.mat[3] + mat.mat[7] +  mat.mat[11] +  mat.mat[15];
        tof.x /= w;
        tof.y /= w;
        tof.z /= w;
        vec = tof;
    }
    private void CalcAABB(){
        for(int i = 0; i!= vertexes.length; i+=3){
            vec3 ver = new vec3();
            ver.x = vertexes[i];
            ver.y = vertexes[i+1];
            ver.z = vertexes[i+2];
            vecmatmult(ver, rotMat[0]);
            vecmatmult(ver, rotMat[1]);
            vecmatmult(ver, rotMat[2]);
            vecmatmult(ver, scaleMat);
            if(Math.abs(ver.x) >= aabb.x ){
                aabb.x = Math.abs(ver.x);
            }
            if(Math.abs(ver.y) >= aabb.y ){
                aabb.y = Math.abs(ver.y);
            }
            if(Math.abs(ver.z) >= aabb.z ){
                aabb.z = Math.abs(ver.z);
            }
        }
    }
    public void initMesh(String fshader, String vshader){
        rotMat[0] = new mat4();
        rotMat[1] = new mat4();
        rotMat[2] = new mat4();
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
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexes.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexbuf = bb.asFloatBuffer();
        vertexbuf.put(vertexes);
        vertexbuf.position(0);

        bb = ByteBuffer.allocateDirect(normals.length * 4);
        bb.order(ByteOrder.nativeOrder());
        normalbuf = bb.asFloatBuffer();
        normalbuf.put(normals);
        normalbuf.position(0);

        bb = ByteBuffer.allocateDirect(uv.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvbuf = bb.asFloatBuffer();
        uvbuf.put(uv);
        uvbuf.position(0);

        ByteBuffer buffer = ByteBuffer.allocateDirect(texture.length);
        buffer.put(texture);
        buffer.position(0);
        GLES32.glGenTextures(1, albedoHandle, 0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, albedoHandle[0]);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA, texResolution.x ,texResolution.y, 0, GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, buffer);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_MIRRORED_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_MIRRORED_REPEAT);
        float[] borderColor = { 0.0f, 0.0f, 0.0f, 1.0f };
        GLES32.glTexParameterfv(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BORDER_COLOR, FloatBuffer.wrap(borderColor));
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);

        buffer = ByteBuffer.allocateDirect(specular.length);
        buffer.put(specular);
        buffer.position(0);
        GLES32.glGenTextures(1, specularHandle, 0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, specularHandle[0]);
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA, texResolution.x ,texResolution.y, 0, GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, buffer);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_MIRRORED_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_MIRRORED_REPEAT);
        GLES32.glTexParameterfv(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BORDER_COLOR, FloatBuffer.wrap(borderColor));
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
        CalcAABB();
    }
    void makeQuad(String fshader, String vshader, Engine handle, byte[] tex, int tx, int ty){
        vertexes = new float[18];
        vertexes[0] = -1;
        vertexes[1] = 0.9f;
        vertexes[2] = 1;
        vertexes[3] = -1;
        vertexes[4] = 1;
        vertexes[5] = 1;
        vertexes[6] = -0.9f;
        vertexes[7] = 1;
        vertexes[8] = 1;
        vertexes[9] = -1;
        vertexes[10] = 0.9f;
        vertexes[11] = 1;
        vertexes[12] = -0.9f;
        vertexes[13] = 1;
        vertexes[14] = 1;
        vertexes[15] = -0.9f;
        vertexes[16] = 0.9f;
        vertexes[17] = 1;

        uv = new float[12];
        uv[0] = 0;
        uv[1] = 1;
        uv[2] = 0;
        uv[3] = 0;
        uv[4] = 1;
        uv[5] = 0;
        uv[6] = 0;
        uv[7] = 1;
        uv[8] = 1;
        uv[9] = 0;
        uv[10] = 1;
        uv[11] = 1;
        normals = new float[18];
        enablePLayerInteract = false;
        handle.copyucharArray(tex, texture);
        texResolution = new ivec2(tx, ty);
        initMesh(fshader, vshader);
    }
    public void Draw(Engine handle){
        rotMat[0].buildxrotmat(meshRot.x);
        rotMat[1].buildyrotmat(meshRot.y);
        rotMat[2].buildzrotmat(meshRot.z);
        scaleMat.buildscaleMat(meshScale);
        CalcAABB();
        if(!handle.shadowpass){
            if(handle.enableColision && enablePLayerInteract){
                isinteracting = handle.aabbPlayer(meshPosition, aabb, colision);
            }else{
                isinteracting = false;
            }
            GLES32.glUseProgram(program);

            GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, albedoHandle[0]);
            GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "tex1"), 0);

            GLES32.glActiveTexture(GLES32.GL_TEXTURE1);
            GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, specularHandle[0]);
            GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "spec1"), 1);

            for(int i = 2; i != 12; i++){
                String uniname = "shadowMap"+(i-2);
                GLES32.glActiveTexture(GLES32.GL_TEXTURE0+i);
                GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, handle.shadowimg[i-2]);
                GLES32.glUniform1i(GLES32.glGetUniformLocation(program, uniname), i);
            }

            GLES32.glUniform3fv(GLES32.glGetUniformLocation(program, "lightsPos"), 10, handle.lightPositions, 0);
            GLES32.glUniform3fv(GLES32.glGetUniformLocation(program, "lightsCol"), 10, handle.lightColors, 0);
            GLES32.glUniform3f(GLES32.glGetUniformLocation(program, "viewPos"), handle.pos.x, handle.pos.y, handle.pos.z);


            meshMatrix.buildtranslatemat(meshPosition);

            positionHandle = GLES32.glGetAttribLocation(program, "positions");
            GLES32.glEnableVertexAttribArray(positionHandle);
            GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexbuf);

            normalHandle = GLES32.glGetAttribLocation(program, "normals");
            GLES32.glEnableVertexAttribArray(normalHandle);
            GLES32.glVertexAttribPointer(normalHandle, 3, GLES32.GL_FLOAT, false, 0, normalbuf);

            uvHandle = GLES32.glGetAttribLocation(program, "uv");
            GLES32.glEnableVertexAttribArray(uvHandle);
            GLES32.glVertexAttribPointer(uvHandle, 2, GLES32.GL_FLOAT, false, 0, uvbuf);

            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "proj"), 1, false, handle.perspective.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "uiproj"), 1, false, handle.uiMat.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "translate"), 1, false, handle.translate.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "xrot"), 1, false, handle.xrot.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "yrot"), 1, false, handle.yrot.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "meshm"), 1, false, meshMatrix.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "meshx"), 1, false, rotMat[0].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "meshy"), 1, false, rotMat[1].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "meshz"), 1, false, rotMat[2].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "meshs"), 1, false, scaleMat.mat,  0);
            GLES32.glUniform1iv(GLES32.glGetUniformLocation(program, "lightStates"), 10, handle.usedLights, 0);

            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "sproj"), 10, false, handle.shadowProj.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "stranslate"), 10, false, handle.shadowTrans.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "sxrot"), 10, false, handle.shadowxrot.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "syrot"), 10, false, handle.shadowyrot.mat,  0);
        }else{
            meshMatrix.buildtranslatemat(meshPosition);

            positionHandle = GLES32.glGetAttribLocation(handle.sprogram, "positions");
            GLES32.glEnableVertexAttribArray(positionHandle);
            GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexbuf);

            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "meshm"), 1, false, meshMatrix.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "meshx"), 1, false, rotMat[0].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "meshy"), 1, false, rotMat[1].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "meshz"), 1, false, rotMat[2].mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "meshs"), 1, false, scaleMat.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "sproj"), 10, false, handle.shadowProj.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "stranslate"), 10, false, handle.shadowTrans.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "sxrot"), 10, false, handle.shadowxrot.mat,  0);
            GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(handle.sprogram, "syrot"), 10, false, handle.shadowyrot.mat,  0);
        }

        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexes.length/3);
        GLES32.glDisableVertexAttribArray(positionHandle);
        GLES32.glDisableVertexAttribArray(normalHandle);
        GLES32.glDisableVertexAttribArray(uvHandle);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
    }
}
