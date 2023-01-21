package com.smoke.shadowtechandroid;

public class clickZone {
    public vec2 corner1 = new vec2(0, 0);
    public vec2 corner2 = new vec2(1, 1);
    public clickZone(vec2 v1, vec2 v2){
        corner1 = v1;
        corner2 = v2;
    }
    public clickZone(){}
    public boolean update(ivec2 screenResolution, vec2 pointerCoord, boolean isClicking){
        vec2 pc = new vec2(pointerCoord.x / screenResolution.x, pointerCoord.y / screenResolution.y);
        return pc.x >= corner1.x && pc.y >= corner1.y && pc.x <= corner2.x && pc.y <= corner2.y && isClicking == true;
    }
}
