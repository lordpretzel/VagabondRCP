package org.vagabond.rcp.controller;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.vagabond.rcp.gui.views.ExplRankView;
import org.vagabond.rcp.model.ContentProvider;

public class ExplViewActionGroup extends ActionGroup {
	private final ExplRankView explView;
	
    private SelectionListenerAction nextAction;
    private SelectionListenerAction previousAction;
    private SelectionListenerAction firstAction;
    
    public ExplViewActionGroup(ExplRankView explView) {
		this.explView = explView;

		this.firstAction = new FirstExplAction(this.explView);
		this.nextAction = new NextExplAction(this.explView);
		this.previousAction = new PrevExplAction(this.explView);
    }
    
	public void fillActionBars(IActionBars actionBars) {
        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(this.firstAction);
        toolBar.add(this.previousAction);
        toolBar.add(this.nextAction);        
    }
	
	public void updateActionBars() {
		this.firstAction.setEnabled(true);
		this.nextAction.setEnabled(ContentProvider.getInstance().getExplCol().hasNext());
		this.previousAction.setEnabled(ContentProvider.getInstance().getExplCol().hasPrevious());
	}
}
