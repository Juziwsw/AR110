package com.hiar.ar110.data.vehicle;

import com.hiar.ar110.R;

public class VehicleColor {
    //根据传入的车辆颜色描述，获取车辆颜色的资源ID，找不到一律是黑色
    public static int getCarColor(String color) throws IllegalArgumentException {
        int totalColor = VEHICLE_COLOUR_DEF.length;
        if(VEHICLE_COLOUR_DEF.length != CAR_COLOUR_NAME.length) {
            throw new IllegalArgumentException("颜色资源数组和颜色描述数组个数不一致，请检查代码！");
        }

        if(color == null) {
            throw new IllegalArgumentException("传入的车辆颜色值是空的！");
        }

        for(int i=0; i<totalColor; i++) {
            if(CAR_COLOUR_NAME[i].contains(color)) {
                return VEHICLE_COLOUR_DEF[i];
            }
        }

        throw new IllegalArgumentException("找不到对应的车辆颜色资源！");
    }

    private static final int  VEHICLE_COLOUR_DEF[] = {
            R.drawable.ic_icon_colour_black,
            R.drawable.ic_icon_colour_white,
            R.drawable.ic_icon_colour_blue,
            R.drawable.ic_icon_colour_cyan,
            R.drawable.ic_icon_colour_silvery,
            R.drawable.ic_icon_colour_golden,
            R.drawable.ic_icon_colour_green,
            R.drawable.ic_icon_colour_grey,
            R.drawable.ic_icon_colour_red,
            R.drawable.ic_icon_colour_yellow,
            R.drawable.ic_icon_colour_violet,
            R.drawable.ic_icon_colour_brown
    };

    private static final String CAR_COLOUR_NAME[] = {
            "黑","白", "蓝","青","银","金","绿","灰","红","黄","紫", "棕"
    };
}
