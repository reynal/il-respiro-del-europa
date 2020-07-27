package fan;

import com.pi4j.io.gpio.*;

public class Ventilateur {

	public final static com.pi4j.io.gpio.Pin FAN_1 = RaspiPin.GPIO_26; // pin 32 (GPIO_XX = wPi numbering scheme)
	public final static com.pi4j.io.gpio.Pin FAN_2 = RaspiPin.GPIO_27; // pin 36 
	public final static com.pi4j.io.gpio.Pin FAN_3 = RaspiPin.GPIO_28; // pin 38 
	public final static com.pi4j.io.gpio.Pin FAN_4 = RaspiPin.GPIO_29; // pin 40
	
	GpioPinDigitalOutput fan1, fan2, fan3, fan4;
	
	public Ventilateur() {
		fan1 = GpioFactory.getInstance().provisionDigitalOutputPin(FAN_1);
		fan2 = GpioFactory.getInstance().provisionDigitalOutputPin(FAN_2);
		fan3 = GpioFactory.getInstance().provisionDigitalOutputPin(FAN_3);
		fan4 = GpioFactory.getInstance().provisionDigitalOutputPin(FAN_4);
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		Ventilateur v = new Ventilateur();
		int i=0;
		while(i++ < 10) {
			System.out.println(i);
			//v.fan1.toggle();
			//Thread.sleep((int)(1000 * Math.random()));
			v.fan2.toggle();
			Thread.sleep((int)(5000 * Math.random()));
			//Thread.sleep(5000);
			/*v.fan3.toggle();
			Thread.sleep((int)(1000 * Math.random()));
			v.fan4.toggle();
			Thread.sleep((int)(1000 * Math.random()));*/
		}
		v.fan1.low();
		v.fan2.low();
		v.fan3.low();
		v.fan4.low();
	}

}
