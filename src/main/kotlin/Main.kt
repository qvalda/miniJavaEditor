import parser.Parser
import tokenizer.Token
import tokenizer.TokenType
import tokenizer.Tokenizer
import java.io.File

fun main(args: Array<String>) {
    val p = Parser()
    val tokenizer = Tokenizer()
    val s = """
class Factorial{
    public static void main(String[] a){
    Fac f;
    int r;
	 f = new Fac();
    r= f.ComputeFac(10);
    }
}

class Fac {
    public int ComputeFac(int num){
	int num_aux ;
	if (num < 1)
	    num_aux = 1 ;
	else 
	    num_aux = num * (this.ComputeFac(num-1)) ;
	return num_aux ;
    }
}
    """.trimIndent()
    val q = File("""D:\Kotlin\bigInput.txt""").readText()
    val tokens = tokenizer.getTokens(q).filter { t->t.type!= TokenType.Whitespace }.toTypedArray()
    p.parse(TokensSource(tokens))

//    for (token in tokens){
//        if (token.type!= TokenType.Whitespace) {
//            println(token)
//        }
//    }
}

class TokensSource(private val tokens : Array<Token>) {
    private var index = 0;

    fun accept() {
        index++
    }

    fun isEOF(): Boolean {
        return index >= tokens.size //|| tokens[index] == Token.EOF
    }

    val currentToken: Token
        get() {
            return tokens[index]
        }
    val nextToken: Token
        get() {
            return tokens[index + 1]
        }
    val nextToken2: Token
        get() {
            return tokens[index + 2]
        }
}



/*
ID:
  identifier ::= letter ( letter | digit | '_' )*
IL:
  integer-literal ::= digit+

  letter ::= 'a'-'z' | 'A'-'Z'

  digit ::= '0'-'9'
 */