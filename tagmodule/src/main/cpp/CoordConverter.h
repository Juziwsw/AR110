#pragma once

#include <hiarvgis.h>
#include <vector>
#include "TagPosition.h"

using namespace tag;

class CoordConverter {
public:
	CoordConverter();
	~CoordConverter();

	/**
	*设置相机内参
	*/
	void setCameraIntrinsics(double height, HiARVGISCameraIntrinsics* intrinsics, uint32_t len);

	/**
	*设置相机位置
	*/
	void setCameraLonLat(const PointLonLat& lonlat);

	/**
	*屏幕坐标转3d世界坐标和经纬度坐标
	*@param ptz:云台信息
	*@param postion:要转换的tag位置信息
	*/
	int screenPointTolonLat(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos);

	/**
	*经纬度坐标转屏幕坐标
	*/
	int lonLatPointToScreen(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos);

	/**
	*获取跟踪目标点云台旋转信息
	*@param ptz:云台信息，返回一个需要调整的云台信息
	*@param postion:要转换的tag位置信息
	*@return 返回计算结果
	*/
	int trackTargetPtz(double ptz[], const PointLonLat& cameraLonLat, TagPosition& tagPos);


	/**
	*经纬度转3D坐标，单位为米，向东为X轴正向，向北为Y轴正向，Z轴为垂直地面向上
	*/
	Point3d lonLatToPoint3D(PointLonLat p);

	/**
	*3D坐标转经纬度，单位为米，向东为X轴正向，向北为Y轴正向，Z轴为垂直地面向上
	*/
	PointLonLat point3DTolonLat(Point3d p);

	/**
	*返回两点（经纬度）之间的弧度距离，单位是米
	*/
	double arcDistance(PointLonLat p1, PointLonLat p2);


	/**
	*弥勒投影,经纬度转换为平面坐标，单位是米
	*坐标原点为北极点，x轴从本初子午线自西向东增加，y轴由北极向南极增加
	*/
	Point2d millerLonLatToPoint2d(PointLonLat p);

	/**
	*弥勒投影,平面坐标转换为经纬度，单位是米
	*坐标原点为北极点，x轴从本初子午线自西向东增加，y轴由北极向南极增加
	*/
	PointLonLat millerPoint2dToLonLat(Point2d p);

private:
	HiARVGISHandle hiarvgis_;					//算法句柄
};
