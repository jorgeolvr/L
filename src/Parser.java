import java.io.IOException;

public class Parser {
  Integer valueInt = null; // Access the value globally in the parser
  String valueChar = null; // Access the value globally in the parser
  LexicalAnalyzer lAnalyzer; // Instance of the lexical analyzer

  /**
   * Constructor responsible to make the first call of the automaton
   * 
   * @param analyzer the instance of the lexical analyzer
   * @throws IOException
   */
  public Parser(LexicalAnalyzer analyzer) throws IOException {
    this.lAnalyzer = analyzer;
    lAnalyzer.automaton();
  }

  /**
   * Verify if the current token matches the expected token and calls the
   * automaton
   * 
   * @param expectedToken the token that the parser is waiting to proceed the
   *                      analysis
   * @throws IOException
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
   * Verify the lexeme to see if it was already declared
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
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
   */
  public void setConstKind(String lexeme, TypeEnum type) {
    if ((type == TypeEnum.CHARACTER || type == TypeEnum.INTEGER || type == TypeEnum.LOGIC)
        && SymbolTable.getKind(lexeme) == KindEnum.EMPTY) {
      SymbolTable.setType(lexeme, type);
      SymbolTable.setKind(lexeme, KindEnum.CONST);

      // System.out.println(
      // "Lexema: " + lexeme + ", Type: " + SymbolTable.getType(lexeme) + ", Class: "
      // + SymbolTable.getKind(lexeme));
    }
  }

  /**
   * Sets the variable type, but first checks if the type is empty
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * @param type   type char, int or boolean
   */
  public void setVarKind(String lexeme, TypeEnum type) {
    if ((type == TypeEnum.CHARACTER || type == TypeEnum.INTEGER || type == TypeEnum.LOGIC)
        && SymbolTable.getKind(lexeme) == KindEnum.EMPTY) {
      SymbolTable.setType(lexeme, type);
      SymbolTable.setKind(lexeme, KindEnum.VAR);
    }
  }

  /**
   * Verifies if the types are compatibles
   * 
   * @param lexeme the token that the parser is waiting to proceed the analysis
   * @param type   types that are being analyzed
   */
  public void verifyAttributionCompatibility(String lexeme, TypeEnum type) {
    if (SymbolTable.getType(lexeme) != type) {
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }
  }

  /**
   * The initial method of the parser
   * 
   * @throws IOException
   */
  public void parse() throws IOException {
    try {
      s();
    } catch (Exception ex) {
      System.err.println("ERRO: " + ex);
    }
  }

  // S -> { D } main "{" { C } "}" EOF
  public void s() throws IOException {
    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FINAL
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.INT
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.CHAR
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.BOOLEAN) {
      d();
    }

    matchToken(TokenEnum.MAIN);
    matchToken(TokenEnum.OPEN_BRACES);

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

    if (lAnalyzer.isEOF()) {
      System.out.println(lAnalyzer.currentLine + " linhas compiladas.");
      System.exit(1);
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }
  }

  // D -> final id [ = [ ( - ) ] V ] {, id [ = [ ( - ) ] V ]}; | T
  public void d() throws IOException {
    String idLexeme = null; // Lexeme of the id
    TypeEnum valueType = null; // Type of the value in the attribution
    boolean signal = false; // True if have plus or minus signal

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FINAL) {
      matchToken(TokenEnum.FINAL);

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
        // Method that verifies if the id is already declared
        verifyIdentifier(lAnalyzer.lexeme);
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
        matchToken(TokenEnum.EQUAL);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
          signal = true;
          matchToken(TokenEnum.MINUS);
        }

        valueType = v();

        // The type must be integer if have plus or minus signal
        if (signal = true && valueType != TypeEnum.INTEGER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;

          // Method that sets the kind of the id as const
          setConstKind(idLexeme, valueType);
        }
      }

      while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
        matchToken(TokenEnum.COMMA);

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
          // Method that verifies if the id is already declared
          verifyIdentifier(lAnalyzer.lexeme);
          idLexeme = lAnalyzer.lexeme;
          matchToken(TokenEnum.ID);
        } else {
          System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
          System.exit(1);
        }

        if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
          matchToken(TokenEnum.EQUAL);

          if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
            signal = true;
            matchToken(TokenEnum.MINUS);
          }

          valueType = v();

          // The type must be integer if have plus or minus signal
          if (signal != false && valueType != TypeEnum.INTEGER) {
            System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
            System.exit(1);
          } else {
            // Method that sets the kind of the id as const
            setConstKind(idLexeme, valueType);
          }
        }
      }

      matchToken(TokenEnum.SEMICOLON);
    } else {
      t();
    }
  }

  // T -> (int|char|boolean) id (“[“ value “]” | := [ ( + | - ) ] V) {, id (“[“
  // value “]” | := [ ( + | - ) ] V)};
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
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      verifyIdentifier(lAnalyzer.lexeme);
      idLexeme = lAnalyzer.lexeme;
      setVarKind(lAnalyzer.lexeme, type);
      matchToken(TokenEnum.ID);
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      // The type must be integer to get the size
      if (SymbolTable.getType(lAnalyzer.lexeme) == TypeEnum.INTEGER) {
        size = Integer.parseInt(lAnalyzer.lexeme);

        // Impossible to create a vector if the size is zero or
        // greater than 8kb
        if (size > 0 && size <= 8192) {
          SymbolTable.setSize(idLexeme, size);
          matchToken(TokenEnum.VALUE);
        } else {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        }
      } else {
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

      valueType = v();

      // The type must be integer if have plus or minus signal
      if (signal != false && valueType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;

        // Method that compare if the type of id and value is the same
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
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }

      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        if (SymbolTable.getType(lAnalyzer.lexeme) == TypeEnum.INTEGER) {
          size = Integer.parseInt(lAnalyzer.lexeme);
          if (size > 0) {
            SymbolTable.setSize(idLexeme, size);
            matchToken(TokenEnum.VALUE);
          } else {
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          }
        } else {
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

        valueType = v();
        if (signal != false && valueType != TypeEnum.INTEGER) {
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

  // V -> value | TRUE | FALSE
  public TypeEnum v() throws IOException {
    TypeEnum type = null; // Type of the value, TRUE or FALSE

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.VALUE) {
      type = SymbolTable.getType(lAnalyzer.lexeme);
      matchToken(TokenEnum.VALUE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE) {
      type = SymbolTable.getType(lAnalyzer.lexeme);
      matchToken(TokenEnum.TRUE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FALSE) {
      type = SymbolTable.getType(lAnalyzer.lexeme);
      matchToken(TokenEnum.FALSE);
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    return type;
  }

  // C -> A | I | F | W | R | ;
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

  // R → readln “(“ id [ “[“ EXP “]” ] “)”
  public void r() throws IOException {
    TypeEnum expType = null;

    matchToken(TokenEnum.READLN);
    matchToken(TokenEnum.OPEN_PARENTHESES);

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) != KindEnum.EMPTY) {
        matchToken(TokenEnum.ID);
      } else {
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp();

      // Type must be integer
      if (expType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      matchToken(TokenEnum.CLOSE_BRACKETS);
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);
  }

  // L -> ( ATT | C ) {, C }
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
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);
      c();
    }
  }

  // ATT -> id [ “[“ EXP “]” ] [:= [ ( + | - ) ] EXP ]
  public void att() throws IOException {
    boolean signal = false;
    TypeEnum expType = null;
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
            + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      } else {
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    // [ “[“ EXP “]” ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp();

      if (expType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      // Se exp.value <= 0 || Se exp.value > 8192 || exp.value > idLexeme.size então
      // ERRO
      if (valueInt != null && valueInt <= 0) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > 8192) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
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

      expType = exp();

      if (signal != false && expType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, expType);

        // se idLexeme.size < exp.value.length (atribuição de char no vetor) então ERRO
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

  // F -> for “(“ [ L ] ; EXP ; [ L ] “)” ( C | “{“ { C ;}+ “}” )
  public void f() throws IOException {
    TypeEnum type = null;

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

    type = exp();

    if (type != TypeEnum.LOGIC) {
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
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }
  }

  // I -> if “(“ EXP “)” then ( C | “{“ { C } “}” ) [ else ( C | “{“ { C } “}” ) ]
  public void i() throws IOException {
    TypeEnum type = null;

    // if “(“ EXP “)” then
    matchToken(TokenEnum.IF);
    matchToken(TokenEnum.OPEN_PARENTHESES);
    type = exp();

    if (type != TypeEnum.LOGIC) {
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

  // A -> id [ “[“ EXP “]” ] [:= [ ( + | - ) ] EXP ] {, id [ “[“ EXP “]” ] [ := [
  // ( + | - ) ] EXP ] }
  public void a() throws IOException {
    boolean signal = false;
    TypeEnum expType = null;
    String idLexeme = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.VAR) {
        idLexeme = lAnalyzer.lexeme;
        matchToken(TokenEnum.ID);
      } else if (SymbolTable.getKind(lAnalyzer.lexeme) == KindEnum.CONST) {
        System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
            + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      } else {
        System.out.println(
            lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    // [ “[“ EXP “]” ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
      matchToken(TokenEnum.OPEN_BRACKETS);

      expType = exp();

      if (expType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

      // Se exp.value <= 0 || Se exp.value > 8192 || exp.value > idLexeme.size então
      // ERRO
      if (valueInt != null && valueInt <= 0) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > 8192) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
        System.exit(1);
      } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
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

      expType = exp();

      if (signal != false && expType != TypeEnum.INTEGER) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      } else {
        signal = false;
        verifyAttributionCompatibility(idLexeme, expType);

        // se idLexeme.size < exp.value.length (atribuição de char no vetor) então ERRO
        if (valueChar != null && SymbolTable.getSize(idLexeme) < valueChar.length() - 1
            && expType == TypeEnum.CHARACTER) {
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
          System.out.println(lAnalyzer.currentLine + "\n" + "classe de identificador incompatível" + " ["
              + lAnalyzer.lexeme + "]" + ".");
          System.exit(1);
        } else {
          System.out.println(
              lAnalyzer.currentLine + "\n" + "identificador nao declarado" + " [" + lAnalyzer.lexeme + "]" + ".");
          System.exit(1);
        }
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
        System.exit(1);
      }

      // [ “[“ EXP “]” ]
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);

        expType = exp();

        if (expType != TypeEnum.INTEGER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        // Se exp.value <= 0 || Se exp.value > 8192 || exp.value > idLexeme.size então
        // ERRO
        if (valueInt != null && valueInt <= 0) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (valueInt != null && valueInt > 8192) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
          System.exit(1);
        } else if (valueInt != null && valueInt > SymbolTable.getSize(idLexeme)) {
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

        expType = exp();

        if (signal != false && expType != TypeEnum.INTEGER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        } else {
          signal = false;
          verifyAttributionCompatibility(idLexeme, expType);

          // se idLexeme.size < exp.value.length (atribuição de char no vetor) então ERRO
          if (valueChar != null && SymbolTable.getSize(idLexeme) < valueChar.length() - 1
              && expType == TypeEnum.CHARACTER) {
            System.out.println(lAnalyzer.currentLine + "\n" + "tamanho do vetor excede o maximo permitido.");
            System.exit(1);
          } else {
            valueChar = null;
          }
        }
      }
    }
  }

  // W -> write “(“ EXP {, EXP } “)” | writeln “(“ EXP {, EXP } “)”
  public void w() throws IOException {
    TypeEnum type = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITE) {
      matchToken(TokenEnum.WRITE);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.WRITELN) {
      matchToken(TokenEnum.WRITELN);
    }

    matchToken(TokenEnum.OPEN_PARENTHESES);
    type = exp();

    if (type == TypeEnum.LOGIC) {
      System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
      System.exit(1);
    }

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.COMMA) {
      matchToken(TokenEnum.COMMA);
      type = exp();

      if (type == TypeEnum.LOGIC) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }
    }

    matchToken(TokenEnum.CLOSE_PARENTHESES);
  }

  // EXP -> EXPS [ ( = | <> | < | > | <= | >=) EXPS ]
  public TypeEnum exp() throws IOException {
    TypeEnum finalType = null;
    TypeEnum type1 = null;
    TypeEnum type2 = null;

    type1 = exps();

    // [ ( = | <> | < | > | <= | >=) EXPS ]
    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.EQUAL) {
      matchToken(TokenEnum.EQUAL);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;
      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT_EQUAL) {
      matchToken(TokenEnum.NOT_EQUAL);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;

      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_THAN) {
      matchToken(TokenEnum.LESS_THAN);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;

      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_THAN) {
      matchToken(TokenEnum.GREATER_THAN);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;

      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.LESS_OR_EQUAL) {
      matchToken(TokenEnum.LESS_OR_EQUAL);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;

      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.GREATER_OR_EQUAL) {
      matchToken(TokenEnum.GREATER_OR_EQUAL);
      type2 = exps();

      if (type1 == type2) {
        finalType = TypeEnum.LOGIC;

      } else {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else {
      finalType = type1;
    }

    return finalType;
  }

  // EXPS -> [ (+ | -) ] TERM { ( + | - | or) TERM }
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

    type1 = term();

    if (signal != false && type1 != TypeEnum.INTEGER) {
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
        valueAux = valueInt;
        type2 = term();

        if (valueAux != null && valueInt != null) {
          valueInt = valueAux + valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
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

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MINUS) {
        signal = true;

        matchToken(TokenEnum.MINUS);
        valueAux = valueInt;
        type2 = term();

        if (valueAux != null && valueInt != null) {
          valueInt = valueAux - valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
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
      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OR) {
        matchToken(TokenEnum.OR);
        type2 = term();

        if (type1 != type2) {
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

  // TERM -> FACTOR { (* | / | % | and) FACTOR }
  public TypeEnum term() throws IOException {
    boolean signal = false;
    TypeEnum type1 = null;
    TypeEnum type2 = null;
    TypeEnum finalType = null;
    Integer valueAux = null;

    type1 = factor();

    while (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.AND) {
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.MULTIPLY) {
        signal = true;
        matchToken(TokenEnum.MULTIPLY);

        valueAux = valueInt;
        type2 = factor();

        if (valueAux != null && valueInt != null) {
          valueInt = valueAux * valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
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

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.DIVIDE) {
        signal = true;
        matchToken(TokenEnum.DIVIDE);

        valueAux = valueInt;
        type2 = factor();

        if (valueAux != null && valueInt != null) {
          valueInt = (int) (valueAux / valueInt);
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
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

      } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.PERCENTAGE) {
        signal = true;
        matchToken(TokenEnum.PERCENTAGE);

        valueAux = valueInt;
        type2 = factor();

        if (valueAux != null && valueInt != null) {
          valueInt = valueAux % valueInt;
        }

        if (signal != false && type2 != TypeEnum.INTEGER) {
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
        type2 = factor();

        if (type1 != type2) {
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

  // FACTOR -> not FACTOR | “(“ EXP “)” | id [ “[“ EXP “]” ] | V
  public TypeEnum factor() throws IOException {
    TypeEnum type = null;
    TypeEnum idType = null;

    if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.NOT) {
      matchToken(TokenEnum.NOT);
      type = factor();

      if (type != TypeEnum.LOGIC) {
        System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
        System.exit(1);
      }

    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_PARENTHESES) {
      matchToken(TokenEnum.OPEN_PARENTHESES);
      type = exp();
      matchToken(TokenEnum.CLOSE_PARENTHESES);
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.ID) {
      idType = SymbolTable.getType(lAnalyzer.lexeme);
      matchToken(TokenEnum.ID);
      if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.OPEN_BRACKETS) {
        matchToken(TokenEnum.OPEN_BRACKETS);
        type = exp();

        if (type != TypeEnum.INTEGER) {
          System.out.println(lAnalyzer.currentLine + "\n" + "tipos incompativeis.");
          System.exit(1);
        }

        matchToken(TokenEnum.CLOSE_BRACKETS);
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.VALUE) {
      if (Character.isDigit(lAnalyzer.lexeme.charAt(0))
          && lAnalyzer.lexeme.charAt(lAnalyzer.lexeme.length() - 1) != 'h') {
        valueInt = Integer.parseInt(lAnalyzer.lexeme);
        type = v();
      } else {
        valueChar = lAnalyzer.lexeme;
        type = v();
      }
    } else if (SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.TRUE
        || SymbolTable.getToken(lAnalyzer.lexeme) == TokenEnum.FALSE) {
      type = v();
    } else {
      System.out.println(lAnalyzer.currentLine + "\n" + "token nao esperado" + " [" + lAnalyzer.lexeme + "]" + ".");
      System.exit(1);
    }

    if (idType != null) {
      type = idType;
    }

    return type;
  }
}