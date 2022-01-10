package com.hiar.ar110.data.vehicle;

import com.hiar.ar110.R;

public class VehicleType {
    //根据传入的车辆类型描述，获取车辆颜色的资源ID，找不到一律是黑色
    public static int getCarType(String type) throws IllegalArgumentException {
        int totalType = VEHICLE_TYPE_DEF.length;
        if(VEHICLE_TYPE_DEF.length != CAR_TYPE_NAME.length) {
            throw new IllegalArgumentException("车辆类型资源数组和车辆类型描述数组个数不一致，请检查代码！");
        }

        if(type == null) {
            throw new IllegalArgumentException("传入的车辆类型是空的！");
        }

        for(int i=0; i<totalType; i++) {
            if(type.contains(CAR_TYPE_NAME[i])) {
                return VEHICLE_TYPE_DEF[i];
            }
        }

        throw new IllegalArgumentException("找不到对应的车辆类型资源！");
    }

    private static final int  VEHICLE_TYPE_DEF[] = {
        R.drawable.ic_tag_small_car,
        R.drawable.ic_tag_large_vehicle,
        R.drawable.ic_tag_truck,
        R.drawable.ic_tag_coach_car,
        R.drawable.ic_tag_farm_vehicles,
        R.drawable.ic_tag_moped,
        R.drawable.ic_tag_motorcycle
    };

    private static final String CAR_TYPE_NAME[] = {
            "小型", "大型", "集卡", "教练","农用运输车", "轻便摩托车","摩托车"
    };
}
