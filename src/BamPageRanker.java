import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * This class implements PageRank algorithm on simple graph structure.
 * Put your name(s), ID(s), and section here.
 *
 */
class Page{
	
	private int pid;
	private ArrayList<Integer> inLink;
	private ArrayList<Integer> outLink;
	private Double pageRank;
	
	public Page(int pid) {
		this.pid = pid;
		inLink = new ArrayList<Integer>();
		outLink = new ArrayList<Integer>();
		pageRank = 0.0;
	}
	
	public int getPid() {
		return pid;
	}
	
	public void setPid(int pid) {
		this.pid = pid;
	}
	
	public ArrayList<Integer> getInLink() {
		return inLink;
	}
	
	public void setInLink(ArrayList<Integer> list) {
		this.inLink = list;
	}
	
	public ArrayList<Integer> getOutLink() {
		return outLink;
	}

	public void setOutLink(ArrayList<Integer> outLink) {
		this.outLink = outLink;
	}
	
	public Double getPageRank() {
		return pageRank;
	}

	public void setPageRank(Double pageRank) {
		this.pageRank = pageRank;
	}

}

public class BamPageRanker {
	
	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into memory.
	 * Each line in the input file should have the following format:
	 * <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. 
	 * You can assume that a page ID is an integer.
	 * @throws Exception 
	 */
	Map<Integer, Page> pages = new HashMap<Integer, Page>();
	Map<Integer, Page> sink = new HashMap<Integer, Page>();
	Double d = 0.85;
	ArrayList<Double> perplexity = new ArrayList<Double>();
	
	public void loadData(String inputLinkFilename) throws Exception{
		File f = new File(inputLinkFilename);
		Scanner input = new Scanner(f);
		int pid, qid;
		Page p, q;
		
		while(input.hasNextLine())
		{	
			String line = input.nextLine();
			String[] tokens = line.trim().split(" ");
			pid = Integer.parseInt(tokens[0]);
			if(pages.containsKey(pid)){
				p = pages.get(pid);
			}
			else{
				p = new Page(pid);
				pages.put(pid, p);
			}
			if(tokens.length > 1){
				for(int i=1; i<tokens.length; i++){
					String token = tokens[i];
					qid = Integer.parseInt(token);
					if(pages.containsKey(qid)){
						q = pages.get(qid);
					}
					else{
						q = new Page(qid);
						pages.put(qid, q);
					}
					if(!q.getOutLink().contains(pid))
						q.getOutLink().add(pid);
					p.getInLink().add(qid);
				}
			}
			//System.out.println(pages.get(pid).getPid() + " " + pages.get(pid).getInLink().toString());
		}
		input.close();

		
	}
	

	/**
	 * This method will be called after the graph is loaded into the memory.
	 * This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize()
	{
		Double n = (double) pages.size();
		//System.out.println("N = " + n);
		for(Page p : pages.values()){
			if(p.getOutLink().isEmpty()){
				sink.put(p.getPid(), p);
			}
			p.setPageRank(1/n);
			//System.out.println("PID: " + p.getPid());
			//System.out.println("In-Link" + p.getInLink());
			//System.out.println("Out-Link" + p.getOutLink());
			//System.out.println("PageRank: " + p.getPageRank());
		}
		//System.out.println("There are " + pages.size() + " pages.");
		//System.out.println("There are " + sink.size() + " sink nodes.");
	}
	
	/**
	 * Computes the perplexity of the current state of the graph. The definition
	 * of perplexity is given in the project specs.
	 */
	public double getPerplexity()
	{
		Double h = (double) 0;
		Double perp;
		for(Page p : pages.values()){
			h += p.getPageRank()*(Math.log(p.getPageRank())/Math.log(2));
		}
		//System.out.println("H = " + h);
		perp = Math.pow(2, -h);
		//System.out.println("perp = " + perp);
		return perp;
	}
	
	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank algorithm).
	 * Returns false otherwise (and PageRank algorithm continue to update the page scores). 
	 */
	public boolean isConverge()
	{
		if(perplexity.size()>=4){
			if((perplexity.get(perplexity.size()-2) - perplexity.get(perplexity.size()-1)) >= 1 ||
				(perplexity.get(perplexity.size()-2) - perplexity.get(perplexity.size()-1)) <= -1)
				return false;
			else if((perplexity.get(perplexity.size()-2) - perplexity.get(perplexity.size()-1)) >= 1 ||
					(perplexity.get(perplexity.size()-2) - perplexity.get(perplexity.size()-1)) <= -1)
				return false;
			else if((perplexity.get(perplexity.size()-4) - perplexity.get(perplexity.size()-1)) >= 1 ||
					(perplexity.get(perplexity.size()-4) - perplexity.get(perplexity.size()-1)) <= -1)
				return false;
			else
				return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * The main method of PageRank algorithm. 
	 * Can assume that initialize() has been called before this method is invoked.
	 * While the algorithm is being run, this method should keep track of the perplexity
	 * after each iteration. 
	 * 
	 * Once the algorithm terminates, the method generates two output files.
	 * [1]	"perplexityOutFilename" lists the perplexity after each iteration on each line. 
	 * 		The output should look something like:
	 *  	
	 *  	183811
	 *  	79669.9
	 *  	86267.7
	 *  	72260.4
	 *  	75132.4
	 *  
	 *  Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate.
	 * 		The output should look something like:
	 * 		
	 * 		1	0.1235
	 * 		2	0.3542
	 * 		3 	0.236
	 * 		
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * @throws IOException 
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename) throws IOException
	{
		Double sinkPR, temp;
		Double n = (double) pages.size();
		int j=0;
		while(!isConverge()){
			sinkPR = 0.0;
			Map<Integer, Double> newPR = new HashMap<Integer, Double>();
			for(Page p : sink.values()){
				sinkPR += p.getPageRank();
			}
			for(Page p : pages.values()){
				temp = (1-d)/n;
				if(p.getPid() == 4){
					System.out.println("Temp1: " + temp);
				}
				temp += d*sinkPR/n;
				if(p.getPid() == 4){
					System.out.println("Temp2: " + temp);
				}
				for(int qid : p.getInLink()){
					Page q = pages.get(qid);
					int L = q.getOutLink().size();
					temp += d*q.getPageRank()/L;
					if(p.getPid() == 4){
						System.out.println("q: " + qid);
						System.out.println("PR(q): " + q.getPageRank());
						System.out.println("L(q): " + L);
						System.out.println("Temp3: " + temp);
					}
					//System.out.println("L(" + q.getPid() + ") = " + q.getOutLink().size());
				}
				newPR.put(p.getPid(), temp);
			}
			if(j == 0){
				//System.out.println("Initial PR: " + pages.get(2).getPageRank());
				System.out.println("Sink PR: " + sinkPR);
				//System.out.println("d: " + d);
				System.out.println("New PR: " + newPR.get(4));
			}
			//System.out.println("Iteration: " + j);
			for(Page p : pages.values()){
				p.setPageRank(newPR.get(p.getPid()));
				//System.out.println("PID: " + p.getPid());
				//System.out.println("PageRank: " + p.getPageRank());
			}
			perplexity.add(getPerplexity());
		}

		File perpFile = new File(perplexityOutFilename);
		File prFile = new File(prOutFilename);
		BufferedWriter perpWriter = new BufferedWriter(new FileWriter(perpFile));
		BufferedWriter prWriter = new BufferedWriter(new FileWriter(prFile));
		//System.out.println("Perplexity: ");
		for(Double i : perplexity){
			//System.out.println(i);
			perpWriter.write(i + "\n");
		}
		//System.out.println("PageRank: ");
		for(Page p : pages.values()){
			//System.out.println(p.getPageRank());
			prWriter.write(p.getPid() + " " + p.getPageRank() + "\n");
		}
		perpWriter.close();
		prWriter.close();
	}
	
	
	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K)
	{
		Integer[] rankedPages;
		if(K < pages.size()){
			rankedPages = new Integer[K];
		}
		else{
			rankedPages = new Integer[pages.size()];
		}
		List<Entry<Integer, Page>> list = new ArrayList<>(pages.entrySet());
	    Collections.sort(list, new Comparator<Map.Entry<Integer, Page>>() {
	    	@Override
	        public int compare(Map.Entry<Integer, Page> o1, Map.Entry<Integer, Page> o2) {
	            return (((Map.Entry<Integer, Page>) (o1)).getValue().getPageRank()).compareTo(((Map.Entry<Integer, Page>) (o2)).getValue().getPageRank());
	        }
	    });
	    for(int i=0; i<K; i++){
	    	if(i >= list.size()){
	    		break;
	    	}
	    	rankedPages[i] = list.get(list.size()-(i+1)).getKey();
	    }
		return rankedPages;
	}
	
	public static void main(String args[]) throws Exception
	{
	long startTime = System.currentTimeMillis();
		BamPageRanker pageRanker =  new BamPageRanker();
		pageRanker.loadData("citeseer.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(100);
	double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}
