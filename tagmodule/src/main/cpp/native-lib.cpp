#include <jni.h>
#include <string>
#include <vector>
#include "CoordConverter.h"

#define JNI_FUNCTION(name) Java_com_hiscene_armap_HiARVGISNative_##name

#ifdef __cplusplus
extern "C"
{
#endif

jstring JNICALL JNI_FUNCTION(stringFromJNI(JNIEnv * env, jobject /* this */)) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

jlong JNICALL JNI_FUNCTION(CoordConverterConstructor(JNIEnv * env, jobject /* this */)) {
    return (jlong) new CoordConverter();
}

void JNICALL JNI_FUNCTION(CoordConverterDestructor(JNIEnv * env, jobject /* this */, jlong owner)) {
    if(owner){
        CoordConverter *coor = (CoordConverter*)owner;
        delete coor;
    }
}


void JNICALL JNI_FUNCTION(setCameraIntrinsics(JNIEnv * env, jobject /* this */, jlong owner, double height, jdoubleArray intrinsics,jint intrinsic_count)) {
    jdoubleArray lonlat = env->NewDoubleArray(3);
    if(owner) {
        CoordConverter *coor = (CoordConverter *) owner;
        PointLonLat camera_lonlat;
        TagPosition tag_pos;
        jdouble *arr;
        arr = env->GetDoubleArrayElements(intrinsics, NULL);

        std::vector<HiARVGISCameraIntrinsics> intrinsic_list;
        for(int i=0;i<intrinsic_count;i++){
            HiARVGISCameraIntrinsics ins;
            int index = i*12;
            ins.width = arr[index++];
            ins.height = arr[index++];             /// number of rows in pixels of image for calibration

            ins.fx = arr[index++];              /// focal length x
            ins.fy = arr[index++];              /// focal length y
            ins.cx = arr[index++];              /// principal point x
            ins.cy = arr[index++];              /// principal point y

            ins.k1 = arr[index++];              /// first radial distortion coefficient
            ins.k2 = arr[index++];              /// second radial distortion coefficient
            ins.k3 = arr[index++];              /// third radial distortion coefficient
            ins.p1 = arr[index++];              /// first tangential distortion coefficient
            ins.p2 = arr[index++];              /// second tangential distortion coefficient

            ins.zoom = arr[index++];            /// scale factor
            intrinsic_list.push_back(ins);
        }
        coor->setCameraIntrinsics(0, intrinsic_list.data(), intrinsic_count);
    }
}

jdoubleArray JNICALL JNI_FUNCTION(screenPointTolonLat(JNIEnv * env, jobject /* this */, jlong owner, jdoubleArray cameraPTZ, jdoubleArray cameraLonLat, jdoubleArray screen)) {
    jdoubleArray tag_position = env->NewDoubleArray(10);
    if(owner){
        CoordConverter *coor = (CoordConverter*)owner;
        double ptz[3];
        PointLonLat camera_lonlat;
        TagPosition tag_pos;
        jdouble* arr;
        arr = env->GetDoubleArrayElements(cameraPTZ,NULL);
        ptz[0] = arr[0];
        ptz[1] = arr[1];
        ptz[2] = arr[2];

        arr = env->GetDoubleArrayElements(cameraLonLat,NULL);
        camera_lonlat.lon = arr[0];
        camera_lonlat.lat = arr[1];
        camera_lonlat.height = arr[2];

        arr = env->GetDoubleArrayElements(screen,NULL);
        tag_pos.screenPoint.x = arr[0];
        tag_pos.screenPoint.y = arr[1];
        coor->screenPointTolonLat(ptz,camera_lonlat,tag_pos);

        jdouble buf[] = {
            tag_pos.lonLatPoint.lon
            ,tag_pos.lonLatPoint.lat
            ,tag_pos.lonLatPoint.height
            ,tag_pos.screenPoint.x
            ,tag_pos.screenPoint.y
            ,tag_pos.worldPoint.x
            ,tag_pos.worldPoint.y
            ,tag_pos.cameraPoint.x
            ,tag_pos.cameraPoint.y
            ,tag_pos.cameraPoint.z
        };
        env->SetDoubleArrayRegion(tag_position,0,10,buf);
    }
    return tag_position;
}

jdoubleArray JNICALL JNI_FUNCTION(lonLatPointToScreen(JNIEnv * env, jobject /* this */, jlong owner, jdoubleArray cameraPTZ, jdoubleArray cameraLonLat, jdoubleArray tagLonLat)) {
    jdoubleArray tag_position = env->NewDoubleArray(10);
    if(owner){
        CoordConverter *coor = (CoordConverter*)owner;
        double ptz[3];
        PointLonLat camera_lonlat;
        TagPosition tag_pos;
        jdouble* arr;
        arr = env->GetDoubleArrayElements(cameraPTZ,NULL);
        ptz[0] = arr[0];
        ptz[1] = arr[1];
        ptz[2] = arr[2];

        arr = env->GetDoubleArrayElements(cameraLonLat,NULL);
        camera_lonlat.lon = arr[0];
        camera_lonlat.lat = arr[1];
        camera_lonlat.height = arr[2];

        arr = env->GetDoubleArrayElements(tagLonLat,NULL);
        tag_pos.lonLatPoint.lon = arr[0];
        tag_pos.lonLatPoint.lat = arr[1];
        tag_pos.lonLatPoint.height = arr[2];
        coor->lonLatPointToScreen(ptz,camera_lonlat,tag_pos);

        jdouble buf[] = {
            tag_pos.lonLatPoint.lon
            ,tag_pos.lonLatPoint.lat
            ,tag_pos.lonLatPoint.height
            ,tag_pos.screenPoint.x
            ,tag_pos.screenPoint.y
            ,tag_pos.worldPoint.x
            ,tag_pos.worldPoint.y
            ,tag_pos.cameraPoint.x
            ,tag_pos.cameraPoint.y
            ,tag_pos.cameraPoint.z
        };
        env->SetDoubleArrayRegion(tag_position,0,10,buf);
    }
    return tag_position;
}



#ifdef __cplusplus
}
#endif

