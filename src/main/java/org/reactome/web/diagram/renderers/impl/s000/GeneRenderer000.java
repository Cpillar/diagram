package org.reactome.web.diagram.renderers.impl.s000;

import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.renderers.impl.abs.GeneAbstractRenderer;
import org.reactome.web.diagram.util.AdvancedContext2d;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 */
public class GeneRenderer000 extends GeneAbstractRenderer {
    @Override
    public void drawText(AdvancedContext2d ctx, DiagramObject item, Double factor, Coordinate offset) {
        //No text at this level
    }
}
