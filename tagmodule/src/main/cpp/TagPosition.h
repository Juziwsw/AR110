#pragma once

#include <hiarvgis.h>

namespace tag {
	struct PointLonLat
	{
		double lon = 0;		//longitude,经度
		double lat = 0;		//latitude,维度
		double height = 0;		//altitude,高程
		PointLonLat() {}

		PointLonLat(double longitude, double latitude, double altitude) {
			this->lon = longitude;
			this->lat = latitude;
			this->height = altitude;
		}
	};

	struct Point2d
	{
		double x = 0;
		double y = 0;
		Point2d() {}
		Point2d(double x, double y) {
			this->x = x;
			this->y = y;
		}

		Point2d operator -(const Point2d& p) {
			Point2d np;
			np.x = this->x - p.x;
			np.y = this->y - p.y;
			return np;
		}

		Point2d operator +(const Point2d& p) {
			Point2d np;
			np.x = this->x + p.x;
			np.y = this->y + p.y;
			return np;
		}
	};

	struct Point3d
	{
		double x = 0;
		double y = 0;
		double z = 0;
		Point3d() {}
		Point3d(double x, double y, double z) {
			this->x = x;
			this->y = y;
			this->z = z;
		}

		Point3d operator -(const Point3d& p) {
			Point3d np;
			np.x = this->x - p.x;
			np.y = this->y - p.y;
			np.z = this->z - p.z;
			return np;
		}

		Point3d operator +(const Point3d& p) {
			Point3d np;
			np.x = this->x + p.x;
			np.y = this->y + p.y;
			np.z = this->z + p.z;
			return np;
		}
	};

	struct TagPosition {
		PointLonLat lonLatPoint;				//经纬度坐标
		Point2d screenPoint;					//点在中屏幕相对位置,单位是(像素/宽)
		Point2d worldPoint;					    //平面世界坐标
		Point3d cameraPoint;					//3d相机坐标
	};
};

