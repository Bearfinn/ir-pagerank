import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This class implements PageRank algorithm on simple graph structure.
 * 
 * Dawit Chusetthagarn	5888032 Section 1
 * Weerawat Pawanawiwat 5888125 Section 1
 * Parintorn Pooyoi 	5888149 Section 1
 *
 */
public class TopPageRanker {

	// Node class for storing data of each node
	public class Node {
		int pageId;
		double pageRank;
		Set<Integer> inLinks; // List of documents that have links to this page
		Set<Integer> outLinks; // List of documents that have links from this page

		public Node(int id) {
			pageId = id;
			inLinks = new HashSet<Integer>();
			outLinks = new HashSet<Integer>();
		}
	}

	public Map<Integer, Node> pageGraph;
	public Set<Integer> sinkNodes;
	public ArrayList<Double> perplexity;
	public int numberOfNodes;

	/**
	 * This method reads the direct graph stored in the file "inputLinkFilename" into memory. Each line in the input file should have the following
	 * format: <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. You can assume that a page ID is an integer.
	 */
	public void loadData(String inputLinkFilename) {
		pageGraph = new HashMap<Integer, Node>();
		Scanner s = null;
		try {
			s = new Scanner(new File(inputLinkFilename));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Populating pageGraph
		while (s.hasNextLine()) {
			int pageId;
			int inId;
			Node n, n2;
			String line = s.nextLine();
			String[] tokens = line.split(" ");
			pageId = Integer.parseInt(tokens[0]);
			if (!pageGraph.containsKey(pageId)) {
				n = new Node(pageId);
				pageGraph.put(pageId, n);
			}
			else {
				n = pageGraph.get(pageId);
			}
			for (int i = 1; i < tokens.length; i++) {
				inId = Integer.parseInt(tokens[i]);
				if (!pageGraph.containsKey(inId)) {
					n2 = new Node(inId);
					pageGraph.put(inId, n2);
				}
				else {
					n2 = pageGraph.get(inId);

				}
				n.inLinks.add(inId);
				n2.outLinks.add(pageId);
			}
		}

		// Close the scanner
		s.close();
	}

	/**
	 * This method will be called after the graph is loaded into the memory. This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize() {
		sinkNodes = new HashSet<Integer>();
		perplexity = new ArrayList<Double>();
		numberOfNodes = pageGraph.size();

		// Determine sink nodes
		for (Node n : pageGraph.values()) {
			if (n.outLinks.size() == 0) {
				sinkNodes.add(n.pageId);
			}
		}

		// Initialize page rank score
		for (Node n : pageGraph.values()) {
			n.pageRank = 1.0 / numberOfNodes;
		}
	}

	/**
	 * Computes the perplexity of the current state of the graph. The definition of perplexity is given in the project specs.
	 */
	public double getPerplexity() {
		double sum = 0;
		for (Node n : pageGraph.values()) {
			sum += n.pageRank * (Math.log(n.pageRank) / Math.log(2));
		}
		System.out.println(Math.pow(2, -sum));
		return Math.pow(2, -sum);
	}

	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank algorithm). Returns false otherwise (and PageRank algorithm continue to
	 * update the page scores).
	 */
	public boolean isConverge() {
		if (perplexity.size() < 4) return false;
		for (int i = perplexity.size() - 1; i > perplexity.size() - 4; i--) {
			double p1 = perplexity.get(i);
			double p2 = perplexity.get(i - 1);
			if ((int) p1 % 10 != (int) p2 % 10 || p1 - p2 > 1.0) return false; // Second cond. prevents rare case of same unit with far values ex. 101, 91, 71, 31
		}
		return true;
	}

	/**
	 * The main method of PageRank algorithm. Can assume that initialize() has been called before this method is invoked. While the algorithm is being
	 * run, this method should keep track of the perplexity after each iteration.
	 * 
	 * Once the algorithm terminates, the method generates two output files. [1] "perplexityOutFilename" lists the perplexity after each iteration on each
	 * line. The output should look something like:
	 * 
	 * 183811 
	 * 79669.9
	 * 86267.7
	 * 72260.4
	 * 75132.4
	 * 
	 * Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate. The output should look something like:
	 * 
	 * 1 0.1235
	 * 2 0.3542
	 * 3 0.236
	 * 
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename) {
		double sinkPR;
		double newPR[] = new double[numberOfNodes];
		double d = 0.85;
		int index = 0;
		while (!isConverge()) {
			sinkPR = 0;
			index = 0;
			for (int s : sinkNodes) {
				sinkPR += pageGraph.get(s).pageRank;
			}

			for (Node n : pageGraph.values()) {
				newPR[index] = (1.0 - d) / numberOfNodes;
				newPR[index] += d * sinkPR / numberOfNodes;
				for (int in : n.inLinks) {
					newPR[index] += d * pageGraph.get(in).pageRank / pageGraph.get(in).outLinks.size();
				}
				index++;
			}
			index = 0;
			for (Node n : pageGraph.values()) {
				n.pageRank = newPR[index++];
			}
			perplexity.add(getPerplexity());
		}
		StringBuilder perp = new StringBuilder();
		StringBuilder pr = new StringBuilder();
		ArrayList<Integer> sortedPid = new ArrayList<Integer>(pageGraph.keySet());
		Collections.sort(sortedPid);
		for (double p : perplexity) {
			perp.append(p + "\n");
		}

		for (int pid : sortedPid) {
			pr.append(pageGraph.get(pid).pageId + " " + pageGraph.get(pid).pageRank + "\n");
		}
		BufferedWriter perpFile;
		BufferedWriter prFile;
		try {
			perpFile = new BufferedWriter(new FileWriter(perplexityOutFilename));
			prFile = new BufferedWriter(new FileWriter(prOutFilename));
			perpFile.write(perp.toString());
			prFile.write(pr.toString());
			perpFile.close();
			prFile.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K) {
		if (pageGraph.size() < K) K = numberOfNodes;
		double[][] ranking = new double[numberOfNodes][2];
		int i = 0;
		for (Node n : pageGraph.values()) {
			ranking[i][0] = n.pageId;
			ranking[i][1] = n.pageRank;
			i++;
		}

		Arrays.sort(ranking, new Comparator<double[]>() {
			public int compare(double[] val1, double[] val2) {
				return Double.compare(val2[1], val1[1]);
			}
		});
		Integer[] result = new Integer[K];
		for (i = 0; i < K; i++) {
			result[i] = (int) ranking[i][0];
		}
		return result;
	}

	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		double estimatedTime;
		TopPageRanker pageRanker = new TopPageRanker();
		pageRanker.loadData("./data/citeseer.dat");
		estimatedTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
		System.out.println("Loaded - " + estimatedTime);
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		estimatedTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;
		System.out.println("Ranked - " + estimatedTime);
		Integer[] rankedPages = pageRanker.getRankedPages(100);
		estimatedTime = (double) (System.currentTimeMillis() - startTime) / 1000.0;

		System.out.println("Top 100 Pages are:\n" + Arrays.toString(rankedPages));
		System.out.println("Proccessing time: " + estimatedTime + " seconds");
	}
}
