package byow.Core;

import byow.TileEngine.TETile;

import java.util.Arrays;

/**
 * This is the main entry point for the program. This class simply parses
 * the command line inputs, and lets the byow.Core.Engine class take over
 * in either keyboard or input string mode.
 */
public class Main {
    public static void main(String[] args) {
        Engine engine = new Engine();
        TETile[][] world1 = engine.interactWithInputString("n7193300625454684331saaawasdaawd:q");
        engine.interactWithInputString("lwsd");
        engine.interactWithInputString("N");
        engine.interactWithKeyboard();
        //System.out.println(engine.toString());
    }
}
