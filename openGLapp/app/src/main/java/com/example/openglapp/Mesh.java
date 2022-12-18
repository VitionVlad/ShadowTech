package com.example.openglapp;

import android.opengl.GLES32;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Mesh {
    private FloatBuffer vertexbuf;
    private FloatBuffer normalbuf;
    private FloatBuffer uvbuf;
    public float[] vertexes;
    public float[] normals;
    public float[] uv;
    public byte[] texture;
    public ivec2 texResolution;
    private int program;
    private int positionHandle;
    private int normalHandle;
    private int uvHandle;
    private int[] albedoHandle = new int[1];

    public void initMesh(String fshader, String vshader){
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
        GLES32.glGenTextures(0, IntBuffer.wrap(albedoHandle));
        GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA, texResolution.x ,texResolution.y, 0, GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, buffer);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);

        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_MIRRORED_REPEAT);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_MIRRORED_REPEAT);
        float[] borderColor = { 0.0f, 0.0f, 0.0f, 1.0f };
        GLES32.glTexParameterfv(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_BORDER_COLOR, FloatBuffer.wrap(borderColor));
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_NEAREST);
        GLES32.glTexParameteri(GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR);
    }
    public void Draw(Engine handle){
        GLES32.glUseProgram(program);
        GLES32.glUniform1i(GLES32.glGetUniformLocation(program, "tex1"), 0);
        GLES32.glActiveTexture(GLES32.GL_TEXTURE0);
        GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, albedoHandle[0]);

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
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "translate"), 1, false, handle.translate.mat,  0);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "xrot"), 1, false, handle.xrot.mat,  0);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "yrot"), 1, false, handle.yrot.mat,  0);

        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexes.length/3);
        GLES32.glDisableVertexAttribArray(positionHandle);
        GLES32.glDisableVertexAttribArray(normalHandle);
        GLES32.glDisableVertexAttribArray(uvHandle);
    }
}
