import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

public class LoadBalancing2 {
	
	private static int clients = 20;
	private static int machines = 10;
	private static double[][] weight = new double[machines][clients];
	private static HashMap<Integer, Integer> allocation = new HashMap<>();//key is clientNo, value is machineNo
	
	public static void main(String[] args) {
		
		LoadBalancing2 ld = new LoadBalancing2();
		
		ld.generateWeight();
		ld.randomAllocation();
		
		System.out.println("\n**********Initial Allocation**********");
		for (int i=0; i<machines; i++) {
			ld.printAllocation(i);
		}
		
		ld.balancing();
	}
	
	public void generateWeight() {
		
		LoadBalancing2 ld = new LoadBalancing2();
		double[] Qi = ld.generateQi();
		double[][] Rij = ld.generateRij();
		
		System.out.println("Qi/Rij: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				weight[i][j] = Qi[j]/Rij[i][j];
				System.out.print(weight[i][j] + "\t");
			}
			System.out.println();
		}
	}
	
	public double[] generateQi() {
		
		double[] Qi = new double[clients];
		Random r = new Random();
		
		for (int i=0; i<clients; i++) {
			Qi[i] = r.nextDouble(); //randomly generate Qi from 0 to 1
		}
		
		/*System.out.println("Qi: ");
		for (int i=0; i<clients; i++) {
			System.out.print(Qi[i] + "\t");
		}
		System.out.println();*/
		
		return Qi;
	}
	
	public double[][] generateRij() {
		
		double[][] Rij = new double[machines][clients];
		Random r = new Random();
		
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				Rij[i][j] = r.nextDouble(); //randomly generate Rij from 0 to 1
			}
		}

		/*System.out.println("Rij: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				System.out.print(Rij[i][j] + "\t");
			}
			System.out.println();
		}*/
		
		return Rij;
	}
	
	public void randomAllocation() {
		
		Random r = new Random();
		int machineNo;
		
		for (int i=0; i<clients; i++) {
			machineNo = r.nextInt(machines); //randomly allocate to machine 0 to 9
			allocation.put(i, machineNo);
		}
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
		
		LoadBalancing2 ld = new LoadBalancing2();
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
		
		LoadBalancing2 ld = new LoadBalancing2();
		int currentMachine = allocation.get(clientNo);
		double currentLoad = ld.getMachineLoad(currentMachine);
		double newLoad;
		int change = 0;
		
		for (int i=0; i<machines; i++) {
			if (i != currentMachine) {
				newLoad = ld.getMachineLoad(i) + weight[i][clientNo];
				if (newLoad < currentLoad) {
					allocation.put(clientNo, i);
					
					System.out.println("\n**********New Allocation**********");
					for (int j=0; j<machines; j++) {
						ld.printAllocation(j);
					}
					
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