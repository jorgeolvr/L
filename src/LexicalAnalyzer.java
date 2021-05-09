import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.BufferedReader;
import java.awt.event.KeyEvent;

public class LexicalAnalyzer {
  BufferedReader reader;
  int currentLine; // Number of the current line in the program
  int nextState; // Number of current state
  int finalState; // Number of the final state of the automatons

  SymbolTable symbolTable; // Instance of the SymbolTable
  String lexeme; // Responsible to make the lexeme from the character read
  char character; // Stores the character read in the program
  Boolean returnCharacter; // Indicates if a character was returned
  Boolean EOF; // Indicates if is end of the program

  // List of allowed symbols in the language
  List<Character> symbol = new ArrayList<Character>(Arrays.asList('<', '>', '=', '!', '+', '-', '/', '*', '(', ')', '[',
      ']', '{', '}', '\'', '\"', ';', ':', '_', ',', '?', '@', '%', '.'));

  // List of blank spaces in the language
  List<Character> blank = new ArrayList<Character>(Arrays.asList(' ', '\n', '\r', '\t'));

  // List of characters in the language
  List<Character> letter = new ArrayList<Character>(Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
      'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
      'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'));

  // List of digits in the language
  List<Character> digit = new ArrayList<Character>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));

  // Constructor responsible to initialize the variables
  LexicalAnalyzer(BufferedReader reader) {
    this.symbolTable = new SymbolTable();
    this.reader = reader;
    this.lexeme = "";
    this.character = '\0';
    this.currentLine = 1;
    this.nextState = 0;
    this.finalState = 2;
    this.returnCharacter = false;
    this.EOF = false;
  }

  /**
   * Method responsible to get the instance of the class Symboltable
   * 
   * @return instance of SymbolTable
   * 
   */
  public SymbolTable getSymbolTable() {
    return symbolTable;
  }

  /**
   * Reads the next character of the file if any character is returned
   * 
   * @throws IOException
   * 
   */
  public void readNextCharacter() throws IOException {
    try {
      if (returnCharacter == false) {
        character = (char) reader.read();
        checkInvalidCharacter(character);
        countNewLine(character);
      } else {
        returnCharacter = false;
      }

    } catch (Exception ex) {
      System.err.println(ex);
    }
  }

  /**
   * Checks the character and increases the counter if it is a line wrap
   * 
   * @param character the current character of the file
   * 
   */
  public void countNewLine(char character) {
    if (character == '\n') {
      currentLine++;
    }
  }

  /**
   * Checks if a character is invalid in the language and throws a error
   * 
   * @param character the current character of the file
   * 
   */
  public void checkInvalidCharacter(char character) {
    if (!letter.contains(character) && !digit.contains(character) && !symbol.contains(character)
        && !blank.contains(character) && (int) character != 65535) {
      System.out.println(currentLine + "\n" + "caractere invalido.");
      System.exit(1);
    }
  }

  /**
   * Verify if the character is printable or not
   * 
   * @param character the current character of the file
   * @return true or false
   * 
   */
  public boolean isPrintableCharacter(char character) {
    Character.UnicodeBlock block = Character.UnicodeBlock.of(character);
    return (!Character.isISOControl(character)) && character != KeyEvent.CHAR_UNDEFINED && block != null
        && block != Character.UnicodeBlock.SPECIALS;
  }

  /**
   * Verify if the file reaches the end of file
   * 
   * @return true to equivalent of EOF
   * 
   */
  public boolean isEOF() {
    return this.EOF;
  }

  /**
   * Search the lexeme in the SymbolTable and return the token if it exists
   * 
   * @param lexeme the current lexeme made by the automaton
   * 
   */
  public void searchLexeme(String lexeme) {
    if (!isEOF()) {
      Boolean searched = symbolTable.searchInSymbolTable(lexeme);
      if (searched == false) {
        if (Character.isLetter(lexeme.charAt(0)) || lexeme.charAt(0) == '_') {
          /**
           * All new identifier have the kind as empty when it's inserted in the symbol
           * table
           */
          symbolTable.insertSymbolTable(lexeme, TokenEnum.ID, KindEnum.EMPTY);
        } else if (Character.isDigit(lexeme.charAt(0)) && lexeme.charAt(lexeme.length() - 1) != 'h') {
          symbolTable.insertSymbolTable(lexeme, TokenEnum.VALUE, TypeEnum.INTEGER);
        } else {
          symbolTable.insertSymbolTable(lexeme, TokenEnum.VALUE, TypeEnum.CHARACTER);
        }
      }
    }
  }

  /**
   * Carry out the steps of the automaton from initial state to final state
   * 
   */
  public void automaton() throws IOException {
    lexeme = ""; // Clear the lexeme every time the automaton is called

    // Passes through the states until reaches the final state
    while (nextState != finalState) {
      readNextCharacter(); // Read a new character from the file

      switch (nextState) {
        case 0:
          state0();
          break;
        case 1:
          state1();
          break;
        case 3:
          state3();
          break;
        case 4:
          state4();
          break;
        case 5:
          state5();
          break;
        case 6:
          state6();
          break;
        case 7:
          state7();
          break;
        case 8:
          state8();
          break;
        case 9:
          state9();
          break;
        case 10:
          state10();
          break;
        case 11:
          state11();
          break;
        case 12:
          state12();
          break;
        case 13:
          state13();
          break;
        case 14:
          state14();
          break;
        case 15:
          state15();
          break;
        case 16:
          state16();
          break;
        case 17:
          state17();
          break;
        case 18:
          state18();
          break;
        case 19:
          state19();
          break;
        default:
          break;
      }
    }

    nextState = 0; // Reset the state to the inicial state
  }

  public void state0() throws IOException {
    if (character == ' ' || character == '\n' || character == '\r' || character == '\t') {
      nextState = 0;
    } else if (Character.isLetter(character)) {
      lexeme += character;
      nextState = 1;
    } else if (character == '_') {
      lexeme += character;
      nextState = 3;
    } else if (Character.isDigit(character) && character != '0') {
      lexeme += character;
      nextState = 5;
    } else if (character == '0') {
      lexeme += character;
      nextState = 6;
    } else if (character == '"') {
      lexeme += character;
      nextState = 4;
    } else if (character == '\'') {
      lexeme += character;
      nextState = 9;
    } else if (character == ':') {
      lexeme += character;
      nextState = 11;
    } else if (character == '>') {
      lexeme += character;
      nextState = 13;
    } else if (character == '<') {
      lexeme += character;
      nextState = 12;
    } else if (character == '/') {
      lexeme += character;
      nextState = 16;
    } else if (character == '=' || character == '(' || character == ')' || character == '+' || character == '-'
        || character == '*' || character == '{' || character == '}' || character == ',' || character == ';'
        || character == '%' || character == '[' || character == ']') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      EOF = true;
      nextState = finalState;
      searchLexeme(lexeme);
    } else {
      lexeme += character;

      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state1() throws IOException {
    if (character == '_' || Character.isDigit(character) || Character.isLetter(character)) {
      lexeme += character;
      nextState = 1;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state3() throws IOException {
    if (character == '_') {
      lexeme += character;
      nextState = 3;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else if (Character.isDigit(character) || Character.isLetter(character)) {
      lexeme += character;
      nextState = 1;
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state4() throws IOException {
    if (character == '"') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else if (character != '\n' && character != '$' && character != '\r' && character != '\t') {
      lexeme += character;
      nextState = 4;
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state5() throws IOException {
    if (Character.isDigit(character)) {
      lexeme += character;
      nextState = 5;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state6() throws IOException {
    if (Character.isDigit(character)) {
      lexeme += character;
      nextState = 14;
    } else if ((character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F')) {
      lexeme += character;
      nextState = 7;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state7() throws IOException {
    if (Character.isDigit(character) || (character >= 'a' && character <= 'f')
        || (character >= 'A' && character <= 'F')) {
      lexeme += character;
      nextState = 8;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state8() throws IOException {
    if (character == 'h' || character == 'H') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state9() throws IOException {
    if (isPrintableCharacter(character) && (int) character != 65535) {
      lexeme += character;
      nextState = 10;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state10() throws IOException {
    if (character == '\'') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state11() throws IOException {
    if (character == '=') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }

  public void state12() throws IOException {
    if (character == '>' || character == '=') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state13() throws IOException {
    if (character == '=') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state14() throws IOException {
    if (Character.isDigit(character)) {
      lexeme += character;
      nextState = 15;
    } else if ((character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F')) {
      lexeme += character;
      nextState = 19;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state15() throws IOException {
    if (Character.isDigit(character)) {
      lexeme += character;
      nextState = 5;
    } else if (character == 'h' || character == 'H') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state16() throws IOException {
    if (character == '*') {
      lexeme += character;
      nextState = 17;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      returnCharacter = true;
      nextState = finalState;
      searchLexeme(lexeme);
    }
  }

  public void state17() throws IOException {
    if (character != '*' && (int) character != 65535) {
      lexeme += character;
      nextState = 17;
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      lexeme += character;
      nextState = 18;
    }
  }

  public void state18() throws IOException {
    if (character == '*') {
      lexeme += character;
      nextState = 18;
    } else if ((int) character == 65535) {
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else if (character == '/') {
      nextState = 0;
      lexeme = "";
    } else if (character != '*' || character != '/') {
      lexeme += character;
      nextState = 17;
    }
  }

  public void state19() throws IOException {
    if (character == 'h' || character == 'H') {
      lexeme += character;
      nextState = finalState;
      searchLexeme(lexeme);
    } else if ((int) character == 65535) {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "fim de arquivo nao esperado.");
      System.exit(1);
    } else {
      // Throws an error and exit the program
      System.out.println(currentLine + "\n" + "lexema nao identificado" + " [" + lexeme + "]" + ".");
      System.exit(1);
    }
  }
}
