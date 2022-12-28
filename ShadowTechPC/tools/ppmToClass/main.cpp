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
    myfile.open(name+"_texture.hpp");
    myfile << "class " << name << "_texture {" << endl;
    myfile << "public:" << endl;
    myfile << "int resx = " << resolutionx << ";" << endl;
    myfile << "int resy = " << resolutiony << ";" << endl;
    myfile << "unsigned char pixels [65535] = {" << endl;
    for(int i = 0; i != resolutionx*resolutiony*4; i+=4){
        myfile << pixels[i] << "," << pixels[i+1] << "," << pixels[i+2] << "," << pixels[i+3] << "," << endl;
    }
	myfile << "};" << endl << "};";
	myfile.close();
    return 1;
}