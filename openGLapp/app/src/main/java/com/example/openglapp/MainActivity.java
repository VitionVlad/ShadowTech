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

import com.example.openglapp.cube.cube_model;
import com.example.openglapp.cube.cube_normals;
import com.example.openglapp.cube.cube_texture;
import com.example.openglapp.cube.cube_uv;

class render implements GLSurfaceView.Renderer {

    Mesh triangle = new Mesh();

    private final String vertexShaderCode =
                    "attribute vec3 positions;" +
                     "attribute vec3 normals;" +
                     "attribute vec2 uv;" +
                    "uniform mat4 proj;" +
                    "uniform mat4 translate;" +
                    "uniform mat4 xrot;" +
                    "uniform mat4 yrot;" +
                    "uniform mat4 meshm;" +
                    "varying vec2 fuv;"+
                    "varying vec3 fnormals;"+
                    "void main() {" +
                    "  gl_Position = proj * xrot * yrot * translate * meshm * vec4(positions, 1.0f);" +
                            "fuv = uv;"+
                            "fnormals = normals;"+
                    "}";

    private final String fragmentShaderCode =
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "varying vec2 fuv;"+
                    "varying vec3 fnormals;"+
                    "void main() {" +
                    "  gl_FragColor = texture2D(tex1, fuv).rgba;" +
                    "}";

    Engine eng = new Engine();

    ivec2 screenres = new ivec2();

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        eng.Init();
        triangle.vertexes = new cube_model().verts;
        triangle.normals = new cube_normals().verts;
        triangle.uv = new cube_uv().verts;
        triangle.texResolution = new cube_texture().res;
        triangle.texture = new cube_texture().pixels;
        triangle.meshPosition.z = -1.5f;
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