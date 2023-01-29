#include <iostream>

#include <AL/alut.h>

class audioSource{
    public:
    ALuint buffer;
    ALuint source;
    void init(const char *path){
        buffer = alutCreateBufferFromFile(path);
        alGenSources(1, &source);
        alSourcei(source, AL_BUFFER, buffer);
    }
    audioSource(const char *path){
        init(path);
    }
    void play(){
        alSourcePlay(source);
    }
    void stop(){
        alSourceStop(source);
    }
};