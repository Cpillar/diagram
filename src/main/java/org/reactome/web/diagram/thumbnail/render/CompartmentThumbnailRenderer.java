package org.reactome.web.diagram.thumbnail.render;

import org.reactome.web.diagram.data.layout.Compartment;
import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.data.layout.impl.CoordinateFactory;
import org.reactome.web.diagram.profiles.diagram.DiagramColours;
import org.reactome.web.diagram.profiles.diagram.model.DiagramProfile;
import org.reactome.web.diagram.util.AdvancedContext2d;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class CompartmentThumbnailRenderer extends AbstractThumbnailRenderer {
    @Override
    public void draw(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        Compartment node = (Compartment) item;

        Coordinate corner = CoordinateFactory.get(node.getMinX(), node.getMinY()).add(offset).multiply(factor);
        double w = (node.getMaxX() - node.getMinX()) * factor;
        double h = (node.getMaxY() - node.getMinY()) * factor;

        ctx.save();
        DiagramProfile profile = DiagramColours.get().PROFILE;
        ctx.setStrokeStyle(profile.getCompartment().getStroke());
        ctx.setFillStyle(profile.getCompartment().getFill());
        ctx.fillRect(corner.getX(), corner.getY(), w, h);
        ctx.restore();
    }
}
