package org.reactome.web.diagram.thumbnail;

import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.thumbnail.render.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class ThumbnailRendererManager {

    private static final ThumbnailRendererManager manager = new ThumbnailRendererManager();

    private Map<String, ThumbnailRenderer> thumbnailMap = new HashMap<String, ThumbnailRenderer>();

    public ThumbnailRendererManager() {
        initialiseRenderers();
    }

    public static ThumbnailRendererManager get() {
        return manager;
    }

    public ThumbnailRenderer getRenderer(DiagramObject item){
        if(item==null) return null;
        return thumbnailMap.get(item.getRenderableClass());
    }

    private void initialiseRenderers(){
//        thumbnailMap.put("OrgGkRenderNote", new NoteThumbnailRenderer());
        thumbnailMap.put("Compartment", new CompartmentThumbnailRenderer());
        thumbnailMap.put("Protein", new ProteinThumbnailRenderer());
        thumbnailMap.put("Chemical", new ChemicalThumbnailRenderer());
        thumbnailMap.put("Reaction", new ReactionThumbnailRenderer());
        thumbnailMap.put("Complex", new ComplexThumbnailRenderer());
        thumbnailMap.put("Entity", new OtherEntityThumbnailRenderer());
        thumbnailMap.put("EntitySet", new SetThumbnailRenderer());
        thumbnailMap.put("ProcessNode", new ProcessNodeThumbnailRenderer());
        thumbnailMap.put("FlowLine", new FlowlineThumbnailRenderer());
        thumbnailMap.put("Gene", new GeneThumbnailRenderer());
        thumbnailMap.put("RNA", new RNAThumbnailRenderer());
//        aux = new LinkThumbnailRenderer();
//        thumbnailMap.put("EntitySetAndMemberLink", aux);
//        thumbnailMap.put("EntitySetAndEntitySetLink", aux);
    }
}
