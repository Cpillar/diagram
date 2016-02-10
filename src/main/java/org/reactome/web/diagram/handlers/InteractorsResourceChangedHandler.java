package org.reactome.web.diagram.handlers;

import com.google.gwt.event.shared.EventHandler;
import org.reactome.web.diagram.events.InteractorsResourceChangedEvent;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public interface InteractorsResourceChangedHandler extends EventHandler {
    void onInteractorsResourceChanged(InteractorsResourceChangedEvent event);
}
