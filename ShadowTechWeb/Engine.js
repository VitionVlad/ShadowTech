
class vec2{
    constructor(x, y){
        this.x  = x;
        this.y = y;
    }
}

class vec3{
    constructor(x, y, z){
        this.x  = x;
        this.y = y;
        this.z = z;
    }
}

class mat4{
    constructor(){
        this.mat = new Float32Array([0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0]);
    }
    buildtranslatemat(pos){
        this.mat[0] = 1;
        this.mat[5] = 1;
        this.mat[10] = 1;
        this.mat[15] = 1;
        this.mat[12] = pos.x;
        this.mat[13] = pos.y;
        this.mat[14] = pos.z;
    }
    buildxrotmat(angle){
        this.mat[0] = 1;
        this.mat[5] = Math.cos(angle);
        this.mat[6] = -Math.sin(angle);
        this.mat[9] = Math.sin(angle);
        this.mat[10] = Math.cos(angle);
        this.mat[15] = 1;
    }
    buildyrotmat(angle){
        this.mat[0] = Math.cos(angle);
        this.mat[5] = 1.0;
        this.mat[2] = Math.sin(angle);
        this.mat[8] = -Math.sin(angle);
        this.mat[10] = Math.cos(angle);
        this.mat[15] = 1;
    }
    buildzrotmat(angle){
        this.mat[0] = Math.cos(angle);
        this.mat[5] = Math.cos(angle);
        this.mat[4] = Math.sin(angle);
        this.mat[1] = -Math.sin(angle);
        this.mat[10] = 1;
        this.mat[15] = 1;
    }
    buildperspectivemat(fov, zNear, zFar, aspect){
        var S = Math.tan((fov/2)*(3.1415/180));
        this.mat[0] = 1/(aspect*S);
        this.mat[5] = 1/S;
        this.mat[10] = -zFar/(zFar-zNear);
        this.mat[11] = -1;
        this.mat[14] = -zFar*zNear/(zFar-zNear);
    }
    buildorthomat(r, l, t, b, zNear, zFar){
        this.mat[0] = 2/(r-l);
        this.mat[5] = 2/(r-l);
        this.mat[10] = -2/(zFar-zNear);
        this.mat[15] = 1;
        this.mat[3] = (r+r)/(r-l);
        this.mat[7] = (t+b)/(t-b);
        this.mat[11] = (zFar+zNear)/(zFar-zNear);
    }
    buildIdentityMat(){
        this.mat[0] = 1;
        this.mat[5] = 1;
        this.mat[10] = 1;
        this.mat[15] = 1;
    }
    buildScaleMat(scale){
        this.mat[0] = scale.x;
        this.mat[5] = scale.y;
        this.mat[10] = scale.z;
        this.mat[15] = 1;
    }
    clearmat(){
        for(var i = 0; i != 16; i++){
            this.mat[i] = 0.0;
        }
    }
}

class Engine{
    loadShader(gl, type, source) {
        const shader = gl.createShader(type);
        gl.shaderSource(shader, source);
        gl.compileShader(shader);
        if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
            alert('An error occurred compiling the shaders: ' + gl.getShaderInfoLog(shader));
            gl.deleteShader(shader);
            return null;
        }
        return shader;
    }
    initShaderProgram(vsSource, fsSource) {
        const vertexShader = this.loadShader(this.gl, this.gl.VERTEX_SHADER, vsSource);
        const fragmentShader = this.loadShader(this.gl, this.gl.FRAGMENT_SHADER, fsSource);
      
        const shaderProgram = this.gl.createProgram();
        this.gl.attachShader(shaderProgram, vertexShader);
        this.gl.attachShader(shaderProgram, fragmentShader);
        this.gl.linkProgram(shaderProgram);
        if (!this.gl.getProgramParameter(shaderProgram, this.gl.LINK_STATUS)) {
            alert('Unable to initialize the shader program: ' + this.gl.getProgramInfoLog(shaderProgram));
            return null;
        }
        return shaderProgram;
    }
    constructor(gl, canvas){
        canvas = document.querySelector("#glCanvas");
        gl = canvas.getContext("webgl2");
        gl.viewport(0, 0, gl.canvas.width, gl.canvas.height);
        gl.clearColor(0.0, 0.0, 0.0, 1.0);
        gl.enable(gl.DEPTH_TEST);
        gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
        this.gl = gl;
        this.fov = 120;
        this.pos = new vec3(0.0, 0.0, 0.0);
        this.rot = new vec2(0.0, 0.0);
    }
    beginFrame(){
        this.gl.viewport(0, 0, this.gl.canvas.width, this.gl.canvas.height);
        this.gl.clear(this.gl.COLOR_BUFFER_BIT | this.gl.DEPTH_BUFFER_BIT);
        this.gl.clearColor(0.0, 0.0, 0.0, 1.0);
    }
}

class Mesh{
    constructor(geometry, fshader, vshader, engineh){
        this.vBuf = engineh.gl.createBuffer();
        engineh.gl.bindBuffer(engineh.gl.ARRAY_BUFFER, this.vBuf);
        engineh.gl.bufferData(engineh.gl.ARRAY_BUFFER, geometry, engineh.gl.STATIC_DRAW);
        this.meshMat = new mat4();
        this.shaderprog = engineh.initShaderProgram(vshader, fshader);
        this.positionLoc = engineh.gl.getAttribLocation(this.shaderprog, "positions");
        this.totalv = geometry.length/3;
        this.pos = new vec3(0.0, 0.0, 0.0);
        this.rot = new vec3(0.0, 0.0, 0.0);
    }
    Draw(engineh){
        engineh.gl.enableVertexAttribArray(this.positionLoc);
        engineh.gl.useProgram(this.shaderprog);
        var numComponents = 3; 
        var type = engineh.gl.FLOAT;   
        var normalize = false; 
        var offset = 0;        
        var stride = 0; 
        engineh.gl.vertexAttribPointer(this.positionLoc, numComponents, type, normalize, offset, stride);

        this.meshMat.clearmat();
        this.meshMat.buildperspectivemat(engineh.fov, 0.1, 100.0, engineh.gl.canvas.width/engineh.gl.canvas.height);
        engineh.gl.uniformMatrix4fv(engineh.gl.getUniformLocation(this.shaderprog, "proj"), false, this.meshMat.mat);

        this.meshMat.clearmat();
        this.meshMat.buildtranslatemat(engineh.pos);
        engineh.gl.uniformMatrix4fv(engineh.gl.getUniformLocation(this.shaderprog, "trans"), false, this.meshMat.mat);

        this.meshMat.clearmat();
        this.meshMat.buildxrotmat(-engineh.rot.y);
        engineh.gl.uniformMatrix4fv(engineh.gl.getUniformLocation(this.shaderprog, "roty"), false, this.meshMat.mat);

        this.meshMat.clearmat();
        this.meshMat.buildyrotmat(-engineh.rot.x);
        engineh.gl.uniformMatrix4fv(engineh.gl.getUniformLocation(this.shaderprog, "rotx"), false, this.meshMat.mat);

        engineh.gl.drawArrays(engineh.gl.TRIANGLES, offset, this.totalv);
    }
}