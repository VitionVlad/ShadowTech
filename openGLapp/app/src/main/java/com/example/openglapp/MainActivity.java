package com.example.openglapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.view.MotionEvent;

import com.example.openglapp.cube.*;

import com.example.openglapp.plane.*;

import com.example.openglapp.monitor.*;

class render implements GLSurfaceView.Renderer {

    Mesh triangle = new Mesh();

    Mesh plane = new Mesh();

    Mesh monitor = new Mesh();

    private final String vertexShaderCode =
                    "#version 320 es\n" +
                    "in vec3 positions;" +
                    "in vec3 normals;" +
                    "in vec2 uv;" +
                    "uniform mat4 proj;" +
                    "uniform mat4 translate;" +
                    "uniform mat4 xrot;" +
                    "uniform mat4 yrot;" +
                    "uniform mat4 meshm;" +
                    "out vec2 fuv;"+
                    "out vec3 fnormals;"+
                    "void main() {" +
                    "  gl_Position = proj * xrot * yrot * translate * meshm * vec4(positions, 1.0f);" +
                            "fuv = uv;"+
                            "fnormals = normals;"+
                    "}";

    private final String fragmentShaderCode =
                    "#version 320 es\n" +
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "uniform sampler2D spec1;"+
                    "uniform sampler2D shadowMap;"+
                    "uniform vec3 lightsPos[10];"+
                    "in vec2 fuv;"+
                    "in vec3 fnormals;"+
                    "layout(location = 0) out vec4 color;"+
                    "void main() {" +
                    "  color = vec4(texture(tex1, fuv).rgb, 1.0);" +
                    "}";

    private final String fragmentShaderCode2 =
            "#version 320 es\n" +
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "uniform sampler2D spec1;"+
                    "uniform sampler2D shadowMap;"+
                    "uniform vec3 lightsPos[10];"+
                    "in vec2 fuv;"+
                    "in vec3 fnormals;"+
                    "layout(location = 0) out vec4 color;"+
                    "void main() {" +
                    "  color = vec4(texture(shadowMap, vec2(-fuv.x, fuv.y)).rrr, 1.0);" +
                    "}";

    Engine eng = new Engine();

    ivec2 screenres = new ivec2();

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        eng.Init();
        eng.shadowProj.buildperspectivemat(90, 0.1f, 100, 1);
        eng.shadowTrans.buildtranslatemat(new vec3(0, 0, -1f));
        eng.shadowxrot.buildxrotmat(-0.2f);
        eng.shadowyrot.buildyrotmat(0);

        triangle.vertexes = new cube_model().verts;
        triangle.normals = new cube_normals().verts;
        triangle.uv = new cube_uv().verts;
        triangle.texResolution = new cube_texture().res;
        triangle.texture = new cube_texture().pixels;
        triangle.specular = new specular_texture().pixels;
        triangle.meshPosition.z = -1.5f;
        triangle.initMesh(fragmentShaderCode, vertexShaderCode, eng);

        plane.vertexes = new plane_model().verts;
        plane.normals = new plane_normals().verts;
        plane.uv = new plane_uv().verts;
        plane.texResolution = new cube_texture().res;
        plane.texture = new cube_texture().pixels;
        plane.specular = new specular_texture().pixels;
        plane.meshPosition.y = -0.5f;
        plane.initMesh(fragmentShaderCode, vertexShaderCode, eng);

        monitor.vertexes = new monitor_model().verts;
        monitor.normals = new monitor_normals().verts;
        monitor.uv = new monitor_uv().verts;
        monitor.texResolution = new cube_texture().res;
        monitor.texture = new cube_texture().pixels;
        monitor.specular = new specular_texture().pixels;
        monitor.meshPosition = new vec3(-1.5f, 0.5f, 1.5f);
        monitor.initMesh(fragmentShaderCode2, vertexShaderCode, eng);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        screenres.x = i;
        screenres.y = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        eng.beginShadowPass();

        triangle.Draw(eng);
        plane.Draw(eng);
        monitor.Draw(eng);

        eng.beginMainPass(screenres);

        triangle.Draw(eng);
        plane.Draw(eng);
        monitor.Draw(eng);

        eng.endFrame(screenres);
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