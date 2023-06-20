package tokenizer

enum class TokenType {
    NotDefined,
    EOF,
    InvalidSyntax,

    Whitespace,
    Comment,
    NameIdentifier,

    BracketRoundOpen,
    BracketRoundClose,
    BracketSquareOpen,
    BracketSquareClose,
    BracketCurlyOpen,
    BracketCurlyClose,

    SymbolDot,
    SymbolComma,
    SymbolSemicolon,
    SymbolColon,

    OperatorEquals,
    OperatorNotEquals,
    OperatorLessOrEqual,
    OperatorMoreOrEqual,
    OperatorDecrement,
    OperatorIncrement,
    OperatorMinusAssign,
    OperatorPlusAssign,
    OperatorMultAssign,
    OperatorDivAssign,
    OperatorAnd,
    OperatorOr,
    OperatorNot,
    OperatorAssign,
    OperatorLess,
    OperatorMore,
    OperatorPlus,
    OperatorMinus,
    OperatorDiv,
    OperatorMult,

    LiteralString,
    LiteralNumber,
    LiteralChar,
    LiteralTrue,
    LiteralFalse,
    LiteralNull,

    KeyWordAbstract,
    KeyWordContinue,
    KeyWordFor,
    KeyWordNew,
    KeyWordSwitch,
    KeyWordAssert,
    KeyWordDefault,
    KeyWordGoto,
    KeyWordPackage,
    KeyWordSynchronized,
    KeyWordBoolean,
    KeyWordDo,
    KeyWordIf,
    KeyWordPrivate,
    KeyWordThis,
    KeyWordBreak,
    KeyWordDouble,
    KeyWordImplements,
    KeyWordProtected,
    KeyWordThrow,
    KeyWordByte,
    KeyWordElse,
    KeyWordImport,
    KeyWordPublic,
    KeyWordThrows,
    KeyWordCase,
    KeyWordEnum,
    KeyWordInstanceof,
    KeyWordReturn,
    KeyWordTransient,
    KeyWordCatch,
    KeyWordExtends,
    KeyWordInt,
    KeyWordShort,
    KeyWordTry,
    KeyWordChar,
    KeyWordFinal,
    KeyWordInterface,
    KeyWordStatic,
    KeyWordVoid,
    KeyWordClass,
    KeyWordFinally,
    KeyWordLong,
    KeyWordStrictfp,
    KeyWordVolatile,
    KeyWordConst,
    KeyWordFloat,
    KeyWordNative,
    KeyWordSuper,
    KeyWordWhile,
    KeyWordString,
}