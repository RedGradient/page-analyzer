package hexlet.code.domain;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
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
}
