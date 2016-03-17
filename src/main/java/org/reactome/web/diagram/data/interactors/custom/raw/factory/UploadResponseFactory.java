package org.reactome.web.diagram.data.interactors.custom.raw.factory;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import org.reactome.web.diagram.data.interactors.custom.raw.RawToken;
import org.reactome.web.diagram.data.interactors.custom.raw.RawUploadResponse;

/**
 * @author Kostas Sidiropoulos <ksidiro@ebi.ac.uk>
 */
public abstract class UploadResponseFactory {

    @SuppressWarnings("UnusedDeclaration")
    interface ModelAutoBeanFactory extends AutoBeanFactory {
        AutoBean<RawToken> token();
        AutoBean<RawUploadResponse> response();
    }

    public static <T> T getUploadResponseObject(Class<T> cls, String json) throws UploadResponseException {
        try {
            AutoBeanFactory factory = GWT.create(ModelAutoBeanFactory.class);
            AutoBean<T> bean = AutoBeanCodex.decode(factory, cls, json);
            return bean.as();
        } catch (Throwable e) {
            throw new UploadResponseException();
        }
    }
}
