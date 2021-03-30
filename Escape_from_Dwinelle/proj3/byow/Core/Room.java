package byow.Core;

import java.util.ArrayList;

public class Room {
    private int x;
    private int y;
    private int width;
    private int height;
    private int roomNo;
    private ArrayList<Room> hallways;

    public Room(int x, int y, int width, int height, int totalRooms) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.roomNo = totalRooms;
        this.hallways = new ArrayList<>();
    }

    public Integer[] getBotLeft() {
        return new Integer[] {x, y};
    }

    public Integer[] getBotRight() {
        return new Integer[] {x + width - 1, y};
    }

    public Integer[] getTopLeft() {
        return new Integer[] {x, y + height - 1};
    }

    public Integer[] getTopRight() {
        return new Integer[] {x + width - 1, y + height - 1};
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public int getLeft() {
        return x;
    }

    public int getRight() {
        return getBotRight()[0];
    }

    public int getBottom() {
        return getY();
    }

    public int getTop() {
        return getTopLeft()[1];
    }

    public ArrayList<Integer[]> getPoints() {
        ArrayList<Integer[]> list = new ArrayList<>();
        list.add(getBotLeft());
        list.add(getBotRight());
        list.add(getTopLeft());
        list.add(getTopRight());
        return list;
    }

    public void addHallway(Room r) {
        hallways.add(r);
    }

    public ArrayList<Room> getHallways() {
        return hallways;
    }
}
