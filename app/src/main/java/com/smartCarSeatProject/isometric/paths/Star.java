package com.smartCarSeatProject.isometric.paths;


import com.smartCarSeatProject.isometric.Path;
import com.smartCarSeatProject.isometric.Point;

/**
 * Created by fabianterhorst on 01.04.17.
 */

public class Star extends Path {

    public Star(Point origin, double outerRadius, double innerRadius, int points) {
        super();
        double r;
        for (int i = 0; i < points * 2; i++) {
            r = (i % 2 == 0) ? outerRadius : innerRadius;
            push(new Point(
                    (r * Math.cos(i * Math.PI / points)) + origin.getX(),
                    (r * Math.sin(i * Math.PI / points)) + origin.getY(),
                    origin.getZ()));
        }
    }
}
