package scripts.filesAndDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;

public class MoveBadFiles {
	private static final String SSDIRECTORY = System.getenv("ENRON_DIR"),
			SSEXCEPTION = "SS/EXC",
			SSERROR = "SS/ERR";

	public static void main(String[] args) throws Exception {
		File dir = new File(SSDIRECTORY);
		
		if (!dir.isDirectory()) {
  		System.err.println("SSDirectory is not a directory!");
  		return;
		}
		
		BufferedReader read = new BufferedReader(new FileReader(System.getenv("ENRON_DIR") + "../../round1.txt"));
		Scanner scan = new Scanner(read);
		
		HashSet<String> filesToExclude = getFiles(scan);
		
		for (File file : dir.listFiles()) {
			if (!file.isFile()) continue;
			String name = file.toString().replaceAll("\\\\", "/").replaceAll(" ", "_");
			
			if (filesToExclude.contains(name)) {
				boolean success = file.renameTo(new File(name.replaceAll("SS", "SS/BAD")));
				System.out.println(name + " moved: " + success);
				continue;
			}
		}
	}
	
	public static HashSet<String> getFiles(Scanner scan) {
		HashSet<String> files = new HashSet<String>();
		while (scan.hasNext()) {
			String next = scan.nextLine().trim();
			if (next.startsWith("C:/")) {
				String file = next.replaceAll(SSERROR, "SS").replaceAll(SSEXCEPTION, "SS");
				files.add(file);
			}
		}
		
		return files;
	}
}