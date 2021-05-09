import java.util.ArrayList;
import java.util.List;

public class SymbolTable {

  // Creates a HashMap with a String and TokenEnum
  static List<Symbol> symbolTable = new ArrayList<Symbol>();

  /**
   * Constructor responsible to initialize the SymbolTable with the lexemes and
   * tokens
   */
  SymbolTable() {
    symbolTable.add(new Symbol("final", TokenEnum.FINAL));
    symbolTable.add(new Symbol("int", TokenEnum.INT, TypeEnum.INTEGER));
    symbolTable.add(new Symbol("char", TokenEnum.CHAR, TypeEnum.CHARACTER));
    symbolTable.add(new Symbol("boolean", TokenEnum.BOOLEAN, TypeEnum.LOGIC));
    symbolTable.add(new Symbol("if", TokenEnum.IF));
    symbolTable.add(new Symbol("for", TokenEnum.FOR));
    symbolTable.add(new Symbol("TRUE", TokenEnum.TRUE, TypeEnum.LOGIC, KindEnum.CONST));
    symbolTable.add(new Symbol("FALSE", TokenEnum.FALSE, TypeEnum.LOGIC, KindEnum.CONST));
    symbolTable.add(new Symbol("else", TokenEnum.ELSE));
    symbolTable.add(new Symbol("and", TokenEnum.AND));
    symbolTable.add(new Symbol("or", TokenEnum.OR));
    symbolTable.add(new Symbol("not", TokenEnum.NOT));
    symbolTable.add(new Symbol("then", TokenEnum.THEN));
    symbolTable.add(new Symbol("readln", TokenEnum.READLN));
    symbolTable.add(new Symbol("write", TokenEnum.WRITE));
    symbolTable.add(new Symbol("writeln", TokenEnum.WRITELN));
    symbolTable.add(new Symbol("main", TokenEnum.MAIN));
    symbolTable.add(new Symbol(":=", TokenEnum.ATTRIBUTION));
    symbolTable.add(new Symbol("=", TokenEnum.EQUAL));
    symbolTable.add(new Symbol("(", TokenEnum.OPEN_PARENTHESES));
    symbolTable.add(new Symbol(")", TokenEnum.CLOSE_PARENTHESES));
    symbolTable.add(new Symbol("<", TokenEnum.LESS_THAN));
    symbolTable.add(new Symbol(">", TokenEnum.GREATER_THAN));
    symbolTable.add(new Symbol("<>", TokenEnum.NOT_EQUAL));
    symbolTable.add(new Symbol(">=", TokenEnum.GREATER_OR_EQUAL));
    symbolTable.add(new Symbol("<=", TokenEnum.LESS_OR_EQUAL));
    symbolTable.add(new Symbol(",", TokenEnum.COMMA));
    symbolTable.add(new Symbol("+", TokenEnum.PLUS));
    symbolTable.add(new Symbol("-", TokenEnum.MINUS));
    symbolTable.add(new Symbol("*", TokenEnum.MULTIPLY));
    symbolTable.add(new Symbol("/", TokenEnum.DIVIDE));
    symbolTable.add(new Symbol(";", TokenEnum.SEMICOLON));
    symbolTable.add(new Symbol("{", TokenEnum.OPEN_BRACES));
    symbolTable.add(new Symbol("}", TokenEnum.CLOSE_BRACES));
    symbolTable.add(new Symbol("%", TokenEnum.PERCENTAGE));
    symbolTable.add(new Symbol("[", TokenEnum.OPEN_BRACKETS));
    symbolTable.add(new Symbol("]", TokenEnum.CLOSE_BRACKETS));
  }

  /**
   * Insert a new symbol with lexeme and token in the SymbolTable
   * 
   */
  public void insertSymbolTable(String lexeme, TokenEnum token) {
    symbolTable.add(new Symbol(lexeme, token));
  }

  /**
   * Insert a new symbol with lexeme, token and kind in the SymbolTable
   * 
   */
  public void insertSymbolTable(String lexeme, TokenEnum token, KindEnum kind) {
    symbolTable.add(new Symbol(lexeme, token, kind));
  }

  /**
   * Insert a new symbol with lexeme, token and type in the SymbolTable
   * 
   */
  public void insertSymbolTable(String lexeme, TokenEnum token, TypeEnum type) {
    symbolTable.add(new Symbol(lexeme, token, type));
  }

  /**
   * Search and returns true if the lexeme exists in the SymbolTable
   * 
   */
  public Boolean searchInSymbolTable(String lexeme) {
    Boolean result = false;

    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        result = true;
      }
    }

    return result;
  }

  /**
   * @return the token of the current lexeme
   * 
   */
  public static TokenEnum getToken(String lexeme) {
    TokenEnum token = null;

    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        token = symbol.getToken();
      }
    }

    return token;
  }

  /**
   * @return the kind of the current lexeme
   * 
   */
  public static TypeEnum getType(String lexeme) {
    TypeEnum type = null;

    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        type = symbol.getType();
      }
    }

    return type;
  }

  /**
   * @return the kind of the current lexeme
   * 
   */
  public static KindEnum getKind(String lexeme) {
    KindEnum kind = null;

    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        kind = symbol.getKind();
      }
    }

    return kind;
  }

  /**
   * @return the kind of the current lexeme
   * 
   */
  public static int getSize(String lexeme) {
    int size = 0;

    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        size = symbol.getSize();
      }
    }

    return size;
  }

  /**
   * Set the kind of the current lexeme
   * 
   */
  public static void setKind(String lexeme, KindEnum kind) {
    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        symbol.setKind(kind);
      }
    }
  }

  /**
   * Set the type of the current lexeme
   * 
   */
  public static void setType(String lexeme, TypeEnum type) {
    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        symbol.setType(type);
      }
    }
  }

  /**
   * Set the type of the current lexeme
   * 
   */
  public static void setSize(String lexeme, int size) {
    for (Symbol symbol : symbolTable) {
      if (symbol.getLexeme().equals(lexeme)) {
        symbol.setSize(size);
      }
    }
  }

  /**
   * Prints in the screen all the symbols of the SymbolTable
   * 
   */
  public void printSymbolTable() {
    System.out.println("--------------------------");
    System.out.println("TABELA DE SIMBOLOS");
    System.out.println("--------------------------");
    for (Symbol symbol : symbolTable) {
      TokenEnum token = symbol.getToken();
      String lexeme = symbol.getLexeme().toString();
      TypeEnum type = symbol.getType();
      KindEnum kind = symbol.getKind();

      System.out.println("Token: " + token);
      System.out.println("Lexeme: " + lexeme);
      System.out.println("Type: " + type);
      System.out.println("Kind: " + kind);
      System.out.println("--------------------------");
    }
  }
}
