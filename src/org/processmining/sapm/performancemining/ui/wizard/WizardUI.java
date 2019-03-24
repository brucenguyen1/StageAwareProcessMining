package org.processmining.sapm.performancemining.ui.wizard;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.sapm.performancemining.filter.TraceAttributeFilterParameters;
import org.processmining.sapm.performancemining.models.SPF;
import org.processmining.sapm.performancemining.models.SPFManager;
import org.processmining.sapm.performancemining.models.StageBasedEnhancementChecker;
import org.processmining.sapm.performancemining.parameters.SPFConfig;
import org.processmining.sapm.performancemining.parameters.SPFSettingsListener;

/**
 * This class manages all windows for user inputs, such as wizard or parameter
 * forms It returns a frame as the result window
 */
public class WizardUI implements SPFSettingsListener {
	private UIPluginContext uicontext;
	private PluginContext context;

	SPFConfig config;

	private int introductionStep;
	private int eventStageMappingStep;
	private int bpfConfigStep;

	// steps
	private int currentStep;
	private int noSteps = 3;

	// gui for each steps
	private WizardStep[] wizardSteps;

	public WizardUI(PluginContext context, SPFConfig config) {
		this.context = context;
		this.config = config;
		//		Logger.startLog("C:\\Temp", "Log.txt");
	}

	public WizardUI(UIPluginContext context, SPFConfig config) {
		uicontext = context;
		this.config = config;
		//		Logger.startLog("C:\\Temp", "Log.txt");
	}

	public SPF mine(XLog log) {

		InteractionResult result = InteractionResult.NEXT;

		noSteps = 0;
		introductionStep = noSteps++;
		eventStageMappingStep = noSteps++;
		bpfConfigStep = noSteps++;

		wizardSteps = new WizardStep[noSteps];

		wizardSteps[introductionStep] = new IntroductionStep();
		wizardSteps[introductionStep].setListener(this);

		wizardSteps[eventStageMappingStep] = new EventStageMappingStep(true, config);
		wizardSteps[eventStageMappingStep].setListener(this);

		wizardSteps[bpfConfigStep] = new ConfigurationStep(true, config);
		wizardSteps[bpfConfigStep].setListener(this);

		currentStep = introductionStep;

		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= noSteps) {
				currentStep = noSteps - 1;
			}

			result = uicontext.showWizard("Performance Mining with Staged Process Flows", currentStep == 0,
					currentStep == (noSteps - 1), wizardSteps[currentStep]);
			switch (result) {
				case NEXT :
					go(1);
					break;
				case PREV :
					go(-1);
					break;
				case FINISHED :
					readSettings();
					try {
						StageBasedEnhancementChecker checker = new StageBasedEnhancementChecker(log, config);
						if (!checker.check()) {
							JOptionPane.showMessageDialog(null,
									"Stage-Based Enhancement does not meet required conditions. "
											+ "Check error messages at " + checker.getOutputFileName(), "Check",
									JOptionPane.ERROR_MESSAGE);
							return null;
						}

						TraceAttributeFilterParameters filter = new TraceAttributeFilterParameters();
						filter.setName("Full BPF");
						SPF fullBPF = SPFManager.getInstance().createBPF(config, filter);
						//return getBpfFrame(fullBPF, uicontext, log);
						return fullBPF;
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, e.getMessage());
						e.printStackTrace();
					} // do the main task
				default :
					uicontext.getFutureResult(0).cancel(true);
					uicontext.getFutureResult(1).cancel(true);
					return null;
			}
		}

	}

	private int go(int direction) {
		currentStep += direction;
		if ((currentStep >= 0) && (currentStep < noSteps)) {
			if (wizardSteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}

	private void readSettings() {
		for (int currentStep = 1; currentStep < noSteps; currentStep++) {
			wizardSteps[currentStep].readSettings();
		}
	}

	public void setStageList(List<String> stageList) {
		config.setStageList(stageList);
	}

	public void setEventStageMap(Map<String, String> eventStageMap) {
		config.setEventStageMap(eventStageMap);
	}

	public void setTimeZone(TimeZone timezone) {
		config.setTimeZone(timezone);
	}

	public void setCaseExitStatusList(List<String> exitTypeList) {
		config.setCaseExitStatusList(exitTypeList);
	}

	public void setCheckStartCompleteEvents(boolean check) {
		config.setCheckStartCompleteEvents(check);
	}
}
