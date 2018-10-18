package searchTool;


import java.io.IOException;
import java.util.*;

public class SearchDriver {
	
	public static void main(String [ ] args){
		try{			
			MyDesignTimePatterns.setPathsForThisRun();
			MyRunTimeConstants.shouldWeUseRelativeURLS = true;
			MyRunTimeConstants.shouldWeUseDotDotURLS = true;
			MyRunTimeConstants.shouldWeUseRelativeSlashURLS = true;
			MyRunTimeConstants.shouldWeUseRelativeNoSlashURLS = true;
			MyRunTimeConstants.shouldWeUseHTTPSURLS = false;
			MyRunTimeConstants.printToMasterFile = false;
			MyRunTimeConstants.shouldWeExcludeCertainPatterns = true;
			MyRunTimeConstants.shouldWeFollowRedirections = false;
			String testString = "http://www.umd.edu/";
					
			SearchTool test = new SearchTool(MyRunTimeConstants.RootPath ,testString);
			Dataminer miner = new Dataminer();	
			ArrayList<Webpage> all404s, 
			//all200s, 
			//not200s,
			all300s, all400sNo404s, dotDots, allErrors, all404sInPattern = new ArrayList<Webpage>();			
			test.limitedBreadthSearch(10);
			test.giveData(miner);
			
			//all200s = miner.find200();
			//not200s = miner.findNot200();
			all300s = miner.find300To399();
			all400sNo404s = miner.find400To499();
			all404s = miner.findAll404();
			allErrors = miner.findErrors();
			dotDots = miner.findDotDot();
			
			miner.summarizeWebpagePopulations();
			
			
			//miner.writeSimple(MyDesignTimePatterns.dirMaster, miner.pages);
			//miner.writeSimple(MyDesignTimePatterns.dir200s, all200s);
			//miner.writeSimple(MyDesignTimePatterns.dirNot200s, not200s);
			
			MyDesignTimePatterns.appendToTextFile("Dataminer Starting : " + new Date().toString(), MyDesignTimePatterns.progressTextFile);

			miner.writeSimple(MyDesignTimePatterns.dir404s, all404s);
			miner.write(MyDesignTimePatterns.dir404s, all404s);
			miner.writeWithoutTrace(MyDesignTimePatterns.dir404s, all404s);
			miner.writeParentsFirst(MyDesignTimePatterns.dir404s, all404s);
			
			miner.write(MyDesignTimePatterns.dir300s, all300s);
			miner.write(MyDesignTimePatterns.dir4xxNot404s, all400sNo404s);
			miner.write(MyDesignTimePatterns.dirDump, allErrors);
			
			
//			miner.writeAllFormats(miner.folders[6], all404s);


			
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
			
			MyDesignTimePatterns.appendToTextFile("Dataminer Finished : " + new Date().toString(), MyDesignTimePatterns.progressTextFile);
//			miner.write(miner.folders[1], miner.pages);

			
//			folders[1] = masterDir.toString();	
//			folders[2] = dir200.toString();
//			folders[3] = dirNot200.toString();
//			folders[4] = dir300.toString();
//			folders[5] = dir400.toString();
//			folders[6] = dir404.toString();
//			folders[7] = dotDotLinks.toString();
//			folders[8] = dump.toString();
		} catch(IOException e){ 
			e.printStackTrace();
		}
	}

}
