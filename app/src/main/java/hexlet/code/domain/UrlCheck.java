package hexlet.code.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
@Table(name = "url_check")
public class UrlCheck extends BaseModel {
    @Column(nullable = false)
    private int statusCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String h1;

    @Column(nullable = false)
    @Lob
    private String description;

    @ManyToOne
    private Url url;

    public UrlCheck() {

    }

    public UrlCheck(int statusCode, String title, String h1, String description, Url url) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
    }
}
