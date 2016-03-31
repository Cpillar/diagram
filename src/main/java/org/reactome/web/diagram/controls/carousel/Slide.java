package org.reactome.web.diagram.controls.carousel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 */
public class Slide extends AbsolutePanel {
    private ImageResource imageResource;
    private String caption;
    private boolean isInitialized = false;

    public Slide(ImageResource imageResource, String caption, String textColour) {
        this.imageResource = imageResource;
        this.caption = caption;
        getElement().getStyle().setColor(textColour);
    }

    public void init(int width, int height) {
        if(isInitialized) return;

        Style style = getElement().getStyle();
        style.setWidth(width, Style.Unit.PX);
        style.setHeight(height, Style.Unit.PX);
        style.setFloat(Style.Float.LEFT);

        Image image = new Image(imageResource);

        Label captionLb = new Label(caption);
        captionLb.addStyleName(RESOURCES.getCSS().caption());
        style = captionLb.getElement().getStyle();
        style.setWidth(width, Style.Unit.PX);
        style.setTextAlign(Style.TextAlign.CENTER);


        add(image, 0, 0);
        add(captionLb, 0, (int) (height * 0.68));
        isInitialized = true;
    }


    public static Resources RESOURCES;
    static {
        RESOURCES = GWT.create(Resources.class);
        RESOURCES.getCSS().ensureInjected();
    }

    /**
     * A ClientBundle of resources used by this widget.
     */
    public interface Resources extends ClientBundle {
        /**
         * The styles used in this widget.
         */
        @Source(ResourceCSS.CSS)
        ResourceCSS getCSS();
    }

    /**
     * Styles used by this widget.
     */
    @CssResource.ImportedWithPrefix("diagram-Slide")
    public interface ResourceCSS extends CssResource {
        /**
         * The path to the default CSS styles used by this resource.
         */
        String CSS = "org/reactome/web/diagram/controls/carousel/Slide.css";

        String caption();
    }

}
