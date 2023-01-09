package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.NotNull;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Entity
public class Url extends Model {
    @Id
    private long id;

    @Column(unique = true, nullable = false)
    private String name;

    @WhenCreated
    private LocalDateTime createdAt;

    public Url() {
    }

    public String getName() {
        return name;
    }
    public void setName(String value) {
        this.name = value;
    }

    public Url(String name) {
        this.name = name;
    }

}
