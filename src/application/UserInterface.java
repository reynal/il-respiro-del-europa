package application;

import java.awt.Dimension;
import java.awt.Toolkit;
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
	
	private Box mainPanel;
	
	private JLabel microphoneLevelLBL, microphoneStatutLBL, breathForceLBL, chaosIntensityLBL;
	private JLabel windWaveStateLBL;
		
	/**
	 * 
	 */
	public UserInterface() {
		
		super();
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainPanel = new Box(BoxLayout.Y_AXIS);
		//mainPanel.setLayout(new GridLayout(60,2));
		
		microphoneStatutLBL = addJLabel("Record from: ");
		microphoneLevelLBL = addJLabel("Input level (dB): ");
		breathForceLBL = addJLabel("Breath force: ");
		chaosIntensityLBL = addJLabel("Chaos intensity: ");
		addJButton("(Force breath by value below)", e -> chaosDynamics.forceBreath());
		addJSpinner("Breath force value (%)", e -> chaosDynamics.breathForceValue=0.01*((Integer)((JSpinner)e.getSource()).getValue()));
		addJButton("(Force chaos to value below)", e -> chaosDynamics.forceChaosIntensity());
		addJSpinner("Chaos force value (%)", e -> chaosDynamics.chaosForceValue=0.01*((Integer)((JSpinner)e.getSource()).getValue()));
		addJSpinner("idle below (%)", e -> windWave.idle_threshold=0.01*((Integer)((JSpinner)e.getSource()).getValue()));
		addJSpinner("gentle below (%)", e -> windWave.gentle_threshold=0.01*((Integer)((JSpinner)e.getSource()).getValue()));
		addJSpinner("breath below (%)", e -> windWave.breath_threshold=0.01*((Integer)((JSpinner)e.getSource()).getValue()));

		addJSpinner("Decay time (s)", e -> chaosDynamics.setDecayTime((Integer)((JSpinner)e.getSource()).getValue()));
		addJSpinner("DECODER_ITERATION_PERIOD (>=0)", e -> sentencesAnimator.decoder_iteration_period=((Integer)((JSpinner)e.getSource()).getValue()));
		
		windWaveStateLBL = addJLabel("WindWave current state: ");
		
		
		addJButton("WindWave: force CHAOS", e -> windWave.setState(WindWave.State.CHAOS));
		addJButton("WindWave: force BREATH", e -> windWave.setState(WindWave.State.BREATHE));
		addJButton("WindWave: force GENTLE", e -> windWave.setState(WindWave.State.GENTLE));
		addJButton("WindWave: force IDLE", e -> windWave.setState(WindWave.State.IDLE));
		addWindWaveJSpinner(WindWave.State.CHAOS);
		addWindWaveJSpinner(WindWave.State.BREATHE);
		addWindWaveJSpinner(WindWave.State.GENTLE);
		addWindWaveJSpinner(WindWave.State.IDLE);
		
		//addJToggleButton("windwave", e -> System.out.println(((JToggleButton)e.getSource()).isSelected()));
		
		add(new JScrollPane(mainPanel));
		//this.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(600,dim.height/2);
		this.setLocation(0,dim.height/2);
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
		
		Box box = new Box(BoxLayout.X_AXIS);
		JLabel l1 = new JLabel(lbl);
		//l1.setHorizontalAlignment(SwingConstants.LEFT);
		JLabel l2 = new JLabel("--------------------");
		//l2.setHorizontalAlignment(SwingConstants.LEFT);
		box.add(l1);
		box.add(Box.createHorizontalGlue());
		box.add(l2);
		mainPanel.add(box);
		return l2;
	}
	
	private JSpinner addJSpinner(String lbl, ChangeListener listener) {
		
		Box box = new Box(BoxLayout.X_AXIS);
		JSpinner s = new JSpinner();
		JLabel l = new JLabel(lbl);
		//l.setHorizontalAlignment(SwingConstants.RIGHT);
		box.add(l);
		box.add(s);
		mainPanel.add(box);
		//this.getContentPane().add(b);
		//s.addChangeListener(e -> System.out.println(lbl+"="+s.getValue()));
		if (listener != null) s.addChangeListener(listener);
		return s;
	}
	
	private JButton addJButton(String buttonLbl, ActionListener listener) {
		
		JButton b = new JButton(buttonLbl);
		b.setAlignmentX(CENTER_ALIGNMENT);
		mainPanel.add(b);
		b.addActionListener(listener);
		return b;
	}
	
	private JToggleButton addJToggleButton(String lbl, ActionListener listener) {
		
		JToggleButton b = new JToggleButton(lbl);
		mainPanel.add(b);
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

	public void setWindWaveState(WindWave.State s) {
		SwingUtilities.invokeLater(() ->  windWaveStateLBL.setText(s.toString()));
	}

	// --------------
	
	public static void main(String[] args) throws Exception {
		new UserInterface();
	}
}
