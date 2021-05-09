
/**   
 *  Componentes do grupo
 *  Carol Lima - 561397
 *  Jorge Oliveira - 559855
 *  Ricardo Xavier Sena - 481694
 * 
 */

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

public class L {
    public static void main(String[] args) throws IOException {
        try {
            // Sets the encode of the file
            System.setProperty("file.encoding", "UTF-8");

            // Gets the file
            String file = args[0];

            // Instance of the lexical analyzer
            LexicalAnalyzer lAnalyzer = new LexicalAnalyzer(new BufferedReader(new FileReader(file)));

            // Instance of the syntax analyzer
            Parser sAnalyzer = new Parser(lAnalyzer);

            // Calls the method responsible to realize the syntax analysis
            sAnalyzer.parse();
        } catch (Exception ex) {
            // Throws an error and exit the program
            System.err.println("ERRO:" + ex + " Não foi possível ler o arquivo.");
        }
    }
}
