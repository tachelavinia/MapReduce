/**
 * Clasa ce reprezinta o solutie partiala pentru problema de rezolvat. Aceste
 * solutii partiale constituie task-uri care sunt introduse in workpool.
 */
interface PartialSolution{
	public void process();
}

/**
 * Clasa ce reprezinta un thread worker.
 */
public class ReplicatedWorkers extends Thread {
	WorkPool wp;

	public ReplicatedWorkers(WorkPool workpool) {
		this.wp = workpool;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	void processPartialSolution(PartialSolution ps) {
		ps.process();
	}
	
	public void run() {
		while (true) {
			PartialSolution ps = wp.getWork();
			if (ps == null)
				break;
			
			processPartialSolution(ps);
		}
	}
}
