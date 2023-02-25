const fshader = `#version 300 es
precision mediump float;
out vec4 color;
in vec2 xy;
in vec3 norm;
void main(){
    color = vec4(norm, 1);
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
out vec2 xy;
out vec3 norm;
void main(){
    gl_Position = proj * rotx * roty * trans * vec4(positions, 1.0);
    xy = uv;
    norm = normals;
}
`;

function main(){
    const speed = 0.001;
    var canvas;
    var gl;
    var eng = new Engine(gl, canvas);
    eng.pos.z = -1.0;
    eng.rot.x = 0.0;
    eng.rot.y = 0.0;
    var mesh = new Mesh(susv, susn, susu, fshader, vshader, eng);
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
            if (event.key == "ArrowUp") {
                eng.rot.y -= speed/10;
            }
            if (event.key == "ArrowDown") {
                eng.rot.y += speed/10;
            }
            if (event.key == "ArrowLeft") {
                eng.rot.x -= speed/10;
            }
            if (event.key == "ArrowRight") {
                eng.rot.x += speed/10;
            }
        }, true);
    }
    drawFrame();
    function drawFrame(){
        eng.beginFrame();
        key_callback();
        
        mesh.Draw(eng);

        eng.endFrame(drawFrame);
    }
}

window.onload = main;