package org.reactome.web.diagram.search.autocomplete;

import com.google.gwt.http.client.*;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.UriUtils;
import org.reactome.web.diagram.client.DiagramFactory;
import org.reactome.web.diagram.util.Console;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 */
public class AutoCompleteResultsFactory {

    private static final String SEARCH = "/content/getTags?tagName=##tag##";
    private static Request request;

    public interface AutoCompleteResultsHandler {
        void onAutoCompleteSearchResult(List<AutoCompleteResult> results);
        void onAutoCompleteError();
    }

    public static void searchForTag(String tag, AutoCompleteResultsHandler handler) {

        String url = SEARCH.replace("##tag##", UriUtils.encode(tag));

        if (request != null && request.isPending()) request.cancel();

        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, DiagramFactory.SERVER + url);
        requestBuilder.setHeader("Accept", "application/json");
        try {
            request = requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    switch (response.getStatusCode()){
                        case Response.SC_OK:
                            handler.onAutoCompleteSearchResult(getResults(tag, toList(response.getText())));
                            break;
                        default:
                            handler.onAutoCompleteError();
                    }
                }
                @Override
                public void onError(Request request, Throwable exception) {
                    Console.error(exception.getCause());
                    handler.onAutoCompleteError();
                }
            });
        }catch (RequestException ex) {
            handler.onAutoCompleteError();
        }
    }

    private static List<AutoCompleteResult> getResults(String tag, List<String> stringList) {
        List<AutoCompleteResult> rtn = new ArrayList<>();

        for (String result: stringList) {
            rtn.add(new AutoCompleteResult(tag, result));
        }

        return rtn;
    }

    private static List<String> toList(String jsonStr) {
        List<String> rtn = new ArrayList<>();
        if (jsonStr == null || jsonStr.isEmpty() ) return rtn;

        JSONValue parsed = JSONParser.parseStrict(jsonStr);
        JSONArray jsonArray = parsed.isArray();

        if (jsonArray == null) return rtn;


        for (int i = 0; i < jsonArray.size(); i++) {
            JSONValue jsonValue = jsonArray.get(i);
            JSONString jsonString = jsonValue.isString();
            String stringValue = (jsonString == null) ? jsonValue.toString() : jsonString.stringValue();
            rtn.add(stringValue);
        }

        return rtn;
    }

}
