DROP TABLE IF EXISTS REVIEW_LIKES;
DROP TABLE IF EXISTS REVIEWS;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS directors;
DROP TABLE IF EXISTS films_directors;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS genreNames;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS MPARatings;
DROP TABLE IF EXISTS filmLikes;
DROP TABLE IF EXISTS userFriends;


CREATE TABLE IF NOT EXISTS films
(
    id          LONG PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR NOT NULL,
    description VARCHAR(200),
    releaseDate DATE,
    duration    INTEGER,
    rate        LONG,
    ratingMPAId INTEGER
);

CREATE TABLE IF NOT EXISTS users
(
    id       LONG PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR NOT NULL,
    login    VARCHAR NOT NULL,
    name     VARCHAR,
    birthday DATE
);

CREATE TABLE IF NOT EXISTS genreNames
(
    genreId INTEGER PRIMARY KEY AUTO_INCREMENT,
    genre   VARCHAR
);

CREATE TABLE IF NOT EXISTS genre
(
    filmId  INTEGER,
    genreId INTEGER,
    PRIMARY KEY (filmId, genreId)
);

CREATE TABLE IF NOT EXISTS MPARatings
(
    ratingMPAId INTEGER PRIMARY KEY AUTO_INCREMENT,
    ratingname  VARCHAR
);

CREATE TABLE IF NOT EXISTS filmLikes
(
    filmId INTEGER,
    userId LONG,
    PRIMARY KEY (filmId, userId)
);

CREATE TABLE IF NOT EXISTS userFriends
(
    userId    LONG,
    friendsId LONG,
    status    BOOLEAN,
    PRIMARY KEY (userId, friendsId)
);

CREATE TABLE IF NOT EXISTS directors
(
    id   integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films_directors
(
    film_id     integer REFERENCES films (id) ON DELETE CASCADE,
    director_id integer REFERENCES directors (id) ON DELETE CASCADE,
    UNIQUE (film_id, director_id)
);

create unique index if not exists USER_EMAIL_UINDEX on USERS (email);
create unique index if not exists USER_LOGIN_UINDEX on USERS (login);

create table if not exists REVIEWS
(
    REVIEW_ID   LONG auto_increment,
    FILM_ID     LONG    not null,
    USER_ID     LONG    not null,
    CONTENT     CHARACTER VARYING,
    IS_POSITIVE BOOLEAN not null,
    constraint REVIEWS_PK
        primary key (REVIEW_ID),
    constraint REVIEWS_FILMS_ID_FK
        foreign key (FILM_ID) references FILMS,
    constraint REVIEWS_USERS_ID_FK
        foreign key (USER_ID) references USERS
);

create table if not exists REVIEW_LIKES
(
    REVIEW_ID INTEGER not null,
    USER_ID   INTEGER not null,
    ISLIKE    INTEGER not null,
    constraint REVIEW_LIKES_PK
        primary key (REVIEW_ID),
    constraint "review_likes_REVIEWS_REVIEW_ID_fk"
        foreign key (REVIEW_ID) references REVIEWS
            on update cascade on delete cascade,
    constraint "review_likes_USERS_ID_fk"
        foreign key (USER_ID) references USERS
);
