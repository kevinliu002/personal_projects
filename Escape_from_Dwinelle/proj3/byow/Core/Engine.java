package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.algs4.Stopwatch;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Engine {
       TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final int SENSITIVITY = 30;
    PrintWriter writer;
    public String master;
    public String inputString;
    private Random random;
    public int steps;
    public ArrayList<Room>  doorsTracker = new ArrayList<>();


    private static ArrayList<Room> rooms = new ArrayList<>();
    private static ArrayList<Room> availableRooms = new ArrayList<>();
    TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
    StringBuilder sb = new StringBuilder();

    FileReader reader;
    BufferedReader br;

    boolean enteringSeed;

    int[] currLoc;
    boolean menuScreen;
    boolean colonPressed;
    boolean loadedWorld;
    boolean switchOn;
    //avatarType
    int avatarType;
    File f = new File("./master.txt");
    boolean doQuit;
    boolean withKeyboard;
    boolean notLose = true;
    String msg;



    public void interactWithKeyboard() {
        setUpMenu();
        avatarType = 0;
        doQuit = false;
        withKeyboard = true;

        master = "";

        while (!doQuit) {
            while (menuScreen) {
                if (StdDraw.hasNextKeyTyped()) {
                    Character c = StdDraw.nextKeyTyped();
                    menuControlHandler(c);
                }
            }

            for (int x = 0; x < random.nextInt(10-5)+5; x++){
                Room door = availableRooms.get(random.nextInt(availableRooms.size()-1));
                doors(finalWorldFrame, door);
                doorsTracker.add(door);
            }

            Room goal = availableRooms.get(random.nextInt(availableRooms.size()-1));

            fillTiles(finalWorldFrame,Tileset.FLOWER,goal.getWidth() - 2,goal.getHeight() - 2,goal.getX() + 1,goal.getY() + 1);

            ter.initialize(WIDTH, HEIGHT);
            ter.renderFrame(finalWorldFrame);

            while (!menuScreen) {
                ter.renderFrame(finalWorldFrame);
                printTileType(finalWorldFrame);
                StdDraw.textLeft(10,HEIGHT-1,"Moves remaining " + Integer.toString(steps));

                if (findCurrRoom(currLoc) == goal){
                    notLose = true;
                    youWin();
                }
                for (int x = 0; x < doorsTracker.size(); x++) {
                    if (findCurrRoom(currLoc) == doorsTracker.get(x)) {
                        portal();
                    }
                }

                showMessage();
                StdDraw.show();
                if (StdDraw.hasNextKeyTyped()) {
                    Character c = StdDraw.nextKeyTyped();
                    duringGameControlHandler(c);
                }
            }
        }

        System.exit(0);


    }


    public TETile[][] interactWithInputString(String input) {
        withKeyboard = false;
        menuScreen = true;
        avatarType = 0;

        master = "";

        System.out.println(input);

        ArrayList<Character> charList = inputToCharList(input);

        int i = 0;
        while(i < charList.size() && !doQuit) {
            while (i < charList.size() && menuScreen) {
                menuControlHandler(charList.get(i));
                i++;
            }

//            ter.initialize(WIDTH, HEIGHT);
//            ter.renderFrame(finalWorldFrame);

            while (i < charList.size() && !menuScreen) {
//                ter.renderFrame(finalWorldFrame);
                duringGameControlHandler(charList.get(i));
                i++;
            }
        }
        return finalWorldFrame;
    }

    /**
     * This method draws a line of tiles
     *
     * @param world     the array of tiles to edit
     * @param number    the number of tiles to change
     * @param direction the direction in which to draw the line
     * @param type      the type of tile to draw the line with
     * @param startXDir the X value of the starting position
     * @param startYDir the Y value of the starting position
     */
    public void drawTiles(TETile[][] world, int number, int direction, TETile type, int startXDir, int startYDir) {
        //takes in number of tiles to draw, tile type and direction to draw in
        // 1 is up, 2 is down, 3 is left, 4 is right

        if (direction > 4 || direction < 1) {
            throw new IllegalArgumentException("Error: Direction needs to be between 1 and 4");
        }

        for (int x = 0; x < number; x++) {
            if (direction == 1) {
                world[startXDir][startYDir + x] = type;
            } else if (direction == 2) {
                world[startXDir][startYDir - x] = type;
            } else if (direction == 3) {
                world[startXDir - x][startYDir] = type;
            } else if (direction == 4) {
                world[startXDir + x][startYDir] = type;
            }
        }
    }

    /**
     * This method draws a room with width WIDTH and height HEIGHT where the bottom left corner is at the location (startXDir, startYDir)
     *
     * @param world     the array of tiles to edit
     * @param width     width of the room
     * @param height    height of the room
     * @param startXDir the X value of the top-left corner
     * @param startYDir the Y value of the top-left corner
     */
    public void drawRoom(TETile[][] world, int width, int height, int startXDir, int startYDir, int index) {
        //Draw the top wall
        drawTiles(world, width, 4, Tileset.WALL, startXDir, startYDir + height - 1);

        //Draw the bottom wall
        drawTiles(world, width, 4, Tileset.WALL, startXDir, startYDir);

        //Draw the left wall
        drawTiles(world, height, 1, Tileset.WALL, startXDir, startYDir);

        //Draw the right wall
        drawTiles(world, height, 1, Tileset.WALL, startXDir + width - 1, startYDir);

        //Fill the room with floor
        fillTiles(world, Tileset.FLOOR, width - 2, height - 2, startXDir + 1, startYDir + 1);

        Room r = new Room(startXDir, startYDir, width, height, index);
        rooms.add(r);
        availableRooms.add(r);
    }

    /**
     * Fill the tiles in a rectangle of WIDTH and HEIGHT starting from top-left at (startXDir, startYDir)
     *
     * @param world     the array of tiles to edit
     * @param type      the tile type to fill with
     * @param width     the width to fill
     * @param height    the height to fill
     * @param startXDir top-left X location to start fill
     * @param startYDir top-left Y location to start fill
     */
    public void fillTiles(TETile[][] world, TETile type, int width, int height, int startXDir, int startYDir) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x + startXDir][y + startYDir] = type;
            }
        }
    }

    //
//    /**
//     *
//     * @param world array of tiles to edit
//     * @param length length of the hallway
//     * @param direction direction to build in. 1 is up, 2 is down, 3 is left, 4 is right
//     * @param startXDir, starting X coordinate
//     * @param startYDir, starting Y coordinate
//     */
    public void addHallway(TETile[][] world, Room r, boolean connectVertical) {
        int direction = 0;
        int length = 0;
        int startXDir = r.getX();
        int startYDir = r.getY();

        if (connectVertical) {
            direction = 1;
            length = r.getHeight() + 2;
            startXDir++;
            startYDir--;
        } else {
            direction = 4;
            length = r.getWidth() + 2;
            startYDir++;
            startXDir--;
        }

        drawTiles(world, length, direction, Tileset.FLOOR, startXDir, startYDir);
        if (direction == 1) {
            drawTiles(world, length, direction, Tileset.WALL, startXDir - 1, startYDir);
            drawTiles(world, length, direction, Tileset.WALL, startXDir + 1, startYDir);
        } else {
            drawTiles(world, length, direction, Tileset.WALL, startXDir, startYDir - 1);
            drawTiles(world, length, direction, Tileset.WALL, startXDir, startYDir + 1);
        }
//        System.out.println("Adding hallway");
    }

    private boolean checkNewRoom(Room newRoom) {
        for (Room r : rooms) {
            if (r.getLeft() <= newRoom.getRight() && r.getRight() >= newRoom.getLeft()
                    && r.getTop() >= newRoom.getBottom() && r.getBottom() <= newRoom.getTop()) {
                return true;
            }
        }
        return false;
    }

    public boolean runsOffUniverse(Room r) {
        int x = r.getX();
        int y = r.getY();
        int width = r.getWidth();
        int height = r.getHeight();

        if (x + width > WIDTH) {
            return true;
        } else if (y + height > HEIGHT) {
            return true;
        } else {
            return false;
        }
    }

    public boolean allConnected(UnionFind uf, int numRooms) {
//        System.out.println("Size of 0 set " + uf.sizeOf(0));
        return uf.sizeOf(0) == numRooms;
    }

    /**
     * Randomly choses a wall and turns it into a locked/unlocked door.
     *
     * @param world the array of tiles to edit
     * @param walls an arraylist of tiles to choose from.
     */
    //TODO Take seed from input
    public void doors(TETile[][] world, Room room) {
        int lower = room.getBotLeft()[0];
        int upper = room.getBotRight()[0];
        int tileNum = random.nextInt(upper-lower) + lower;
        world[tileNum+1][room.getY()+1] = Tileset.UNLOCKED_DOOR;
    }

    public void joinRooms(TETile[][] world, UnionFind connectedRooms, int i1, int i2, Random random) {
        Room r1 = rooms.get(i1);
        Room r2 = rooms.get(i2);

        int r1RoomNo = r1.getRoomNo();

        boolean isStraightHallway = false;
        boolean connectVertical = false;

        //initialize and declare overlap variables
        int xo1 = -1;
        int xo2 = -1;
        int yo1 = -1;
        int yo2 = -1;

        //Check to see if there's any x overlap in floor tiles
        if (r1.getLeft() + 1 < r2.getRight() - 1 && r2.getLeft() + 1 < r1.getRight() - 1) {

            isStraightHallway = true;
            connectVertical = true;

            if (r1.getLeft() <= r2.getLeft()) {
                xo1 = r2.getLeft() + 1;
            } else {
                xo1 = r1.getLeft() + 1;
            }
            if (r1.getRight() >= r2.getRight()) {
                xo2 = r2.getRight() - 1;
            } else {
                xo2 = r1.getRight() - 1;
            }
        }
        // Any y overlap
        else if (r1.getBottom() + 1 < r2.getTop() - 1 && r2.getBottom() + 1 < r1.getTop() - 1) {

            isStraightHallway = true;
            connectVertical = false;

            if (r1.getBottom() <= r2.getBottom()) {
                yo1 = r2.getBottom() + 1;
            } else {
                yo1 = r1.getBottom() + 1;
            }
            if (r1.getTop() >= r2.getTop()) {
                yo2 = r2.getTop() - 1;
            } else {
                yo2 = r1.getTop() - 1;
            }
        }

        if (isStraightHallway) {
            if (connectVertical) {
                Room top;
                Room bottom;

                if (r1.getBottom() > r2.getTop()) {
                    top = r1;
                    bottom = r2;
                } else {
                    top = r2;
                    bottom = r1;
                }

                int midX = RandomUtils.uniform(random, xo2 - xo1 + 1) + xo1;

                Room hallway = new Room(midX - 1, bottom.getTop() + 1, 3, top.getBottom() - bottom.getTop() - 1, 100);

                if (!runsOffUniverse(hallway) && !checkNewRoom(hallway)) {
                    rooms.add(hallway);
                    addHallway(world, hallway, connectVertical);

                    connectedRooms.union(i1, i2);
                    r1.addHallway(hallway);
                    r2.addHallway(hallway);
                }
            } else {
                Room left;
                Room right;

                if (r1.getLeft() > r2.getRight()) {
                    right = r1;
                    left = r2;
                } else {
                    right = r2;
                    left = r1;
                }

                int midY = RandomUtils.uniform(random, yo2 - yo1 + 1) + yo1;

                Room hallway = new Room(left.getRight() + 1, midY - 1, right.getLeft() - left.getRight() - 1, 3, 100);

                if (!runsOffUniverse(hallway) && !checkNewRoom(hallway)) {
                    rooms.add(hallway);

                    addHallway(world, hallway, connectVertical);

                    connectedRooms.union(i1, i2);
                    r1.addHallway(hallway);
                    r2.addHallway(hallway);
                }
            }
        }
    }

    public void removeUnconnectedRooms(TETile[][] world, UnionFind connectedRooms, int totalRooms) {
        int largestRoot = 0;
        int mostChildren = 0;
        for (int i = 0; i < totalRooms; i++) {
            if (connectedRooms.sizeOf(i) > mostChildren) {
                largestRoot = connectedRooms.find(i);
                mostChildren = connectedRooms.sizeOf(i);
            }
        }

        for (int i = 0; i < totalRooms; i++) {
            if (connectedRooms.sizeOf(i) != mostChildren && i != largestRoot) {
                removeRoom(world, rooms.get(i));
                availableRooms.remove(rooms.get(i));
                removeHallways(world, i);
            }
        }

    }

    public void removeRoom(TETile[][] world, Room r) {
        int width = r.getWidth();
        int height = r.getHeight();
        int startXDir = r.getX();
        int startYDir = r.getY();


        //Draw the top wall
        drawTiles(world, width, 4, Tileset.NOTHING, startXDir, startYDir + height - 1);

        //Draw the bottom wall
        drawTiles(world, width, 4, Tileset.NOTHING, startXDir, startYDir);

        //Draw the left wall
        drawTiles(world, height, 1, Tileset.NOTHING, startXDir, startYDir);

        //Draw the right wall
        drawTiles(world, height, 1, Tileset.NOTHING, startXDir + width - 1, startYDir);

        //Fill the room with floor
        fillTiles(world, Tileset.NOTHING, width - 2, height - 2, startXDir + 1, startYDir + 1);

    }

    public Room connectingHallway(TETile[][] world, Room r1, Room r2) {
        return null;
    }

    public void removeHallways(TETile[][] world, int i) {
        Room r = rooms.get(i);
        for (Room h : r.getHallways()) {
            removeRoom(world, h);
        }
    }

    public int[] placePlayer(TETile[][] world, Random random, int totalRooms) {
        Room r = availableRooms.get(RandomUtils.uniform(random, availableRooms.size()));

        //Random floor tile
        int x = RandomUtils.uniform(random, r.getRight() - r.getLeft() - 1) + r.getLeft() + 1;
        int y = RandomUtils.uniform(random, r.getTop() - r.getBottom() - 1) + r.getBottom() + 1;

        //Set floor tile to player
        world[x][y] = Tileset.AVATAR;
        int[] currLoc = {x, y};
        return currLoc;
    }

    public void placeInformation(TETile[][] world, Random random, int totalRooms) {
        for (int i = 0; i < 4; i++) {
            Room r = availableRooms.get(RandomUtils.uniform(random, availableRooms.size()));

            //Random floor tile
            int x = RandomUtils.uniform(random, r.getRight() - r.getLeft() - 1) + r.getLeft() + 1;
            int y = RandomUtils.uniform(random, r.getTop() - r.getBottom() - 1) + r.getBottom() + 1;

            //Set floor tile to information
            world[x][y] = Tileset.WATER;
        }
    }


    /**
     * @param world     - 2D tile array
     * @param direction -  1 is up, 2 is down, 3 is left, 4 is right
     */
    public int[] movePlayer(TETile[][] world, int direction, int currX, int currY) {
//        System.out.println("Moving player");

        world[currX][currY] = Tileset.FLOOR;
        int newX = currX;
        int newY = currY;

        switch (direction) {
            case 0:
                break;
            case 1:
                newY++;
                break;
            case 2:
                newY--;
                break;
            case 3:
                newX--;
                break;
            case 4:
                newX++;
                break;
        }

        if (world[newX][newY] == Tileset.WALL) {
            world[currX][currY] = Tileset.AVATAR;
            return new int[]{currX, currY};

        }
        Room newRoom = findCurrRoom(new int[] {newX, newY});

        if (!switchOn || newRoom != findCurrRoom(currLoc)) {
            lightUpRoom(finalWorldFrame, findCurrRoom(currLoc), Tileset.FLOOR);
        }

        if (switchOn) {
            lightUpRoom(finalWorldFrame, newRoom, Tileset.SAND);
        }

        if (finalWorldFrame[newX][newY] == Tileset.WATER) {
            changeMessage();
        }

        world[newX][newY] = Tileset.AVATAR;
        int[] newLoc = {newX, newY};
        return newLoc;
    }

    public void printTileType(TETile[][] world) {
        int  x_cord = (int) StdDraw.mouseX();
        int  y_cord = (int) StdDraw.mouseY();
        if (y_cord == 30) {
            y_cord--;
        }
        TETile x = world[x_cord][y_cord];
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.textLeft(1,HEIGHT - 1,x.description());
    }

    public Room findCurrRoom(int[] currLoc) {
        int currX = currLoc[0];
        int currY = currLoc[1];
        for (Room r : rooms) {
            if (currX >= r.getLeft() && currX <= r.getRight() && currY >= r.getBottom() && currY <= r.getTop()) {
                return r;
            }
        }
        return null;
    }

    public void lightUpRoom(TETile[][] world, Room r, TETile t) {
        for (int x = r.getLeft() + 1; x < r.getRight(); x++) {
            for (int y = r.getBottom() + 1; y < r.getTop(); y++) {
                if (world[x][y] != Tileset.WATER) {
                    world[x][y] = t;
                }
            }
        }
    }

    public void duringGameControlHandler (Character c) {

        int direction = 0;
        switch (c) {
            case 'w':
                master += "w";
                direction = 1;
                steps--;
                break;
            case 's':
                master += "s";
                direction = 2;
                steps--;
                break;
            case 'a':
                master += "a";
                direction = 3;
                steps--;
                break;
            case 'd':
                master += "d";
                direction = 4;
                steps--;
                break;
            case 'o':
                master += "o";
                switchOn = !switchOn;
                break;
            case 'c':
                master += "c";
                avatarType = (avatarType + 1) % 3;
                Tileset.switchAvatar(avatarType);
                break;
            case ':':
                colonPressed = true;
                break;
            case 'q':
                if (colonPressed) {
                    sb = new StringBuilder();
                    try {
                        writer = new PrintWriter(f);
                    } catch (FileNotFoundException e) {
                        System.out.println(e);
                    }
                    System.out.println(master);
                    writer.println(master);
                    writer.close();

                    doQuit = true;
                    menuScreen = true;
                }
                break;
        }

        if (c == 'w' || c == 's' || c == 'a' || c == 'd' || c == 'o' || c == 'c') {
            currLoc = movePlayer(finalWorldFrame, direction, currLoc[0], currLoc[1]);
        }
        if (steps <= 0 && withKeyboard){
            youLose();
            doQuit =true;
            notLose = false;
        }

    }


    public void menuControlHandler(Character c) {
        if (c == 'l') {
            String input = "";
            try {
                br = new BufferedReader(new FileReader(f));
                input = br.readLine();
                System.out.println(input);
                br.close();
            } catch (FileNotFoundException e) {
                System.out.println(e);
            } catch (IOException e) {
                System.out.println(e);
            }
            interactWithInputString(input);
            System.out.println("done interacting");
            enteringSeed = false;
            menuScreen = false;
            withKeyboard = true;
        }
        if (c == 'n') {
            master += "n";
            if (withKeyboard) {
                askForSeed();
            }
            enteringSeed = true;
            rooms.clear();
            availableRooms.clear();
        }
        if (enteringSeed && c >= '0' && c <= '9') {
            master += c.toString();
            sb.append(c);
        }
        if (enteringSeed && c == 's') {
            master += "s";
            System.out.println("done adding seed");
            enteringSeed = false;
            setUpGame(Long.parseLong(sb.toString()));
            menuScreen = false;
        }
    }


    public void askForSeed() {
        StdDraw.text(WIDTH / 2, HEIGHT / 2 + 3 , "Enter seed");
        StdDraw.show();
    }

    public void setUpMenu() {
        StdDraw.clear();
        StdDraw.show();
        ter.initialize(WIDTH, HEIGHT);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH/2, HEIGHT/2,"Escape From Dwinelle, A CS61B Game");
        StdDraw.text(WIDTH/2,HEIGHT/2 - 3, "New Game (N)");
        StdDraw.text(WIDTH/2,HEIGHT/2 - 6, "Load Game (L)");
        StdDraw.text(WIDTH/2,HEIGHT/2 - 9, "Quit (Q)");
        StdDraw.text(WIDTH/2,HEIGHT/2 - 12, "Use C to switch avatars");
        StdDraw.show();
        menuScreen = true;
    }

    public void youLose(){

        System.out.println("YOU LOSE. :( SAD.");
        StdDraw.clear();
        StdDraw.show();
        ter.initialize(WIDTH, HEIGHT);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH/2, HEIGHT/2,"You lose. Sorry");
        StdDraw.show();
        try {
            Thread.sleep(1000); }
        catch (InterruptedException e) {
            System.out.println(e);
        }
        menuScreen = true;
        boolean notLose = false;
    }

    public void youWin(){
        System.out.println("YOU WIN");
        StdDraw.clear();
        StdDraw.show();
        ter.initialize(WIDTH, HEIGHT);
        StdDraw.setFont(new Font("Arial", Font.BOLD, 40));
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH/2, HEIGHT/2,"You Won. Congrats! ");
        StdDraw.show();
        try {
            Thread.sleep(1000); }
        catch (InterruptedException e) {
            System.out.println(e);
        }
        menuScreen = true;
        boolean notLose = true;
    }

    public int[] setUpGame(long seed) {
        random = new Random(seed);
        steps = random.nextInt(80-20) + 50;
        Tileset.switchAvatar(avatarType);
        fillTiles(finalWorldFrame, Tileset.NOTHING, WIDTH, HEIGHT, 0, 0);

        msg = "";

        // STEP ONE: Non-overlapping rooms

        // 0: Setup. f keeps track of how many failures we have left
        int f = SENSITIVITY;

        //0: Setup. keeps track of how many rooms we have
        int totalRooms = 0;

        while (f >= 0) {
            // A: Random location, width, height
            int x = RandomUtils.uniform(random, WIDTH);
            int y = RandomUtils.uniform(random, HEIGHT);
            int w = RandomUtils.uniform(random, WIDTH / 6) + 3;
            int h = RandomUtils.uniform(random, HEIGHT / 6) + 3;

            Room newRoom = new Room(x, y, w, h, totalRooms);

            //B: Check to see if the new room runs off the universe
            if (!runsOffUniverse(newRoom)) {
                //C: Check to see if the new room overlaps and old one
                boolean isOverlapping = checkNewRoom(newRoom);

                //D: If it is overlapping, try again, and decrement s. If not, draw the room, reset f, and increment totalRooms
                if (isOverlapping) {
                    f--;
                } else {
                    f = SENSITIVITY;
                    drawRoom(finalWorldFrame, w, h, x, y, totalRooms);
                    totalRooms++;
                }
            }
        }

        //STEP TWO: Adjoining Halls

        // A: Setup the disjoint set of rooms
        UnionFind connectedRooms = new UnionFind(rooms.size());
        int count = 0;
        while (!allConnected(connectedRooms, totalRooms)) {
            // B: Pick two random rooms
            int i1 = RandomUtils.uniform(random, totalRooms);
            int i2 = RandomUtils.uniform(random, totalRooms);

            // C: Check if they're connected
            boolean isConnected = connectedRooms.connected(i1, i2);


            if (!isConnected) {
                joinRooms(finalWorldFrame, connectedRooms, i1, i2, random);
            }
            count++;
            if (count > 2000) {
                removeUnconnectedRooms(finalWorldFrame, connectedRooms, totalRooms);
                break;
            }
        }

        //STEP THREE: PLACE THE PLAYER
        currLoc = placePlayer(finalWorldFrame, random, totalRooms);
        colonPressed = false;
        switchOn = true;

        Room newRoom = findCurrRoom(currLoc);
        lightUpRoom(finalWorldFrame, newRoom, Tileset.SAND);

        finalWorldFrame[currLoc[0]][currLoc[1]] = Tileset.AVATAR;

        // PLACE THE INFORMATION TILE
        placeInformation(finalWorldFrame, random, totalRooms);

        return currLoc;

    }

    public ArrayList<Character> inputToCharList(String input) {
        char[] arr = input.toCharArray();
        Character[] charObjectArr = new Character[arr.length];
        for (int i = 0; i < charObjectArr.length; i++) {
            charObjectArr[i] = new Character(arr[i]);
        }
        return new ArrayList<Character>(Arrays.asList(charObjectArr));
    }

    public void showMessage() {
        StdDraw.textLeft(1, HEIGHT - 3, msg);
    }

    public void changeMessage() {
        msg = Messages.Messages[RandomUtils.uniform(random, Messages.Messages.length)];
    }

    public void portal(){
        finalWorldFrame[currLoc[0]][currLoc[1]] = Tileset.NOTHING;
        // Room room = rooms.get(newRoomGen.nextInt(rooms.size()));
        currLoc = placePlayer(finalWorldFrame, random, rooms.size());
    }
}
