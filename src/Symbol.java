
/**   
 *  Componentes do grupo
 *  Carolina de Lima Silva - 561397
 *  Jorge Allan de Castro Oliveira - 559855
 *  Ricardo Xavier Sena - 481694
 * 
 */

public class Symbol {
  public String lexeme; // The lexeme of the Symbol
  public TokenEnum token; // The token of the Symbol
  public TypeEnum type; // The type of the Symbol
  public KindEnum kind; // The kind of the Symbol
  public Integer size; // The size of the Symbol
  public int address; // The address of thr Symbol

  /**
   * Constructor responsible initialize the lexeme and token of a Symbol
   * 
   * @param lexeme the lexeme of a Symbol
   * @param token  the token of a Symbol
   * 
   */
  Symbol(String lexeme, TokenEnum token) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = null;
    this.kind = null;
    this.size = null;
  }

  /**
   * Constructor responsible initialize the lexeme, token and type of a Symbol
   * 
   * @param lexeme the lexeme of a Symbol
   * @param token  the token of a Symbol
   * @param type   the type of a Symbol
   * 
   */
  Symbol(String lexeme, TokenEnum token, TypeEnum type) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = null;
    this.size = null;
  }

  /**
   * Constructor responsible initialize the lexeme, token and kind of a Symbol
   * 
   * @param lexeme the lexeme of a Symbol
   * @param token  the token of a Symbol
   * @param kind   the kind of a Symbol
   * 
   */
  Symbol(String lexeme, TokenEnum token, KindEnum kind) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = null;
    this.kind = kind;
    this.size = null;
  }

  /**
   * Constructor responsible initialize the lexeme, token, type and kind of a
   * Symbol
   * 
   * @param lexeme the lexeme of a Symbol
   * @param token  the token of a Symbol
   * @param type   the type of a Symbol
   * @param kind   the kind of a Symbol
   * 
   */
  Symbol(String lexeme, TokenEnum token, TypeEnum type, KindEnum kind) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = kind;
    this.size = null;
  }

  /**
   * Constructor responsible initialize the lexeme, token, type, kind and size of
   * a Symbol
   * 
   * @param lexeme the lexeme of a Symbol
   * @param token  the token of a Symbol
   * @param type   the type of a Symbol
   * @param kind   the kind of a Symbol
   * @param size   the kind of a Symbol
   * 
   */
  Symbol(String lexeme, TokenEnum token, TypeEnum type, KindEnum kind, Integer size) {
    this.lexeme = lexeme;
    this.token = token;
    this.type = type;
    this.kind = kind;
    this.size = size;
  }

  /**
   * Method responsible to get the lexeme of a Symbol
   * 
   * @return lexeme
   * 
   */
  public String getLexeme() {
    return lexeme;
  }

  /**
   * Method responsible to get the token of a Symbol
   * 
   * @return token
   * 
   */
  public TokenEnum getToken() {
    return token;
  }

  /**
   * Method responsible to get the type of a Symbol
   * 
   * @return type
   * 
   */
  public TypeEnum getType() {
    return type;
  }

  /**
   * Method responsible to get the kind of a Symbol
   * 
   * @return kind
   * 
   */
  public KindEnum getKind() {
    return kind;
  }

  /**
   * Method responsible to get the size of a Symbol
   * 
   * @return size
   * 
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Method responsible to get the address of a Symbol
   * 
   * @return address
   * 
   */
  public int getAddress() {
    return address;
  }

  /**
   * Method responsible to set the type of a Symbol
   * 
   * @param type the type of a Symbol
   * 
   */
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * Method responsible to set the kind of a Symbol
   * 
   * @param kind the kind of a Symbol
   * 
   */
  public void setKind(KindEnum kind) {
    this.kind = kind;
  }

  /**
   * Method responsible to set the size of a Symbol
   * 
   * @param size the size of a Symbol
   * 
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Method responsible to set the address of a Symbol
   * 
   * @param size the address of a Symbol
   * 
   */
  public void setAddress(int address) {
    this.address = address;
  }
}