package hexlet.code.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;


@Getter
@Entity
@Table(name = "url")
public class Url extends BaseModel {

    @Setter
    @Column(unique = true, nullable = false)
    private String name;

    @OneToMany
    private List<UrlCheck> urlChecks;

    public Url() {

    }

    public Url(String name) {
        this.name = name;
    }

}
