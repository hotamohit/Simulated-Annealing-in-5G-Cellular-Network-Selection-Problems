import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

public class LoadBalancing3 {
	
	private static int clients = 20;
	private static int machines = 10;
	private static double[][] Qij = new double[machines][clients];
	private static double[][] Rij = new double[machines][clients];
	private static double[][] weight = new double[machines][clients];
	private static HashMap<Integer, Integer> allocation = new HashMap<>();//key is clientNo, value is machineNo
	private static ArrayList<String> configuration = new ArrayList<>();
	
	public static void main(String[] args) {
		
		LoadBalancing3 ld = new LoadBalancing3();
		
		Qij = ld.generateXij();
		
		System.out.println("Qij: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				System.out.print(Qij[i][j] + "\t");
			}
			System.out.println();
		}
		
		Rij = ld.generateXij();
		
		System.out.println("Rij: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				System.out.print(Rij[i][j] + "\t");
			}
			System.out.println();
		}
		
		ld.generateWeight();
		ld.randomAllocation();
		
		System.out.println("\n**********Initial Allocation**********");
		for (int i=0; i<machines; i++) {
			ld.printAllocation(i);
		}
		
		ld.balancing();
	}
	
	public void generateWeight() {
		
		System.out.println("Qij/Rij: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				weight[i][j] = Qij[i][j]/Rij[i][j];
				System.out.print(weight[i][j] + "\t");
			}
			System.out.println();
		}
	}

	public double[][] generateXij() {
		
		double[][] Xij = new double[machines][clients];
		Random r = new Random();
		
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				Xij[i][j] = r.nextDouble(); //randomly generate Xij from 0 to 1
			}
		}
		
		return Xij;
	}
	
	public void randomAllocation() {
		
		Random r = new Random();
		int machineNo;
		String str = "";
		
		for (int i=0; i<clients; i++) {
			machineNo = r.nextInt(machines); //randomly allocate to machine 0 to 9
			allocation.put(i, machineNo);
			str = str + machineNo + ", ";
		}
		
		configuration.add(str);
	}
	
	public void printAllocation(int machineNo) {
		
		ArrayList<Integer> clientList = new ArrayList<>();
		ArrayList<Double> weightList = new ArrayList<>();
		
		for (int clientNo : allocation.keySet()) {
			if (allocation.get(clientNo).equals(machineNo)) {
				clientList.add(clientNo);
				weightList.add(weight[machineNo][clientNo]);
			}
		}
		
		System.out.println("Machine" + machineNo + ": ");
		System.out.println(clientList);
		System.out.println(weightList);
	}
	
	public void balancing() {
		
		LoadBalancing3 ld = new LoadBalancing3();
		int sum;
		
		while(true) {
			sum = 0;
			
			for (int i=0; i<clients; i++)
				sum += ld.willChange(i);
			
			if (sum == 0)
				break;
		}
	}
	
	public int willChange(int clientNo) {
		
		LoadBalancing3 ld = new LoadBalancing3();
		int currentMachine = allocation.get(clientNo);
		double currentLoad = ld.getMachineLoad(currentMachine);
		double currentThroughput = Qij[currentMachine][clientNo]/currentLoad; 
		double newLoad, newThroughput;
		int change = 0;
		String str = "";
		
		for (int i=0; i<machines; i++) {
			if (i != currentMachine) {
				newLoad = ld.getMachineLoad(i) + weight[i][clientNo];
				newThroughput = Qij[i][clientNo]/newLoad;
				
				if (newThroughput > currentThroughput) {
					allocation.put(clientNo, i);
					
					System.out.println("\n**********New Allocation**********");
					for (int j=0; j<machines; j++) {
						ld.printAllocation(j);
					}
					
					for (int j : allocation.keySet()) {
						str = str + allocation.get(j) + ", ";
					}
					
					for (String str1 : configuration) {
						if (str.equals(str1)) {
							System.out.println("\n¡¾THERE IS NO PURE NASH EQUILIBRIUM!¡¿");
							System.exit(0);
						}
					}
					
					configuration.add(str);
					change = 1;				
					break;
				}
			}
		}
		
		return change;
	}
	
	public double getMachineLoad(int machineNo) {
		
		double load = 0;
		
		for (int clientNo : allocation.keySet()) {
			if (allocation.get(clientNo).equals(machineNo)) {
				load += weight[machineNo][clientNo];
			}
		}
		
		return load;
	}
}