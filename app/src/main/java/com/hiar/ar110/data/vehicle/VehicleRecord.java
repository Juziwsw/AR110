package com.hiar.ar110.data.vehicle;

import java.util.Arrays;

/**
 * author: liwf
 * date: 2021/5/27 14:43
 */
public class VehicleRecord {
    public int total;
    public int identified;
    public String imageUrl;
    public VehicleRecogList[] carRecoList;
    @Override
    public String toString() {
        return "VehicleRecord{" +
            "total=" + total +
            ", identified=" + identified +
            ", imageUrl='" + imageUrl + '\'' +
            ", carRecoList=" + Arrays.toString(carRecoList) +
            '}';
    }
}
