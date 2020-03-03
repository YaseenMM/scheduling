import java.util.Scanner;
import java.util.*;
import java.io.*;
import java.util.Arrays;

class lab2{
    public static void main (String[] args) throws FileNotFoundException {
        if (args.length < 1 || (args[0] == "--verbose" && args.length < 2)){
            System.out.println("No file was inputted...program will now terminate.");
            System.exit(1);
        };
        File text;
        if (args.length > 1){
            text = new File(args[1]);
        }
        else{
            text = new File(args[0]);
        };
        String input = "";
        String line;
        Scanner userIn = new Scanner(text);
        while (userIn.hasNextLine()){
            line = userIn.nextLine();
            input += line + "\n";
        };
        System.out.println(input);
    }
}