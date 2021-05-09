public class Symbol {
  public String lexeme;
  public TokenEnum token;
  public TypeEnum type;
  public KindEnum kind;
  public int size;

  Symbol(String lexeme, TokenEnum token) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = null;
    this.kind = null;
  }

  Symbol(String lexeme, TokenEnum token, TypeEnum type) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = null;
  }

  Symbol(String lexeme, TokenEnum token, KindEnum kind) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = null;
    this.kind = kind;
  }

  Symbol(String lexeme, TokenEnum token, TypeEnum type, KindEnum kind) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = kind;
  }

  Symbol(String lexeme, TokenEnum token, TypeEnum type, KindEnum kind, int size) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = kind;
    this.size = size;
  }

  public String getLexeme() {
    return lexeme;
  }

  public TokenEnum getToken() {
    return token;
  }

  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public KindEnum getKind() {
    return kind;
  }

  public void setKind(KindEnum kind) {
    this.kind = kind;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getSize() {
    return size;
  }
}
