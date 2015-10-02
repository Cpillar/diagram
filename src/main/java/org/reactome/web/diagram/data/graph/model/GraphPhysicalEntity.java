package org.reactome.web.diagram.data.graph.model;

import org.reactome.web.diagram.data.graph.raw.EntityNode;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public abstract class GraphPhysicalEntity extends GraphObject {

    protected String identifier;
    protected List<String> geneNames = new ArrayList<>();
    protected String sampleIdentifier;
    protected List<GraphPhysicalEntity> children = new ArrayList<>();
            
    private List<GraphReactionLikeEvent> isInputIn = new ArrayList<>();
    private List<GraphReactionLikeEvent> isOutputIn = new ArrayList<>();
    private List<GraphReactionLikeEvent> isCatalystIn = new ArrayList<>();
    private List<GraphReactionLikeEvent> isActivatorIn = new ArrayList<>();
    private List<GraphReactionLikeEvent> isInhibitorIn = new ArrayList<>();

    public GraphPhysicalEntity(EntityNode node) {
        super(node);
        this.identifier = node.getIdentifier();
        if(node.getGeneNames()!=null) this.geneNames = node.getGeneNames();
    }

    public boolean addParent(List<GraphPhysicalEntity> parents){
        return this.parents.addAll(parents);
    }

    public boolean addChildren(List<GraphPhysicalEntity> children){
        return this.children.addAll(children);
    }

    public boolean addInputIn(GraphReactionLikeEvent rle){
        return isInputIn.add(rle);
    }

    public boolean addOutputIn(GraphReactionLikeEvent rle){
        return isOutputIn.add(rle);
    }

    public boolean addCatalystIn(GraphReactionLikeEvent rle){
        return isCatalystIn.add(rle);
    }

    public boolean addActivatorIn(GraphReactionLikeEvent rle){
        return isActivatorIn.add(rle);
    }

    public boolean addInhibitorIn(GraphReactionLikeEvent rle){
        return isInhibitorIn.add(rle);
    }


    public boolean isHit() {
        return sampleIdentifier!=null;
    }

    public void setIsHit(String sampleIdentifier, List<Double> expression) {
        this.sampleIdentifier = sampleIdentifier;
        this.expression = expression;
    }

    public void resetHit(){
        this.sampleIdentifier = null;
        this.expression = null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<String> getGeneNames() {
        return geneNames;
    }

    public Set<GraphPhysicalEntity> getParticipants(){
        Set<GraphPhysicalEntity> rtn = new HashSet<>();
        rtn.add(this);
        return rtn;
    }

    public Map<String, Double> getParticipantsExpression(int column){
        Map<String, Double> rtn = new HashMap<>();
        if(this.isHit()) {
            rtn.put(sampleIdentifier, this.getExpression(column));
        }
        return rtn;
    }

    public Set<GraphPhysicalEntity> getHitParticipants(){
        Set<GraphPhysicalEntity> rtn = new HashSet<>();
        if(this.isHit()) {
            rtn.add(this);
        }
        return rtn;
    }

    public Set<GraphPhysicalEntity> getParentLocations() {
        Set<GraphPhysicalEntity> rtn = new HashSet<>();
        for (GraphPhysicalEntity parent : parents) {
            rtn.addAll(parent.getParentDiagramIds());
        }
        return rtn;
    }

    private Set<GraphPhysicalEntity> getParentDiagramIds(){
        Set<GraphPhysicalEntity> rtn = new HashSet<>();
        if(!getDiagramObjects().isEmpty()){
            rtn.add(this);
        }
        for (GraphPhysicalEntity parent : parents) {
            rtn.addAll(parent.getParentDiagramIds());
        }
        return rtn;
    }

    public Set<GraphReactionLikeEvent> participatesIn(){
        Set<GraphReactionLikeEvent> rtn = new HashSet<>();
        for (GraphReactionLikeEvent rle : isInputIn) {
            rtn.add(rle);
        }
        for (GraphReactionLikeEvent rle : isOutputIn) {
            rtn.add(rle);
        }
        for (GraphReactionLikeEvent rle : isCatalystIn) {
            rtn.add(rle);
        }
        for (GraphReactionLikeEvent rle : isActivatorIn) {
            rtn.add(rle);
        }
        for (GraphReactionLikeEvent rle : isInhibitorIn) {
            rtn .add(rle);
        }
        return rtn;
    }

    @Override
    protected String getSecondaryDisplayName() {
        String rtn = super.getSecondaryDisplayName();
        if(identifier!=null) {
            rtn += (rtn.isEmpty() ? "" : " ") + identifier ;
        }
        if(geneNames!=null){
            for (String geneName : geneNames) {
                rtn +=  (rtn.isEmpty() ? "" : " ") + geneName;
            }
        }
        return rtn;
    }

    public static Comparator<GraphPhysicalEntity> getIdentifierComparator(){
        return new Comparator<GraphPhysicalEntity>() {
            @Override
            public int compare(GraphPhysicalEntity o1, GraphPhysicalEntity o2) {
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        };
    }
}
