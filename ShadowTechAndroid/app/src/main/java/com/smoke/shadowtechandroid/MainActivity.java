package com.smoke.shadowtechandroid;

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

import com.smoke.shadowtechandroid.cube.*;

import com.smoke.shadowtechandroid.plane.*;

import com.smoke.shadowtechandroid.monitor.*;

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
                            "uniform mat4 meshx;" +
                            "uniform mat4 meshy;"+
                            "uniform mat4 meshz;"+
                            "uniform mat4 meshs;"+

                            "uniform mat4 sproj[10];" +
                            "uniform mat4 stranslate[10];" +
                            "uniform mat4 sxrot[10];" +
                            "uniform mat4 syrot[10];" +

                    "out vec2 fuv;"+
                    "out vec3 fnormals;"+
                    "out vec3 fpos;"+
                    "out vec4 projlightmat;"+

                    "void main() {" +
                            "  vec4 tr = meshs * vec4(positions, 1.0f);" +
                    "  tr = meshm * meshx * meshy * meshz * tr;"+
                            "  gl_Position = proj * xrot * yrot * translate * tr;"+
                            "fuv = uv;"+
                            "fnormals = mat3(transpose(inverse(mat4(1.0f)))) * normals;"+
                            "fpos = vec3(mat4(1.0f) * vec4(positions, 1.0f));"+
                            "projlightmat = sproj[0] * sxrot[0] * syrot[0] * stranslate[0] * tr;"+
                    "}";

    private final String fragmentShaderCode =
                    "#version 320 es\n" +
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "uniform sampler2D spec1;"+
                    "uniform sampler2D shadowMap0;"+
                    "uniform vec3 lightsPos[10];"+
                    "uniform vec3 lightsCol[10];"+
                    "uniform int lightStates[10];"+
                    "uniform vec3 viewPos;"+
                    "in vec4 projlightmat;"+
                    "in vec2 fuv;"+
                    "in vec3 fnormals;"+
                    "in vec3 fpos;"+
                    "layout(location = 0) out vec4 color;"+
                    "float shadowMapping(){" +
                    "  vec3 projected = projlightmat.xyz / projlightmat.w;" +
                    "  float shadow = 0.0f;" +
                    "  if(projected.z <= 1.0f){" +
                    "   projected = (projected + 1.0f)/2.0f;" +
                    "   float closestDepth = texture(shadowMap0, projected.xy).r;" +
                    "   float currentDepth = projected.z;" +
                    "   if(currentDepth - 0.001 > closestDepth){" +
                    "       shadow+=1.0f;" +
                    "   }" +
                    "  }" +
                    "  return shadow;" +
                    "}" +
                    "float phongl(vec3 lightpos){" +
                    "  float ambient = 0.2;" +
                    "  vec3 norm = normalize(fnormals);" +
                    "  vec3 ldir = normalize(lightpos-fpos);" +
                    "  float diffuse = max(dot(norm, ldir), 0.0);" +
                    "  vec3 viewDir = normalize(-viewPos - fpos);" +
                    "  vec3 halfwayDir = normalize(ldir + viewDir);" +
                    "  float spec = pow(max(dot(norm, halfwayDir), 0.0), 16.0) * texture(spec1, fuv).r;" +
                    "  return float(spec + diffuse)*((1.0-shadowMapping()) + ambient);" +
                    "}" +
                    "void main() {" +
                    "  vec3 toOut;" +
                            "  for(int i = 0; i!= 10; i++){" +
                            "  if(lightStates[i]==1){" +
                            "  toOut += phongl(lightsPos[i]) * lightsCol[i] * texture(tex1, fuv).rgb;" +
                            "  }" +
                            "  }" +
                    "  color = vec4( toOut, 1.0);" +
                    "}";

    private final String fragmentShaderCode2 =
            "#version 320 es\n" +
                    "precision mediump float;" +
                    "uniform sampler2D tex1;"+
                    "uniform sampler2D spec1;"+
                    "uniform sampler2D shadowMap0;"+
                    "uniform vec3 lightsPos[10];"+
                    "in vec4 projlightmat;"+
                    "in vec2 fuv;"+
                    "in vec3 fnormals;"+
                    "layout(location = 0) out vec4 color;"+
                    "void main() {" +
                    "  color = vec4(texture(shadowMap0, fuv).rrr, 1.0);" +
                    "}";

    Engine eng = new Engine();

    ivec2 screenres = new ivec2();

    Prop triangleProp = new Prop();

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        eng.Init();
        eng.shadowProj.buildperspectivemat(90, 0.1f, 100, 1, 0);
        eng.shadowTrans.buildtranslatemat(new vec3(0, 0, -1), 0);
        eng.shadowxrot.buildxrotmat(-0.2f, 0);
        eng.shadowyrot.buildyrotmat(0, 0);
        eng.setLight(0, new vec3(0, 1, 2f), new vec3(1, 1, 0.5f), 1);

        triangle.vertexes = new cube_model().verts;
        triangle.normals = new cube_normals().verts;
        triangle.uv = new cube_uv().verts;
        triangle.texResolution = new cube_texture().res;
        triangle.texture = new cube_texture().pixels;
        triangle.specular = new specular_texture().pixels;
        triangle.meshPosition.z = -1.5f;
        triangle.meshPosition.y = 5;
        //triangle.meshRot.x = 0.5f;
        //triangle.meshScale.y = 1.5f;
        triangle.initMesh(fragmentShaderCode, vertexShaderCode);

        plane.vertexes = new plane_model().verts;
        plane.normals = new plane_normals().verts;
        plane.uv = new plane_uv().verts;
        plane.texResolution = new cube_texture().res;
        plane.texture = new cube_texture().pixels;
        plane.specular = new specular_texture().pixels;
        plane.meshPosition.y = -0.5f;
        plane.initMesh(fragmentShaderCode, vertexShaderCode);

        monitor.vertexes = new monitor_model().verts;
        monitor.normals = new monitor_normals().verts;
        monitor.uv = new monitor_uv().verts;
        monitor.texResolution = new cube_texture().res;
        monitor.texture = new cube_texture().pixels;
        monitor.specular = new specular_texture().pixels;
        monitor.meshPosition = new vec3(-1.5f, 0.5f, 1.5f);
        monitor.initMesh(fragmentShaderCode2, vertexShaderCode);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        screenres.x = i;
        screenres.y = i1;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        eng.beginShadowPass(0);

        triangle.Draw(eng);
        plane.Draw(eng);
        monitor.Draw(eng);

        eng.beginMainPass(screenres);

        triangleProp.MeshMeshInteract(triangle, plane);
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