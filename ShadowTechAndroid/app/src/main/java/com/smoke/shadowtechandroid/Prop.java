package com.smoke.shadowtechandroid;

public class Prop {
    public float mass = 0.1f;
    private vec3 lastPos = new vec3();
    private static boolean between(float x, float n1, float n2){
        return x >= n1 && x <= n2;
    }
    private void aabMesh(vec3 meshPos, vec3 meshBorder, vec3 omeshPos, vec3 omeshBorder){
        if(between(omeshPos.x, meshPos.x- meshBorder.x- omeshBorder.x, meshPos.x+meshBorder.x+ omeshBorder.x)&&between(omeshPos.y, meshPos.y - meshBorder.y, meshBorder.y+meshPos.y+ omeshBorder.y)&&between(omeshPos.z, meshPos.z-meshBorder.z- omeshBorder.z, meshBorder.z+meshPos.z+ omeshBorder.z)){
            omeshPos.y = lastPos.y;
            if(between(omeshPos.y, meshPos.y - meshBorder.y, meshBorder.y+meshPos.y+ omeshBorder.y/2)){
                omeshPos.x = lastPos.x;
                omeshPos.z = lastPos.z;
            }
        }
    }
    public void MeshMeshInteract(Mesh toInteract, Mesh interactWith){
        toInteract.meshPosition.y -= mass;
        aabMesh(interactWith.meshPosition, interactWith.aabb, toInteract.meshPosition, toInteract.aabb);
        lastPos.x = toInteract.meshPosition.x;
        lastPos.y = toInteract.meshPosition.y;
        lastPos.z = toInteract.meshPosition.z;
    }
}
