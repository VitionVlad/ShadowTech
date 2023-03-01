const fshader = `#version 300 es
precision mediump float;
layout (location = 0) out vec4 color;
in vec2 xy;
in vec3 norm;
in float dep;
uniform sampler2D albedo;
uniform sampler2D specular;
uniform sampler2D shadow;
uniform vec3 lightp[5];
uniform vec3 lightc[5];
uniform vec3 ppos;
in vec3 posit;

const float constant = 1.0;
const float linear = 0.09;
const float quadratic = 0.032;

in vec4 str;

float shadowMapping(){
  vec3 projected = str.xyz / str.w;
  float fshadow = 0.0f;
  if(projected.z <= 1.0f){ 
   projected.xy = (projected.xy + 1.0f)/2.0f; 
   float closestDepth = texture(shadow, projected.xy).r; 
   float currentDepth = projected.z; 
   if(currentDepth - 0.005 > closestDepth){ 
    fshadow+=1.0f;
   } 
  } 
  return fshadow; 
} 

void main(){
    vec3 finalcolor = vec3(0);
    vec3 normal = normalize(norm);
    for(int i = 0; i!=5; i++){
        float ambientStrength = 0.1;
        vec3 ambient = ambientStrength * lightc[i];

        vec3 lightDir = normalize(lightp[i] - posit);
        float diff = max(dot(normal, lightDir), 0.0);
        vec3 diffuse = diff * lightc[i];

        float specularStrength = texture(specular, xy).r;
        vec3 viewDir = normalize(vec3(-ppos.x, -ppos.y, -ppos.z) - posit);
        vec3 reflectDir = reflect(-lightDir, normal);  
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
        vec3 specu = specularStrength * spec * lightc[i];  

        float distance    = length(lightp[i] - posit);
        float attenuation = 1.0 / (constant + linear * distance + quadratic * (distance * distance)); 
        ambient  *= attenuation; 
        diffuse  *= attenuation;
        specu *= attenuation;     

        finalcolor += ((diffuse + specu)*(1.0-shadowMapping())+ambient) * texture(albedo, xy).rgb;
    }
    color = vec4(finalcolor, 1);
}
`;

const vshader = `#version 300 es
in vec3 positions;
in vec3 normals;
in vec2 uv;
uniform mat4 proj;
uniform mat4 trans;
uniform mat4 rotx;
uniform mat4 roty;

uniform mat4 mtrans;
uniform mat4 mrotx;
uniform mat4 mroty;
uniform mat4 mrotz;
uniform mat4 mscale;

uniform mat4 sproj;
uniform mat4 strans;
uniform mat4 srotx;
uniform mat4 sroty;

out vec2 xy;
out vec3 norm;
out float dep;
out vec3 posit;
out vec4 str;
void main(){
    vec4 fin = mscale * vec4(positions, 1.0);
    fin = mtrans * mroty * mrotx * mrotz * fin;
    fin = proj * roty * rotx * trans * fin;
    gl_Position = fin;
    fin = mscale * vec4(positions, 1.0);
    fin = mtrans * mroty * mrotx * mrotz * fin;
    fin = sproj * sroty * srotx * strans * fin;
    str = fin;
    dep = fin.z;
    xy = uv;
    norm = normals;
    posit = positions;
}
`;

var locked = false;

function main(){
    document.body.style.cursor = 'none';
    var speed = 0.001;
    var canvas;
    var gl;
    var eng = new Engine(gl, canvas);
    eng.pos.z = -1.0;
    eng.pos.y = -1.7;
    eng.rot.x = 0.0;
    eng.rot.y = 0.0;
    eng.sfov = 110;
    eng.shadowpos.z = -1.0;
    eng.shadowpos.y = -1.7;
    eng.setLight(0, new vec3(0, 2, 0), new vec3(1, 1, 1));
    var mesh = new Mesh(susv, susn, susu, fshader, vshader, eng, tex, tex, texx, texy);
    function key_callback(){
        document.addEventListener('keydown', function(event) {
            if (event.key == "w") {
                eng.pos.z += Math.cos(eng.rot.y) * Math.cos(eng.rot.x) * speed;
                eng.pos.x -= Math.cos(eng.rot.y) * Math.sin(eng.rot.x) * speed;
            }
            if (event.key == "a") {
                eng.pos.x += Math.cos(eng.rot.y) * Math.cos(eng.rot.x) * speed;
                eng.pos.z += Math.cos(eng.rot.y) * Math.sin(eng.rot.x) * speed;
            }
            if (event.key == "s") {
                eng.pos.z -= Math.cos(eng.rot.y) * Math.cos(eng.rot.x) * speed;
                eng.pos.x += Math.cos(eng.rot.y) * Math.sin(eng.rot.x) * speed;
            }
            if (event.key == "d") {
                eng.pos.x -= Math.cos(eng.rot.y) * Math.cos(eng.rot.x) * speed;
                eng.pos.z -= Math.cos(eng.rot.y) * Math.sin(eng.rot.x) * speed;
            }
            if (event.key == "q") {
                eng.pos.y += speed;
            }
            if (event.key == "e") {
                eng.pos.y -= speed;
            }
        }, true);
    }
    function mousecallback(){
        document.addEventListener("mousemove", function(event){
            eng.rot.x += ((event.movementX) / (eng.gl.canvas.width/2))/200;
            eng.rot.y += ((event.movementY) / (eng.gl.canvas.height/2))/200;
            if(eng.rot.y > 1.5){
                eng.rot.y = 1.5;
            }
            if(eng.rot.y < -1.5){
                eng.rot.y = -1.5;
            }
        }, false);     
        document.getElementById("glCanvas").onclick = function(){
            document.getElementById("glCanvas").requestPointerLock();
            document.getElementById("glCanvas").requestFullscreen();
        };
    }
    drawFrame();
    function drawFrame(){
        eng.beginShadowPass();
        mesh.Draw(eng);
        eng.beginFrame();
        mousecallback();
        key_callback();
        
        mesh.Draw(eng);

        eng.endFrame(drawFrame);
    }
}

window.onload = main;