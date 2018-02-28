package org.reactome.web.diagram.controls.top.search;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.FlowPanel;
import org.reactome.web.diagram.search.SearchLauncher;
import org.reactome.web.diagram.search.autocomplete.AutoCompletePanel;
import org.reactome.web.diagram.search.suggester.SuggestionPanel;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class SearchPanel extends FlowPanel {

    public SearchPanel(EventBus eventBus) {
        //Setting the legend style
        setStyleName(RESOURCES.getCSS().searchPanel());

        final SearchLauncher launcher = new SearchLauncher(eventBus);
        this.add(launcher);

        AutoCompletePanel autoCompletePanel = new AutoCompletePanel();
        launcher.addSearchPerformedHandler(autoCompletePanel);
        launcher.addAutoCompleteRequestedHandler(autoCompletePanel);
        launcher.addPanelCollapsedHandler(autoCompletePanel);
        launcher.addPanelExpandedHandler(autoCompletePanel);
        launcher.addOptionsCollapsedHandler(autoCompletePanel);
        launcher.addOptionsExpandedHandler(autoCompletePanel);
        this.add(autoCompletePanel);

        SuggestionPanel suggestions = new SuggestionPanel(eventBus);
        // Listen to click events on suggestions and return focus on SearchBox
        suggestions.addClickHandler(event -> launcher.setFocus(true));
        launcher.addSearchPerformedHandler(suggestions);
        launcher.addPanelCollapsedHandler(suggestions);
        launcher.addPanelExpandedHandler(suggestions);
        launcher.addSearchBoxArrowKeysHandler(suggestions);
        launcher.addSuggestionResetHandler(suggestions);
        this.add(suggestions);

//        SelectionInfoPanel infoPanel = new SelectionInfoPanel(eventBus);
//        suggestions.addSuggestionSelectedHandler(infoPanel);
//        launcher.addPanelCollapsedHandler(infoPanel);
//        launcher.addPanelExpandedHandler(infoPanel);
//        this.add(infoPanel);
    }


    public static SearchPanelResources RESOURCES;
    static {
        RESOURCES = GWT.create(SearchPanelResources.class);
        RESOURCES.getCSS().ensureInjected();
    }

    /**
     * A ClientBundle of resources used by this widget.
     */
    public interface SearchPanelResources extends ClientBundle {
        /**
         * The styles used in this widget.
         */
        @Source(SearchPanelCSS.CSS)
        SearchPanelCSS getCSS();
    }

    /**
     * Styles used by this widget.
     */
    @CssResource.ImportedWithPrefix("diagram-SearchPanel")
    public interface SearchPanelCSS extends CssResource {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String CSS = "org/reactome/web/diagram/controls/top/search/SearchPanel.css";

        String searchPanel();
    }
}
