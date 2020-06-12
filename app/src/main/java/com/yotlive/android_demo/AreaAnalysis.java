package com.yotlive.android_demo;

/**
 * Calculate and store some statistics of pressure data by areas (8*9 resolution).
 */
class AreaAnalysis{
    public double a1;
    public double a2;
    public double b1;
    public double b2;
    public double c1;
    public double c2;

    // Use a threshold to filter out noise.
    public AreaAnalysis(int[][] data, int threshold){
        a1 = 0;
        a2 = 0;
        b1 = 0;
        b2 = 0;
        c1 = 0;
        c2 = 0;
        double a1Points = 0;
        double a2Points = 0;
        double b1Points = 0;
        double b2Points = 0;
        double c1Points = 0;
        double c2Points = 0;
        for(int i=0; i<4; i++){
            for(int j=0; j<3; j++){
                if(data[i][j]>threshold){
                    a1 = a1 + data[i][j];
                    a1Points = a1Points + 1;
                }
            }
        }
        for(int i=0; i<4; i++){
            for(int j=3; j<6; j++){
                if(data[i][j]>threshold){
                    a2 = a2 + data[i][j];
                    a2Points = a2Points + 1;
                }
            }
        }
        for(int i=0; i<4; i++){
            for(int j=6; j<9; j++){
                if(data[i][j]>threshold){
                    b1 = b1 + data[i][j];
                    b1Points = b1Points + 1;
                }
            }
        }
        for(int i=4; i<8; i++){
            for(int j=0; j<3; j++){
                if(data[i][j]>threshold){
                    b2 = b2 + data[i][j];
                    b2Points = b2Points + 1;
                }
            }
        }
        for(int i=4; i<8; i++){
            for(int j=3; j<6; j++){
                if(data[i][j]>threshold){
                    c1 = c1 + data[i][j];
                    c1Points = c1Points + 1;
                }
            }
        }
        for(int i=4; i<8; i++){
            for(int j=6; j<9; j++){
                if(data[i][j]>threshold){
                    c2 = c2 + data[i][j];
                    c2Points = c2Points + 1;
                }
            }
        }
        if(a1Points != 0){
            a1 = a1/a1Points;
        }
        if(a2Points != 0){
            a2 = a2/a2Points;
        }
        if(b1Points != 0){
            b1 = b1/b1Points;
        }
        if(b2Points != 0){
            b2 = b2/b2Points;
        }
        if(c1Points != 0){
            c1 = c1/c1Points;
        }
        if(c2Points != 0){
            c2 = c2/c2Points;
        }
    }
}
