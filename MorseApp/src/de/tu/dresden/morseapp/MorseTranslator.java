package de.tu.dresden.morseapp;
import java.util.LinkedList;

/**
 * Created by simeon on 24.01.15.
 */

//for basics of the morse code see: http://de.wikipedia.org/wiki/Morsezeichen
public class MorseTranslator {
    
	public static MorseTranslator instance; 
	private Node root;

    private class Node{

        private Node point, line;
        String letter;

        public Node(String letter) {
            this.letter = letter;
        }

        public Node getPoint() {
            return point;
        }

        public void setPoint(Node point) {
            this.point = point;
        }

        public Node getLine() {
            return line;
        }

        public void setLine(Node line) {
            this.line = line;
        }

        public String getLetter() {
            return letter;
        }


    }

    public static MorseTranslator getInstance()
    {
    	if (instance == null)
    		instance = new MorseTranslator();
    	return instance;
    }
    
    private MorseTranslator() {
        initDecodeTree();
    }

    //a three for the binary-three search
    private void initDecodeTree(){
        root = new Node(null);

        root.setPoint(new Node("e"));
        root.setLine(new Node("t"));

        root.getPoint().setPoint(new Node("i"));
        root.getPoint().setLine(new Node("a"));
        root.getLine().setPoint(new Node("n"));
        root.getLine().setLine(new Node("m"));

        root.getPoint().getPoint().setPoint(new Node("s"));
        root.getPoint().getPoint().setLine(new Node("u"));
        root.getPoint().getLine().setPoint(new Node("r"));
        root.getPoint().getLine().setLine(new Node("w"));
        root.getLine().getPoint().setPoint(new Node("d"));
        root.getLine().getPoint().setLine(new Node("k"));
        root.getLine().getLine().setPoint(new Node("g"));
        root.getLine().getLine().setLine(new Node("o"));

        root.getPoint().getPoint().getPoint().setPoint(new Node("h"));
        root.getPoint().getPoint().getPoint().setLine(new Node("v"));
        root.getPoint().getPoint().getLine().setPoint(new Node("f"));
        root.getPoint().getPoint().getLine().setLine(new Node("ü"));
        root.getPoint().getLine().getPoint().setPoint(new Node("l"));
        root.getPoint().getLine().getPoint().setLine(new Node("ä"));
        root.getPoint().getLine().getLine().setPoint(new Node("p"));
        root.getPoint().getLine().getLine().setLine(new Node("j"));
        root.getLine().getPoint().getPoint().setPoint(new Node("b"));
        root.getLine().getPoint().getPoint().setLine(new Node("x"));
        root.getLine().getPoint().getLine().setPoint(new Node("c"));
        root.getLine().getPoint().getLine().setLine(new Node("y"));
        root.getLine().getLine().getPoint().setPoint(new Node("z"));
        root.getLine().getLine().getPoint().setLine(new Node("q"));
        root.getLine().getLine().getLine().setPoint(new Node("ö"));
        root.getLine().getLine().getLine().setLine(new Node(null));


        root.getLine().getLine().getLine().getLine().setLine(new Node("0"));
        root.getPoint().getLine().getLine().getLine().setLine(new Node("1"));
        root.getPoint().getPoint().getLine().getLine().setLine(new Node("2"));
        root.getPoint().getPoint().getPoint().getLine().setLine(new Node("3"));
        root.getPoint().getPoint().getPoint().getPoint().setLine(new Node("4"));
        root.getPoint().getPoint().getPoint().getPoint().setPoint(new Node("5"));
        root.getLine().getPoint().getPoint().getPoint().setPoint(new Node("6"));
        root.getLine().getLine().getPoint().getPoint().setPoint(new Node("7"));
        root.getLine().getLine().getLine().getPoint().setPoint(new Node("8"));
        root.getLine().getLine().getLine().getLine().setPoint(new Node("9"));

        root.getPoint().getPoint().getPoint().getLine().getLine().setPoint(new Node(null));
        root.getPoint().getPoint().getPoint().getLine().getLine().getPoint().setPoint(new Node("ß"));
        root.getPoint().getLine().getPoint().getLine().setPoint(new Node("+"));
        root.getPoint().getLine().getPoint().getLine().getPoint().setLine(new Node("."));
        root.getLine().getLine().getPoint().getPoint().setLine(new Node(null));
        root.getLine().getLine().getPoint().getPoint().getLine().setLine(new Node(","));
        root.getLine().getLine().getLine().getPoint().getPoint().setPoint(new Node(":"));
        root.getLine().getPoint().getLine().getPoint().setLine(new Node(null));
        root.getLine().getPoint().getLine().getPoint().getLine().setPoint(new Node(";"));
        root.getPoint().getPoint().getLine().getLine().setPoint(new Node(null));
        root.getPoint().getPoint().getLine().getLine().getPoint().setPoint(new Node("?"));
        root.getPoint().getPoint().getLine().getLine().getPoint().setLine(new Node("_"));
        root.getLine().getPoint().getPoint().getPoint().getPoint().setLine(new Node("-"));
        root.getLine().getPoint().getLine().getLine().setPoint(new Node("("));
        root.getLine().getPoint().getLine().getLine().getPoint().setLine(new Node(")"));
        root.getPoint().getLine().getLine().getLine().getLine().setPoint(new Node("'"));
        root.getLine().getPoint().getPoint().getPoint().setLine(new Node("="));
        root.getPoint().getLine().getLine().getPoint().setLine(new Node(null));
        root.getPoint().getLine().getLine().getPoint().getLine().setPoint(new Node("@"));
        root.getLine().getPoint().getPoint().getLine().setPoint(new Node("/"));

    }

    private String parseStringToMorse(char imput)
    {

        imput = Character.toUpperCase(imput);
        switch (imput)
        {
            case 'A': return ".-";
            case 'B': return "-...";
            case 'C': return "-.-.";
            case 'D': return "-..";
            case 'E': return ".";
            case 'F': return "..-.";
            case 'G': return "--.";
            case 'H': return "....";
            case 'I': return "..";
            case 'J': return ".---";
            case 'K': return "-.-";
            case 'L': return ".-..";
            case 'M': return "--";
            case 'N': return "-.";
            case 'O': return "---";
            case 'P': return ".--.";
            case 'Q': return "--.-";
            case 'R': return ".-.";
            case 'S': return "...";
            case 'T': return "-";
            case 'U': return "..-";
            case 'V': return "...-";
            case 'W': return ".--";
            case 'X': return "-..-";
            case 'Y': return "-.--";
            case 'Z': return "--..";
            case '0': return "-----";
            case '1': return ".----";
            case '2': return "..---";
            case '3': return "...--";
            case '4': return "....-";
            case '5': return ".....";
            case '6': return "-....";
            case '7': return "--...";
            case '8': return "---..";
            case '9': return "----.";
            case 'Ä': return ".-.-";
            case 'Ö': return "---.";
            case 'Ü': return "..--";
            case 'ß': return "...--..";
            case '.': return ".-.-.-";
            case ',': return "--..--";
            case ':': return "---...";
            case ';': return "-.-.-.";
            case '?': return "..--..";
            case '-': return "-....-";
            case '_': return "..--.-";
            case '(': return "-.--.";
            case ')': return "-.--.-";
            case 39: return ".----."; //for '
            case '=': return "-...-";
            case '+': return ".-.-.";
            case '/': return "-..-.";
            case '@': return ".--.-.";

            default: return "..--..";
        }
    }

    //insert a String and return a List of strings
    //every String represent a Letter, so have to take 3 "tick" pause between every letter
    //in a string there are . (1 tick), -(3 tick) and / (1 tick pause)
    //if a letter is unknown, you get back the morse code of ?
    //with the function LinkedList<String>.removeFirst() you get the first letter of the message
    public LinkedList<String> stringToMorse(String inputString)
    {
        LinkedList<String> fifo = new LinkedList<String>();
        String[] words = inputString.split(" ");
        for(int i = 0; i < words.length; ++i){
            for(int j = 0; j < words[i].length(); ++j){
                fifo.add( parseStringToMorse(words[i].charAt(j)));
            }
            fifo.add("/");
        }
        fifo.removeLast();
        return fifo;
    }

    //convert a LinkedList of morse-code to a String
    //you have to look, that every string is a letter
    //blanks between words have to represent, with a String with the value "/"
    //unknown letters will represented with a ?
    public String morseToString(LinkedList<String> imputList)
    {
        String result = "";
        Node current_point = root;
        for(int i = 0; i < imputList.size(); ++i){
            current_point = root;
            if(imputList.get(i).equals("/"))
            {
                result += " ";
            }
            else
            {
                for(int j = 0; j < imputList.get(i).length(); ++j)
                {
                    if(imputList.get(i).charAt(j) == '.' )
                    {
                        if(current_point.getPoint() != null)
                        {
                            current_point = current_point.getPoint();
                        }
                        else
                        {
                            result += "?";
                            break;
                        }
                    }
                    else
                    {
                        if(current_point.getLine() != null)
                        {
                            current_point = current_point.getLine();
                        }
                        else
                        {
                            result += "?";
                            break;
                        }
                    }
                }
                if(current_point.getLetter() != null)
                {
                    result += current_point.getLetter();
                }
                else
                {
                    result += "?";
                }
            }
        }


        return result;
    }
}
