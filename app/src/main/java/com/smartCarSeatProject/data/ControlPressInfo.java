package com.smartCarSeatProject.data;

public class ControlPressInfo {
    private int weight;
    private int press1;
    private int press2;
    private int press3;
    private int press4;
    private int press5;
    private int press6;
    private int press7;
    private int press8;

    public ControlPressInfo() {

    }

    public ControlPressInfo(int weight, int press1, int press2, int press3, int press4, int press5, int press6, int press7, int press8) {
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

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPress1() {
        return press1;
    }

    public void setPress1(int press1) {
        this.press1 = press1;
    }

    public int getPress2() {
        return press2;
    }

    public void setPress2(int press2) {
        this.press2 = press2;
    }

    public int getPress3() {
        return press3;
    }

    public void setPress3(int press3) {
        this.press3 = press3;
    }

    public int getPress4() {
        return press4;
    }

    public void setPress4(int press4) {
        this.press4 = press4;
    }

    public int getPress5() {
        return press5;
    }

    public void setPress5(int press5) {
        this.press5 = press5;
    }

    public int getPress6() {
        return press6;
    }

    public void setPress6(int press6) {
        this.press6 = press6;
    }

    public int getPress7() {
        return press7;
    }

    public void setPress7(int press7) {
        this.press7 = press7;
    }

    public int getPress8() {
        return press8;
    }

    public void setPress8(int press8) {
        this.press8 = press8;
    }

    @Override
    public String toString() {
        return "ControlPressInfo{" +
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
