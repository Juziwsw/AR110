package com.hiscene.armap;

import com.hiscene.gis.Point2d;
import com.hiscene.gis.PointLonLat;

public class TagInfo {
    public final static double ACTIVATION_DISTANCE_SQU = 0.002;
    public String name = "";
    public Point2d screenPoint = new Point2d();
    public PointLonLat lonLat = new PointLonLat();

    public TagInfo() {
    }


    public TagInfo(String name, Point2d screen, PointLonLat lonLat) {
        this.name = name;
        this.screenPoint = screen;
        this.lonLat = lonLat;
    }

    public void setScreenPoint(Point2d p) {
        double dis_squ = disSqu(p,this.screenPoint);
        if (dis_squ  > ACTIVATION_DISTANCE_SQU){
            this.screenPoint = p;
        }
    }

    public double disSqu(Point2d p1,Point2d p2) {
        return (p1.x - p2.x)* (p1.x - p2.x) + (p1.y - p2.y)* (p1.y - p2.y);
    }

}
