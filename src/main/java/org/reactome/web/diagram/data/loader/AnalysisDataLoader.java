package org.reactome.web.diagram.data.loader;

import com.google.gwt.event.shared.EventBus;
import org.reactome.web.analysis.client.AnalysisClient;
import org.reactome.web.analysis.client.AnalysisHandler;
import org.reactome.web.analysis.client.model.*;
import org.reactome.web.diagram.client.DiagramFactory;
import org.reactome.web.diagram.data.AnalysisStatus;
import org.reactome.web.diagram.data.DiagramContent;
import org.reactome.web.diagram.data.graph.model.GraphPathway;
import org.reactome.web.diagram.events.AnalysisResultLoadedEvent;
import org.reactome.web.diagram.events.AnalysisResultRequestedEvent;
import org.reactome.web.diagram.events.DiagramInternalErrorEvent;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is in charge of providing the analysis result for a given token, resource and pathway.
 * For the pair (token, resource) there is a analysisSummary associated that ONLY needs to be retrieved
 * when these change (either one, the other or both).
 * <p/>
 * Related to pathways, please note that there are two components to take into account: (1) the hit
 * entities in the pathway diagram and (2) the entities hit in the encapsulated pathways.
 * <p/>
 * It is right to think that the hit entities in the encapsulated pathways are retrieved with the first
 * query but since the deconstruction of the encapsulated pathways (process nodes) is not contained in
 * the graph original graph (and this is done in purpose to optimise memory usage) the easiest way of
 * knowing the percentage of hit elements in this process nodes is querying the analysis service for the
 * pathway summary.
 *
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class AnalysisDataLoader implements AnalysisHandler.Summary, AnalysisHandler.Summaries, AnalysisHandler.Entities {
    private static AnalysisDataLoader analysisDataLoader;
    protected final static String PREFIX = DiagramFactory.SERVER + "/AnalysisService/token/";

    private EventBus eventBus;

    private AnalysisStatus analysisStatus;
    private DiagramContent diagramContent;
    private AnalysisSummary analysisSummary;
    private ExpressionSummary expressionSummary;

    private PathwayEntities entities;

    AnalysisDataLoader(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public static void initialise(EventBus eventBus) {
        if (analysisDataLoader != null) {
            throw new RuntimeException("Analysis Data Loader has already been initialised. " +
                    "Only one initialisation is permitted per Diagram Viewer instance.");
        }
        analysisDataLoader = new AnalysisDataLoader(eventBus);
    }

    public static AnalysisDataLoader get() {
        if (analysisDataLoader == null) {
            throw new RuntimeException("Analysis Data Loader has not been initialised yet. " +
                    "Please call initialise before using 'get'");
        }
        return analysisDataLoader;
    }

    public void loadAnalysisResult(AnalysisStatus analysisStatus, DiagramContent diagramContent) {
        eventBus.fireEventFromSource(new AnalysisResultRequestedEvent(diagramContent.getDbId()), this);
        this.diagramContent = diagramContent;
        if(analysisStatus!=null && !analysisStatus.equals(this.analysisStatus)){
            this.analysisSummary = null;
            this.expressionSummary = null;
            this.analysisStatus = analysisStatus;
            AnalysisClient.getResultSummary(analysisStatus.getToken(), analysisStatus.getResource(), this);
        } else {
            onResultSummaryLoaded(analysisSummary, expressionSummary, 0);
        }
    }

    @Override
    public void onResultSummaryLoaded(AnalysisSummary summary, ExpressionSummary expressionSummary, long time) {
        this.analysisSummary = summary;
        this.expressionSummary = expressionSummary;
        if (diagramContent.containsOnlyEncapsulatedPathways()) {
            entities = null;
            getPathwaySummaries();
        } else {
            AnalysisClient.getPathwayFoundEntities(analysisStatus.getToken(), analysisStatus.getResource(), diagramContent.getDbId(), this);
        }
    }

    @Override
    public void onResultSummaryNotFound(long time) {
        //Nothing here...
    }

    @Override
    public void onResultSummaryError(AnalysisError error) {
        eventBus.fireEventFromSource(new DiagramInternalErrorEvent(error.getReason()), this);
    }

    @Override
    public void onPathwayEntitiesLoaded(PathwayEntities identifiers, long time) {
        this.entities = identifiers;
        loadPathwaySummaries(time);
    }

    @Override
    public void onPathwayEntitiesNotFound(long time) {
        this.entities = null;
        loadPathwaySummaries(time);
    }

    @Override
    public void onPathwayEntitiesError(AnalysisError error) {
        eventBus.fireEventFromSource(new DiagramInternalErrorEvent(error.getReason()), this);
    }

    private void loadPathwaySummaries(long time) {
        if (diagramContent.containsEncapsulatedPathways()) {
            getPathwaySummaries();
        } else {
            eventBus.fireEventFromSource(new AnalysisResultLoadedEvent(analysisSummary, expressionSummary, entities, null, time), this);
        }
    }

    @Override
    public void onPathwaySummariesLoaded(List<PathwaySummary> pathwaySummaries, long time) {
        eventBus.fireEventFromSource(new AnalysisResultLoadedEvent(analysisSummary, expressionSummary, entities, pathwaySummaries, time), this);
    }

    @Override
    public void onPathwaySummariesNotFound(long time) {
        eventBus.fireEventFromSource(new AnalysisResultLoadedEvent(analysisSummary, expressionSummary, entities, null, time), this);
    }

    @Override
    public void onPathwaySummariesError(AnalysisError error) {
        eventBus.fireEventFromSource(new DiagramInternalErrorEvent(error.getReason()), this);
    }

    @Override
    public void onAnalysisServerException(String message) {
        eventBus.fireEventFromSource(new DiagramInternalErrorEvent(message), this);
    }

    private void getPathwaySummaries(){
        List<String> pathways = new LinkedList<>();
        for (GraphPathway graphPathway : diagramContent.getEncapsulatedPathways()) {
            pathways.add(graphPathway.getDbId().toString());
        }
        AnalysisClient.getPathwaySummaries(analysisStatus.getToken(), analysisStatus.getResource(), pathways, this);
    }
}
