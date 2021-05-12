import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Parser {
  Integer valueInt = null; // Responsible to store the value of type integer
  String valueChar = null; // Responsible to store the value of type character
  LexicalAnalyzer lAnalyzer; // Instance of the lexical analyzer
  BufferedWriter assemblyFile; // Instance of the assembly program file
  public List<String> buffer; // Buffer responsible store the assembly instructions

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
    buffer.add("byte 4000h DUP(?) ;dimensiona pilha");
    buffer.add("sseg ENDS ;fim seg. pilha");
    buffer.add("dseg SEGMENT PUBLIC ;início seg. dados");
    buffer.add("byte 4000h DUP(?) ;temporários");

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
    buffer.add("ASSUMECS:cseg, DS:dseg");
    buffer.add("strt: ;início do programa");
    buffer.add("mov ax, dseg");
    buffer.add("mov ds, ax");

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
    buffer.add("mov ah, 4Ch");
    buffer.add("int 21h");
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
   * D -> final id [ = [ ( - ) ] V ] {, id [ = [ ( - ) ] V ]}; | T
   * 
   * @throws IOException
   * 
   */
  public void d() throws IOException {
    String idLexeme = null; // Lexeme of the id
    TypeEnum valueType = null; // Type of the value in the attribution
    boolean signal = false; // True if have plus or minus signal

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

        valueType = v(); // Catches the type of the value that will be attribuited to the identifier

        // The type must be integer if have plus or minus signal
        if (signal == true && valueType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          setConstKind(idLexeme, valueType); // Method that sets the kind of the id as const
        }
      }

      while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
        matchToken(TokenEnum.COMMA);

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

          valueType = v(); // Catches the type of the value that will be attribuited to the identifier

          // The type must be integer if have plus or minus signal
          if (signal != false && valueType != TypeEnum.INTEGER) {
            // Throws an error and exit the program
            System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
            System.exit(1);
          } else {
            signal = false;
            setConstKind(idLexeme, valueType); // Method that sets the kind of the id as const
          }
        }
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
        if (size > 0 && size <= 8192) {
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

      matchToken(TokenEnum.CLOSE_BRACKETS);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
      matchToken(TokenEnum.ATTRIBUTION);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
        signal = true;
        matchToken(TokenEnum.PLUS);
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;
        matchToken(TokenEnum.MINUS);
      }

      valueType = v(); // Catches the type of the value that will be attribuited in the identifier

      // The type must be integer if have plus or minus signal
      if (signal != false && valueType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, valueType);
      }
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);

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
          if (size > 0 && size <= 8192) {
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

        matchToken(TokenEnum.CLOSE_BRACKETS);
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ATTRIBUTION) {
        matchToken(TokenEnum.ATTRIBUTION);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
          signal = true;
          matchToken(TokenEnum.PLUS);
        } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
          signal = true;
          matchToken(TokenEnum.MINUS);
        }

        valueType = v(); // Catches the type of the value that will be attribuited in the identifier

        if (signal != false && valueType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          verifyAttributionCompatibility(idLexeme, valueType);
        }
      }
    }

    matchToken(TokenEnum.SEMICOLON);
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
    TypeEnum expType = null;

    matchToken(TokenEnum.READLN);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) != KindEnum.EMPTY) {
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
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp(); // Catches the type of the expression

      // Type must be integer
      if (expType != TypeEnum.INTEGER) {
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
   * ATT -> id [ “[“ EXP “]” ] [:= [ ( + | - ) ] EXP ]
   * 
   * @throws IOException
   * 
   */
  public void att() throws IOException {
    boolean signal = false;
    TypeEnum expType = null;
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
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
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp(); // Catches the type of the expression

      if (expType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      /**
       * If exp.value <= 0 || exp.value > 8192 || exp.value > idLexeme.size then ERROR
       *
       */
      if (valueInt != null && valueInt <= 0) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > 8192) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else {
        valueInt = null;
      }

      matchToken(TokenEnum.CLOSE_BRACKETS);
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

      expType = exp(); // Catches the type of the expression

      if (signal != false && expType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, expType);

        // If idLexeme.size < exp.value.length then ERROR
        if (valueChar != null & SymbolTable.getSize(idLexeme) < valueChar.length() - 1
            && expType == TypeEnum.CHARACTER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else {
          valueChar = "";
        }
      }
    }
  }

  /**
   * F -> for “(“ [ L ] ; EXP ; [ L ] “)” ( C | “{“ { C ;}+ “}” )
   * 
   * @throws IOException
   * 
   */
  public void f() throws IOException {
    TypeEnum expType = null;

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

    expType = exp(); // Catches the type of the expression

    if (expType != TypeEnum.LOGIC) {
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

      do {
        c();
        matchToken(TokenEnum.SEMICOLON);
      } while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
          || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN);

      matchToken(TokenEnum.CLOSE_BRACES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.READLN
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FOR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.SEMICOLON
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.IF
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      c();
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
    TypeEnum expType = null;

    // if “(“ EXP “)” then
    matchToken(TokenEnum.IF);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    expType = exp(); // Catches the type of the expression

    if (expType != TypeEnum.LOGIC) {
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
    TypeEnum expType = null;
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
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
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp(); // Catches the type of the expression

      if (expType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      // If exp.value <= 0 || exp.value > 8192 || exp.value > idLexeme.size then ERROR
      if (valueInt != null && valueInt <= 0) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > 8192) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else {
        valueInt = null;
      }

      matchToken(TokenEnum.CLOSE_BRACKETS);
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

      expType = exp(); // Catches the type of the expression

      if (signal != false && expType != TypeEnum.INTEGER) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, expType);

        // If idLexeme.size < exp.value.length then ERROR
        if (valueChar != null && SymbolTable.getSize(idLexeme) < valueChar.length() - 1
            && expType == TypeEnum.CHARACTER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else {
          valueChar = null;
        }
      }
    }

    // {, id [ “[“ EXP “]” ] [ := [ ( + | - ) ] EXP ] }
    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
          idLexeme = lAnalyzer.lexeme;
          matchToken(TokenEnum.ID);
        } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
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
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        expType = exp(); // Catches the type of the expression

        if (expType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        // If exp.value <= 0 || exp.value > 8192 || exp.value > idLexeme.size then ERROR
        if (valueInt != null && valueInt <= 0) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (valueInt != null && valueInt > 8192) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else {
          valueInt = null;
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

        expType = exp(); // Catches the type of the expression

        if (signal != false && expType != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          verifyAttributionCompatibility(idLexeme, expType);

          // se idLexeme.size < exp.value.length (atribuição de char no vetor) então ERRO
          if (valueChar != null && SymbolTable.getSize(idLexeme) < valueChar.length() - 1
              && expType == TypeEnum.CHARACTER) {
            // Throws an error and exit the program
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          } else {
            valueChar = null;
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
    TypeEnum expType = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE) {
      matchToken(TokenEnum.WRITE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      matchToken(TokenEnum.WRITELN);
    }

    matchToken(TokenEnum.OPEN_PARENTHESES);
    expType = exp(); // Catches the type of the expression

    if (expType == TypeEnum.LOGIC) {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);
      expType = exp(); // Catches the type of the expression

      if (expType == TypeEnum.LOGIC) {
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
   * @throws IOException
   * 
   */
  public TypeEnum exp() throws IOException {
    TypeEnum finalType = null;
    TypeEnum type1 = null;
    TypeEnum type2 = null;

    type1 = exps(); // Catches the type of the expression

    // [ ( = | <> | < | > | <= | >=) EXPS ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
      matchToken(TokenEnum.EQUAL);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT_EQUAL) {
      matchToken(TokenEnum.NOT_EQUAL);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_THAN) {
      matchToken(TokenEnum.LESS_THAN);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_THAN) {
      matchToken(TokenEnum.GREATER_THAN);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_OR_EQUAL) {
      matchToken(TokenEnum.LESS_OR_EQUAL);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_OR_EQUAL) {
      matchToken(TokenEnum.GREATER_OR_EQUAL);
      type2 = exps(); // Catches the type of the expression

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    } else {
      finalType = type1;
    }

    return finalType;
  }

  /**
   * EXPS -> [ (+ | -) ] TERM { ( + | - | or) TERM }
   * 
   * @throws IOException
   * @return TypeEnum of EXPS
   * 
   */
  public TypeEnum exps() throws IOException {
    boolean signal = false;
    TypeEnum finalType = null;
    TypeEnum type1 = null;
    TypeEnum type2 = null;
    Integer valueAux = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PLUS) {
      signal = true;
      matchToken(TokenEnum.PLUS);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
      signal = true;
      matchToken(TokenEnum.MINUS);
    }

    type1 = term(); // Catches the type of the first term

    if (signal != false && type1 != TypeEnum.INTEGER) {
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
        signal = true;

        matchToken(TokenEnum.PLUS);
        valueAux = valueInt; // Temporary vartiable to store the first value
        type2 = term(); // Catches the type of the second term

        // The final value will be the sum of the temporary with the second value
        if (valueAux != null && valueInt != null) {
          valueInt = valueAux + valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = type1;
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;

        matchToken(TokenEnum.MINUS);
        valueAux = valueInt; // Temporary vartiable to store the first value
        type2 = term(); // Catches the type of the second term

        // The final value will be the difference of the temporary and the second value
        if (valueAux != null && valueInt != null) {
          valueInt = valueAux - valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = type1;
        }
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OR) {
        matchToken(TokenEnum.OR);
        type2 = term(); // Catches the type of the second term

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = TypeEnum.LOGIC;
        }
      }
    }

    if (type2 == null) {
      finalType = type1;
    }

    return finalType;
  }

  /**
   * TERM -> FACTOR { (* | / | % | and) FACTOR }
   * 
   * @throws IOException
   * @return TypeEnum of TERM
   * 
   */
  public TypeEnum term() throws IOException {
    boolean signal = false;
    TypeEnum type1 = null;
    TypeEnum type2 = null;
    TypeEnum finalType = null;
    Integer valueAux = null;

    type1 = factor(); // Catches the type of the first factor

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.AND) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY) {
        signal = true;
        matchToken(TokenEnum.MULTIPLY);

        valueAux = valueInt; // Temporary vartiable to store the first value
        type2 = factor(); // Catches the type of the second factor

        /**
         * The final value will be the multiplication of the temporary and the second
         * value
         * 
         */
        if (valueAux != null && valueInt != null) {
          valueInt = valueAux * valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = type1;
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE) {
        signal = true;
        matchToken(TokenEnum.DIVIDE);

        valueAux = valueInt; // Temporary vartiable to store the first value
        type2 = factor(); // Catches the type of the second factor

        if (valueAux != null && valueInt != null) {
          valueInt = (int) (valueAux / valueInt);
        }

        /**
         * The final value will be the division of the temporary and the second value
         * 
         */
        if (signal != false && type2 != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = type1;
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE) {
        signal = true;
        matchToken(TokenEnum.PERCENTAGE);

        valueAux = valueInt; // Temporary vartiable to store the first value
        type2 = factor(); // Catches the type of the second factor

        // The final value will be the module of the temporary and the second value
        if (valueAux != null && valueInt != null) {
          valueInt = valueAux % valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        if (type1 != type2) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = type1;
        }

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.AND) {
        matchToken(TokenEnum.AND);
        type2 = factor(); // Catches the type of the second factor

        if (type1 != type2) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          finalType = TypeEnum.LOGIC;
        }
      }
    }

    if (type2 == null) {
      finalType = type1;
    }

    return finalType;
  }

  /**
   * FACTOR -> not FACTOR | “(“ EXP “)” | id [ “[“ EXP “]” ] | V
   * 
   * @throws IOException
   * @return TypeEnum of FACTOR
   * 
   */
  public TypeEnum factor() throws IOException {
    TypeEnum type = null;
    TypeEnum idType = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT) {
      matchToken(TokenEnum.NOT);
      type = factor(); // Catches the type of the factor

      if (type != TypeEnum.LOGIC) {
        // Throws an error and exit the program
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_PARENTHESES) {
      matchToken(TokenEnum.OPEN_PARENTHESES);
      type = exp(); // Catches the type of the expression
      matchToken(TokenEnum.CLOSE_PARENTHESES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      idType = SymbolTable.getType(lAnalyzer.lexeme);
      matchToken(TokenEnum.ID);
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);
        type = exp(); // Catches the type of the expression

        if (type != TypeEnum.INTEGER) {
          // Throws an error and exit the program
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.VALUE) {
      if (Character.isDigit(lAnalyzer.lexeme.charAt(0))
          && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) != 'h') {
        valueInt = Integer.parseInt(lAnalyzer.lexeme);
        type = v(); // Catches the type of the value
      } else {
        valueChar = lAnalyzer.lexeme;
        type = v(); // Catches the type of the value
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FALSE) {
      type = v();
    } else {
      // Throws an error and exit the program
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (idType != null) {
      type = idType;
    }

    return type;
  }
}