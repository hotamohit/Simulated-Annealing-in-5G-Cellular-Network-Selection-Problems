import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

public class TheWholeProject2 extends Frame{

	private static final long serialVersionUID = 1L;
	private static int stations = 35;
	private static int clients = 100;
	private static double NA_SW=0x3f3f3f3f;//social welfare of nash equilibrium
	private static double SA_SW=0;//social welfare of Optimum by simulated annealing
	private static double[] sX = new double[stations]; //x-coordinate of stations
	private static double[] sY = new double[stations]; //y-coordinate of stations
	private static double[] cX = new double[clients]; //x-coordinate of clients
	private static double[] cY = new double[clients]; //y-coordinate of clients
	private static double[][] distance = new double[clients][stations];
	private static double[][] signalStrength = new double[clients][stations];
	private static double[][] signalStrength_T = new double[stations][clients];
	private static HashMap<Integer, Integer> connection = new HashMap<>();//key is clientNo, value is stationNo
	private static HashMap<Integer, Integer> allocation = new HashMap<>();// key is clientNo, value is basestationNo
	private static ArrayList<String> configuration = new ArrayList<>();
	public static void main(String[] args) {
		
		TheWholeProject2 sa = new TheWholeProject2();
		
		sa.setSize(1200, 1000);
		sa.setVisible(true);
		sa.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				super.windowClosing(e);
			}
		});
		
		sa.stationDeploy();
		sa.clientDistribute();
		signalStrength_T=tfunction(signalStrength);
		//sa.connect();
		sa.randomAllocation();

//		System.out.println("\n**********Initial Allocation**********");
		for (int i = 0; i < stations; i++) {
			sa.printAllocation(i);
		}

		sa.balancing();
		for (int i=0; i<clients; i++) {
			if(NA_SW>getcigema(i))
			NA_SW = getcigema(i);
		}
		System.out.println("¡¾Final Result for Nash Equilibrium¡¿");
		System.out.println("NA_SW:  "+NA_SW);
		System.out.println("¡¾Final Connection for Nash Equilibrium¡¿");
		for (int i : allocation.keySet()) {
			System.out.print(i + "-" + allocation.get(i) + "\t");
		}
		System.out.println();
	//upside is for nash equilibrium
    //downside is for simulated annealing
		sa.connect();
		sa.annealing();
		System.out.println("¡¾Final Result Of Simulated Annealing¡¿");
		System.out.println("SA_SW = " + SA_SW);
		System.out.println("¡¾Final Connection Of Simulated Annealing¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();
		System.out.println("POA: "+NA_SW/SA_SW);
	}
	
	public void paint(Graphics g) {
		
		int x, y;
		
		g.setColor(Color.RED);
		for (int i=0; i<stations; i++) {
			x = (int)sX[i] + 40;
			y = (int)sY[i] + 40;
			g.fillOval(x, y, 15, 15);
		}
		
		g.setColor(Color.BLACK);
		for (int i=0; i<clients; i++) {
			x = (int)cX[i] + 40;
			y = (int)cY[i] + 40;
			g.fillOval(x, y, 5, 5);
		}
    }
	
	public void stationDeploy() {
		
		double x = 0, y = 0;
		double d1 = 70, d2 = 2*Math.sqrt(3)*d1; //d1 is the distance between rows, d2 is the distance between columns
		int num = 0;
		int sRow = 7, sCol = 5; //sRow is the number of rows, sCol is the number of columns
		
		for (int i=0; i<sRow; i++) {
			
			x = 0;
			
			if (i % 2 == 0) { //odd rows
				for(int j=0; j<sCol; j++) {
					sX[num] = x;
					sY[num] = y;
					x += d2;
					num ++;
				}
			}
			else { //even rows
				for(int j=0; j<sCol; j++) {
					sX[num] = x + d2/2.0;
					sY[num] = y;
					x += d2;
					num ++;
				}
			}
			
			y += d1;
		}
		
		/*System.out.println("Station Deployment:");
		for (int i=0; i<stations; i++) {
			System.out.print("(" + sX[i] + "," + sY[i] + ") ");
		}
		System.out.println();*/
	}
	
	public void clientDistribute() {
		
		double x, y;
		
		for (int i=0; i<clients; i++) {
			
			x = Math.random()*1100; //[0,1100)
			y = Math.random()*410; //[0,400)
			cX[i] = x;
			cY[i] = y;
			
			for (int j=0; j<stations; j++) {
				distance[i][j] = Math.sqrt((cX[i]-sX[j])*(cX[i]-sX[j]) + (cY[i]-sY[j])*(cY[i]-sY[j])) / 100.0; //divide by 100 to make it small
				signalStrength[i][j] = calcSignalStrength(distance[i][j]);
			}
		}
		
		/*System.out.println("Client Distribution:");
		for (int i=0; i<clients; i++) {
			System.out.print("(" + cX[i] + "," + cY[i] + ") ");
		}
		System.out.println();
		
		System.out.println("Distance:");
		for (int i=0; i<clients; i++) {
			for (int j=0; j<stations; j++) {
				System.out.print(distance[i][j] + " ");
			}
			System.out.println();
		}
		
		System.out.println("Signal Strength:");
		for (int i=0; i<clients; i++) {
			for (int j=0; j<stations; j++) {
				System.out.print(signalStrength[i][j] + " ");
			}
			System.out.println();
		}*/
	}
	
	public double calcSignalStrength(double d){
		
		double d0 = 24.0; //reference distance
		double a = 4.6, b = 0.0075, c = 12.6; //constants to model the terrain type
		double hTx = 7.0, hRx = 1.5; //transmitter and receiver antenna heights in meters
		double f = 28000.0; //carrier frequency in MHz
		double y = 299792458.0/(f*1000000.0); //carrier wavelength in meters
		double power = 1.0;
		
		double PLd0 = 20.0 * Math.log10((4.0*Math.PI*d0)/y); //free space path loss in dB
		double n = a - b*hTx + c/hTx;
		double Xfc = 6.0 * Math.log10(f/2000.0); //correction factor for frequency
		double XRx = -10.8 * Math.log10(hRx/2.0); //correction factor for receiver heights
		
		double PLSUId = PLd0 + 10.0*n*Math.log10(d/d0) + Xfc + XRx;
		double signalStrength = power * Math.pow(10.0, -PLSUId/10.0);
		
		/*System.out.println("d: " + d);
		System.out.println("PLd0: " + PLd0);
		System.out.println("n: " + n);
		System.out.println("Xfc: " + Xfc);
		System.out.println("XRx: " + XRx);
		System.out.println("PLSUId: " + PLSUId);
		System.out.println("signal strength: " + signalStrength);
		System.out.println();*/
		
		return signalStrength;
	}
	
	public void connect() { //connect clients to stations where clients can get maximum signal strength
		
		double maxSignalStrength;
		int stationNo = 0;
		
		for (int i=0; i<clients; i++) {
			
			maxSignalStrength = signalStrength[i][0];
			
			for (int j=0; j<stations; j++) {
				if (maxSignalStrength < signalStrength[i][j]) {
					maxSignalStrength = signalStrength[i][j];
					stationNo = j;
				}
			}
			
			connection.put(i, stationNo);
		}
		
		/*System.out.println("¡¾Connection¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();*/
	}
	public void randomAllocation() {

		Random r = new Random();
		int machineNo;
		String str = "";

		for (int i = 0; i < clients; i++) {
			machineNo = r.nextInt(stations); // randomly allocate to machine 0 to 9
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
				weightList.add(signalStrength_T[machineNo][clientNo]);
			}
		}

//		System.out.println("Station" + machineNo + ": ");
//		System.out.println(clientList);
//		System.out.println(weightList);
	}
	public static double[][] tfunction(double[][] test){
	int m=test.length;
	int n=test[0].length;
	double t[][]=new double[n][m];
	for(int i=0;i<n;i++){
		for(int j=0;j<m;j++){
			t[i][j]=test[j][i];
		}
	}
	return  t;
}
	public void balancing() {

		TheWholeProject2 ld = new TheWholeProject2();
		int sum;

		while (true) {
			sum = 0;

			for (int i = 0; i < clients; i++)
				sum += ld.willChange(i);

			if (sum == 0)
				break;
		}
	}

	public int willChange(int clientNo) {

		TheWholeProject2 ld = new TheWholeProject2();
		int currentMachine = allocation.get(clientNo);
		double currentLoad = ld.getMachineLoad(currentMachine);
		double currentThroughtput = signalStrength_T[currentMachine][clientNo] / currentLoad;
		double newLoad, newThroughtput;
		int change = 0;
		String str = "";

		for (int i = 0; i < stations; i++) {
			if (i != currentMachine) {
				newLoad = ld.getMachineLoad(i) + signalStrength_T[i][clientNo];
				newThroughtput = signalStrength_T[i][clientNo] / newLoad;

				if (((newThroughtput - currentThroughtput) / currentThroughtput) > 0.1) {
					allocation.put(clientNo, i);

//					System.out.println("\n**********New Allocation**********");
					for (int j = 0; j < stations; j++) {
						ld.printAllocation(j);
					}

					for (int j : allocation.keySet()) {
						str = str + allocation.get(j) + ", ";
					}

					for (String str1 : configuration) {
						if (str.equals(str1)) {
							System.out.println("\nTHERE IS NO PURE NASH EQUILIBRIUM!");
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
				load += signalStrength_T[machineNo][clientNo];
			}
		}

		return load;
	}

	public static double getcigema(int clientNo) {//throuput for each client of nash equilibrium
		
		double sum = 0;
		int stationNo = allocation.get(clientNo);
		
		for (int i : allocation.keySet()) {
			if (allocation.get(i).equals(stationNo)) {
				sum += signalStrength[i][stationNo];
			}
		}
		
		return Math.log10(1+(signalStrength[clientNo][stationNo] / sum));
	}
	public void annealing() {
		
		HashMap<Integer, Integer> original = new HashMap<>();
		double minThroughput1=0, minThroughput2=0;
		double minSINR, thisSINR;
		double T = 3000, k0 = Math.pow(10, -10); //T is temperature, k0 is Boltzmann's constant
		double probability;
		
		while (T > 0.01) {
			minThroughput1=0; minThroughput2=0;
			for (int i : connection.keySet()) { //save original configuration
				original.put(i, connection.get(i));
			}
			
			minSINR = getSINR(0);		
			for (int i=0; i<clients; i++) {
				thisSINR = getSINR(i);			
				if (minSINR > thisSINR) {
					minSINR = thisSINR;
				}
			}
			minThroughput1 = Math.log10(1 + minSINR);
			
			change();
			
			minSINR = getSINR(0);
			for (int i=0; i<clients; i++) {
				thisSINR = getSINR(i);
				if (minSINR > thisSINR) {
					minSINR = thisSINR;
				}
			}	
			minThroughput2 = Math.log10(1 + minSINR);
			
//			System.out.println("Minimum throughput is " + minThroughput1 + " before change, " + minThroughput2 + " after change.");
			
			if (minThroughput2 > minThroughput1) { //if the change increases minimum throughput, accept
//				System.out.println("Accepted.");
//				System.out.println("¡¾New connection¡¿");
//				for (int i : connection.keySet()) {
//					System.out.print(i + "-" + connection.get(i) + "\t");
//				}
//				System.out.println();
				
			}
			else { //if the change doesn't increase minimum throughput, accept probabilistically and decrease temperature
				probability = Math.pow(Math.E, (minThroughput2-minThroughput1)/(k0*T));
//				System.out.println("probability = " + probability);
				
				if (probability < 0.00001) { //if the probability becomes too small, end the algorithm
					break;
				}
				
				if (Math.random() < probability) {
//					System.out.println("Accepted.");
//					System.out.println("¡¾New connection¡¿");
//					for (int i : connection.keySet()) {
//						System.out.print(i + "-" + connection.get(i) + "\t");
//					}
//					System.out.println();
					
				}
				else { //recover original configuration
					for (int i : original.keySet()) {
						connection.put(i, original.get(i));
					}
					
					
//					System.out.println("Not Accepted.");
//					System.out.println("¡¾Original connection¡¿");
//					for (int i : connection.keySet()) {
//						System.out.print(i + "-" + connection.get(i) + "\t");
//					}
//					System.out.println();
//					System.out.println("¡¾result of SA¡¿:  "+minThroughput2);
				}
				
				T*=0.999;
			}
		}
		
		SA_SW=minThroughput1;
//		System.out.println("¡¾Final result of SA¡¿:  "+minThroughput2);
//		System.out.println("The End");
	}
	public void change() { //select (clients/10) number of clients with minimum SINR, and randomly connect to a new station
		
		Random r = new Random();
		int clientNo, stationNo, newStationNo;
		
		/*System.out.println("¡¾What's changed¡¿");*/
		for (int i=0; i<clients/10; i++) {
			
			clientNo = findMinSINR();
			stationNo = connection.get(clientNo);
			newStationNo = stationNo;
			
			while (newStationNo == stationNo) {
				newStationNo = r.nextInt(stations);
			}
			
			connection.put(clientNo, newStationNo);
			/*System.out.print(clientNo + "-" + newStationNo + "\t");*/
		}
		/*System.out.println();*/
	}
	
	public int findMinSINR() {
		
		double minSINR = getSINR(0);
		double thisSINR;
		int clientNo = 0;
		
		for (int i=0; i<clients; i++) {
			
			thisSINR = getSINR(i);
			
			if (minSINR > thisSINR) {
				minSINR = thisSINR;
				clientNo = i;
			}
		}
		
		return clientNo;
	}

	public double getSINR(int clientNo) {
		
		double sum = 0;
		int stationNo = connection.get(clientNo);
		
		for (int i : connection.keySet()) {
			if (connection.get(i).equals(stationNo)) {
				sum += signalStrength[i][stationNo];
			}
		}
		
		return signalStrength[clientNo][stationNo] / sum;
	}
}