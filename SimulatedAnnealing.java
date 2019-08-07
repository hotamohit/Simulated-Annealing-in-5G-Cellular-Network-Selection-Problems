import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Random;

public class SimulatedAnnealing extends Frame{
	
	private static int stations = 30;
	private static int clients = 100;
	private static double[] sX = new double[stations]; //x-coordinate of stations
	private static double[] sY = new double[stations]; //y-coordinate of stations
	private static double[] cX = new double[clients]; //x-coordinate of clients
	private static double[] cY = new double[clients]; //y-coordinate of clients
	private static double[][] distance = new double[clients][stations];
	private static double[][] signalStrength = new double[clients][stations];
	private static HashMap<Integer, Integer> connection = new HashMap<>();//key is clientNo, value is stationNo
	
	public static void main(String[] args) {
		
		SimulatedAnnealing sa = new SimulatedAnnealing();
		
		sa.setSize(1200, 500);
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
		sa.repaint();
	}
	
	public void paint(Graphics g) {
		
		int x, y, x2, y2; 
		
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
		
		g.setColor(Color.BLUE);
		for (int i=0; i<clients; i++) {
			x = (int)cX[i] + 42;
			y = (int)cY[i] + 42;
			x2 = (int)sX[connection.get(i)] + 47;
			y2 = (int)sY[connection.get(i)] + 47;
			g.drawLine(x, y, x2, y2);
		}
    }
	
	public void stationDeploy() {
		
		double x = 0, y = 0;
		double d1 = 70, d2 = 2*Math.sqrt(3)*d1; //d1 is the distance between rows, d2 is the distance between columns
		int num = 0;
		int sRow = 6, sCol = 5; //sRow is the number of rows, sCol is the number of columns
		
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
			y = Math.random()*400; //[0,400)
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
				if (maxSignalStrength <= signalStrength[i][j]) {
					maxSignalStrength = signalStrength[i][j];
					stationNo = j;
				}
			}
			
			connection.put(i, stationNo);
		}
		
		System.out.println("¡¾Original Connection¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();
	}
	
	public void annealing() {
		
		HashMap<Integer, Integer> original = new HashMap<>();
		double sumOfThroughput1 = 0, sumOfThroughput2 = 0;
		double minThroughput1 = 0, minThroughput2 = 0, maxThroughput1 = 0, maxThroughput2 = 0;
		double minSINR, thisSINR, maxSINR;
		double T = 3000, k0 = 5 * Math.pow(10, -10); //T is temperature, k0 is Boltzmann's constant
		double probability;
		int accept = 0;
		
		while (T > 1) {
			
			sumOfThroughput1 = 0;
			sumOfThroughput2 = 0;
			
			for (int i : connection.keySet()) { //save original configuration
				original.put(i, connection.get(i));
			}
			
			minSINR = getSINR(0);
			maxSINR = getSINR(0);
			
			for (int i=0; i<clients; i++) {
				
				thisSINR = getSINR(i);
				
				sumOfThroughput1 += Math.log10(1 + thisSINR);
				
				if (minSINR > thisSINR) {
					minSINR = thisSINR;
				}
				
				if (maxSINR < thisSINR) {
					maxSINR = thisSINR;
				}
			}
			
			minThroughput1 = Math.log10(1 + minSINR);
			maxThroughput1 = Math.log10(1 + maxSINR);
			
			change();
			
			minSINR = getSINR(0);
			maxSINR = getSINR(0);
			
			for (int i=0; i<clients; i++) {
				
				thisSINR = getSINR(i);
				
				sumOfThroughput2 += Math.log10(1 + thisSINR);
				
				if (minSINR > thisSINR) {
					minSINR = thisSINR;
				}
				
				if (maxSINR < thisSINR) {
					maxSINR = thisSINR;
				}
			}
			
			minThroughput2 = Math.log10(1 + minSINR);
			maxThroughput2 = Math.log10(1 + maxSINR);
			
			System.out.println("Sum of throughput is " + sumOfThroughput1 + " before change, " + sumOfThroughput2 + " after change.");
			
			if (sumOfThroughput2 > sumOfThroughput1) { //if the change increases objective function, accept
				accept = 1;
				System.out.println("Accepted.");
				System.out.println("¡¾New connection¡¿");
				for (int i : connection.keySet()) {
					System.out.print(i + "-" + connection.get(i) + "\t");
				}
				System.out.println();
			}
			else { //if the change doesn't increase objective function, accept probabilistically and decrease temperature
				probability = Math.pow(Math.E, (sumOfThroughput2-sumOfThroughput1)/(k0*T));
				System.out.println("probability = " + probability);
				
				if (Math.random() < probability) {
					accept = 1;
					System.out.println("Accepted.");
					System.out.println("¡¾New connection¡¿");
					for (int i : connection.keySet()) {
						System.out.print(i + "-" + connection.get(i) + "\t");
					}
					System.out.println();
				}
				else {
					accept = 0;
					for (int i : original.keySet()) { //recover original configuration
						connection.put(i, original.get(i));
					}
					System.out.println("Not Accepted.");
				}
				
				T*=0.8;
			}
		}
		
		System.out.println("¡¾Final Connection¡¿");
		for (int i : connection.keySet()) {
			System.out.print(i + "-" + connection.get(i) + "\t");
		}
		System.out.println();
		
		if (accept == 0) { //not accepted
			System.out.println("Sum of throughput is " + sumOfThroughput1);
			System.out.println("Minimum throughput is " + minThroughput1);
			System.out.println("Maximum throughput is " + maxThroughput1);
			System.out.println("Maximum throughput / minimum throughput is " + maxThroughput1/minThroughput1);	
		}
		else { //accepted
			System.out.println("Sum of throughput is " + sumOfThroughput2);
			System.out.println("Minimum throughput is " + minThroughput2);
			System.out.println("Maximum throughput is " + maxThroughput2);
			System.out.println("Maximum throughput / minimum throughput is " + maxThroughput2/minThroughput2);	
		}
		
		System.out.println("The End");
	}
	
	public void change() { //select (clients/10) number of clients with minimum SINR, and randomly connect to a new station
		
		Random r = new Random();
		int clientNo, stationNo, newStationNo;
		
		System.out.println("¡¾What's changed¡¿");
		for (int i=0; i<clients/10; i++) {
			
			clientNo = findMinSINR();
			stationNo = connection.get(clientNo);
			newStationNo = stationNo;
			
			while (newStationNo == stationNo) {
				newStationNo = r.nextInt(stations);
			}
			
			connection.put(clientNo, newStationNo);
			System.out.print(clientNo + "-" + newStationNo + "\t");
		}
		System.out.println();
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