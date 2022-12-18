package com.example.openglapp;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mesh {
    private FloatBuffer vertexbuf;
    private FloatBuffer normalbuf;
    private FloatBuffer uvbuf;
    public float[] vertexes;
    public float[] normals;
    public float[] uv;
    private int program;
    private int positionHandle;
    private int normalHandle;
    private int uvHandle;

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
    }
    public void Draw(Engine handle){
        GLES32.glUseProgram(program);

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
