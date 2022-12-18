#include <iostream>

#include <cstring>

#include <string>

#include <stdio.h>

#include <fstream>

#include <vector>

using namespace std;

class vec3{
    public:
    float x = 0;
    float y = 0;
    float z = 0;
};

class vec2{
    public:
    float x = 0;
    float y = 0;
};

class mesh{
    public:
    vector<vec3> vert;
    vector<vec3> normals;
    vector<vec2> uv;
};

void loadobj(const char* path, mesh& toReturn){
	vector<vec3> lvertex;
	vector<vec3> lnormals;
	vector<vec2> luv;
	vector<int> indices;
	vector<int> nindices;
	vector<int> uindices;
    FILE* obj;
	//fopen(obj, path, "r");
    obj = fopen(path, "r");
	string arg;
	int line = 1;
	int nline = 1;
	int uvline = 1;
	int faceline = 0;
	while (1) {
		char lineHeader[128];

		if (obj == 0) {
			exit(-1);
		}

		int res = fscanf(obj, "%s", lineHeader);
		if (res == EOF) {
			break;
		}

		if (strcmp(lineHeader, "#") == 0) {
			fscanf(obj, "%*[^\n]\n");
		}

		if (strcmp(lineHeader, "s") == 0) {
			fscanf(obj, "%*[^\n]\n");
		}

		if (strcmp(lineHeader, "o") == 0) {
			fscanf(obj, "%*[^\n]\n");
		}

		if (strcmp(lineHeader, "v") == 0) {
			lvertex.resize(lvertex.size()+6);
			fscanf(obj, "%f %f %f \n", &lvertex[line].x, &lvertex[line].y, &lvertex[line].z);
            cout << "vertex = " << lvertex[line].x << " " << lvertex[line].y << " " << lvertex[line].z << endl;
			line++;
		}

		if (strcmp(lineHeader, "vn") == 0) {
			lnormals.resize(lnormals.size()+6);
			fscanf(obj, "%f %f %f \n", &lnormals[nline].x, &lnormals[nline].y, &lnormals[nline].z);
            cout << "normals = " << lnormals[nline].x << " " << lnormals[nline].y << " " << lnormals[nline].z << endl;
			nline++;
		}

		if (strcmp(lineHeader, "vt") == 0) {
			luv.resize(luv.size()+4);
			fscanf(obj, "%f %f \n", &luv[uvline].x, &luv[uvline].y);
            cout << "uv = " << luv[uvline].x << " " << luv[uvline].y << endl;
			uvline++;
		}

		if (strcmp(lineHeader, "f") == 0) {
			indices.resize(indices.size()+3);
			uindices.resize(uindices.size()+3);
			nindices.resize(nindices.size()+3);
			fscanf(obj, "%d/%d/%d %d/%d/%d %d/%d/%d \n", &indices[faceline], &uindices[faceline], &nindices[faceline], &indices[faceline+1], &uindices[faceline+1], &nindices[faceline+1], &indices[faceline+2], &uindices[faceline+2], &nindices[faceline+2]);
			faceline = faceline + 3;
		}
	}
	fclose(obj);
    cout << "resizing..." << endl;
    toReturn.vert.resize(faceline);
    toReturn.normals.resize(faceline);
    toReturn.uv.resize(faceline);
    cout << "resized!" << endl;
    for(int i = 0; i != faceline; i++){
        toReturn.vert[i].x = lvertex[indices[i]].x;
		toReturn.vert[i].y = lvertex[indices[i]].y;
		toReturn.vert[i].z = lvertex[indices[i]].z;

        toReturn.normals[i].x = lnormals[nindices[i]].x;
		toReturn.normals[i].y = lnormals[nindices[i]].y;
		toReturn.normals[i].z = lnormals[nindices[i]].z;

        toReturn.uv[i].x = luv[uindices[i]].x;
		toReturn.uv[i].y = luv[uindices[i]].y;
    }
}

int main(){
    mesh Finale;
    loadobj("test.obj", Finale);
    ofstream myfile;
    myfile.open("class.java");\
    myfile << "package com.example.openglapp;" << endl << endl;
    myfile << "public class model {" << endl;
    myfile << "float[] verts = {" << endl;
    cout << "package com.example.openglapp;" << endl << endl;
    cout << "public class model {" << endl;
    cout << "float[] verts = {" << endl;
    for(int i = 0; i != Finale.vert.size(); i++){
        myfile << Finale.vert[i].x << "f, " << Finale.vert[i].y << "f, " << Finale.vert[i].z << "f, " << endl;
        cout << Finale.vert[i].x << "f, " << Finale.vert[i].y << "f, " << Finale.vert[i].z << "f, " << endl;
    }
    myfile << "};" << endl;
    cout << "};" << endl;
	myfile << "float[] normals = {" << endl;
    cout << "float[] normals = {" << endl;
    for(int i = 0; i != Finale.vert.size(); i++){
        myfile << Finale.normals[i].x << "f, " << Finale.normals[i].y << "f, " << Finale.normals[i].z << "f, " << endl;
        cout << Finale.normals[i].x << "f, " << Finale.normals[i].y << "f, " << Finale.normals[i].z << "f, " << endl;
    }
    myfile << "};" << endl;
    cout << "};" << endl;
	myfile << "float[] uv = {" << endl;
    cout << "float[] uv = {" << endl;
    for(int i = 0; i != Finale.vert.size(); i++){
        myfile << Finale.uv[i].x << "f, " << Finale.uv[i].y << "f, " <<  endl;
        cout << Finale.uv[i].x << "f, " << Finale.uv[i].y << "f, " <<  endl;
    }
    myfile << "};" << endl << "}";
    cout << "};" << endl << "}";
    return 0;
}
