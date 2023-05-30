DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS filmGenre;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS MPA;
DROP TABLE IF EXISTS filmLikes;
DROP TABLE IF EXISTS friends;

CREATE TABLE IF NOT EXISTS films(
        id LONG PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR NOT NULL,
        description VARCHAR(200),
        releaseDate DATE,
        duration INTEGER,
        rate LONG,
        rateId INTEGER
);

CREATE TABLE IF NOT EXISTS users(
        id LONG PRIMARY KEY AUTO_INCREMENT,
        email VARCHAR NOT NULL,
        login VARCHAR NOT NULL,
        name VARCHAR,
        birthday DATE
);

CREATE TABLE IF NOT EXISTS genre(
         genreId INTEGER PRIMARY KEY AUTO_INCREMENT,
         name VARCHAR
);

CREATE TABLE IF NOT EXISTS filmGenre(
         filmId INTEGER,
         genreId INTEGER,
         PRIMARY KEY (filmId, genreId)
);

CREATE TABLE IF NOT EXISTS MPA(
        rateId INTEGER PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR
);

CREATE TABLE IF NOT EXISTS filmLikes(
         filmId LONG,
         userId LONG,
         PRIMARY KEY (filmId, userId)
);

CREATE TABLE IF NOT EXISTS friends(
        userId LONG,
        friendsId LONG,
        status BOOLEAN,
        PRIMARY KEY (userId, friendsId)
);

create unique index if not exists USER_EMAIL_UINDEX on USERS (email);
create unique index if not exists USER_LOGIN_UINDEX on USERS (login);