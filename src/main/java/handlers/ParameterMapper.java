package handlers;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ParameterMapper {
    /**
     * parameter name
     */
    private String paramName;

    /**
     * parameter type
     */
    private Class<?> paramType;

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public Class<?> getParamType() {
        return paramType;
    }

    public void setParamType(Class<?> paramType) {
        this.paramType = paramType;
    }
}
