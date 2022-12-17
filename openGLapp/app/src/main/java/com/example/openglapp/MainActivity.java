package com.example.openglapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES32;
import android.view.MotionEvent;

class render implements GLSurfaceView.Renderer {

    Mesh triangle = new Mesh();

    float[] vertices =  {
            -1, -1, -1,
            0, 1, -1,
            1, -1, -1
    };

    private final String vertexShaderCode =
                    "attribute vec3 positions;" +
                    "uniform mat4 proj;" +
                    "uniform mat4 translate;" +
                    "uniform mat4 xrot;" +
                    "uniform mat4 yrot;" +
                    "void main() {" +
                    "  gl_Position = proj * xrot * yrot * translate * vec4(positions, 1.0f);" +
                    "}";

    private final String fragmentShaderCode =
                    "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(1.0f, 0.0f, 0.0f, 1.0f);" +
                    "}";

    Engine eng = new Engine();

    ivec2 screenres = new ivec2();

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        triangle.vertexes = vertices;
        triangle.initMesh(fragmentShaderCode, vertexShaderCode);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        screenres.x = i;
        screenres.y = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        eng.perFrame(screenres);
        triangle.Draw(eng);
    }
}

class surface extends GLSurfaceView {

    private final render renderer;

    private int mActivePointerId;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(e.getAction() == android.view.MotionEvent.ACTION_UP){
            renderer.eng.touchpos.x = 0;
            renderer.eng.touchpos.y = 0;
            renderer.eng.speed = 0;
            renderer.eng.allowmove = false;
        }else{
            renderer.eng.touchpos.x = e.getRawX();
            renderer.eng.touchpos.y = e.getRawY();
            renderer.eng.allowmove = true;
        }
        return true;
    }

    public surface(Context context) {
        super(context);
        setEGLContextClientVersion(2);

        renderer = new render();
        setRenderer(renderer);
    }
}

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        getSupportActionBar().hide();
        glView = new surface(this);
        setContentView(glView);
    }
}