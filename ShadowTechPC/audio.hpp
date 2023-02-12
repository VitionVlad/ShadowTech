#include <iostream>

#include <SFML/Audio.hpp>

class audioSource{
    public:
    sf::SoundBuffer buffer;
    sf::Sound sound;
    void init(const char *path){
        buffer.loadFromFile(path);
        sound.setBuffer(buffer);
    }
    audioSource(const char *path){
        init(path);
    }
    void play(){
        sound.play();
    }
    void stop(){
        sound.stop();
    }
};