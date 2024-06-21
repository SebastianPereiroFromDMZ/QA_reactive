
create table developers
(
    id         serial
        primary key,
    email      varchar(255),
    first_name varchar(255),
    last_name  varchar(255),
    specialty  varchar(255),
    status     varchar(255)
        constraint developers_status_check
            check ((status)::text = ANY ((ARRAY ['ACTIVE':: character varying, 'DELETED':: character varying])::text[])
) );
