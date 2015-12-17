package org.reactome.web.diagram.messages;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import org.reactome.web.diagram.common.PwpButton;
import org.reactome.web.diagram.events.AnalysisResultLoadedEvent;
import org.reactome.web.diagram.events.AnalysisResultRequestedEvent;
import org.reactome.web.diagram.events.DiagramInternalErrorEvent;
import org.reactome.web.diagram.events.DiagramRequestedEvent;
import org.reactome.web.diagram.handlers.AnalysisResultLoadedHandler;
import org.reactome.web.diagram.handlers.AnalysisResultRequestedHandler;
import org.reactome.web.diagram.handlers.DiagramInternalErrorHandler;
import org.reactome.web.diagram.handlers.DiagramRequestedHandler;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ErrorMessage extends MessagesPanel implements AnalysisResultRequestedHandler, AnalysisResultLoadedHandler,
        DiagramRequestedHandler, DiagramInternalErrorHandler, ClickHandler {

    private Label message;

    public ErrorMessage(EventBus eventBus) {
        super(eventBus);

        MessagesPanelCSS css = RESOURCES.getCSS();
        //Setting the legend style
        addStyleName(css.errorMessage());

        FlowPanel fp = new FlowPanel();
        fp.add(new Image(RESOURCES.error()));

        PwpButton closeBtn = new PwpButton("close", RESOURCES.getCSS().close(), this);
        fp.add(closeBtn);

        message = new Label("Error holder");
        message.setStyleName(MessagesPanel.RESOURCES.getCSS().errorMessageText());
        fp.add(message);

        this.add(fp);
        setVisible(false);
        this.initHandlers();
    }

    @Override
    public void onAnalysisResultLoaded(AnalysisResultLoadedEvent event) {
        this.setVisible(false);
    }

    @Override
    public void onAnalysisResultRequested(AnalysisResultRequestedEvent event) {
        this.setVisible(false);
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
        this.setVisible(false);
    }

    @Override
    public void onDiagramRequested(DiagramRequestedEvent event) {
        this.setVisible(false);
    }

    @Override
    public void onDiagramInternalError(DiagramInternalErrorEvent event) {
        this.message.setText(event.getMessage());
        this.setVisible(true);
    }

    private void initHandlers() {
        this.eventBus.addHandler(DiagramInternalErrorEvent.TYPE, this);
        this.eventBus.addHandler(DiagramRequestedEvent.TYPE, this);
        this.eventBus.addHandler(AnalysisResultRequestedEvent.TYPE, this);
        this.eventBus.addHandler(AnalysisResultLoadedEvent.TYPE, this);
    }
}
