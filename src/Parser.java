
/**   
 *  Componentes do grupo
 *  Carolina de Lima Silva - 561397
 *  Jorge Allan de Castro Oliveira - 559855
 *  Ricardo Xavier Sena - 481694
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Parser {
  int nextTemp = 0; // Counter of temporary
  int nextMemo = 0x4000;// The first address to variable declaration
  int nextLabel = 0; // Counter to create new labels
  LexicalAnalyzer lAnalyzer; // Instance of the lexical analyzer
  BufferedWriter assemblyFile; // Instance of the assembly program file
  public List<String> buffer; // Buffer responsible store the assembly instructions
  boolean notHasVector = false;

  /**
   * Constructor responsible to make the first call of the automaton
   * 
   * @param analyzer the instance of the lexical analyzer
   * @throws IOException
   * 
   */
  public Parser(LexicalAnalyzer analyzer) throws IOException {
    /**
     * The path of the assembly program
     * 
     */
    assemblyFile = new BufferedWriter(new FileWriter("/Users/jorgeoliveira/Developer/L/program.asm"));
    buffer = new ArrayList<>();

    this.lAnalyzer = analyzer;
    lAnalyzer.automaton(); // Realizes the first call of the automaton
  }

  public int temporary(int numByte) {
    nextTemp += numByte;
    return nextTemp - numByte;
  }

  public int memory(int numByte) {
    nextMemo += numByte;
    return nextMemo - numByte;
  }

  public String label() {
    return "R" + nextLabel++;
  }

  /**
   * Verify if the current token matches the expected token and calls the
   * automaton
   * 
   * @param expectedToken the token that the parser is waiting to proceed the
   *                      analysis
   * @throws IOException
   * 
   */
  public void matchToken(TokenEnum expectedToken) throws IOException {
    if (SymbolTable.getToken(lAnalyzer.lexeme) == expectedToken) {
      lAnalyzer.automaton();
    } else {
      if (lAnalyzer.isEOF()) {
        System.out.println(lAnalyzer.currentLine + "\n" + "fim de arquivo nao esperado.");
        System.exit(1);
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    }
  }

  /**
   * Verifies if the identifier it was already declared in the program
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * 
   */
  public void verifyIdentifier(String lexeme) {
    if (!(SymbolTable.getKind(lexeme) == KindEnum.EMPTY)) {
      System.out
          .println(lAnalyzer.currentLine + "\n" + "identificador ja declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }
  }

  /**
   * Sets the const type, but first checks if the type is empty
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * @param type   type char, int or boolean
   * 
   */
  public void setConstKind(String lexeme, TypeEnum type) {
    if ((type == TypeEnum.CHARACTER || type == TypeEnum.INTEGER || type == TypeEnum.LOGIC)
        && SymbolTable.getKind(lexeme) == KindEnum.EMPTY) {
      SymbolTable.setType(lexeme, type);
      SymbolTable.setKind(lexeme, KindEnum.CONST);
    }
  }

  /**
   * Sets the variable type, but first checks if the type is empty
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * @param type   type char, int or boolean
   * 
   */
  public void setVarKind(String lexeme, TypeEnum type) {
    if ((type == TypeEnum.CHARACTER || type == TypeEnum.INTEGER || type == TypeEnum.LOGIC)
        && SymbolTable.getKind(lexeme) == KindEnum.EMPTY) {
      SymbolTable.setType(lexeme, type);
      SymbolTable.setKind(lexeme, KindEnum.VAR);
    }
  }

  /**
   * Verifies if the types are compatibles to make an attribution
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * @param type   types that are being analyzed
   * 
   */
  public void verifyAttributionCompatibility(String lexeme, TypeEnum type) {
    if (SymbolTable.getType(lexeme) != type) {
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }
  }

  /**
   * Writes in the file all the assembly code stored in the buffer
   * 
   * @throws IOException
   * 
   */
  public void createAssembly() throws IOException {
    for (String s : buffer) {
      assemblyFile.write(s);
      assemblyFile.newLine();
    }
    assemblyFile.close();
  }

  /**
   * The initial method of the parser
   * 
   * @throws IOException
   * 
   */
  public void parse() throws IOException {
    try {
      s();
    } catch (Exception ex) {
      System.err.println("ERRO: " + ex);
    }
  }

  /**
   * S -> { D } main "{" { C } "}" EOF
   * 
   * @throws IOException
   * 
   */
  public void s() throws IOException {

    // Beginning of the assembly code
    buffer.add("sseg SEGMENT STACK ;início seg. pilha");
    buffer.add("  byte 4000h DUP(?) ;dimensiona pilha");
    buffer.add("sseg ENDS ;fim seg. pilha");
    buffer.add("dseg SEGMENT PUBLIC ;início seg. dados");
    buffer.add("  byte 4000h DUP(?) ;temporários");

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FINAL
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.INT
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.CHAR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.BOOLEAN) {
      d();
    }

    matchToken(TokenEnum.MAIN);
    matchToken(TokenEnum.OPEN_BRACES);

    buffer.add("dseg ENDS ;fim seg. dados");
    buffer.add("cseg SEGMENT PUBLIC ;início seg. código");
    buffer.add("  ASSUME CS:cseg, DS:dseg");
    buffer.add("strt: ;início do programa");
    buffer.add("  mov ax, dseg");
    buffer.add("  mov ds, ax");

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN) {
      c();
    }

    matchToken(TokenEnum.CLOSE_BRACES);

    // End of the assembly code
    buffer.add("  mov ah, 4Ch");
    buffer.add("  int 21h");
    buffer.add("cseg ENDS ;fim seg. código");
    buffer.add("END strt; fim programa");

    createAssembly(); // Creates the program in assembly machine language

    if (lAnalyzer.isEOF()) {
      // Shows the number of compiled lines and exit the program
      System.out.println(lAnalyzer.currentLine + " linhas compiladas.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }
  }

  /**
   * D -> final id [ = [ ( - ) ] V ]; | T
   * 
   * @throws IOException
   * 
   */
  public void d() throws IOException {
    String idLexeme = null; // Lexeme of the id
    TypeEnum valueType = null; // Type of the value in the attribution
    boolean signal = false; // True if have plus or minus signal
    String line = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FINAL) {
      matchToken(TokenEnum.FINAL);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        verifyIdentifier(lAnalyzer.lexeme); // Method that verifies if the id is already declared
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
        matchToken(TokenEnum.EQUAL);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
          signal = true;
          matchToken(TokenEnum.MINUS);
        }

        String lexemeValue = lAnalyzer.lexeme;

        if (lexemeValue.charAt(0) == '\"' && lexemeValue.charAt(lexemeValue.length() - 1) == '\"') {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          valueType = v(); // Catches the type of the value that will be attribuited in the identifier
        }

        if (valueType == TypeEnum.CHARACTER) {
          line = "  byte ";
        } else {
          line = "  sword ";
        }

        // The type must be integer if have plus or minus signal
        if (signal == true && valueType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (signal == true) {
          line += "-";
        }

        signal = false;
        setConstKind(idLexeme, valueType); // Method that sets the kind of the id as const

        if (valueType == TypeEnum.LOGIC) {
          lexemeValue = lexemeValue.equals("TRUE") ? "1" : "0";
        }

        buffer.add(line + lexemeValue + " ; " + idLexeme);
      }

      matchToken(TokenEnum.SEMICOLON);
    } else {
      t();
    }
  }

  /**
   * T -> (int|char|boolean) id (“[“ value “]” | := [ ( + | - ) ] V) {, id (“[“
   * value “]” | := [ ( + | - ) ] V)};
   * 
   * @throws IOException
   * 
   */
  public void t() throws IOException {
    TypeEnum type = null; // Type of the id
    String idLexeme = null; // Lexeme of the id
    TypeEnum valueType = null; // Type of the value in the attribution
    boolean signal = false; // True if have plus or minus signal
    int size = 0; // Size of the vector
    String line = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.CHAR) {
      type = TypeEnum.CHARACTER;
      matchToken(TokenEnum.CHAR);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.BOOLEAN) {
      type = TypeEnum.LOGIC;
      matchToken(TokenEnum.BOOLEAN);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.INT) {
      type = TypeEnum.INTEGER;
      matchToken(TokenEnum.INT);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (type == TypeEnum.CHARACTER) {
      line = "  byte ";
    } else {
      line = "  sword ";
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      verifyIdentifier(lAnalyzer.lexeme);
      idLexeme = lAnalyzer.lexeme;
      setVarKind(lAnalyzer.lexeme, type);
      matchToken(TokenEnum.ID);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      // The type must be integer to get the size
      if (SymbolTable.getType(lAnalyzer.lexeme) == TypeEnum.INTEGER) {
        size = Integer.parseInt(lAnalyzer.lexeme);

        /**
         * Impossible to create a vector if the size is zero or greater than 8kb
         */
        if (size > 0 && size * (type == TypeEnum.CHARACTER ? 1 : 2) <= 8192) {
          SymbolTable.setSize(idLexeme, size);
          matchToken(TokenEnum.VALUE);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        }
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      line += size + " DUP(?)";

      matchToken(TokenEnum.CLOSE_BRACKETS);
    } else {
      SymbolTable.setSize(idLexeme, null);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
      matchToken(TokenEnum.ATTRIBUTION);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
        matchToken(TokenEnum.PLUS);
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;
        matchToken(TokenEnum.MINUS);
      }

      String lexemeValue = lAnalyzer.lexeme;

      if (SymbolTable.getType(idLexeme) == TypeEnum.CHARACTER && size <= 0 && lexemeValue.charAt(0) == '\"') {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        valueType = v(); // Catches the type of the value that will be attribuited in the identifier
      }

      // The type must be integer if have plus or minus signal
      if (signal == true && valueType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        verifyAttributionCompatibility(idLexeme, valueType);
      }

      if (signal == true) {
        line += "-";
      }

      signal = false;
      if (valueType == TypeEnum.LOGIC) {
        lexemeValue = lexemeValue.equals("TRUE") ? "1" : "0";
      }

      line += lexemeValue;

    } else {
      line += "?";
    }

    buffer.add(line + " ;" + idLexeme);

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);

      line = "";

      if (type == TypeEnum.CHARACTER) {
        line = "  byte ";
      } else {
        line = "  sword ";
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        verifyIdentifier(lAnalyzer.lexeme);
        idLexeme = lAnalyzer.lexeme;
        setVarKind(lAnalyzer.lexeme, type);
        matchToken(TokenEnum.ID);
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        if (SymbolTable.getType(lAnalyzer.lexeme) == TypeEnum.INTEGER) {
          size = Integer.parseInt(lAnalyzer.lexeme);

          /**
           * Impossible to create a vector if the size is zero or greater than 8kb
           */
          if (size > 0 && size * (type == TypeEnum.CHARACTER ? 1 : 2) <= 8192) {
            SymbolTable.setSize(idLexeme, size);
            matchToken(TokenEnum.VALUE);
          } else {
            // Throws an error and exit the program
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          }
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        line += size + " DUP(?)";

        matchToken(TokenEnum.CLOSE_BRACKETS);
      } else {
        SymbolTable.setSize(idLexeme, null);
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
        matchToken(TokenEnum.ATTRIBUTION);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
          matchToken(TokenEnum.PLUS);
        } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
          signal = true;
          matchToken(TokenEnum.MINUS);
        }

        String lexemeValue = lAnalyzer.lexeme;

        if (SymbolTable.getType(idLexeme) == TypeEnum.CHARACTER && size <= 0 && lexemeValue.charAt(0) == '\"') {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          valueType = v(); // Catches the type of the value that will be attribuited in the identifier
        }

        if (signal == true && valueType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          verifyAttributionCompatibility(idLexeme, valueType);
        }

        if (signal == true) {
          line += "-";
        }

        signal = false;
        if (valueType == TypeEnum.LOGIC) {
          lexemeValue = lexemeValue.equals("TRUE") ? "1" : "0";
        }

        line += lexemeValue;

      } else {
        line += "?";
      }

      buffer.add(line + " ;" + idLexeme);
    }

    matchToken(TokenEnum.SEMICOLON);
  }

  /**
   * ATT -> id [ “[“ EXP “]” ] [:= [ ( + | - ) ] EXP ]
   * 
   * @throws IOException
   * 
   */
  public void att() throws IOException {
    boolean signal = false;
    Register register = new Register();
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompativel" + " ["
            + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      } else {
        // Throws an error and exit the program
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    // [ “[“ EXP “]” ]
    if (SymbolTable.getSize(idLexeme) != null) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        register = exp(); // Catches the type of the expression

        if (register.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
        // } else if (SymbolTable.getType(idLexeme) == TypeEnum.INTEGER &&
        // SymbolTable.getSize(idLexeme) == null) {
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    }

    // [:= [ ( + | - ) ] EXP ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
      matchToken(TokenEnum.ATTRIBUTION);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
        signal = true;
        matchToken(TokenEnum.PLUS);
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;
        matchToken(TokenEnum.MINUS);
      }

      String valueLexeme = lAnalyzer.lexeme;
      register = exp(); // Catches the type of the expression

      if (signal == true && register.getType() != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, register.getType());

        if (SymbolTable.getType(idLexeme) == TypeEnum.CHARACTER
            && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
            && SymbolTable.getSize(idLexeme) < lAnalyzer.lexeme.length() - 1) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
            && SymbolTable.getSize(idLexeme) < SymbolTable.getSize(valueLexeme)) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (SymbolTable.getSize(idLexeme) == null && SymbolTable.getSize(valueLexeme) != null) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }
      }
    }
  }

  /**
   * V -> value | TRUE | FALSE
   * 
   * @return TypeEnum type of the value, TRUE or FALSE
   * @throws IOException
   * 
   */

  public TypeEnum v() throws IOException {
    TypeEnum type = null; // Stores the type

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.VALUE) {
      type = SymbolTable.getType(lAnalyzer.lexeme); // Type is integer or character
      matchToken(TokenEnum.VALUE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE) {
      type = SymbolTable.getType(lAnalyzer.lexeme); // Type is logic
      matchToken(TokenEnum.TRUE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FALSE) {
      type = SymbolTable.getType(lAnalyzer.lexeme); // Type is logic
      matchToken(TokenEnum.FALSE);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    return type;
  }

  /**
   * C -> A | I | F | W | R | ;
   * 
   * @throws IOException
   * 
   */
  public void c() throws IOException {
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON) {
      matchToken(TokenEnum.SEMICOLON);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN) {
      r();
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR) {
      f();
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF) {
      i();
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      a();
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      w();
    }
  }

  /**
   * R -> readln “(“ id [ “[“ EXP “]” ] “)”
   * 
   * @throws IOException
   * 
   */
  public void r() throws IOException {
    Register register = null;
    String lexemeValue = "";

    matchToken(TokenEnum.READLN);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) != KindEnum.EMPTY) {
        lexemeValue = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else {
        // Throws an error and exit the program
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {

      if (SymbolTable.getSize(lexemeValue) == null) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      matchToken(TokenEnum.OPEN_BRACKETS);

      register = exp(); // Catches the type of the expression

      // Type must be integer
      if (register.getType() != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      matchToken(TokenEnum.CLOSE_BRACKETS);
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);
  }

  /**
   * L -> ( ATT | C ) {, C }
   * 
   * @throws IOException
   * 
   */
  public void l() throws IOException {
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      att();
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      c();
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);
      c();
    }
  }

  /**
   * F -> for “(“ [ L ] ; EXP ; [ L ] “)” ( C | “{“ { C ;}+ “}” )
   * 
   * @throws IOException
   * 
   */
  public void f() throws IOException {
    Register register = null;

    matchToken(TokenEnum.FOR);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    // [ L ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      l();
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON) {
      matchToken(TokenEnum.SEMICOLON);
    }

    register = exp(); // Catches the type of the expression

    if (register.getType() != TypeEnum.LOGIC) {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }

    matchToken(TokenEnum.SEMICOLON);

    // [ L ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      l();
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);

    // ( C | “{“ { C ;}+ “}” )
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACES) {
      matchToken(TokenEnum.OPEN_BRACES);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        c();
        matchToken(TokenEnum.SEMICOLON);
      }

      matchToken(TokenEnum.CLOSE_BRACES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      c();
      matchToken(TokenEnum.SEMICOLON);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }
  }

  /**
   * I -> if “(“ EXP “)” then ( C | “{“ { C } “}” ) [ else ( C | “{“ { C } “}” ) ]
   * 
   * @throws IOException
   * 
   */
  public void i() throws IOException {
    Register register = null;

    // if “(“ EXP “)” then
    matchToken(TokenEnum.IF);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    register = exp(); // Catches the type of the expression

    if (register.getType() != TypeEnum.LOGIC) {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);
    matchToken(TokenEnum.THEN);

    // ( C | “{“ { C } “}” )
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACES) {
      matchToken(TokenEnum.OPEN_BRACES);

      while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
        c();
      }

      matchToken(TokenEnum.CLOSE_BRACES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      c();
      matchToken(TokenEnum.SEMICOLON);
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    // [ else ( C | “{“ { C } “}” ) ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ELSE) {
      matchToken(TokenEnum.ELSE);

      // ( C | “{“ { C } “}” )
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACES) {
        matchToken(TokenEnum.OPEN_BRACES);

        while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
            || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
          c();
        }

        matchToken(TokenEnum.CLOSE_BRACES);

        // C
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
        c();
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        c();
        matchToken(TokenEnum.SEMICOLON);
      }
    }
  }

  /**
   * A -> id [ “[“ EXP “]” ] [:= [ ( + | - ) ] EXP ] {, id [ “[“ EXP “]” ] [ := [(
   * + | - ) ] EXP ] }
   * 
   * @throws IOException
   * 
   */
  public void a() throws IOException {
    boolean signal = false;
    Register register = null;
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompativel" + " ["
            + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      } else {
        // Throws an error and exit the program
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    // [ “[“ EXP “]” ]
    if (SymbolTable.getSize(idLexeme) != null) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        register = exp(); // Catches the type of the expression

        if (register.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
        // } else if (SymbolTable.getType(idLexeme) == TypeEnum.INTEGER &&
        // SymbolTable.getSize(idLexeme) == null) {
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    }

    // [:= [ ( + | - ) ] EXP ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
      matchToken(TokenEnum.ATTRIBUTION);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
        signal = true;
        matchToken(TokenEnum.PLUS);
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;
        matchToken(TokenEnum.MINUS);
      }

      String valueLexeme = lAnalyzer.lexeme;
      register = exp(); // Catches the type of the expression

      if (signal == true && register.getType() != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, register.getType());

        if (SymbolTable.getType(idLexeme) == TypeEnum.CHARACTER
            && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
            && SymbolTable.getSize(idLexeme) < lAnalyzer.lexeme.length() - 1) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
            && SymbolTable.getSize(idLexeme) < SymbolTable.getSize(valueLexeme)) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (SymbolTable.getSize(idLexeme) == null && SymbolTable.getSize(valueLexeme) != null) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else if (SymbolTable.getSize(idLexeme) == null && valueLexeme.charAt(valueLexeme.length() - 1) == '\"') {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }
      }

      // {, id [ “[“ EXP “]” ] [ := [ ( + | - ) ] EXP ] }
      while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
        matchToken(TokenEnum.COMMA);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
          if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
            // idLexeme = lAnalyzer.lexeme;
            matchToken(TokenEnum.ID);
          } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
            // Throws an error and exit the program
            System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompativel" + " ["
                + lAnalyzer.lexeme + "]" + ".");
            System.exit(1);
          } else {
            // Throws an error and exit the program
            System.out.println(
                lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
            System.exit(1);
          }
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
          System.exit(1);
        }

        // [ “[“ EXP “]” ]
        if (SymbolTable.getSize(idLexeme) != null) {
          if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
            matchToken(TokenEnum.OPEN_BRACKETS);

            register = exp(); // Catches the type of the expression

            if (register.getType() != TypeEnum.INTEGER) {
              // Throws an error and exit the program
              System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
              System.exit(1);
            }

            matchToken(TokenEnum.CLOSE_BRACKETS);
            // } else if (SymbolTable.getType(idLexeme) == TypeEnum.INTEGER &&
            // SymbolTable.getSize(idLexeme) == null) {
          } else {
            System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
            System.exit(1);
          }
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
      }

      // [ := [ ( + | - ) ] EXP ]
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
        matchToken(TokenEnum.ATTRIBUTION);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
          signal = true;
          matchToken(TokenEnum.PLUS);
        } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
          signal = true;
          matchToken(TokenEnum.MINUS);
        }

        String newValueLexeme = lAnalyzer.lexeme;
        register = exp(); // Catches the type of the expression

        if (signal == true && register.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          verifyAttributionCompatibility(idLexeme, register.getType());

          if (SymbolTable.getType(idLexeme) == TypeEnum.CHARACTER
              && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
              && SymbolTable.getSize(idLexeme) < lAnalyzer.lexeme.length() - 1) {
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          } else if (lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) == '\"'
              && SymbolTable.getSize(idLexeme) < SymbolTable.getSize(newValueLexeme)) {
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          } else if (SymbolTable.getSize(idLexeme) == null && SymbolTable.getSize(newValueLexeme) != null) {
            System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
            System.exit(1);
          }
        }
      }
    }
  }

  /**
   * W -> write “(“ EXP {, EXP } “)” | writeln “(“ EXP {, EXP } “)”
   * 
   * @throws IOException
   * 
   */
  public void w() throws IOException {
    Register register;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE) {
      matchToken(TokenEnum.WRITE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      matchToken(TokenEnum.WRITELN);
    }

    matchToken(TokenEnum.OPEN_PARENTHESES);

    register = exp(); // Catches the type of the expression

    if (register.getType() == TypeEnum.LOGIC) {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);
      register = exp(); // Catches the type of the expression

      if (register.getType() == TypeEnum.LOGIC) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);
  }

  /**
   * EXP -> EXPS [ ( = | <> | < | > | <= | >=) EXPS ]
   * 
   * @throws IOException Fazer os ifs de comparação para essa regra
   * 
   */
  public Register exp() throws IOException {
    Register firstRegister;
    Register secondRegister;
    Register finalRegister = new Register();

    firstRegister = exps();

    finalRegister.setAddress(firstRegister.getAddress());
    finalRegister.setSize(firstRegister.getSize());
    finalRegister.setType(firstRegister.getType());

    // [ ( = | <> | < | > | <= | >=) EXPS ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
      matchToken(TokenEnum.EQUAL);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {
        finalRegister.setType(TypeEnum.LOGIC);
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT_EQUAL) {
      matchToken(TokenEnum.NOT_EQUAL);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {

        if (firstRegister.getType() != TypeEnum.CHARACTER) {
          finalRegister.setType(TypeEnum.LOGIC);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_THAN) {
      matchToken(TokenEnum.LESS_THAN);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {

        if (firstRegister.getType() != TypeEnum.CHARACTER) {
          finalRegister.setType(TypeEnum.LOGIC);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_THAN) {
      matchToken(TokenEnum.GREATER_THAN);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {

        if (firstRegister.getType() != TypeEnum.CHARACTER) {
          finalRegister.setType(TypeEnum.LOGIC);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_OR_EQUAL) {
      matchToken(TokenEnum.LESS_OR_EQUAL);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {

        if (firstRegister.getType() != TypeEnum.CHARACTER) {
          finalRegister.setType(TypeEnum.LOGIC);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_OR_EQUAL) {
      matchToken(TokenEnum.GREATER_OR_EQUAL);
      secondRegister = exps(); // Catches the type of the expression

      if (firstRegister.getType() == secondRegister.getType()) {

        if (firstRegister.getType() != TypeEnum.CHARACTER) {
          finalRegister.setType(TypeEnum.LOGIC);
        } else {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    }

    return finalRegister;
  }

  /**
   * EXPS -> [ (+ | -) ] TERM { ( + | - | or) TERM }
   * 
   * @throws IOException
   * @return finalRegister Register
   * 
   */
  public Register exps() throws IOException {
    boolean signal = false;
    Register firstRegister;
    Register secondRegister;
    Register finalRegister = new Register();

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
      matchToken(TokenEnum.PLUS);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
      signal = true;
      matchToken(TokenEnum.MINUS);
    }

    firstRegister = term(); // Catches the type of the first term

    finalRegister.setAddress(firstRegister.getAddress());
    finalRegister.setSize(firstRegister.getSize());
    finalRegister.setType(firstRegister.getType());

    if (signal == true && firstRegister.getType() != TypeEnum.INTEGER) {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    } else {
      signal = false;
    }

    // { ( + | - | or) TERM }
    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OR) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {

        matchToken(TokenEnum.PLUS);

        secondRegister = term(); // Catches the type of the second term

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        matchToken(TokenEnum.MINUS);
        secondRegister = term(); // Catches the type of the second term

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OR) {
        matchToken(TokenEnum.OR);
        secondRegister = term(); // Catches the type of the second term

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.LOGIC
            || secondRegister.getType() != TypeEnum.LOGIC) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }
      }
    }

    return finalRegister;
  }

  /**
   * TERM -> FACTOR { (* | / | % | and) FACTOR }
   * 
   * @throws IOException
   * @return finalRegister Register
   * 
   */
  public Register term() throws IOException {
    Register firstRegister;
    Register secondRegister;
    Register finalRegister = new Register();

    firstRegister = factor(); // Catches the type of the first factor

    finalRegister.setAddress(firstRegister.getAddress());
    finalRegister.setSize(firstRegister.getSize());
    finalRegister.setType(firstRegister.getType());

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.AND) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY) {
        matchToken(TokenEnum.MULTIPLY);

        secondRegister = factor(); // Catches the type of the second factor

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE) {
        matchToken(TokenEnum.DIVIDE);

        secondRegister = factor(); // Catches the type of the second factor

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE) {
        matchToken(TokenEnum.PERCENTAGE);

        secondRegister = factor(); // Catches the type of the second factor

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.INTEGER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          finalRegister = firstRegister;
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.AND) {
        matchToken(TokenEnum.AND);
        secondRegister = factor(); // Catches the type of the second factor

        if (firstRegister.getType() != secondRegister.getType() || firstRegister.getType() != TypeEnum.LOGIC
            || secondRegister.getType() != TypeEnum.LOGIC) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }
      }
    }

    return finalRegister;
  }

  /**
   * FACTOR -> not FACTOR | “(“ EXP “)” | id [ “[“ EXP “]” ] | V
   * 
   * @throws IOException
   * @return TypeEnum of FACTOR
   * 
   */
  public Register factor() throws IOException {
    Register register = new Register();

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT) {
      matchToken(TokenEnum.NOT);
      register = factor(); // Catches the type of the factor

      if (register.getType() != TypeEnum.LOGIC) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_PARENTHESES) {
      matchToken(TokenEnum.OPEN_PARENTHESES);

      register = exp(); // Catches the type of the expression
      matchToken(TokenEnum.CLOSE_PARENTHESES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      String l = lAnalyzer.lexeme;
      register.setType(SymbolTable.getType(lAnalyzer.lexeme));
      matchToken(TokenEnum.ID);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        TypeEnum type = exp().getType(); // Catches the type of the expression

        if (type != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
      } else if (register.getType() == TypeEnum.LOGIC && SymbolTable.getSize(l) != null) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.VALUE) {
      if (Character.isDigit(lAnalyzer.lexeme.charAt(0))
          && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) != 'h') {
        int temp = temporary(2);
        buffer.add("  mov ax, " + lAnalyzer.lexeme);
        buffer.add("  mov DS:[" + temp + "], ax");
        register.setAddress(temp);
      } else {
        int temp = temporary(1);
        buffer.add("  mov ah, " + lAnalyzer.lexeme);
        buffer.add("  mov DS:[" + temp + "], ah");
        register.setAddress(temp);
      }

      register.setType(SymbolTable.getType(lAnalyzer.lexeme)); // Type is integer or character
      matchToken(TokenEnum.VALUE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FALSE) {
      int temp = temporary(2);
      buffer.add("  mov ax, " + (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE ? 1 : 0));
      buffer.add("  mov DS:[" + temp + "], ax");
      register.setAddress(temp);

      register.setType(SymbolTable.getType(lAnalyzer.lexeme)); // Type is logic
      matchToken(SymbolTable.getToken(lAnalyzer.lexeme));
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    return register;
  }
}