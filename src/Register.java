public class Register {
  int address; // The address of the Register
  TypeEnum type; // The type of the Register
  Integer size = null; // The size of the Register

  Register() {
    // Constructor just to create a new instance
  }

  /**
   * Constructor responsible initialize the address, type and size of a Register
   * 
   * @param address the address of a Register
   * @param type    the type of a Register
   * @param size    the size of a Register
   * 
   */
  Register(int address, TypeEnum type, Integer size) {
    this.address = address;
    this.type = type;
    this.size = size;
  }

  /**
   * Method responsible to get the address of a Register
   * 
   * @return address
   * 
   */
  public int getAddress() {
    return address;
  }

  /**
   * Method responsible to get the size of a Register
   * 
   * @return size
   * 
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Method responsible to get the type of a Register
   * 
   * @return type
   * 
   */
  public TypeEnum getType() {
    return type;
  }

  /**
   * Method responsible to set the address of a Register
   * 
   * @param address the address of a Register
   * 
   */
  public void setAddress(int address) {
    this.address = address;
  }

  /**
   * Method responsible to set the size of a Register
   * 
   * @param size the size of a Register
   * 
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Method responsible to set the type of a Register
   * 
   * @param type the type of a Register
   * 
   */
  public void setType(TypeEnum type) {
    this.type = type;
  }

}