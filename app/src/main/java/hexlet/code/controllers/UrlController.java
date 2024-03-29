package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

import static hexlet.code.App.getLogger;

public class UrlController {
    public static Handler mainPage = ctx -> ctx.render("index.html");

    public static Handler getUrls = ctx -> {
        var urls = new QUrl().findList();

        HashMap<Url, Optional<UrlCheck>> urlsAndChecks = new HashMap<>();
        for (var url : urls) {
            var urlCheck = new QUrlCheck()
                    .url.equalTo(url)
                    .createdAt.desc()
                    .setMaxRows(1)
                    .findOneOrEmpty();
            urlsAndChecks.put(url, urlCheck);
        }

        ctx.attribute("urlsAndChecks", urlsAndChecks);
        ctx.render("list.html");
    };

    public static Handler addUrl = ctx -> {
        var urlString = ctx.formParamAsClass("url", String.class).get();
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("alertType", "alert-warning");
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
            Integer id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
            var url = new QUrl().id.equalTo(id).findOneOrEmpty().orElseThrow();
            var urlChecks = new QUrlCheck()
                    .id.desc()
                    .url.equalTo(url)
                    .findList();
            ctx.attribute("url", url);
            ctx.attribute("urlChecks", urlChecks);
            ctx.render("show.html");
        } catch (NoSuchElementException e) {
            getLogger().error("Attempt to find URL that does not exist");
            ctx.status(HttpCode.NOT_FOUND);
        } catch (Exception e) {
            getLogger().error(e.getMessage());
            ctx.status(HttpCode.NOT_FOUND);
        }
    };

    public static Handler checkUrl = ctx -> {
        try {
            var id = Integer.valueOf(ctx.pathParam("id"));
            var url = new QUrl().id.equalTo(id).findOneOrEmpty().orElseThrow();

            // --- check url ---
            var response = Unirest.get(url.getName()).asString();
            Document doc = Jsoup.parse(response.getBody());
            var h1 = doc.selectFirst("h1");
            var h1Text = (h1 != null) ? h1.text() : "";
            var meta = doc.selectFirst("meta[name=description]");
            var description = (meta != null) ? meta.attr("content") : "";
            var urlCheck = new UrlCheck(
                    response.getStatus(),
                    doc.title(),
                    h1Text,
                    description,
                    url
            );
            url.getUrlChecks().add(urlCheck);
            url.save();
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
            getLogger().error(e.getMessage());
            ctx.sessionAttribute("flash", "Что-то пошло не так");
            ctx.sessionAttribute("alertType", "alert-warning");
        }
    };
}
