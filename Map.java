import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class Map implements PartialSolution{
	private String name;
	private int begin;
	private int end;	
	private HashMap<Integer, Integer> hashForThisFile;
	private Character []sequence;
	
	public Map(String name, int offSet, int dimFragment, long totalDimension) {
		this.name = name;
		this.end = (int) ((totalDimension <= offSet + dimFragment) ? totalDimension - 8 : (offSet + dimFragment));
		this.begin = offSet;
		
		hashForThisFile = new HashMap<Integer, Integer>();
	}
	
	boolean isNotDelim(Character character){
		return (character != null) && (Character.isLetter(character) || Character.isDigit(character));
	}
	
	public String getName(){
		return name;
	}
	
	public HashMap<Integer, Integer> getLocalHash(){
		return hashForThisFile;
	}
	
	@Override
	public void process() {
		String word = "";
			
		File file = new File(name);
		try {
			RandomAccessFile randomAccess = new RandomAccessFile(file, "rw");
			/*     Corecteaza pozitia de start in cazul in care indexul este in interiorul unui cuvant.
			 * Pentru acest caz cuvantul va fi ignorat*/
			if(begin != 0){
				randomAccess.seek(begin);
				Character character = (char)randomAccess.readByte();
				int newIndex = 1;
				while(isNotDelim(character)){
					newIndex++;
					character = (char)randomAccess.readByte();
				}
				begin += newIndex;
			}
			
			/*     Corecteaza indexul de sfarsit in cazul este pozitionat in mijlocul unui cuvant.
			 * Pentru acest caz cuvantul va fi complet procesat.*/
			randomAccess.seek(end);
			Character character = (char)randomAccess.readByte();
			int newIndex = 0; 
			while(isNotDelim(character)){
				newIndex++;
				character = (char)randomAccess.readByte();
			}
			end += newIndex;
			int size = end - begin;
			sequence = new Character[Math.abs(size + 1)];
				
			randomAccess.seek(begin);
			int contor = 0;
			while(size > 0){
				character = (char)randomAccess.readByte();
				sequence[contor] = character;
				size--;
				contor++;
			}
			randomAccess.close();
				
			/* Salveaza lista de cuvinte maximale */
			for(int i = 0; i < sequence.length; i++){
				Character current = sequence[i];
				if(isNotDelim(current)){
					word += sequence[i];
				}else if(hashForThisFile.containsKey(word.length())){
					if(word.length() > 0){
						hashForThisFile.put(word.length(), hashForThisFile.get(word.length()) + 1);
					}
					if(word.length() >= Main.getMaxForThisDoc(name) && word.length() > 0){
						if(!Main.contains(name, word)){
							Main.updateMaximalsForThisFile(name, word);
						}
					}					
					word = "";
					}else{
						if(word.length() > Main.getMaxForThisDoc(name) && word.length() > 0){
							Main.updateMaxim(name, word.length());
							Main.clearLatestVersionForThisFile(name);
							Main.updateMaximalsForThisFile(name, word);
						}else if(word.length() == Main.getMaxForThisDoc(name)){
							if(!Main.contains(name, word)){
								Main.updateMaximalsForThisFile(name, word);
							}
						}
						if(word.length() > 0){
							hashForThisFile.put(word.length(), 1);
						}
						word = "";
					}
				}
				
		/* Stocheaza datele in hash-ul comun (din main) pentru a putea fi in continuare procesate de ReduceTask*/
		Main.addToMap(name, hashForThisFile);
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("End of File Exception!");
			e.printStackTrace();
		}
	}
}
