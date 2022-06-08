
import java.io.*;
import java.util.*;
/**
 *
 * 
 */
public class Parser {
    private static Token t;			// current token (recently recognized)
    private static Token la;                    // lookahead token
    private static int sym;			// always contains la.kind
    private static int errors = 0;
    private static int errDist = 0;

    private static BitSet exprStart, statStart, sux;

    private static final int
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

    private static String[] TokenName = {
        "none",
        "ident ",
        "number ",
        "charCon ",
        "+",
        "-",
        "*",
        "/",
        "%",
        "==",
        "!=",
        "<",
        "<=",
        ">",
        ">=",
        "&&",
        "||",
        "=",
        "++",
        "--",
        ";",
        ",",
        ".",
        "(",
        ")",
        "[",
        "]",
        "{",
        "}",
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
        "eof"
    };

    private static void scan() {
        t = la;
        la = Scanner.next();
        sym = la.kind;
        errDist++;
    }

    private static void check(int expected, BitSet sux) {
        if (sym == expected) scan();
        else error(TokenName[expected] + " expected", sux);
    }

    public static void error(String msg, BitSet sux) {
        if(errDist >= 5){
            System.out.println("-- line " + la.line + " col " + la.col + ": " + msg);
            errors++;
        }
        while(!sux.get(sym))
            scan();
        errDist = 0;
    }

    public static BitSet Add(BitSet a, BitSet b){
        BitSet c = (BitSet)a.clone();
        c.or(b);
        return c;
    }

    private static void Program(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(final_); next.set(class_);
        next.set(lbrace); next.set(void_); next.set(rbrace);
        check(program_, Add(sux, next));
        check(ident, Add(sux, next));
        for(;;){
            if(sym == final_) ConstDecl(Add(sux, next));
            else if(sym == ident) VarDecl(Add(sux, next));
            else if(sym == class_) ClassDecl(Add(sux, next));
            else if(sym == lbrace || sym == void_ || sym == rbrace || sux.get(sym))
                break;
            else
                error("neispravan program ", Add(sux, next));
        }
        next.clear(final_); next.clear(lbrace); next.clear(class_);
        check(lbrace, Add(sux, next));
        for(;;){
            if(sym == void_ || sym == ident)
                MethodDecl(Add(sux, next));
            else if(sym == rbrace || sux.get(sym))
                break;
            else
                error("neispravan program", Add(sux, next));
        }
        check(rbrace, sux);
    }

    private static void ConstDecl(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(assign); next.set(number); next.set(charCon); next.set(semicolon);
        check(final_, Add(sux, next));
        Type(Add(sux, next));
        next.clear(ident);
        check(ident, Add(sux, next));
        next.clear(assign);
        check(assign, Add(sux, next));
        if(sym != number && sym != charCon)
            error("Greska u CD", Add(sux, next));
        if(sym == number || sym == charCon)
            scan();
        check(semicolon, sux);
    }

    private static void VarDecl(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(comma); next.set(semicolon);
        Type(Add(sux, next));
        next.clear(ident);
        check(ident, Add(sux, next));
        for(;;){
            if(sym == comma){
                scan();
                check(ident, Add(sux, next));
            }else if(sym == semicolon || sux.get(sym))
                break;
            else
                error("Greska u VD", Add(sux, next));
        }
        check(semicolon, sux);
    }

    private static void ClassDecl(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(lbrace); next.set(rbrace);
        check(class_, Add(sux, next));
        check(ident, Add(sux, next));
        next.clear(lbrace);
        check(lbrace, Add(sux, next));
        for(;;){
            if(sym == ident)
                VarDecl(Add(sux, next));
            else if(sym == semicolon || sux.get(sym))
                break;
            else
                error("Greska u ClassD", Add(sux, next));
        }
        check(rbrace, sux);
    }

    private static void MethodDecl(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(lpar); next.set(rpar); next.set(lbrace);
        if (sym != ident && sym != void_) {
        	error("Greska u MD", Add(sux, next));
        }
        if(sym == ident)
            Type(Add(sux, next));
        else 
        	check(void_, Add(sux, next));
        check(ident, Add(sux, next));
        next.clear(lpar);
        check(lpar, Add(sux, next));
        if(sym != ident && !next.get(sym))
            error("Greska u MD", Add(sux, next));
        if(sym == ident)
            FormPars(Add(sux, next));
        next.clear(rpar);
        check(rpar, Add(sux, next));
        for(;;){
            if(sym == ident)
                VarDecl(Add(sux, next));
            else if(sym == lbrace || sux.get(sym))
                break;
            else
                error("Greska u MD", Add(sux, next));
        }
        Block(sux);
    }

    private static void FormPars(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(comma);
        Type(Add(sux, next));
        check(ident, Add(sux, next));
        for(;;){
            if(sym == comma){
                scan();
                Type(Add(sux, next));
                check(ident, Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u FP", Add(sux, next));
        }
    }

    private static void Type(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(lbrack);
        check(ident, Add(sux, next));
        if(sym != lbrack && !sux.get(sym))
            error("Greska u Type", Add(sux, next));
        if(sym == lbrack){
            scan();
            check(rbrack, sux);
        }
    }

    private static void Block(BitSet sux) {
        BitSet next = new BitSet(64);
        next = Add(next, statStart); next.set(rbrace);
        check(lbrace, Add(sux, next));
        for(;;){
            if(statStart.get(sym))
                Statement(Add(sux, next));
            else if(sym == rbrace || sux.get(sym))
                break;
            else
                error("Greska u Block", Add(sux, next));
        }
        check(rbrace, sux);
    }

    private static void Statement(BitSet sux) {
        if(!statStart.get(sym))
            error("Greska u Statement", Add(sux, statStart));
        if(sym == ident){
            BitSet next = new BitSet(64);
            next.set(assign); next.set(lpar); next.set(pplus); next.set(mminus); next.set(semicolon);
            Designator(Add(sux, next));
            if(sym != assign && sym != lpar && sym != pplus && sym != mminus)
                error("Greska u Statement", Add(sux, next));
            if(sym == assign){
                scan();
                next.clear(assign); next.clear(lpar); next.clear(pplus); next.clear(mminus);
                Expr(Add(sux, next));
            }else if(sym == lpar){
                scan();
                next.clear(assign); next.clear(lpar); next.clear(pplus); next.clear(mminus);
                next.set(rpar);
                if(!exprStart.get(sym) && !next.get(sym))
                    error("Greska u Statement1", Add(statStart, Add(sux, next)));
                if(exprStart.get(sym))
                    ActPars(Add(sux, next));
                next.clear(rpar);
                check(rpar, Add(sux, next));
            }else if(sym == pplus || sym == mminus)
                scan();
            check(semicolon, sux);
        }else if(sym == if_){
            BitSet next = new BitSet(64);
            next.set(lpar); next = Add(next, exprStart); next.set(rpar); next = Add(next, statStart); next.set(else_);
            scan();
            check(lpar, Add(sux, next));
            next.clear(minus); next.clear(number); next.clear(charCon); next.clear(new_); next.clear(lpar);
            Condition(Add(sux, next));
            next.clear(rpar);
            check(rpar, Add(sux, next));
            next.clear(); next.set(else_);
            Statement(Add(sux, next));
            if(sym != else_ && !sux.get(sym))
                error("Greska u Statement3", Add(sux, next));
            if(sym == else_){
                scan();
                Statement(sux);
            }
        }else if(sym == while_){
            BitSet next = new BitSet(64);
            next.set(lpar); next = Add(next, exprStart); next.set(rpar); next = Add(next, statStart);
            scan();
            check(lpar, Add(sux, next));
            next.clear(minus); next.clear(number); next.clear(charCon); next.clear(new_); next.clear(lpar);
            Condition(Add(sux, next));
            next.clear(rpar);
            check(rpar, Add(sux, next));
            Statement(sux);
        }else if(sym == break_){
            scan();
            check(semicolon, sux);
        }else if(sym == return_){
            BitSet next = new BitSet(64);
            next = Add(next, exprStart); next.set(semicolon);
            scan();
            if(!exprStart.get(sym) && sym != semicolon)
                error("Greska u Statement4", Add(sux, next));
            next.clear(); next.set(semicolon);
            if(exprStart.get(sym))
                Expr(Add(sux, next));
            check(semicolon, sux);
        }else if(sym == read_){
            BitSet next = new BitSet(64);
            next.set(ident); next.set(rpar); next.set(semicolon);
            scan();
            check(lpar, Add(sux, next));
            next.clear(ident);
            Designator(Add(sux, next));
            next.clear(rpar);
            check(rpar, Add(sux, next));
            check(semicolon, sux);
        }else if(sym == print_){
            BitSet next = new BitSet(64);
            next = Add(next, exprStart); next.set(comma);
            next.set(rpar); next.set(semicolon);
            scan();
            check(lpar, Add(sux, next));
            next.clear(); next.set(comma); next.set(rpar); next.set(semicolon);
            Expr(Add(sux, next));
            if(sym != comma && sym != rpar)
                error("Greska u Statement5", Add(sux, next));
            if(sym == comma){
                scan();
                next.clear(comma);
                check(number, Add(sux, next));
            }
            next.clear(rpar);
            check(rpar, Add(sux, next));
            check(semicolon, sux);
        }else if(sym == lbrace){
            Block(sux);
        }else if(sym == semicolon)
            scan();
    }

    private static void ActPars(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(comma);
        Expr(Add(sux, next));
        for(;;){
            if(sym == comma){
                scan();
                Expr(Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u ActualPars", Add(sux, next));
        }
    }

    private static void Condition(BitSet sux){
        BitSet next = new BitSet(64);
        next.set(or);
        CondTerm(Add(sux, next));
        for(;;){
            if(sym == or){
                scan();
                CondTerm(Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u Condition", Add(sux, next));
        }
    }

    private static void CondTerm(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(and);
        CondFact(Add(sux, next));
        for(;;){
            if(sym == and){
                scan();
                CondFact(Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u CondTerm", Add(sux, next));
        }
    }

    private static void CondFact(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(eql); next.set(neq); next.set(gtr); next.set(geq);
        next.set(lss); next.set(leq); next = Add(next, exprStart);
        Expr(Add(sux, next));
        Relop(Add(sux, exprStart));
        Expr(sux);
    }

    private static void Relop(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(eql); next.set(neq); next.set(gtr); next.set(geq); next.set(lss); next.set(leq);
        if(sym != eql && sym != neq && sym != gtr && sym != geq && sym != lss && sym != leq)
            error("Missing relop", Add(sux, next));
        if(sym == eql || sym == neq || sym == gtr || sym == geq || sym == lss || sym == leq)
            scan();
    }

    private static void Expr(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(number); next.set(charCon); next.set(new_);
        next.set(lpar);
        if(sym != minus && !next.get(sym))
            error("Greska u Expr", Add(sux, next));
        if(sym == minus)
            scan();
        next.clear(); next.set(plus); next.set(minus);
        Term(Add(sux, next));
        for(;;){
            if(sym == plus || sym == minus){
                scan();
                Term(Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u Expr", Add(sux, next));

        }
    }

    private static void Term(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(times); next.set(slash); next.set(rem);
        Factor(Add(sux, next));
        for(;;){
            if(sym == times || sym == slash || sym == rem){
                scan();
                Factor(Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u Term", Add(sux, next));
        }
    }

    private static void Factor(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(ident); next.set(number); next.set(charCon); next.set(new_); next.set(lpar);
        if(!next.get(sym))
            error("Greska u Factor", Add(sux, next));
        if(sym == ident){
            next.clear(); next.set(lpar);
            Designator(Add(sux, next));
            if(sym != lpar && !sux.get(sym))
                error("Greska u Factor1", Add(sux, next));
            if(sym == lpar){
                scan();
                next.clear(lpar);
                next.set(rpar);
                if(!exprStart.get(sym) && !next.get(sym))
                    error("Greska u Factor2", Add(exprStart, Add(sux, next)));
                if(exprStart.get(sym))
                    Expr(Add(sux, next));
                check(rpar, sux);
            }
        }else if(sym == number || sym == charCon){
            scan();
        }else if(sym == new_){
            next.clear();
            next.set(lbrack);
            scan();
            check(ident, Add(sux, next));
            if(sym != lbrack && !sux.get(sym))
                error("Greska u Factor3", Add(sux, next));
            if(sym == lbrack){
                scan();
                next.clear(lbrack);
                next.set(rbrack);
                Expr(Add(next, sux));
                check(rbrack, sux);
            }
        }else if(sym == lpar){
            next.clear();
            next.set(rpar);
            scan();
            Expr(Add(sux, next));
            check(rpar, sux);
        }
    }

    private static void Designator(BitSet sux) {
        BitSet next = new BitSet(64);
        next.set(period); next.set(lbrack);
        check(ident, Add(sux, next));
        for(;;){
            if(sym == period){
                scan();
                check(ident, Add(sux, next));
            }else if(sym == lbrack){
                scan();
                next.set(rbrack);
                Expr(Add(sux, next));
                next.clear(rbrack);
                check(rbrack, Add(sux, next));
            }else if(sux.get(sym))
                break;
            else
                error("Greska u Designator", Add(sux, next));
        }
    }

    public static void parse() {
        BitSet s;
        s = new BitSet(64); exprStart = s;
        s.set(ident); s.set(number); s.set(charCon); s.set(new_); s.set(lpar); s.set(minus);

        s = new BitSet(64); statStart = s;
        s.set(ident); s.set(if_); s.set(while_); s.set(break_); s.set(return_);
        s.set(read_); s.set(print_); s.set(lbrace); s.set(semicolon);

        // start parsing
        scan();

        sux = new BitSet(64);
        sux.set(eof);

        Program(sux);

        if (errors > 0)
            System.out.println(errors + " gresaka");
        else
            System.out.println("Parsiranje uspesno");
    }

    public static void main(String[] args) {
        Token t;
        String path = "C:\\Users\\Public\\sample.txt";
        try {
            Scanner.init(new InputStreamReader(new FileInputStream(path)));
            Parser.parse();
        }catch(IOException e) {
            System.out.println("-- cannot open input file " + path);
        }
    }
}