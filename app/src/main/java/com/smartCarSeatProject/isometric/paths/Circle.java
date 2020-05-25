package com.smartCarSeatProject.isometric.paths;

import com.smartCarSeatProject.isometric.Path;
import com.smartCarSeatProject.isometric.Point;
/**
 * Created by fabianterhorst on 01.04.17.
 */

public class Circle extends Path {

    @Deprecated
    public Circle(Point origin, double radius) {
        this(origin, radius, 20);
    }

    public Circle(Point origin, double radius, double vertices) {
        super();
        for (int i = 0; i < vertices; i++) {
            push(new Point(
                    (radius * Math.cos(i * 2 * Math.PI / vertices)) + origin.getX(),
                    (radius * Math.sin(i * 2 * Math.PI / vertices)) + origin.getY(),
                    origin.getZ()));
        }
    }
}
