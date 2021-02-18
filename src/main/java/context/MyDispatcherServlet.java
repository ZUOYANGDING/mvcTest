package context;


import core.BeanDefinition;
import core.annotations.MyController;
import core.annotations.MyRequestMapping;
import factory.MyDefaultListableBeanFactory;
import handlers.HandlerMappers;
import handlers.ParameterMapper;
import org.apache.commons.lang3.StringUtils;
import utils.MethodUtil;
import utils.ParameterTypeUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.*;
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

    private List<View> viewList = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        /**
         * init IOC
         */
        MyAnnotationApplicationContext context = new MyAnnotationApplicationContext("com.zuoyang");
        this.context = context;
        this.defaultListableBeanFactory = context.defaultListableBeanFactory;

        getTargetController();

        getTargetProperties();

        initViewResolver();
    }
    
    private void initViewResolver() {
        try {
            String path = defaultListableBeanFactory.getProperties("view.rootPath");
            URL url = this.getClass().getClassLoader().getResource(path);
            assert url != null;
            File file = new File(url.toURI());
            doLoadFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadFile(File file) {
        String suffix = defaultListableBeanFactory.getProperties("view.suffix");
        if (file.isDirectory()) {
            File[] files= file.listFiles();
            assert files != null;
            for (File f: files) {
                doLoadFile(f);
            }
        } else {
            if (file.getName().endsWith(suffix)) {
                View view = new View(file.getName(), file);
                viewList.add(view);
            }
        }
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
     * set mapping of each method in each controller class
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
        resp.setHeader("content-type", "text/html;charset=utf-8");
        doService(req, resp);
    }

    /**
     * wrapper the req as ServletRequestAttribute
     * @param req
     * @param resp
     */
    private void doService(HttpServletRequest req, HttpServletResponse resp) {
        String requestUrI = req.getRequestURI();
        try {
            if (!handlerMappersMap.contains(requestUrI)) {
                resp.getWriter().write("404 NOT FOUND");
            }
            HandlerMappers handlerMappers = handlerMappersMap.get(requestUrI);
            doHandlerAdapter(req, resp, handlerMappers);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.getWriter().write("error");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    /**
     * transfer and store the params from request
     * @param req
     * @param resp
     * @param handlerMappers
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IOException
     */
    private void doHandlerAdapter(HttpServletRequest req, HttpServletResponse resp, HandlerMappers handlerMappers) throws
            InvocationTargetException, IllegalAccessException, IOException {
        Map<String, String[]> parameterMap = req.getParameterMap();
        List<ParameterMapper> parameterMappers = handlerMappers.getParameterMapperList();
        Set<String> parameters = parameterMap.keySet();
        Object[] params = new Objects[parameterMap.size()];
        for (int i=0; i<parameterMappers.size(); i++) {
            ParameterMapper pm = parameterMappers.get(i);
            for (String parameter : parameters) {
                if (parameter.equals(pm.getParamName())) {
                    params[i] = ParameterTypeUtils.typeConversion(pm.getParamType(), req.getParameter(parameter));
                }
            }
        }
        invokeTargetMethod(resp, handlerMappers, params.length==0 ? null : params);
    }

    /**
     * run the matching method by reflection
     * @param resp
     * @param handlerMappers
     * @param params
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void invokeTargetMethod(HttpServletResponse resp, HandlerMappers handlerMappers, Object[] params)
            throws InvocationTargetException, IllegalAccessException, IOException {
        ModelAndView modelAndView = new ModelAndView();
        Object ob = handlerMappers.getMethod().invoke(handlerMappers.getTargetObj(), params);

        if (!Objects.isNull(ob)) {
            if (ob instanceof ModelAndView) {
                // response a jsp
                modelAndView = (ModelAndView) ob;
                modelAndView.setHasView(true);
            } else {
                // response as json
                modelAndView.setViewData(ob);
            }
        }
        applyDefaultViewName(modelAndView);
        if (!modelAndView.isHasView()) {
            resp.getWriter().write(modelAndView.getViewData().toString());
        }

        View view = getView(modelAndView.getViewData().toString());
        if (!Objects.isNull(view)) {
            view.replaceTargetInJSP(modelAndView, resp);
        }
    }

    private void applyDefaultViewName(ModelAndView modelAndView) {
        if (modelAndView.isHasView()) {
            String viewName = modelAndView.getViewData().toString();
            String prefix = defaultListableBeanFactory.getProperties("view.prefix");
            String suffix = defaultListableBeanFactory.getProperties("");
            viewName = prefix + viewName + suffix;
            modelAndView.setViewData(viewName);
        }
    }

    private View getView(String viewName) {
        for (View view: viewList) {
            if (view.getViewName().equals(viewName)) {
                return view;
            }
        }
        return null;
    }

    /**
     * get controller's parameters, create mapper for these parameters
     */
    private void getTargetProperties() {
        try {
            Collection<HandlerMappers> values = handlerMappersMap.values();
            for (HandlerMappers handlerMapper : values) {
                Method method = handlerMapper.getMethod();
                List<String> parameterNameList = MethodUtil.getMethodParamNames(handlerMapper.getTargetObj().getClass(), method);
                Class<?>[] parameterTypes = handlerMapper.getMethod().getParameterTypes();
                ArrayList<ParameterMapper> parameterMappers = new ArrayList<>();
                for (int i=0; i<parameterTypes.length; i++) {
                    parameterMappers.add(new ParameterMapper(parameterNameList.get(i), parameterTypes[i]));
                }
                handlerMapper.setParameterMapperList(parameterMappers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
