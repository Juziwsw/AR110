package com.hiar.ar110.data.vehicle;

public class CarListResByTask {
    public int retCode;
    public VehicleRecord data;
    public static class VehicleRecord {
        public int total;
        public int identified;
        public String imageUrl;
        public VehicleRecogList[] carRecoList;
    }
}
