package com.smartCarSeatProject.data;

public class City {
    private int weight;
    private int press1;
    private int press2;
    private int press3;
    private int press4;
    private int press5;
    private int press6;
    private int press7;
    private int press8;

    public City(int weight,int press1,int press2,int press3,int press4,int press5,int press6,int press7,int press8) {
        this.weight = weight;
        this.press1 = press1;
        this.press2 = press2;
        this.press3 = press3;
        this.press4 = press4;
        this.press5 = press5;
        this.press6 = press6;
        this.press7 = press7;
        this.press8 = press8;
    }


    @Override
    public String toString() {
        return "City{" +
                "weight=" + weight +
                ", press1=" + press1 +
                ", press2=" + press2 +
                ", press3=" + press3 +
                ", press4=" + press4 +
                ", press5=" + press5 +
                ", press6=" + press6 +
                ", press7=" + press7 +
                ", press8=" + press8 +
                '}';
    }
}
