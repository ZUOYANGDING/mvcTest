package context;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    /**
     * view object
     */
    private Object viewData;

    /**
     * data params
     */
    private Map model = new HashMap();

    /**
     * if a view or not
     */
    private boolean hasView = false;

    public void addObject(Object key, Object value) {
        this.model.put(key, value);
    }

    public Object getViewData() {
        return viewData;
    }

    public void setViewData(Object viewData) {
        this.viewData = viewData;
    }

    public Map getModel() {
        return model;
    }

    public void setModel(Map model) {
        this.model = model;
    }

    public boolean isHasView() {
        return hasView;
    }

    public void setHasView(boolean hasView) {
        this.hasView = hasView;
    }

}
