package com.smartCarSeatProject.isometric.shapes;


import com.smartCarSeatProject.isometric.Path;
import com.smartCarSeatProject.isometric.Point;
import com.smartCarSeatProject.isometric.Shape;

/**
 * Created by fabianterhorst on 02.04.17.
 */

public class Octahedron extends Shape {

    public Octahedron(Point origin) {
        super();
        Point center = origin.translate(0.5, 0.5, 0.5);
        Path upperTriangle = new Path(new Point[]{origin.translate(0, 0, 0.5), origin.translate(0.5, 0.5, 1), origin.translate(0, 1, 0.5)});
        Path lowerTriangle = new Path(new Point[]{origin.translate(0, 0, 0.5), origin.translate(0, 1, 0.5), origin.translate(0.5, 0.5, 0)});
        Path[] paths = new Path[8];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            paths[count++] = upperTriangle.rotateZ(center, i * Math.PI / 2.0);
            paths[count++] = lowerTriangle.rotateZ(center, i * Math.PI / 2.0);
        }
        setPaths(paths);
        scalePaths(center, Math.sqrt(2) / 2.0, Math.sqrt(2) / 2.0, 1);
    }
}
