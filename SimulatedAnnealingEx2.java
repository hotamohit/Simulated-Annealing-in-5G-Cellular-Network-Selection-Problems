import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SimulatedAnnealingEx2 extends Frame{
	
	private static int stations = 4;
	private static int clients = 8;
	private static double[] sX = new double[stations];
	private static double[] sY = new double[stations];
	private static double[] cX = new double[clients];
	private static double[] cY = new double[clients];
	private static double[][] distance = new double[clients][stations];
	private static double[][] signalStrength = new double[clients][stations];
	private static HashMap<Integer, Integer> connection = new HashMap<>();//key is clientNo, value is stationNo
	private static double resultOfSA, resultOfEN;
	
	public static void main(String[] args) {
		
		SimulatedAnnealingEx2 sa = new SimulatedAnnealingEx2();
		
		sa.setSize(500, 150);
		sa.setVisible(true);
		sa.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				super.windowClosing(e);
			}
		});
		
		sa.stationDeploy();
		sa.clientDistribute();
		sa.connect();
		sa.annealing();
		sa.enumerate();
		sa.repaint();
		
		System.out.println("The accuracy of simulated annealing is " + resultOfSA/resultOfEN);
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
		double d1 = 70, d2 = 2*Math.sqrt(3)*d1;
		int num = 0;
		int sRow = 2, sCol = 2;
		
		for (int i=0; i<sRow; i++) {
			
			x = 0;
			
			if (i % 2 == 0) { // odd rows
				for(int j=0; j<sCol; j++) {
					sX[num] = x;
					sY[num] = y;
					x += d2;
					num ++;
				}
			}
			else { // even rows
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
			
			x = Math.random()*400; //[0,400)
			y = Math.random()*50;
			cX[i] = x;
			cY[i] = y;
			
			for (int j=0; j<stations; j++) {
				distance[i][j] = Math.sqrt((cX[i]-sX[j])*(cX[i]-sX[j]) + (cY[i]-sY[j])*(cY[i]-sY[j]));
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
		
		double d0 = 1.0;
		double a = 4.6, b = 0.0075, c = 12.6;
		double hTx = 7.0, hRx = 1.5;
		double f = 28000.0, y = 299792458.0/(f*1000000.0);
		double power = 1.0;
		
		double PLd0 = 20.0 * Math.log10((4.0*Math.PI*d0)/y);
		double n = a - b*hTx + c/hTx;
		double Xfc = 6.0 * Math.log10(f/2000.0);
		double XRx = -10.8 * Math.log10(hRx/2.0);
		
		double PLSUId = PLd0 + 10.0*n*Math.log10(d/d0) + Xfc + XRx;
		double signalStrength = power * Math.pow(10.0, -PLSUId/10.0);
		
		/*System.out.println("PLd0: " + PLd0);
		System.out.println("n: " + n);
		System.out.println("Xfc: " + Xfc);
		System.out.println("XRx: " + XRx);
		System.out.println("PLSUId: " + PLSUId);
		System.out.println("signal strength: " + signalStrength);*/
		
		return signalStrength;
	}
	
	public void connect() { //connect clients to stations where clients can get maximum signal strength
		
		double maxSignalStrength;
		int stationNo = 0;
		
		for (int i=0; i<clients; i++) {
			
			maxSignalStrength = signalStrength[i][0];
			
			for (int j=0; j<stations; j++) {
				if (maxSignalStrength <= signalStrength[i][j]) {
					maxSignalStrength = signalStrength[i][j];
					stationNo = j;
				}
			}
			
			connection.put(i, stationNo);
		}
		
		/*System.out.println("¡¾Original Connection¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();*/
	}
	
	public void annealing() {
		
		HashMap<Integer, Integer> original = new HashMap<>();
		double minThroughput1 = 0, minThroughput2 = 0;
		double minSINR, thisSINR;
		double T = 3000, k0 = 4.5 * Math.pow(10, -5); //T is temperature, k0 is Boltzmann's constant
		double probability;
		int accept = 0;
		
		while (T > 1) {
			
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
			
			/*System.out.println("Minimum throughput is " + minThroughput1 + " before change, " + minThroughput2 + " after change.");*/
			
			if (minThroughput2 > minThroughput1) { //if the change increases minimum throughput, accept
				accept = 1;
				/*System.out.println("Accepted.");
				System.out.println("¡¾New connection¡¿");
				for (int i : connection.keySet()) {
					System.out.print(i + "-" + connection.get(i) + "\t");
				}
				System.out.println();*/
			}
			else { //if the change doesn't increase minimum throughput, accept probabilistically and decrease temperature
				probability = Math.pow(Math.E, (minThroughput2-minThroughput1)/(k0*T));
				/*System.out.println("probability = " + probability);*/
				
				if (Math.random() < probability) {
					accept = 1;
					/*System.out.println("Accepted.");
					System.out.println("¡¾New connection¡¿");
					for (int i : connection.keySet()) {
						System.out.print(i + "-" + connection.get(i) + "\t");
					}
					System.out.println();*/
				}
				else {
					accept = 0;
					for (int i : original.keySet()) { //recover original configuration
						connection.put(i, original.get(i));
					}
					/*System.out.println("Not Accepted.");*/
				}
				
				T*=0.995;
			}
		}
		
		/*System.out.println("¡¾Final Connection¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();*/
		
		if (accept == 0) { //not accepted
			resultOfSA = minThroughput1;
			System.out.println("Minimum throughput is " + minThroughput1 + " from simulated annealing");
		}
		else { //accepted
			resultOfSA = minThroughput2;
			System.out.println("Minimum throughput is " + minThroughput2 + " from simulated annealing");
		}
		
		/*System.out.println("The End");*/
	}
	
	public void change() { //select (clients/10) number of clients with minimum SINR, and randomly connect to a new station
		
		Random r = new Random();
		int clientNo, stationNo, newStationNo;
		
		/*System.out.println("¡¾What's changed¡¿");*/
		for (int i=0; i<2; i++) {
			
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
	
	public void enumerate() {
		
		int[] using = new int[clients];
		double minThroughput, thisThroughput;
		ArrayList<Double> result = new ArrayList<>();
		double bestResult;

		for (int i=0; i<Math.pow(stations, clients); i++) {
			
			/*for (int j=0; j<clients; j++) {
				System.out.print(using[j] + "\t");
			}
			System.out.println();*/
			
			minThroughput = Math.log10(1 + getSINR(0, using));
			
			for (int j=0; j<clients; j++) {
				thisThroughput = Math.log10(1 + getSINR(j, using));
				if (minThroughput > thisThroughput) {
					minThroughput = thisThroughput;
				}
				result.add(minThroughput);
			}
			
			for (int j=clients-1; j>=0; j--) {
				using[j] = (using[j]+1)%stations;
				if (using[j] != 0) {
					break;
				}
			}
		}
		
		bestResult = result.get(0);
		
		for (double d : result) {
			if (bestResult < d) {
				bestResult = d;
			}
		}
		
		resultOfEN = bestResult;
		System.out.println("Minimum throughput is " + bestResult + " from enumeration");
	}
	
	public double getSINR(int clientNo, int[] using) {
		
		double sum = 0;
		int stationNo = using[clientNo];
		
		for (int i=0; i<clients; i++) {
			if (using[i] == stationNo) {
				sum +=signalStrength[i][stationNo];
			}
		}
		
		return signalStrength[clientNo][stationNo] / sum;
	}	
}