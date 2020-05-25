package com.smartCarSeatProject.isometric.shapes;


import com.smartCarSeatProject.isometric.Point;
import com.smartCarSeatProject.isometric.Shape;
import com.smartCarSeatProject.isometric.paths.Circle;

/**
 * Created by fabianterhorst on 01.04.17.
 */

public class Cylinder extends Shape {

    public Cylinder(Point origin, double vertices, double height) {
        this(origin, 1, vertices, height);
    }

    public Cylinder(Point origin, double radius, double vertices, double height) {
        super();
        Circle circle = new Circle(origin, radius, vertices);
        extrude(this, circle, height);
    }
}
