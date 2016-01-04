package org.reactome.web.diagram.handlers;

import com.google.gwt.event.shared.EventHandler;
import org.reactome.web.diagram.events.DiagramRenderedEvent;


/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface DiagramRenderedHandler extends EventHandler {

    void onDiagramRendered(DiagramRenderedEvent event);
}
