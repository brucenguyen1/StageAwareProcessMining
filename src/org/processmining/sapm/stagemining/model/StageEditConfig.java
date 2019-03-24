package org.processmining.sapm.stagemining.model;

import java.util.List;

public class StageEditConfig {
	private Stage currentStage = null;
	private List<StageItem> newStageItems = null;
	private StageItem newEndMilestone = null;
	private boolean mergeWithPrevStage = false;
	private boolean endMilestoneBelongToPrevStage = false;
	
	public StageEditConfig(Stage currentStage, boolean mergeWithPrevStage, List<StageItem> newStageItems, 
								StageItem newEndMilestone, boolean endMilestoneBelongToPrevStage) {
		this.currentStage = currentStage;
		this.newStageItems = newStageItems;
		this.newEndMilestone = newEndMilestone;
		this.mergeWithPrevStage = mergeWithPrevStage;
		this.endMilestoneBelongToPrevStage = endMilestoneBelongToPrevStage;
	}
	
	public Stage getCurrentStage() {
		return this.currentStage;
	}
	
	public List<StageItem> getStageItems() {
		return this.newStageItems;
	}
	
	public StageItem getEndMilestone() {
		return this.newEndMilestone;
	}
	
	public boolean isMergeWithPrevStage() {
		return this.mergeWithPrevStage;
	}
	
	public boolean isEndMilestoneBelongToPrevStage() {
		return this.endMilestoneBelongToPrevStage;
	}
							
}
