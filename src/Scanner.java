
import java.io.*;

/**
 *
 * 
 */

public class Scanner {
    private static Reader in;
    private static char ch;
    public static int line,col;
    private static final int eofCh = '\u0080';
    private static final char eol = '\n';
    private static char[] lex;

    static final int
        none = 0,
        ident = 1,
        number = 2,
        charCon = 3,
        plus = 4,       /* + */
        minus = 5,      /* - */
        times = 6,      /* * */
        slash = 7,      /* / */
        rem = 8,        /* % */
        eql = 9,        /* == */
        neq = 10,       /* != */
        lss = 11,       /* < */
        leq = 12,       /* <= */
        gtr = 13,       /* > */
        geq = 14,       /* >= */
        and = 15,       /* && */
        or = 16,        /* || */
        assign = 17,    /* = */
        pplus = 18,     /* ++ */
        mminus = 19,    /* -- */
        semicolon = 20, /* ; */
        comma = 21,     /* , */
        period = 22,    /* . */
        lpar = 23,      /* ( */
        rpar = 24,      /* ) */
        lbrack = 25,    /* [ */
        rbrack = 26,    /* ] */
        lbrace = 27,    /* { */
        rbrace = 28,    /* } */
        break_ = 29,
        class_ = 30,
        else_ = 31,
        final_ = 32,
        if_ = 33,
        new_ = 34,
        print_ = 35,
        program_ = 36,
        read_ = 37,
        return_ = 38,
        void_ = 39,
        while_ = 40,
        eof = 41;

    private static String[] key = new String[]{
        "break",
        "class",
        "else",
        "final",
        "if",
        "new",
        "print",
        "program",
        "read",
        "return",
        "void",
        "while",
    };

    private static int[] keyVal = new int[]{
        break_,
        class_,
        else_,
        final_,
        if_,
        new_,
        print_,
        program_,
        read_,
        return_,
        void_,
        while_,
    };

    private static void nextCh() {
        try{
            ch = (char)in.read(); col++;
            if (ch == '\n'){line++; col = 0;}
            else if (ch == '\uffff') ch = eofCh;
        }catch(IOException e){
            ch = eofCh;
        }
    }

    public static Token next() {
        while(ch<=' ') nextCh();
        Token t = new Token();
        t.line = line; t.col=col; int i=0;
        switch(ch) {
            case 'a' : case 'b' : case 'c' : case 'd' : case 'e' : case 'f' : case 'g' : case 'h' : case 'i' : case 'j' : case 'k' : case 'l' : case 'm' :
            case 'n' : case 'o' : case 'p' : case 'q' : case 'r' : case 's' : case 't' : case 'u' : case 'v' : case 'w' : case 'x' : case 'y' : case 'z' :
            case 'A' : case 'B' : case 'C' : case 'D' : case 'E' : case 'F' : case 'G' : case 'H' : case 'I' : case 'J' : case 'K' : case 'L' : case 'M' :
            case 'N' : case 'O' : case 'P' : case 'Q' : case 'R' : case 'S' : case 'T' : case 'U' : case 'V' : case 'W' : case 'X' : case 'Y' : case 'Z' :
                do {
                    lex[i++] = ch;
                    nextCh();
                }while('a'<= ch && ch<='z' || 'A' <= ch && ch<='Z' || '0' <=ch && ch<='9');
                t.string = new String (lex, 0, i);
                t.kind = keyword(t.string);
                break;
            case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
                do{
                    lex[i++] = ch;
                    nextCh();
                }while(ch >= '0' && ch <= '9');
                String help = new String(lex, 0, i);
                if (inRange(help)){
                    t.val = Integer.parseInt(help);
                    t.kind = number;
                }else{
                    t.kind = none;
                    System.err.println("Error! Line: " + line + " Col: " + col + " Type: Integer constant is to large!");
                }
                break;
            case '\'':
                t.kind = charCon;
                nextCh();
                if (' ' <= ch && ch <= '~'){
                    t.val = (int)ch;
                    nextCh();
                    if(ch == '\''){
                        nextCh();
                    }else{
                        t.kind = none;
                        System.err.println("Error! Line: " + line + " Col: " + col + " Type: Character constant has missing quote!");
                    }
                }else{
                    t.kind = none;
                    System.err.println("Error! Line: " + line + " Col: " + col + " Type: Invalid character constant!");
                }
                break;
            case '+':
                nextCh();
                if (ch == '+'){
                    t.kind = pplus;
                    nextCh();
                }else{
                    t.kind = plus;
                }
                break;
            case '-':
                nextCh();
                if (ch == '-'){
                    t.kind = mminus;
                    nextCh();
                }else{
                    t.kind = minus;
                }
                break;
            case '*':
                t.kind = times;
                nextCh();
                break;
            case '/':
                nextCh();
                if (ch == '/'){
                    do{
                        nextCh();
                    }while (ch != '\n' && ch != eofCh);
                    t = next();
                }else{
                    t.kind = slash;
                }
                break;
            case '%':
                t.kind = rem;
                nextCh();
                break;
            case '=':
                nextCh();
                if (ch == '='){
                    t.kind = eql;
                    nextCh();
                }else{
                    t.kind = assign;
                }
                break;
            case '!':
                nextCh();
                if (ch == '='){
                    t.kind = neq;
                    nextCh();
                }else{
                    t.kind = none;
                }
                break;
            case '<':
                nextCh();
                if (ch == '='){
                    t.kind = leq;
                    nextCh();
                }else{
                    t.kind = lss;
                }
                break;
            case '>':
                nextCh();
                if (ch == '='){
                    t.kind = geq;
                    nextCh();
                }else{
                    t.kind = gtr;
                }
                break;
            case '&':
                nextCh();
                if (ch == '&'){
                    t.kind = and;
                    nextCh();
                }else{
                    t.kind = none;
                }
                break;
            case '|':
                nextCh();
                if (ch == '|'){
                    t.kind = or;
                    nextCh();
                }else{
                    t.kind = none;
                }
                break;
            case ';':
                t.kind = semicolon;
                nextCh();
                break;
            case ',':
                t.kind = comma;
                nextCh();
                break;
            case '.':
                t.kind = period;
                nextCh();
                break;
            case '(':
                t.kind = lpar;
                nextCh();
                break;
            case ')':
                t.kind = rpar;
                nextCh();
                break;
            case '[':
                t.kind = lbrack;
                nextCh();
                break;
            case ']':
                t.kind = rbrack;
                nextCh();
                break;
            case '{':
                t.kind = lbrace;
                nextCh();
                break;
            case '}':
                t.kind = rbrace;
                nextCh();
                break;
            case eofCh:
                t.kind = eof;
                break;
            default:
                t.kind = none;
                System.err.println("Error! Line: " + line + " Col: " + col + " Type: Invalid character!");
                nextCh();
                break;
        }
        return t;
    }

    private static int keyword(String S) {
        int i = 0 ; int j = key.length -1;
        int k, d;
        while ( i<=j) {
            k=(i+j)/2;
            d=S.compareTo(key[k]);
            if (d<0) j=k-1;
            else if (d>0) i=k+1;
            else return keyVal[k];
        }
        return ident;
    }

    private static boolean inRange(String s){
        boolean ok = true;
        int pom = 0;
        try{
            pom = Integer.parseInt(s);
        }catch (Exception ex){
            ok = false;
        }
        return ok;
    }

    public static void init (Reader r) {
        in = r;
        lex = new char [64];
        line = 1; col = 0;
        nextCh();
    }
}