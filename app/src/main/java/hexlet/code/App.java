package hexlet.code;

import hexlet.code.controllers.UrlController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;


public class App {
    private static final int PORT = 5000;
    private static final String DEVELOPMENT = "development";
    private static final String PRODUCTION = "production";
    public static void main(String[] args) {
        var app = getApp();
        app.start(getPort());
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", Integer.toString(PORT));
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", DEVELOPMENT);
    }

    private static boolean isProduction() {
        return getMode().equals(PRODUCTION);
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static Logger getLogger() {
        return LoggerFactory.getLogger(App.class);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", UrlController.mainPage);
        app.get("/urls", UrlController.getUrls);
        app.get("/urls/{id}", UrlController.showUrl);
        app.post("/urls", UrlController.postUrls);
        app.post("/urls/{id}/checks", UrlController.checkUrl);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }
}
