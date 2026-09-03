CREATE TABLE town (
    id INT PRIMARY KEY,
    name_ko VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    notable_places TEXT[] NOT NULL
);
