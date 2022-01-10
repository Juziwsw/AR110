#ifndef HIARVGIS_H
#define HIARVGIS_H


#include <stdint.h>

#undef HIAR_VGIS_API
#ifdef _MSC_VER
// We are using a Microsoft compiler:
#ifdef HIAR_VGIS_EXPORT
#define HIAR_VGIS_API __declspec(dllexport)
#else
#define HIAR_VGIS_API __declspec(dllimport)
#endif
#else
// Not Microsoft compiler so set empty definition:
#define HIAR_VGIS_API
#endif

typedef struct tagHiARVGISVersion
{
    uint16_t    major;             /// such as "2" in "2.4.7"
    uint16_t    minor;             /// such as "4" in "2.4.7"
    uint16_t    patch;             /// such as "7" in "2.4.7"
    uint16_t    build;             /// reserved
    char        description[64];   /// description of this version
} HiARVGISVersion;

typedef struct tagHiARVGISCameraIntrinsics
{
    int width;              /// number of cols in pixels of image for calibration
    int height;             /// number of rows in pixels of image for calibration

    double fx;              /// focal length x
    double fy;              /// focal length y
    double cx;              /// principal point x
    double cy;              /// principal point y

    double k1;              /// first radial distortion coefficient
    double k2;              /// second radial distortion coefficient
    double k3;              /// third radial distortion coefficient
    double p1;              /// first tangential distortion coefficient
    double p2;              /// second tangential distortion coefficient

    double zoom;            /// scale factor 
} HiARVGISCameraIntrinsics;

typedef struct tagHiARVGISCamera
{
    int                          id;                           /// camera id
    double                       height;                       /// camera height in meters
    int                          intrinsicsCount;              /// Number of camera intrinsics
    HiARVGISCameraIntrinsics*    intrinsics;                   /// pointer to camera intrinsics, its count is intrinsicsCount.
} HiARVGISCamera;

typedef struct tagHiARVGISCameraPose
{
    int                           id;                           /// camera id
    double                        panPos;                       /// pan pose, 相机朝正北时pan为0，向东旋转pan逐渐增加，单位是度。
    double                        tiltPos;                      /// tilt pose, 相机水平时tilt为0, 向下旋转tilt逐渐增加，单位是度。
    double                        zoomPos;                      /// scale factor 
} HiARVGISCameraPose;

typedef struct tagHiARVGISPoint2D
{
    double                        x;                            /// 屏幕左上为原点，单位是(像素/宽)
    double                        y;                            /// 屏幕左上为原点，单位是(像素/高)
} HiARVGISPoint2D;

typedef struct tagHiARVGISPoint3D                               /// 3D点所在坐标系原点为相机正下方地面位置。
{
    double                        x;                             /// 向东为x正向，单位是米。
    double                        y;                             /// 向北为y正向，单位是米。
    double                        z;                             /// 目前z值均为0，因为我们假定地面高度为0。
} HiARVGISPoint3D;

enum HiARVGISResultCode
{
    HIAR_VGIS_RESULT_OK = 0,                                     /// no error, success
    HIAR_VGIS_RESULT_NULL_PTR = -1,                              /// one or more pointer parameters are empty
    HIAR_VGIS_RESULT_FAILURE = -2,                               /// api function failed
    HIAR_VGIS_RESULT_UNKNOWN = -3,                               /// all other unlisted errors
    HIAR_VGIS_RESULT_INVALID_PARAMETER = -4,                     /// invalid parameter, parameter is nullptr
    HIAR_VGIS_RESULT_INVALID_CAMERA_ID = -5,                     /// camera id not found
    HIAR_VGIS_RESULT_INVALID_SCREEN_POINT = -6,                  /// invalid screen point
    HIAR_VGIS_RESULT_CONVERT_FAILURE = -7,                       /// can not convert screen point to world point
    HIAR_VGIS_RESULT_GET_CAMERA_POSE_FAILURE = -8,               /// can not get camerapose when world point on screen center
};

typedef void* HiARVGISHandle;                  /// HiARVGIS handle, representing an opaque HiARVGIS object

typedef int   HiARVGISResult;                  /// return type of HiARVGIS api function

#ifdef __cplusplus
extern "C"
{
#endif
    /**
     * Get the version of HiARVGIS SDK.
     * @param version               [output] the version of HiARVGIS SDK
     * return                       0:success, <0:check HiARVGISResultCode
     */
    HIAR_VGIS_API HiARVGISResult HiARVGISGetVersion(HiARVGISVersion* version);

    /**
     * Create a HiARVGIS object instance, initialize the system and return the handle to it.
     * @param hiarvgis              [output] the HiARVGIS object handle just created
     * return                       0:success, <0:check HiARVGISResultCode
     */
    HIAR_VGIS_API HiARVGISResult HiARVGISCreate(HiARVGISHandle* hiarvgis);

    /**
    * Register a camera.
    * @param hiarvgis              the HiARVGIS object handle
    * @param camera                camera info
    * return                       0:success, <0:check HiARVGISResultCode
    */
    HIAR_VGIS_API HiARVGISResult HiARVGISRegisterCamera(HiARVGISHandle hiarvgis, const HiARVGISCamera* camera);

    /**
    * Unregister a camera.
    * @param hiarvgis              the HiARVGIS object handle
    * @param cameraId              id of camera
    * return                       0:success, <0:check HiARVGISResultCode
    */
    HIAR_VGIS_API HiARVGISResult HiARVGISUnRegisterCamera(HiARVGISHandle hiarvgis, int cameraId);

    /**
    * Convert a screen point to its matching 3D world point.
    * @param hiarvgis              the HiARVGIS object handle
    * @param pose                  current camera pose
    * @param screenPoint           the point in frame image (screen)
    * @param worldPoint            [output] the point in world
    * return                       0:success, <0:check HiARVGISResultCode
    */
    HIAR_VGIS_API HiARVGISResult HiARVGISScreenPointToWorld(HiARVGISHandle hiarvgis, const HiARVGISCameraPose* pose, const HiARVGISPoint2D* screenPoint, HiARVGISPoint3D* worldPoint);

    /**
    * Convert a 3D world point to its matching screen point.
    * @param hiarvgis              the HiARVGIS object handle
    * @param pose                  current camera pose
    * @param worldPoint            the point in world
    * @param screenPoint           [output] the point in frame image (screen)
    * return                       0:success, <0:check HiARVGISResultCode
    */
    HIAR_VGIS_API HiARVGISResult HiARVGISWorldPointToScreen(HiARVGISHandle hiarvgis, const HiARVGISCameraPose* pose, const HiARVGISPoint3D* worldPoint, HiARVGISPoint2D* screenPoint);

    /**
    * Get CameraPose when 3D world point is on screen center.
    * @param hiarvgis              the HiARVGIS object handle
    * @param worldPoint            the point in world
    * @param pose                  [input/output] camera pose
    *                              [input]        pose.id pose.zoomPos
    *                              [output]       pose.panPos pose.tiltPos
    * return                       0:success, <0:check HiARVGISResultCode
    */
    HIAR_VGIS_API HiARVGISResult HiARVGISCameraPoseWorldPointOnScreenCenter(HiARVGISHandle hiarvgis, const HiARVGISPoint3D* worldPoint, HiARVGISCameraPose* pose);
    /**
     * Destroy the HiARVGIS object handle.
     * @param hiarvgis              the HiARVGIS object handle
     * return                       0:success, <0:check HiARVGISResultCode
     */
    HIAR_VGIS_API HiARVGISResult HiARVGISDestroy(HiARVGISHandle hiarvgis);

#ifdef __cplusplus
}
#endif

#endif // HIARVGIS_H