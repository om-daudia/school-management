--division table
create table division(
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    division CHAR NOT NULL,
    standard_id INT,
    CONSTRAINT fk_standard
        FOREIGN KEY (standard_id)
        REFERENCES standard(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);