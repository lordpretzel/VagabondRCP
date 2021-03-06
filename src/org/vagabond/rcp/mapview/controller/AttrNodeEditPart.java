package org.vagabond.rcp.mapview.controller;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.vagabond.rcp.mapview.model.AttributeGraphNode;
import org.vagabond.rcp.mapview.model.ForeignKeyConnection;
import org.vagabond.rcp.mapview.model.MapConnection;
import org.vagabond.rcp.mapview.model.Node;
import org.vagabond.rcp.mapview.view.AttributeFigure;
import org.vagabond.rcp.mapview.view.LeftRightParentBoxFigureAnchor;
import org.vagabond.rcp.util.PluginLogProvider;

public class AttrNodeEditPart extends AbstractGraphicalEditPart 
	implements NodeEditPart, VagaSelectionEventProvider  {

	static Logger log = PluginLogProvider.getInstance().getLogger(
			AttrNodeEditPart.class);
	
	public AttrNodeEditPart(Node node) { 
		setModel(node);
	}
	
	@Override
	protected IFigure createFigure() {
		IFigure node = new AttributeFigure(false);		
		return node;
	}

	@Override
	public void activate() {
		super.activate();
	}

	@Override
	public void deactivate() {
	}

	protected void refreshVisuals() {
		AttributeFigure figure = (AttributeFigure) getFigure();
		AttributeGraphNode node = (AttributeGraphNode) getModel();
		figure.setAttrName(node.getName());
		figure.setBoldFont(node.isPK());
	}

	protected List<?> getModelSourceConnections() {
		return ((AttributeGraphNode)getModel()).getSourceConnections();
	}
	
	protected List<?> getModelTargetConnections() {
		return ((AttributeGraphNode)getModel()).getTargetConnections();
	}
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		// correspondence edit part connect source on left
		if (connection instanceof CorrespondenceEditPart)
			return new LeftRightParentBoxFigureAnchor(getFigure(), false);
		if (connection instanceof MapConnectionEditPart) {
			MapConnection mapConnModel = (MapConnection) connection.getModel();
			return new LeftRightParentBoxFigureAnchor(getFigure(), 
					mapConnModel.getSourceAttachLeft());
		}
		if (connection instanceof ForeignKeyConnEditPart) {
			ForeignKeyConnection fk = (ForeignKeyConnection) connection.getModel();
			return new LeftRightParentBoxFigureAnchor(getFigure(), 
					fk.getSourceAttachLeft());
		}
		throw new RuntimeException ("unkown connection type " 
				+ connection.getClass().getName());
    }
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new ChopboxAnchor(getFigure());
    }
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor (ConnectionEditPart connection) {
		// correspondence edit part connect source on left
		if (connection instanceof CorrespondenceEditPart)
			return new LeftRightParentBoxFigureAnchor(getFigure(), true);
		if (connection instanceof MapConnectionEditPart) {
			MapConnection mapConnModel = (MapConnection) connection.getModel();
			return new LeftRightParentBoxFigureAnchor(getFigure(), 
					mapConnModel.getTargetAttachLeft());
		}
		if (connection instanceof ForeignKeyConnEditPart) {
			ForeignKeyConnection fk = (ForeignKeyConnection) connection.getModel();
			return new LeftRightParentBoxFigureAnchor(getFigure(), 
					fk.getTargetAttachLeft());
		}
		throw new RuntimeException ("unkown connection type " 
				+ connection.getClass().getName());
//		return new LeftRightParentBoxFigureAnchor(getFigure(), true);
	}
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, 
				new SelectionFeedbackPolicy());
	}
	
	public boolean isSelected () {
		return getSelected() != EditPart.SELECTED_NONE;
	}

	@Override
	public void fireSelectionEvent(boolean selected) {	
		VagaSelectionEventProvider parent = (VagaSelectionEventProvider) 
				getParent();
		
		parent.fireSelectionEvent(selected);
	}

	@Override
	public boolean wasUserInteraction() {
		VagaSelectionEventProvider parent = (VagaSelectionEventProvider) 
				getParent();
		return parent.wasUserInteraction();
	}
	
	
}