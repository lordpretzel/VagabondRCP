package org.vagabond.rcp.selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.ui.PlatformUI;
import org.vagabond.rcp.controller.StatusLineController;
import org.vagabond.rcp.gui.views.CorrView;
import org.vagabond.rcp.gui.views.ExplView;
import org.vagabond.rcp.gui.views.MappingsView;
import org.vagabond.rcp.gui.views.SourceDBView;
import org.vagabond.rcp.gui.views.TargetDBView;
import org.vagabond.rcp.gui.views.TransView;
import org.vagabond.rcp.selection.VagaSelectionEvent.ModelType;
import org.vagabond.rcp.util.PluginLogProvider;
import org.vagabond.util.LoggerUtil;

/**
 * 
 * @author lord_pretzel
 *
 */
public class GlobalSelectionController {

	static Logger log = PluginLogProvider.getInstance().getLogger(
			GlobalSelectionController.class);
	
	private static GlobalSelectionController inst = new GlobalSelectionController();
	
	private VagaSelectionSequence seq;
	private Map<ModelType, List<VagaSelectionListener>> listeners;
	private boolean mode = false;
	
	
	/**
	 * Create the single GlobalSelectionController with an empty selection sequence
	 */
	
	private GlobalSelectionController () {
		seq = (VagaSelectionSequence) VagaSelectionSequence.EMPTY_SELECTION_SEQ.clone();
		listeners = new HashMap<ModelType, List<VagaSelectionListener>> ();
		for(ModelType type: ModelType.values())
			listeners.put(type, new ArrayList<VagaSelectionListener> ());
	}

	/**
	 * 
	 * @param e
	 */
	
	public static void fireModelSelection (VagaSelectionEvent e) {
		log.debug("got selection event: " + e.toString() + " current sequence is: " 
				+ inst.seq.toString() + " under mode " + inst.mode);
		
		// depending on mode change type of events
		e.setLimitScope(getMode());
		
		if(!inst.validateEvent(e))
			return;
		inst.informListeners(e); //TODO more complex, parts of the seq may be reduced need to tell listeneres about that
		inst.setViewsBasedOnType(e);
		inst.updateStatusLine();
	}
	
	// check if event valid and if we need to fire additional deselection events
	private boolean validateEvent (VagaSelectionEvent e) {
		// try to reset or clear 
		if (e.elementType.equals(ModelType.None)) {
			// clear/reset empty sequence?
			if (inst.seq.isEmpty()) {
				log.error("desect or reset event for empty queue.");
				return false;
			}
			
			// reset/clear is always ok
			inst.seq.clear();
			return true;
		}
		// new seq started
		if (inst.seq.isEmpty()) {
			inst.seq.appendEvent(e);
			return true;
		}
		// type of interaction changed, clear selection/reset and start new seq
		if (inst.seq.isLimitScope() != e.isLimitScope()) {
			// send reset/clear event
			if (inst.seq.isLimitScope()) {
				log.debug("sent RESET_SCOPE");
				informListeners(VagaSelectionEvent.RESET_SCOPE);
			}
			else {
				log.debug("sent DESELECTION");
				informListeners(VagaSelectionEvent.DESELECT);
			}
			inst.seq.makeSingleton(e);
			
			return true;
		}
		// normal navigation
		if (!e.isLimitScope()) {
			log.debug("sent DESELECTION");
			informListeners(VagaSelectionEvent.DESELECT);
			inst.seq.makeSingleton(e);
			return true;
		}
		// scope navigation
		log.debug("sent RESET_SCOPE");
		informListeners(VagaSelectionEvent.RESET_SCOPE);
		inst.seq.makeSingleton(e);
		//TODO
		return true;
	}
	
	private void setViewsBasedOnType(VagaSelectionEvent e) {
		try {
			switch(e.getElementType()) {
			case None:
				break;
			case Mapping:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(MappingsView.ID);
				break;
			case Transformation:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(TransView.ID);
				break;
			case SourceRelation:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(SourceDBView.ID);
				break;
			case TargetRelation:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(TargetDBView.ID);
				break;
			case Correspondence:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(CorrView.ID);
				break;
			case Explanation:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.showView(ExplView.ID);
				break;
			default:
				break;
			}
		} catch (Exception e1) {
			LoggerUtil.logException(e1, log);
		}
	}
	
	/**
	 * 
	 * @param e
	 */
	
	private void informListeners (VagaSelectionEvent e) {
		for(VagaSelectionListener listener: listeners.get(e.getElementType()))  {
			try {
				listener.event(e);
			} catch (Exception e1) {
				LoggerUtil.logException(e1, log, "Error processing event " 
						+ e.toString  + " by listener " + listener.toString());
			}
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	
	public static void addSelectionListener (VagaSelectionListener listener) {
		// add listener to the list of listeners for all events it is interested in
		for(ModelType interest: listener.interestedIn()) {
			if (!inst.listeners.get(interest).contains(listener))
				inst.listeners.get(interest).add(listener);
		}
	}

	public static void removeSelectionListener(VagaSelectionListener listener) {
		for(ModelType interest: listener.interestedIn())
			inst.listeners.get(interest).remove(listener);		
	}
	
	public static boolean getMode() {
		return inst.mode;
	}
	
	public static void setMode (boolean mode) {
		if (!inst.seq.isEmpty())
			fireModelSelection(inst.mode ? VagaSelectionEvent.RESET_SCOPE 
					: VagaSelectionEvent.DESELECT);
		inst.mode = mode;
	}

	private void updateStatusLine () {
		StatusLineController.setStatus(seq.toUserString());
	}

	public static void switchMode() {
		inst.mode = !inst.mode;
		if (!inst.seq.isEmpty()) {
			inst.seq.clear();
			inst.informListeners(inst.clearForMode(!inst.mode));
		}
	}
	
	private VagaSelectionEvent clearForMode (boolean mode) {
		if (mode)
			return VagaSelectionEvent.RESET_SCOPE;
		return VagaSelectionEvent.DESELECT;
	}

	public static String getModeAsString() {
		return inst.mode ? "Drill down navigation" : "Browsing";
	}
	
}
