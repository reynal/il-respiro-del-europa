package application;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import audio.ChaosDynamics;
import audio.Microphone;
import display.SentencesAnimator;
import fan.WindWave;

@SuppressWarnings("serial")
public class UserInterface extends JFrame {

	private SentencesAnimator sentencesAnimator; 
	private WindWave windWave;
	private Microphone microphone;
	private ChaosDynamics chaosDynamics;
	
	private JLabel microphoneLevelLBL, microphoneStatutLBL, breathForceLBL, chaosIntensityLBL;
		
	/**
	 * 
	 */
	public UserInterface() {
		
		super();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLayout(new GridLayout(60,2));
		
		microphoneStatutLBL = addJLabel("Record from: ");
		microphoneLevelLBL = addJLabel("Input level (dB): ");
		breathForceLBL = addJLabel("Breath force: ");
		chaosIntensityLBL = addJLabel("Chaos intensity: ");
		addJSpinner("Decay time (ms)", e -> chaosDynamics.setDecayTime((Integer)((JSpinner)e.getSource()).getValue()));
		
		addJButton("WindWave: state", "force CHAOS", e -> windWave.setState(WindWave.State.CHAOS));
		addJButton("WindWave: state", "force BREATH", e -> windWave.setState(WindWave.State.BREATHE));
		addJButton("WindWave: state", "force GENTLE", e -> windWave.setState(WindWave.State.GENTLE));
		addJButton("WindWave: state", "force IDLE", e -> windWave.setState(WindWave.State.IDLE));
		addWindWaveJSpinner(WindWave.State.CHAOS);
		addWindWaveJSpinner(WindWave.State.BREATHE);
		addWindWaveJSpinner(WindWave.State.GENTLE);
		addWindWaveJSpinner(WindWave.State.IDLE);
		
		//addJToggleButton("windwave", e -> System.out.println(((JToggleButton)e.getSource()).isSelected()));
		
		this.pack();
		//this.setSize(800,400);
		this.setVisible(true);
	}
	
	public void setChaosDynamics(ChaosDynamics chaosDynamics) {
		this.chaosDynamics = chaosDynamics;
	}

	public void setWindWave(WindWave windWave) {
		this.windWave = windWave;
	}

	public void setMicrophone(Microphone microphone) {
		this.microphone = microphone;
	}

	public void setSentencesAnimator(SentencesAnimator sentencesAnimator) {
		this.sentencesAnimator = sentencesAnimator;
	}

	private JLabel addJLabel(String lbl) {
		
		JLabel l1 = new JLabel(lbl);
		l1.setHorizontalAlignment(SwingConstants.RIGHT);
		JLabel l2 = new JLabel("                                                                   ");
		add(l1);
		add(l2);
		return l2;
	}
	
	private JSpinner addJSpinner(String lbl, ChangeListener listener) {
		
		JSpinner s = new JSpinner();
		JLabel l = new JLabel(lbl);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		//Box b = new Box(BoxLayout.X_AXIS);
		add(l);
		add(s);
		//this.getContentPane().add(b);
		//s.addChangeListener(e -> System.out.println(lbl+"="+s.getValue()));
		if (listener != null) s.addChangeListener(listener);
		return s;
	}
	
	private JButton addJButton(String lbl, String buttonLbl, ActionListener listener) {
		
		JLabel l = new JLabel(lbl);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		JButton b = new JButton(buttonLbl);
		add(l);
		add(b);
		b.addActionListener(listener);
		return b;
	}
	
	private JToggleButton addJToggleButton(String lbl, ActionListener listener) {
		
		JLabel l = new JLabel(lbl);
		l.setHorizontalAlignment(SwingConstants.RIGHT);
		JToggleButton b = new JToggleButton("on/off");
		add(l);
		add(b);
		b.addActionListener(listener);
		return b;
	}
	
	// --------------
	
	private void addWindWaveJSpinner(WindWave.State state) {
		addJSpinner(
				"WindWave " + state + ": T(ms)", 
				e -> {
					state.T=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": offset0(%)",
				e -> {
					state.offset0=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": duty0(%)",
				e -> {
					state.duty0=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": offset1(%)",
				e -> {
					state.offset1=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": duty1(%)",
				e -> {
					state.duty1=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": offset2(%)",
				e -> {
					state.offset2=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": duty2(%)",
				e -> {
					state.duty2=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": offset3(%)",
				e -> {
					state.offset3=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
		addJSpinner(
				"WindWave " + state + ": duty3(%)",
				e -> {
					state.duty3=((Integer)((JSpinner)e.getSource()).getValue());
					windWave.setState(state);}
				);
	}
	
	// --------------
	
	public void setMicrophoneStatut(String s) {
		SwingUtilities.invokeLater(() ->  microphoneStatutLBL.setText(s));
	}

	public void setMicrophoneLevel(double lvl) {
		SwingUtilities.invokeLater(() ->  microphoneLevelLBL.setText(Double.toString(lvl)));
	}

	public void setBreathForce(double lvl) {
		SwingUtilities.invokeLater(() ->  breathForceLBL.setText(Double.toString(lvl)));
	}

	public void setChaosIntensity(double lvl) {
		SwingUtilities.invokeLater(() ->  chaosIntensityLBL.setText(Double.toString(lvl)));
	}

	// --------------
	
	public static void main(String[] args) throws Exception {
		new UserInterface();
	}
}
