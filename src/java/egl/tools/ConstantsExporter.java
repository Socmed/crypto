
package egl.tools;

import egl.http.GetConstants;
import egl.util.JSON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class ConstantsExporter {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: ConstantsExporter <destination constants.js file>");
            System.exit(1);
        }

        Writer writer;
        try {
            writer = new FileWriter(new File(args[0]));
            writer.write("if (!NRS) {\n" +
                    "    var NRS = {};\n" +
                    "    NRS.constants = {};\n" +
                    "}\n\n");
            writer.write("NRS.constants.SERVER = ");
            JSON.writeJSONString(GetConstants.getConstants(), writer);
            writer.write("\n\n" +
                    "if (isNode) {\n" +
                    "    module.exports = NRS.constants.SERVER;\n" +
                    "}\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
