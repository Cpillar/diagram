package org.reactome.web.diagram.thumbnail.render;

import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.data.layout.Node;
import org.reactome.web.diagram.profiles.diagram.DiagramColours;
import org.reactome.web.diagram.profiles.diagram.model.DiagramProfile;
import org.reactome.web.diagram.util.AdvancedContext2d;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 */
public class RNAThumbnailRenderer extends AbstractThumbnailRenderer {
    @Override
    public void draw(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        ctx.save();
        DiagramProfile profile = DiagramColours.get().PROFILE;
        ctx.setStrokeStyle(profile.getRna().getStroke());
        ctx.setFillStyle(profile.getRna().getFill());
        drawNode(ctx, (Node) item, factor, offset);
        ctx.restore();
    }
}
