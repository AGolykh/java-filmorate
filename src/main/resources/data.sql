MERGE INTO "rating_mpa" KEY("rating_id")
VALUES (1, 'G'),
        (2, 'PG'),
        (3, 'PG-13'),
        (4, 'R'),
        (5, 'NC-17');

MERGE INTO "genres" KEY ("genre_id")
VALUES (1,'Комедия'),
       (2,'Драма'),
       (3,'Мультфильм'),
       (4,'Триллер'),
       (5,'Документальный'),
       (6,'Боевик');

/*
MERGE INTO "films" KEY ("film_id")
    VALUES (1, 1,'Евангелион 3.0+1.0', 'Мехи, гиганты и тд', '2021-03-08', 155),
           (2, 5,'Карты, деньги, два ствола', 'Стейтем не бьет морды, ', '1998-08-23', 107),
           (3, 2,'Большой куш', 'Борис Бритва вещает про надежность большого и тяжелого', '2000-08-23', 104),
           (4, 3,'Побег из Шоушенка', 'Бухгалтер Энди Дюфрейн обвинён в убийстве собственной жены', '1994-09-24', 142),
           (5, 4 ,'Аватар', 'Синие голые чуваки бегают по лесу', '2009-12-10', 162);

MERGE INTO "users" KEY ("user_id")
    VALUES (1, 'email@yandex.ru', 'trulala', 'Trexo', '2011-03-08'),
           (2, 'ema@mail.ru', 'login', 'Name', '2001-06-05'),
           (3, 'ema@yahoo.ru', 'loginator', 'SurName', '1988-01-02'),
           (4, 'ail@rambler.ru', 'user34321', 'User', '2021-03-18'),
           (5, 'eml@ms.ru', 'kpoisk', 'Dbnjh', '1994-11-25');

MERGE INTO "film_categories" KEY("film_id", "category_id")
    VALUES (1,   1),
           (1,   19),
           (1,   30),
           (1,   3),
           (1,   9),

           (2,   3),
           (2,   12),
           (2,   15),

           (3,   3),
           (3,   12),
           (3,   15),

           (4, 9),

           (5, 30),
           (5, 3),
           (5, 9),
           (5, 23);
*/


