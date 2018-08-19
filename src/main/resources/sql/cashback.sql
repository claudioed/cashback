CREATE TABLE cashback (
    id      varchar(200) primary key,
    type   varchar(10) NOT NULL,
    user_id      varchar(40) NOT NULL,
    user_email      varchar(100) NOT NULL,
    store      varchar(400) NOT NULL,
    order_id     varchar(40) NOT NULL,
    order_total numeric(10,2) NOT NULL,
    points_earned numeric(10,2) NOT NULL
);