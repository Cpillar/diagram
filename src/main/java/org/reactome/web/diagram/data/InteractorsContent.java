package org.reactome.web.diagram.data;

import org.reactome.web.diagram.data.graph.model.GraphObject;
import org.reactome.web.diagram.data.graph.model.GraphPhysicalEntity;
import org.reactome.web.diagram.data.interactors.common.InteractorsSummary;
import org.reactome.web.diagram.data.interactors.model.DiagramInteractor;
import org.reactome.web.diagram.data.interactors.model.InteractorEntity;
import org.reactome.web.diagram.data.interactors.model.InteractorLink;
import org.reactome.web.diagram.data.interactors.model.InteractorSearchResult;
import org.reactome.web.diagram.data.interactors.raw.RawInteractor;
import org.reactome.web.diagram.data.layout.Coordinate;
import org.reactome.web.diagram.data.layout.DiagramObject;
import org.reactome.web.diagram.data.layout.Node;
import org.reactome.web.diagram.data.layout.SummaryItem;
import org.reactome.web.diagram.util.MapSet;
import org.reactome.web.pwp.model.util.LruCache;
import uk.ac.ebi.pwp.structures.quadtree.client.Box;
import uk.ac.ebi.pwp.structures.quadtree.client.QuadTree;

import java.util.*;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class InteractorsContent {



    static final int INTERACTORS_RESOURCE_CACHE_SIZE = 5;
    static final int INTERACTORS_FRAME_OFFSET = 1000;

    //The number of elements for every QuadTree quadrant node
    static final int NUMBER_OF_ELEMENTS = 25;
    //Quadrant minimum area (width * height):             180
    //  Right now an area of 180 x 80 = 14400 would     [--][--] 8
    //  host 4 entities of 90x40 each                   [--][--] 0
    //  An area of 60,000 includes 25 entities
    static final int MIN_AREA = 90000;

    static final double DEFAULT_SCORE = 0.45;

    static Map<String, Double> interactorsThreshold = new HashMap<>();

    private Map<String, Map<DiagramInteractor.Type, String>> urlsPerResource = new HashMap<>(); //resource -> (interactor/interaction) -> URL
    private Map<String, MapSet<String, RawInteractor>> rawInteractorsCache; //resource -> node acc -> raw interactors

    private MapSet<String, InteractorsSummary> interactorsSummaryMap; //resource -> InteractorsSummary
    private Map<String, Map<String, InteractorEntity>> interactorsCache; //resource -> acc -> interactors
    private Map<String, MapSet<Node, InteractorLink>> interactionsPerNode; //resource -> layout node -> interaction

    private LruCache<String, QuadTree<DiagramInteractor>> interactorsTreeCache;
    private double minX, minY, maxX, maxY;

    public InteractorsContent(double minX, double minY, double maxX, double maxY) {
        this.rawInteractorsCache = new HashMap<>();
        this.interactorsSummaryMap = new MapSet<>();
        this.interactorsCache = new HashMap<>();
        this.interactionsPerNode = new HashMap<>();

        this.interactorsTreeCache = new LruCache<>(INTERACTORS_RESOURCE_CACHE_SIZE);
        this.minX = minX - INTERACTORS_FRAME_OFFSET;
        this.maxX = maxX + INTERACTORS_FRAME_OFFSET;
        this.minY = minY - INTERACTORS_FRAME_OFFSET;
        this.maxY = maxY + INTERACTORS_FRAME_OFFSET;
    }

    public void add(String resource, DiagramInteractor.Type type, String url) {
        Map<DiagramInteractor.Type, String> urls = urlsPerResource.get(resource);
        if (urls == null) {
            urls = new HashMap<>();
            urlsPerResource.put(resource, urls);
        }
        urls.put(type, url);
    }

    public void cache(String resource, String acc, RawInteractor rawInteractor) {
        getOrCreateRawInteractorCachedResource(resource).add(acc, rawInteractor);
    }

    public MapSet<String, RawInteractor> getOrCreateRawInteractorCachedResource(String resource) {
        MapSet<String, RawInteractor> map = rawInteractorsCache.get(resource);
        if (map == null) {
            map = new MapSet<>();
            rawInteractorsCache.put(resource, map);
        }
        return map;
    }

    public void cache(String resource, InteractorEntity interactor) {
        Map<String, InteractorEntity> map = interactorsCache.get(resource);
        if (map == null) {
            map = new HashMap<>();
            interactorsCache.put(resource, map);
        }
        map.put(interactor.getAccession(), interactor);
    }

    public void cache(String resource, Node node, InteractorLink link) {
        MapSet<Node, InteractorLink> cache = interactionsPerNode.get(resource);
        if (cache == null) {
            cache = new MapSet<>();
            interactionsPerNode.put(resource, cache);
        }
        cache.add(node, link);
    }

    public void cacheInteractors(String resource, String acc, Integer number, MapSet<String, GraphObject> identifierMap) {
        if (number == 0) return;
        Set<GraphObject> elements = identifierMap.getElements(acc);
        if (elements != null) {
            for (GraphObject graphObject : elements) {
                if (graphObject instanceof GraphPhysicalEntity) {
                    GraphPhysicalEntity pe = (GraphPhysicalEntity) graphObject;
                    for (DiagramObject diagramObject : pe.getDiagramObjects()) {
                        InteractorsSummary summary = new InteractorsSummary(acc, diagramObject.getId(), number);
                        interactorsSummaryMap.add(resource, summary);
                        Node node = (Node) diagramObject;
                        node.getInteractorsSummary().setNumber(summary.getNumber());
                        node.getInteractorsSummary().setPressed(summary.isPressed());
                        //The changes need to be updated in the cache, so when restoring, the pressed ones are known
                        node.setDiagramEntityInteractorsSummary(summary);
                    }
                }
            }
        }
    }

    //This method is not checking whether the interactors where previously put in place since
    //when it is called, the interactors have probably been retrieved "again" from the server
    //IMPORTANT: To avoid loading data that already exists -> CHECK BEFORE RETRIEVING :)
    public void addToView(String resource, DiagramInteractor interactor) {
        QuadTree<DiagramInteractor> tree = interactorsTreeCache.get(resource);
        if (tree == null) {
            tree = new QuadTree<>(minX, minY, maxX, maxY, NUMBER_OF_ELEMENTS, MIN_AREA);
            interactorsTreeCache.put(resource, tree);
        }
        tree.add(interactor);
    }

    public void updateView(String resource, DiagramInteractor interactor) {
        QuadTree<DiagramInteractor> tree = interactorsTreeCache.get(resource);
        if (tree != null) {
            tree.remove(interactor);
            tree.add(interactor);
        }
    }

    public void removeFromView(String resource, DiagramInteractor interactor) {
        QuadTree<DiagramInteractor> tree = interactorsTreeCache.get(resource);
        if (tree != null) tree.remove(interactor);
    }

    public void clearInteractors(String resource){
        Map<String, InteractorEntity> entities = interactorsCache.get(resource);
        if(entities!=null) {
            for (InteractorEntity entity : entities.values()) {
                entity.getLinks().clear();
            }
        }
        QuadTree<DiagramInteractor> tree = interactorsTreeCache.get(resource);
        if (tree != null) {
            tree.clear();
        }
        interactionsPerNode.remove(resource);
    }
    
    public void removeInteractorLink(String resource, InteractorLink link){
        MapSet<Node, InteractorLink> cache = interactionsPerNode.get(resource);
        if (cache != null) {
            Set<InteractorLink> links = cache.getElements(link.getNodeFrom());
            if (links != null) links.remove(link);
        }
    }

    public String getURL(String resource, DiagramInteractor.Type type){
        Map<DiagramInteractor.Type, String> urls = urlsPerResource.get(resource);
        if(urls==null) return null;
        return urls.get(type);
    }

    public String getURL(String resource, DiagramInteractor interactor){
        Map<DiagramInteractor.Type, String> urls = urlsPerResource.get(resource);
        if(urls==null) return null;
        if(interactor instanceof InteractorLink){
            String url = urls.get(DiagramInteractor.Type.INTERACTION);
            List<String> cluster = ((InteractorLink) interactor).getCluster();
            if (url != null && cluster != null && !cluster.isEmpty()) {
                String id = cluster.toString().replaceAll(", ", "%20OR%20").replace("[","").replace("]","");
                return url.replaceAll("##ID##", id);
            }
        } else {
            InteractorEntity entity = (InteractorEntity) interactor;
            String url = entity.isChemical() ? urls.get(DiagramInteractor.Type.CHEMICAL) : urls.get(DiagramInteractor.Type.PROTEIN);
            if(url!=null){
                return url.replaceAll("##ID##", entity.getAccession());
            }
        }
        return null;
    }

    public String getURL(String resource, RawInteractor rawInteractor, DiagramInteractor.Type type){
        Map<DiagramInteractor.Type, String> urls = urlsPerResource.get(resource);
        if (urls == null) return null;
        String url;
        switch (type){
            case INTERACTION:
                url = urls.get(DiagramInteractor.Type.INTERACTION);
                List<String> cluster = rawInteractor.getCluster();
                if (url != null && cluster != null && !cluster.isEmpty()) {
                    String id = rawInteractor.getCluster().toString().replaceAll(", ", "%20OR%20").replace("[","").replace("]","");
                    return url.replaceAll("##ID##", id);
                }
                break;
            default:
                url = urls.get(type);
                if(url!=null){
                    return url.replaceAll("##ID##", rawInteractor.getAcc());
                }
        }
        return null;
    }

    public List<InteractorLink> getInteractorLinks(String resource, Node node) {
        MapSet<Node, InteractorLink> cache = interactionsPerNode.get(resource);
        if (cache != null) {
            Set<InteractorLink> set = cache.getElements(node);
            if (set != null) {
                List<InteractorLink> rtn = new ArrayList<>(set);
                Collections.sort(rtn);
                return rtn;
            }
        }
        return new LinkedList<>();
    }

    public InteractorEntity getInteractorEntity(String resource, String acc) {
        Map<String, InteractorEntity> cache = interactorsCache.get(resource);
        if (cache != null) return cache.get(acc);
        return null;
    }


    public Collection<DiagramInteractor> getHoveredTarget(String resource, Coordinate p, double factor) {
        double f = 1 / factor;
        return getVisibleInteractors(resource, new Box(p.getX() - f, p.getY() - f, p.getX() + f, p.getY() + f));
    }

    //We keep this cache to avoid creating it every time
    private Map<String, List<InteractorSearchResult>> interactorsSearchItemsPerResource = new HashMap<>();
    public List<InteractorSearchResult> getInteractorSearchResult(String resource, DiagramContent content) {
        // IMPORTANT: First check whether the rawInteractors have been loaded
        // If not then there is no point in searching for a term and caching the results
        MapSet<String, RawInteractor> map = rawInteractorsCache.get(resource);
        if (map == null || map.isEmpty()) return new ArrayList<>();

        List<InteractorSearchResult> rtn = interactorsSearchItemsPerResource.get(resource);
        if (rtn != null) return rtn;
        rtn = new ArrayList<>();
        Map<String, InteractorSearchResult> cache = new HashMap<>();
        for (String diagramAcc : map.keySet()) {
            for (RawInteractor rawInteractor : map.getElements(diagramAcc)) {
                String accession = rawInteractor.getAcc();

                // If the interactor is in the diagram we do not
                // present it as a separate result
                if (!map.keySet().contains(accession)) {
                    InteractorSearchResult result = cache.get(accession);
                    if (result == null) {
                        result = new InteractorSearchResult(resource, accession, rawInteractor.getAlias());
                        cache.put(accession, result);
                        rtn.add(result);
                    }
                    result.addInteractsWith(rawInteractor.getId(), getInteractsWith(diagramAcc, content));
                    result.addInteraction(rawInteractor);
                }
            }
        }
        interactorsSearchItemsPerResource.put(resource, rtn);
        return rtn;
    }

    private Set<GraphObject> getInteractsWith(String diagramAcc, DiagramContent content) {
        Set<GraphObject> aux = content.getIdentifierMap().getElements(diagramAcc);
        if (aux != null) return aux;
        return new HashSet<>();
    }

    public List<RawInteractor> getRawInteractors(String resource, String acc) {
        List<RawInteractor> rtn = new ArrayList<>();
        MapSet<String, RawInteractor> map = rawInteractorsCache.get(resource);
        if (map != null) {
            Set<RawInteractor> set = map.getElements(acc);
            if (set != null) {
                rtn.addAll(set);
                Collections.sort(rtn, new Comparator<RawInteractor>() {
                    @Override
                    public int compare(RawInteractor o1, RawInteractor o2) {
                        int c = Double.compare(o2.getScore(), o1.getScore());
                        if (c == 0) return o1.getAcc().compareTo(o2.getAcc());
                        return c;
                    }
                });
            }
        }
        return rtn;
    }

    public MapSet<String, RawInteractor> getRawInteractorsPerResource(String resource) {
        return rawInteractorsCache.get(resource);
    }

    public boolean isResourceLoaded(String resource) {
        return rawInteractorsCache.keySet().contains(resource);
    }

    public Collection<DiagramInteractor> getVisibleInteractors(String resource, Box visibleArea) {
        Set<DiagramInteractor> rtn = new HashSet<>();
        if (resource != null) {
            QuadTree<DiagramInteractor> quadTree = interactorsTreeCache.get(resource);
            if (quadTree != null) rtn = quadTree.getItems(visibleArea);
        }
        return rtn;
    }

    public boolean isInteractorResourceCached(String resource) {
        return interactorsSummaryMap.keySet().contains(resource);
    }

    public void resetBurstInteractors(String resource, Collection<DiagramObject> diagramObjects) {
        Set<InteractorsSummary> summaries = interactorsSummaryMap.getElements(resource);
        if (summaries != null) {
            for (InteractorsSummary summary : summaries) {
                summary.setPressed(false);
            }
        }
        for (DiagramObject diagramObject : diagramObjects) {
            if (diagramObject instanceof Node) {
                Node node = (Node) diagramObject;
                SummaryItem summaryItem = node.getInteractorsSummary();
                if (summaryItem != null) {
                    summaryItem.setPressed(null);
                }
            }
        }
    }

    public void restoreInteractorsSummary(String resource, DiagramContent content) {
        Set<InteractorsSummary> items = interactorsSummaryMap.getElements(resource);
        if (items == null) return;
        for (InteractorsSummary summary : items) {
            Node node = (Node) content.getDiagramObject(summary.getDiagramId());
            node.getInteractorsSummary().setNumber(summary.getNumber());
            node.getInteractorsSummary().setPressed(summary.isPressed());
            //The changes need to be updated in the cache, so when restoring, the pressed ones are known
            node.setDiagramEntityInteractorsSummary(summary);
        }
    }

    public static double getInteractorsThreshold(String resource) {
        Double threshold = interactorsThreshold.get(resource);
        if (threshold == null) {
            threshold = DEFAULT_SCORE;
            setInteractorsThreshold(resource, threshold);
        }
        return threshold;
    }

    public static void setInteractorsThreshold(String resource, double threshold) {
        interactorsThreshold.put(resource, threshold);
    }
}