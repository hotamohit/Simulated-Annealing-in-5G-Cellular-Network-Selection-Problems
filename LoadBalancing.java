import java.util.Random;

public class LoadBalancing {
	
	public static void main(String[] args) {
		
		int clients = 20;
		int machines = 10;
		
		int[] weight = new int[clients]; //one-dimensional array of length 20
		int[][] allocation = new int[machines][clients]; //two-dimensional array with 10 rows and 20 columns
		
		LoadBalancing ld = new LoadBalancing();
		
		System.out.println("Weight: ");
		for (int i=0; i<clients; i++) {
			weight[i] = ld.generateWeight();
			System.out.print(weight[i] + " ");
		}
		
		for (int i=0; i<clients; i++) {
			allocation[ld.randomAllocation(machines)][i] = weight[i];
		}
		
		System.out.println("\nAllocation: ");
		for (int i=0; i<machines; i++) {
			for (int j=0; j<clients; j++) {
				System.out.print(allocation[i][j] + "\t");
			}
			System.out.println();
		}
		
		ld.balancing(allocation, machines, clients);
	}
	
	public int generateWeight() {
		Random r = new Random();
		int weight = (1 + r.nextInt(100)); //randomly generate weight from 1 to 100
		return weight;
	}
	
	public int randomAllocation(int machines) {
		Random r = new Random();
		int machineNo = r.nextInt(machines); //randomly allocate to machine 0 to 9
		return machineNo;
	}
	
	public void balancing(int allocation[][], int machines, int clients) {
		
		int load, maxLoad=0, minLoad=2000, maxMachineNo=0, minMachineNo=0;
		
		for (int i=0; i<machines; i++) {
			
			load = 0;

			for (int j=0; j<clients; j++) {
				load += allocation[i][j];
			}
			if (load > maxLoad) {
				maxLoad = load;
				maxMachineNo = i;
			}
			if (load < minLoad) {
				minLoad = load;
				minMachineNo = i;
			}
		}
		
		System.out.println("Maximum load: " + maxLoad);
		System.out.println("Minimum load: " + minLoad);
		System.out.println("Machine with the maximum load: " + maxMachineNo);
		System.out.println("Machine with the minimum load: " + minMachineNo);
		
		int minWeight=100, minClientNo=0;
		
		for (int i=0; i<clients; i++) {
			if ((allocation[maxMachineNo][i] > 0) && (allocation[maxMachineNo][i] < minWeight)) {
				minWeight = allocation[maxMachineNo][i];
				minClientNo = i;
			}
		}
		
		System.out.println("Minimum weight: " + minWeight);
		System.out.println("Client(Job) with the minimum weight: " + minClientNo);
		
		if (minWeight + minLoad < maxLoad) {
			
			allocation[maxMachineNo][minClientNo] = 0;
			allocation[minMachineNo][minClientNo] = minWeight;
			
			System.out.println("\nAllocation: ");
			for (int i=0; i<machines; i++) {
				for (int j=0; j<clients; j++) {
					System.out.print(allocation[i][j] + "\t");
				}
				System.out.println();
			}
			
			LoadBalancing ld = new LoadBalancing();
			ld.balancing(allocation, machines, clients);
		}
	}
	
	/*public boolean equals (int arr1[][], int arr2[][]) {
		
		boolean equals = true;
		
		for (int i=0; i<arr1.length; i++) //arr1.length returns the number of rows of arr1[][]
			for (int j=0; j<arr1[0].length; j++) //arr1[0].length returns the number of columns of arr1[0][]
				if (arr1[i][j] != arr2[i][j])
					equals = false;
		
		return equals;
	}*/
}