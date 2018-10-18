package searchTool;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.StringBuilder;
import java.lang.management.*;
import java.net.*;

import javax.management.*;

import searchTool.Webpage;

public class SearchTool {
	
	private static Date lastTimePaused = null;
	private int rootDepth = 0;
	private ArrayList<Webpage> next = new ArrayList<Webpage>(); 
	private ArrayList<Webpage> visited = new ArrayList<Webpage>();
	private HashSet<Webpage> container = new HashSet<Webpage>();
	public HashMap<Integer,HashSet<Webpage>> mapPagesByStatus = new HashMap<Integer,HashSet<Webpage>>();
	public HashMap<Webpage,HashSet<Webpage>> map404ParentToChildren = new HashMap<Webpage,HashSet<Webpage>>();
	public HashMap<Webpage,HashSet<Webpage>> map404ChildToParents = new HashMap<Webpage,HashSet<Webpage>>();
	public HashMap<String,HashSet<Webpage>> mapFileTypeToURL = new HashMap<String,HashSet<Webpage>>();
	public HashMap<Webpage,HashSet<ArrayList<Webpage>>> mapParentTo404Trace = new HashMap<Webpage,HashSet<ArrayList<Webpage>>>();
	private HashSet<String> setOfUniqueDomainNames = new HashSet<String>();
	private HashMap<Webpage ,String> problemsByPatterns = new HashMap<Webpage , String>();
	//Problems by patterns divides the 404 problems by the first folder in their path, note that the page that gives the 404 is given a folder, not the parent that links to it

	
/**
 * Initiates a SearchTool object with only one starting website
 * @param root
 * @param url
 * @throws IOException
 */
public SearchTool( String root, String url ) throws IOException {
		if(!root.equals("DEBUG")){
			testPath();
		}
		
		next.add(new Webpage(url));
		container.add(next.get(next.size()-1));
		next.get(next.size()-1).depth = (++rootDepth)+"";	
		MyDesignTimePatterns.AllowedDomainNames += " , " + next.get(next.size()-1).url.getHost();
}


/**
 * Tests root path given by user to assure validity and sets up folders for 
 * printed data 
 * @throws IOException
 */
public void testPath() throws IOException{
	File dir = new File(MyRunTimeConstants.RootPath), 
			dir200 = new File(MyDesignTimePatterns.dir200s), 
			dir404 = new File(MyDesignTimePatterns.dir404s), 
			dir400 = new File(MyDesignTimePatterns.dir4xxNot404s), 
			dir300 = new File(MyDesignTimePatterns.dir300s),
			dirNot200 = new File(MyDesignTimePatterns.dirNot200s),
			masterDir = new File(MyDesignTimePatterns.dirMaster),
			dumpDirectory = new File(MyDesignTimePatterns.dirDump),
			dirDotDot = new File(MyDesignTimePatterns.dirDotDot),
			dirProblemsByPatterns = new File(MyDesignTimePatterns.dirProblemsByPatterns),
			dirProblems = new File(MyDesignTimePatterns.dirProblems),
			testFile;
	PrintWriter writer;
	FileReader reader;
	BufferedReader bReader;
	
	if(dir.exists()){
	
		if(!masterDir.exists())
			masterDir.mkdir();
		if(!dumpDirectory.exists())
			dumpDirectory.mkdir();
		if(!dirProblems.exists())
			dirProblems.mkdir();
		if(!dirProblemsByPatterns.exists())
			dirProblemsByPatterns.mkdir();
		if(!dir404.exists())
			dir404.mkdir();
		if(!dir400.exists())
			dir400.mkdir();
		if(!dir300.exists())
			dir300.mkdir();
		if(!dir200.exists())
			dir200.mkdir();
		if(!dirNot200.exists())
			dirNot200.mkdir();
		if(!dirDotDot.exists())
			dirDotDot.mkdir();
	} else{
		dir.mkdirs();
		dirProblems.mkdir();
		dirProblemsByPatterns.mkdir();
		masterDir.mkdir();
		dumpDirectory.mkdir();
		dir404.mkdir();
		dir400.mkdir();
		dir300.mkdir();
		dir200.mkdir();
		dirNot200.mkdir();
		dirDotDot.mkdir();
		
		
	}
	
	testFile = new File(dumpDirectory+"\\test.txt");
	
	writer = new PrintWriter(dumpDirectory+"\\test.txt", "UTF-8");
	writer.println("testing");
	writer.close();
	
	reader = new FileReader(dumpDirectory+"\\test.txt");
	bReader = new BufferedReader(reader);
	bReader.readLine();
	bReader.close();
	reader.close();
	
	testFile.delete();
	


}


public HashMap<Webpage ,String> getPatternMap(){
	return problemsByPatterns;
}


/**
 * @return the ArrayList<Webpage> next
 */
public ArrayList<Webpage> getNext(){
	return next;
}

/**
 * @return the ArrayList<Webpage> visited
 */
public ArrayList<Webpage> getVisited(){
	return visited;
}

/**
 * Takes the page at next.get(0) and checks properties like content type, response code
 * If the webpage being accessed returns any code 300+, the method sets the groundwork for 
 * immediately accessing the redirected page, or handles reallocation of data if the redirected page 
 * was already checked
 * @return the webpage that was accessed
 * @throws IOException
 */
public Webpage gatherPreliminaryInformationAboutNextWebpageInQueue(){
	HttpURLConnection connection;
	Webpage newWebpage = null, redirectedPage;
	InetAddress address;
	boolean excludeThisURL = false;
	
	newWebpage = next.get(0);
	next.remove(0);
	
	MyDesignTimePatterns.appendToTextFile(newWebpage.url.toString(), MyDesignTimePatterns.processFile);
	
	if(MyRunTimeConstants.shouldWeExcludeCertainPatterns){
	checkForAnyExcludedPatterns(newWebpage);
	}
	
	if(excludeThisURL){
		MyDesignTimePatterns.appendToTextFile("Previous Link Excluded", MyDesignTimePatterns.processFile);
	}
	
	try {
		if(!visited.contains(newWebpage) && !excludeThisURL){
			
			address = InetAddress.getByName(newWebpage.url.getHost());
			connection = (HttpURLConnection)newWebpage.url.openConnection();
			connection.setConnectTimeout(MyDesignTimePatterns.webpageAccessTimeoutPeriod);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
			connection.connect();
			newWebpage.status = connection.getResponseCode();
			newWebpage.IPAddress = address.getHostAddress();
			if(newWebpage.status == 404 && newWebpage.isRelative && newWebpage.relativeURL.contains("//"))
				newWebpage.relativeURL += "---Possible mispelling of URL on parent page---";

			newWebpage.contentType = connection.getContentType();
			
			if(newWebpage.contentType != null ){
				if(newWebpage.contentType.contains(";")){
					newWebpage.contentType = newWebpage.contentType.substring(0, newWebpage.contentType.indexOf(";"));
				}
				newWebpage.contentType = newWebpage.contentType.toLowerCase();
			}
			visited.add(newWebpage);
			
			
			if(newWebpage.status >= 300 && newWebpage.status < 400 && MyRunTimeConstants.shouldWeFollowRedirections){
				newWebpage.redirect = new Webpage(connection.getHeaderField("Location"));
				
				
				if(visited.contains(newWebpage.redirect)){
					newWebpage.redirect = visited.get(visited.indexOf(newWebpage.redirect));
					redirectedPage = newWebpage.redirect;
					
					while(!newWebpage.parent.children.contains(redirectedPage)){
						newWebpage.parent.children.add(newWebpage.parent.children.indexOf(newWebpage)+1, redirectedPage);
						
						if(redirectedPage.redirect != null)
							redirectedPage = visited.get(visited.indexOf(redirectedPage.redirect));
					}
					
				}else if(container.contains(newWebpage.redirect)){
							newWebpage.redirect = next.get(next.indexOf(newWebpage.redirect));

							if(!newWebpage.parent.children.contains(newWebpage.redirect))
								newWebpage.parent.children.add(newWebpage.redirect);
							
							next.remove(next.indexOf(newWebpage.redirect));
							next.add(0, newWebpage.redirect);
					
				
				} else{
							next.add(0, newWebpage.redirect);
							container.add(newWebpage.redirect);
							newWebpage.redirect.parent = newWebpage.parent;
							if(newWebpage.redirect.parent == null){
								newWebpage.redirect.depth = ++rootDepth+"";
							}
							newWebpage.redirect.parent.children.add(newWebpage.redirect.parent.children.indexOf(newWebpage)+1, newWebpage.redirect);
				}
				
			} else if(MyRunTimeConstants.shouldWeFollowRedirections){
				MyDesignTimePatterns.appendToTextFile("3xx found, but not followed", MyDesignTimePatterns.progressTextFile);
			}
			connection.disconnect();
			
		} else if(visited.contains(newWebpage) && newWebpage.isRelative && (newWebpage.isRelative != visited.get(visited.indexOf(newWebpage)).isRelative)){
				visited.get(visited.indexOf(newWebpage)).isRelative = true;
				visited.get(visited.indexOf(newWebpage)).relativeURL = newWebpage.relativeURL;
				newWebpage = null;
		} else
			newWebpage = null;
			
	} catch (IOException e) {
		newWebpage.status = 0;
		newWebpage.contentType = e.toString();
		visited.add(newWebpage);
	
	} catch(IllegalArgumentException e){
		newWebpage.status = 0;
		newWebpage.contentType = e.toString();
		visited.add(newWebpage);
	
	}
	
	return newWebpage;
	
}

private boolean checkForAnyExcludedPatterns(Webpage page){
	
	if(!MyDesignTimePatterns.ignoreContainsPattern.isEmpty()){
		for(String pattern : MyDesignTimePatterns.ignoreContainsPattern){
			if(page.url.toString().contains(pattern)){
				page.status = 998;
				page.IPAddress = "Excluded from Processing";
				visited.add(page);		
				return true;
			}
		}
	}

	if(!MyDesignTimePatterns.ignoreStartsWithPattern.isEmpty()){
		for(String pattern : MyDesignTimePatterns.ignoreStartsWithPattern){
			if(page.url.toString().startsWith(pattern)){
				page.status = 998;
				page.IPAddress = "Excluded from Processing";
				visited.add(page);		
				return true;
			}
		}
	}
	
	if(!MyDesignTimePatterns.ignoreEndsWithPattern.isEmpty()){
		for(String pattern : MyDesignTimePatterns.ignoreEndsWithPattern){
			if(page.url.toString().endsWith(pattern)){
				page.status = 998;
				page.IPAddress = "Excluded from Processing";
				visited.add(page);		
				return true;
			}
		}
	}

	if(!MyDesignTimePatterns.ignoreEqualsPattern.isEmpty()){
		for(String pattern : MyDesignTimePatterns.ignoreEqualsPattern){
			if(page.url.toString().equals(pattern)){
				page.status = 998;
				page.IPAddress = "Excluded from Processing";
				visited.add(page);		
				return true;
			}
		}
	}
	
	if( !MyRunTimeConstants.shouldWeUseHTTPSURLS && page.url.toString().startsWith("https://")){
		page.status = 998;
		page.IPAddress = "Excluded from Processing";
		visited.add(page);
		MyDesignTimePatterns.appendToTextFile( new Date().toString() + " , " + page.url.toString() + page.parent.url.toString(), MyDesignTimePatterns.httpsLinksFile);
		return true;
		
	}
	
	return false;

}


/** 
 * returns the a stringbuilder containing the given wepage's complete html code 
 * this one allows the user to select directly from the array
 * @param index
 * @throws IOException 
 * **/
public StringBuilder loadPage (int index) throws IOException{
	return loadPage(visited.get(index));	
}


/** 
 * Returns the a stringbuilder containing the given wepage's complete html code 
 * this one allows the user to denote which webpage object to load. 
 * WARNING: the webpage to be loaded does not need to be in ArrayLsit visited
 * @param check
 * @throws IOException 
 * **/
public StringBuilder loadPage( Webpage check ){
	InputStream stream = null;
	BufferedInputStream buffStream = null;
	StringBuilder page = new StringBuilder();
	
	
	if(check.status == 200 && check.contentType != null && check.contentType.contains("text")){
	try {
		
		
			
			URL url = check.url;
			stream = url.openStream();
			buffStream = new BufferedInputStream(stream);
			int data;
		
	
			do{
				data = buffStream.read();
				if(data != -1)
					page.append((char)data);
			}while(data != -1);
		

		stream.close();
		buffStream.close();
	} catch (IOException e) {
		e.printStackTrace();
	
	}
	}	
		
		return page;	
}

/**
 * Gives the datminer object all the data that the searchtool has collected including 
 * allowed paths, visited pages, and allowed domains
 * @param miner
 */
public void giveData(Dataminer miner){
	for( Webpage page : visited){
		miner.pages.add(page);
		miner.container.add(page);
	}
	miner.problemsByPatterns = problemsByPatterns;
	miner.mapFileTypeToURL = mapFileTypeToURL;
}

/**
 * Removes the commented sections of the html page to avoid picking up commented out urls
 * @param page
 * @return the cleaned page
 */
public static StringBuilder cleanComments(StringBuilder page){
	String commentRegex = "<!--(?:.*?)-->";
	Pattern commentReg = Pattern.compile(commentRegex, Pattern.DOTALL);
	Matcher  commentMatcher = commentReg.matcher(page);
	
	page = new StringBuilder(commentMatcher.replaceAll("<!-- replaced -->"));
	
	return page;
}

/**
 * Collects all valid url links from the current loaded page 
 * and places them into ArrayList next. 
 * @param page
 * @throws IOException 
 */
public static ArrayList<String> matchURLs( StringBuilder page, Webpage parent) throws IOException{
	String matched;
	Pattern reg ;
	Matcher  matcher;
	ArrayList<String> matches = new ArrayList<String>();
	
	if(page.length() != 0){
		page = cleanComments(page);
		
		reg = Pattern.compile(MyDesignTimePatterns.REGEX_TO_PICK_UP_HTTP_URLS, Pattern.DOTALL);
		matcher = reg.matcher(page);
		while(matcher.find()){	
			matched = matcher.group();
			matched = matched.contains("\"") ?	matched.substring(matched.indexOf("\"") + 1, matched.lastIndexOf("\"")): matched.substring(matched.indexOf("'") + 1, matched.lastIndexOf("'"));
			matches.add(matched.toLowerCase());	
			
		}
		
		if(MyRunTimeConstants.shouldWeUseHTTPSURLS){
			reg = Pattern.compile(MyDesignTimePatterns.REGEX_TO_PICK_UP_HTTPS_URLS, Pattern.DOTALL);
			matcher = reg.matcher(page);
			while(matcher.find()){	
				matched = matcher.group();
				matched = matched.contains("\"") ?	matched.substring(matched.indexOf("\"") + 1, matched.lastIndexOf("\"")): matched.substring(matched.indexOf("'") + 1, matched.lastIndexOf("'"));
				matches.add(matched.toLowerCase());	
			
			}
		}
		
	}
	
	if(MyRunTimeConstants.shouldWeUseRelativeURLS){
		matches.addAll(matchRelativeURLs(page, parent));	
	}
	
	return matches;
}


public static ArrayList<String> matchRelativeURLs( StringBuilder page, Webpage parent) throws IOException{
	String matched;
	Pattern reg;
	Matcher  matcher;
	ArrayList<String> matches = new ArrayList<String>();
	
	if(page.length() != 0){
		page = cleanComments(page);
		
		if(MyRunTimeConstants.shouldWeUseRelativeSlashURLS){
			
			reg = Pattern.compile(MyDesignTimePatterns.REGEX_TO_PICK_UP_RELATIVE_SLASH_URLS, Pattern.DOTALL);
			matcher = reg.matcher(page);
			while(matcher.find()){	
				matched = matcher.group();
				matched = matched.contains("\"") ?	matched.substring(matched.indexOf("\"") + 1, matched.lastIndexOf("\"")): matched.substring(matched.indexOf("'") + 1, matched.lastIndexOf("'"));
				matches.add(matched.toLowerCase());	
			}	
		}
		
		if(MyRunTimeConstants.shouldWeUseRelativeNoSlashURLS){
			
			reg = Pattern.compile(MyDesignTimePatterns.REGEX_TO_PICK_UP_RELATIVE_NO_SLASH_URLS, Pattern.DOTALL);
			matcher = reg.matcher(page);
			while(matcher.find()){	
				matched = matcher.group();
				matched = matched.contains("\"") ?	matched.substring(matched.indexOf("\"") + 1, matched.lastIndexOf("\"")): matched.substring(matched.indexOf("'") + 1, matched.lastIndexOf("'"));
				matched = matched.toLowerCase();
				if(!matched.startsWith("http") && !matched.startsWith("/") && !matched.startsWith("../") && !matched.startsWith("mailto:") && !matched.startsWith("#") && !matched.endsWith(";") && !matched.contains("javascript"))
				matches.add(matched);	
			}
		}
		
		if(MyRunTimeConstants.shouldWeUseDotDotURLS){
			reg = Pattern.compile(MyDesignTimePatterns.REGEX_TO_PICK_UP_DOT_DOT_URLS, Pattern.DOTALL);
			matcher = reg.matcher(page);
			while(matcher.find()){	
				matched = matcher.group();
				matched = matched.contains("\"") ?	matched.substring(matched.indexOf("\"") + 1, matched.lastIndexOf("\"")): matched.substring(matched.indexOf("'") + 1, matched.lastIndexOf("'"));
				matches.add(matched.toLowerCase());	
			}
		}
	}
	return matches;
}
/**
 * @param page
 * @return The breadth at which page is located
 */
public int breadth(Webpage page){
	String depth, check = "-";
	int count = 0;
	
	if(page != null){
		
		depth = page.depth;
		
		while(depth.contains(check)){
			count++;
			depth = depth.substring(depth.indexOf(check)+1);
		}
	}
		return count;
}

/**
 * Cross references the given page's domain name with all allowed domain names
 * @param page
 * @return True if page's domain is also an allowed domain, false otherwise
 */
public boolean domainCheck(Webpage page){
	String domainName = page.url.getHost().toLowerCase();
	
	if(  !setOfUniqueDomainNames.contains(domainName) ){
		setOfUniqueDomainNames.add(domainName);	
		MyDesignTimePatterns.appendToTextFile(page.IPAddress + " , " + domainName, MyDesignTimePatterns.domainNameFile);
		if(page.IPAddress != null)
			MyDesignTimePatterns.appendToTextFile(page.IPAddress, MyDesignTimePatterns.ipFile);
	}
	
	
	return MyDesignTimePatterns.AllowedDomainNames.contains(domainName);
	}

public void sortSite(Webpage page){
	
	addToHashMap(mapPagesByStatus, page.status, page);
	addToHashMap(mapFileTypeToURL, page.contentType, page);
	
	if(page.parent != null && page.status == 404){
		addToHashMap(map404ParentToChildren, page.parent, page);
		addToHashMap(map404ChildToParents, page, page.parent);
		addToTraceMap( page.parent, page);
	}
	
	for(Webpage child : page.children){
		if(child.status == 404){
			addToHashMap(map404ChildToParents, child, page);
		}
	}
	
}

public void writeSite(Webpage page){
	String progressText =  page.IPAddress + " , " + page.url;
	
	if (page.isRelative){
		progressText +=  " , " + page.relativeURL;
	} 
	if(page.parent != null){
		progressText += " , " + page.parent.url;
	}
	
	MyDesignTimePatterns.appendToTextFile(progressText, MyDesignTimePatterns.masterFile);

	if(page.isRelative){
		if(page.relativeURL.startsWith("/")) {
			MyDesignTimePatterns.appendToTextFile(  page.status + " , " + progressText , MyDesignTimePatterns.forwardSlashRelativeLinkFile);
		} else if(page.relativeURL.startsWith("../")){
			MyDesignTimePatterns.appendToTextFile(  page.status + " , " + progressText , MyDesignTimePatterns.dotDotRelativeLinkFile);
		} else{
			MyDesignTimePatterns.appendToTextFile(  page.status + " , " + progressText , MyDesignTimePatterns.noSlashRelativeLinkFile);
		}
	}
	
	if(page.status == 200){
		MyDesignTimePatterns.appendToTextFile(progressText, MyDesignTimePatterns.fileProgressive200s);
	}else{
		MyDesignTimePatterns.appendToTextFile(progressText + page.status, MyDesignTimePatterns.fileNot200s);
		if(page.status > 299 && page.status < 400){
			MyDesignTimePatterns.appendToTextFile(progressText + page.status, MyDesignTimePatterns.fileProgressive300s);
		}else if(page.status > 399 && page.status < 500 && page.status != 404){
			MyDesignTimePatterns.appendToTextFile(progressText + page.status, MyDesignTimePatterns.fileProgressive300s);
		}else if(page.status == 404){
			MyDesignTimePatterns.appendToTextFile(progressText, MyDesignTimePatterns.fileProgressive404s);
		}
	}
	
	//TODO create a txt file not200snot404s 
	MyDesignTimePatterns.appendToTextFile( visited.size()+ " , " + page.depth + " , "+ MyDesignTimePatterns.getCurrentDateTimeInMyFormat() + "," + progressText, MyDesignTimePatterns.progressTextFile);
		
}


/**
 * @return Current cpu usage by the program
 * @throws Exception
 */
public static double getProcessCpuLoad() throws Exception {

    MBeanServer mbs    = ManagementFactory.getPlatformMBeanServer();
    ObjectName name    = ObjectName.getInstance("java.lang:type=OperatingSystem");
    AttributeList list = mbs.getAttributes(name, new String[]{ "ProcessCpuLoad" });

    if (list.isEmpty())     return Double.NaN;

    Attribute att = (Attribute)list.get(0);
    Double value  = (Double)att.getValue();

    // usually takes a couple of seconds before we get real values
    if (value == -1.0)      return Double.NaN;
    // returns a percentage value with 1 decimal point precision
    return ((int)(value * 1000) / 10.0);
}

/**
 * @return current memory and cpu usage by the program
 */
public static String getProcessResourceLoad(){
	String returnValue = null;
	
	try {
		returnValue = 
				"CPU load:" + getProcessCpuLoad() + "%" +
				"\nMemory usage:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1048576.0);
	} catch (Exception e) {
	
		e.printStackTrace();
	} 
	
	return returnValue;
}

/**
 * Allows the program to retrieve pages directly from the hashSet container
 * @param page
 * @return the Webpage to be found, null if operation fails or if page does not exist
 */
public Webpage getPageFromContainer( Webpage page){
	if(container.contains(page))
		for(Webpage containerPage: container)
			if(page.equals(containerPage))
				return containerPage;
	return null;
}

public Webpage buildRelativeURL(String URL, Webpage parent) throws IOException{
	Webpage checkWebpage= null;

	if(URL.startsWith("/")){  
		checkWebpage = buildRelativeSlashURL(URL, parent);
		
	
	}else if(URL.startsWith("../")){
		checkWebpage =  buildRelativeDotDotURL(URL, parent);

	
	} else{
		checkWebpage = buildRelativeNoSlashURL(URL, parent);
	}
   
	return checkWebpage;
}


public Webpage buildRelativeSlashURL(String URL, Webpage parent)throws  IOException{
	Webpage checkWebpage= null;
	String host = parent.url.getHost();

	
		checkWebpage = new Webpage("http://" + host + URL);
		checkWebpage.isRelative = true;
		checkWebpage.relativeURL = URL;	
   
	return checkWebpage;
	
	
}

public Webpage buildRelativeNoSlashURL(String URL, Webpage parent)throws  IOException{
Webpage checkWebpage= null;
String host = parent.url.getHost(), path = parent.url.getPath();

	
	if(path.endsWith("/")){
		path = path.substring(0, path.lastIndexOf("/"));
	}
		
	checkWebpage =  new Webpage("http://" + host + path.substring(0, path.lastIndexOf("/")) + "/" + URL);
	checkWebpage.isRelative = true;
	checkWebpage.relativeURL = URL;	

return checkWebpage;
}


public Webpage buildRelativeDotDotURL(String URL, Webpage parent) throws IOException{
	Webpage checkWebpage= null;
	String host = parent.url.getHost();

		String temp = URL;
	
	while(temp.contains("../")){		
		temp = temp.substring(temp.indexOf("/")+1);				
	}
	
	checkWebpage =  new Webpage("http://" + host + temp);
	checkWebpage.isRelative = true;
	checkWebpage.relativeURL = URL;	
   
	return checkWebpage;
}


/**
 * builds the URLs that will be used in limitedBreadthSearch
 * @param URL
 * @param parent
 * @return
 */
public Webpage buildURL(String URL, Webpage parent) throws IOException{
	Webpage checkWebpage = null;
	String host = parent.url.getHost();
	
		
		if(MyRunTimeConstants.shouldWeUseRelativeURLS && !URL.startsWith("http")){
			return buildRelativeURL(URL, parent);
			
		}else
			checkWebpage= new Webpage(URL);
		
		if(checkWebpage.url.toString().startsWith("https://") && MyRunTimeConstants.shouldWeUseHTTPSURLS){
			processHttps(URL, parent , checkWebpage);
		}	
		
		MyDesignTimePatterns.appendToTextFile(checkWebpage.url + "," + URL + "," + host, MyDesignTimePatterns.absoluteLinkFile);
		
		return checkWebpage;
		
}


public void processHttps(String URL, Webpage parent, Webpage child) throws IOException{
		
			MyDesignTimePatterns.appendToTextFile( new Date().toString() + " , " + child.url.toString() + " , " + child.parent.url.toString(), MyDesignTimePatterns.httpsLinksFile);	
			
		/*
		 * the function does little more than print the newly formed url, as http and https urls are bot built the same way
		 * but at the very least, this gives room for https urls to be treated seperately after being built
		 */
		

		
	
}

/**
 * Extracts the first part of a URL's path
 * I.E. http://www.domain.com/[this one]/[not]/[any]/[of]/[these]
 * @param page
 * @return the first part of the path
 */
public static String extractPathPattern(Webpage page){
	String pagePath = page.url.getPath(), returnValue = MyRunTimeConstants.FOLDER_NAME_FOR_NO_PATTERN;
	if(!pagePath.isEmpty()){
		pagePath = pagePath.substring(1);
	}
	if(pagePath.contains("/") && pagePath.length() > 1){
		
		returnValue = pagePath.substring(0, pagePath.indexOf("/"));
	}
		
	return returnValue;
}

public void addPatternToHashmap( String pattern, Webpage page){
	problemsByPatterns.put(page, pattern);
}

public void processPatternMap( Webpage page){

		String pattern = extractPathPattern(page);
		if(!problemsByPatterns.containsKey(page.url.toString()))
			addPatternToHashmap( pattern, page);
		if(!MyDesignTimePatterns.dirPatterns.contains(MyDesignTimePatterns.dirProblemsByPatterns + "\\" + pattern + "\\404s")){
			new File(MyDesignTimePatterns.dirProblemsByPatterns + "\\" + pattern ).mkdir();
			MyDesignTimePatterns.dirPatterns.add( MyDesignTimePatterns.dirProblemsByPatterns + "\\" + pattern + "\\404s");
		}
	
}

/**
 * The bulk of the program's 
 * 
 * @param iterations
 */
public void limitedBreadthSearch(int iterations){
	Webpage newWebpage = null, checkWebpage = null;
	ArrayList<String> matches = new ArrayList<String>();	
	StringBuilder report =  new StringBuilder();
		
	iterations--;
	lastTimePaused = new Date();	
		
	
	
	try {		
		
		MyDesignTimePatterns.appendToTextFile("Resource Usage:", MyDesignTimePatterns.dumpFile);
		while(!next.isEmpty()){
			
			newWebpage = gatherPreliminaryInformationAboutNextWebpageInQueue();
				
			lastTimePaused = new Date();		
			report =new StringBuilder("\nat "+ lastTimePaused + "\n"+ getProcessResourceLoad() + "\n");				
			MyDesignTimePatterns.appendToTextFile(report.toString(), MyDesignTimePatterns.dumpFile);
			
			if(newWebpage != null){
				if(newWebpage.parent!=null){
					newWebpage.depth = newWebpage.parent.depth + "-" + String.format("%d", newWebpage.parent.children.indexOf(newWebpage) + 1);
				}
				
				
				if(newWebpage.status == 404){
					processPatternMap(newWebpage);
				}
				
				writeSite(newWebpage);	
				
 				if(breadth(newWebpage) < iterations){
					
					if(domainCheck(newWebpage)){
						matches = matchURLs(loadPage(newWebpage), newWebpage);
						report = new StringBuilder("On page: " + newWebpage);
						
						for( String match : matches){
							report.append("\n\t\t" + match);
						}
						
						MyDesignTimePatterns.appendToTextFile(report.toString(), MyDesignTimePatterns.matchFile);
						
						printToConsole(newWebpage.depth);
					}
				
					
					while(!matches.isEmpty()){
						
						
						checkWebpage = buildURL(matches.get(0), newWebpage);
						if(checkWebpage != null){
							
						if(container.contains(checkWebpage))
							newWebpage.children.add(getPageFromContainer(checkWebpage));
						else{
							next.add(checkWebpage);
							container.add(checkWebpage);
							checkWebpage.parent = newWebpage;
							newWebpage.children.add(checkWebpage);
						}
						}
						matches.remove(0);
					}
				}
				sortSite(newWebpage);
			}
		}
		
		MyDesignTimePatterns.appendToTextFile("Search Over. Printing within searchtool.", MyDesignTimePatterns.progressTextFile);
		
		printFromHashMap(MyDesignTimePatterns.SearchToolChildFirst404file, map404ChildToParents);
		printFromHashMap(MyDesignTimePatterns.SearchToolParentFirst404file, map404ParentToChildren);
		
		MyDesignTimePatterns.appendToTextFile("SearchTool Print Over", MyDesignTimePatterns.progressTextFile);
		
	} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}
	
public static void printToConsole( Object o){
	//System.out.println(o.toString());
		
}
	
public static void addToHashMap( HashMap<Webpage,HashSet<Webpage>> map , Webpage key , Webpage value){
	if(!map.containsKey(key)){
		map.put(key, new HashSet<Webpage>());
		map.get(key).add(value);
	} else {
		map.get(key).add(value);
	}
}
	
public static void addToHashMap( HashMap<String,HashSet<Webpage>> map , String key , Webpage value){
	if(!map.containsKey(key)){
		map.put(key, new HashSet<Webpage>());
		map.get(key).add(value);
	} else {
		map.get(key).add(value);
	}
}

public void addToTraceMap(Webpage parent , Webpage child){
	if(!mapParentTo404Trace.containsKey(parent)){
		mapParentTo404Trace.put(parent, new HashSet<ArrayList<Webpage>>());
		mapParentTo404Trace.get(parent).add(traceRedirectionsToWebpageInSearchTool( parent, child));
	} else {
		mapParentTo404Trace.get(parent).add(traceRedirectionsToWebpageInSearchTool( parent, child));
	}
}
	
	public static void addToHashMap( HashMap<Integer,HashSet<Webpage>> map , Integer key , Webpage value){
		if(!map.containsKey(key)){
			map.put(key, new HashSet<Webpage>());
			map.get(key).add(value);
		} else {
			map.get(key).add(value);
		}
	}
	
	/**
	 * A similar function like this already exists in the dataminer class and works, i was just leaving the framework 
	 * here in case the searchtool would like to feature redirection tracing
	 * @param parent
	 * @param child
	 * @return
	 */
	public ArrayList<Webpage> traceRedirectionsToWebpageInSearchTool( Webpage parent, Webpage child){
		ArrayList<Webpage> tracePath = new ArrayList<Webpage>();
//		HashSet<Webpage> all301sAnd302s = new HashSet<Webpage>();
//		Webpage checkPage = child;
//		
//		
//			if(mapPagesByStatus.containsKey(301)){
//				all301sAnd302s.addAll(mapPagesByStatus.get(301));
//				}
//		
//			if(mapPagesByStatus.containsKey(302)){
//				all301sAnd302s.addAll(mapPagesByStatus.get(302));
//				}
//		
//		while( !tracePath.contains(checkPage.url.toString())){
//			 tracePath.add(checkPage);
//			 
//			 for(Webpage page : all301sAnd302s){
//				 if(parent != null && parent.children.contains(page) && page.redirect.equals(checkPage)){
//					 checkPage = page;
//					 break;
//				 }
//			 }
//		}	
		tracePath.add(child);
		return tracePath;
	}
	
	public void printFromHashMap( File file , HashMap< Webpage , HashSet<Webpage>> map){
		int outerCount = 0, innerCount;
		if(!map.isEmpty()){
			for(Webpage page : map.keySet()){
				MyDesignTimePatterns.appendToTextFile( ++outerCount + " , " + page, file);
				innerCount = 1;
				
				for(Webpage subpage : map.get(page)){	
					MyDesignTimePatterns.appendToTextFile( outerCount + "." +innerCount++ + " , " + subpage, file);
				}
			}
		}
		
	}
	
	public void reset(){
		next.clear();
		next.add(visited.get(0));
		visited.clear();
	}
	
}


