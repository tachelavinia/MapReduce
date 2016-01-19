import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

public class Reduce implements PartialSolution{
	
	/* Task-urile de tip REDUCE implementeaza interfata PatialSolution  pentru a putea folosi acelasi Workpool*/
	private String name;
	private Vector<HashMap<Integer, Integer>> hashForThisFile = new Vector<HashMap<Integer, Integer>>();
	private HashMap<Integer, Integer> reducedHash;
	
	public Reduce(String name, Vector<HashMap<Integer, Integer>> hashForFile, ArrayList<String> maximalWords) {
		this.name = name;
		this.hashForThisFile = hashForFile;
		reducedHash = new HashMap<Integer, Integer>();
	}

	@Override
	public void process() {
		/*      Consta in combinarea listei de rezultate partiale din etapa de MAP : se vor combina hash-urile astfel incat la final sa avem un singur
		 * hash ce reprezinta lungimea cuvintelor impreuna cu numarul lor de aparitii pentru intreg documentul. */
		doSomeMagicAndCombine();
		/*       Consta in calcularea rangului unui document folosind hash-ului din etapa de combinare si sirul lui Fibonacci */
		doSomeMagicAndProcess();
	}
	
	/* Genereaza corespunzatorul lui -number- din sirul lui Fibonacci */
	public int fibo(int number){
		if(number < 0)
			return -1;
		else if(number == 0)
			return 0;
		else if(number == 1)
			return 1;
		else
			return fibo(number - 1) + fibo(number - 2);
	}
	
	/* In prima operatie de Reduce se aduna numarul de aparitii pentru fiecare lungime de cuvant, iterand prin lista de hash-uri  */
	private void doSomeMagicAndCombine() {
		int length = 0;
		for(HashMap<Integer, Integer> forFile : hashForThisFile){
			for(Entry<Integer, Integer> entry : forFile.entrySet()){
				length = entry.getKey();
				int occurrence = entry.getValue();
				if(reducedHash.containsKey(length)){
					int totalOccurrence  = occurrence + reducedHash.get(length);
					reducedHash.put(length, totalOccurrence);
				}else{
					reducedHash.put(length, occurrence);
				}
			}
		}
	}
		/* Calculeaza rang-ul documentului si returnez lista cu cele mai lungi cuvinte */
	private void doSomeMagicAndProcess() {
		float rank = 0;
		int totalWords = 0;
		for(Entry<Integer,Integer> entry : reducedHash.entrySet()){
			totalWords += entry.getValue();
		}
		for(Entry<Integer,Integer> entry : reducedHash.entrySet()){
			int key = entry.getKey();
			int value = entry.getValue();
			rank += fibo(key + 1) * value;
		}
		Main.AddRank(name, rank/totalWords);
	}
}
