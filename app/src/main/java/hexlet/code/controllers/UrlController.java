package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static hexlet.code.App.getLogger;

public class UrlController {
    public static Handler mainPage = ctx -> {
        ctx.render("index.html");
    };

    public static Handler getUrls = ctx -> {
        var urls = new QUrl().findList();

        HashMap<String, Map<String, String>> urlsAndChecks = new HashMap<>();
        for (var url : urls) {
            var urlCheck = new QUrlCheck()
                    .url.equalTo(url)
                    .createdAt.desc()
                    .setMaxRows(1)
                    .findOneOrEmpty();
            String statusCode = "";
            String latestCheck = "";
            if (urlCheck.isPresent()) {
                statusCode = String.valueOf(urlCheck.get().getStatusCode());
                latestCheck = urlCheck.get().getCreatedAt().toString();
            }
            urlsAndChecks.put(
                    url.getName(),
                    Map.of("statusCode", statusCode, "latestCheck", latestCheck)
            );
        }

        ctx.attribute("urls", urls);
        ctx.attribute("urlsAndChecks", urlsAndChecks);

        ctx.render("list.html");
    };

    public static Handler postUrls = ctx -> {
        var urlString = ctx.formParamAsClass("url", String.class).get();
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            ctx.redirect("/");
            return;
        }

        var parsedUrl = url.getProtocol() + "://" + url.getAuthority();
        var count = new QUrl().name.equalTo(parsedUrl).findCount();
        if (count > 0) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("alertType", "alert-warning");
        } else {
            new Url(parsedUrl).save();
            getLogger().info("Added: " + parsedUrl);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("alertType", "alert-primary");
        }

        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        try {
            var id = Integer.valueOf(ctx.pathParam("id"));
            var url = new QUrl().id.equalTo(id).findOneOrEmpty().orElseThrow();
            var urlChecks = new QUrlCheck()
                    .id.desc()
                    .url.equalTo(url)
                    .findList();
            ctx.attribute("url", url);
            ctx.attribute("urlChecks", urlChecks);
            ctx.render("show.html");
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            ctx.status(HttpCode.NOT_FOUND);
        } catch (Exception e) {
            ctx.status(HttpCode.NOT_FOUND);
        }
    };

    public static Handler checkUrl = ctx -> {
        try {
            var id = Integer.valueOf(ctx.pathParam("id"));
            var url = new QUrl().id.equalTo(id).findOneOrEmpty().orElseThrow();


            // --- check url ---

            new UrlCheck(
                    200,
                    "Example Title",
                    "Some Text",
                    "h1-h1-h1-h1-h1-h1",
                    url
            ).save();
            // -----------------


            var urlChecks = new QUrlCheck()
                    .url.equalTo(url)
                    .id.desc()
                    .findList();
            ctx.sessionAttribute("urlChecks", urlChecks);
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("alertType", "alert-primary");
            ctx.redirect("/urls/" + id);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.sessionAttribute("flash", "Что-то пошло не так");
            ctx.sessionAttribute("alertType", "alert-warning");
        }
    };
}
