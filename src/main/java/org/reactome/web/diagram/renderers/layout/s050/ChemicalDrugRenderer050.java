package org.reactome.web.diagram.renderers.layout.s050;

import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.data.layout.Node;
import org.reactome.web.diagram.data.layout.NodeProperties;
import org.reactome.web.diagram.data.layout.impl.NodePropertiesFactory;
import org.reactome.web.diagram.renderers.layout.abs.ChemicalDrugAbstractRenderer;
import org.reactome.web.diagram.util.AdvancedContext2d;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ChemicalDrugRenderer050 extends ChemicalDrugAbstractRenderer {

    @Override
    public void draw(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        Node node = (Node) item;
        NodeProperties prop = NodePropertiesFactory.transform(node.getProp(), factor, offset);
        shape(ctx, prop, node.getNeedDashedBorder());
        ctx.stroke();
        ctx.fill();
        ctx.beginPath();
        rx(ctx, prop);
        ctx.stroke();
        ctx.save();
        ctx.setFillStyle("#A00000");
        ctx.fill();
        ctx.restore();
        drawCross(ctx, node, prop);
    }

    @Override
    public boolean isVisible(DiagramObject item) {
        return true;
    }
}
