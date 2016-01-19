import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;

public class Main {
	static HashMap<String, Vector<HashMap<Integer, Integer>>> map;
	static HashMap<String, Integer> maximForDocs;
	static HashMap<String, ArrayList<String>> maximalWords = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, Float> rank = new HashMap<String, Float>();
	
	public static void AddRank(String name, Float value){
		rank.put(name, value);
	}
	
	public static void updateMaxim(String file, Integer maxValue){
		maximForDocs.put(file, maxValue);
	}
	
	public static int getMaxForThisDoc(String file){
		return maximForDocs.get(file);
	}
	
	public static void updateMaximalsForThisFile(String file, String word){
		maximalWords.get(file).add(word);
	}
	
	public static void clearLatestVersionForThisFile(String file){
		maximalWords.get(file).clear();
	}
	
	public static boolean contains(String name, String word){
		return maximalWords.get(name).contains(word);
	}
	
	public static void addToMap(String name, HashMap<Integer, Integer> hashForThisFile){
		map.get(name).add(hashForThisFile);
	}

	public static void addToArray(String fileName, ArrayList<String> words){
		maximalWords.put(fileName, words);
	}

	public static List<Entry<String, Float>> sortByValue(HashMap<String, Float> map){
		List<Entry<String, Float>> list = new LinkedList<Entry<String, Float>>(map.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Float>>() {
			@Override
			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
				return -o1.getValue().compareTo(o2.getValue());
			}
		});
		return list;
	}
	
	public static void main(String args[]) throws IOException, InterruptedException {
		
		int numThreads = 0;
		String fileIn = "", fileOut = "";
		String line;
		int dimFragment, numDocuments;
		String files[];
		
		long startTime = System.currentTimeMillis();
		
		if(args.length != 3){
			System.out.println("Programul trebuie rulat cu NT fisin fisout");
			System.exit(1);
		}else{
			try {
				numThreads = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.out.println("Primul argument trebuie sa fie intreg");
				System.exit(1);
			}
			fileIn = args[1];
			fileOut = args[2];
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), "UTF8"));
		
		/* dimensiunea fragmentelor */
		line = reader.readLine();
		dimFragment = Integer.parseInt(line);
		
		/* numarul de fisiere */
		line = reader.readLine();
		numDocuments = Integer.parseInt(line);
		
		/* numele fisierelor*/
		files = new String[numDocuments];
		for(int i = 0; i < numDocuments; i++){
			files[i] = reader.readLine();
		}
		
		reader.close();
		
		WorkPool workPool = new WorkPool(numThreads);
		
		map = new HashMap<String, Vector<HashMap<Integer, Integer>>>();
		maximForDocs = new HashMap<String, Integer>();
		for(int i = 0; i < numDocuments; i++){
			File forSize = new File(files[i]);
			map.put(files[i], new Vector<HashMap<Integer, Integer>>());
			maximalWords.put(files[i], new ArrayList<String>());
			maximForDocs.put(files[i], 0);
			long length = forSize.length();
			for(int j = 0; j < length; j += dimFragment){
				Map task = new Map(files[i], j, dimFragment, length);
				workPool.putWork(task);
			}
		}
		
		/* workeri*/
		ReplicatedWorkers[] workersMap = new ReplicatedWorkers[numThreads];
		for(int i = 0; i < numThreads; i++){
			workersMap[i] = new ReplicatedWorkers(workPool);
		}
		
		/* start pentru workeri*/
		for(int i = 0; i < numThreads; i++){
			workersMap[i].start();
		}
		
		for(int i = 0; i < numThreads; i++){
			workersMap[i].join();
		}
		
		for(int i = 0; i < numDocuments; i++){
			Reduce task = new Reduce(files[i], map.get(files[i]), maximalWords.get(files[i]));
			workPool.putWork(task);
		}
		
		ReplicatedWorkers[] workersReduce = new ReplicatedWorkers[numThreads];
		for(int i = 0; i < numThreads; i++){
			workersReduce[i] = new ReplicatedWorkers(workPool);
		}
		
		/* start pentru workeri*/
		for(int i = 0; i < numThreads; i++){
			workersReduce[i].start();
		}
		
		for(int i = 0; i < numThreads; i++){
			workersReduce[i].join();
		}

		List<Entry<String, Float>> list = new LinkedList<Entry<String, Float>>();
		list = sortByValue(rank);
		int maxDim = 0, noWords = 0;
		NumberFormat formetter = new DecimalFormat("#0.00");
		BufferedWriter output = new BufferedWriter(new FileWriter(fileOut));
		for(Entry<String, Float> entry : list){
			String fileName = entry.getKey();
			Float rank = entry.getValue();
			if(maximalWords.containsKey(fileName)){
				ArrayList<String> max = maximalWords.get(fileName);
				noWords = max.size();
				maxDim = max.get(0).length();
			}
			output.write(fileName);
			output.write(";");
			output.write(formetter.format(rank));
			output.write(";[");
			output.write(String.valueOf(maxDim));
			output.write(String.valueOf(noWords));
			output.write("]");
			output.newLine();
		}
		
		long endTime = System.currentTimeMillis();
		long realTimeRunning = endTime - startTime;
		output.close();
		System.out.println(realTimeRunning/1000);
	}
	
}
