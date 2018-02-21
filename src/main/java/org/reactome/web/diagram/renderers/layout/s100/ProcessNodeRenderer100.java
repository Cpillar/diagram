package org.reactome.web.diagram.renderers.layout.s100;

import com.google.gwt.canvas.dom.client.Context2d;
import org.reactome.web.diagram.data.graph.model.GraphPathway;
import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.data.layout.Node;
import org.reactome.web.diagram.data.layout.NodeProperties;
import org.reactome.web.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.web.diagram.renderers.common.ColourProfileType;
import org.reactome.web.diagram.renderers.common.OverlayContext;
import org.reactome.web.diagram.renderers.common.RendererProperties;
import org.reactome.web.diagram.renderers.layout.abs.ProcessNodeAbstractRenderer;
import org.reactome.web.diagram.util.AdvancedContext2d;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ProcessNodeRenderer100 extends ProcessNodeAbstractRenderer {
    @Override
    public void draw(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        if(!isVisible(item)) return;

        Node node = (Node) item;
        NodeProperties prop = NodePropertiesFactory.transform(node.getProp(), factor, offset);
        shape(ctx, prop, node.getEncapsulated(), node.getNeedDashedBorder());
        ctx.fill();
        ctx.stroke();

        ctx.save();
        ctx.setFillStyle("#FEFDFF");
        innerShape(ctx, prop, node.getEncapsulated());
        ctx.fill();
        ctx.stroke();
        ctx.restore();
    }

    @Override
    public void shape(AdvancedContext2d ctx, NodeProperties prop, Boolean encapsulated, Boolean needsDashed) {
        ctx.beginPath();
        if(encapsulated!=null && encapsulated){
            ctx.homePlate(
                    prop.getX(),
                    prop.getY(),
                    prop.getWidth(),
                    prop.getHeight(),
                    RendererProperties.PROCESS_NODE_INSET_WIDTH
            );
        } else {
            ctx.rect(
                    prop.getX(),
                    prop.getY(),
                    prop.getWidth(),
                    prop.getHeight()
            );
        }
    }

    protected void innerShape(AdvancedContext2d ctx, NodeProperties prop, Boolean encapsulated) {
        ctx.beginPath();
        if (encapsulated != null && encapsulated) {
            ctx.homePlate(
                    prop.getX() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
                    prop.getY() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
                    prop.getWidth() - RendererProperties.PROCESS_NODE_INSET_WIDTH * 2,
                    prop.getHeight() - RendererProperties.PROCESS_NODE_INSET_WIDTH * 2,
                    RendererProperties.PROCESS_NODE_INSET_WIDTH * .75
            );
        }else {
            ctx.rect(
                    prop.getX() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
                    prop.getY() + RendererProperties.PROCESS_NODE_INSET_WIDTH,
                    prop.getWidth() - RendererProperties.PROCESS_NODE_INSET_WIDTH * 2,
                    prop.getHeight() - RendererProperties.PROCESS_NODE_INSET_WIDTH * 2
            );
        }
    }

    @Override
    public void drawAnalysisResult(AdvancedContext2d ctx, OverlayContext overlay, DiagramObject item, Double factor, Coordinate offset){
        Node node = (Node) item;
        NodeProperties prop = NodePropertiesFactory.transform(node.getProp(), factor, offset);

        ctx.save();
        setColourProperties(ctx, ColourProfileType.NORMAL);
        shape(ctx, prop, node.getEncapsulated(), node.getNeedDashedBorder());
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        ctx.save();
        ctx.setFillStyle("#FEFDFF");
        ctx.setGlobalAlpha(0.75);
        innerShape(ctx, prop, node.getEncapsulated());
        ctx.fill();
        ctx.stroke();
        ctx.restore();

        GraphPathway graphPathway = node.getGraphObject();
        AdvancedContext2d buffer = overlay.getBuffer();
        buffer.save();
        setColourProperties(buffer, ColourProfileType.NORMAL);
        buffer.setLineWidth(ctx.getLineWidth());
        buffer.setFillStyle(ctx.getFillStyle());
        buffer.fillRect(prop.getX(), prop.getY(), prop.getWidth() * graphPathway.getPercentage(), prop.getHeight());

        buffer.setGlobalCompositeOperation(Context2d.Composite.SOURCE_ATOP);
        buffer.setStrokeStyle(ctx.getStrokeStyle());
        innerShape(buffer, prop, node.getEncapsulated());
        buffer.stroke();

        buffer.setGlobalCompositeOperation(Context2d.Composite.DESTINATION_IN);
        shape(buffer, prop, node.getEncapsulated(), node.getNeedDashedBorder());
        buffer.fill();

        ctx.drawImage(buffer.getCanvas(), 0, 0); //TODO: Improve this to copy only the region
        buffer.restore();
    }
}
