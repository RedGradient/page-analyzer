package hexlet.code.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Url extends BaseModel {

    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany
    private UrlCheck urlChecks;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public Url() {

    }

    public Url(String name) {
        this.name = name;
    }

}
