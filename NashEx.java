import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class NashEx extends Frame {
	
	private static int stations = 4;
	private static int clients = 8;
	private static double[] sX = new double[stations]; //x-coordinate of stations
	private static double[] sY = new double[stations]; //y-coordinate of stations
	private static double[] cX = new double[clients]; //x-coordinate of clients
	private static double[] cY = new double[clients]; //y-coordinate of clients
	private static double[][] distance = new double[clients][stations];
	private static double[][] signalStrength = new double[clients][stations];
	
	public static void main(String[] args) {
		
		NashEx ns = new NashEx();
		
		ns.setSize(500, 150);
		ns.setVisible(true);
		ns.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
				super.windowClosing(e);
			}
		});
		
		ns.stationDeploy();
		ns.clientDistribute();
		ns.enumerate();
		ns.repaint();
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
		int sRow = 2, sCol = 2; //sRow is the number of rows, sCol is the number of columns
		
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
			
			x = Math.random()*400; //[0,1100)
			y = Math.random()*50; //[0,400)
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
	
	public void enumerate() {
		
		int[] using = new int[clients];
		int sum;
		double sumOfThroughput = 0;
		double minSINR, thisSINR, minThroughput;
		ArrayList<Double> resultOfSum = new ArrayList<>();
		ArrayList<Double> resultOfMin = new ArrayList<>();
		double worstResult;
		
		for (int i=0; i<Math.pow(stations, clients); i++) {
			
			/*for (int j=0; j<clients; j++) {
				System.out.print(using[j] + "\t");
			}
			System.out.println();*/
				
			sum = 0;
			sumOfThroughput = 0;
			
			for (int j=0; j<clients; j++) {
				sum += willChange(j, using);
			}
			
			if (sum == 0) {
				
				for (int j=0; j<clients; j++) {
					System.out.print(using[j] + "\t");
				}
				System.out.println();
				
				minSINR = getSINR(0, using);
				
				for (int j=0; j<clients; j++) {
					
					thisSINR = getSINR(j, using);
					
					sumOfThroughput += Math.log10(1 + thisSINR);
					
					if (minSINR > thisSINR) {
						minSINR = thisSINR;
					}
				}
				
				minThroughput = Math.log10(1 + minSINR);
				
			//	resultOfSum.add(sumOfThroughput);
				//resultOfMin.add(minThroughput);
				
				System.out.println("Sum of throughput is " + sumOfThroughput);
				System.out.println("Minimum throughput is " + minThroughput);
			}
			
			for (int j=clients-1; j>=0; j--) {
				using[j] = (using[j]+1)%stations;
				if (using[j] != 0) {
					break;
				}
			}
		}
		
	/*	worstResult = resultOfSum.get(0);
		
		for (double d : resultOfSum) {
			if (worstResult > d) {
				worstResult = d;
			}
		}

		System.out.println("Worst sum of throughput is " + worstResult);
		
		worstResult = resultOfMin.get(0);
		
		for (double d : resultOfMin) {
			if (worstResult > d) {
				worstResult = d;
			}
		}

		System.out.println("Worst minimum throughput is " + worstResult);*/
	}
	
	public int willChange(int clientNo, int[] using) {
		
		int currentStation = using[clientNo];
		double currentLoad = getStationLoad(currentStation, using);
		double currentSINR = signalStrength[clientNo][currentStation] / currentLoad;
		double currentThroughput = Math.log10(1 + currentSINR);
		double newLoad, newSINR, newThroughput;
		int change = 0;
		
		for (int i=0; i<stations; i++) {
			if (i != currentStation) {
				newLoad = getStationLoad(i, using) + signalStrength[clientNo][i];
				newSINR = signalStrength[clientNo][i] / newLoad;
				newThroughput = Math.log10(1 + newSINR);
				
				if (newThroughput > currentThroughput) {
					change = 1;
					break;
				}
			}
		}
		
		return change;
	}
	
	public double getSINR(int clientNo, int[] using) {
		
		int stationNo = using[clientNo];
		double SINR = signalStrength[clientNo][stationNo] / getStationLoad(stationNo, using);
		
		return SINR;
	}
	
	public double getStationLoad(int stationNo, int[] using) {
		
		double load = 0;
		
		for (int i=0; i<clients; i++) {
			if (using[i] == stationNo) {
				load +=signalStrength[i][stationNo];
			}
		}
		
		return load;
	}
}