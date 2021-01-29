package context;


import core.BeanDefinition;
import core.annotations.MyController;
import core.annotations.MyRequestMapping;
import factory.MyDefaultListableBeanFactory;
import handlers.HandlerMappers;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(name = "dispatcherServlet")
public class MyDispatcherServlet extends HttpServlet {
    private MyAnnotationApplicationContext context;
    private MyDefaultListableBeanFactory defaultListableBeanFactory;

    /**
     * store the mapping between requests and controllers
     */
    private ConcurrentHashMap<String, HandlerMappers> handlerMappersMap =
            new ConcurrentHashMap<String, HandlerMappers>();

    @Override
    public void init() throws ServletException {
        /**
         * init IOC
         */
        MyAnnotationApplicationContext context = new MyAnnotationApplicationContext("com.zuoyang");
        this.context = context;
        this.defaultListableBeanFactory = context.defaultListableBeanFactory;

        getTargetController();
    }

    /**
     * get all mapping for controllers
     */
    private void getTargetController() {
        Map<String, BeanDefinition> beanDefinitionMap = context.defaultListableBeanFactory.beanDefinitionMap;
        Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values();
        beanDefinitions.forEach(beanDefinition -> {
            if (beanDefinition.getBeanClass().isAnnotationPresent(MyController.class)) {
                getTargetHandlerMappers(beanDefinition);
            }
        });
    }

    /**
     * set mapping between each method in each controller class
     * @param beanDefinition
     */
    private void getTargetHandlerMappers(BeanDefinition beanDefinition) {
        String controllerUrl = "";
        Class<?> beanClass = beanDefinition.getBeanClass();
        if (beanClass.isAnnotationPresent(MyRequestMapping.class) &&
                StringUtils.isNotEmpty(beanClass.getAnnotation(MyRequestMapping.class).value())) {
            String value = beanClass.getAnnotation(MyRequestMapping.class).value();
            if (value.startsWith("/"))  {
                controllerUrl += value;
            } else {
                controllerUrl = controllerUrl + "/" + value;
            }
        }

        String methodUrl = "";
        Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(MyRequestMapping.class)) {
                String value = method.getAnnotation(MyRequestMapping.class).value();
                if (value.startsWith("/")) {
                    methodUrl += value;
                } else {
                    methodUrl = methodUrl + "/" + value;
                }
            }

            if (StringUtils.isNotEmpty(methodUrl)) {
                if (handlerMappersMap.contains(methodUrl)) {
                    throw new RuntimeException(beanClass + "'s current request mapping is duplicated to " +
                            handlerMappersMap.get(methodUrl).getMethod());
                }
                handlerMappersMap.put(methodUrl,
                        new HandlerMappers(methodUrl, context.getBean(beanDefinition.getBeanName()), method));
            } else {
                throw new RuntimeException("There is no request mapping for " + method);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) {
        
    }
}
