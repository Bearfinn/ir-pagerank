import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

class Node {
	public int id;
	public List<Node> fromNodes;
	public List<Node> toNodes;
	public double PR;
	public double newPR;
	
	public Node(int id) {
		this.id = id;
		this.fromNodes = new ArrayList<Node>();
		this.toNodes = new ArrayList<Node>();
	}
	
	public void addToNode(Node node) {
		if (!toNodes.contains(node)) toNodes.add(node);
	}
	
	public void addFromNode(Node node) {
		if (!fromNodes.contains(node)) fromNodes.add(node);
	}
	
	public List<Node> getFromNode() {
		return fromNodes;
	}
	
	public List<Node> getToNode() {
		return toNodes;
	}

	public int getId() {
		return id;
	}
}

public class PageRanker {
	private HashMap<Integer, Node> allNode;
	int latestPerplexity; // The perplexity of current iteration
	int streak; // The amount of time perplexity has the same unit values
	double N; // Number of nodes(pages)
	double d; // Damp or Teleportation Constant

	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into
	 * memory. Each line in the input file should have the following format: <pid_1>
	 * <pid_2> <pid_3> .. <pid_n>
	 *
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to
	 * page pid_1. You can assume that a page ID is an integer.
	 */

	public void loadData(String inputLinkFilename) {
		allNode = new HashMap<Integer, Node>();
		File input = new File(inputLinkFilename);
		try (BufferedReader br = new BufferedReader(new FileReader(input))) {
			String currentLine;

			while ((currentLine = br.readLine()) != null) { // see each line
				String[] numString = currentLine.trim().split("\\s+"); // split white space
				int destNodeId = Integer.parseInt(numString[0]);
				Node destNode = new Node(destNodeId); // create new node
				if (!allNode.containsKey(destNodeId)) { // if don't have this node
					allNode.put(destNodeId, destNode); // add to set P
					for (int i = 1; i < numString.length; i++) { // loop for other node in that line
						int srcNodeId = Integer.parseInt(numString[i]);
						Node srcNode = new Node(srcNodeId); // create new node
						if (!allNode.containsKey(srcNodeId)) {
							allNode.put(srcNodeId, srcNode);
							srcNode.addToNode(destNode); // add toNode to them
						} else {
							srcNode = allNode.get(srcNodeId);
							srcNode.addToNode(destNode);
						}
						destNode.addFromNode(srcNode); // add from Node to the current node of the line
					}
				} else {
					for (int i = 1; i < numString.length; i++) { // loop for other node in that line
						int srcNodeId = Integer.parseInt(numString[i]);
						Node srcNode = new Node(srcNodeId); // create new node
						if (!allNode.containsKey(srcNodeId)) {
							allNode.put(srcNodeId, srcNode);
							srcNode.addToNode(destNode); // add toNode to them
						} else {
							srcNode = allNode.get(srcNodeId);
							srcNode.addToNode(destNode);
						}
						allNode.get(destNodeId).addFromNode(srcNode); // add from Node to the current node of the line
					}
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String mapToString(Map<Integer, Node> map) {
		StringBuilder sb = new StringBuilder();
		for (Entry<Integer, Node> e : map.entrySet()) {
			sb.append(e.getKey() + " | ");
			for (Node node : e.getValue().getFromNode()) {
				sb.append(node.getId() + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * This method will be called after the graph is loaded into the memory. This
	 * method initialize the parameters for the PageRank algorithm including setting
	 * an initial weight to each page.
	 */
	public void initialize() {
		N = allNode.keySet().size();
		latestPerplexity = -1;
		streak = 0;
		d = 0.85;
		for (Node node : allNode.values()) {
			node.PR = 1.0 / N;
		}
	}

	/**
	 * Computes the perplexity of the current state of the graph. The definition of
	 * perplexity is given in the project specs.
	 */

	public double getPerplexity() {
		double entropy = 0.0;
		for (Node node : allNode.values()) {
			entropy += node.PR * (Math.log10(node.PR) / Math.log10(2.0));
		}
		double perplexity = Math.pow(2, -entropy);
		//System.out.println(perplexity);
		return perplexity;
	}

	/**
	 * Returns true if the perplexity converges (hence, terminate the PageRank
	 * algorithm). Returns false otherwise (and PageRank algorithm continue to
	 * update the page scores).
	 */

	public boolean isConverge() {
		double dblPerplexity = getPerplexity();
		int currentPerplexity = (int) Math.floor(dblPerplexity);
		if (currentPerplexity == latestPerplexity) {
			streak++;
			if (streak >= 3) {
				return true;
			}
		} else {
			streak = 0;
			latestPerplexity = currentPerplexity;
		}
		return false;

	}

	/**
	 * The main method of PageRank algorithm. Can assume that initialize() has been
	 * called before this method is invoked. While the algorithm is being run, this
	 * method should keep track of the perplexity after each iteration.
	 *
	 * Once the algorithm terminates, the method generates two output files. [1]
	 * "perplexityOutFilename" lists the perplexity after each iteration on each
	 * line. The output should look something like:
	 * 
	 * 183811 79669.9 86267.7 72260.4 75132.4
	 * 
	 * Where, for example,the 183811 is the perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm
	 * terminate. The output should look something like:
	 * 
	 * 1 0.1235 2 0.3542 3 0.236
	 * 
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 *
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename) {

		StringBuilder perplexString = new StringBuilder();
		StringBuilder prString = new StringBuilder();

		for (Node node : allNode.values()) {
			node.PR = 1.0 / N;
		}

		while (!isConverge()) {
			double sinkPR = 0.0;
			for (Node node : allNode.values()) {
				if (node.getToNode().isEmpty()) {
					sinkPR += node.PR;
				}
			}
			for (Node node : allNode.values()) {
				node.newPR = (1 - d) / N;
				node.newPR += d * sinkPR / N;
				for (Node srcNode : node.getFromNode()) {
					node.newPR += d * srcNode.PR / srcNode.getToNode().size();
				}
			}
			for (Node node : allNode.values()) {
				node.PR = node.newPR;
			}
			perplexString.append(getPerplexity() + "\n");
		}
		for (Node node : allNode.values()) {
			prString.append(node.getId() + " " + node.PR + "\n");
		}

		File perplexFile = new File(perplexityOutFilename);
		File prFile = new File(prOutFilename);

		FileWriter perplexWriter;
		FileWriter prWriter;
		try {
			perplexWriter = new FileWriter(perplexFile, false);
			perplexWriter.write(perplexString.toString());
			perplexWriter.close();

			prWriter = new FileWriter(prFile, false);
			prWriter.write(prString.toString());
			prWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K) {
		if (N < K)
			return getRankedPages((int) N);
		List<Map.Entry<Integer, Node>> list = new ArrayList<Entry<Integer, Node>>(allNode.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Node>>() {
			@Override
			public int compare(Map.Entry<Integer, Node> o1, Map.Entry<Integer, Node> o2) {
				return Double.compare(o2.getValue().PR, o1.getValue().PR);
			}
		});

		ArrayList<Integer> resultArray = new ArrayList<Integer>();
		int i = 0;
		for (Map.Entry<Integer, Node> entry : list) {
			resultArray.add(entry.getKey());
			if (i == K)
				break;
		}
		Integer[] result = new Integer[K];
		for (int j = 0; j < K; j++) {
			result[j] = resultArray.remove(0);
		}
		//Integer[] result = resultArray.toArray(new Integer[K]);
		return result;
	}

	public static void main(String args[]) {
		long startTime = System.currentTimeMillis();
		PageRanker pageRanker =  new PageRanker();
		pageRanker.loadData("citeseer.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(100);
		double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
			
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}
