package org.reactome.web.diagram.controls.top.key;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import org.reactome.web.diagram.controls.top.common.AbstractMenuDialog;
import org.reactome.web.diagram.data.graph.model.GraphObject;
import org.reactome.web.diagram.data.layout.*;
import org.reactome.web.diagram.data.layout.factory.DiagramObjectException;
import org.reactome.web.diagram.data.layout.factory.DiagramObjectsFactory;
import org.reactome.web.diagram.data.layout.impl.CoordinateFactory;
import org.reactome.web.diagram.events.DiagramProfileChangedEvent;
import org.reactome.web.diagram.events.DiagramRequestedEvent;
import org.reactome.web.diagram.events.GraphObjectHoveredEvent;
import org.reactome.web.diagram.events.GraphObjectSelectedEvent;
import org.reactome.web.diagram.handlers.DiagramProfileChangedHandler;
import org.reactome.web.diagram.handlers.DiagramRequestedHandler;
import org.reactome.web.diagram.handlers.GraphObjectHoveredHandler;
import org.reactome.web.diagram.handlers.GraphObjectSelectedHandler;
import org.reactome.web.diagram.profiles.diagram.DiagramColours;
import org.reactome.web.diagram.renderers.Renderer;
import org.reactome.web.diagram.renderers.RendererManager;
import org.reactome.web.diagram.renderers.common.ColourProfileType;
import org.reactome.web.diagram.renderers.common.RendererProperties;
import org.reactome.web.diagram.util.AdvancedContext2d;
import org.reactome.web.diagram.util.Console;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class DiagramKey extends AbstractMenuDialog implements GraphObjectHoveredHandler, GraphObjectSelectedHandler,
        DiagramRequestedHandler, DiagramProfileChangedHandler {

    private static final Double FACTOR = 0.82;
    private static final Coordinate OFFSET = CoordinateFactory.get(0, 0);

    private EventBus eventBus;
    private Diagram diagram;

    private DiagramObject selected;

    private AdvancedContext2d hover;
    private AdvancedContext2d items;
    private AdvancedContext2d selection;
    private List<Canvas> canvases = new LinkedList<>();

    public DiagramKey(EventBus eventBus) {
        super("Diagram key");
        this.eventBus = eventBus;
        this.initHandlers();

        AbsolutePanel canvases = new AbsolutePanel();
        canvases.setStyleName(RESOURCES.getCSS().diagramCanvases());
        this.hover = this.createCanvas(canvases, 200, 315);
        this.items = this.createCanvas(canvases, 200, 315);
        this.selection = this.createCanvas(canvases, 200, 315);

        add(canvases);
        add(new Image(RESOURCES.diagramKey()));

        try {
            diagram = DiagramObjectsFactory.getModelObject(Diagram.class, RESOURCES.diagramkeyJson().getText());
        } catch (DiagramObjectException e) {
            Console.error(e.getMessage());
        }
    }

    private void draw() {
        if (!isVisible()) return;

        //Now time for tricks in order to maintain the code optimised for the general rendering ;)
        double factor = RendererProperties.getFactor();
        RendererProperties.setFactor(FACTOR);

        this.clearDiagramKey();
        for (Node node : diagram.getNodes()) {
            Renderer renderer = RendererManager.get().getDiagramKeyRenderer(node);
            if (renderer != null) {
                renderer.setColourProperties(items, ColourProfileType.NORMAL);
                renderer.draw(items, node, FACTOR, OFFSET);
                renderer.setTextProperties(items, ColourProfileType.NORMAL);
                items.setFont(RendererProperties.getFont(8));
                renderer.drawText(items, node, FACTOR, OFFSET);
            } else {
                Console.error(node.getRenderableClass());
            }
        }
        for (Edge edge : diagram.getEdges()) {
            Renderer renderer = RendererManager.get().getDiagramKeyRenderer(edge);
            if (renderer != null) {
                renderer.setColourProperties(items, ColourProfileType.NORMAL);
                renderer.draw(items, edge, FACTOR, OFFSET);
                renderer.setTextProperties(items, ColourProfileType.NORMAL);
                renderer.drawText(items, edge, FACTOR, OFFSET);
            } else {
                Console.error(edge.getRenderableClass());
            }
        }
        for (Note note : diagram.getNotes()) {
            Renderer renderer = RendererManager.get().getDiagramKeyRenderer(note);
            if (renderer != null) {
                renderer.setTextProperties(items, ColourProfileType.NORMAL);
                renderer.drawText(items, note, FACTOR, OFFSET);
            } else {
                Console.error(note.getRenderableClass());
            }
        }

        RendererProperties.setFactor(factor);
        //End of the tricks xDD

        selection.setStrokeStyle(DiagramColours.get().PROFILE.getProperties().getSelection());
        highlight(this.selection, this.selected);
    }

    private DiagramObject getDiagramObject(GraphObject graphObject) {
        if (graphObject != null) {
            return graphObject.getDiagramObjects().get(0);
        }
        return null;
    }

    private void highlight(AdvancedContext2d ctx, DiagramObject diagramObject) {
        if (diagramObject == null) return;

        //Now time for tricks in order to maintain the code optimised for the general rendering ;)
        double factor = RendererProperties.getFactor();
        RendererProperties.setFactor(FACTOR);

        String renderableClass = diagramObject.getRenderableClass();
        for (Node node : diagram.getNodes()) {
            if (node.getRenderableClass().equals(renderableClass)) {
                Renderer renderer = RendererManager.get().getDiagramKeyRenderer(node);
                if (renderer != null) {
                    renderer.highlight(ctx, node, FACTOR, OFFSET);
                    break;
                }
            }
        }
        for (Edge edge : diagram.getEdges()) {
            if (edge.getRenderableClass().equals(renderableClass)) {
                Renderer renderer = RendererManager.get().getDiagramKeyRenderer(edge);
                if (renderer != null) {
                    ctx.save();
                    ctx.setLineWidth(ctx.getLineWidth() / 1.5);
                    renderer.draw(ctx, edge, FACTOR, OFFSET);
                    ctx.restore();
                    break;
                }
            }
        }

        RendererProperties.setFactor(factor);
        //End of the tricks xDD
    }

    private void initHandlers() {
        this.eventBus.addHandler(GraphObjectSelectedEvent.TYPE, this);
        this.eventBus.addHandler(GraphObjectHoveredEvent.TYPE, this);
        this.eventBus.addHandler(DiagramProfileChangedEvent.TYPE, this);
        this.eventBus.addHandler(DiagramRequestedEvent.TYPE, this);
    }

    @Override
    public void onGraphObjectHovered(GraphObjectHoveredEvent event) {
        hover.setLineWidth(FACTOR * 9);
        hover.setStrokeStyle(DiagramColours.get().PROFILE.getProperties().getHighlight());
        cleanCanvas(hover);
        DiagramObject diagramObject = getDiagramObject(event.getGraphObject());
        highlight(hover, diagramObject);
    }


    @Override
    public void onGraphObjectSelected(GraphObjectSelectedEvent event) {
        selection.setLineWidth(FACTOR * 3);
        selection.setStrokeStyle(DiagramColours.get().PROFILE.getProperties().getSelection());
        cleanCanvas(selection);
        this.selected = getDiagramObject(event.getGraphObject());
        highlight(selection, this.selected);
    }

    @Override
    public void onDiagramRequested(DiagramRequestedEvent event) {

    }

    @Override
    public void onProfileChanged(DiagramProfileChangedEvent event) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                draw();
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) draw();
    }

    private void cleanCanvas(Context2d ctx) {
        ctx.clearRect(0, 0, ctx.getCanvas().getWidth(), ctx.getCanvas().getHeight());
    }

    private void clearDiagramKey() {
        for (Canvas canvas : canvases) {
            cleanCanvas(canvas.getContext2d());
        }
    }

    private AdvancedContext2d createCanvas(AbsolutePanel container, int width, int height) {
        Canvas canvas = Canvas.createIfSupported();
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        canvas.setPixelSize(width, height);
        container.add(canvas, 0, 10);
        this.canvases.add(canvas);
        return canvas.getContext2d().cast();
    }


    public static Resources RESOURCES;

    static {
        RESOURCES = GWT.create(Resources.class);
        RESOURCES.getCSS().ensureInjected();
    }

    public interface Resources extends ClientBundle {
        @Source(ResourceCSS.CSS)
        ResourceCSS getCSS();

        @Source("data/diagramkey.json")
        TextResource diagramkeyJson();

        @Source("../images/diagramKey.png")
        ImageResource diagramKey();

    }

    @CssResource.ImportedWithPrefix("diagram-DiagramKey")
    public interface ResourceCSS extends CssResource {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String CSS = "org/reactome/web/diagram/controls/top/key/DiagramKey.css";

        String diagramCanvases();
    }
}
