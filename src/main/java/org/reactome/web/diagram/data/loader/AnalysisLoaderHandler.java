package org.reactome.web.diagram.data.loader;

import org.reactome.web.diagram.data.analysis.AnalysisSummary;
import org.reactome.web.diagram.data.analysis.ExpressionSummary;
import org.reactome.web.diagram.data.analysis.PathwayIdentifiers;
import org.reactome.web.diagram.data.analysis.PathwaySummary;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
interface AnalysisLoaderHandler {

    void onResultSummaryLoaded(AnalysisSummary summary, ExpressionSummary expressionSummary, long time);

    void onResultSummaryNotFound(long time);

    void onResultSummaryError(Throwable e);

    void onPathwayIdentifiersLoaded(PathwayIdentifiers identifiers, long time);

    void onPathwayIdentifiersNotFound(long time);

    void onPathwayIdentifiersError(Throwable e);

    void onPathwaySummariesLoaded(List<PathwaySummary> pathwaySummaries, long time);

    void onPathwaySummariesNotFound(long time);

    void onPathwaySummariesError(Throwable e);

}
