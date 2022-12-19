#include <iostream>

#include <fstream>

#include <string>

using namespace std;

int resolutionx, resolutiony;

void readImage(int* pixels, const char* path){
    fstream readimage;
    readimage.open(path);
    int i1, i2, i3;
    string trash;
    readimage >> trash;
    readimage >> resolutionx >> resolutiony;
    if(resolutionx * resolutiony * 4 > 65535){
        throw runtime_error("file too large!");
    }
    readimage >> i1;
    for(int i = 0; readimage >> i1 >> i2 >> i3; i+=4){
        pixels[i] = i1;
        pixels[i+1] = i2;
        pixels[i+2] = i3;
        pixels[i+3] = 255;
    }
    readimage.close();
}

int main(){
    int pixels[65535];
    string name;
    cout << "enter file name:";
    cin >> name;
    readImage(pixels, name.c_str());
    cout << "enter class name:";
    cin >> name;
    ofstream myfile;
    myfile.open(name+"_texture.java");
    myfile << "package com.example.openglapp;" << endl << endl;
    myfile << "public class " << name << "_texture {" << endl;
    myfile << "public ivec2 res = new ivec2(" << resolutionx << ", " << resolutiony << ");" << endl;
    myfile << "public byte[] pixels = {" << endl;
    cout << "package com.example.openglapp;" << endl << endl;
    cout << "public class model {" << endl;
    cout << "public byte[] pixels = {" << endl;
    for(int i = 0; i != resolutionx*resolutiony*4; i+=4){
        myfile << "(byte) " << pixels[i] << "," << "(byte) " << pixels[i+1] << "," << "(byte) " << pixels[i+2] << "," << "(byte) " << pixels[i+3] << "," << endl;
        cout << "(byte) " << pixels[i] << "," << "(byte) " << pixels[i+1] << "," << "(byte) " << pixels[i+2] << "," << "(byte) " << pixels[i+3] << "," << endl;
    }
	myfile << "};" << endl << "}";
    cout << "};" << endl << "}";
	myfile.close();
    return 1;
}
