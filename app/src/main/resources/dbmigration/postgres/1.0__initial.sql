-- apply changes
create table url (
  id                            bigint generated by default as identity not null,
  created_at                    timestamptz not null,
  name                          varchar(255) not null,
  constraint uq_url_name unique (name),
  constraint pk_url primary key (id)
);

create table url_check (
  id                            bigint generated by default as identity not null,
  status_code                   integer not null,
  url_id                        bigint,
  created_at                    timestamptz not null,
  title                         varchar(255) not null,
  h1                            varchar(255) not null,
  description                   text not null,
  constraint pk_url_check primary key (id)
);

-- foreign keys and indices
create index ix_url_check_url_id on url_check (url_id);
alter table url_check add constraint fk_url_check_url_id foreign key (url_id) references url (id) on delete restrict on update restrict;

