public class SimulatedAnnealingTest{
	
	public static void main(String[] args) {
		
		SimulatedAnnealingTest t = new SimulatedAnnealingTest();
		t.calcSignalStrength(10);
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
		
		System.out.println("d: " + d);
		System.out.println("PLd0: " + PLd0);
		System.out.println("n: " + n);
		System.out.println("Xfc: " + Xfc);
		System.out.println("XRx: " + XRx);
		System.out.println("PLSUId: " + PLSUId);
		System.out.println("signal strength: " + signalStrength);
		System.out.println();
		
		return signalStrength;
	}
}