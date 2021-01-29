package handlers;


import java.lang.reflect.Method;
import java.util.List;

public class HandlerMappers {
    private String url;

    private Object targetObj;

    private Method method;

    private List<ParameterMapper> parameterMapperList;

    public HandlerMappers(String url, Object targetObj, Method method) {
        this.url = url;
        this.targetObj = targetObj;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Object getTargetObj() {
        return targetObj;
    }

    public void setTargetObj(Object targetObj) {
        this.targetObj = targetObj;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public List<ParameterMapper> getParameterMapperList() {
        return parameterMapperList;
    }

    public void setParameterMapperList(List<ParameterMapper> parameterMapperList) {
        this.parameterMapperList = parameterMapperList;
    }
}
