package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.time.Instant;

@MappedSuperclass
public abstract class BaseModel extends Model {

    @Id
    protected long id;

    @Version
    protected long version;

    @WhenCreated
    protected Instant createdOn;

    @WhenModified
    protected Instant modifiedOn;

    // getters and setters
}