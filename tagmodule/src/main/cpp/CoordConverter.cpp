#include "CoordConverter.h"
#include <math.h>

static const double EarthRadius = 6371004.0;	//地球半径，单位米
static const double Pi = 3.1415926;

inline static double degToRad(double deg)
{
	return deg / 180 * Pi;
}

inline static double radToDeg(double rad)
{
	return rad / Pi * 180;
}

static inline bool isNear(float a, float b, float range = 1e-6f)
{
	return a - b < range && a - b > -range;
}


CoordConverter::CoordConverter(){
	HiARVGISCreate(&hiarvgis_);
}

CoordConverter::~CoordConverter() {
	HiARVGISDestroy(hiarvgis_);
}

/**
*设置相机内参
*/
void CoordConverter::setCameraIntrinsics(double height, HiARVGISCameraIntrinsics* intrinsics, uint32_t len) {
	HiARVGISUnRegisterCamera(hiarvgis_, 0);
	HiARVGISCamera intrinsic;
	intrinsic.id = 0;
	intrinsic.height = height;
	intrinsic.intrinsicsCount = len;
	intrinsic.intrinsics = intrinsics;
	HiARVGISResult result = HiARVGISRegisterCamera(hiarvgis_, &intrinsic);
}

int CoordConverter::screenPointTolonLat(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos) {
	HiARVGISPoint3D p3d;
	HiARVGISPoint2D p2d;
	p2d.x = tagPos.screenPoint.x;
	p2d.y = tagPos.screenPoint.y;
	HiARVGISCameraPose pose;
	pose.id = 0;
	//pose.panPos = ptz.PanPos - camera_.panPos + camera_.direction; //角度偏置至相机初始化位置
	pose.panPos = ptz[0];
	pose.tiltPos = ptz[1];
	pose.zoomPos = ptz[2];
	HiARVGISResult result = HiARVGISScreenPointToWorld(hiarvgis_, &pose, &p2d, &p3d);
	double x = p3d.x;
	double y = p3d.y;
	double z = p3d.z;
	//if (!isNear(height,0)) {
	//	//根据相似三角形计算公式，计算有高度的等效投影3d坐标位置
	//	//x1 = x * (1 - h / H);
	//	//y1 = y * (1 - h / H);
	//	x = x * (1.0 - height / camera_.height);
	//	y = y * (1.0 - height / camera_.height);
	//	z = height;
	//}
	Point3d pc(x, y, z);
	Point2d pw = millerLonLatToPoint2d(cameraLonLat);
	pw.x += p3d.x;
	pw.y -= p3d.y;	//算法坐标y轴与米勒投影坐标y相反，y轴需要做镜像
	tagPos.lonLatPoint = millerPoint2dToLonLat(pw);
	tagPos.cameraPoint = pc;
	tagPos.worldPoint = pw;
	return result;
}

int CoordConverter::lonLatPointToScreen(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos) {
	HiARVGISPoint3D p3d;
	HiARVGISPoint2D p2d;
	HiARVGISCameraPose pose;
	pose.id = 0;
	//pose.panPos = ptz.PanPos - camera_.panPos + camera_.direction; //角度偏置至相机初始化位置
	pose.panPos = ptz[0]; //角度偏置至相机初始化位置
	pose.tiltPos = ptz[1];
	pose.zoomPos = ptz[2];

	Point2d p1 = millerLonLatToPoint2d(PointLonLat(cameraLonLat));
	Point2d p2 = millerLonLatToPoint2d(tagPos.lonLatPoint);
	Point2d pc = p2 - p1;
	p3d.x = pc.x;
	p3d.y = -pc.y;	//算法坐标y轴与米勒投影坐标y相反，y轴需要做镜像
	p3d.z = tagPos.lonLatPoint.height;
	HiARVGISResult result = HiARVGISWorldPointToScreen(hiarvgis_, &pose, &p3d, &p2d);
	tagPos.screenPoint = Point2d(p2d.x, p2d.y);
	tagPos.cameraPoint = Point3d(p3d.x, p3d.y, p3d.z);
	tagPos.worldPoint = p2;
	return result;
}


int CoordConverter::trackTargetPtz(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos) {
	HiARVGISPoint3D p3d;
	HiARVGISPoint2D p2d;
	HiARVGISCameraPose pose;
	pose.id = 0;
	//pose.panPos = ptz.PanPos - camera_.panPos + camera_.direction; //输入相机初始化位置，返回需要转到的位置
	pose.panPos = ptz[0]; //输入相机初始化位置，返回需要转到的位置
	pose.tiltPos = ptz[1];
	pose.zoomPos = ptz[2];

	Point2d p1 = millerLonLatToPoint2d(PointLonLat(cameraLonLat));
	Point2d p2 = millerLonLatToPoint2d(tagPos.lonLatPoint);
	Point2d pc = p2 - p1;
	p3d.x = pc.x;
	p3d.y = -pc.y;	//算法坐标y轴与米勒投影坐标y相反，y轴需要做镜像
	p3d.z = tagPos.lonLatPoint.height;
	HiARVGISResult result = HiARVGISCameraPoseWorldPointOnScreenCenter(hiarvgis_, &p3d, &pose);
	if (HIAR_VGIS_RESULT_OK == result) {
		//ptz.PanPos = pose.panPos + camera_.panPos - camera_.direction;//算给出的云台pose转换到相机初始化的世界坐标
		ptz[0] = pose.panPos;//算给出的云台pose转换到相机初始化的世界坐标
		ptz[0] -= int(ptz[0] / 360) * 360;
		ptz[1] = pose.tiltPos;
		ptz[2] = pose.zoomPos;
	}
	return result;
}

double CoordConverter::arcDistance(PointLonLat p1, PointLonLat p2) {
	p1.lat = degToRad(p1.lat);
	p1.lon = degToRad(p1.lon);
	p2.lat = degToRad(p2.lat);
	p2.lon = degToRad(p2.lon);
	double C = sin(p1.lat)*sin(p2.lat) + cos(p1.lat)*cos(p2.lat)*cos(p1.lon - p2.lon);
	return EarthRadius * acos(C)*Pi / 180;
}

Point3d CoordConverter::lonLatToPoint3D(PointLonLat p) {
	p.lat = degToRad(p.lat);
	p.lon = degToRad(p.lon);
	double x = EarthRadius * cos(p.lat)*cos(p.lon);
	double y = EarthRadius * cos(p.lat)*sin(p.lon);
	double z = EarthRadius * sin(p.lat);
	return Point3d(x, y, z);
}

PointLonLat CoordConverter::point3DTolonLat(Point3d p) {
	PointLonLat plonLat;
	plonLat.lat = asin(p.z / EarthRadius);
	plonLat.lon = atan(p.y / p.x);
	plonLat.lat = radToDeg(plonLat.lat);
	plonLat.lon = radToDeg(plonLat.lon);
	if (plonLat.lon < 0) {
		if (p.x < 0 && p.y >0) {
			//第二象限
			plonLat.lon += 180.0;
		}
	}
	return plonLat;
}


Point2d CoordConverter::millerLonLatToPoint2d(PointLonLat p) {
	double L = EarthRadius * Pi * 2;     // 地球周长
	double W = L;     // 平面展开后，x轴等于周长
	double H = L / 2;     // y轴约等于周长一半
	double mill = 2.3;      // 米勒投影中的一个常数，范围大约在正负2.3之间
	double x = degToRad(p.lon);     // 将经度从度数转换为弧度
	double y = degToRad(p.lat);     // 将纬度从度数转换为弧度
	// 米勒投影的转换
	//u = tan(0.25 * Pi + 0.4 * y)
	//y = 1.25 * log(u);
	double u = tan(0.25 * Pi + 0.4 * y);
	y = 1.25 * log(u);
	// 这里将弧度转为实际距离
	x = (W / 2) + (W / (2 * Pi)) * x;
	y = (H / 2) - (H / (2 * mill)) * y;
	// 转换结果的单位是公里
	// 可以根据此结果，算出在某个尺寸的画布上，各个点的坐标
	return Point2d(x, y);
}

PointLonLat CoordConverter::millerPoint2dToLonLat(Point2d p) {
	double L = EarthRadius * Pi * 2;     // 地球周长
	double W = L;     // 平面展开后，x轴等于周长
	double H = L / 2;     // y轴约等于周长一半
	double mill = 2.3;      // 米勒投影中的一个常数，范围大约在正负2.3之间
	double x = (p.x - W/2) / (W / (2 * Pi));
	double y = -(p.y - H/2) / (H / (2 * mill));
	// 反算米勒投影的经纬度
	//u = tan(0.25 * Pi + 0.4 * y)
	//y = 1.25 * log(u);
	double u = exp(y / 1.25);
	y = (atan(u) - 0.25*Pi) / 0.4;
	return PointLonLat(radToDeg(x), radToDeg(y), 0);
}
