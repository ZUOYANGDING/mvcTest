package context;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class View {
    /**
     * view name
     */
    private String viewName;

    /**
     * view file *.jsp
     */
    private File file;

    public View(String viewName, File file) {
        this.viewName = viewName;
        this.file = file;
    }

    /**
     * get ${} part in jsp file
     */
    Pattern pattern=Pattern.compile("[.\\w]*[${]([\\w]+)[}]");

    public void replaceTargetInJSP(ModelAndView modelAndView, HttpServletResponse resp) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer result = new StringBuffer();

        while (reader.read() > 0) {
            String line = reader.readLine();
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String key = matcher.group(1);
                if (modelAndView.getModel().containsKey(key)) {
                    line = line.replace("${" + key + "}", modelAndView.getModel().get(key).toString());
                }
            }
            result.append(line);
        }

        resp.getWriter().write(result.toString());
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

//    public static void main(String[] args)  {
//        String testString = "${}akldjfkl${}";
//        Pattern pattern = Pattern.compile("[${}]+");
//        Matcher matcher = pattern.matcher(testString);
//        while (matcher.find()) {
//            System.out.println(matcher.group());
//            System.out.println(matcher.groupCount());
//        }
//    }
}
