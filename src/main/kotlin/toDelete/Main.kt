package toDelete//import parser.Parser
import parser.RecursiveParser
import tokenizer.TokenType
import tokenizer.Tokenizer
import tokenizer.TokensSource
import java.io.File
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    //val p1 = Parser()
    val tokenizer = Tokenizer()
    val s = """
//class Factorial{
//    public static void main(String[] a){
//    Fac f;
//    int r;
//	 f = new Fac();
//    r= f.ComputeFac(10);
//    }
//}
//
//class Fac {
//    public int ComputeFac(int num){
//	int num_aux ;
//	if (num < 1)
//	    num_aux = 1 ;
//	else 
//	    num_aux = num * (this.ComputeFac(num-1)) ;
//	return num_aux ;
//    }
//}
(true) && (true) 
    """.trimIndent()
    val q = File("""D:\Kotlin\factorial.java""").readText()
//    var time =measureTimedValue{
//        val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//        println(tokens1)
//    }
//    println(time)
//    time =measureTimedValue{
//        val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//        println(tokens1)
//    }
//    println(time)
//    time =measureTimedValue{
//        val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//        p1.parse(tokenizer.TokensSource(tokens1), Grammar.Program)
//        println(tokens1)
//    }
//    println(time)
//    time =measureTimedValue{
//        val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//        p1.parse(tokenizer.TokensSource(tokens1), Grammar.Program)
//        println(tokens1)
//    }
//    println(time)
//    var time =measureTimedValue{
//        val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace && t.type!=TokenType.Comment }.toTypedArray()
//        val ts = TokensSource(tokens1)
//        val p2 = RecursiveParser(ts)
//        val parseExpression = p2.parse()
//        println(parseExpression)
//    }


    //val q = "public int inc(int c){ if(c<1){c=c+1;}else{c=c-1;} return c+1;} public int inc(int c){ if(c<1){c=c+1;}else{c=c-1;} return c+1;} }"
//    val tokens1 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//    val tokens2 = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
//    p1.parse(tokenizer.TokensSource(tokens1), Grammar.Program)
//    val parseExpression = p2.parse(tokenizer.TokensSource(tokens2))
//    println(parseExpression)
//    for (token in tokens){
//        if (token.type!= TokenType.Whitespace) {
//            println(token)
//        }
//    }
}

//inline fun <T> trywr(selector: () -> T, block: (T) -> Unit) {
//    val selector1 = selector()
//    selector() = 2
//}


/*
ID:
  identifier ::= letter ( letter | digit | '_' )*
IL:
  integer-literal ::= digit+

  letter ::= 'a'-'z' | 'A'-'Z'

  digit ::= '0'-'9'
 */