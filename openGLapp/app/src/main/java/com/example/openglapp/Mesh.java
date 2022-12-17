package com.example.openglapp;

import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Mesh {
    private FloatBuffer vertexbuf;
    public float[] vertexes;
    private int program;
    private int positionHandle;


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
    }
    public void Draw(Engine handle){
        GLES32.glUseProgram(program);
        positionHandle = GLES32.glGetAttribLocation(program, "positions");
        GLES32.glEnableVertexAttribArray(positionHandle);
        GLES32.glVertexAttribPointer(positionHandle, 3, GLES32.GL_FLOAT, false, 0, vertexbuf);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "proj"), 1, false, handle.perspective.mat,  0);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "translate"), 1, false, handle.translate.mat,  0);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "xrot"), 1, false, handle.xrot.mat,  0);
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(program, "yrot"), 1, false, handle.yrot.mat,  0);
        GLES32.glDrawArrays(GLES32.GL_TRIANGLES, 0, vertexes.length/3);
        GLES32.glDisableVertexAttribArray(positionHandle);
    }
}
