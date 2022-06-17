create table rf_connect
(
    id           INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) primary key,
    title        varchar(64),
    host         varchar(64),
    port         int,
    username     varchar(64),
    password     varchar(64),
    ssl          varchar(10),
    connect_mode varchar(10),
    ssl_config   varchar(500),
    ssh_config   varchar(500)
)

