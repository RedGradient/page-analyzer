package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import lombok.Getter;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.Instant;


@Getter
@MappedSuperclass
public abstract class BaseModel extends Model {

    @Id
    protected long id;

    @WhenCreated
    protected Instant createdAt;

    @WhenModified
    protected Instant modifiedAt;

    public long getId() {
        return id;
    }

}
