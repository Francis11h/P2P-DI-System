package utils;

public class Debug {

    private boolean debugMode;

    public Debug(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void println(String s) {

        if(debugMode)
            System.out.println(s);

    }

    public void print(String s) {

        if(debugMode)
            System.out.print(s);

    }

}
