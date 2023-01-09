package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

import static hexlet.code.App.getLogger;


public class UrlController {
    public static Handler mainPage = ctx -> {
        ctx.render("index.html");
    };

    public static Handler getUrls = ctx -> {
        var urls = new QUrl().findList();

        ctx.attribute("urls", urls);
        ctx.render("list.html");
    };

    public static Handler postUrl = ctx -> {
        String urlString = ctx.formParam("url");

        if (urlString == null) {
            getLogger().info("Url is null. Redirect to '/'");
            ctx.redirect("/");
            return;
        }

        var url = new java.net.URL(urlString);
        var parsedUrl = url.getProtocol() + "://" + url.getAuthority();

        try {
            new Url(parsedUrl).save();
            getLogger().info("Added: " + parsedUrl);
            ctx.sessionAttribute("flash", "Страница успешно добавлена");
        } catch (Exception e) {
            getLogger().warn(e.getMessage());
            var message = String.format("URL %s already exists. Nothing changed", parsedUrl);
            getLogger().info(message);
            ctx.sessionAttribute("flash", "Страница уже существует");
        }

        ctx.redirect("/");
    };

    public static Handler showUrl = ctx -> {

        var strId = ctx.pathParam("id");
        Integer id;

        try {
            id = Integer.valueOf(strId);
        } catch (NumberFormatException e) {
            ctx.status(HttpCode.NOT_FOUND);
            return;
        }

        var temp = new QUrl().name.equalTo("some-url").findOneOrEmpty();

        var url = new QUrl().id.equalTo(id).findOneOrEmpty();
        if (url.isEmpty()) {
            getLogger().info(String.format("Url with id=%s not found", id));
            ctx.status(HttpCode.NOT_FOUND);
            return;
        }

        ctx.attribute("url", url.get());
        ctx.render("show.html");
    };
}
