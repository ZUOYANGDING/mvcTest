package context;


import core.BeanDefinition;
import core.annotations.MyController;
import factory.MyDefaultListableBeanFactory;
import handlers.HandlerMappers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
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

        /**
         * get all mapping between about controllers
         */
        getTargetController();
    }

    private void getTargetController() {
        Map<String, BeanDefinition> beanDefinitionMap = context.defaultListableBeanFactory.beanDefinitionMap;
        Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values();
        beanDefinitions.forEach(beanDefinition -> {
            if (beanDefinition.getBeanClass().isAnnotationPresent(MyController.class)) {
                getTargetHandlerMappers(beanDefinition);
            }
        });
    }

    private void getTargetHandlerMappers(BeanDefinition beanDefinition) {

    }
}
