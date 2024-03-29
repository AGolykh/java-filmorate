DROP TABLE IF EXISTS PUBLIC.FEEDS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILM_DIRECTORS CASCADE;
DROP TABLE IF EXISTS PUBLIC.DIRECTORS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILM_GENRES CASCADE;
DROP TABLE IF EXISTS PUBLIC.FRIENDSHIPS CASCADE;
DROP TABLE IF EXISTS PUBLIC.GENRES CASCADE;
DROP TABLE IF EXISTS PUBLIC.LIKES CASCADE;
DROP TABLE IF EXISTS PUBLIC.REVIEW_MARKS CASCADE;
DROP TABLE IF EXISTS PUBLIC.REVIEWS CASCADE;
DROP TABLE IF EXISTS PUBLIC.FILMS CASCADE;
DROP TABLE IF EXISTS PUBLIC.RATING CASCADE;
DROP TABLE IF EXISTS PUBLIC.USERS CASCADE;

CREATE TABLE IF NOT EXISTS PUBLIC.FILMS
(
    FILM_ID      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    FILM_NAME    VARCHAR(255)                                        NOT NULL,
    DESCRIPTION  VARCHAR(255)                                        NOT NULL,
    RELEASE_DATE DATE                                                NOT NULL,
    DURATION     INT                                                 NOT NULL,
    RATING_ID    INTEGER                                             NOT NULL
);

CREATE TABLE IF NOT EXISTS PUBLIC.RATING
(
    RATING_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    RATING_NAME VARCHAR(10)                                         NOT NULL,
    CONSTRAINT FK_FILM_RATING_ID FOREIGN KEY (RATING_ID) REFERENCES PUBLIC.RATING (RATING_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.GENRES
(
    GENRE_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    GENRE_NAME VARCHAR(100)                                        NOT NULL
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_GENRES
(
    FILM_ID  BIGINT NOT NULL,
    GENRE_ID BIGINT NOT NULL,
    UNIQUE (FILM_ID, GENRE_ID),
    CONSTRAINT FK_FILMS_GENRES_FILM_ID FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT FK_FILM_GENRES_GENRES_ID FOREIGN KEY (GENRE_ID) REFERENCES PUBLIC.GENRES (GENRE_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.USERS
(
    USER_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    EMAIL     VARCHAR(255)                                        NOT NULL,
    LOGIN     VARCHAR(255)                                        NOT NULL,
    USER_NAME VARCHAR(255)                                        NOT NULL,
    BIRTHDAY  DATE
);

CREATE TABLE IF NOT EXISTS PUBLIC.FRIENDSHIPS
(
    USER_ID   BIGINT  NOT NULL,
    FRIEND_ID BIGINT  NOT NULL,
    STATUS    BOOLEAN NOT NULL,
    UNIQUE (USER_ID, FRIEND_ID),
    CONSTRAINT FK_FRIENDSHIP_USER_ID FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE,
    CONSTRAINT FK_FRIENDSHIP_FRIEND_ID FOREIGN KEY (FRIEND_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS PUBLIC.LIKES
(
    FILM_ID BIGINT NOT NULL,
    USER_ID BIGINT NOT NULL,
    UNIQUE (FILM_ID, USER_ID),
    CONSTRAINT FK_LIKES_FILM_ID FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT FK_LIKES_USER_ID FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE
);

CREATE TABLE PUBLIC.REVIEWS
(
    REVIEW_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    CONTENT     TEXT                                                NOT NULL,
    IS_POSITIVE BOOLEAN                                             NOT NULL,
    FILM_ID     BIGINT                                              NOT NULL,
    USER_ID     BIGINT                                              NOT NULL,
    UNIQUE (FILM_ID, USER_ID),
    CONSTRAINT FK_REVIEWS_USER_ID FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE,
    CONSTRAINT FK_REVIEWS_FILM_ID FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE
);

CREATE TABLE PUBLIC.REVIEW_MARKS
(
    REVIEW_ID BIGINT  NOT NULL,
    USER_ID   BIGINT  NOT NULL,
    IS_LIKE   BOOLEAN NOT NULL,
    UNIQUE (REVIEW_ID, USER_ID),
    CONSTRAINT FK_REVIEW_MARKS_REVIEW_ID FOREIGN KEY (REVIEW_ID) REFERENCES PUBLIC.REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    CONSTRAINT FK_REVIEW_MARKS_USER_ID FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS (USER_ID) ON DELETE CASCADE
);

CREATE TABLE PUBLIC.FEEDS
(
    EVENT_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    TIMESTAMP  BIGINT                                              NOT NULL,
    USER_ID    BIGINT                                              NOT NULL,
    EVENT_TYPE VARCHAR(10)                                         NOT NULL CHECK (EVENT_TYPE IN ('LIKE', 'REVIEW', 'FRIEND')),
    OPERATION  VARCHAR(10)                                         NOT NULL CHECK (OPERATION IN ('REMOVE', 'ADD', 'UPDATE')),
    ENTITY_ID  BIGINT                                              NOT NULL
);

CREATE TABLE PUBLIC.DIRECTORS
(
    DIRECTOR_ID   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL,
    DIRECTOR_NAME VARCHAR(255)                                        NOT NULL
);

CREATE TABLE PUBLIC.FILM_DIRECTORS
(
    FILM_ID     BIGINT NOT NULL,
    DIRECTOR_ID BIGINT NOT NULL,
    UNIQUE (FILM_ID, DIRECTOR_ID),
    CONSTRAINT FK_FILM_DIRECTORS_FILM_ID FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS (FILM_ID) ON DELETE CASCADE,
    CONSTRAINT FK_FILM_DIRECTORS_DIRECTOR_ID FOREIGN KEY (DIRECTOR_ID) REFERENCES PUBLIC.DIRECTORS (DIRECTOR_ID) ON DELETE CASCADE
);