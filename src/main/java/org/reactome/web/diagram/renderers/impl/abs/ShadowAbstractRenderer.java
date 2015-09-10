package org.reactome.web.diagram.renderers.impl.abs;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import org.reactome.web.diagram.data.layout.*;
import org.reactome.web.diagram.data.layout.impl.CoordinateFactory;
import org.reactome.web.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.web.diagram.renderers.common.ColourProfileType;
import org.reactome.web.diagram.renderers.common.RendererProperties;
import org.reactome.web.diagram.util.AdvancedContext2d;

import java.util.List;

/**
 * @author Kostas Sidiropoulos (ksidiro@ebi.ac.uk)
 */
public class ShadowAbstractRenderer extends AbstractRenderer {
    @Override
    public void draw(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        Shadow shadow = (Shadow) item;

        Coordinate initial = shadow.getPoints().get(0).transform(factor, offset);
        ctx.beginPath();
        ctx.moveTo(initial.getX(), initial.getY());
        for (int i = 1; i < shadow.getPoints().size(); i++) {
            Coordinate aux = shadow.getPoints().get(i).transform(factor, offset);
            ctx.lineTo(aux.getX(), aux.getY());
        }
        ctx.closePath();
        ctx.setFillStyle(shadow.getColour());
        ctx.setGlobalAlpha(0.2);
        ctx.fill();
    }

    @Override
    public void drawText(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        if (item.getDisplayName() == null || item.getDisplayName().isEmpty()) {
            return;
        }

        TextMetrics metrics = ctx.measureText(item.getDisplayName());
        Shadow shadow = (Shadow) item;
        ctx.setGlobalAlpha(1);
        ctx.setFillStyle(shadow.getColour());
        ctx.setFont(RendererProperties.getFont(RendererProperties.WIDGET_FONT_SIZE * 5));

        ctx.setTextAlign(Context2d.TextAlign.CENTER);
        ctx.setTextBaseline(Context2d.TextBaseline.MIDDLE);

        NodeProperties prop = NodePropertiesFactory.get(
                shadow.getMinX(),
                shadow.getMinY(),
                shadow.getMaxX() - shadow.getMinX(),
                shadow.getMaxY() - shadow.getMinY()
        );
        prop = NodePropertiesFactory.transform(prop, factor, offset);

        double padding = RendererProperties.NODE_TEXT_PADDING * 2;
        padding = (prop.getWidth() - padding * 2 < 0) ? 0 : padding;
        TextRenderer textRenderer = new TextRenderer(RendererProperties.WIDGET_FONT_SIZE * 5, padding);
        double x = prop.getX() + prop.getWidth() / 2d;
        double y = prop.getY() + prop.getHeight() / 2d;
        if (metrics.getWidth() <= prop.getWidth() - 0.5 * padding) {
            textRenderer.drawTextSingleLine(ctx, item.getDisplayName(), CoordinateFactory.get(x, y));
        } else {
            textRenderer.drawTextMultiLine(ctx, item.getDisplayName(), prop);
        }
    }

    @Override
    public void highlight(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {

    }

    @Override
    public Long getHovered(DiagramObject item, Coordinate pos) {
        return null;
    }

    @Override
    public boolean isVisible(DiagramObject item) {
        return true;
    }

    @Override
    public void setColourProperties(AdvancedContext2d ctx, ColourProfileType type) {

    }

    @Override
    public void setTextProperties(AdvancedContext2d ctx, ColourProfileType type) {
//        ctx.setTextAlign(Context2d.TextAlign.CENTER);
//        ctx.setTextBaseline(Context2d.TextBaseline.MIDDLE);
//        ctx.setFont(RendererProperties.getFont(RendererProperties.WIDGET_FONT_SIZE));
//        type.setTextProfile(ctx, DiagramColours.get().PROFILE.getCompartment());
    }
}