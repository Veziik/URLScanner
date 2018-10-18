package searchTool;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;


public class MyDesignTimePatterns {
public final static String REGEX_FOR_ALL_URLS = "(?:href|src|url)=(\"|')[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public final static String REGEX_TO_PICK_UP_HTTPS_URLS = "(?:href|src|url)=(\"|')https:\\/\\/[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public final static String REGEX_TO_PICK_UP_DOT_DOT_URLS = "(?:href|src|url)=(\"|')\\.\\.\\/+[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public final static String REGEX_TO_PICK_UP_RELATIVE_SLASH_URLS = "(?:href|src|url)=(\"|')[../]+[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public final static String REGEX_TO_PICK_UP_HTTP_URLS = "(?:href|src|url)=(\"|')http:\\/\\/[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public final static String REGEX_TO_PICK_UP_RELATIVE_NO_SLASH_URLS = "(?:href|src|url)=(\"|')[A-Za-z0-9\\-\\.\\\\_~:\\/?#\\[\\]\\s@!$&%@#^\\(\\)*\\+,;=]+\\1";
public static String AllowedDomainNames ="www.montgomerycountymd.gov,www.mcgov.org";
public static String rootFolder = "D:\\webpageStatuses";
public static String dir404s;
public static String dirProblems;
public static String dir200s;
public static String dir300s;
public static String dir4xxNot404s; 
public static String dirDotDot;
public static String dirDump;
public static String dirMaster;
public static String dirNot200s;
public static String dirProblemsByPatterns;
public static File fileProgressive200s;
public static File fileNot200s;
public static File fileNot200sNot400s;
public static File fileProgressive404s;
public static File SearchToolChildFirst404file;
public static File SearchToolParentFirst404file;
public static File fileProgressive300s;
public static File masterFile;
public static File domainNameFile;
public static File forwardSlashRelativeLinkFile;
public static File noSlashRelativeLinkFile;
public static File dotDotRelativeLinkFile ;
public static File absoluteLinkFile ;
public static File httpsLinksFile;
public static File progressTextFile ;
public static File processFile ;
public static File debugFile;
public static File dumpFile;
public static File matchFile;
public static File typeFile;
public static File statusesFile;
public static File ipFile;
public static HashSet<String> dirPatterns = new HashSet<String>();
public static HashSet<String> ignoreStartsWithPattern = new HashSet<String>();
public static HashSet<String> ignoreEndsWithPattern = new HashSet<String>();
public static HashSet<String> ignoreContainsPattern = new HashSet<String>();
public static HashSet<String> ignoreEqualsPattern = new HashSet<String>();
public static int webpageAccessTimeoutPeriod = MyRunTimeConstants.RUNTIME_DEFAULT_TIMEOUT;
public enum DEPTH_LEVEL {
    DEBUG(2), SECOND_BREADTH(3), 
    THIRD_BREADTH(4), FOURTH_BREADTH(5),
    FIFTH_BREADTH(6), SIXTH_BREADTH(7),
    SEVENTH_BREADTH(8);

    private int value;

    DEPTH_LEVEL(int numVal) {
        this.value = numVal;
    }

    public int getNumVal() {
        return value;
    }
}

public static String getCurrentDateTimeInMyFormat(){
	return new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
}

public static void appendToTextFile(String textToWrite, File path){
	
	try(FileWriter fw = new FileWriter(path ,true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter writer = new PrintWriter(bw);){
	
		writer.print(textToWrite + "\n");
		writer.close();
	
	} catch (IOException e) {
	}
		

	
}


public String getDirFromDirPatterns( String dir){
	if(dirPatterns.contains(dir))
		for(String dirPatternsDir: dirPatterns)
			if(dir.equals(dirPatternsDir))
				return dir;
	return null;
}

public static void setPathsForThisRun(){
	String out = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
	MyRunTimeConstants.RootPath = MyDesignTimePatterns.rootFolder + "\\runasof_" + out;
	dirProblems = MyRunTimeConstants.RootPath + "\\OutputForCustomer";
	dirDump = MyRunTimeConstants.RootPath + "\\TechnicalInformation";
	dir200s = dirProblems + "\\200s";
	dirMaster = dirDump + "\\Master";
	dirProblemsByPatterns = dirProblems + "\\ProblemsbyDepartment";
	dir300s = dirProblems + "\\300s";
	dir404s = dirProblems + "\\404s";
	dir4xxNot404s = dirProblems + "\\4xxnot404s"; 
	dirDotDot = dirProblems + "\\DotDotUrls";
	dirNot200s = dirProblems + "\\Not200s";
	domainNameFile = new File(dirProblems + "\\UniqueDomainNames.txt");
	ipFile = new File(dirProblems + "\\UniqueIPAddresses.txt");
	forwardSlashRelativeLinkFile = new File(dirDump + "\\ForwardSlashRelativeLinks.txt");
	noSlashRelativeLinkFile = new File(dirDump + "\\NoSlashRelativeLinks.txt");
	dotDotRelativeLinkFile = new File(dirDump + "\\DotDotRelativeLinks.txt");
	absoluteLinkFile = new File(dirDump + "\\AbsoluteLinks.txt");
	httpsLinksFile = new File(dirDump + "\\httpsLinks.txt");
	progressTextFile = new File(dirDump + "\\ProgressText.txt");
	processFile = new File(dirDump + "\\WebpageToProcess.txt");
	typeFile = new File(dirProblems + "\\SummaryOfWebpageTypes.txt");
	statusesFile = new File(dirProblems + "\\SummaryOfWebpageStatuses.txt");
	debugFile = new File(dirDump + "\\debug.txt");
	dumpFile = new File(dirDump + "\\TechnicalInfo.txt");
	fileProgressive200s = new File(dirDump + "\\200s.csv");
	fileProgressive300s = new File(dirDump + "\\300s.csv");
	fileProgressive404s = new File(dirDump + "\\404s.csv");
	SearchToolChildFirst404file = new File(dirDump + "\\SearchTool404sChildFirst.csv");
	SearchToolParentFirst404file = new File(dirDump + "\\SearchTool404sParentFirst.csv");
	fileNot200s = new File(dirNot200s + "\\Not200s.csv");
	fileNot200sNot400s = new File(dirNot200s + "\\Not200sNot400s.csv");
	masterFile = new File(dirMaster + "\\MasterFile.csv");
	matchFile = new File(dirDump + "\\MatchesFound.txt" );
}



}

