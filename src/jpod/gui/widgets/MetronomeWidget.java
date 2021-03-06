package jpod.gui.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jpod.utils.Metronome;
import jpod.gui.PresetSelectionDialog;
import jpod.gui.basic.MetronomeAction;
import jpod.gui.basic.MetronomeListModel;
import jpod.gui.widgets.BaseWidget;
import line6.Device;
import line6.DeviceSettings;
import line6.commands.ChangeChannelCommand;
import line6.commands.parameters.BaseParameter;

public class MetronomeWidget extends BaseWidget {

	private JSpinner bpm;
	private JSpinner beatsPerMeasure;
	private JButton browseButton;
	private JLabel currentBarLabel;
	private Metronome metronome;
	private JButton startStopButton;
	private JList actionList;
	
	public MetronomeWidget(Device dev) {
		super(dev);
		setLayout(new GridBagLayout());
		
		bpm = new JSpinner();
		bpm.setModel(new SpinnerNumberModel(120,1, 500, 10));
		bpm.setToolTipText("Tempo - Beats per minute");
		
		actionList = new JList();
		actionList.setLayoutOrientation(JList.VERTICAL_WRAP);
		MetronomeListModel model = new MetronomeListModel();
		actionList.setModel(model);
		JScrollPane listScroller = new JScrollPane(actionList);
		
		beatsPerMeasure = new JSpinner();
		beatsPerMeasure.setModel(new SpinnerNumberModel(4,1,64,1));
		beatsPerMeasure.setToolTipText("Beats per measure");
		bpm.addChangeListener(new MetronomeSettingsChangeListener());
		beatsPerMeasure.addChangeListener(new MetronomeSettingsChangeListener());
		metronome = new Metronome(4,120, new MetronomeBeatListener());
		
		browseButton = new JButton("Add preset...");
		browseButton.addActionListener(new BrowseButtonListener());
		
		currentBarLabel = new JLabel("Current bar #1");
		
		startStopButton = new JButton("Start");
		startStopButton.setActionCommand("start_metronome");
		startStopButton.addActionListener(new StartStopMetronomeListener());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.CENTER;
		c.weighty = 1;
		c.weightx = 1;
		add(currentBarLabel, c);
		
		
		c.gridx = 1;
		add(beatsPerMeasure, c);
		
		c.gridx = 2;
		add(bpm, c);
		
		c.gridx = 3;
		add(startStopButton,c);
		c.gridx = 4;
		add(browseButton,c);
		c.gridy = 1;
		c.gridx = 0;
		c.weightx = 4;
		c.weighty = 5;
		c.gridwidth = 5;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		add(listScroller, c);
		
	}

	@Override
	void activeDeviceChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void presetsSynchronized(Device dev) {
		
	}

	@Override
	public void activePresetChanged(Device dev, DeviceSettings oldPreset) {
		// TODO Auto-generated method stub

	}

	@Override
	public void parameterChanged(Device dev, BaseParameter p, int value,
			int oldValue) {
		// TODO Auto-generated method stub

	}

	class MetronomeBeatListener extends TimerTask
	{
		@Override
		public void run() {
			MetronomeListModel model = (MetronomeListModel)actionList.getModel();
			if(model.getSize() > 0)
			{
				MetronomeAction action = (MetronomeAction)model.getElementAt(0);
				if(metronome.getBar() == action.getBar())
				{
					ChangeChannelCommand c = new ChangeChannelCommand(action.getPreset().getId());
					sendCommand(c);
					model.removeElementAt(0);
				}
			}
			currentBarLabel.setText(String.format("Current bar #%d",metronome.getBar()));
		}
	}
	
	class MetronomeSettingsChangeListener implements ChangeListener
	{

		@Override
		public void stateChanged(ChangeEvent e) {
			JSpinner source = (JSpinner)e.getSource();
			SpinnerNumberModel model = (SpinnerNumberModel)source.getModel();
			if(source == bpm)
			{
				metronome.setBpm(model.getNumber().intValue());
			}
			else if(source == beatsPerMeasure)
			{
				metronome.setBeatsPerMeasure(model.getNumber().intValue());
			}
		}
		
	}
	
	class StartStopMetronomeListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent arg) {
			if(metronome.isRunning())
			{
				metronome.stop();
				startStopButton.setText("Start");
			}
			else
			{
				metronome.start();
				startStopButton.setText("Stop");
			}
			currentBarLabel.setText("Current bar #1");
		}
		
	}
	
	class BrowseButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(activeDevice != null)
			{
				PresetSelectionDialog dialog = new PresetSelectionDialog(activeDevice.getPresets());
				dialog.addActionListener(new PresetSelectionListener());
				dialog.setVisible(true);
			}
		}
		
	}
	
	class PresetSelectionListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getID() == PresetSelectionDialog.OK_BUTTON_PRESSED)
			{
				PresetSelectionDialog dialog = (PresetSelectionDialog)e.getSource();
				DeviceSettings preset = dialog.getSelectedPreset();
				if(preset != null)
				{
					int bar;
					String input = JOptionPane.showInputDialog(MetronomeWidget.this, "Please enter bar in which preset should change", "1");
					bar = Integer.parseInt(input);
					MetronomeListModel model = (MetronomeListModel)actionList.getModel();
					model.addElement(new MetronomeAction(preset, bar));
				}	
			}
		}
		
	}
}
