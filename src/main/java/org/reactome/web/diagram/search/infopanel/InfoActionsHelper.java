package org.reactome.web.diagram.search.infopanel;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventBus;
import org.reactome.web.diagram.data.graph.model.GraphObject;
import org.reactome.web.diagram.events.GraphObjectHoveredEvent;
import org.reactome.web.diagram.events.GraphObjectSelectedEvent;
import org.reactome.web.diagram.events.InteractorSelectedEvent;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
abstract class InfoActionsHelper {

    static ClickHandler getLinkClickHandler(final GraphObject reaction, final EventBus eventBus, final Object source){
        return event -> eventBus.fireEventFromSource(new GraphObjectSelectedEvent(reaction, true), source);
    }

    static MouseOutHandler getLinkMouseOut(final EventBus eventBus, final Object source){
        return event -> eventBus.fireEventFromSource(new GraphObjectHoveredEvent(), source);
    }

    static MouseOverHandler getLinkMouseOver(final GraphObject reaction, final EventBus eventBus, final Object source){
        return event -> eventBus.fireEventFromSource(new GraphObjectHoveredEvent(reaction), source);
    }

    /////////////////
    // Interactors //
    /////////////////

    static ClickHandler getInteractorLinkClickHandler(final String url, final EventBus eventBus, final Object source){
        return event -> {
            event.stopPropagation(); event.preventDefault();
            eventBus.fireEventFromSource(new InteractorSelectedEvent(url), source);
        };
    }

    static ClickHandler getInteractionLinkClickHandler(final String url, final EventBus eventBus, final Object source){
        return event -> {
            event.stopPropagation(); event.preventDefault();
            eventBus.fireEventFromSource(new InteractorSelectedEvent(url), source);
        };
    }
}
