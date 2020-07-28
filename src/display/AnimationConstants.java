package display;

public enum AnimationConstants {
	
	SCREEN_0(5), 
	SCREEN_1(5);
	
	double Tsec;
	
	AnimationConstants(double Tsec){
		this.Tsec = Tsec;
	}
}