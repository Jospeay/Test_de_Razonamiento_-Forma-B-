package JPA.ni.Test_Razonamiento_B.run;

import org.openxava.util.*;

/**
 * Execute this class to start the application.
 */

public class Test_Razonamiento_B {

	public static void main(String[] args) throws Exception {
		//DBServer.start("Test_Razonamiento_B-db"); // To use your own database comment this line and configure src/main/webapp/META-INF/context.xml
		AppServer.run("Test_Razonamiento_B"); // Use AppServer.run("") to run in root context
	}

}
