package searchTool;

import java.io.IOException;
//import java.io.PrintWriter;
//import java.text.SimpleDateFormat;
import java.util.*;

//import searchTool.MyDesignTimePatterns.DEPTH_LEVEL;

public class AlphaShell {
	
	public static void main(String [ ] args){
		try{			
			 
			MyRunTimeConstants.shouldWeUseRelativeURLS = true;
			MyRunTimeConstants.shouldWeUseDotDotURLS = true;
			MyRunTimeConstants.shouldWeUseRelativeNoSlashURLS = true;
			MyRunTimeConstants.shouldWeUseRelativeSlashURLS = true;
			MyRunTimeConstants.shouldWeUseHTTPSURLS = false;
			int level;
			String testString, pathString;
			SearchTool test;
			Dataminer miner = new Dataminer();	
			ArrayList<Webpage> all404s, all200s, not200s,all300s, all400sNo404s, dotDots, allErrors, all404sInPattern = new ArrayList<Webpage>();			
			Scanner reader = new Scanner(System.in);
			System.out.println("URL Crawler v0.0.2\n"
					+ "EARLY ALPHA STAGE, REPRESENTATIVE OF ONLY ONE MONTH'S WORK, NOT FOR REDISTRIBUTION OR REPLICATION OF ANY KIND\n"
					+ "\nThis program was designed to search through a collection of webpages, collecting as much information as it can\n"
					+ "about each page for datamining and quality assurance purposes. Currently, it sorts the pages it finds by status\n"
					+ "and provides a list of any pages with code 404 found. The program also provides a list of 3xx, 4xx, and 5xx statuses\n"
					+ "along with a simple list of all urls with code 200. While the program runs, a folder named runasof[digits] will\n"
					+ "appear in the folder that you selected, this is where all  information will go afet the program has finished executing\n"
					+ "but will also provide files that are updated as the program runs as a sort of progress marker. Feel free to let the\n"
					+ "program run in the background while working, higher depth searches can take a while. After the program finishes, all\n"
					+ "information will be stored in that runasof folder in .csv format for easy imports into excel. Any files that are not 200\n"
					+ "will be stored in the 'Possible Problems' folder. This shell is meant to provide limited automation so please do make sure\n"
					+ "to follow given directions as accurately as possible. please note that https urls will not be scanned for status in this version\n\n");
			
			System.out.println("Enter the location to save to [C:\\something\\]. Enter 'default' for default path of C:\\WebpageStatuses\\");
			pathString = reader.nextLine();
			if(!(pathString.equals("default") || pathString.equals("'default'") )){
				System.out.println("Using path " + pathString);
				MyDesignTimePatterns.rootFolder = pathString;
				
			} else {
				System.out.println("Using default path");
			}
			
			MyDesignTimePatterns.setPathsForThisRun();
			
			System.out.println("Enter the Url to start from [http://www.[domain.name].[TLD] .\n"
					+ "Do not enter a page that immediatley redirects to another");
			testString = reader.nextLine();
			if(!testString.startsWith("http://")){
				System.out.println("WARNING: non http url detected, may not be able to start search correctly");
			}
			
			System.out.println("Enter the depth level of the scan [ x<1: the given url\n"
					+ "x == 2: the given url and all the sites it references\n"
					+ "x >= 3:the given url, all the sites it references, all the sites that those reference\netc,]");
			level = reader.nextInt();
			reader.close();
			
			System.out.println("Running Depth " + level);			
			
			test = new SearchTool(MyRunTimeConstants.RootPath ,testString);
			
			
			test.limitedBreadthSearch(level);
			test.giveData(miner);
			all200s = miner.find200();
			not200s = miner.findNot200();
			all300s = miner.find300To399();
			all400sNo404s = miner.find400To499();
			all404s = miner.findAll404();
			allErrors = miner.findErrors();
			dotDots = miner.findDotDot();
			
			miner.summarizeWebpagePopulations();
			
			all200s = miner.find200();
			not200s = miner.findNot200();
			all300s = miner.find300To399();
			all400sNo404s = miner.find400To499();
			all404s = miner.findAll404();
			allErrors = miner.findErrors();
			dotDots = miner.findDotDot();
			
			miner.summarizeWebpagePopulations();
			
			
			miner.writeSimple(MyDesignTimePatterns.dirMaster, miner.pages);
			miner.writeSimple(MyDesignTimePatterns.dir200s, all200s);
			miner.writeSimple(MyDesignTimePatterns.dirNot200s, not200s);
			
			
			miner.write(MyDesignTimePatterns.dir300s, all300s);
			miner.write(MyDesignTimePatterns.dir4xxNot404s, all400sNo404s);
			miner.write(MyDesignTimePatterns.dirDump, allErrors);
			
			
//			miner.writeAllFormats(miner.folders[6], all404s);
			miner.writeSimple(MyDesignTimePatterns.dir404s, all404s);
			miner.write(MyDesignTimePatterns.dir404s, all404s);
			miner.writeWithoutTrace(MyDesignTimePatterns.dir404s, all404s);
			miner.writeParentsFirst(MyDesignTimePatterns.dir404s, all404s);

			
			miner.write(MyDesignTimePatterns.dirDotDot, dotDots);
			
			if(!MyDesignTimePatterns.dirPatterns.isEmpty()){
				for(String route: MyDesignTimePatterns.dirPatterns){
				
					for(Webpage page : miner.problemsByPatterns.keySet()){
						if((MyDesignTimePatterns.dirProblemsByPatterns + "\\" + miner.problemsByPatterns.get(page) + "\\404s").equals(route)){
							all404sInPattern.add(page);
						}
					}
					if(!all404sInPattern.isEmpty()){
						miner.writeSimple(route, all404sInPattern);
						miner.write(route, all404sInPattern);
						miner.writeParentsFirst(route, all404sInPattern);
						all404sInPattern.clear();
					} 
					
					
				
			}
			}
			
		} catch(IOException e){ 
			e.printStackTrace();
		}
	}

}
